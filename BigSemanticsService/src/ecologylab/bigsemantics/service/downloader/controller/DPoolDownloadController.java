package ecologylab.bigsemantics.service.downloader.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.AbstractHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.collecting.SemanticsSite;
import ecologylab.bigsemantics.documentparsers.DocumentParser;
import ecologylab.bigsemantics.downloaderpool.BasicResponse;
import ecologylab.bigsemantics.downloaderpool.DownloaderResult;
import ecologylab.bigsemantics.downloaderpool.MessageScope;
import ecologylab.bigsemantics.downloaders.LocalDocumentCache;
import ecologylab.bigsemantics.downloaders.controllers.DownloadController;
import ecologylab.bigsemantics.filestorage.FileMetadata;
import ecologylab.bigsemantics.filestorage.FileStorageProvider;
import ecologylab.bigsemantics.filestorage.FileSystemStorage;
import ecologylab.bigsemantics.httpclient.BasicResponseHandler;
import ecologylab.bigsemantics.httpclient.HttpClientFactory;
import ecologylab.bigsemantics.httpclient.ModifiedHttpClientUtils;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.bigsemantics.service.logging.ServiceLogRecord;
import ecologylab.concurrent.DownloadableLogRecord;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * Connect to the DPool service for downloading a web page.
 * 
 * @author quyin
 */
public class DPoolDownloadController implements DownloadController
{

  private static Logger            logger;

  private static HttpClientFactory httpClientFactory;

  static
  {
    logger = LoggerFactory.getLogger(DPoolDownloadController.class);
    httpClientFactory = new HttpClientFactory();
  }

  public static int                HTTP_DOWNLOAD_REQUEST_TIMEOUT = 60000;

  private static String            SERVICE_LOCS;

  private static String            SERVICE_LOC;

  public static void setServiceLocs(String serviceLocs)
  {
    SERVICE_LOCS = serviceLocs;
  }

  private static void tryServiceLocs()
  {
    String[] locs = SERVICE_LOCS.split(",");
    BasicResponseHandler handler = new BasicResponseHandler();
    for (String loc : locs)
    {
      logger.info("Trying dpool service at " + loc);
      String testLoc = loc.replace("page/download.xml", "echo/get?msg=TEST");
      AbstractHttpClient client = httpClientFactory.get();
      HttpGet get = new HttpGet(testLoc);
      try
      {
        client.execute(get, handler);
        BasicResponse resp = handler.getResponse();
        if (resp != null && resp.getHttpRespCode() == HttpStatus.SC_OK)
        {
          String content = resp.getContent();
          if (content.contains("TEST"))
          {
            logger.info("Picked dpool service at " + loc);
            SERVICE_LOC = loc;
            return;
          }
        }
      }
      catch (Throwable t)
      {
        logger.error("Cannot locate the download controller!");
      }
      finally
      {
        get.releaseConnection();
      }
    }
  }

  private String           userAgent;

  private DocumentClosure  closure;

  private ParsedURL        location;

  private ParsedURL        redirectedLocation;

  private DownloaderResult result;

  private String           mimeType;

  private boolean          good;

  private InputStream      inputStream;

  public DPoolDownloadController()
  {
    assert SERVICE_LOCS != null;
    if (SERVICE_LOC == null)
    {
      synchronized (SERVICE_LOCS)
      {
        if (SERVICE_LOC == null)
        {
          tryServiceLocs();
        }
      }
    }
  }

  public void setDocumentClosure(DocumentClosure closure)
  {
    this.closure = closure;
  }

  @Override
  public void setUserAgent(String userAgent)
  {
    this.userAgent = userAgent;
  }

  @Override
  public boolean accessAndDownload(ParsedURL location) throws IOException
  {
    this.location = location;

    Document document = closure.getDocument();
    SemanticsGlobalScope semanticScope = document.getSemanticsScope();
    SemanticsSite site = document.getSite();

    ServiceLogRecord logRecord = ServiceLogRecord.DUMMY;
    DownloadableLogRecord downloadableLogRecord = closure.getLogRecord();
    if (downloadableLogRecord instanceof ServiceLogRecord)
    {
      logRecord = (ServiceLogRecord) downloadableLogRecord;
    }

    // FIXME: this should really be: semanticsScope.getPersistentHtmlCache();
    FileStorageProvider storageProvider = FileSystemStorage.getStorageProvider();
    if (!location.isFile())
    {
      long t0Lookup = System.currentTimeMillis();
      String filePath = storageProvider.lookupFilePath(location);
      logRecord.setMsPageCacheLookup(System.currentTimeMillis() - t0Lookup);

      if (filePath == null)
      {
        // not cached, network download:
        logger.info("Not cached: " + location);
        logRecord.setHtmlCacheHit(false);

        result = downloadPage(site, location, userAgent);

        if (result == null)
        {
          logger.error("Failed to download {}: null result from downloadPage()", location);
          return false;
        }
        else if (result.getHttpRespCode() == HttpStatus.SC_OK)
        {
          // handle other locations (e.g. redirects)
          List<String> otherLocations = result.getOtherLocations();
          if (otherLocations != null && otherLocations.size() > 0)
          {
            for (String otherLocation : otherLocations)
            {
              redirectedLocation = ParsedURL.getAbsolute(otherLocation);
              handleRedirectLocation(semanticScope, closure, location, redirectedLocation);
            }
          }

          long t0PageCaching = System.currentTimeMillis();
          String localPath = cacheNewPage(location, storageProvider, result, redirectedLocation);
          logRecord.setMsPageCaching(System.currentTimeMillis() - t0PageCaching);
          if (logRecord != null)
          {
            String urlHash = localPath.substring(localPath.lastIndexOf(File.separatorChar));
            logRecord.setUrlHash(urlHash);
          }

          // set local location
          document.setLocalLocation(ParsedURL.getAbsolute("file://" + localPath));
        }
        else
        {
          logger.error("Failed to download {}: {}", location, result.getHttpRespCode());
          return false;
        }
      }
      else
      {
        // cached:
        logger.info("Cached: " + location);
        logRecord.setHtmlCacheHit(true);

        // document is present in local cache. read meta information as well
        document.setLocalLocation(ParsedURL.getAbsolute("file://" + filePath));

        FileMetadata fileMetadata = storageProvider.getFileMetadata(location);
        if (fileMetadata != null)
        {
          // additional location
          redirectedLocation = fileMetadata.getRedirectedLocation();
          if (redirectedLocation != null)
          {
            logger.debug("Changing {} using redirected location {}", document, redirectedLocation);
            handleRedirectLocation(semanticScope, closure, location, redirectedLocation);
          }

          mimeType = fileMetadata.getMimeType();
        }
        else
        {
          logger.error("Failed to find file meta for " + location);
          return false;
        }
      }
    }

    // irrespective of document origin, its now saved to a local location
    LocalDocumentCache localDocumentCache = new LocalDocumentCache(document);
    long t0ConnectingPageLocalFile = System.currentTimeMillis();
    localDocumentCache.connect();
    PURLConnection purlConn = localDocumentCache.getPurlConnection();
    inputStream = purlConn == null ? null : purlConn.inputStream();
    good = inputStream != null;
    logRecord.setMsPageLocalFileConnecting(System.currentTimeMillis() - t0ConnectingPageLocalFile);

    DocumentParser documentParser = localDocumentCache.getDocumentParser();
    // document parser is set only when URL is local directory
    if (documentParser != null)
    {
      closure.setDocumentParser(documentParser);
    }

    return good;
  }

  private DownloaderResult downloadPage(SemanticsSite site,
                                        ParsedURL origLoc,
                                        String userAgentString)
  {
    Map<String, String> params = new HashMap<String, String>();
    params.put("url", origLoc.toString());
    params.put("agent", userAgentString);
    params.put("int", String.valueOf((int) (site.getMinDownloadInterval() * 1000)));
    params.put("natt", "3");
    params.put("tatt", "60000");
    HttpGet get = ModifiedHttpClientUtils.generateGetRequest(SERVICE_LOC, params);

    AbstractHttpClient client = httpClientFactory.get();
    client.getParams().setParameter("http.connection.timeout", HTTP_DOWNLOAD_REQUEST_TIMEOUT);
    BasicResponseHandler handler = new BasicResponseHandler();
    try
    {
      client.execute(get, handler);
    }
    catch (IOException e)
    {
      logger.error("Error downloading " + origLoc + " using DPool!", e);
    }
    finally
    {
      get.releaseConnection();
    }

    BasicResponse resp = handler.getResponse();
    if (resp != null && resp.getHttpRespCode() == HttpStatus.SC_OK)
    {
      String resultStr = resp.getContent();
      DownloaderResult result = null;
      try
      {
        result = (DownloaderResult) MessageScope.get().deserialize(resultStr, StringFormat.XML);
        assert result != null : "Deserialization results in null!";
        String content = result.getContent();
        logger.info("Service received DPool result for {}: "
                    + "task id: {}, state: {}, status code: {}, content length: {}",
                    origLoc,
                    result.getTaskId(),
                    result.getState(),
                    result.getHttpRespCode(),
                    content == null ? 0 : content.length());
      }
      catch (SIMPLTranslationException e)
      {
        logger.error("Error deserializing DPool result for " + origLoc, e);
      }
      return result;
    }
    else
    {
      logger.error("DPool controller error status when downloading {}: {} {}",
                   origLoc,
                   resp.getHttpRespCode(),
                   resp.getHttpRespMsg());
    }

    return null;
  }

  private String cacheNewPage(ParsedURL origLoc,
                              FileStorageProvider storageProvider,
                              DownloaderResult result,
                              ParsedURL redirectedLocation) throws IOException
  {
    String localPath = null;
    // save to local file
    InputStream ssi = new ByteArrayInputStream(result.getContent().getBytes(Charsets.UTF_8));
    localPath = storageProvider.saveFile(origLoc, ssi);
    FileMetadata fileMetadata = new FileMetadata(origLoc,
                                                 redirectedLocation,
                                                 localPath,
                                                 result.getMimeType(),
                                                 new Date());
    storageProvider.saveFileMetadata(fileMetadata);
    return localPath;
  }

  private void handleRedirectLocation(SemanticsGlobalScope semanticScope,
                                      DocumentClosure documentClosure,
                                      ParsedURL originalPurl,
                                      ParsedURL redirectedLocation)
  {
    Document newDocument = semanticScope.getOrConstructDocument(redirectedLocation);
    newDocument.addAdditionalLocation(originalPurl);
    documentClosure.changeDocument(newDocument);
  }

  @Override
  public boolean isGood()
  {
    return good;
  }

  @Override
  public int getStatus()
  {
    return result == null ? -1 : result.getHttpRespCode();
  }

  @Override
  public String getStatusMessage()
  {
    return result == null ? null : result.getHttpRespMsg();
  }

  @Override
  public ParsedURL getLocation()
  {
    return location;
  }

  @Override
  public ParsedURL getRedirectedLocation()
  {
    return redirectedLocation;
  }

  @Override
  public String getMimeType()
  {
    if (mimeType != null)
    {
      return mimeType;
    }
    return result == null ? null : result.getMimeType();
  }

  @Override
  public String getCharset()
  {
    return result == null ? null : result.getCharset();
  }

  @Override
  public String getHeader(String name)
  {
    throw new RuntimeException("Not implemented.");
  }

  @Override
  public InputStream getInputStream()
  {
    return isGood() ? inputStream : null;
  }

}

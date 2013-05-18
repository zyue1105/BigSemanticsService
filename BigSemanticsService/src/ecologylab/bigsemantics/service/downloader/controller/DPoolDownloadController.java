package ecologylab.bigsemantics.service.downloader.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.UriBuilder;

import com.google.common.base.Charsets;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.collecting.SemanticsSite;
import ecologylab.bigsemantics.documentparsers.DocumentParser;
import ecologylab.bigsemantics.downloaderpool.DownloaderResult;
import ecologylab.bigsemantics.downloaderpool.MessageScope;
import ecologylab.bigsemantics.downloaders.LocalDocumentCache;
import ecologylab.bigsemantics.downloaders.controllers.DownloadController;
import ecologylab.bigsemantics.filestorage.FileMetadata;
import ecologylab.bigsemantics.filestorage.FileStorageProvider;
import ecologylab.bigsemantics.filestorage.FileSystemStorage;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.concurrent.DownloadableLogRecord;
import ecologylab.generic.Debug;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * A download controller that connects to a pool of distributed downloaders. The pool of downloaders
 * is managed as a web service.
 * 
 * @author quyin
 */
public class DPoolDownloadController extends Debug implements DownloadController
{

  public static int     HTTP_DOWNLOAD_REQUEST_TIMEOUT = 45000;
  
  private static String SERVICE_LOCS;

  private static String SERVICE_LOC;

  public static void setServiceLocs(String serviceLocs)
  {
    SERVICE_LOCS = serviceLocs;
  }
  
  private static void tryServiceLocs()
  {
    String[] locs = SERVICE_LOCS.split(",");
    for (String loc : locs)
    {
      debugT(DPoolDownloadController.class, ": trying dpool service at " + loc);
      try
      {
        String testLoc = loc.replace("page/download.xml", "echo/get?msg=TEST");
        Client client = Client.create();
        WebResource r = client.resource(URI.create(testLoc));
        ClientResponse resp = r.get(ClientResponse.class);
        if (resp != null && resp.getStatus() == ClientResponse.Status.OK.getStatusCode())
        {
          String content = resp.getEntity(String.class);
          if (content.contains("TEST"))
          {
            debugT(DPoolDownloadController.class, ": picked dpool service at " + loc);
            SERVICE_LOC = loc;
            return;
          }
        }
      }
      catch (Throwable t)
      {
        // do nothing.
      }
    }
  }

  ConcurrentHashMap<ParsedURL, Boolean> recentlyCached;

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

    recentlyCached = new ConcurrentHashMap<ParsedURL, Boolean>(1000);
  }

  @Override
  public void connect(DocumentClosure documentClosure) throws IOException
  {
    ParsedURL origLoc = documentClosure.getDownloadLocation();
    DownloadableLogRecord logRecord = documentClosure.getLogRecord();
    Document document = documentClosure.getDocument();
    SemanticsGlobalScope semanticScope = document.getSemanticsScope();
    SemanticsSite site = document.getSite();

    FileStorageProvider storageProvider = FileSystemStorage.getStorageProvider();
    String mimeType = null;

    if (!origLoc.isFile())
    {
      // the location is a network address, not a local file address:

      // check if the page is cached
      String filePath = storageProvider.lookupFilePath(origLoc);
      if (filePath == null)
      {
        // not cached, network download:
        logRecord.setHtmlCacheHit(false);
        // htmlCacheLog.debug("Uncached URL: " + originalPURL);

        String userAgentString = document.getMetaMetadata().getUserAgentString();
        DownloaderResult result = downloadPage(site, origLoc, userAgentString);
        if (result.getHttpRespCode() == ClientResponse.Status.OK.getStatusCode())
        {
          // this var is only used for creating fileMetadata. FileMetadata should really
          // contain a list of redirected locations instead of only one.
          // TODO multiple redirects
          ParsedURL redirectLoc = null;

          // handle other locations (e.g. redirects)
          List<String> otherLocations = result.getOtherLocations();
          if (otherLocations != null && otherLocations.size() > 0)
          {
            for (String otherLocation : otherLocations)
            {
              redirectLoc = ParsedURL.getAbsolute(otherLocation);
              setCached(redirectLoc);
              handleRedirectLocation(semanticScope, documentClosure, origLoc, redirectLoc);
            }
          }

          String localPath = cacheNewPage(origLoc, storageProvider, result, redirectLoc);
          if (logRecord != null)
          {
            String urlHash = localPath.substring(localPath.lastIndexOf(File.separatorChar));
            logRecord.setUrlHash(urlHash);
          }

          // set local location
          document.setLocalLocation(ParsedURL.getAbsolute("file://" + localPath));

          // set mime type
          mimeType = result.getMimeType();
        }
        else
        {
          error("failed to download " + origLoc + ": " + result.getHttpRespCode());
        }
      }
      else
      {
        logRecord.setHtmlCacheHit(true);
        // htmlCacheLog.debug("Cached URL[" + originalPURL + "] at " + filePath);

        // document is present in local cache. read meta information as well
        document.setLocalLocation(ParsedURL.getAbsolute("file://" + filePath));

        FileMetadata fileMetadata = storageProvider.getFileMetadata(origLoc);
        if (fileMetadata != null)
        {
          // additional location
          ParsedURL redirectLoc = fileMetadata.getRedirectedLocation();
          if (redirectLoc != null)
          {
            debug("Changing " + document + " using redirected location " + redirectLoc);
            handleRedirectLocation(semanticScope, documentClosure, origLoc, redirectLoc);
          }

          // set mime type
          mimeType = fileMetadata.getMimeType();
        }
      }
    }

    // irrespective of document origin, its now saved to a local location
    LocalDocumentCache localDocumentCache = new LocalDocumentCache(document);
    localDocumentCache.connect();
    PURLConnection purlConnection = localDocumentCache.getPurlConnection();
    DocumentParser documentParser = localDocumentCache.getDocumentParser();

    // set mime type for purl connection
    if (mimeType != null)
      purlConnection.setMimeType(mimeType);
    debug("Setting purlConnection[" + purlConnection.getPurl() + "] to documentClosure");
    documentClosure.setPurlConnection(purlConnection);

    // document parser is set only when URL is local directory
    if (documentParser != null)
      documentClosure.setDocumentParser(documentParser);
  }

  private DownloaderResult downloadPage(SemanticsSite site,
                                        ParsedURL origLoc,
                                        String userAgentString) throws IOException
  {
    try
    {
      Client client = Client.create();
      client.setFollowRedirects(true);
      client.setReadTimeout(HTTP_DOWNLOAD_REQUEST_TIMEOUT);

      UriBuilder ub = UriBuilder.fromUri(SERVICE_LOC);
      ub.queryParam("url", origLoc);
      ub.queryParam("agent", userAgentString);
      ub.queryParam("int", (int) (site.getMinDownloadInterval() * 1000));
      ub.queryParam("natt", 3);
      ub.queryParam("tatt", 60000);
      URI requestUri = ub.build();

      WebResource r = client.resource(requestUri);
      ClientResponse resp = r.get(ClientResponse.class);
      if (resp != null && resp.getStatus() == ClientResponse.Status.OK.getStatusCode())
      {
        // got response from download controller:
        String resultStr = resp.getEntity(String.class);
        DownloaderResult result =
            (DownloaderResult) MessageScope.get().deserialize(resultStr, StringFormat.XML);
        return result;
      }
      else
      {
        error("pool controller error status when downloading " + origLoc + ": " + resp.getStatus());
      }
    }
    catch (SIMPLTranslationException e)
    {
      error("exception when deserializing downloading result for page " + origLoc);
      error("" + e.getStackTrace());
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

  /**
   * This method sets the cache status of the input URL to 'cached'. Note that this status is only a
   * temporary status in memory, for quick deciding if this document has been cached or not without
   * hitting the disk, and does not necessarily reflect the real cache status on disk.
   * 
   * @param url
   */
  private void setCached(ParsedURL url)
  {
    if (url != null)
      recentlyCached.put(url, true);
  }

  @Override
  public boolean isCached(ParsedURL purl)
  {
    if (purl == null)
      return false;
    if (!recentlyCached.containsKey(purl))
    {
      FileStorageProvider storageProvider = FileSystemStorage.getStorageProvider();
      String filePath = storageProvider.lookupFilePath(purl);
      File cachedFile = filePath == null ? null : new File(filePath);
      boolean cached = cachedFile != null && cachedFile.exists();
      Boolean previous = recentlyCached.putIfAbsent(purl, cached);
      if (previous != null)
        cached = previous;
      return cached;
    }
    return recentlyCached.get(purl);
  }

}

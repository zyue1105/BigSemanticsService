package ecologylab.bigsemantics.service.metadata;

import java.io.IOException;
import java.util.Date;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import ecologylab.bigsemantics.collecting.DownloadStatus;
import ecologylab.bigsemantics.filestorage.FileSystemStorage;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.service.SemanticServiceErrorCodes;
import ecologylab.bigsemantics.service.SemanticServiceScope;
import ecologylab.bigsemantics.service.downloader.controller.DPoolDownloadControllerFactory;
import ecologylab.bigsemantics.service.logging.ServiceLogRecord;
import ecologylab.concurrent.DownloadableLogRecord;
import ecologylab.generic.Continuation;
import ecologylab.generic.Debug;
import ecologylab.logging.ILogger;
import ecologylab.logging.ILoggerFactory;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * helper class for metadata.xml and metadata.json
 * 
 * @author ajit
 */
public class MetadataServiceHelper extends Debug implements Continuation<DocumentClosure>
{

  public static int                   CONTINUATION_TIMOUT = 60000;

  static ILogger                      serviceLog;

  static ILogger                      servicePerfLog;

  private static SemanticServiceScope semanticsServiceScope;

  static
  {
    ILoggerFactory loggerFactory = SemanticServiceScope.get().getLoggerFactory();
    serviceLog = loggerFactory.getLogger(MetadataServiceHelper.class);
    servicePerfLog = loggerFactory.getLogger("ecologylab.bigsemantics.service.PERF");
    semanticsServiceScope = SemanticServiceScope.get();
  }

  private Document                    document;

  private ServiceLogRecord            logRecord;

  private Object                      sigDownloadDone     = new Object();

  public MetadataServiceHelper()
  {
    // this.urlSpanMap = new HashMap<String, Integer>();
    this.logRecord = new ServiceLogRecord();
  }

  ServiceLogRecord getServiceLogRecord()
  {
    return logRecord;
  }

  /**
   * The entry method that accepts a URL and returns a Response with extracted metadata.
   * 
   * @param url
   * @param format
   * @param reload
   * @return
   */
  public Response getMetadataResponse(ParsedURL url, StringFormat format, boolean reload)
  {
    logRecord.setBeginTime(new Date());

    Response resp = null;
    document = null;
    getMetadata(url, reload);
    if (document != null)
    {
      try
      {
        long millis = System.currentTimeMillis();
        String responseBody = SimplTypesScope.serialize(document, format).toString();
        logRecord.setmSecInSerialization(System.currentTimeMillis() - millis);
        resp = Response.status(Status.OK).entity(responseBody).build();
      }
      catch (SIMPLTranslationException e)
      {
        // FIXME use logger to capture exception and callstack trace
        e.printStackTrace();
        serviceLog.error("exception while serializing document: %s", e.getMessage());
        resp = Response
            .status(Status.INTERNAL_SERVER_ERROR)
            .entity(SemanticServiceErrorCodes.INTERNAL_ERROR)
            .type(MediaType.TEXT_PLAIN)
            .build();
      }
    }
    else
    {
      serviceLog.error("metadata couldn't be obtained for [%s]", url);
      resp = Response
          .status(Status.NOT_FOUND)
          .entity(SemanticServiceErrorCodes.METADATA_NOT_FOUND)
          .type(MediaType.TEXT_PLAIN)
          .build();
    }

    logRecord.setMsTotal(System.currentTimeMillis() - logRecord.getBeginTime().getTime());
    logRecord.setResponseCode(resp.getStatus());
    servicePerfLog.info(serializeToString(logRecord, StringFormat.JSON));

    return resp;
  }

  Document getMetadata(ParsedURL purl, boolean reload)
  {
    logRecord.setRequestUrl(purl);

    // get or construct the document
    document = semanticsServiceScope.getOrConstructDocument(purl);
    ParsedURL docPurl = document.getLocation();
    logRecord.setDocumentUrl(document.getLocation());
    if (!docPurl.equals(purl))
    {
      serviceLog.info("normalizing %s to %s", purl, docPurl);
    }
    DownloadStatus docStatus = document.getDownloadStatus();
    serviceLog.debug("Download status of %s: %s", document, docStatus);
    if (docStatus == DownloadStatus.DOWNLOAD_DONE)
    {
      logRecord.setDocumentCollectionCacheHit(true);
      serviceLog.debug("%s cached in service local document collection", document);
    }

    // take actions based on the status of the document
    switch (document.getDownloadStatus())
    {
    case UNPROCESSED:
      download(document);
      break;
    case QUEUED:
    case CONNECTING:
    case PARSING:
      DocumentClosure closure = document.getOrConstructClosure(new DPoolDownloadControllerFactory());
      addCallbackAndWaitForDownloadDone(closure);
      break;
    case IOERROR:
    case RECYCLED:
      reload = true;
      // intentionally fall through the next case.
      // the idea is: when the document is in state IOERROR or RECYCLED, it should be reloaded.
    case DOWNLOAD_DONE:
      if (reload)
      {
        removeFromPersistentDocumentCache(docPurl);
        // redownload and parse document
        document = semanticsServiceScope.getOrConstructDocument(purl);
        download(document);
      }
      break;
    }

    // if no_cache is set, remove the document from local document collection
    MetaMetadata mmd = (MetaMetadata) document.getMetaMetadata();
    if (mmd.isNoCache())
    {
      removeFromLocalDocumentCollection(purl);
      removeFromLocalDocumentCollection(docPurl);
    }
    
    return this.document;
  }

  private void download(Document document)
  {
    DocumentClosure closure = document.getOrConstructClosure(new DPoolDownloadControllerFactory());
    try
    {
      synchronized(closure)
      {
        closure.performDownload();
      }

      if (closure.getDownloadStatus() == DownloadStatus.QUEUED
          || closure.getDownloadStatus() == DownloadStatus.CONNECTING
          || closure.getDownloadStatus() == DownloadStatus.PARSING)
      {
        serviceLog.error("closure status is not download done after calling performDownload()!");
      }
      else
      {
        callback(closure);
      }
    }
    catch (IOException e)
    {
      serviceLog.error("error in downloading %s: %s\n%s",
                       document.getLocation(),
                       e.getMessage(),
                       e.getStackTrace());
      e.printStackTrace();
    }
  }
  
  private void addCallbackAndWaitForDownloadDone(DocumentClosure closure)
  {
    if (closure.addContinuationBeforeDownloadDone(this))
    {
      synchronized (sigDownloadDone)
      {
        try
        {
          sigDownloadDone.wait(CONTINUATION_TIMOUT);
        }
        catch (InterruptedException e)
        {
          serviceLog.debug("waiting for download done interrupted.");
        }
      }
    }
    else
    {
      callback(closure);
    }
  }

  @Override
  public synchronized void callback(DocumentClosure incomingClosure)
  {
    Document newDoc = incomingClosure.getDocument();
    if (document != null && document != newDoc)
    {
      serviceLog.debug("remapping old %s to new %s", document, newDoc);
      semanticsServiceScope.getLocalDocumentCollection().remap(document, newDoc);
    }
    document = newDoc;

    DownloadableLogRecord docLogRecord = incomingClosure.getLogRecord();
    if (docLogRecord != null)
      serviceLog.info("%s", serializeToString(docLogRecord, StringFormat.JSON));

    synchronized (sigDownloadDone)
    {
      sigDownloadDone.notifyAll();
    }
  }

  /**
   * @param docPurl
   */
  private void removeFromLocalDocumentCollection(ParsedURL docPurl)
  {
    serviceLog.debug("removing document [%s] from service local document collection", docPurl);
    semanticsServiceScope.getLocalDocumentCollection().remove(docPurl);
  }

  /**
   * @param docPurl
   */
  private void removeFromPersistentDocumentCache(ParsedURL docPurl)
  {
    serviceLog.debug("removing document [%s] from persistent document caches", docPurl);
    semanticsServiceScope.getPersistentDocumentCache().removeDocument(docPurl);
    FileSystemStorage.getStorageProvider().removeFileAndMetadata(docPurl);
  }

  private static String serializeToString(Object obj, StringFormat format)
  {
    try
    {
      return SimplTypesScope.serialize(obj, format).toString();
    }
    catch (SIMPLTranslationException e)
    {
      // FIXME logging
      e.printStackTrace();
      return "Cannot serialize " + obj + " to string: " + e.getMessage();
    }
  }

}

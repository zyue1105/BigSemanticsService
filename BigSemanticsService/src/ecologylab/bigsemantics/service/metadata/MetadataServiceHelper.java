package ecologylab.bigsemantics.service.metadata;

import java.util.Date;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import ecologylab.bigsemantics.downloaders.controllers.DownloadControllerType;
import ecologylab.bigsemantics.filestorage.FileSystemStorage;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.service.SemanticServiceErrorCodes;
import ecologylab.bigsemantics.service.SemanticServiceScope;
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
 * 
 */
public class MetadataServiceHelper extends Debug implements Continuation<DocumentClosure>
{

	public static final long						CONTINUATION_CHECK_INTERVAL	= 1000;

	public static int										CONTINUATION_TIMOUT_CYCLES	= 60;

	static ILogger											serviceLog;

	static ILogger											servicePerfLog;

	private static SemanticServiceScope	semanticsServiceScope;

	static
	{
		ILoggerFactory loggerFactory = SemanticServiceScope.get().getLoggerFactory();
		serviceLog = loggerFactory.getLogger(MetadataServiceHelper.class);
		servicePerfLog = loggerFactory.getLogger("ecologylab.bigsemantics.service.PERF");
		semanticsServiceScope = SemanticServiceScope.get();
		// visitedMetadata = new HashSet<Metadata>();
	}

	/**
	 * main document to be returned corresponding to request url
	 */
	private Document										document;

	private ServiceLogRecord						logRecord;

	private boolean											finished;

	private Object											lockFinished								= new Object();

	public MetadataServiceHelper()
	{
		// this.urlSpanMap = new HashMap<String, Integer>();
		this.logRecord = new ServiceLogRecord();
	}

	private void waitForFinish()
	{
		if (!finished)
		{
			synchronized (lockFinished)
			{
				for (int i = 0; !finished && i < CONTINUATION_TIMOUT_CYCLES; ++i)
				{
					try
					{
						lockFinished.wait(CONTINUATION_CHECK_INTERVAL);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
				if (!finished)
				{
					serviceLog.error("Request timed out%s.", document != null ? document : "");
				}
			}
		}
	}

	private void setFinished(boolean finished)
	{
		synchronized (lockFinished)
		{
			this.finished = finished;
		}
	}

	private void queueDocumentForDownload(Document document)
	{
		DocumentClosure closure = document.getOrConstructClosure(DownloadControllerType.OODSS);
		closure.addContinuation(this);
		closure.setLogRecord(logRecord);
		serviceLog.debug("Queueing %s for downloading.", document);
		closure.queueDownload();
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
		Response resp = null;

		Document document = getMetadata(url, reload);
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
				e.printStackTrace();
				serviceLog.error("exception while serializing document: %s", e.getMessage());
				resp = Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(SemanticServiceErrorCodes.INTERNAL_ERROR).type(MediaType.TEXT_PLAIN).build();
			}
		}
		else
		{
			serviceLog.error("metadata couldn't be obtained for [%s]", url);
			resp = Response.status(Status.SERVICE_UNAVAILABLE)
					.entity(SemanticServiceErrorCodes.METADATA_NOT_FOUND).type(MediaType.TEXT_PLAIN).build();
		}

		logRecord.setMsTotal(System.currentTimeMillis() - logRecord.getBeginTime().getTime());
		logRecord.setResponseCode(resp.getStatus());
		servicePerfLog.info(serializeToString(logRecord, StringFormat.JSON));

		return resp;
	}

	Document getMetadata(ParsedURL url, boolean reload)
	{
		this.finished = false;

		logRecord.setBeginTime(new Date());
		requestMetadata(url, reload);
		waitForFinish();

		return this.document;
	}

	private void requestMetadata(ParsedURL thatPurl, boolean reload)
	{
		logRecord.setRequestUrl(thatPurl);

		// get or construct the document
		Document document = semanticsServiceScope.getOrConstructDocument(thatPurl);
		logRecord.setDocumentUrl(document.getLocation());
		serviceLog
				.debug("Document received from the service scope for URL[%s]: %s", thatPurl, document);
		serviceLog.debug("Download status of %s: %s", document, document.getDownloadStatus());

		// check for <meta_metadata>.no_cache
		MetaMetadata mmd = (MetaMetadata) document.getMetaMetadata();
		reload = reload || mmd == null ? false : mmd.isNoCache();

		// take actions based on the status of the document
		DocumentClosure closure = null;
		ParsedURL docPurl = document.getLocation();
		switch (document.getDownloadStatus())
		{
		case UNPROCESSED:
			queueDocumentForDownload(document);
			break;
		case QUEUED:
		case CONNECTING:
		case PARSING:
			logRecord.setDocumentCollectionCacheHit(true);
			serviceLog.debug("%s has been cached in service global document collection", document);

			serviceLog.debug("adding continuation to the closure of %s", document);
			closure = document.getOrConstructClosure(DownloadControllerType.OODSS);
			closure.addContinuation(this);
			this.document = document;
			break;
		case IOERROR:
		case RECYCLED:
			reload = true;
			// intentionally fall through the next case.
			// the idea is: when the document is in state IOERROR or RECYCLED, it should be reloaded.
		case DOWNLOAD_DONE:
			logRecord.setDocumentCollectionCacheHit(true);
			serviceLog.debug("%s has been cached in service global document collection", document);

			if (reload)
			{
				// remove from caches
				serviceLog.debug("removing document [%s] from service global collection", thatPurl);
				semanticsServiceScope.getGlobalCollection().removed(thatPurl);
				if (!docPurl.equals(thatPurl))
				{
					serviceLog.debug("removing document [%s] from service global collection", docPurl);
					semanticsServiceScope.getGlobalCollection().removed(docPurl);
				}
				serviceLog.debug("removing document [%s] from caches", docPurl);
				semanticsServiceScope.getDBDocumentProvider().removeDocument(docPurl);
				FileSystemStorage.getStorageProvider().removeFileAndMetadata(docPurl);

				// redownload and parse document
				document = semanticsServiceScope.getOrConstructDocument(thatPurl);
				this.document = document;
				queueDocumentForDownload(document);
			}
			else
			{
			  // document is already in global document collection and we don't have to redownload
			  // use it directly and return results immediately
				this.document = document;
				setFinished(true);
			}
			break;
		}
	}

	@Override
	public synchronized void callback(DocumentClosure incomingClosure)
	{
		Document newDoc = incomingClosure.getDocument();
		if (document != null && document != newDoc)
		{
			serviceLog.debug("remapping old %s to new %s", document, newDoc);
			semanticsServiceScope.getGlobalCollection().remap(document, newDoc);
		}
		document = newDoc;
		// if (origDoc != newDoc)
		// {
		// semanticsServiceScope.getGlobalCollection().remap(origDoc, newDoc);
		// }
		// generateSpan(newDoc);

		setFinished(true);

		DownloadableLogRecord docLogRecord = incomingClosure.getLogRecord();
		if (docLogRecord != null)
			serviceLog.info("%s", serializeToString(docLogRecord, StringFormat.JSON));
	}
	
	boolean isFinished()
	{
	  synchronized (lockFinished)
	  {
  	  return finished;
	  }
	}

	ServiceLogRecord getServiceLogRecord()
	{
		return logRecord;
	}

	private static String serializeToString(Object obj, StringFormat format)
	{
		try
		{
			return SimplTypesScope.serialize(obj, format).toString();
		}
		catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
			return "Cannot serialize " + obj + " to string: " + e.getMessage();
		}
	}

	// private static String decodeUrl(String url)
	// {
	// if (url != null)
	// {
	// try
	// {
	// return URLDecoder.decode(url, "UTF-8");
	// }
	// catch (UnsupportedEncodingException e)
	// {
	// e.printStackTrace();
	// }
	// }
	// return url;
	// }

	// /**
	// * used by document iterator
	// */
	// private static HashSet<Metadata> visitedMetadata;

	// private int span;

	// private Document origDoc = null;

	// /**
	// * url to graph level map
	// */
	// private HashMap<String, Integer> urlSpanMap;

	// private void generateSpan(Document document)
	// {
	// // locallocation not required in response
	// document.setLocalLocationMetadata(null);
	//
	// int level = -1;
	// String loc = getLocationInMap(document);
	// if (loc == null)
	// return;
	// else
	// level = urlSpanMap.get(loc);
	//
	// serviceLog.debug("span: " + level + " document: " + document);
	//
	// // set the document that is to be returned
	// if (level == 0)
	// this.document = document;
	//
	// if (level++ < this.span)
	// {
	// MetadataBase md;
	// ClassAndCollectionIterator iter;
	//
	// iter = document.metadataIterator(visitedMetadata);
	// while (iter.hasNext())
	// {
	// md = iter.next();
	// // System.out.println("metadata: " + md);
	// if (md instanceof Document)
	// {
	// Document doc1 = (Document) md;
	// if (doc1.getLocation() != null)
	// requestMetadata(doc1.getLocation(), level);
	// else
	// serviceLog.warn("location is null for document: " + doc1);
	// // don't track linked documents as they get added to GlobalDocumentCollection
	// }
	// }
	// }
	//
	// // notify main thread to return response if queue is empty
	// synchronized (urlSpanMap)
	// {
	// urlSpanMap.remove(loc);
	// if (urlSpanMap.isEmpty())
	// urlSpanMap.notify();
	// }
	// }

	// private String getLocationInMap(Document document)
	// {
	// // check level by getting span from the hashmap
	// String loc = (document instanceof Image) ? ((Image) document).getInternetLocation().toString()
	// : document.getLocation().toString();
	// loc = decodeUrl(loc);
	// if (!urlSpanMap.containsKey(loc))
	// {
	// List<MetadataParsedURL> additionalLocations = document.getAdditionalLocations();
	// if (additionalLocations != null)
	// {
	// for (MetadataParsedURL additionalLocation : additionalLocations)
	// {
	// loc = additionalLocation.getValue().toString();
	// loc = decodeUrl(loc);
	// if (urlSpanMap.containsKey(loc))
	// {
	// return loc;
	// }
	// }
	// }
	//
	// StringBuilder sb = StringBuilderUtils.acquire();
	// for (String existingUrl : urlSpanMap.keySet())
	// sb.append("    ").append(existingUrl).append("\n");
	// String content = sb.toString();
	// StringBuilderUtils.release(sb);
	// serviceLog.warn(document
	// + " location doesn't match with the queued location! recorded locations:\n"
	// + content);
	// return null;
	// }
	// else
	// return loc;
	// }

}

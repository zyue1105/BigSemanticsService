package ecologylab.bigsemantics.service.crawler;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import ecologylab.bigsemantics.collecting.DownloadStatus;
import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.bigsemantics.metadata.MetadataDeserializationHookStrategy;
import ecologylab.bigsemantics.metadata.builtins.CompoundDocument;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.MetaMetadataCollectionField;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.bigsemantics.metametadata.MetaMetadataField;
import ecologylab.generic.Debug;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * Crawls given documents and their linked documents as indicated.
 * 
 * @author quyin
 * 
 */
public class BasicCrawler extends Debug
{

  public static final int TIMEOUT_MS = 60000;

  /**
   * Helper class that is used to record the span (how many levels of linked documents you want to
   * crawl) of each queued documents.
   * 
   * @author quyin
   * 
   */
  private static class DocStub
  {

    public Document doc;

    public boolean  reload;

    public int      span;

    public DocStub(Document doc, boolean reload, int span)
    {
      this.doc = doc;
      this.reload = reload;
      this.span = span;
    }

  }

  private ParsedURL             serviceBaseUrl;

  private SimplTypesScope       metadataTScope;

  private SemanticsSessionScope sss;

  private Client                client;

  private LinkedList<DocStub>   docs;

  private Set<ParsedURL>        seenDocs;

  /**
   * @param serviceBaseUrl
   *          The base URL for the service. This typically ends with <code>metadata.xml</code>.
   *          Currently only the XML service is supported.
   */
  public BasicCrawler(ParsedURL serviceBaseUrl)
  {
    super();
    this.serviceBaseUrl = serviceBaseUrl;

    metadataTScope = RepositoryMetadataTranslationScope.get();
    sss = new SemanticsSessionScope(metadataTScope, null);
    docs = new LinkedList<DocStub>();
    seenDocs = new HashSet<ParsedURL>();

    client = Client.create();
    client.setFollowRedirects(true);
    client.setConnectTimeout(TIMEOUT_MS);
  }

  public ParsedURL getServiceBaseUrl()
  {
    return serviceBaseUrl;
  }

  public int numberOfWaitingDocs()
  {
    return docs.size();
  }

  public void queueDoc(ParsedURL url)
  {
    queueDoc(url, false);
  }

  public void queueDoc(ParsedURL url, boolean reload)
  {
    queueDoc(url, reload, 0);
  }

  /**
   * Queue a document for crawling.
   * 
   * @param url
   *          The location of the document.
   * @param reload
   *          If the service should reload the document instead of using cache.
   * @param span
   *          How many levels of linked documents you want to crawl from this document. 0 indicates
   *          crawling this document only, 1 indicates crawling this document and documents
   *          immediately linked to this document, and so on.
   */
  public void queueDoc(ParsedURL url, boolean reload, int span)
  {
    if (url == null)
      return;

    Document doc = sss.getOrConstructDocument(url);
    url = doc.getLocation();
    if (!seenDocs.contains(url))
    {
      DocStub stub = new DocStub(doc, reload, span);
      docs.offer(stub);
      seenDocs.add(url);
    }
  }

  /**
   * If the crawler has the next document.
   * 
   * @return true if there is still unaccessed documents. Note that for performance considerations,
   *         queued documents are not accessed until nextDoc() is called, so even this method
   *         returns true, the document may be unavailable due to accessibility / network issues.
   */
  public boolean hasNextDoc()
  {
    return docs.size() > 0;
  }

  /**
   * Retrieve the next document.
   * 
   * @return The next crawled document. If unavailable due to accessibility / network issues,
   *         returns null.
   */
  public Document nextDoc()
  {
    if (hasNextDoc())
    {
      DocStub stub = docs.poll();
      ParsedURL url = stub.doc.getLocation();
      if (url != null)
      {
        Document doc = getDocumentFromService(url.toString(), stub.reload);
        stub.doc = doc;
        if (stub.span > 0 && doc != null)
          expandDoc(stub);
        return doc;
      }
    }
    return null;
  }

  private void expandDoc(DocStub stub)
  {
    Document doc = stub.doc;
    boolean reload = stub.reload;
    int linkedDocSpan = stub.span;
    MetaMetadata mmd = (MetaMetadata) doc.getMetaMetadata();

    for (MetaMetadataField field : mmd.getChildMetaMetadata())
    {
      if (field instanceof MetaMetadataCompositeField)
      {
        Object value = field.getMetadataFieldDescriptor().getValue(doc);
        queueLinkedDocIfApplicable(value, reload, linkedDocSpan);
      }
      else if (field instanceof MetaMetadataCollectionField)
      {
        MetaMetadataCollectionField collectionField = (MetaMetadataCollectionField) field;
        if (!collectionField.isCollectionOfScalars())
        {
          Collection<?> collection = field.getMetadataFieldDescriptor().getCollection(doc);
          if (collection != null)
          {
            for (Object item : collection)
              queueLinkedDocIfApplicable(item, reload, linkedDocSpan);
          }
        }
      }
    }
  }

  private void queueLinkedDocIfApplicable(Object linkedDoc, boolean reload, int linkedDocSpan)
  {
    if (linkedDoc != null
        && linkedDoc instanceof CompoundDocument
        && !linkedDoc.getClass().equals(CompoundDocument.class))
    {
      ParsedURL linkedDocLoc = ((Document) linkedDoc).getLocation();
      if (linkedDocLoc != null)
        queueDoc(linkedDocLoc, reload, linkedDocSpan);
    }
  }

  /**
   * Get the Document object given a URL from the semantics service, using the XML format.
   * 
   * @param url
   * @param reload
   * @return the Document object when everything goes well, or null when something is wrong.
   */
  private Document getDocumentFromService(String url, boolean reload)
  {
    String encodedUrl = null;
    try
    {
      encodedUrl = URLEncoder.encode(url, "UTF8");
    }
    catch (UnsupportedEncodingException e1)
    {
      error("Cannot encode URL [" + url + "]");
      e1.printStackTrace();
      return null;
    }

    String reqUrl = String.format("%s?url=%s%s",
                                  serviceBaseUrl.toString(),
                                  encodedUrl,
                                  reload ? "&reload=true" : "");
    String serial = null;
    try
    {
      debug("Accessing " + url + " using request " + reqUrl);
      WebResource resource = client.resource(reqUrl);
      ClientResponse resp = resource == null ? null : resource.get(ClientResponse.class);
      serial = resp == null ? null : resp.getEntity(String.class);
    }
    catch (Throwable e2)
    {
      error("Network operation failed for [" + reqUrl + "]");
      e2.printStackTrace();
    }

    if (serial != null && serial.length() > 0)
    {
      // debug("serialized form:\n" + serial);
      Object obj = null;
      try
      {
        obj = metadataTScope.deserialize(serial,
                                         new MetadataDeserializationHookStrategy(sss),
                                         StringFormat.XML);
      }
      catch (SIMPLTranslationException e3)
      {
        error("Malformed or invalid XML returned from the service, request URL: [" + reqUrl + "]");
        e3.printStackTrace();
      }

      if (obj != null && obj instanceof Document)
      {
        Document doc = (Document) obj;
        if (doc.getDownloadStatus() != DownloadStatus.RECYCLED)
          return doc;
      }
    }

    return null;
  }

}

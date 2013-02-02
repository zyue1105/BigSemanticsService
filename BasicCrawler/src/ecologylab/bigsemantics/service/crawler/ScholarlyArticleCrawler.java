package ecologylab.bigsemantics.service.crawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.bigsemantics.generated.library.bibManaging.CiteseerxCiting;
import ecologylab.bigsemantics.generated.library.bibManaging.CiteseerxSimilar;
import ecologylab.bigsemantics.generated.library.bibManaging.CiteseerxSummary;
import ecologylab.bigsemantics.generated.library.creative_work.Author;
import ecologylab.bigsemantics.generated.library.creative_work.CreativeWork;
import ecologylab.bigsemantics.generated.library.ieeeXplore.IeeeXploreArticle;
import ecologylab.bigsemantics.generated.library.ieeeXplore.IeeeXploreCitations;
import ecologylab.bigsemantics.generated.library.ieeeXplore.IeeeXploreReferences;
import ecologylab.bigsemantics.generated.library.ieeeXplore.IeeeXploreSearch;
import ecologylab.bigsemantics.generated.library.scholarlyArticle.ScholarlyArticle;
import ecologylab.bigsemantics.generated.library.scienceDirect.ScienceDirectArticle;
import ecologylab.bigsemantics.generated.library.scienceDirect.ScopusArticle;
import ecologylab.bigsemantics.generated.library.scienceDirect.ScopusAuthor;
import ecologylab.bigsemantics.generated.library.scienceDirect.ScopusSearch;
import ecologylab.bigsemantics.metadata.MetadataDeserializationHookStrategy;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.service.crawler.ScholarlyArticleCrawler.Report.Level;
import ecologylab.generic.Debug;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * Expand scholarly articles using authors, references, and citations.
 * 
 * @author quyin
 */
public class ScholarlyArticleCrawler extends Debug
{

  static final int      TIMEOUT_MS    = 300000;

  /**
   * This program uses the semantics service so that HTML pages can be cached on the server.
   */
  static String         xmlServiceUrl = "http://ecology-service/ecologylabSemanticService/metadata.xml";

  /**
   * The Jersey client.
   */
  Client                client;

  /**
   * The metadata types scope.
   */
  SimplTypesScope       metadataTScope;

  /**
   * The semantics session scope. Used to deserialize service returned XML into semantic objects.
   */
  SemanticsSessionScope sss;

  /**
   * Used to prevent infinite loops.
   */
  Set<String>           accessedUrls;

  /**
   * Represents an item in the final report.
   * 
   * @author quyin
   */
  static class Report
  {

    static enum Level
    {
      IMPROVE, FIX
    }

    /**
     * Severity of this report.
     */
    Level  level;

    /**
     * Involved URL.
     */
    String url;

    /**
     * The message.
     */
    String msg;

    Report(Level level, String url, String msg)
    {
      this.level = level;
      this.url = url;
      this.msg = msg;
    }

  }

  /**
   * Buffer for all reports. They will be printed again in the end of execution.
   */
  List<Report>        reports;

  /**
   * Buffer for BFS.
   */
  LinkedList<DocStub> queuedArticles;

  /**
   * All use default settings.
   * 
   * @throws IOException
   */
  public ScholarlyArticleCrawler(Properties props) throws IOException
  {
    client = Client.create();
    client.setFollowRedirects(true);
    client.setConnectTimeout(TIMEOUT_MS);

    metadataTScope = RepositoryMetadataTranslationScope.get();

    if (props == null)
    {
      sss = new SemanticsSessionScope(metadataTScope, CybernekoWrapper.class);
    }
    else
    {
      File repositoryLocation = new File(props.getProperty("repository_dir"));
      sss = new SemanticsSessionScope(repositoryLocation, metadataTScope, CybernekoWrapper.class);
    }
    
    accessedUrls = new HashSet<String>();

    reports = new ArrayList<Report>();
  }

  /**
   * Convenience structure used in BFS queue.
   * 
   * @author quyin
   */
  static class DocStub
  {

    int      generation;

    Document doc;

    boolean  downloaded;

    DocStub(int generation, Document article)
    {
      this.generation = generation;
      this.doc = article;
      this.downloaded = false;
    }

  }

  /**
   * Expand a list of URLs to scholarly articles, given a maximum level.
   * 
   * @param seedUrls
   * @param maxGeneration
   */
  public void crawl(List<String> seedUrls, int maxGeneration)
  {
    queuedArticles = new LinkedList<DocStub>();

    // init
    for (String seedUrl : seedUrls)
    {
      if (seedUrl != null && seedUrl.length() > 0)
      {
        Document doc = sss.getOrConstructDocument(ParsedURL.getAbsolute(seedUrl));
        if (doc instanceof ScholarlyArticle)
          queuedArticles.add(new DocStub(1, (ScholarlyArticle) doc));
      }
    }

    long startTime = System.currentTimeMillis();
    int n = 1;

    // BFS
    while (queuedArticles.size() > 0)
    {
      DocStub stub = queuedArticles.removeFirst();

      long currentTime = System.currentTimeMillis();
      int totalSec = (int) ((currentTime - startTime) / 1000);
      int min = totalSec / 60;
      int sec = totalSec % 60;
      debug(String.format("Processing #%8d [Generation: %d], Waiting: %8d, Time elapsed: %d:%d",
                          n++,
                          stub.generation,
                          queuedArticles.size(),
                          min,
                          sec));

      if (!stub.downloaded && stub.doc != null)
      {
        ParsedURL location = stub.doc.getLocation();
        String url = location == null ? null : location.toString();
        if (url != null)
        {
          ScholarlyArticle article = getScholarlyArticleFromService(url);
          stub.doc = article;
          if (article != null)
            try
            {
              crawlScholarlyArticle(article);
            }
            catch (Throwable e)
            {
              reportFix(url, "Exception during expanding article");
              e.printStackTrace();
            }
          stub.downloaded = stub.doc != null;
        }
      }

      if (stub.generation < maxGeneration && stub.doc != null)
      {
        if (stub.downloaded && validateScholarlyArticle(stub.doc))
        {
          ScholarlyArticle article = (ScholarlyArticle) stub.doc;
          List<Author> authors = article.getAuthors();
          for (Author author : authors)
          {
            List<CreativeWork> creativeWorks = author.getCreativeWorks();
            crawlDocList(creativeWorks, stub.generation);
          }

          List<Document> references = article.getReferences();
          crawlDocList(references, stub.generation);

          List<CreativeWork> citations = article.getCitations();
          crawlDocList(citations, stub.generation);
        }
      }
    }
  }

  /**
   * Helper method that expands a list of documents.
   * 
   * @param docList
   * @param generation
   */
  <T extends Document> void crawlDocList(List<T> docList, int generation)
  {
    if (docList == null)
      return;

    for (T doc : docList)
    {
      ParsedURL loc = doc == null ? null : doc.getLocation();
      if (loc != null)
      {
        String url = loc.toString();
        if (accessedUrls.contains(url))
          continue;

        Document newDoc = sss.getOrConstructDocument(loc);
        queuedArticles.add(new DocStub(generation + 1, newDoc));
      }
    }
  }

  /**
   * Additional operations required to expand a scholarly article. For those not confirming to the
   * standard format of a scholarly article, this method uses specific fields to expand it
   * correctly.
   * 
   * @param article
   */
  void crawlScholarlyArticle(ScholarlyArticle article)
  {
    if (article instanceof CiteseerxSummary)
    {
      CiteseerxSummary typed = (CiteseerxSummary) article;

      {
        typed.setCitedArticles(tryCrawling(typed.getCitedArticles()));
        Document citedArticlesPage = typed.getCitedArticles();
        if (citedArticlesPage != null && citedArticlesPage instanceof CiteseerxCiting)
        {
          CiteseerxCiting citingPage = (CiteseerxCiting) citedArticlesPage;
          List<CiteseerxSummary> citings = citingPage.getSearchResults();
          if (citings != null)
            article.citations().addAll(citings);
        }
      }

      {
        typed
            .setArticlesThatCiteTheSameWorks(tryCrawling(typed.getArticlesThatCiteTheSameWorks()));
        Document citeSamePage = typed.getArticlesThatCiteTheSameWorks();
        if (citeSamePage != null && citeSamePage instanceof CiteseerxSimilar)
        {
          CiteseerxSimilar citeSameObj = (CiteseerxSimilar) citeSamePage;
          List<CreativeWork> citeSameArticles = citeSameObj.getCitations();
          if (citeSameArticles != null)
            article.citations().addAll(citeSameArticles);
        }
      }

      {
        typed.setArticlesCitedByTheSameWorks(tryCrawling(typed.getArticlesCitedByTheSameWorks()));
        Document bySamePage = typed.getArticlesCitedByTheSameWorks();
        if (bySamePage != null && bySamePage instanceof CiteseerxSimilar)
        {
          CiteseerxSimilar bySameObj = (CiteseerxSimilar) bySamePage;
          List<CreativeWork> bySameArticles = bySameObj.getCitations();
          if (bySameArticles != null)
            article.citations().addAll(bySameArticles);
        }
      }
    }
    else if (article instanceof ScienceDirectArticle)
    {
      ScienceDirectArticle typed = (ScienceDirectArticle) article;

      {
        List<Author> authors = typed.getAuthors();
        if (authors != null)
          for (Author author : authors)
          {
            if (author != null && author.getLocation() != null)
            {
              Document authorPage = getDocumentFromService(author.getLocation().toString());
              if (authorPage != null && authorPage instanceof ScopusAuthor)
              {
                ScopusAuthor scopusAuthor = (ScopusAuthor) authorPage;
                ScopusSearch articlesByAuthorPage = scopusAuthor.getArticlesByAuthorPage();
                if (articlesByAuthorPage != null && articlesByAuthorPage.getLocation() != null)
                {
                  Document byAuthorDoc = getDocumentFromService(articlesByAuthorPage.getLocation()
                      .toString());
                  if (byAuthorDoc != null && byAuthorDoc instanceof ScopusSearch)
                  {
                    ScopusSearch byAuthor = (ScopusSearch) byAuthorDoc;
                    if (byAuthor.getSearchResults() != null)
                      author.creativeWorks().addAll(articlesByAuthorPage.getSearchResults());
                  }
                }
              }
            }
          }
      }

      {
        ScopusSearch citationsPage = typed.getCitationsPage();
        if (citationsPage != null && citationsPage.getLocation() != null)
        {
          Document citationsPageDoc = getDocumentFromService(citationsPage.getLocation().toString());
          if (citationsPageDoc != null && citationsPageDoc instanceof ScopusSearch)
          {
            ScopusSearch scopusCitations = (ScopusSearch) citationsPageDoc;
            List<ScopusArticle> citations = scopusCitations.getSearchResults();
            if (citations != null)
              typed.citations().addAll(citations);
          }
        }
      }
    }
    else if (article instanceof IeeeXploreArticle)
    {
      IeeeXploreArticle typed = (IeeeXploreArticle) article;

      {
        List<IeeeXploreSearch> articlesByAuthors = typed.getArticlesByAuthors();
        if (articlesByAuthors != null)
          for (int i = 0; i < articlesByAuthors.size(); ++i)
          {
            IeeeXploreSearch articles = articlesByAuthors.get(i);
            if (articles != null && articles.getTitle() != null)
            {
              Author author = new Author();
              author.setTitle(articles.getTitle());
              if (articles.getLocation() != null)
              {
                Document articlesDoc = getDocumentFromService(articles.getLocation().toString());
                if (articlesDoc != null && articlesDoc instanceof IeeeXploreSearch)
                {
                  IeeeXploreSearch accessedArticles = (IeeeXploreSearch) articlesDoc;
                  if (accessedArticles.getSearchResults() != null)
                    author.creativeWorks().addAll(accessedArticles.getSearchResults());
                }
              }
              typed.authors().add(author);
            }
          }
      }

      {
        IeeeXploreReferences referencesPage = typed.getReferencesPage();
        if (referencesPage != null && referencesPage.getLocation() != null)
        {
          Document referencesDoc = getDocumentFromService(referencesPage.getLocation().toString());
          if (referencesDoc != null && referencesDoc instanceof IeeeXploreReferences)
          {
            IeeeXploreReferences references = (IeeeXploreReferences) referencesDoc;
            if (references.getReferences() != null)
              typed.references().addAll(references.getReferences());
          }
        }
      }

      {
        IeeeXploreCitations citationsPage = typed.getCitationsPage();
        if (citationsPage != null && citationsPage.getLocation() != null)
        {
          Document citationsDoc = getDocumentFromService(citationsPage.getLocation().toString());
          if (citationsDoc != null && citationsDoc instanceof IeeeXploreCitations)
          {
            IeeeXploreCitations citations = (IeeeXploreCitations) citationsDoc;
            if (citations.getCitations() != null)
              typed.citations().addAll(citations.getCitations());
          }
        }
      }
    }
  }

  /**
   * Helper method that tries to get an expanded (downloaded and extracted) form of the input doc.
   * If anything goes wrong, return the untouched document.
   * 
   * @param doc
   * @return
   */
  <T extends Document> T tryCrawling(T doc)
  {
    if (doc != null)
    {
      ParsedURL loc = doc.getLocation();
      String url = loc == null ? null : loc.toString();
      if (url != null)
      {
        Document expanded = getDocumentFromService(url);
        if (expanded != null && doc.getClass().isAssignableFrom(expanded.getClass()))
          return (T) expanded;
      }
    }
    return doc;
  }

  /**
   * Validate a scholarly article. Requires a scholarly article to have title and authors. Missing
   * references or citations will be reported but does not count towards validation.
   * 
   * @param downloadedDoc
   * @return true if the article is valid, otherwise false.
   */
  boolean validateScholarlyArticle(Document downloadedDoc)
  {
    if (!(downloadedDoc instanceof ScholarlyArticle))
    {
      return false;
    }
    ScholarlyArticle article = (ScholarlyArticle) downloadedDoc;

    // title and authors are necessary information
    if (article.getTitle() == null || article.getTitle().length() == 0)
    {
      return false;
    }
    if (article.getAuthors() == null || article.getAuthors().size() == 0)
    {
      return false;
    }

    // references and citations are not necessary (some publishers may lack these pieces of
    // information), but we would like to have them when possible, thus report.
    String url = downloadedDoc.getLocation().toString();
    if (article.getReferences() == null || article.getReferences().size() == 0)
    {
      reportImprove(url, "No references");
    }
    if (article.getCitations() == null || article.getCitations().size() == 0)
    {
      reportImprove(url, "No citations");
    }

    return true;
  }

  /**
   * Get the ScholarlyArticle object from the service using the input URL. If there is an error, or
   * the returned Document is not a ScholarlyArticle, returns null.
   * 
   * @param url
   * @return
   */
  ScholarlyArticle getScholarlyArticleFromService(String url)
  {
    Document doc = getDocumentFromService(url);
    if (doc != null && doc instanceof ScholarlyArticle)
    {
      ScholarlyArticle article = (ScholarlyArticle) doc;
      List<Author> authors = article.getAuthors();
      if (authors != null)
      {
        for (int i = 0; i < authors.size(); ++i)
        {
          Author author = authors.get(i);
          ParsedURL loc = author.getLocation();
          String locUrl = loc == null ? null : filterLocation(loc);
          if (locUrl != null && !accessedUrls.contains(locUrl))
          {
            Document expandedAuthor = getDocumentFromService(locUrl);
            if (expandedAuthor instanceof Author)
              authors.set(i, (Author) expandedAuthor);
          }
        }
      }
      return article;
    }
    else
    {
      reportFix(url, "Wrapper missing");
      return null;
    }
  }

  /**
   * Helper method that does location filtering.
   * 
   * @param loc
   * @return
   */
  String filterLocation(ParsedURL loc)
  {
    Document doc = sss.getOrConstructDocument(loc);
    return doc == null ? null : doc.getLocation().toString();
  }

  /**
   * Get the Document object given a URL from the semantics service, using the XML format.
   * 
   * @param url
   * @return the Document object when everything goes well, or null when something is wrong.
   */
  Document getDocumentFromService(String url)
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

    String reqUrl = String.format("%s?url=%s", xmlServiceUrl, encodedUrl);
    String serial = null;
    try
    {
      debug("Accessing " + url);
      WebResource resource = client.resource(reqUrl);
      accessedUrls.add(url);
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
        checkForBanningAccess(doc);
        return doc;
      }
    }

    reportFix(url, "Unprocessed URL");
    return null;
  }

  void checkForBanningAccess(Document doc)
  {
    if (doc == null)
      return;

    try
    {
      if ("503 - Service Temporarily Unavailable".equals(doc.getTitle()))
      {
        Thread.sleep(1000L * 60 * 60 * 4);
      }
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Report a place that can be improved.
   * 
   * @param url
   * @param msg
   */
  void reportImprove(String url, String msg)
  {
    report(Level.IMPROVE, url, msg);
  }

  /**
   * Report a place that must be fixed.
   * 
   * @param url
   * @param msg
   */
  void reportFix(String url, String msg)
  {
    report(Level.FIX, url, msg);
  }

  /**
   * The real method for reporting.
   * 
   * @param level
   * @param url
   * @param msg
   */
  private void report(Level level, String url, String msg)
  {
    debug(String.format("Report: %8s : [%s] : %s.", level, url, msg));
    reports.add(new Report(level, url, msg));
  }

  /**
   * Output all reports during the execution.
   */
  public void outputReports()
  {
    System.out.format("\n\n\n");

    int maxMsgLen = 8;
    for (Report report : reports)
      if (report.msg.length() > maxMsgLen)
        maxMsgLen = report.msg.length();

    String fmt = String.format("%%8s: %%%ds: %%s\n", maxMsgLen);
    for (Report report : reports)
    {
      System.out.format(fmt, report.level, report.msg, report.url);
    }
  }

  public static void main(String[] args) throws SIMPLTranslationException, IOException
  {
    if (args.length < 1)
    {
      System.err.println("args: <property-file>");
      System.exit(-1);
    }
    
    Properties props = new Properties();
    props.load(new FileInputStream(args[0]));
    ScholarlyArticleCrawler sae = new ScholarlyArticleCrawler(props);
    
    List<String> seedUrls = new ArrayList<String>();
    seedUrls.add("http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.31.1768");
    seedUrls.add("http://ieeexplore.ieee.org/xpl/articleDetails.jsp?arnumber=1532126");
    seedUrls.add("http://ieeexplore.ieee.org/xpl/articleDetails.jsp?arnumber=1208999");
    seedUrls.add("http://www.sciencedirect.com/science/article/pii/S1570826805000089");
    seedUrls.add("http://dl.acm.org/citation.cfm?id=1795387&preflayout=flat");
    seedUrls.add("http://dl.acm.org/citation.cfm?id=1148196&preflayout=flat");
    seedUrls.add("http://dl.acm.org/citation.cfm?id=1645966&preflayout=flat");
    seedUrls.add("http://dl.acm.org/citation.cfm?id=1835572&preflayout=flat");
    seedUrls.add("http://dl.acm.org/citation.cfm?id=1835621&preflayout=flat");
    seedUrls.add("http://dl.acm.org/citation.cfm?id=1871745&preflayout=flat");
    seedUrls.add("http://dl.acm.org/citation.cfm?id=1646146&preflayout=flat");
    seedUrls.add("http://dl.acm.org/citation.cfm?id=313121&preflayout=flat");
    seedUrls.add("http://dl.acm.org/citation.cfm?id=2034731&preflayout=flat");
    seedUrls.add("http://dl.acm.org/citation.cfm?id=564440&preflayout=flat");

    sae.crawl(seedUrls, 6);
    sae.outputReports();
  }

}

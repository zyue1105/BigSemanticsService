package ecologylab.bigsemantics.service.crawler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.net.ParsedURL;

/**
 * Verifies the basic behaviors and error handling of BasicCrawler.
 * 
 * @author quyin
 *
 */
public class TestBasicCrawler
{

  ParsedURL    serviceBaseUrl;

  BasicCrawler crawler;

  @Before
  public void setup()
  {
    serviceBaseUrl = ParsedURL.getAbsolute("http://ecoarray0.cs.tamu.edu:2080/BigSemanticsService/metadata.xml");
    crawler = new BasicCrawler(serviceBaseUrl);
  }

  @Test
  public void testCreation()
  {
    assertEquals(0, crawler.numberOfWaitingDocs());
    assertEquals(serviceBaseUrl, crawler.getServiceBaseUrl());
  }

  @Test
  public void testQueueOneDoc()
  {
    ParsedURL url = ParsedURL.getAbsolute("http://www.amazon.com/Unit-Testing-Java-Engineering-Programming/dp/1558608680");
    crawler.queueDoc(url, true);
    assertEquals(1, crawler.numberOfWaitingDocs());
  }

  @Test
  public void testCrawlOneDocWithoutExpanding()
  {
    ParsedURL url = ParsedURL.getAbsolute("http://www.amazon.com/Unit-Testing-Java-Engineering-Programming/dp/1558608680");
    crawler.queueDoc(url, true);

    assertTrue(crawler.hasNextDoc());
    Document doc = crawler.nextDoc();
    assertNotNull(doc);
    assertEquals(url, doc.getLocation());
    assertFalse(crawler.hasNextDoc());

    assertEquals(0, crawler.numberOfWaitingDocs());
  }

  @Test
  public void testQueueVisitedDocs()
  {
    ParsedURL url = ParsedURL.getAbsolute("http://www.amazon.com/Unit-Testing-Java-Engineering-Programming/dp/1558608680");
    crawler.queueDoc(url, true);
    assertEquals(1, crawler.numberOfWaitingDocs());

    crawler.queueDoc(url, true);
    assertEquals(1, crawler.numberOfWaitingDocs());
  }

  @Test
  public void testQueueVisitedByAdditionalLocationsDocs()
  {
    ParsedURL url1 = ParsedURL.getAbsolute("http://www.amazon.com/Unit-Testing-Java-Engineering-Programming/dp/1558608680");
    crawler.queueDoc(url1, true);
    assertEquals(1, crawler.numberOfWaitingDocs());

    ParsedURL url2 = ParsedURL.getAbsolute("http://www.amazon.com/Unit-Testing-Java-Engineering-Programming/dp/1558608680/ref=abc");
    crawler.queueDoc(url2, true);
    assertEquals(1, crawler.numberOfWaitingDocs());

    assertTrue(crawler.hasNextDoc());
    Document doc1 = crawler.nextDoc();
    assertNotNull(doc1);
    assertFalse(crawler.hasNextDoc());
  }

  @Test
  public void testCrawlOneDocWithExpanding()
  {
    ParsedURL url = ParsedURL.getAbsolute("http://www.amazon.com/Unit-Testing-Java-Engineering-Programming/dp/1558608680");
    crawler.queueDoc(url, true, 1);
    assertEquals(1, crawler.numberOfWaitingDocs());

    assertTrue(crawler.hasNextDoc());
    Document doc1 = crawler.nextDoc();
    assertNotNull(doc1);
    System.out.println("Actual number of waiting docs: " + crawler.numberOfWaitingDocs());
    assertTrue(crawler.numberOfWaitingDocs() > 1);
  }

  @Test
  public void testQueueNull()
  {
    crawler.queueDoc(null, true);
    crawler.queueDoc(null, true, 1);
    assertEquals(0, crawler.numberOfWaitingDocs());
    assertFalse(crawler.hasNextDoc());
  }

  @Test
  public void testQueueInaccessibleUrl()
  {
    ParsedURL url = ParsedURL.getAbsolute("http://invalid.invalid");
    crawler.queueDoc(url, true);
    // before it is accessed, we don't know if it is accessible
    assertEquals(1, crawler.numberOfWaitingDocs());
    assertTrue(crawler.hasNextDoc());
    Document nextDoc = crawler.nextDoc();
    // now we know that it is inaccessible
    assertNull(nextDoc);
    assertFalse(crawler.hasNextDoc());
  }

}

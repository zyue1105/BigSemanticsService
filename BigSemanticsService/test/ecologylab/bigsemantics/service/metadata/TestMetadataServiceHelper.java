package ecologylab.bigsemantics.service.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import ecologylab.bigsemantics.generated.library.product_and_service.AmazonProduct;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.service.logging.ServiceLogRecord;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * 
 * @author quyin
 *
 */
public class TestMetadataServiceHelper
{

  private MetadataServiceHelper msh;

  @Before
  public void init()
  {
    // TODO computationally mimic downloader starting with an empty cache.
    // currently we must purge the cache and start the downloader ourselves!
    msh = new MetadataServiceHelper();
  }

  @Test
  public void testConstruction()
  {
    // empty
  }

  @Test(timeout = 30000)
  public void testGettingSingleUncachedDocument()
  {
    ParsedURL purl = ParsedURL
        .getAbsolute("http://www.amazon.com/Washburn-Series-WG35SCE-Acoustic-Electric/dp/B003EYV89Q/");
    Document doc = msh.getMetadata(purl, false);
    assertNotNull(doc);
    assertTrue(doc instanceof AmazonProduct);
    assertNotNull(doc.getTitle());
    assertTrue(doc.getTitle().length() > 0);
  }

  @Test(timeout = 60000)
  public void testGettingCachedDocument()
  {
    ParsedURL purl = ParsedURL.getAbsolute("http://www.amazon.com/Seagull-S6-Original-Acoustic-Guitar/dp/B000RW0GT6/");
    Document doc = msh.getMetadata(purl, false);
    for (int i = 0; i < 50; ++i)
    {
      sleep(100);
      doc = msh.getMetadata(purl, false);
      assertNotNull(doc);
      assertTrue(doc instanceof AmazonProduct);
      assertNotNull(doc.getTitle());
      assertTrue(doc.getTitle().length() > 0);
    }
  }

  private class Runner implements Runnable
  {

    private int                    index;

    private ParsedURL              purl;

    private Map<Integer, Document> results;

    public Runner(int index, ParsedURL purl, Map<Integer, Document> results)
    {
      this.index = index;
      this.purl = purl;
      this.results = results;
    }

    @Override
    public void run()
    {
      Document doc = msh.getMetadata(purl, false);
      results.put(index, doc);
    }

  }

  @Test(timeout = 60000)
  public void testMultipleRequestsForSameDocument() throws InterruptedException
  {
    final ParsedURL purl = ParsedURL.getAbsolute("http://www.amazon.com/Yamaha-FG700S-Acoustic-Guitar/dp/B000FIZISQ/");
    final Map<Integer, Document> docs = new ConcurrentHashMap<Integer, Document>();
    int n = 10;
    ExecutorService exec = Executors.newFixedThreadPool(n);
    for (int i = 0; i < n; ++i)
    {
      exec.submit(new Thread(new Runner(i, purl, docs)));
    }
    exec.shutdown();
    exec.awaitTermination(50000, TimeUnit.MILLISECONDS);
    assertEquals(n, docs.size());
    for (int i = 0; i < n; ++i)
    {
      Document doc = docs.get(i);
      assertNotNull(doc);
      assertTrue(doc instanceof AmazonProduct);
      assertNotNull(doc.getTitle());
      assertTrue(doc.getTitle().length() > 0);
    }
  }

  static void sleep(long time)
  {
    try
    {
      Thread.sleep(time);
    }
    catch (InterruptedException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  @Test(timeout = 60000)
  public void testServiceLogRecordBeingGeneratedCorrectly()
  {
    ParsedURL purl = ParsedURL.getAbsolute("http://www.amazon.com/Musicians-Gear-Tubular-Guitar-Stand/dp/B0018TIADQ/");
    msh.getMetadataResponse(purl, StringFormat.XML, false);
    
    ServiceLogRecord log = msh.getServiceLogRecord();
    assertNotNull(log.getBeginTime());
    assertTrue(log.getMsTotal() > 0);
    // assertNotNull(log.getRequesterIp());
    assertNotNull(log.getRequestUrl());
    assertTrue(log.getResponseCode() > 0);
    
    assertNotNull(log.getDocumentUrl());
    assertTrue(log.getmSecInHtmlDownload() > 0);
    assertTrue(log.getmSecInExtraction() > 0);
    assertTrue(log.getmSecInSerialization() > 0);
    
    assertNotNull(log.getQueuePeekIntervals());
    assertTrue(log.getQueuePeekIntervals().size() > 0);
    assertTrue(log.getEnQueueTimestamp() > 0);
    assertNotNull(log.getUrlHash());
    
    // for easier debugging:
    System.out.println();
    System.out.println();
    SimplTypesScope.serializeOut(log, "", StringFormat.XML);
  }

}

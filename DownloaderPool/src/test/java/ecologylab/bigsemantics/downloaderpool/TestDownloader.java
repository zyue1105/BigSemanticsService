package ecologylab.bigsemantics.downloaderpool;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TestDownloader
{

  MockDownloader d;

  @Before
  public void init()
  {
    d = new MockDownloader();
    d.sleepBetweenLoop = 100;
  }

  @Test
  public void testConstruction()
  {
    assertNotNull(d);
    assertNotNull(d.downloadMonitor);
  }
  
  @Test
  public void testFormRequest()
  {
    DownloaderRequest req = d.formRequest();
    assertNotNull(req);
    assertTrue(req.getMaxTaskCount() > 0);
  }
  
  @Test
  public void testSendingRequest()
  {
    d.presetTasks = presetTasks("http://google.com", "http://yahoo.com");
    d.start();
    
    Utils.sleep(d.sleepBetweenLoop * 3);
    assertTrue(d.numTasksRequested >= 2 && d.numTasksRequested <= 3);
    
    d.stop();
    d.numTasksRequested = 0;
    Utils.sleep(d.sleepBetweenLoop * 3);
    assertEquals(0, d.numTasksRequested);
  }
  
  @Test
  public void testQueuingReceivedTasks()
  {
    d.presetTasks = presetTasks("http://google.com", "http://yahoo.com");
    d.start();
    
    Utils.sleep(d.sleepBetweenLoop * 3);
    assertTrue(d.numPagesQueued >= 4 && d.numPagesQueued <= 6);
    
    d.stop();
    d.numPagesQueued = 0;
    Utils.sleep(d.sleepBetweenLoop * 3);
    assertEquals(0, d.numPagesQueued);
  }
  
  @Test
  public void testFormingResult()
  {
    d.presetTasks = presetTasks("http://google.com");
    d.presetResponder = new MockDownloaderResponder();
    d.start();
    Utils.sleep(d.sleepBetweenLoop + d.sleepBetweenLoop / 10);
    d.stop();
    
    assertEquals(1, d.presetResponder.numCallbacks);
    Page page = d.presetResponder.lastCallbackPage;
    assertNotNull(page);
    DownloaderResult result = page.getResult();
    assertNotNull(result);
    assertEquals(200, result.getHttpRespCode());
    assertNotNull(result.getHttpRespMsg());
    assertNotNull(result.getMimeType());
    assertNotNull(result.getContent());
  }

  private List<Task> presetTasks(String... urls)
  {
    List<Task> tasks = new ArrayList<Task>();

    for (int i = 0; i < urls.length; ++i)
    {
      Task aTask = new Task();
      aTask.setId("" + i);
      aTask.setUri(urls[i]);
      tasks.add(aTask);
    }

    return tasks;
  }
  
}

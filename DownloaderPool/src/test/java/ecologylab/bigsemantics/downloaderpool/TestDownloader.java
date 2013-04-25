package ecologylab.bigsemantics.downloaderpool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
    d.sst = new SimpleSiteTable();
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
    d.presetTasks.add(new Task("1", "http://google.com"));
    d.presetTasks.add(new Task("2", "http://yahoo.com"));
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
    d.presetTasks.add(new Task("1", "http://google.com"));
    d.presetTasks.add(new Task("2", "http://yahoo.com"));
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
    d.presetTasks.add(new Task("1", "http://1.google.com"));
    d.presetResponder = new MockDownloaderResponder();
    d.start();
    Utils.sleep(d.sleepBetweenLoop + d.sleepBetweenLoop / 10);
    d.stop();

    assertTrue(d.presetResponder.numCallbacks >= 1 && d.presetResponder.numCallbacks <= 2);
    MockPage page = (MockPage) d.presetResponder.lastCallbackPage;
    assertNotNull(page);
    assertTrue(page.performed);
  }

  @Test
  public void testFormingBlacklist()
  {
    int dt = d.sleepBetweenLoop;

    Task t1 = new Task("1", "http://google.com");
    t1.setDomainInterval(dt * 2);
    Task t2 = new Task("2", "http://yahoo.com");
    t2.setDomainInterval(dt * 4);

    d.presetTasks.add(t1);
    d.presetTasks.add(t2);
    d.usePresetTasksOnce = true;
    d.start();

    // 0.5dt after start:
    Utils.sleep(dt / 2);
    assertBlacklisted(d, "google.com");
    assertBlacklisted(d, "yahoo.com");

    Utils.sleep(dt / 2 + dt);
    // 2dt after start:
    assertBlacklisted(d, "google.com");
    assertBlacklisted(d, "yahoo.com");

    Utils.sleep(dt / 2 + dt);
    // 3.5dt after start:
    assertNotBlacklisted(d, "google.com");
    assertBlacklisted(d, "yahoo.com");

    Utils.sleep(dt * 3);
    // 6.5dt after start:
    assertNotBlacklisted(d, "google.com");
    assertNotBlacklisted(d, "yahoo.com");
  }

  private void assertBlacklisted(Downloader d, String domain)
  {
    DownloaderRequest req = d.formRequest();
    List<String> blist = req.getBlacklist();
    assertNotNull(blist);
    assertTrue(blist.contains(domain));
  }

  private void assertNotBlacklisted(Downloader d, String domain)
  {
    DownloaderRequest req = d.formRequest();
    if (req != null)
    {
      List<String> blist = req.getBlacklist();
      if (blist != null)
      {
        assertFalse(blist.contains(domain));
      }
    }
  }

}

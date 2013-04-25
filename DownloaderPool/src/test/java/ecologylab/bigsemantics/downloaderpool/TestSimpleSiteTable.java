package ecologylab.bigsemantics.downloaderpool;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import ecologylab.concurrent.Site;


public class TestSimpleSiteTable
{
  
  SimpleSiteTable sst;
  
  @Before
  public void init()
  {
    sst = new SimpleSiteTable();
  }
  
  @Test
  public void testConstruction()
  {
    assertNotNull(sst);
  }
  
  @Test
  public void testGettingTheSameSite()
  {
    Site s1 = sst.getSite("google.com");
    Site s2 = sst.getSite("google.com");
    assertNotNull(s1);
    assertNotNull(s2);
    assertSame(s1, s2);
    assertEquals("google.com", s1.domain());
  }
  
  @Test
  public void testFindingBusySites()
  {
    long dt = 100;
    
    SimpleSite s1 = (SimpleSite) sst.getSite("google.com");
    SimpleSite s2 = (SimpleSite) sst.getSite("yahoo.com");
    SimpleSite s3 = (SimpleSite) sst.getSite("bing.com");
    
    s1.setDownloadInterval(dt);
    s2.setDownloadInterval(dt * 2);
    s3.setDownloadInterval(0);
    
    // beginning
    s1.advanceNextAvailableTime();
    s2.advanceNextAvailableTime();
    s3.advanceNextAvailableTime();
    
    Set<Site> bs = sst.getBusySites();
    assertSiteBusy(bs, "google.com");
    assertSiteBusy(bs, "yahoo.com");
    assertSiteNotBusy(bs, "bing.com");
    
    Utils.sleep(dt - dt / 10);
    // 0.9dt from beginning
    bs = sst.getBusySites();
    assertSiteBusy(bs, "google.com");
    assertSiteBusy(bs, "yahoo.com");
    assertSiteNotBusy(bs, "bing.com");
    
    Utils.sleep(dt / 10 + dt / 2);
    // 1.5dt from beginning
    bs = sst.getBusySites();
    assertSiteNotBusy(bs, "google.com");
    assertSiteBusy(bs, "yahoo.com");
    assertSiteNotBusy(bs, "bing.com");
    
    Utils.sleep(dt + dt / 2);
    // 3dt from beginning
    bs = sst.getBusySites();
    assertSiteNotBusy(bs, "google.com");
    assertSiteNotBusy(bs, "yahoo.com");
    assertSiteNotBusy(bs, "bing.com");
    
    s1.advanceNextAvailableTime();
    bs = sst.getBusySites();
    assertSiteBusy(bs, "google.com");
    Utils.sleep(dt + dt / 2);
    bs = sst.getBusySites();
    assertSiteNotBusy(bs, "google.com");
  }
  
  private void assertSiteBusy(Set<Site> busySites, String siteDomain)
  {
    assertNotNull(busySites);
    for (Site site : busySites)
    {
      if (site.domain().equals(siteDomain))
        return;
    }
    fail("Domain " + siteDomain + " is expected to be busy but not contained in busySites.");
  }
  
  private void assertSiteNotBusy(Set<Site> busySites, String siteDomain)
  {
    if (busySites != null)
    {
      for (Site site : busySites)
      {
        if (site.domain().equals(siteDomain))
          fail("Domain " + siteDomain + " is expected not to be busy but contained in busySites.");
      }
    }
  }
  
}

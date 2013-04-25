package ecologylab.bigsemantics.downloaderpool;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ecologylab.concurrent.Site;

public class SimpleSiteTable
{
  
  ConcurrentHashMap<String, SimpleSite> sites = new ConcurrentHashMap<String, SimpleSite>();
  
  /**
   * Create a SimpleSite object.
   * 
   * @param domain
   * @return
   */
  public Site getSite(String domain)
  {
    SimpleSite site;
    if (sites.contains(domain))
    {
      site = sites.get(domain);
    }
    else
    {
      site = new SimpleSite(domain);
      SimpleSite existingSite = sites.putIfAbsent(domain, site);
      if (existingSite != null)
      {
        site = existingSite;
      }
    }
    return site;
  }
  
  /**
   * Get the set of sites that are too busy to download from at this moment.
   * @return
   */
  public Set<Site> getBusySites()
  {
    Set<Site> results = new HashSet<Site>();
    
    long t = System.currentTimeMillis();
    for (SimpleSite site : sites.values())
    {
      // if the site is down or ignored, we treat it as busy because we don't want to download from
      // them anyway.
      if (site.isDown() || site.isIgnored())
      {
        results.add(site);
      }
      if (site.isDownloadingConstrained() && site.getNextAvailableTime() > t)
      {
        results.add(site);
      }
    }
    
    return results;
  }
  
}

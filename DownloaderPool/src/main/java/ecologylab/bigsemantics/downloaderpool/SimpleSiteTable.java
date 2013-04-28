package ecologylab.bigsemantics.downloaderpool;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ecologylab.concurrent.Site;

/**
 * A table that records all the sites that we are accessing.
 * 
 * @author quyin
 */
public class SimpleSiteTable
{

  /**
   * The real table, from domain to Site object.
   */
  private ConcurrentHashMap<String, SimpleSite> sites = new ConcurrentHashMap<String, SimpleSite>();

  /**
   * Get the Site object by domain. Lazily create the object with the given downloadInterval if
   * needed.
   * 
   * @param domain
   * @param downloadInterval
   *          Interval between requests to this domain, in millisecond. Will be ignored if the Site
   *          object for this domain already exists.
   * @return
   */
  public Site getSite(String domain, long downloadInterval)
  {
    SimpleSite site;
    if (sites.containsKey(domain))
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
      site.setDownloadInterval(downloadInterval);
    }
    return site;
  }

  /**
   * Get the set of sites that are too busy to download from at this moment.
   * 
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

package ecologylab.bigsemantics.downloaderpool;

import java.util.Random;

import ecologylab.concurrent.Site;
import ecologylab.net.ParsedURL;

/**
 * 
 * @author quyin
 * 
 */
public class SimpleSite implements Site
{

  private static final int TWELVE_HOURS_IN_MILLIS = 1000 * 60 * 60 * 12;

  private static Random    rand                   = new Random(System.currentTimeMillis());

  private String           domain;

  private boolean          ignored;

  private boolean          down;

  private boolean          downloading;

  private long             lastDownloadingTime;

  private long             downloadInterval;

  private long             nextAvailableTime;

  private int              cNormal;

  private int              cTimeout;

  private int              cNotFound;

  private int              cOther;

  public SimpleSite(String domain)
  {
    this.domain = domain;
  }

  @Override
  public String domain()
  {
    return domain;
  }

  @Override
  public boolean isIgnored()
  {
    return ignored;
  }

  @Override
  public void setIgnored(boolean ignored)
  {
    this.ignored = ignored;
  }

  @Override
  public boolean isDown()
  {
    return down;
  }

  @Override
  public boolean isDownloading()
  {
    return downloading;
  }

  @Override
  public long getLastDownloadAt()
  {
    return lastDownloadingTime;
  }

  @Override
  public boolean isDownloadingConstrained()
  {
    return downloadInterval > 0;
  }

  public long getDownloadInterval()
  {
    return downloadInterval;
  }

  protected void setDownloadInterval(long downloadInterval)
  {
    this.downloadInterval = downloadInterval;
  }

  @Override
  public long getDecentDownloadInterval()
  {
    if (isDownloadingConstrained())
    {
      return downloadInterval + rand.nextInt((int) downloadInterval / 2);
    }
    else
    {
      return 0;
    }
  }

  @Override
  public long getNextAvailableTime()
  {
    return nextAvailableTime;
  }

  @Override
  public void advanceNextAvailableTime()
  {
    if (isDownloadingConstrained())
    {
      nextAvailableTime = System.currentTimeMillis() + getDecentDownloadInterval();
    }
  }

  @Override
  public void setAbnormallyLongNextAvailableTime()
  {
    nextAvailableTime = System.currentTimeMillis() + TWELVE_HOURS_IN_MILLIS;
  }

  @Override
  public void queueDownload(ParsedURL location)
  {
    // no-op
  }

  @Override
  public void beginDownload(ParsedURL location)
  {
    lastDownloadingTime = System.currentTimeMillis();
    downloading = true;

  }

  @Override
  public void endDownload(ParsedURL location)
  {
    downloading = false;
  }

  @Override
  public void countNormalDownload(ParsedURL location)
  {
    cNormal++;
  }

  @Override
  public void countTimeout(ParsedURL location)
  {
    cTimeout++;
  }

  @Override
  public void countFileNotFound(ParsedURL location)
  {
    cNotFound++;
  }

  @Override
  public void countOtherIoError(ParsedURL location)
  {
    cOther++;
  }

  public int numOfNormalDownloads()
  {
    return cNormal;
  }

  public int numOfTimeouts()
  {
    return cTimeout;
  }

  public int numOfNotFounds()
  {
    return cNotFound;
  }

  public int numOfOtherIoError()
  {
    return cOther;
  }

}

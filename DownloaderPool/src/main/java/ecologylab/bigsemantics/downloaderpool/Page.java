package ecologylab.bigsemantics.downloaderpool;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.params.CoreProtocolPNames;

import ecologylab.bigsemantics.downloaderpool.DownloaderResult.State;
import ecologylab.concurrent.Downloadable;
import ecologylab.concurrent.DownloadableLogRecord;
import ecologylab.concurrent.Site;
import ecologylab.net.ParsedURL;

/**
 * Represent a page to download. It is the actual class that the DownloadMonitor works with.
 * 
 * @author quyin
 */
public class Page implements Downloadable
{

  // collaborating objects:
  
  HttpClientPool           clientPool;

  SimpleSiteTable          sst;

  // properties:
  
  private ParsedURL        location;

  private String           userAgent;

  private DownloaderResult result;

  public Page(String id, ParsedURL location, String userAgent)
  {
    this.location = location;
    this.userAgent = userAgent;
    result = new DownloaderResult();
    result.setTaskId(id);
  }

  @Override
  public ParsedURL location()
  {
    return location;
  }

  @Override
  public ParsedURL getDownloadLocation()
  {
    return location;
  }

  public void setDownloadLocation(ParsedURL location)
  {
    this.location = location;
  }

  @Override
  public Site getSite()
  {
    if (location != null)
    {
      String domain = location.domain();
      return sst.getSite(domain, 0);
    }
    return null;
  }

  @Override
  public Site getDownloadSite()
  {
    return getSite();
  }

  @Override
  public DownloadableLogRecord getLogRecord()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void performDownload()
  {
    AbstractHttpClient client = clientPool.acquire();
    if (userAgent != null && userAgent.length() > 0)
    {
      client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
    }
    PageResponseHandler handler = new PageResponseHandler(result);
    PageRedirectStrategy redirectStrategy = new PageRedirectStrategy(result);
    client.setRedirectStrategy(redirectStrategy);

    HttpGet httpGet = new HttpGet(location.toString());
    try
    {
      client.execute(httpGet, handler);
    }
    catch (ClientProtocolException e)
    {
      // TODO logging
      result.setState(State.ERR_PROTOCOL);
    }
    catch (IOException e)
    {
      // TODO logging
      result.setState(State.ERR_IO);
    }

    clientPool.release(client);
  }

  public DownloaderResult getResult()
  {
    return result;
  }

  @Override
  public void handleIoError(Throwable e)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isCached()
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isImage()
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isRecycled()
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void recycle()
  {
    // TODO Auto-generated method stub

  }

  @Override
  public String message()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public String toString()
  {
    return String.format("%s[%s]", this.getClass().getSimpleName(), this.location);
  }

}

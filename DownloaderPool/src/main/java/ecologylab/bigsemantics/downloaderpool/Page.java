package ecologylab.bigsemantics.downloaderpool;

import java.io.IOException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;

import ecologylab.concurrent.Downloadable;
import ecologylab.concurrent.DownloadableLogRecord;
import ecologylab.concurrent.Site;
import ecologylab.net.ParsedURL;

public class Page implements Downloadable
{

  private ParsedURL location;

  DownloaderResult  result;

  SimpleSiteTable   sst;

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
      return sst.getSite(domain);
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
  public void handleIoError(Throwable arg0)
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
  public ParsedURL location()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String message()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void performDownload() throws IOException
  {
    System.err.println("Perform downloading: " + this);

    // TODO pooling?
    DefaultHttpClient client = new DefaultHttpClient();
    client.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, true);

    HttpGet httpGet = new HttpGet(location.toString());
    PageRedirectStrategy redirectStrategy = new PageRedirectStrategy();
    PageResponseHandler handler = new PageResponseHandler();
    client.setRedirectStrategy(redirectStrategy);
    result = client.execute(httpGet, handler);
  }

  @Override
  public void recycle()
  {
    // TODO Auto-generated method stub

  }

  public DownloaderResult getResult()
  {
    return result;
  }

  public String toString()
  {
    return String.format("%s[%s]", this.getClass().getSimpleName(), this.location);
  }

}

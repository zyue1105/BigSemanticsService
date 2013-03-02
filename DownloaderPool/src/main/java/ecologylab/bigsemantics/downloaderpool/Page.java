package ecologylab.bigsemantics.downloaderpool;

import java.io.IOException;

import ecologylab.concurrent.BasicSite;
import ecologylab.concurrent.Downloadable;
import ecologylab.concurrent.DownloadableLogRecord;
import ecologylab.net.ParsedURL;

public class Page implements Downloadable
{

  @Override
  public ParsedURL getDownloadLocation()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BasicSite getDownloadSite()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DownloadableLogRecord getLogRecord()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BasicSite getSite()
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
    // TODO Auto-generated method stub

  }

  @Override
  public void recycle()
  {
    // TODO Auto-generated method stub

  }

}

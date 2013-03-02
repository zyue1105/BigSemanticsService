package ecologylab.bigsemantics.downloaderpool;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ecologylab.bigsemantics.downloaderpool.Downloader;

public class TestDownloader
{

  Downloader d;

  @Before
  public void init()
  {
    d = new Downloader();
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
  }

}

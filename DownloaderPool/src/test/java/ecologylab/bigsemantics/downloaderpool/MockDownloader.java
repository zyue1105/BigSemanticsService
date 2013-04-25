package ecologylab.bigsemantics.downloaderpool;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author quyin
 * 
 */
public class MockDownloader extends Downloader
{

  int        numTasksRequested  = 0;

  int        numPagesQueued     = 0;

  List<Task> presetTasks        = new ArrayList<Task>();

  boolean    usePresetTasksOnce = false;

  @Override
  public List<Task> requestTasks()
  {
    numTasksRequested++;
    List<Task> result = presetTasks;
    if (usePresetTasksOnce)
    {
      presetTasks = null;
    }
    return result;
  }

  @Override
  protected Page createPage()
  {
    return new MockPage();
  }

  @Override
  protected void queuePageToDownload(Page pageToDownload)
  {
    super.queuePageToDownload(pageToDownload);
    System.err.println("Queued page to download: " + pageToDownload);
    numPagesQueued++;
  }

  MockDownloaderResponder presetResponder;

  @Override
  protected DownloaderResponder createDownloaderResponder()
  {
    if (presetResponder != null)
    {
      return presetResponder;
    }
    else
    {
      return super.createDownloaderResponder();
    }
  }

}

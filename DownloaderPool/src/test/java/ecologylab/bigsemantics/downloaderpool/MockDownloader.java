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

  int        numTasksRequested = 0;

  int        numPagesQueued    = 0;

  List<Task> presetTasks;

  @Override
  public List<Task> requestTasks()
  {
    numTasksRequested++;

    return presetTasks;
  }

  @Override
  protected void queuePageToDownload(Page pageToDownload)
  {
    super.queuePageToDownload(pageToDownload);
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

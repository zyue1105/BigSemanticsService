package ecologylab.bigsemantics.downloaderpool;

import java.util.List;

import ecologylab.concurrent.DownloadMonitor;
import ecologylab.concurrent.Downloadable;

/**
 * 
 * @author quyin
 * 
 */
public class Downloader
{

  // TODO these need to be in the property file:

  static String         name;

  static int            numDownloadThreads = 10;

  int                   maxTaskCount;

  DownloadMonitor<Page> downloadMonitor;

  public Downloader()
  {
    super();
    downloadMonitor = new DownloadMonitor<Page>("Downloader: " + name, numDownloadThreads);
  }

  public DownloaderRequest formRequest()
  {
    DownloaderRequest req = new DownloaderRequest();
    // TODO
    return null;
  }

  public List<Task> requestTasks()
  {
    // TODO
    return null;
  }

  public DownloaderResult formResult(Task task)
  {
    // TODO
    return null;
  }

}

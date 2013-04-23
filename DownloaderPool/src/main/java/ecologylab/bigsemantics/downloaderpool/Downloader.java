package ecologylab.bigsemantics.downloaderpool;

import java.util.List;

import ecologylab.concurrent.DownloadMonitor;
import ecologylab.net.ParsedURL;

/**
 * 
 * @author quyin
 * 
 */
public class Downloader implements Runnable
{

  static enum Status
  {
    NEW, READY, RUNNING, STOP_PENDING, STOPPED
  }

  // TODO these need to be in the property file:

  static int            numDownloadThreads = 10;

  int                   sleepBetweenLoop   = 500;

  String                name;

  int                   maxTaskCount       = 10;

  DownloadMonitor<Page> downloadMonitor;

  Status                status;
  
  Object                lockStatus = new Object();

  public Downloader()
  {
    super();
    status = Status.NEW;
    downloadMonitor = new DownloadMonitor<Page>("Downloader: " + name, numDownloadThreads);
    status = Status.READY;
  }

  public List<Task> requestTasks()
  {
    DownloaderRequest req = formRequest();
    // TODO: send request and wait for response;
    return null;
  }

  public DownloaderRequest formRequest()
  {
    DownloaderRequest req = new DownloaderRequest();
    req.setMaxTaskCount(maxTaskCount);
    return req;
  }

  public DownloaderResult formResult(Task task)
  {
    // TODO
    return null;
  }

  @Override
  public void run()
  {
    downloaderLoop();
  }

  public void downloaderLoop()
  {
    // loop to form and send downloader requests
    while (status == Status.RUNNING)
    {
      if (downloadMonitor.toDownloadSize() <= 0)
      {
        List<Task> tasks = requestTasks();
        for (Task task : tasks)
        {
          Page pageToDownload = createDownloadable(task.getUri());
          queuePageToDownload(pageToDownload);
        }
      }
      
      Utils.sleep(sleepBetweenLoop);
    }

    synchronized (lockStatus)
    {
      status = Status.STOPPED;
      lockStatus.notifyAll();
    }
  }

  protected Page createDownloadable(String uri)
  {
    ParsedURL purl = ParsedURL.getAbsolute(uri);
    Page pageToDownload = new Page();
    pageToDownload.setDownloadLocation(purl);
    return pageToDownload;
  }
  
  protected DownloaderResponder createDownloaderResponder()
  {
    return new DownloaderResponder();
  }

  protected void queuePageToDownload(Page pageToDownload)
  {
    DownloaderResponder responder = createDownloaderResponder();
    downloadMonitor.download(pageToDownload, responder);
  }

  public synchronized void start()
  {
    if (status == Status.READY)
      status = Status.RUNNING;
    Thread t = new Thread(this);
    t.start();
  }

  public synchronized void stop()
  {
    if (status == Status.RUNNING)
    {
      synchronized (lockStatus)
      {
        if (status == Status.RUNNING)
        {
          status = Status.STOP_PENDING;
          try
          {
            lockStatus.wait();
            if (status == Status.STOPPED)
            {
              return;
            }
            else
            {
              throw new RuntimeException("Cannot stop downloader!");
            }
          }
          catch (InterruptedException e)
          {
            e.printStackTrace();
          }
        }
      }
    }
  }
  
}

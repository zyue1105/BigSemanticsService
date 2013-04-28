package ecologylab.bigsemantics.downloaderpool;

import java.util.List;
import java.util.Set;

import ecologylab.concurrent.DownloadMonitor;
import ecologylab.concurrent.Site;
import ecologylab.net.ParsedURL;

/**
 * A downloader that actively retrieves tasks from the controller, downloads pages, and sends
 * results back to the controller. The downloader gets all the necessary information, such as the
 * task URL, user agent, and time to wait between requests.
 * 
 * @author quyin
 * 
 */
public class Downloader extends Routine
{

  // configs:
  // TODO should go into a config file

  static int            numDownloadThreads = 4;

  int                   maxTaskCount       = 10;

  // collaborating objects:

  DownloadMonitor<Page> downloadMonitor;

  SimpleSiteTable       sst;

  HttpClientPool        clientPool;

  // runtime properties:

  private String        name;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public void init()
  {
    downloadMonitor = new DownloadMonitor<Page>("Downloader: " + name, numDownloadThreads);
    sst = new SimpleSiteTable();
    clientPool = new HttpClientPool();
  }

  /**
   * Request tasks from the controller. The request will consider ongoing work, in order not to hit
   * the same website too frequently.
   * 
   * @return The list of retrieved tasks. If failed, null.
   */
  public List<Task> requestTasks()
  {
    DownloaderRequest req = formRequest();
    // TODO: send request and wait for response;
    return null;
  }

  /**
   * Form a request to the controller to retrieve tasks.
   * 
   * @return The formed DownloaderRequest object.
   */
  public DownloaderRequest formRequest()
  {
    DownloaderRequest req = new DownloaderRequest();
    req.setMaxTaskCount(maxTaskCount);
    Set<Site> busySites = sst.getBusySites();
    for (Site site : busySites)
    {
      req.addToBlacklist(site.domain());
    }
    return req;
  }

  public void routineBody()
  {
    System.err.println("routineBody()");
    if (downloadMonitor.toDownloadSize() <= 0)
    {
      System.err.println("routineBody() if");
      List<Task> tasks = requestTasks();
      updateSiteIntervals(tasks);
      createAndQueuePages(tasks);
    }
  }

  /**
   * Update waiting interval info for retrieved tasks.
   * 
   * @param tasks
   *          The retrieved tasks.
   */
  protected void updateSiteIntervals(List<Task> tasks)
  {
    if (tasks != null)
    {
      for (Task task : tasks)
      {
        ParsedURL purl = task.getPurl();
        if (purl != null)
        {
          String domain = purl.domain();
          sst.getSite(domain, task.getDomainInterval());
        }
      }
    }
  }

  /**
   * Create Page objects, which represent the pages to download, from retrieved tasks, and queue
   * them to the DownloadMonitor for downloading.
   * 
   * @param tasks
   */
  protected void createAndQueuePages(List<Task> tasks)
  {
    if (tasks != null)
    {
      for (Task task : tasks)
      {
        if (task.getId() != null && task.getPurl() != null)
        {
          Page pageToDownload = createPage(task);
          queuePageToDownload(pageToDownload, task);
        }
      }
    }
  }

  /**
   * Page object factory method.
   * 
   * @param task
   * @return
   */
  protected Page createPage(Task task)
  {
    Page page = new Page(task.getId(), task.getPurl(), task.getUserAgent());
    page.clientPool = this.clientPool;
    page.sst = this.sst;
    return page;
  }

  /**
   * Queue the given Page object to DownloadMonitor.
   * 
   * @param pageToDownload
   * @param associatedTask
   */
  protected void queuePageToDownload(Page pageToDownload, Task associatedTask)
  {
    DownloaderResponder responder = createDownloaderResponder(associatedTask);
    downloadMonitor.download(pageToDownload, responder);
  }

  /**
   * DownloadResponder object factory method.
   * 
   * @param associatedTask
   * @return
   */
  protected DownloaderResponder createDownloaderResponder(Task associatedTask)
  {
    return new DownloaderResponder(associatedTask);
  }

}

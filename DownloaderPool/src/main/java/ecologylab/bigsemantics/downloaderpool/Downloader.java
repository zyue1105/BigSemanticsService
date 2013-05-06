package ecologylab.bigsemantics.downloaderpool;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.concurrent.DownloadMonitor;
import ecologylab.concurrent.Site;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.formatenums.StringFormat;

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

  private static Logger logger;

  static
  {
    logger = LoggerFactory.getLogger(Downloader.class);
  }

  // configs:
  // TODO should go into a config file

  String                controllerBaseUrl  = "http://localhost:8080/DownloaderPool/assign.xml";

  int                   numDownloadThreads = 4;

  int                   maxTaskCount       = 10;

  // collaborating objects:

  DownloadMonitor<Page> downloadMonitor;

  SimpleSiteTable       sst;

  HttpClientPool        clientPool;

  /**
   * This client is used to communicate with the controller.
   */
  HttpClient            client;

  // runtime properties:

  private String        name;

  public Downloader(String name)
  {
    super();
    this.name = name;
    downloadMonitor = new DownloadMonitor<Page>("Downloader: " + name, numDownloadThreads);
    sst = new SimpleSiteTable();
    clientPool = new HttpClientPool();
    client = clientPool.acquire();
    setReady();
  }

  public String getName()
  {
    return name;
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

    Map<String, String> params = new HashMap<String, String>();
    params.put("id", this.name);
    String blist = req.getBlacklistString();
    if (blist != null)
    {
      params.put("blacklist", blist);
    }
    params.put("ntask", String.valueOf(this.maxTaskCount));
    HttpGet get = Utils.generateGetRequest(controllerBaseUrl, params);
    logger.info("HTTP GET: " + get.getURI());

    int status = -1;
    try
    {
      BasicResponse result = new BasicResponse();
      client.execute(get, new BasicResponseHandler(result));
      status = result.getHttpRespCode();
      String content = result.getContent();
      if (status == HttpStatus.SC_OK)
      {
        AssignedTasks assignedTasks = (AssignedTasks) Utils.deserialize(content,
                                                                        MessageScope.get(),
                                                                        StringFormat.XML);
        return assignedTasks.getTasks();
      }
      else
      {
        logger.info("Status message: " + result.getHttpRespMsg());
        logger.info("Content:\n" + content);
      }
    }
    catch (ClientProtocolException e)
    {
      logger.error("Exception when accessing " + get.getURI(), e);
      e.printStackTrace();
      get.abort();
    }
    catch (IOException e)
    {
      logger.error("Exception when accessing " + get.getURI(), e);
      e.printStackTrace();
      get.abort();
    }

    logger.error("Empty response [status code: {}] from {}", status, get.getURI());
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
    logger.debug("routineBody()");
    if (downloadMonitor.toDownloadSize() <= 0)
    {
      logger.debug("routineBody(): request tasks and queue downloads");
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
          logger.info("creating Page object for task[{}][{}]...", task.getId(), task.getPurl());
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
    DownloaderResponder downloaderResponder = new DownloaderResponder(associatedTask);
    downloaderResponder.downloader = this;
    return downloaderResponder;
  }

}

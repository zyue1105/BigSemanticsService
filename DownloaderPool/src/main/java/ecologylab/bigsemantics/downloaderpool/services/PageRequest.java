package ecologylab.bigsemantics.downloaderpool.services;

import java.util.Observable;
import java.util.Observer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ecologylab.bigsemantics.downloaderpool.Controller;
import ecologylab.bigsemantics.downloaderpool.DownloaderResult;
import ecologylab.bigsemantics.downloaderpool.Task;
import ecologylab.bigsemantics.downloaderpool.Task.ObservableEventType;
import ecologylab.bigsemantics.downloaderpool.Task.State;
import ecologylab.bigsemantics.downloaderpool.Utils;
import ecologylab.generic.StringBuilderBaseUtils;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * Requests that are related to a web page.
 * 
 * @author quyin
 */
@Path("/page")
public class PageRequest extends RequestHandlerForController
{

  /**
   * Download a web page.
   * 
   * @param remoteIp
   * @param url
   * @param userAgent
   * @param interval
   * @param longInterval
   * @param numOfAttempts
   * @param timeOfAttempt
   * @param failPattern
   * @param banPattern
   * @return
   */
  public DownloaderResult download(String remoteIp,
                                   String url,
                                   String userAgent,
                                   int interval,
                                   int longInterval,
                                   int numOfAttempts,
                                   int timeOfAttempt,
                                   String failPattern,
                                   String banPattern)
  {
    Controller ctrl = getController();
    
    StringBuilder sb = StringBuilderBaseUtils.acquire();
    sb.append(remoteIp).append("|").append(System.currentTimeMillis()).append("|").append(url);
    byte[] hash = Utils.hashToBytes(sb.toString());
    String tid = Utils.base64urlEncode(hash).substring(0, ctrl.getTaskIdLen());
    StringBuilderBaseUtils.release(sb);

    Task task = new Task(tid, url);
    task.setUserAgent(userAgent);
    task.setDomainInterval(interval);
    task.setDomainLongInterval(longInterval);
    task.setMaxAttempts(numOfAttempts);
    task.setAttemptTime(timeOfAttempt);
    task.setFailRegex(failPattern);
    task.setBanRegex(banPattern);
    
    logger.info("Downloading request from [{}]: {}", remoteIp, task);

    final Object lock = new Object();
    Observer taskOb = new Observer()
    {
      @Override
      public void update(Observable o, Object arg)
      {
        assert o instanceof Task;
        assert arg instanceof Task.ObservableEventType;

        Task task = (Task) o;
        ObservableEventType type = (ObservableEventType) arg;

        if (type == ObservableEventType.STATE_CHANGE)
        {
          State newState = task.getState();
          switch (newState)
          {
          case RESPONDED:
          case TERMINATED:
            synchronized (lock)
            {
              lock.notifyAll();
            }
            task.deleteObserver(this);
            break;
          default:
            // ignore other changes
            break;
          }
        }
      }
    };
    task.addObserver(taskOb);

    synchronized (lock)
    {
      ctrl.queueTask(task);
      logger.info("Task queued: fromIP={}, id={}, url=[{}]", remoteIp, task.getId(), task.getUri());

      try
      {
        logger.info("Waiting for Task[" + task.getId() + "] to be responded ...");
        lock.wait(ctrl.getClientRequestTimeout() * 1000);
      }
      catch (InterruptedException e)
      {
        logger.warn("Waiting for task interrupted.");
        e.printStackTrace();
      }
    }

    logger.info("Task[" + task.getId() + "] responded, result = " + task.getResult());
    return task.getResult();
  }

  @Path("/download.json")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response downloadJson(@Context HttpServletRequest request,
                               @QueryParam("url") String url,
                               @QueryParam("agent") String userAgent,
                               @QueryParam("int") int interval,
                               @QueryParam("lint") int longInterval,
                               @QueryParam("natt") int numOfAttempts,
                               @QueryParam("tatt") int timeOfAttempt,
                               @QueryParam("failp") String failPattern,
                               @QueryParam("banp") String banPattern)
  {
    String remoteIp = request.getRemoteAddr();
    DownloaderResult result = this.download(remoteIp,
                                            url,
                                            userAgent,
                                            interval,
                                            longInterval,
                                            numOfAttempts,
                                            timeOfAttempt,
                                            failPattern,
                                            banPattern);

    return generateResponse(result,
                            StringFormat.JSON,
                            MediaType.APPLICATION_JSON,
                            "The Request Cannot Be Fulfilled.");
  }

  @Path("/download.xml")
  @GET
  @Produces(MediaType.APPLICATION_XML)
  public Response downloadXml(@Context HttpServletRequest request,
                              @QueryParam("url") String url,
                              @QueryParam("agent") String userAgent,
                              @QueryParam("int") int interval,
                              @QueryParam("lint") int longInterval,
                              @QueryParam("natt") int numOfAttempts,
                              @QueryParam("tatt") int timeOfAttempt,
                              @QueryParam("failp") String failPattern,
                              @QueryParam("banp") String banPattern)
  {
    String remoteIp = request.getRemoteAddr();
    DownloaderResult result = this.download(remoteIp,
                                            url,
                                            userAgent,
                                            interval,
                                            longInterval,
                                            numOfAttempts,
                                            timeOfAttempt,
                                            failPattern,
                                            banPattern);

    return generateResponse(result,
                            StringFormat.XML,
                            MediaType.APPLICATION_XML,
                            "The Request Cannot Be Fulfilled.");
  }

  @Path("view.json")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response viewPageJson(@QueryParam("id") String workerId,
                               @QueryParam("url") String url)
  {
    // TODO
    return null;
  }

  @Path("view.xml")
  @GET
  @Produces(MediaType.APPLICATION_XML)
  public Response viewPageXml(@QueryParam("id") String workerId,
                              @QueryParam("url") String url)
  {
    // TODO
    return null;
  }

  /**
   * Report the downloading results from a downloader.
   * 
   * @param request
   * @param taskId
   * @param state
   * @param respCode
   * @param respMsg
   * @param mimeType
   * @param charset
   * @param content
   * @param contentDescription
   * @return
   */
  @Path("/report")
  @POST
  @Produces(MediaType.TEXT_PLAIN)
  public Response report(@Context HttpServletRequest request,
                         @FormParam("wid") String workerId,
                         @FormParam("tid") String taskId,
                         @FormParam("state") DownloaderResult.State state,
                         @FormParam("code") int respCode,
                         @FormParam("msg") String respMsg,
                         @FormParam("mime") String mimeType,
                         @FormParam("charset") String charset,
                         @FormParam("content") String content,
                         @FormParam("descr") String contentDescription)
  {
    String ip = request.getRemoteAddr();
    logger.info("Downloader[{}]@{} reported results for task[{}]; code: {}; content-length: {}.",
                workerId,
                ip,
                taskId,
                respCode,
                content == null ? 0 : content.length());
    Controller ctrl = getController();

    DownloaderResult result = new DownloaderResult();
    result.setTaskId(taskId);
    result.setState(state);
    result.setHttpRespCode(respCode);
    result.setHttpRespMsg(respMsg);
    result.setMimeType(mimeType);
    result.setCharset(charset);
    result.setContent(content);
    result.setContentDescription(contentDescription);

    ctrl.report(taskId, result);

    return Response.ok("Task[" + taskId + "] received.").build();
  }

}

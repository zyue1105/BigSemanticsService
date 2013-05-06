package ecologylab.bigsemantics.downloaderpool.services;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ecologylab.bigsemantics.downloaderpool.Controller;
import ecologylab.bigsemantics.downloaderpool.DownloaderResult;
import ecologylab.bigsemantics.downloaderpool.DownloaderResult.State;

/**
 * Report result from a downloader to the controller.
 * 
 * @author quyin
 */
@Path("/report")
public class Report extends Action
{

  /**
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
  @POST
  @Produces(MediaType.TEXT_PLAIN)
  public Response reportResult(@Context HttpServletRequest request,
                               @FormParam("wid") String workerId,
                               @FormParam("tid") String taskId,
                               @FormParam("state") State state,
                               @FormParam("code") int respCode,
                               @FormParam("msg") String respMsg,
                               @FormParam("mime") String mimeType,
                               @FormParam("charset") String charset,
                               @FormParam("content") String content,
                               @FormParam("descr") String contentDescription)
  {
    String ip = request.getRemoteAddr();
    logger.info("received param map: " + request.getParameterMap());
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

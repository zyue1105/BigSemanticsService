package ecologylab.bigsemantics.downloaderpool.services;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ecologylab.bigsemantics.downloaderpool.AssignedTasks;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * Assign tasks to downloaders.
 * 
 * @author quyin
 */
@Path("/assign.json")
public class AssignJson extends Assign
{

  /**
   * 
   * @param workerId
   * @param blacklist
   *          List of comma separated domains that are currently blacklisted by this worker.
   * @param maxTaskCount
   * @return
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response assignTask(@Context HttpServletRequest request,
                             @QueryParam("id") String workerId,
                             @QueryParam("blacklist") String blacklist,
                             @QueryParam("ntask") int maxTaskCount)
  {
    String remoteIp = request.getRemoteAddr();
    AssignedTasks result = this.assignTask(remoteIp, workerId, blacklist, maxTaskCount);

    return generateResponse(result,
                            StringFormat.JSON,
                            MediaType.APPLICATION_JSON,
                            "Tasks Cannot Be Assigned.");
  }

}

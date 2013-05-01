package ecologylab.bigsemantics.downloaderpool.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ecologylab.bigsemantics.downloaderpool.Task;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * Viewing task details.
 * 
 * @author quyin
 */
@Path("/task.json")
public class TaskJson extends ViewTask
{

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response viewTask(@QueryParam("id") String id,
                           @QueryParam("url") String url)
  {
    Task t = findTask(id, url);
    return generateResponse(t, StringFormat.JSON, MediaType.APPLICATION_JSON, "Cannot Find Task.");
  }

}

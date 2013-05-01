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
@Path("/task.xml")
public class TaskXml extends ViewTask
{

  /**
   * 
   * @param workerId
   * @param url
   * @return
   */
  @GET
  @Produces(MediaType.APPLICATION_XML)
  public Response viewTask(@QueryParam("id") String id,
                           @QueryParam("url") String url)
  {
    Task t = findTask(id, url);
    return generateResponse(t, StringFormat.XML, MediaType.APPLICATION_XML, "Cannot Find Task.");
  }

}

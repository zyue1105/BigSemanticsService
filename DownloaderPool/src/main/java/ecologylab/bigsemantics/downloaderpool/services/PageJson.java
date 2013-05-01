package ecologylab.bigsemantics.downloaderpool.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Viewing a downloaded page.
 * 
 * @author quyin
 */
@Path("/page.json")
public class PageJson
{

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response viewPage(@QueryParam("id") String workerId,
                           @QueryParam("url") String url)
  {
    // TODO
    return null;
  }

}

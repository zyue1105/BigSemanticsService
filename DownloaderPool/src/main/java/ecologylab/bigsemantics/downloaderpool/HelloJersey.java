package ecologylab.bigsemantics.downloaderpool;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * 
 * @author quyin
 * 
 */
@Path("/echo")
public class HelloJersey
{

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Response echo(@QueryParam("msg") String message)
  {
    if (message == null)
      message = "<EMPTY MESSAGE>";
    return Response.ok().entity(message).build();
  }

}

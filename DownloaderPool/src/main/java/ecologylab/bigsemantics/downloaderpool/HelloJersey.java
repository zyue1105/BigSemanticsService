package ecologylab.bigsemantics.downloaderpool;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Test Jersey.
 * 
 * @author quyin
 */
@Path("/echo")
public class HelloJersey
{

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Response echo(@Context HttpServletRequest request,
                       @QueryParam("msg") String message)
  {
    String ip = request.getRemoteAddr();
    if (message == null)
      message = "<EMPTY MESSAGE>";
    message += "\nCalling from " + ip;
    return Response.ok().entity(message).build();
  }
}

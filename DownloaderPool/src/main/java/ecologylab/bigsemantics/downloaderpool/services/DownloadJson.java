package ecologylab.bigsemantics.downloaderpool.services;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ecologylab.bigsemantics.downloaderpool.DownloaderResult;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * Request to download a page.
 * 
 * @author quyin
 */
@Path("/download.json")
public class DownloadJson extends Download
{

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response requestDownload(@Context HttpServletRequest request,
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
    DownloaderResult result = this.requestDownload(remoteIp,
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
}

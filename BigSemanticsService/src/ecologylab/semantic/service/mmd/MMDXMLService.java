/**
 * 
 */
package ecologylab.semantic.service.mmd;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.NDC;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ecologylab.logging.ILogger;
import ecologylab.net.ParsedURL;
import ecologylab.semantic.service.SemanticServiceErrorCodes;
import ecologylab.semantic.service.SemanticServiceScope;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * mmd.xml root resource requests are made with url parameter and are redirected to name parameter
 * 
 * @author ajit
 * 
 */

@Path("/mmd.xml")
@Component
@Scope("singleton")
public class MMDXMLService
{
  // static Logger log4j = Logger.getLogger(ServiceLogger.mmdLogger);
  static ILogger logger;

  static
  {
    logger = SemanticServiceScope.get().getLoggerFactory().getLogger(MMDXMLService.class);
  }

  // request specific UriInfo object to get absolute query path
  @Context
  UriInfo        uriInfo;

  @GET
  @Produces("application/xml")
  public Response getMmd(@QueryParam("url") String url, @QueryParam("name") String name)
  {
    NDC.push("format: xml | url:" + url + " | name:" + name);
    long requestTime = System.currentTimeMillis();
    logger.debug("Requested at: " + (new Date(requestTime)));

    Response resp = null;
    if (url != null)
    {
      ParsedURL purl = ParsedURL.getAbsolute(url);
      if (purl != null)
      {
        resp = MMDServiceHelper.redirectToMmdByName(purl, uriInfo);
      }
    }
    else if (name != null)
    {
      resp = MMDServiceHelper.getMmdByName(name, StringFormat.XML);
    }

    // invalid params
    if (resp == null)
      resp = Response.status(Status.BAD_REQUEST).entity(SemanticServiceErrorCodes.BAD_REQUEST)
          .type(MediaType.TEXT_PLAIN).build();

    logger.debug("Time taken (ms): " + (System.currentTimeMillis() - requestTime));

    NDC.remove();
    return resp;
  }
}

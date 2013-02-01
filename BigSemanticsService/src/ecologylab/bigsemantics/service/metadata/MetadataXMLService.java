/**
 * 
 */
package ecologylab.bigsemantics.service.metadata;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.NDC;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ecologylab.bigsemantics.service.SemanticServiceErrorCodes;
import ecologylab.bigsemantics.service.SemanticServiceScope;
import ecologylab.logging.ILogger;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * metadata.xml root resource metadata request are made with url and for a graph span (default = 0)
 * 
 * @author ajit
 * 
 */

@Path("/metadata.xml")
@Component
@Scope("singleton")
public class MetadataXMLService
{
	// static Logger log4j = Logger.getLogger(ServiceLogger.metadataLogger);
	static ILogger	logger	= SemanticServiceScope.get().getLoggerFactory()
															.getLogger(MetadataXMLService.class);

	@GET
	@Produces("application/xml")
	public Response getMetadata(@QueryParam("url") String url, @QueryParam("span") int span,
			@QueryParam("reload") boolean reload)
	{
		NDC.push("format: xml | url:" + url + " | span:" + span);
		long requestTime = System.currentTimeMillis();
		logger.debug("Requested at: " + (new Date(requestTime)));

		Response resp = null;
		if (url != null)
		{
			ParsedURL purl = ParsedURL.getAbsolute(url);
			if (purl != null)
			{
				MetadataServiceHelper helper = new MetadataServiceHelper(StringFormat.XML);
				resp = helper.getMetadata(purl, span, reload);
			}
		}

		// invalid param
		if (resp == null)
			resp = Response.status(Status.BAD_REQUEST).entity(SemanticServiceErrorCodes.BAD_REQUEST)
					.type(MediaType.TEXT_PLAIN).build();

		logger.debug("Time taken (ms): " + (System.currentTimeMillis() - requestTime));

		NDC.remove();
		return resp;
	}
}
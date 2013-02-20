/**
 * 
 */
package ecologylab.bigsemantics.service.downloader;

import java.io.IOException;
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

import ecologylab.bigsemantics.downloaders.NetworkDocumentDownloader;
import ecologylab.bigsemantics.downloaders.oodss.DownloadResponse;
import ecologylab.bigsemantics.service.SemanticServiceErrorCodes;
import ecologylab.bigsemantics.service.SemanticServiceScope;
import ecologylab.logging.ILogger;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * downloader root resource
 * 
 * @author ajit
 * 
 */

@Path("/download")
@Component
@Scope("singleton")
public class HTTPDownloaderInstance
{
	static ILogger	logger	= SemanticServiceScope.get().getLoggerFactory()
															.getLogger(HTTPDownloaderInstance.class);

	@GET
	@Produces("application/xml")
	public Response performService(@QueryParam("url") String url,
			@QueryParam("userAgentString") String userAgentString)
	{
		NDC.push("url:" + url + " user_agent: " + userAgentString);
		long millis = System.currentTimeMillis();
		logger.debug("Requested at: " + (new Date(millis)));

		Response resp = null;
		ParsedURL purl = ParsedURL.getAbsolute(url);
		if (purl != null)
		{
			NetworkDocumentDownloader documentDownloader = new NetworkDocumentDownloader(purl,
					userAgentString);
			// boolean bChanged = false;
			ParsedURL redirectedLocation = null;
			String location = null;
			String mimeType = null;
			try
			{
				documentDownloader.connect(true);
				// additional location
				redirectedLocation = documentDownloader.getRedirectedLocation();
				// local saved location
				location = documentDownloader.getLocalLocation();
				// mimeType
				mimeType = documentDownloader.mimeType();

				logger.debug("document from url: " + url + " downloaded to: " + location + " in total "
						+ (System.currentTimeMillis() - millis) + "ms");

				String responseBody;
				responseBody = SimplTypesScope.serialize(
						new DownloadResponse(redirectedLocation, location, mimeType), StringFormat.XML)
						.toString();
				resp = Response.status(Status.OK).entity(responseBody).build();
			}
			catch (IOException e)
			{
				logger.error("Cannot download url: " + url + " because of error: " + e.getMessage());
				resp = Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(SemanticServiceErrorCodes.INTERNAL_ERROR).type(MediaType.TEXT_PLAIN).build();
			}
			catch (SIMPLTranslationException e)
			{
				logger.error("exception while serializing DownloadResponse[" + url + "]: %s",
						e.getMessage());
				resp = Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(SemanticServiceErrorCodes.INTERNAL_ERROR).type(MediaType.TEXT_PLAIN).build();
			}
		}

		// invalid param
		if (resp == null)
			resp = Response.status(Status.BAD_REQUEST).entity(SemanticServiceErrorCodes.BAD_REQUEST)
					.type(MediaType.TEXT_PLAIN).build();

		logger.debug("Total Time taken for url : " + url + (System.currentTimeMillis() - millis)
				+ " ms.");

		NDC.remove();
		return resp;
	}
}

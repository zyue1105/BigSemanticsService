package ecologylab.bigsemantics.service.couch;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

@Path("/query_metadata_keyword.json")
@Component
@Scope("singleton")
public class QueryCouchDBKeyword
{
	private static String host = "ecoarray0";
	private static String port = "9084";
	private static String database = "exdb";

	@GET
	@Produces("application/JSON")
	public Response queryMetadataKeyword(@QueryParam("callback") String callback,
			@QueryParam("keyword") String keyword)
	{
		int statusCode;
		Response response = null;
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse httpResponse = null;
		HttpGet getRequest;

		try
		{
			getRequest = new HttpGet("http://" + host + ":" + port + "/"
					+ database
					+ "/_design/query_metadata_keyword/_view/by_keyword?key=%22"
					+ keyword + "%22&include_docs=true");

			httpResponse = client.execute(getRequest);

			statusCode = httpResponse.getStatusLine().getStatusCode();

			if (statusCode == 200)
				response = Response
						.status(Status.OK)
						.entity(callback + "(" + EntityUtils.toString(httpResponse.getEntity()) + ");").build();
			else
				response = Response.status(Status.fromStatusCode(statusCode)).entity(null).type(MediaType.TEXT_PLAIN).build();
				
		}
		catch (IOException e)
		{
			e.printStackTrace();
			
			response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(null).type(MediaType.TEXT_PLAIN).build();		
		}
		catch (HttpException e)
		{
			e.printStackTrace();
			
			response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(null).type(MediaType.TEXT_PLAIN).build();		
		}
		catch (URISyntaxException e)
		{
			e.printStackTrace();
			
			response = Response.status(Status.BAD_REQUEST).entity(null).type(MediaType.TEXT_PLAIN).build();		
		}

		return response;
	}

}

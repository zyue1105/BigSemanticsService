/**
 * 
 */
package ecologylab.bigsemantics.service.downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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

import ecologylab.bigsemantics.downloaders.oodss.DownloadResponse;
import ecologylab.bigsemantics.filestorage.FileSystemStorage;
import ecologylab.bigsemantics.service.SemanticServiceErrorCodes;
import ecologylab.bigsemantics.service.SemanticServiceScope;
import ecologylab.logging.ILogger;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * A fake downloader that uses wget for downloading. The purpose is to see if
 * NetworkDocumentDownloader causes performance issues for downloading.
 * 
 * @author quyin
 * 
 */

@Path("/fakedownload")
@Component
@Scope("singleton")
public class FakeDownloaderInstance
{
  static ILogger logger = SemanticServiceScope.get().getLoggerFactory()
                            .getLogger(FakeDownloaderInstance.class);

  @GET
  @Produces("application/xml")
  public Response performService(@QueryParam("url") String url,
                                 @QueryParam("userAgentString") String userAgentString)
  {
    NDC.push("url:" + url + " user_agent: " + userAgentString);
    long millis = System.currentTimeMillis();
    logger.debug("request received: url:" + url + " user_agent: " + userAgentString);

    Response resp = null;
    ParsedURL purl = ParsedURL.getAbsolute(url);
    if (purl != null)
    {
      WgetDownloader dl = new WgetDownloader();
      // boolean bChanged = false;
      ParsedURL redirectedLocation = null;
      String location = null;
      String mimeType = null;
      try
      {
        dl.wget(purl, userAgentString);
        // additional location
        redirectedLocation = dl.redirectedLocation;
        // local saved location
        location = dl.localLocation;
        // mimeType
        mimeType = dl.mimeType;

        logger.debug("document from url: " + url + " downloaded to: " + location + " in total "
                     + (System.currentTimeMillis() - millis) + "ms");

        String responseBody;
        DownloadResponse respObj = new DownloadResponse(redirectedLocation,
                                                        location,
                                                        mimeType);
        responseBody = SimplTypesScope.serialize(respObj, StringFormat.XML).toString();
        resp = Response.status(Status.OK).entity(responseBody).build();
      }
      // catch (IOException e)
      // {
      // logger.error("Cannot download url: " + url + " because of error: " + e.getMessage());
      // resp = Response.status(Status.INTERNAL_SERVER_ERROR)
      // .entity(SemanticServiceErrorCodes.INTERNAL_ERROR).type(MediaType.TEXT_PLAIN).build();
      // }
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

    logger.debug("Total Time taken for url : " + url + " - "
                 + (System.currentTimeMillis() - millis) + " ms.");

    NDC.remove();
    return resp;
  }

  static class WgetDownloader
  {

    public ParsedURL redirectedLocation;

    public String    localLocation;

    public String    mimeType;

    public int wget(ParsedURL purl, String userAgent)
    {
      File f = FileSystemStorage.getDestinationFileAndCreateDirs("/bigsemantics-service/cache",
                                                                 purl,
                                                                 "html");
      ProcessBuilder pb = new ProcessBuilder();
      pb.command("/usr/local/bin/wget", "-O", f.getAbsolutePath(), "-U", userAgent, purl.toString());
      pb.redirectErrorStream(true);
      Process p = null;
      try
      {
        p = pb.start();
        p.waitFor();
        int exitValue = p.exitValue();
        if (exitValue == 0)
        {
          if (f.exists() && f.length() > 0)
          {
            localLocation = f.getAbsolutePath();
            mimeType = "text/html";
            return 200;
          }
        }
        else
        {
          InputStream is = p.getInputStream();
          BufferedReader br = new BufferedReader(new InputStreamReader(is));
          StringBuffer sb = new StringBuffer();
          while (true)
          {
            String line = br.readLine();
            if (line == null)
              break;
            sb.append(line).append("\n");
          }
          logger.error("wget() failed:\n%s", sb.toString());
          return exitValue;
        }
      }
      catch (IOException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      catch (InterruptedException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      return 0;
    }
  }

}

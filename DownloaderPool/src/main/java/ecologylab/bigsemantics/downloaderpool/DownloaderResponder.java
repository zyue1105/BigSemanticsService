package ecologylab.bigsemantics.downloaderpool;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.bigsemantics.downloaderpool.DownloaderResult.State;
import ecologylab.generic.Continuation;

/**
 * This object sends the downloading result to the controller. An object of this class corresponds
 * to a downloading task (represented by a Task object, and associated with a Page object).
 * 
 * @author quyin
 */
public class DownloaderResponder implements Continuation<Page>
{

  static String CONTROLLER_REPORT_URL = "http://localhost:8080/DownloaderPool/report";

  static Logger logger                = LoggerFactory.getLogger(DownloaderResponder.class);

  Downloader    downloader;

  /**
   * The task that we need to respond to.
   */
  private Task  associatedTask;

  public DownloaderResponder(Task associatedTask)
  {
    this.associatedTask = associatedTask;
  }

  @Override
  public void callback(Page page)
  {
    DownloaderResult result = page.getResult();
    logger.info("Downloading result for Task[{}][{}]: {}: {}",
                associatedTask.getId(),
                associatedTask.getPurl(),
                result.getHttpRespCode(),
                result.getHttpRespMsg());

    searchPatternInContent(result, associatedTask.getFailRegex(), State.ERR_CONTENT);
    searchPatternInContent(result, associatedTask.getBanRegex(), State.ERR_BANNED);

    Map<String, String> params = new HashMap<String, String>();
    params.put("wid", downloader.getName());
    params.put("tid", associatedTask.getId());
    params.put("state", result.getState().toString());
    params.put("code", String.valueOf(result.getHttpRespCode()));
    params.put("msg", result.getHttpRespMsg());
    params.put("mime", result.getMimeType());
    params.put("charset", result.getCharset());
    params.put("content", result.getContent());
    params.put("descr", result.getContentDescription());
    logger.info("form params: " + params);
    HttpPost post = Utils.generatePostRequest(CONTROLLER_REPORT_URL, params);

    BasicResponse postResult = new BasicResponse();
    try
    {
      HttpClient client = new DefaultHttpClient();
      client.execute(post, new BasicResponseHandler(postResult));
    }
    catch (ClientProtocolException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    if (postResult.getHttpRespCode() == HttpStatus.SC_OK)
    {
      logger.info("Downloading result reported.");
    }
    else
    {
      logger.error("Error reporting downloading result: {}: {}",
                   postResult.getHttpRespCode(),
                   postResult.getHttpRespMsg());
    }
  }

  /**
   * Search pattern from the content of the given downloading result.
   * 
   * @param result
   *          The downloading result.
   * @param regex
   *          The pattern to search for.
   * @param newState
   *          The result will be set to this state if the pattern is found in the content of the
   *          result.
   * @return true if the pattern has been found in the content of the result, otherwise false.
   */
  private boolean searchPatternInContent(DownloaderResult result, String regex, State newState)
  {
    String content = result.getContent();
    if (content != null && content.length() > 0 && regex != null && regex.length() > 0)
    {
      Pattern p = Pattern.compile(regex);
      Matcher m = p.matcher(content);
      if (m.find())
      {
        result.setState(newState);
        return true;
      }
    }
    return false;
  }

}

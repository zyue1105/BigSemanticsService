package ecologylab.bigsemantics.downloaderpool;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  /**
   * The task that we need to respond to.
   */
  private Task associatedTask;

  public DownloaderResponder(Task associatedTask)
  {
    this.associatedTask = associatedTask;
  }

  @Override
  public void callback(Page page)
  {
    DownloaderResult result = page.getResult();

    searchPatternInContent(result, associatedTask.getFailRegex(), State.ERR_CONTENT);
    searchPatternInContent(result, associatedTask.getBanRegex(), State.ERR_BANNED);

    // TODO make a HTTP PUT request to the controller to send back result.
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

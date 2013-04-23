package ecologylab.bigsemantics.downloaderpool;

import ecologylab.generic.Continuation;

/**
 * 
 * @author quyin
 */
public class DownloaderResponder implements Continuation<Page>
{

  @Override
  public void callback(Page page)
  {
    DownloaderResult result = page.getResult();
    // TODO make a HTTP PUT request to the controller to send back result.
  }

}

package ecologylab.bigsemantics.downloaderpool;

/**
 * 
 * @author quyin
 * 
 */
public class MockDownloaderResponder extends DownloaderResponder
{

  int  numCallbacks = 0;

  Page lastCallbackPage;

  @Override
  public void callback(Page page)
  {
    numCallbacks++;
    System.err.println("callback(): " + page);
    lastCallbackPage = page;
  }

}

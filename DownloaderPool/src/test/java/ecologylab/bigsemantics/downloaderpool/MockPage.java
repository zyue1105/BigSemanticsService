package ecologylab.bigsemantics.downloaderpool;

/**
 * 
 * @author quyin
 * 
 */
public class MockPage extends Page
{

  boolean performed = false;

  @Override
  public void performDownload()
  {
    performed = true;
    System.err.println("MockPage: performDownload()");
  }

}

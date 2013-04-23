package ecologylab.bigsemantics.downloaderpool;

/**
 * 
 * @author quyin
 *
 */
public class Utils
{

  public static void sleep(long millisec)
  {
    try
    {
      Thread.sleep(millisec);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }

}

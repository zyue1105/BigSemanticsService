package ecologylab.bigsemantics.downloaderpool;

/**
 * Util methods.
 * 
 * @author quyin
 */
public class Utils
{

  /**
   * Sleep for the given time in milliseconds. The sleep can be interrupted but it won't throw
   * exceptions.
   * 
   * @param millisec
   */
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

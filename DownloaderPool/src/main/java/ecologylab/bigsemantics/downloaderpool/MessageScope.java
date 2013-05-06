package ecologylab.bigsemantics.downloaderpool;

import ecologylab.serialization.SimplTypesScope;

/**
 * 
 * @author quyin
 */
public class MessageScope
{

  public static final String  NAME    = "DownloaderPoolMessages";

  public static final Class[] CLASSES = {
      Task.class,
      DownloaderRequest.class,
      AssignedTasks.class,
      BasicResponse.class,
      DownloaderResult.class,
  };

  public static SimplTypesScope get()
  {
    return SimplTypesScope.get(NAME, CLASSES);
  }

}

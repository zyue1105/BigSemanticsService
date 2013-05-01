package ecologylab.bigsemantics.downloaderpool.services;

import ecologylab.bigsemantics.downloaderpool.Controller;
import ecologylab.bigsemantics.downloaderpool.Task;

/**
 * 
 * @author quyin
 */
public class ViewTask extends Action
{

  /**
   * 
   * @param id
   * @param url
   * @return
   */
  public Task findTask(String id, String url)
  {
    Controller ctrl = getController();
    if (id != null && id.length() > 0)
    {
      return ctrl.getTask(id);
    }
    else if (url != null && url.length() > 0)
    {
      return ctrl.getTaskByUri(url);
    }
    return null;
  }
  
}

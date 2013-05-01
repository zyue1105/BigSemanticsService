package ecologylab.bigsemantics.downloaderpool;

import java.util.List;

import ecologylab.serialization.annotations.simpl_collection;

/**
 * 
 * @author quyin
 */
public class AssignedTasks
{

  @simpl_collection("task")
  private List<Task> tasks;

  public List<Task> getTasks()
  {
    return tasks;
  }

  public void setTasks(List<Task> tasks)
  {
    this.tasks = tasks;
  }
  
}

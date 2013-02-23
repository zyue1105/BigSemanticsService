package ecologylab.bigsemantics.service.downloaderpool;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 
 * @author quyin
 * 
 */
public class Controller
{

  ConcurrentLinkedDeque<Task>     waitingTasks;

  ConcurrentHashMap<String, Task> tasksByUri;

  public Controller()
  {
    waitingTasks = new ConcurrentLinkedDeque<Task>();
    tasksByUri = new ConcurrentHashMap<String, Task>();
  }

  public void queueTask(Task task)
  {
    if (task == null)
      throw new IllegalArgumentException("Task to queue cannot be null.");
    if (task.getId() == null || task.getId().length() == 0)
      throw new IllegalArgumentException("Task to queue must have an ID.");
    if (task.getUri() == null || task.getUri().length() == 0)
      throw new IllegalArgumentException("Task to queue must have a URI.");

    Task existingTask = tasksByUri.putIfAbsent(task.getUri(), task);
    if (existingTask != null)
    {
      task.setState(TaskState.DEDUP);
      existingTask.addClients(task.getClients());
      return;
    }
    task.setState(TaskState.WAITING);
    waitingTasks.add(task);
  }

  /**
   * (Note that this operation is costly! It needs to traverse the whole queue.)
   * 
   * @return
   */
  public int countWaitingTasks()
  {
    return waitingTasks.size();
  }

  public List<Task> getTasksForWork(DownloaderRequest req)
  {
    int n = req.getMaxTaskCount();
    if (n <= 0)
      return null;

    List<Task> tasks = new ArrayList<Task>();

    Stack<Task> unacceptedTasks = new Stack<Task>();
    while (tasks.size() < n)
    {
      Task t = waitingTasks.pollFirst();
      if (t == null)
        break;
      t.setState(TaskState.MATCHING);
      if (req.accept(t.getPurl()))
      {
        tasks.add(t);
        t.setState(TaskState.ONGOING);
      }
      else
      {
        unacceptedTasks.push(t);
      }
    }
    while (!unacceptedTasks.empty())
    {
      Task t = unacceptedTasks.pop();
      t.setState(TaskState.WAITING);
      waitingTasks.offerFirst(t);
    }

    return tasks;
  }

  private void moveToWaitingTask(Task ongoingTask)
  {
    // TODO reset task properties
    ongoingTask.setState(TaskState.WAITING);
    waitingTasks.offer(ongoingTask);
  }

  /**
   * (Note that this operation is costly! It needs to traverse the whole task map.)
   * 
   * @return
   */
  public int countOngoingTasks()
  {
    int n = 0;
    for (Task t : tasksByUri.values())
    {
      if (t.getState() == TaskState.ONGOING)
        n++;
    }
    return n;
  }

  public void countDownTasks()
  {
    for (Task t : tasksByUri.values())
    {
      t.countDown();
      if (t.getCounter() <= 0)
      {
        if (t.getAttempts() >= t.getMaxAttempts())
        {
          // TODO terminate this task
          t.setState(TaskState.TERMINATED);
          tasksByUri.remove(t.getUri());
          break;
        }
        else
        {
          t.setState(TaskState.ATTEMPT_FAILED);
          t.resetCounter();
          moveToWaitingTask(t);
        }
      }
    }
  }

}

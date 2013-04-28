package ecologylab.bigsemantics.downloaderpool;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import ecologylab.bigsemantics.downloaderpool.Task.State;

/**
 * The central controller that works with a set of downloaders. The controller accepts requests from
 * clients as tasks, assigns tasks to distributed downloaders, collects results, and returns to
 * clients. The controller doesn't remember previous tasks (memoryless), for simplicity.
 * 
 * @author quyin
 */
public class Controller
{

  /**
   * The maximum number of tasks that can be assigned to a downloader.
   */
  private int                             maxTasksPerDownloader = 10;

  /**
   * The tasks that are received from clients, but not yet been assigned to downloaders.
   */
  private ConcurrentLinkedDeque<Task>     waitingTasks;

  /**
   * Indexing waiting and ongoing tasks. Finished tasks, either succeeded or terminated due to too
   * many failures, will be removed from this map. This implies that the controller is not
   * remembering previous tasks.
   */
  private ConcurrentHashMap<String, Task> tasksByUri;

  public Controller()
  {
    waitingTasks = new ConcurrentLinkedDeque<Task>();
    tasksByUri = new ConcurrentHashMap<String, Task>();
  }

  /**
   * Queue a new task to this controller.
   * 
   * @param task
   */
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
      task.setState(State.DEDUP);
      existingTask.addClients(task.getClients());
      return;
    }
    task.setState(State.WAITING);
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

  /**
   * Process a DownloaderRequest and gives a list of appropriate tasks for this request. The tasks
   * are ordered the same as when they are queued.
   * 
   * @param req
   * @return
   */
  public List<Task> getTasksForWork(DownloaderRequest req)
  {
    int n = req.getMaxTaskCount();
    if (n < 0)
      return null;
    if (n == 0)
      n = maxTasksPerDownloader;

    List<Task> tasks = new ArrayList<Task>();

    Stack<Task> unacceptedTasks = new Stack<Task>();
    while (tasks.size() < n)
    {
      Task t = waitingTasks.pollFirst();
      if (t == null)
        break;
      t.setState(State.MATCHING);
      if (req.accept(t.getPurl()))
      {
        tasks.add(t);
        t.setState(State.ONGOING);
      }
      else
      {
        unacceptedTasks.push(t);
      }
    }
    while (!unacceptedTasks.empty())
    {
      Task t = unacceptedTasks.pop();
      t.setState(State.WAITING);
      waitingTasks.offerFirst(t);
    }

    return tasks;
  }

  /**
   * When a task is attempted but failed, and it has not yet reached the maximum number of allowed
   * attempts, it will re-enter the waiting tasks queue for another attempt.
   * 
   * @param ongoingTask
   */
  private void moveToWaitingTask(Task ongoingTask)
  {
    // TODO reset task properties
    ongoingTask.setState(State.WAITING);
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
      if (t.getState() == State.ONGOING)
        n++;
    }
    return n;
  }

  /**
   * Every call to this method counts down all ongoing tasks. When an ongoing task counts down to 0,
   * the attempt fails (request out of time).
   */
  public void countDownTasks(int passedTime)
  {
    for (Task t : tasksByUri.values())
    {
      t.countDown(passedTime);
      if (t.getTimer() <= 0)
      {
        if (t.getAttempts() >= t.getMaxAttempts())
        {
          // TODO notify the client that this task has been terminated
          t.setState(State.TERMINATED);
          tasksByUri.remove(t.getUri());
          break;
        }
        else
        {
          t.setState(State.ATTEMPT_FAILED);
          t.resetTimer();
          moveToWaitingTask(t);
        }
      }
    }
  }

}

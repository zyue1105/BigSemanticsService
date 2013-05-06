package ecologylab.bigsemantics.downloaderpool;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.bigsemantics.downloaderpool.Task.State;

/**
 * The central controller that works with a set of downloaders. The controller accepts requests from
 * clients as tasks, assigns tasks to distributed downloaders, collects results, and returns to
 * clients. The controller doesn't remember previous tasks (memoryless), for simplicity.
 * 
 * @author quyin
 */
public class Controller extends Routine
{

  private static final int                WAIT_BETWEEN_COUNTDOWN = 500;

  private static Logger                   logger;

  static
  {
    logger = LoggerFactory.getLogger(Controller.class);
  }

  /**
   * The maximum length of the task ID.
   */
  private int                             taskIdLen              = 11;

  /**
   * The maximum number of tasks that can be assigned to a downloader.
   */
  private int                             maxTasksPerDownloader  = 10;

  /**
   * The tasks that are received from clients, but not yet been assigned to downloaders.
   */
  private ConcurrentLinkedDeque<Task>     waitingTasks;

  /**
   * Indexing waiting and ongoing tasks by URL. Finished tasks, either succeeded or terminated due
   * to too many failures, will be removed from this map. This implies that the controller is not
   * remembering previous tasks.
   */
  private ConcurrentHashMap<String, Task> tasksByUri;

  /**
   * Indexing all tasks by ID. We regard conflicts of keys as impossible.
   */
  private Cache                           allTasksById;

  /**
   * Indexing all tasks by URL. When there is conflict this only stores the latest one.
   */
  private Cache                           allTasksByUri;

  public Controller()
  {
    waitingTasks = new ConcurrentLinkedDeque<Task>();
    tasksByUri = new ConcurrentHashMap<String, Task>();
    CacheManager cacheManager = CacheManager.getInstance();

    cacheManager.addCacheIfAbsent("tasks-by-id");
    allTasksById = cacheManager.getCache("tasks-by-id");

    cacheManager.addCacheIfAbsent("tasks-by-uri");
    allTasksByUri = cacheManager.getCache("tasks-by-uri");

    setSleepBetweenLoop(WAIT_BETWEEN_COUNTDOWN);

    setReady();
    logger.info("Controller is constructed.");
  }

  public int getTaskIdLen()
  {
    return taskIdLen;
  }

  public void setTaskIdLen(int taskIdLen)
  {
    this.taskIdLen = taskIdLen;
  }

  public Task getTask(String id)
  {
    Element element = allTasksById.get(id);
    return element == null ? null : (Task) element.getObjectValue();
  }

  public Task getTaskByUri(String uri)
  {
    Element element = allTasksByUri.get(uri);
    return element == null ? null : (Task) element.getObjectValue();
  }

  /**
   * Queue a new task to this controller.
   * 
   * @param task
   */
  public void queueTask(final Task task)
  {
    if (task == null)
      throw new IllegalArgumentException("Task to queue cannot be null.");
    if (task.getId() == null || task.getId().length() == 0)
      throw new IllegalArgumentException("Task to queue must have an ID.");
    if (task.getUri() == null || task.getUri().length() == 0)
      throw new IllegalArgumentException("Task to queue must have a URI.");

    allTasksById.put(new Element(task.getId(), task));
    allTasksByUri.put(new Element(task.getUri(), task));

    Task existingTask = tasksByUri.putIfAbsent(task.getUri(), task);
    if (existingTask != null)
    {
      task.setState(State.DEDUP);
      existingTask.addClients(task.getClients());
      existingTask.addObserver(new Observer()
      {
        @Override
        public void update(Observable arg0, Object arg1)
        {
          task.notifyObservers(arg1);
        }
      });
      return;
    }
    task.setState(State.WAITING);
    logger.info("enqueuing task " + task);
    waitingTasks.add(task);
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
    ongoingTask.setState(State.WAITING);
    waitingTasks.offer(ongoingTask);
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
          t.setState(State.TERMINATED);
          tasksByUri.remove(t.getUri());
          t.notifyObservers();
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

  public void report(String taskId, DownloaderResult result)
  {
    Task task = getTask(taskId);
    if (task != null)
    {
      task.setResult(result);
      tasksByUri.remove(task.getUri());
      task.setState(State.RESPONDED);
    }
  }

  private long t = -1;

  @Override
  void routineBody()
  {
    if (t < 0)
    {
      t = System.currentTimeMillis();
    }
    else
    {
      long dt = System.currentTimeMillis() - t;
      countDownTasks((int) dt);
      t += dt;
    }
  }
}

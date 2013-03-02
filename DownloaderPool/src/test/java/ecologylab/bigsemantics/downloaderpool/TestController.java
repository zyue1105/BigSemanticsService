package ecologylab.bigsemantics.downloaderpool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ecologylab.bigsemantics.downloaderpool.ClientStub;
import ecologylab.bigsemantics.downloaderpool.Controller;
import ecologylab.bigsemantics.downloaderpool.DownloaderRequest;
import ecologylab.bigsemantics.downloaderpool.Task;
import ecologylab.bigsemantics.downloaderpool.TaskState;

/**
 * 
 * @author quyin
 *
 */
public class TestController
{

  Controller ctrl;

  @Before
  public void init()
  {
    ctrl = new Controller();
  }

  @Test
  public void testConstruction()
  {
    assertNotNull(ctrl);
  }

  @Test
  public void testAddTask()
  {
    Task task = new Task("my-task-0", "http://example.com");
    assertEquals(TaskState.INIT, task.getState());
    ctrl.queueTask(task);
    assertEquals(1, ctrl.countWaitingTasks());
    assertEquals(TaskState.WAITING, task.getState());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddNullTask()
  {
    ctrl.queueTask(null);
  }

  @Test
  public void testAddTasksWithSameUri()
  {
    Task task1 = new Task("my-task-1", "http://example.com");
    task1.addClient(new ClientStub("client 1"));
    ctrl.queueTask(task1);
    Task task2 = new Task("my-task-2", "http://example.com");
    task2.addClient(new ClientStub("client 2"));
    ctrl.queueTask(task2);
    assertEquals(1, ctrl.countWaitingTasks());
    assertEquals(TaskState.DEDUP, task2.getState());
    assertEquals(2, task1.getClients().size());
  }
  
  @Test
  public void testGetTasksForWork()
  {
    String uri1 = "http://example.com";
    String uri2 = "http://www.mydomain.com/index.html";
    String uri3 = "http://lib.resources.com/resource/1";
    String uri4 = "http://book.mylibrary.com/search?q=java";
    
    Task task1 = new Task("my-task-1", uri1);
    ctrl.queueTask(task1);
    Task task2 = new Task("my-task-2", uri2);
    ctrl.queueTask(task2);
    Task task3 = new Task("my-task-3", uri3);
    ctrl.queueTask(task3);
    Task task4 = new Task("my-task-4", uri4);
    ctrl.queueTask(task4);
    
    DownloaderRequest req = new DownloaderRequest();
    req.addToBlacklist("example.com");
    req.addToBlacklist("mydomain.com");
    req.setMaxTaskCount(10);
    
    List<Task> tasks = ctrl.getTasksForWork(req);
    assertNotNull(tasks);
    assertEquals(2, tasks.size());
    assertEquals(uri3, tasks.get(0).getUri());
    assertEquals(uri4, tasks.get(1).getUri());
    assertEquals(2, ctrl.countOngoingTasks());
    assertEquals(TaskState.WAITING, task1.getState());
    assertEquals(TaskState.WAITING, task2.getState());
    assertEquals(TaskState.ONGOING, task3.getState());
    assertEquals(TaskState.ONGOING, task4.getState());
  }
  
  @Test
  public void testCountDownTasks()
  {
    Task task = new Task("my-task-0", "http://example.com");
    task.setMaxCounter(2);
    task.setMaxAttempts(2);
    ctrl.queueTask(task);
    
    DownloaderRequest req = new DownloaderRequest();
    req.setMaxTaskCount(1);
    ctrl.getTasksForWork(req);
    assertEquals(1, ctrl.countOngoingTasks());
    
    ctrl.countDownTasks();
    assertEquals(1, task.getCounter());
    assertEquals(1, ctrl.countOngoingTasks());
    assertEquals(0, ctrl.countWaitingTasks());
    
    ctrl.countDownTasks();
    assertEquals(2, task.getCounter()); // task timed out, moved to waiting queue
    assertEquals(0, ctrl.countOngoingTasks());
    assertEquals(1, ctrl.countWaitingTasks());
    
    ctrl.getTasksForWork(req); // task reassigned
    
    ctrl.countDownTasks();
    assertEquals(1, task.getCounter());
    assertEquals(1, ctrl.countOngoingTasks());
    assertEquals(0, ctrl.countWaitingTasks());
    
    ctrl.countDownTasks();
    assertEquals(0, task.getCounter()); // task timed out, and tried maxAttempts times, terminated
    assertEquals(0, ctrl.countOngoingTasks());
    assertEquals(0, ctrl.countWaitingTasks());
    
    assertEquals(TaskState.TERMINATED, task.getState());
  }
  
}

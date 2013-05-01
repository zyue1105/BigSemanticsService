package ecologylab.bigsemantics.downloaderpool.services;

import java.util.Observable;
import java.util.Observer;

import ecologylab.bigsemantics.downloaderpool.Controller;
import ecologylab.bigsemantics.downloaderpool.DownloaderResult;
import ecologylab.bigsemantics.downloaderpool.Task;
import ecologylab.bigsemantics.downloaderpool.Task.ObservableEventType;
import ecologylab.bigsemantics.downloaderpool.Utils;
import ecologylab.generic.StringBuilderBaseUtils;

/**
 * 
 * @author quyin
 */
public class Download extends Action
{

  private static final long TIMEOUT = 60000;

  public DownloaderResult requestDownload(String remoteIp,
                                          String url,
                                          String userAgent,
                                          int interval,
                                          int longInterval,
                                          int numOfAttempts,
                                          int timeOfAttempt,
                                          String failPattern,
                                          String banPattern)
  {
    Controller ctrl = getController();
    
    StringBuilder sb = StringBuilderBaseUtils.acquire();
    sb.append(remoteIp).append("|").append(System.currentTimeMillis()).append("|").append(url);
    byte[] hash = Utils.hashToBytes(sb.toString());
    String tid = Utils.base64urlEncode(hash).substring(0, ctrl.getTaskIdLen());
    StringBuilderBaseUtils.release(sb);

    Task task = new Task(tid, url);
    task.setUserAgent(userAgent);
    task.setDomainInterval(interval);
    task.setDomainLongInterval(longInterval);
    task.setMaxAttempts(numOfAttempts);
    task.setAttemptTime(timeOfAttempt);
    task.setFailRegex(failPattern);
    task.setBanRegex(banPattern);

    final Object lock = new Object();
    Observer taskOb = new Observer()
    {
      @Override
      public void update(Observable o, Object arg)
      {
        assert o instanceof Task;
        assert arg instanceof Task.ObservableEventType;

        Task task = (Task) o;
        ObservableEventType type = (ObservableEventType) arg;

        if (type == ObservableEventType.STATE_CHANGE)
        {
          switch (task.getState())
          {
          case RESPONDED:
          case TERMINATED:
            synchronized (lock)
            {
              lock.notifyAll();
            }
            task.deleteObserver(this);
            break;
          default:
            // ignore other changes
            break;
          }
        }
      }
    };
    task.addObserver(taskOb);

    ctrl.queueTask(task);
    logger.info("Task queued: fromIP={}, id={}, url=[{}]", remoteIp, task.getId(), task.getUri());

    synchronized (lock)
    {
      try
      {
        lock.wait(TIMEOUT);
      }
      catch (InterruptedException e)
      {
        logger.warn("Waiting for task interrupted.");
        e.printStackTrace();
      }
    }

    return task.getResult();
  }

}

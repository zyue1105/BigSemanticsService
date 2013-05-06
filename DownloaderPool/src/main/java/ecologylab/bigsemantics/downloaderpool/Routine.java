package ecologylab.bigsemantics.downloaderpool;

/**
 * A utility class abstracting a runnable (loopy) routine that can be started, paused, and stopped.
 * 
 * @author quyin
 */
public abstract class Routine implements Runnable
{

  /**
   * The status of a Routine.
   * 
   * @author quyin
   */
  public static enum Status
  {
    NEW, READY, RUNNING, PAUSED, STOP_PENDING, STOPPED,
  }

  /**
   * The time to sleep between looping the routine body, in milliseconds.
   */
  private long   sleepBetweenLoop = 300;

  /**
   * The status of this Routine
   */
  private Status status;

  private Object lockStatus       = new Object();

  public Status getStatus()
  {
    return status;
  }

  public long getSleepBetweenLoop()
  {
    return sleepBetweenLoop;
  }

  public void setSleepBetweenLoop(long sleepBetweenLoop)
  {
    this.sleepBetweenLoop = sleepBetweenLoop;
  }

  /**
   * The constructor. Will call init() for initialization.
   */
  public Routine()
  {
    super();
    status = Status.NEW;
  }

  /**
   * The Routine implementation should call this method after getting ready to run.
   */
  protected void setReady()
  {
    status = Status.READY;
  }

  /**
   * The body of the routine loop. This method will be invoked repeatedly to do routine work, unless
   * the Routine is paused or stopped.
   */
  abstract void routineBody();

  @Override
  public void run()
  {
    while (status == Status.RUNNING || status == Status.PAUSED)
    {
      if (status == Status.RUNNING)
      {
        routineBody();
      }

      Utils.sleep(sleepBetweenLoop);
    }

    synchronized (lockStatus)
    {
      status = Status.STOPPED;
      lockStatus.notifyAll();
    }
  }

  public void start()
  {
    if (status == Status.READY)
    {
      synchronized (lockStatus)
      {
        if (status == Status.READY)
        {
          status = Status.RUNNING;
          Thread t = new Thread(this);
          t.start();
        }
      }
    }
  }

  public void pause()
  {
    if (status == Status.RUNNING)
    {
      synchronized (lockStatus)
      {
        if (status == Status.RUNNING)
        {
          status = Status.PAUSED;
        }
      }
    }
  }

  public void stop()
  {
    if (status == Status.RUNNING || status == Status.PAUSED)
    {
      synchronized (lockStatus)
      {
        if (status == Status.RUNNING || status == Status.PAUSED)
        {
          status = Status.STOP_PENDING;
          try
          {
            lockStatus.wait();
            if (status == Status.STOPPED)
            {
              return;
            }
            else
            {
              throw new RuntimeException("Cannot stop " + this + "!");
            }
          }
          catch (InterruptedException e)
          {
            e.printStackTrace();
          }
        }
      }
    }
  }

}

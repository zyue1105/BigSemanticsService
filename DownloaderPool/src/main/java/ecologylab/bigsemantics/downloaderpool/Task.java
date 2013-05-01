package ecologylab.bigsemantics.downloaderpool;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * The representation of a downloading task. This representation is transferred from clients to the
 * controller and then to distributed downloaders, for communication.
 * 
 * @author quyin
 * 
 */
public class Task extends Observable
{

  public static enum ObservableEventType
  {
    STATE_CHANGE, COUNT_DOWN
  }

  /**
   * The state of a task.
   * 
   * @author quyin
   */
  public static enum State
  {

    /**
     * The task is newly created.
     */
    INIT,

    /**
     * The task is a duplication of an existing task.
     */
    DEDUP,

    /**
     * The task is waiting in the queue for being assigned to a downloader.
     */
    WAITING,

    /**
     * The task is taken out of the queue and being matched with a downloader request.
     */
    MATCHING,

    /**
     * The task is matched with a downloader request and is sent to the downloader for execution.
     */
    ONGOING,

    /**
     * The task is attempted but failed for some reason, and will be reentered into the queue soon.
     */
    ATTEMPT_FAILED,

    /**
     * The task has been attempted for several times without success, thus marked as failed and
     * terminated.
     */
    TERMINATED,

    /**
     * The task has been responded from a downloader, meaning either it has been downloaded or there
     * is an error occurrd during downloading it.
     */
    RESPONDED,

  }

  /**
   * Globally unique identifier for this task.
   */
  @simpl_scalar
  private String           id;

  /**
   * The state of this task.
   */
  @simpl_scalar
  private State            state;

  /**
   * The URI to download.
   */
  @simpl_scalar
  private String           uri;

  /**
   * The user agent string that should be used.
   */
  @simpl_scalar
  private String           userAgent;

  /**
   * Normal interval waiting between requests to this domain, in milliseconds.
   */
  @simpl_scalar
  private int              domainInterval;

  /**
   * Long interval waiting between requests to this domain, in milliseconds. This can be used for
   * example after being banned from that domain.
   */
  @simpl_scalar
  private int              domainLongInterval;

  /**
   * Maximum number of attempts that are allowed. In each attempt, the task is assigned to a
   * downloader for accessing and downloading. If more than this number of attempts have been made
   * and it didn't succeed, the task is seen as undoable, thus terminated.
   */
  @simpl_scalar
  private int              maxAttempts;

  /**
   * The time for each attempt, in millisecond.
   */
  @simpl_scalar
  private int              attemptTime;

  /**
   * Some websites returns an error page with status code 200. This regex helps detect such cases,
   * where the pattern will be searched in the downloaded page (in HTML) to determine if it is an
   * error page.
   */
  @simpl_scalar
  private String           failRegex;

  /**
   * Similar to failRegex, but specifically to detect a page that says we are banned from the site
   * :(, so that we can back from the site.
   */
  @simpl_scalar
  private String           banRegex;

  @simpl_composite
  private DownloaderResult result;

  // Runtime properties:

  /**
   * The same as uri, for convenience.
   */
  private ParsedURL        purl;

  // TODO we need locking around clients collection: when we are modifying this collection, the
  // collection should not be used.
  private List<ClientStub> clients;

  private int              attempts;

  private int              timer;

  private Object           lockState;

  /**
   * (for simpl)
   */
  public Task()
  {
    this(null, null);
  }

  public Task(String id, String uri)
  {
    super();
    this.id = id;
    this.uri = uri;
    this.state = State.INIT;

    this.clients = new ArrayList<ClientStub>();
    this.lockState = new Object();
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public State getState()
  {
    State result;
    synchronized (lockState)
    {
      result = state;
    }
    return result;
  }

  public void setState(State state)
  {
    synchronized (lockState)
    {
      this.state = state;
    }
    notifyObservers(ObservableEventType.STATE_CHANGE);
  }

  public String getUri()
  {
    return uri;
  }

  public void setUri(String uri)
  {
    this.uri = uri;
  }

  public String getUserAgent()
  {
    return userAgent;
  }

  public void setUserAgent(String userAgent)
  {
    this.userAgent = userAgent;
  }

  public int getDomainInterval()
  {
    return domainInterval;
  }

  public void setDomainInterval(int domainInterval)
  {
    this.domainInterval = domainInterval;
  }

  public int getDomainLongInterval()
  {
    return domainLongInterval;
  }

  public void setDomainLongInterval(int domainLongInterval)
  {
    this.domainLongInterval = domainLongInterval;
  }

  public int getMaxAttempts()
  {
    return maxAttempts;
  }

  public void setMaxAttempts(int attempts)
  {
    this.maxAttempts = attempts;
    resetTimer();
  }

  public int getAttemptTime()
  {
    return attemptTime;
  }

  public void setAttemptTime(int attemptTime)
  {
    this.attemptTime = attemptTime;
  }

  public String getFailRegex()
  {
    return failRegex;
  }

  public void setFailRegex(String failRegex)
  {
    this.failRegex = failRegex;
  }

  public String getBanRegex()
  {
    return banRegex;
  }

  public void setBanRegex(String banRegex)
  {
    this.banRegex = banRegex;
  }

  public ParsedURL getPurl()
  {
    if (purl == null && uri != null)
      purl = ParsedURL.getAbsolute(uri);
    return purl;
  }

  public List<ClientStub> getClients()
  {
    synchronized (clients)
    {
      return clients;
    }
  }

  public void addClient(ClientStub client)
  {
    synchronized (clients)
    {
      clients.add(client);
    }
  }

  public void addClients(List<ClientStub> moreClients)
  {
    synchronized (clients)
    {
      clients.addAll(moreClients);
    }
  }

  public synchronized int getAttempts()
  {
    return attempts;
  }

  public synchronized int getTimer()
  {
    return this.timer;
  }

  public synchronized void resetTimer()
  {
    this.timer = this.attemptTime;
  }

  public synchronized void countDown(int passedTime)
  {
    timer -= passedTime;
    if (timer <= 0)
      attempts++;
    notifyObservers(ObservableEventType.COUNT_DOWN);
  }

  public DownloaderResult getResult()
  {
    return result;
  }

  public void setResult(DownloaderResult result)
  {
    this.result = result;
  }

}

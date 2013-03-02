package ecologylab.bigsemantics.downloaderpool;

import java.util.ArrayList;
import java.util.List;

import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * 
 * @author quyin
 * 
 */
public class Task
{

  @simpl_scalar
  private String           id;

  @simpl_scalar
  private String           uri;

  @simpl_scalar
  private TaskState        state;

  /**
   * In milliseconds.
   */
  @simpl_scalar
  private int              domainInterval;
  
  @simpl_scalar
  private int              domainLongInterval;

  @simpl_scalar
  private int              maxAttempts;

  @simpl_scalar
  private int              maxCounter;

  @simpl_scalar
  private String           failRegex;

  @simpl_scalar
  private String           banRegex;

  // Runtime properties:

  private ParsedURL        purl;

  // TODO we need locking around clients collection: when we are modifying this collection, the
  // collection should not be used.
  private List<ClientStub> clients;

  private int              attempts;

  private int              counter;

  private Object           lockState;

  public Task()
  {
    this(null, null);
  }

  public Task(String id, String uri)
  {
    super();
    this.id = id;
    this.uri = uri;
    this.state = TaskState.INIT;
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

  public String getUri()
  {
    return uri;
  }

  public void setUri(String uri)
  {
    this.uri = uri;
  }

  public TaskState getState()
  {
    synchronized (lockState)
    {
      return state;
    }
  }

  public void setState(TaskState state)
  {
    synchronized (lockState)
    {
      this.state = state;
    }
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
    resetCounter();
  }

  public int getMaxCounter()
  {
    return maxCounter;
  }

  public void setMaxCounter(int countDown)
  {
    this.maxCounter = countDown;
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
    return clients;
  }

  List<ClientStub> clients()
  {
    if (clients == null)
      clients = new ArrayList<ClientStub>();
    return clients;
  }

  public void addClient(ClientStub client)
  {
    List<ClientStub> clients = clients();
    synchronized (clients)
    {
      clients.add(client);
    }
  }

  public void addClients(List<ClientStub> moreClients)
  {
    List<ClientStub> clients = clients();
    synchronized (clients)
    {
      clients.addAll(moreClients);
    }
  }

  public synchronized int getAttempts()
  {
    return attempts;
  }

  public synchronized int getCounter()
  {
    return this.counter;
  }

  public synchronized void resetCounter()
  {
    this.counter = this.maxCounter;
  }

  public synchronized void countDown()
  {
    counter--;
    if (counter <= 0)
      attempts++;
  }

}

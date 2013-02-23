package ecologylab.bigsemantics.service.downloaderpool;

/**
 * 
 * @author quyin
 * 
 */
public class ClientStub
{

  private String id;

  public ClientStub()
  {
    this(null);
  }

  public ClientStub(String id)
  {
    this.id = id;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

}

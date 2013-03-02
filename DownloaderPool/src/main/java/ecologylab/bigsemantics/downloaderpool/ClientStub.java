package ecologylab.bigsemantics.downloaderpool;

import ecologylab.serialization.annotations.simpl_scalar;

/**
 * 
 * @author quyin
 * 
 */
public class ClientStub
{

  @simpl_scalar
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

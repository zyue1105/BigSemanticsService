package ecologylab.bigsemantics.downloaderpool.services;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.bigsemantics.downloaderpool.Controller;
import ecologylab.bigsemantics.downloaderpool.Utils;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * 
 * @author quyin
 */
public class Action
{

  protected static Logger logger;

  static
  {
    logger = LoggerFactory.getLogger(Download.class);
  }

  private Controller    controller;

  public Action()
  {
    logger.info(this.getClass().getName() + " Constructed");
  }

  public Controller getController()
  {
    return controller;
  }

  public void setController(Controller controller)
  {
    this.controller = controller;
  }

  protected Response generateResponse(Object result,
                                    StringFormat serialFormat,
                                    String mediaType,
                                    String errorMsg)
  {
    String content = Utils.serialize(result, serialFormat);
    Response resp = content == null
        ? Response.status(Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity(errorMsg).build()
        : Response.ok(content, mediaType).build();
    return resp;
  }

}

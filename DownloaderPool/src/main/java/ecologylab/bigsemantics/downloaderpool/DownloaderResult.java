package ecologylab.bigsemantics.downloaderpool;

import java.util.ArrayList;
import java.util.List;

import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * The downloading result sent from a downloader to the controller.
 * 
 * @author quyin
 */
public class DownloaderResult
{

  /**
   * The state of the result.
   * 
   * @author quyin
   */
  public enum State
  {

    /**
     * The request is fulfilled.
     */
    OK,

    /**
     * The request encountered I/O errors, such as network connectivity issues.
     */
    ERR_IO,

    /**
     * The request encountered HTTP protocol errors, including but not limited to 4xx, 5xx status
     * codes.
     */
    ERR_PROTOCOL,

    /**
     * The request encountered content errors, such as unsupported MIME types or unrecognized
     * encoding.
     */
    ERR_CONTENT,

    /**
     * We have been banned by this domain.
     */
    ERR_BANNED,

    /**
     * The request encountered other errors.
     */
    ERR_OTHER,

  }

  /**
   * Used to associate the result with a task.
   */
  @simpl_scalar
  private String       taskId;

  /**
   * The state of the result.
   */
  @simpl_scalar
  private State        state;

  @simpl_scalar
  private int          httpRespCode;

  @simpl_scalar
  private String       httpRespMsg;

  @simpl_scalar
  private String       mimeType;

  /**
   * Charset as returned in the Content-Type section.
   */
  @simpl_scalar
  private String       charset;

  /**
   * The content of the page, e.g. in HTML.
   */
  @simpl_scalar
  private String       content;

  /**
   * Extra description of the content, e.g. how page content is compressed (e.g. zipped or not)
   * and/or encoded (e.g. bse64 or not).
   */
  @simpl_scalar
  private String       contentDescription;

  /**
   * Other locations, e.g. redirected locations.
   */
  @simpl_collection("location")
  private List<String> otherLocations;

  public String getTaskId()
  {
    return taskId;
  }

  public void setTaskId(String taskId)
  {
    this.taskId = taskId;
  }

  public State getState()
  {
    return state;
  }

  public void setState(State state)
  {
    this.state = state;
  }

  public int getHttpRespCode()
  {
    return httpRespCode;
  }

  public void setHttpRespCode(int httpRespCode)
  {
    this.httpRespCode = httpRespCode;
    if (httpRespCode >= 400)
      state = State.ERR_PROTOCOL;
  }

  public String getHttpRespMsg()
  {
    return httpRespMsg;
  }

  public void setHttpRespMsg(String httpRespMsg)
  {
    this.httpRespMsg = httpRespMsg;
  }

  public String getMimeType()
  {
    return mimeType;
  }

  public void setMimeType(String mimeType)
  {
    this.mimeType = mimeType;
  }

  public String getCharset()
  {
    return charset;
  }

  public void setCharset(String charset)
  {
    this.charset = charset;
  }

  public String getContent()
  {
    return content;
  }

  public void setContent(String content)
  {
    this.content = content;
  }

  public String getContentDescription()
  {
    return contentDescription;
  }

  public void setContentDescription(String contentDescription)
  {
    this.contentDescription = contentDescription;
  }

  public List<String> getOtherLocations()
  {
    return otherLocations;
  }

  public void setOtherLocations(List<String> otherLocations)
  {
    this.otherLocations = otherLocations;
  }

  public void addOtherLocation(String location)
  {
    if (otherLocations == null)
    {
      otherLocations = new ArrayList<String>();
    }
    otherLocations.add(location);
  }

}

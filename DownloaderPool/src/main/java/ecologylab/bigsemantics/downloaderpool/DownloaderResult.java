package ecologylab.bigsemantics.downloaderpool;

import java.util.List;

import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * 
 * @author quyin
 * 
 */
public class DownloaderResult
{

  @simpl_scalar
  private String       taskId;

  @simpl_scalar
  private int          httpRespCode;

  @simpl_scalar
  private String       httpRespMsg;

  @simpl_scalar
  private String       mimeType;

  @simpl_scalar
  private String       content;

  /**
   * Describes how page content is compressed (e.g. zipped or not) and/or encoded (e.g. bse64 or
   * not).
   */
  @simpl_scalar
  private String       contentDescription;

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

  public int getHttpRespCode()
  {
    return httpRespCode;
  }

  public void setHttpRespCode(int httpRespCode)
  {
    this.httpRespCode = httpRespCode;
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

}

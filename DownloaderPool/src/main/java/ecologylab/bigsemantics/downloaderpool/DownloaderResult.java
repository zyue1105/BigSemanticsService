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
  private int          httpResponseCode;

  @simpl_scalar
  private String       httpResponseMessage;

  @simpl_scalar
  private String       mimeType;

  @simpl_scalar
  private String       pageContent;

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

  public int getHttpResponseCode()
  {
    return httpResponseCode;
  }

  public void setHttpResponseCode(int httpResponseCode)
  {
    this.httpResponseCode = httpResponseCode;
  }

  public String getHttpResponseMessage()
  {
    return httpResponseMessage;
  }

  public void setHttpResponseMessage(String httpResponseMessage)
  {
    this.httpResponseMessage = httpResponseMessage;
  }

  public String getMimeType()
  {
    return mimeType;
  }

  public void setMimeType(String mimeType)
  {
    this.mimeType = mimeType;
  }

  public String getPageContent()
  {
    return pageContent;
  }

  public void setPageContent(String pageContent)
  {
    this.pageContent = pageContent;
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

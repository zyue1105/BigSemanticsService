package ecologylab.bigsemantics.downloaderpool;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

/**
 * A handler that modifies or returns a BasicResponse.
 * 
 * @author quyin
 */
public class BasicResponseHandler implements ResponseHandler<BasicResponse>
{

  protected BasicResponse result;
  
  public BasicResponseHandler()
  {
    this(null);
  }

  public BasicResponseHandler(BasicResponse result)
  {
    super();
    this.result = result;
  }

  @Override
  public BasicResponse handleResponse(HttpResponse resp) throws IOException, ParseException
  {
    if (result == null)
      result = new BasicResponse();
    
    // status code
    result.setHttpRespCode(resp.getStatusLine().getStatusCode());

    // status msg
    result.setHttpRespMsg(resp.getStatusLine().getReasonPhrase());

    // mime type and charset
    HttpEntity entity = resp.getEntity();
    Header contentTypeHeader = entity.getContentType(); // mime type + charset
    String contentTypeV = contentTypeHeader == null ? null : contentTypeHeader.getValue();
    if (contentTypeV != null && contentTypeV.length() > 0)
    {
      String[] contentTypeInfo = contentTypeV.split(";");
      String mimeType = contentTypeInfo[0].trim();
      String charset = contentTypeInfo.length > 1 ? contentTypeInfo[1].trim() : null;
      if (charset != null && charset.startsWith("charset="))
      {
        charset = charset.substring(8);
      }
      result.setMimeType(mimeType);
      result.setCharset(charset);
    }

    // content
    result.setContent(EntityUtils.toString(entity));

    return result;
  }

}

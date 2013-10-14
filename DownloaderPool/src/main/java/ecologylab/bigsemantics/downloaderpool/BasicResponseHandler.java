package ecologylab.bigsemantics.downloaderpool;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A handler that modifies or returns a BasicResponse.
 * 
 * @author quyin
 */
public class BasicResponseHandler implements ResponseHandler<BasicResponse>
{

  private static Logger   logger = LoggerFactory.getLogger(BasicResponseHandler.class);

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

    HttpEntity entity = resp.getEntity();

    if (entity != null)
    {
      // mime type and charset
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
    }
    else
    {
      logger.warn("No entity from response, requested URL: %s", result.getRequestedUrl());
    }

    return result;
  }

}

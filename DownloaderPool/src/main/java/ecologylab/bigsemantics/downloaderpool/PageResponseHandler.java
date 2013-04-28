package ecologylab.bigsemantics.downloaderpool;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import ecologylab.bigsemantics.downloaderpool.DownloaderResult.State;

/**
 * Extracts useful info from HttpResponse and fills them into the DwonloaderResult object for this
 * download.
 * 
 * @author quyin
 */
public class PageResponseHandler implements ResponseHandler<DownloaderResult>
{

  /**
   * The DownloaderResult object for this download.
   */
  private DownloaderResult result;

  public PageResponseHandler(DownloaderResult result)
  {
    this.result = result;
  }

  @Override
  public DownloaderResult handleResponse(HttpResponse resp)
  {
    // status code
    result.setHttpRespCode(resp.getStatusLine().getStatusCode());

    // status msg
    result.setHttpRespMsg(resp.getStatusLine().getReasonPhrase());

    // mime type and charset
    HttpEntity entity = resp.getEntity();
    Header contentTypeHeader = entity.getContentType(); // mime type + charset
    String[] contentTypeInfo = contentTypeHeader.getValue().split(";");
    String mimeType = contentTypeInfo[0].trim();
    String charset = contentTypeInfo.length > 1 ? contentTypeInfo[1].trim() : null;
    if (charset != null && charset.startsWith("charset="))
    {
      charset = charset.substring(8);
    }
    result.setMimeType(mimeType);
    result.setCharset(charset);

    // content
    try
    {
      result.setContent(EntityUtils.toString(entity));
    }
    catch (ParseException e)
    {
      // TODO logging
      result.setState(State.ERR_CONTENT);
    }
    catch (IOException e)
    {
      // TODO logging
      result.setState(State.ERR_IO);
    }

    // TODO anything else?

    return result;
  }

}

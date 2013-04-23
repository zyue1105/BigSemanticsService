package ecologylab.bigsemantics.downloaderpool;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

public class PageResponseHandler implements ResponseHandler<DownloaderResult>
{

  @Override
  public DownloaderResult handleResponse(HttpResponse resp) throws ClientProtocolException,
      IOException
  {
    DownloaderResult result = new DownloaderResult();
    
    result.setHttpRespCode(resp.getStatusLine().getStatusCode());
    result.setHttpRespMsg(resp.getStatusLine().getReasonPhrase());
    HttpEntity entity = resp.getEntity();
    Header contentTypeHeader = entity.getContentType(); // mime type + charset
    String mimeType = contentTypeHeader.getValue().split(";")[0].trim();
    result.setMimeType(mimeType);
    result.setContent(EntityUtils.toString(entity));
    
    // TODO anything else?
    
    return result;
  }

}

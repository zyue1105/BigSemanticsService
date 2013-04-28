package ecologylab.bigsemantics.downloaderpool;

import java.net.URI;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;

/**
 * This strategy helps find redirected locations for a page. Used with HttpClient.
 * 
 * @author quyin
 */
public class PageRedirectStrategy extends DefaultRedirectStrategy
{

  /**
   * The corresponding DownloaderResult object for this download. Redirected locations will be
   * recorded into this object.
   */
  private DownloaderResult result;

  public PageRedirectStrategy(DownloaderResult result)
  {
    this.result = result;
  }

  @Override
  public URI getLocationURI(HttpRequest request, HttpResponse response, HttpContext context)
      throws ProtocolException
  {
    URI redirectedUri = super.getLocationURI(request, response, context);
    result.addOtherLocation(redirectedUri.toString());
    return redirectedUri;
  }

}

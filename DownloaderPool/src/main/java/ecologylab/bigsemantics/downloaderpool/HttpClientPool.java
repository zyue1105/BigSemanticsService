package ecologylab.bigsemantics.downloaderpool;

import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import ecologylab.generic.ResourcePool;

/**
 * A pool for HttpClient objects.
 * 
 * @author quyin
 */
public class HttpClientPool extends ResourcePool<AbstractHttpClient>
{

  protected HttpClientPool()
  {
    super(false, 10, 10, false);
  }

  @Override
  protected AbstractHttpClient generateNewResource()
  {
    DefaultHttpClient client = new DefaultHttpClient();
    prepareHttpClient(client);
    return client;
  }

  private void resetHttpClient(AbstractHttpClient client)
  {
    client.setParams(null);
    client.clearRequestInterceptors();
    client.clearResponseInterceptors();
  }

  private void prepareHttpClient(AbstractHttpClient client)
  {
    client.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, true);
    client.addRequestInterceptor(new RequestAcceptEncoding());
    client.addResponseInterceptor(new ResponseContentEncoding());
  }

  @Override
  protected void clean(AbstractHttpClient client)
  {
    resetHttpClient(client);
    prepareHttpClient(client);
  }

}

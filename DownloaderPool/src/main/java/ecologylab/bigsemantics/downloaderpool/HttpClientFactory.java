package ecologylab.bigsemantics.downloaderpool;

import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;

/**
 * A factory for HttpClient objects.
 * 
 * @author quyin
 */
public class HttpClientFactory
{

  private PoolingClientConnectionManager connectionManager;

  private AbstractHttpClient             client;

  public HttpClientFactory()
  {
    connectionManager = new PoolingClientConnectionManager();
    connectionManager.setDefaultMaxPerRoute(20);
    connectionManager.setMaxTotal(200);

    client = new DefaultHttpClient(connectionManager);
    prepareHttpClient(client);
  }

  public AbstractHttpClient get()
  {
    return client;
  }

  private void prepareHttpClient(AbstractHttpClient client)
  {
    client.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, true);
    client.addRequestInterceptor(new RequestAcceptEncoding());
    client.addResponseInterceptor(new ResponseContentEncoding());
  }

}

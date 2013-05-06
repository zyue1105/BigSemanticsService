package ecologylab.bigsemantics.downloaderpool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author quyin
 */
public class IntegratedTestWithControllerAndDownloaders
{

  static Logger    logger;

  static
  {
    logger = LoggerFactory.getLogger(IntegratedTestWithControllerAndDownloaders.class);
  }

  static String    HOST       = "localhost";

  static int       PORT       = 2280;

  static String    CONTEXT    = "/DownloaderPool";

  static String    BASE_URL   = "http://" + HOST + ":" + PORT + CONTEXT + "/";

  static Server    server;

  final static int DELAY_TIME = 5000;

  List<Downloader> downloaders;

  HttpClientPool   clientPool = new HttpClientPool();

  @BeforeClass
  public static void setUpServer() throws Exception
  {
    DownloaderResponder.CONTROLLER_REPORT_URL = BASE_URL + "report";
    server = new Server(PORT);
    server.setHandler(new WebAppContext("src/main/webapp", CONTEXT));
    server.start();
    while (!server.isRunning())
    {
      Utils.sleep(500);
    }
    logger.info("Server is now running.");
    Utils.sleep(2000);
  }

  @AfterClass
  public static void tearDownServer() throws Exception
  {
    server.stop();
    logger.info("Server is stopped.");
  }
  
  @Test
  public void testPost() throws ClientProtocolException, IOException
  {
    String tid = "mytask";
    
    Map<String, String> params = new HashMap<String, String>();
    params.put("tid", tid);
    HttpPost post = Utils.generatePostRequest(BASE_URL + "report", params);
    
    HttpClient client = new DefaultHttpClient();
    BasicResponse result = new BasicResponse();
    client.execute(post, new BasicResponseHandler(result));
    
    assertEquals(HttpStatus.SC_OK, result.getHttpRespCode());
    assertNotNull(result.getContent());
    assertTrue(result.getContent().contains(tid));
  }

  public void runDownloaders(int n)
  {
    downloaders = new ArrayList<Downloader>(n);
    for (int i = 0; i < n; ++i)
    {
      Downloader d = new Downloader("D" + (i + 1));
      d.controllerBaseUrl = BASE_URL + "assign.xml";
      d.numDownloadThreads = 1;
      d.maxTaskCount = 1;
      d.start();
      downloaders.add(d);
    }
  }

  public void stopDownloaders()
  {
    if (downloaders != null)
    {
      for (Downloader d : downloaders)
      {
        d.stop();
      }
      downloaders = null;
    }
  }

  //@Test(timeout = DELAY_TIME + DELAY_TIME * 3 / 4)
  @Test
  public void testIntegration() throws InterruptedException
  {
    int n = 1;
    runDownloaders(n);

    List<Callable<Integer>> downloads = new ArrayList<Callable<Integer>>();
    for (int i = 0; i < n; ++i)
    {
      final int j = i;
      downloads.add(new Callable<Integer>()
      {
        @Override
        public Integer call() throws ClientProtocolException, IOException, URISyntaxException
        {
          HttpClient client = clientPool.acquire();

          Map<String, String> params = new HashMap<String, String>();
          params.put("url", BASE_URL + "echo/get?msg=MSG" + j + "&delay=" + DELAY_TIME);
          params.put("int", "1000");
          params.put("natt", "3");
          params.put("tatt", "10000");
          HttpGet get = Utils.generateGetRequest(BASE_URL + "download.xml", params);
          logger.info("HttpGet URI: " + get.getURI());
          int code = client.execute(get, new ResponseHandler<Integer>()
          {
            @Override
            public Integer handleResponse(HttpResponse response)
            {
              return response.getStatusLine().getStatusCode();
            }
          });
          return code;
        }
      });
    }

    ExecutorService es = Executors.newFixedThreadPool(n);
    List<Future<Integer>> codes = es.invokeAll(downloads);
    for (Future<Integer> code : codes)
    {
      int c = -1;
      try
      {
        c = code.get();
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
      catch (ExecutionException e)
      {
        e.printStackTrace();
      }
      assertEquals(200, c);
    }

    stopDownloaders();
  }

  public static void main(String[] args) throws Exception
  {
    setUpServer();
  }

}

package ecologylab.bigsemantics.downloaderpool;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * Util methods.
 * 
 * @author quyin
 */
public class Utils
{

  static Logger logger = LoggerFactory.getLogger(Utils.class);

  /**
   * Sleep for the given time in milliseconds. The sleep can be interrupted but it won't throw
   * exceptions.
   * 
   * @param millisec
   */
  public static void sleep(long millisec)
  {
    try
    {
      Thread.sleep(millisec);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  private static HashFunction hf  = Hashing.sha256();

  private static BaseEncoding enc = BaseEncoding.base64Url();

  public static byte[] hashToBytes(String s)
  {
    return hf.hashString(s, Charsets.UTF_8).asBytes();
  }

  public static String base64urlEncode(byte[] bytes)
  {
    return enc.encode(bytes);
  }
  
  public static <K, V> boolean putNonEmpty(Map<K, V> map, K key, V value)
  {
    if (map == null || key == null || value == null)
      return false;
    if (value instanceof String && ((String) value).isEmpty())
      return false;
    map.put(key, value);
    return true;
  }

  public static String serialize(Object obj, StringFormat fmt)
  {
    if (obj == null)
      return null;

    try
    {
      return SimplTypesScope.serialize(obj, fmt).toString();
    }
    catch (SIMPLTranslationException e)
    {
      logger.error("Exception during serializing " + obj, e);
    }

    return null;
  }

  public static Object deserialize(CharSequence content, SimplTypesScope scope, StringFormat fmt)
  {
    if (content == null)
      return null;

    try
    {
      return scope.deserialize(content, fmt);
    }
    catch (SIMPLTranslationException e)
    {
      logger.error("Exception during deserializing:\n" + content, e);
    }

    return null;
  }

  public static HttpGet generateGetRequest(String url,
                                           Map<String, String> additionalParams)
  {
    try
    {
      URIBuilder ub = new URIBuilder(url);
      if (additionalParams != null)
      {
        for (String key : additionalParams.keySet())
        {
          String value = additionalParams.get(key);
          ub.addParameter(key, value);
        }
      }
      URI uri = ub.build();

      String hostName = uri.getHost();
      int port = uri.getPort();
      String host = hostName + ((port > 0) ? (":" + port) : "");

      HttpGet get = new HttpGet(uri);
      get.addHeader("HOST", host); // this header is required by HTTP 1.1

      return get;
    }
    catch (Exception e)
    {
      logger.error("Exception when generating a HttpGet object for " + url, e);
    }
    return null;
  }

  public static HttpPost generatePostRequest(String url, Map<String, String> formParams)
  {
    HttpPost post = new HttpPost(url);
    URI uri = post.getURI();
    String hostName = uri.getHost();
    int port = uri.getPort();
    String host = hostName + ((port > 0) ? (":" + port) : "");
    post.addHeader("HOST", host); // this header is required by HTTP 1.1

    List<NameValuePair> params = new ArrayList<NameValuePair>();
    for (String key : formParams.keySet())
    {
      String value = formParams.get(key);
      if (value != null)
      {
        params.add(new BasicNameValuePair(key, value));
      }
    }
    HttpEntity entity = new UrlEncodedFormEntity(params, Charsets.UTF_8);
    post.setEntity(entity);

    return post;
  }

}

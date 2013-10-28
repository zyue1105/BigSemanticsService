/**
 * 
 */
package ecologylab.bigsemantics.service.mmd;

import java.net.URI;
import java.util.HashMap;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.service.SemanticServiceErrorMessages;
import ecologylab.bigsemantics.service.SemanticServiceScope;
import ecologylab.logging.ILogger;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * Helper class for mmd.xml and mmd.json root resources
 * 
 * @author ajit
 * 
 */

public class MMDServiceHelper
{
  static SemanticServiceScope          semanticsServiceScope;

  // url to name mapping
  static HashMap<ParsedURL, String>    purlNameMap = new HashMap<ParsedURL, String>();

  // name to mmd maaping
  static HashMap<String, MetaMetadata> mmdCache    = new HashMap<String, MetaMetadata>();

  // static Logger log4j = Logger.getLogger(ServiceLogger.mmdLogger);
  static ILogger                       logger;

  static
  {
    semanticsServiceScope = SemanticServiceScope.get();
    logger = SemanticServiceScope.get().getLoggerFactory().getLogger(MMDServiceHelper.class);
  }

  public static Response redirectToMmdByName(ParsedURL url, UriInfo uriInfo)
  {
    ParsedURL thatPurl = url; // ParsedURL.getAbsolute(url);
    String mmdName;
    MetaMetadata docMM;

    // TODO: implement a selector based purlNameMap lookup?

    // check in cache
    if (purlNameMap.containsKey(thatPurl))
    {
      mmdName = purlNameMap.get(thatPurl);
    }
    else
    {
      docMM = semanticsServiceScope.getMetaMetadataRepository().getDocumentMM(thatPurl);

      if (docMM != null)
      {
        // cache the mmd
        mmdName = docMM.getName();
        purlNameMap.put(thatPurl, mmdName);

        // probably, condition not required if selector based URL lookup implemented
        if (!mmdCache.containsKey(mmdName))
          mmdCache.put(mmdName, docMM);
      }
      else
      {
        mmdName = null;
      }
    }

    // redirect to name based url
    if (mmdName != null)
    {
      URI nameURI = uriInfo.getAbsolutePathBuilder().queryParam("name", mmdName).build();
      return Response.status(Status.SEE_OTHER).location(nameURI).build();
    }
    else
      return Response.status(Status.NOT_FOUND)
          .entity(SemanticServiceErrorMessages.METAMETADATA_NOT_FOUND).type(MediaType.TEXT_PLAIN)
          .build();
  }

  public static Response getMmdByName(String mmdName, StringFormat format)
  {
    MetaMetadata docMM;

    // check in cache
    if (mmdCache.containsKey(mmdName))
    {
      docMM = mmdCache.get(mmdName);
    }
    else
    {
      docMM = semanticsServiceScope.getMetaMetadataRepository().getMMByName(mmdName);

      if (docMM != null)
      {
        // cache the mmd
        mmdCache.put(mmdName, docMM);
      }
    }

    // return json response
    if (docMM != null)
    {
      try
      {
        String responseBody = SimplTypesScope.serialize(docMM, format).toString();
        return Response.status(Status.OK).entity(responseBody).build();
      }
      catch (SIMPLTranslationException e)
      {
        e.printStackTrace();
        return Response.status(Status.INTERNAL_SERVER_ERROR)
            .entity(SemanticServiceErrorMessages.INTERNAL_ERROR).type(MediaType.TEXT_PLAIN).build();
      }
    }
    else
      return Response.status(Status.NOT_FOUND)
          .entity(SemanticServiceErrorMessages.METAMETADATA_NOT_FOUND).type(MediaType.TEXT_PLAIN)
          .build();
  }

}

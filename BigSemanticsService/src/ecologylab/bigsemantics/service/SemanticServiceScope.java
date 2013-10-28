/**
 * 
 */
package ecologylab.bigsemantics.service;

import java.io.InputStream;
import java.util.Properties;

import ecologylab.appframework.types.prefs.Pref;
import ecologylab.bigsemantics.actions.SemanticActionsKeyWords;
import ecologylab.bigsemantics.collecting.DocumentCache;
import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.collecting.SemanticsSite;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.documentcache.EhCacheDocumentCache;
import ecologylab.bigsemantics.documentcache.PersistentDocumentCache;
import ecologylab.bigsemantics.documentcache.PersistentDocumentCacheFactory;
import ecologylab.bigsemantics.documentparsers.DefaultHTMLDOMParser;
import ecologylab.bigsemantics.documentparsers.DocumentParser;
import ecologylab.bigsemantics.documentparsers.ParserBase;
import ecologylab.bigsemantics.downloaders.controllers.DownloadController;
import ecologylab.bigsemantics.filestorage.FileSystemStorage;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.bigsemantics.html.dom.IDOMProvider;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.bigsemantics.service.dbinterface.SimpleDiskDocumentCacheFactory;
import ecologylab.bigsemantics.service.downloader.controller.DPoolDownloadController;
import ecologylab.bigsemantics.service.logging.Log4jLoggerFactory;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.SimplTypesScope.GRAPH_SWITCH;

/**
 * Exclusive semantic scope for the semantic service Keeps a reference to DBDocumentProviderFactory
 * to facilitate DB lookup
 * 
 * @author ajit
 * 
 */
public class SemanticServiceScope extends SemanticsGlobalScope
{
  
  private SemanticServiceScope(SimplTypesScope metadataTScope,
                               Class<? extends IDOMProvider> domProviderClass)
  {
    super(metadataTScope, domProviderClass);
    this.put(KEY_LOGGER_FACTORY, new Log4jLoggerFactory());
  }

  @Override
  protected DocumentCache<ParsedURL, Document> getDocumentCache()
  {
    return new EhCacheDocumentCache();
  }

  @Override
  public PersistentDocumentCache getPersistentDocumentCache()
  {
    PersistentDocumentCache result = null;

    if (documentCacheFactory != null)
      result = documentCacheFactory.getDBDocumentProvider();

    return result;
  }
  
  @Override
  public DownloadController createDownloadController(DocumentClosure closure)
  {
    DPoolDownloadController result = new DPoolDownloadController();
    result.setDocumentClosure(closure);
    return result;
  }
  
  @Override
  public boolean isService()
  {
    return true;
  }

  private static SemanticServiceScope semanticServiceScope = null;

  public static SemanticServiceScope get()
  {
    if (semanticServiceScope == null)
    {
      Pref.usePrefBoolean("donot_lookup_downloaded_documents", true);
      Pref.usePrefBoolean("donot_setup_document_graph_callbacks", true);
      Pref.usePrefBoolean("donot_look_for_favicon", true);

      ParserBase.DONOT_LOOKUP_DOWNLOADED_DOCUMENT = true;
      ParserBase.DONOT_SETUP_DOCUMENT_GRAPH_CALLBACKS = true;
      ParserBase.DONOT_LOOK_FOR_FAVICON = true;

      loadProperties();
      
      SemanticsSite.disableDownloadInterval = true;

      SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;
      semanticServiceScope = new SemanticServiceScope(RepositoryMetadataTranslationScope.get(),
                                                      CybernekoWrapper.class);
      
      // This will disable content body recognization and image-text clipping derivation on the
      // service.
      DocumentParser.map(SemanticActionsKeyWords.HTML_IMAGE_DOM_TEXT_PARSER,
                         DefaultHTMLDOMParser.class);
    }
    return semanticServiceScope;
  }

  private static String propertiesFile = "/service.properties";

  private static void loadProperties()
  {
    try
    {
      Properties serviceProps = new Properties();
      InputStream in = SemanticServiceScope.class.getResourceAsStream(propertiesFile);
      serviceProps.load(in);
      in.close();

      documentCacheFactory = new SimpleDiskDocumentCacheFactory();

      FileSystemStorage.setDownloadDirectory(serviceProps);
      
      String serviceLocs = getProperty(serviceProps, "DPOOL_SERVICE_LOCATIONS", null);
      DPoolDownloadController.setServiceLocs(serviceLocs);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private static String getProperty(Properties props, String name, String defaultValue)
  {
    String value = props.getProperty(name);
    return value == null ? defaultValue : value;
  }

  private static PersistentDocumentCacheFactory documentCacheFactory;

}

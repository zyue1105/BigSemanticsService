/**
 * 
 */
package ecologylab.semantic.service;

import java.io.InputStream;
import java.util.Properties;

import ecologylab.appframework.types.prefs.Pref;
import ecologylab.semantic.service.dbinterface.DBDocumentCacheFactory;
import ecologylab.semantic.service.dbinterface.SimpleDiskDocumentCacheFactory;
import ecologylab.semantic.service.logging.Log4jLoggerFactory;
import ecologylab.semantics.collecting.SemanticsGlobalScope;
import ecologylab.semantics.cyberneko.CybernekoWrapper;
import ecologylab.semantics.dbinterface.IDocumentCache;
import ecologylab.semantics.dbinterface.IDocumentCacheFactory;
import ecologylab.semantics.documentparsers.ParserBase;
import ecologylab.semantics.filestorage.FileSystemStorage;
import ecologylab.semantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.semantics.html.dom.IDOMProvider;
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
  public IDocumentCache getDBDocumentProvider()
  {
    IDocumentCache result = null;

    if (documentCacheFactory != null)
      result = documentCacheFactory.getDBDocumentProvider();

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

      SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;
      semanticServiceScope = new SemanticServiceScope(RepositoryMetadataTranslationScope.get(),
                                                      CybernekoWrapper.class);
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

      if (getProperty(serviceProps, "USE_SIMPLE_DISK_DOCUMENT_PROVIDER", "").equals("true"))
        documentCacheFactory = new SimpleDiskDocumentCacheFactory();
      else if (getProperty(serviceProps, "USE_DB_DOCUMENT_PROVIDER", "").equals("true"))
        documentCacheFactory = new DBDocumentCacheFactory();

      FileSystemStorage.setDownloadDirectory(serviceProps);
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

  private static IDocumentCacheFactory documentCacheFactory;

}

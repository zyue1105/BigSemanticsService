package ecologylab.bigsemantics.service.dbinterface;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.documentcache.PersistentDocumentCache;
import ecologylab.bigsemantics.filestorage.FileMetadata;
import ecologylab.bigsemantics.filestorage.FileStorageProvider;
import ecologylab.bigsemantics.filestorage.FileSystemStorage;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.bigsemantics.metadata.MetadataDeserializationHookStrategy;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.bigsemantics.namesandnums.SemanticsAssetVersions;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.DeserializationHookStrategy;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.TranslationContext;
import ecologylab.serialization.TranslationContextPool;
import ecologylab.serialization.formatenums.Format;

/**
 * 
 * @author quyin
 */
public class SimpleDiskDocumentCache implements PersistentDocumentCache
{

  static Logger              logger;

  static FileStorageProvider storage;

  static File                semanticsDir;

  static
  {
    logger = LoggerFactory.getLogger(SimpleDiskDocumentCache.class);

    storage = FileSystemStorage.getStorageProvider();

    semanticsDir = new File(FileSystemStorage.semanticsFileDirectory);
    if (!semanticsDir.exists())
      PropertiesAndDirectories.createDirsAsNeeded(semanticsDir);
    if (!semanticsDir.exists())
      throw new RuntimeException("The directory to cache extracted semantics does not exist: "
                                 + semanticsDir);
  }

  @Override
  public Document retrieveDocument(DocumentClosure closure)
  {
    if (closure == null || closure.getDocument() == null)
      return null;

    ParsedURL purl = closure.getDocument().getLocation();
    if (purl == null)
      return null;

    FileMetadata fileMetadata = storage.getFileMetadata(purl);
    if (fileMetadata == null)
    {
      // html / semantics not cached
      logger.debug("Neither HTML or metadata cached for " + purl);
      return null;
    }
    float version = fileMetadata.getRepositoryVersion();
    if (version == 0.0)
    {
      // html cached, but semantics not cached
      logger.debug("HTML cached for " + purl + ", but no metadata cached.");
      return null;
    }
    if (version < SemanticsAssetVersions.METAMETADATA_ASSET_VERSION)
    {
      // html cached, semantics cached but stale
      logger.debug("Cached metadata is stale for " + purl);
      return null;
    }

    // html cached, semantics cached, and of the right version
    return loadDocumentFromDisk(purl, closure.getSemanticsScope());
  }

  @Override
  public void storeDocument(Document document)
  {
    if (document == null || document.getLocation() == null)
      return;

    ParsedURL purl = document.getLocation();
    if (purl == null)
      return;

    FileMetadata fileMetadata = storage.getFileMetadata(purl);
    if (fileMetadata == null)
    {
      // html & semantics not cached
      saveDocumentToDisk(document);
      return;
    }
    float version = fileMetadata.getRepositoryVersion();
    if (version == 0.0)
    {
      // html cached, but semantics not cached
      saveDocumentToDisk(document);
      fileMetadata.setRepositoryVersion(SemanticsAssetVersions.METAMETADATA_ASSET_VERSION);
      logger.debug("updating file metadata for " + purl);
      storage.saveFileMetadata(fileMetadata);
      return;
    }
    if (version < SemanticsAssetVersions.METAMETADATA_ASSET_VERSION)
    {
      // html cached, semantics cached but stale
      saveDocumentToDisk(document);
      fileMetadata.setRepositoryVersion(SemanticsAssetVersions.METAMETADATA_ASSET_VERSION);
      logger.debug("updating file metadata for " + purl);
      storage.saveFileMetadata(fileMetadata);
      return;
    }

    // html cached, semantics cached, and of the right version
    logger.debug("We already have the right HTML and metadata cached for " + purl);
    return;
  }

  @Override
  public void removeDocument(ParsedURL url)
  {
    File file = FileSystemStorage.getDestinationFileAndCreateDirs(semanticsDir.getPath(),
                                                                  url,
                                                                  "xml");
    if (file.exists() && !file.delete())
    {
      logger.warn("cached semantics [{}] for url [{}] could not be deleted.",
                  file.getAbsolutePath(),
                  url.toString());
    }
  }

  Document loadDocumentFromDisk(ParsedURL purl, SemanticsGlobalScope semanticsScope)
  {
    logger.debug("loading cached semantics for " + purl + " from disk ...");

    File file = FileSystemStorage.getDestinationFileAndCreateDirs(semanticsDir.getPath(),
                                                                  purl,
                                                                  "xml");
    if (file.exists())
    {
      TranslationContext translationContext = TranslationContextPool.get().acquire();
      DeserializationHookStrategy deserializationHookStrategy = new MetadataDeserializationHookStrategy(semanticsScope);
      Object deserializedObj = null;
      try
      {
        SimplTypesScope simplTypesScope = RepositoryMetadataTranslationScope.get();
        deserializedObj = simplTypesScope.deserialize(file,
                                                      translationContext,
                                                      deserializationHookStrategy,
                                                      Format.XML);
        if (deserializedObj != null && deserializedObj instanceof Document)
        {
          logger.debug("cached semantics loaded for " + purl);
          return (Document) deserializedObj;
        }
      }
      catch (SIMPLTranslationException e)
      {
        logger.error("Error occurred during deserializing cached metadata: " + file, e);
      }
      finally
      {
        TranslationContextPool.get().release(translationContext);
      }
    }
    return null;
  }

  void saveDocumentToDisk(Document document)
  {
    ParsedURL purl = document.getLocation();
    logger.debug("saving extracted semantics for " + purl + " to disk cache ...");

    File file = FileSystemStorage.getDestinationFileAndCreateDirs(semanticsDir.getPath(),
                                                                  purl,
                                                                  "xml");
    TranslationContext translationContext = TranslationContextPool.get().acquire();
    try
    {
      SimplTypesScope.serialize(document, file, Format.XML, translationContext);
      logger.debug("extracted semantics for " + purl + " saved to disk cache: " + file);
    }
    catch (SIMPLTranslationException e)
    {
        logger.error("Error occurred during serializing docuemnt to be cached: " + document, e);
    }
    finally
    {
      TranslationContextPool.get().release(translationContext);
    }
  }

}

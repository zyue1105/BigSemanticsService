package ecologylab.bigsemantics.service.dbinterface;

import java.io.File;

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
import ecologylab.generic.Debug;
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
public class SimpleDiskDocumentCache extends Debug implements PersistentDocumentCache
{

  static FileStorageProvider storage;

  static File                semanticsDir;

  static
  {
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
      return null;
    }
    float version = fileMetadata.getRepositoryVersion();
    if (version == 0.0)
    {
      // html cached, but semantics not cached
      return null;
    }
    if (version < SemanticsAssetVersions.METAMETADATA_ASSET_VERSION)
    {
      // html cached, semantics cached but stale
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
      debug("updating file metadata for " + purl);
      storage.saveFileMetadata(fileMetadata);
      return;
    }
    if (version < SemanticsAssetVersions.METAMETADATA_ASSET_VERSION)
    {
      // html cached, semantics cached but stale
      saveDocumentToDisk(document);
      fileMetadata.setRepositoryVersion(SemanticsAssetVersions.METAMETADATA_ASSET_VERSION);
      debug("updating file metadata for " + purl);
      storage.saveFileMetadata(fileMetadata);
      return;
    }

    // html cached, semantics cached, and of the right version
    return;
  }
  
  @Override
	public void removeDocument(ParsedURL url)
	{
  	File file = FileSystemStorage.getDestinationFileAndCreateDirs(semanticsDir.getPath(), url, "xml");
  	if (file.exists() && !file.delete())
  		warning("cached semantics " + file.getAbsolutePath() + " for url " + url.toString() + " could not be deleted");
	}

  Document loadDocumentFromDisk(ParsedURL purl, SemanticsGlobalScope semanticsScope)
  {
    debug("loading cached semantics for " + purl + " from disk ...");
    
    File file = FileSystemStorage.getDestinationFileAndCreateDirs(semanticsDir.getPath(), purl, "xml");
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
          debug("cached semantics loaded for " + purl);
          return (Document) deserializedObj;
        }
      }
      catch (SIMPLTranslationException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
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
    debug("saving extracted semantics for " + purl + " to disk cache ...");
    
    File file = FileSystemStorage.getDestinationFileAndCreateDirs(semanticsDir.getPath(), purl, "xml");
    TranslationContext translationContext = TranslationContextPool.get().acquire();
    try
    {
      SimplTypesScope.serialize(document, file, Format.XML, translationContext);
      debug("extracted semantics for " + purl + " saved to disk cache");
    }
    catch (SIMPLTranslationException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    finally
    {
      TranslationContextPool.get().release(translationContext);
    }
  }

}

package ecologylab.bigsemantics.service.dbinterface;

import ecologylab.bigsemantics.documentcache.PersistentDocumentCache;
import ecologylab.bigsemantics.documentcache.PersistentDocumentCacheFactory;
import ecologylab.generic.Debug;

/**
 * 
 * @author quyin
 */
public class SimpleDiskDocumentCacheFactory extends Debug implements PersistentDocumentCacheFactory
{

  @Override
  public PersistentDocumentCache getDBDocumentProvider()
  {
    return new SimpleDiskDocumentCache();
  }

}

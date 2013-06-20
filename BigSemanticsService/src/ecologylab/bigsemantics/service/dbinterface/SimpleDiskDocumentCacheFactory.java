package ecologylab.bigsemantics.service.dbinterface;

import ecologylab.bigsemantics.documentcache.IDocumentCache;
import ecologylab.bigsemantics.documentcache.IDocumentCacheFactory;
import ecologylab.generic.Debug;

/**
 * 
 * @author quyin
 */
public class SimpleDiskDocumentCacheFactory extends Debug implements IDocumentCacheFactory
{

  @Override
  public IDocumentCache getDBDocumentProvider()
  {
    return new SimpleDiskDocumentCache();
  }

}

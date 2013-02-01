package ecologylab.bigsemantic.service.dbinterface;

import ecologylab.bigsemantics.dbinterface.IDocumentCache;
import ecologylab.bigsemantics.dbinterface.IDocumentCacheFactory;
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

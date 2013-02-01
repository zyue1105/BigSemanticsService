package ecologylab.semantic.service.dbinterface;

import ecologylab.generic.Debug;
import ecologylab.semantics.dbinterface.IDocumentCache;
import ecologylab.semantics.dbinterface.IDocumentCacheFactory;

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

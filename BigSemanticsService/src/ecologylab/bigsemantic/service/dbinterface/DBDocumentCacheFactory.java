package ecologylab.bigsemantic.service.dbinterface;

import ecologylab.bigsemantics.dbinterface.IDocumentCache;
import ecologylab.bigsemantics.dbinterface.IDocumentCacheFactory;

public class DBDocumentCacheFactory implements IDocumentCacheFactory 
{
	public IDocumentCache getDBDocumentProvider()
	{
		return new DBDocumentCache();
	}
}

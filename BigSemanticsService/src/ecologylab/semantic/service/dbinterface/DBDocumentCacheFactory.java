package ecologylab.semantic.service.dbinterface;

import ecologylab.semantics.dbinterface.IDocumentCache;
import ecologylab.semantics.dbinterface.IDocumentCacheFactory;

public class DBDocumentCacheFactory implements IDocumentCacheFactory 
{
	public IDocumentCache getDBDocumentProvider()
	{
		return new DBDocumentCache();
	}
}

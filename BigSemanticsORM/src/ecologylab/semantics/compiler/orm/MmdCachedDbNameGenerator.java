package ecologylab.semantics.compiler.orm;

import ecologylab.translators.hibernate.DefaultCachedDbNameGenerator;

public class MmdCachedDbNameGenerator extends DefaultCachedDbNameGenerator
{

	@Override
	protected String createTableName(String classSimpleName)
	{
		if (classSimpleName.endsWith("Declaration"))
			return super.createTableName(classSimpleName.substring(0, classSimpleName.lastIndexOf("Declaration")));
		else
			return super.createTableName(classSimpleName);
	}
	
}

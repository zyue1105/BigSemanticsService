package ecologylab.semantics.compiler.orm.scalartypes;

import org.hibernate.PropertyNotFoundException;
import org.hibernate.property.BasicPropertyAccessor;
import org.hibernate.property.Getter;
import org.hibernate.property.Setter;

import ecologylab.semantics.compiler.MetaMetadataJavaTranslator;

public class MetadataScalarAccessor extends BasicPropertyAccessor
{

	@Override
	public Getter getGetter(Class theClass, String propertyName) throws PropertyNotFoundException
	{
		if (propertyName.length() == 1)
			propertyName = String.valueOf(Character.toUpperCase(propertyName.charAt(0)));
		return super.getGetter(theClass, propertyName + MetaMetadataJavaTranslator.SCALAR_GETTER_SETTER_SUFFIX);
	}

	@Override
	public Setter getSetter(Class theClass, String propertyName) throws PropertyNotFoundException
	{
		if (propertyName.length() == 1)
			propertyName = String.valueOf(Character.toUpperCase(propertyName.charAt(0)));
		return super.getSetter(theClass, propertyName + MetaMetadataJavaTranslator.SCALAR_GETTER_SETTER_SUFFIX);
	}

}

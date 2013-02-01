package ecologylab.bigsemantics.compiler.orm.scalartypes;

import org.hibernate.type.FloatType;

import ecologylab.bigsemantics.metadata.scalar.MetadataFloat;

public class MetadataFloatHibernateType extends MetadataScalarHibernateType
{

	public MetadataFloatHibernateType()
	{
		super(MetadataFloat.class, FloatType.INSTANCE);
	}

}

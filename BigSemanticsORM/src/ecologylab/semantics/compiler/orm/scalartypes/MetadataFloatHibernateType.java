package ecologylab.semantics.compiler.orm.scalartypes;

import org.hibernate.type.FloatType;

import ecologylab.semantics.metadata.scalar.MetadataFloat;

public class MetadataFloatHibernateType extends MetadataScalarHibernateType
{

	public MetadataFloatHibernateType()
	{
		super(MetadataFloat.class, FloatType.INSTANCE);
	}

}

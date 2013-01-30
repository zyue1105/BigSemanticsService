package ecologylab.semantics.compiler.orm.scalartypes;

import org.hibernate.type.DoubleType;

import ecologylab.semantics.metadata.scalar.MetadataDouble;

public class MetadataDoubleHibernateType extends MetadataScalarHibernateType
{

	public MetadataDoubleHibernateType()
	{
		super(MetadataDouble.class, DoubleType.INSTANCE);
	}

}

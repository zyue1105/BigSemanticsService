package ecologylab.bigsemantics.compiler.orm.scalartypes;

import org.hibernate.type.DateType;

import ecologylab.bigsemantics.metadata.scalar.MetadataDate;

public class MetadataDateHibernateType extends MetadataScalarHibernateType
{

	public MetadataDateHibernateType()
	{
		super(MetadataDate.class, DateType.INSTANCE);
	}

}

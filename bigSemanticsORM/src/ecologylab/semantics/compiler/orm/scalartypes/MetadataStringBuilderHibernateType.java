package ecologylab.semantics.compiler.orm.scalartypes;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.type.TextType;

import ecologylab.semantics.metadata.scalar.MetadataStringBuilder;

public class MetadataStringBuilderHibernateType extends MetadataScalarHibernateType
{

	public MetadataStringBuilderHibernateType()
	{
		super(MetadataStringBuilder.class, TextType.INSTANCE);
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException
	{
		return value == null ? null : ((MetadataStringBuilder) value).getValue().toString();
	}

	@Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException
	{
		return cached == null ? null : new MetadataStringBuilder(new StringBuilder((String) cached));
	}
	
}

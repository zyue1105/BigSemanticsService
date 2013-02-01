package ecologylab.bigsemantics.compiler.orm.scalartypes;

import org.hibernate.type.TextType;

import ecologylab.bigsemantics.metadata.scalar.MetadataString;

public class MetadataStringHibernateType extends MetadataScalarHibernateType
{

	public MetadataStringHibernateType()
	{
		super(MetadataString.class, TextType.INSTANCE);
	}

//	@Override
//	public Serializable disassemble(Object value) throws HibernateException
//	{
//		return value == null ? "" : ((MetadataString) value).getValue();
//	}

//	@Override
//	public Object assemble(Serializable cached, Object owner) throws HibernateException
//	{
//		return cached == null ? new MetadataString("") : new MetadataString((String) cached);
//	}

}

package ecologylab.bigsemantics.compiler.orm.scalartypes;

import java.io.File;
import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.type.StringType;

import ecologylab.bigsemantics.metadata.scalar.MetadataFile;

public class MetadataFileHibernateType extends MetadataScalarHibernateType
{

	public MetadataFileHibernateType()
	{
		super(MetadataFile.class, StringType.INSTANCE);
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException
	{
		return value == null ? null : ((MetadataFile) value).getValue().getAbsolutePath();
	}

	@Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException
	{
		return cached == null ? null : new MetadataFile(new File((String) cached));
	}
	
}

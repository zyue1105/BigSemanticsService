package ecologylab.bigsemantics.compiler.orm.scalartypes;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;

import ecologylab.bigsemantics.metadata.scalar.MetadataScalarBase;

public class MetadataScalarHibernateType extends HibernateUserType
{

	private static Set<String>	registeredMetadataScalarHibernateTypes	= new HashSet<String>();

	public static boolean isRegisteredMetadataScalarHibernateType(String typeName)
	{
		return registeredMetadataScalarHibernateTypes.contains(typeName);
	}

	public MetadataScalarHibernateType(Class<? extends MetadataScalarBase> metadataScalarClass,
			AbstractSingleColumnStandardBasicType hibernateTypeInstance)
	{
		super(metadataScalarClass, hibernateTypeInstance);
		registeredMetadataScalarHibernateTypes.add(this.getClass().getName());
	}

	public boolean equals(Object x, Object y) throws HibernateException
	{
		if (x == null && y == null)
			return true;
		if (x == null && y != null || x != null && y == null)
			return false;
		if (x instanceof MetadataScalarBase && y instanceof MetadataScalarBase)
			return ((MetadataScalarBase) x).getValue().equals(((MetadataScalarBase) y).getValue());
		else
			return x.equals(y);
	}

	public int hashCode(Object x) throws HibernateException
	{
		return ((MetadataScalarBase) x).getValue().hashCode();
	}

	public Serializable disassemble(Object value) throws HibernateException
	{
		return value == null ? null : (Serializable) ((MetadataScalarBase) value).getValue();
	}

	public Object assemble(Serializable cached, Object owner) throws HibernateException
	{
		if (cached == null)
			return null;

		MetadataScalarBase scalar = null;
		try
		{
			scalar = (MetadataScalarBase) this.theType.newInstance();
			scalar.setValue(cached);
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		return scalar;
	}

}

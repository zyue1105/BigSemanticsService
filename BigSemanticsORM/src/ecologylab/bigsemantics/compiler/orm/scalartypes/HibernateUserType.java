package ecologylab.bigsemantics.compiler.orm.scalartypes;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.usertype.UserType;

public abstract class HibernateUserType implements UserType
{

	protected Class<?>															theType;

	protected int[]																	sqlTypes;

	protected AbstractSingleColumnStandardBasicType	hibernateTypeInstance;

	public HibernateUserType(Class<?> theType,
			AbstractSingleColumnStandardBasicType hibernateTypeInstance)
	{
		this.theType = theType;
		this.sqlTypes = new int[] { hibernateTypeInstance.sqlType() };
		this.hibernateTypeInstance = hibernateTypeInstance;
		HibernateUserTypeRegistry.register(theType.getName(), this.getClass());
	}

	public int[] sqlTypes()
	{
		return sqlTypes;
	}

	public Class returnedClass()
	{
		return theType;
	}

	public boolean equals(Object x, Object y) throws HibernateException
	{
		if (x == null && y == null)
			return true;
		if (x == null && y != null || x != null && y == null)
			return false;
		else
			return x.equals(y);
	}

	public int hashCode(Object x) throws HibernateException
	{
		return x.hashCode();
	}

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException,
			SQLException
	{
		return assemble((Serializable) hibernateTypeInstance.get(rs, names[0]), null);
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException,
			SQLException
	{
		hibernateTypeInstance.set(st, disassemble(value), index);
	}

	public Object deepCopy(Object value) throws HibernateException
	{
		return value; // if immutable
	}

	public boolean isMutable()
	{
		return false; // immutable by default
	}

	abstract public Serializable disassemble(Object value) throws HibernateException;

	abstract public Object assemble(Serializable cached, Object owner) throws HibernateException;

	public Object replace(Object original, Object target, Object owner) throws HibernateException
	{
		return original; // if immutable
	}

}

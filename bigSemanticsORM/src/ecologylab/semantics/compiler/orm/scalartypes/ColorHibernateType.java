package ecologylab.semantics.compiler.orm.scalartypes;

import java.awt.Color;
import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.type.IntegerType;

public class ColorHibernateType extends HibernateUserType
{

	public ColorHibernateType()
	{
		super(Color.class, IntegerType.INSTANCE);
	}

	public Serializable disassemble(Object value) throws HibernateException
	{
		return value == null ? 0 : ((Color) value).getRGB();
	}

	public Object assemble(Serializable cached, Object owner) throws HibernateException
	{
		return cached == null ? new Color(0, true) : new Color((Integer) cached, true);
	}

}

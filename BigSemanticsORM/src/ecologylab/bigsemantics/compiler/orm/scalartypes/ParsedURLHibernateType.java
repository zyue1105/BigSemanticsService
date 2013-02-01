package ecologylab.bigsemantics.compiler.orm.scalartypes;

import java.io.Serializable;
import java.net.URL;

import org.hibernate.HibernateException;
import org.hibernate.type.UrlType;

import ecologylab.net.ParsedURL;

public class ParsedURLHibernateType extends HibernateUserType
{

	public ParsedURLHibernateType()
	{
		super(ParsedURL.class, UrlType.INSTANCE);
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException
	{
		return value == null ? null : ((ParsedURL) value).url();
	}

	@Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException
	{
		return cached == null ? null : new ParsedURL((URL) cached);
	}

}

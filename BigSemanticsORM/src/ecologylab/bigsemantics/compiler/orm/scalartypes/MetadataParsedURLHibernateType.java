package ecologylab.bigsemantics.compiler.orm.scalartypes;

import java.io.Serializable;
import java.net.URL;

import org.hibernate.HibernateException;
import org.hibernate.type.UrlType;

import ecologylab.bigsemantics.metadata.scalar.MetadataParsedURL;
import ecologylab.net.ParsedURL;

public class MetadataParsedURLHibernateType extends MetadataScalarHibernateType
{

	public MetadataParsedURLHibernateType()
	{
		super(MetadataParsedURL.class, UrlType.INSTANCE);
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException
	{
		if (value == null)
			return null;
		MetadataParsedURL metadataPurl = (MetadataParsedURL) value;
		ParsedURL purl = metadataPurl.getValue();
		return purl == null ? null : purl.url();
	}

	@Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException
	{
		return cached == null ? null : new MetadataParsedURL(new ParsedURL((URL) cached));
	}

}

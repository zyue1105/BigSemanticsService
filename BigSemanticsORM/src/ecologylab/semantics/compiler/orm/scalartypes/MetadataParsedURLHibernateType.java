package ecologylab.semantics.compiler.orm.scalartypes;

import java.io.Serializable;
import java.net.URL;

import org.hibernate.HibernateException;
import org.hibernate.type.UrlType;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.scalar.MetadataParsedURL;

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

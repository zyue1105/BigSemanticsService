package ecologylab.bigsemantics.compiler.orm.scalartypes;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.usertype.UserType;

public class HibernateUserTypeRegistry
{

	private static final Map<String, Class<? extends UserType>>	registry																= new HashMap<String, Class<? extends UserType>>();

	public static final HibernateUserType												METADATA_DOUBLE_HIBERNATE_TYPE					= new MetadataDoubleHibernateType();

	public static final HibernateUserType												METADATA_DATE_HIBERNATE_TYPE						= new MetadataDateHibernateType();

	public static final HibernateUserType												METADATA_FLOAT_HIBERNATE_TYPE						= new MetadataFloatHibernateType();

	public static final HibernateUserType												METADATA_INTEGER_HIBERNATE_TYPE					= new MetadataIntegerHibernateType();

	public static final HibernateUserType												METADATA_PARSED_URL_HIBERNATE_TYPE			= new MetadataParsedURLHibernateType();

	public static final HibernateUserType												METADATA_STRING_HIBERNATE_TYPE					= new MetadataStringHibernateType();

	public static final HibernateUserType												METADATA_STRING_BUILDER_HIBERNATE_TYPE	= new MetadataStringBuilderHibernateType();

	public static final HibernateUserType												METADATA_FILE_HIBERNATE_TYPE						= new MetadataFileHibernateType();

	public static final HibernateUserType												COLOR_HIBERNATE_TYPE										= new ColorHibernateType();

	public static final HibernateUserType												PARSED_URL_HIBERNATE_TYPE								= new ParsedURLHibernateType();

	public static void register(String scalarClassName, Class<? extends UserType> hibernateType)
	{
		registry.put(scalarClassName, hibernateType);
	}

	public static Class<? extends UserType> getHibernateType(String typeName)
	{
		return registry.get(typeName);
	}

}

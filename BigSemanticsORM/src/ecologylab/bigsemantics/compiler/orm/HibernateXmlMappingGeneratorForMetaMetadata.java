package ecologylab.bigsemantics.compiler.orm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.usertype.UserType;

import ecologylab.bigsemantics.collecting.MetaMetadataRepositoryLocator;
import ecologylab.bigsemantics.compiler.orm.scalartypes.HibernateUserTypeRegistry;
import ecologylab.bigsemantics.compiler.orm.scalartypes.MetadataParsedURLHibernateType;
import ecologylab.bigsemantics.compiler.orm.scalartypes.MetadataScalarAccessor;
import ecologylab.bigsemantics.compiler.orm.scalartypes.MetadataScalarHibernateType;
import ecologylab.bigsemantics.compiler.orm.scalartypes.MetadataStringBuilderHibernateType;
import ecologylab.bigsemantics.compiler.orm.scalartypes.MetadataStringHibernateType;
import ecologylab.bigsemantics.compiler.orm.scalartypes.ParsedURLHibernateType;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metadata.MetadataClassDescriptor;
import ecologylab.bigsemantics.metadata.builtins.MetadataBuiltinsTypesScope;
import ecologylab.bigsemantics.metadata.builtins.declarations.MetadataBuiltinDeclarationsTranslationScope;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepository;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepositoryLoader;
import ecologylab.bigsemantics.metametadata.MmdScope;
import ecologylab.bigsemantics.model.TextChunkBase;
import ecologylab.bigsemantics.model.TextToken;
import ecologylab.generic.HashMapArrayList;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;
import ecologylab.translators.hibernate.HibernateXmlMappingGenerator;
import ecologylab.translators.hibernate.hbmxml.HibernateClass;
import ecologylab.translators.hibernate.hbmxml.HibernateFieldBase;
import ecologylab.translators.hibernate.hbmxml.HibernateList;
import ecologylab.translators.hibernate.hbmxml.HibernateProperty;

/**
 * 
 * @author quyin
 * 
 */
@SuppressWarnings("rawtypes")
public class HibernateXmlMappingGeneratorForMetaMetadata extends HibernateXmlMappingGenerator
{

	public static final File								REPOSITORY_LOCATION		= MetaMetadataRepositoryLocator.locateRepositoryByDefaultLocations();

	public static final Map<String, String>	CLASS_NAME_ORM_ID_MAP	= new HashMap<String, String>();

	public static final File								MMD_HBM_DIR						= new File("./resources/mmdhbm");
	
	static
	{
		CLASS_NAME_ORM_ID_MAP.put(Metadata.class.getName(), "ormId");
		CLASS_NAME_ORM_ID_MAP.put(TextChunkBase.class.getName(), "ormId");
		CLASS_NAME_ORM_ID_MAP.put(TextToken.class.getName(), "ormId");
	}

	private MetaMetadataRepositoryLoader		repositoryLoader			= new MetaMetadataRepositoryLoader();

	private MetaMetadataRepository					repository;

	private SimplTypesScope									metadataSimplTypesScope;
	
	private MetadataBuiltinDeclarationNamesHelper namesHelper = new MetadataBuiltinDeclarationNamesHelper();

	public HibernateXmlMappingGeneratorForMetaMetadata() throws FileNotFoundException, SIMPLTranslationException
	{
		super(new MmdCachedDbNameGenerator());

		SimplTypesScope.enableGraphSerialization();
		metadataSimplTypesScope = getTargetScope();
		repository = repositoryLoader.loadFromDir(REPOSITORY_LOCATION, Format.XML);
		repository.bindMetadataClassDescriptorsToMetaMetadata(metadataSimplTypesScope);

		new HibernateUserTypeRegistry();
	}
	
	protected SimplTypesScope getTargetScope()
	{
		return RepositoryMetadataTranslationScope.get();
	}
	
	private static Map<String, Integer>	DEFAULT_LENGTH_BY_TYPE	= new HashMap<String, Integer>();

	static
	{
		DEFAULT_LENGTH_BY_TYPE.put(MetadataStringHibernateType.class.getName(), 1024);
		DEFAULT_LENGTH_BY_TYPE.put(MetadataStringBuilderHibernateType.class.getName(), 1024);
		DEFAULT_LENGTH_BY_TYPE.put(MetadataParsedURLHibernateType.class.getName(), 1024);
		DEFAULT_LENGTH_BY_TYPE.put(ParsedURLHibernateType.class.getName(), 1024);
	}
	
	private static class AddingAdditionalPropertiesRecord
	{
		public String mappedClassName;
		public Map<String, HibernateFieldBase> propertyMappings;
		
		public AddingAdditionalPropertiesRecord(String mappedClassName, Map<String, HibernateFieldBase> propertyMappings)
		{
			super();
			this.mappedClassName = mappedClassName;
			this.propertyMappings = propertyMappings;
		}
	}
	
	private Map<String, AddingAdditionalPropertiesRecord> addingAdditionalPropertiesRecords =
			new HashMap<String, AddingAdditionalPropertiesRecord>();
	
	private void addAdditionalProperties(String mappedClassName, Map<String, HibernateFieldBase> propertyMappings)
	{
		HibernateClass classMapping = this.getAllMappings().get(mappedClassName);
		if (classMapping != null)
		{
			debug("adding additional properties to " + mappedClassName);
			classMapping.getProperties().putAll(propertyMappings);
		}
	}
	
	@Override
	protected HibernateClass generateClassMapping(ClassDescriptor cd)
	{
		String className = cd.getDescribedClassName();
		boolean isBuiltinDeclaration = MetadataBuiltinDeclarationsTranslationScope.get()
				.getClassDescriptorByClassName(className) != null;
		boolean isBuiltin = MetadataBuiltinsTypesScope.get().getClassDescriptorByClassName(className) != null;
		if (cd instanceof MetadataClassDescriptor)
		{
			MetadataClassDescriptor mcd = (MetadataClassDescriptor) cd;
			if (mcd.getDefiningMmd() != null)
				isBuiltin = isBuiltin || mcd.getDefiningMmd().isBuiltIn();
		}
		// if this is a built-in class but not a built-in declaration, we simply ignore it because we
		// will map it using the declaration one
		if (isBuiltin && !isBuiltinDeclaration)
		{
			if (!addingAdditionalPropertiesRecords.containsKey(className))
			{
				HashMapArrayList<String, HibernateFieldBase> propertyMappings = generatePropertyMappingsForClass(cd);
				if (propertyMappings.size() > 0)
				{
					String targetClassName = namesHelper.getDeclarationClassName(className);
					debug("will add property mappings from " + cd + " to " + targetClassName + " in the end ...");
					addingAdditionalPropertiesRecords.put(className, new AddingAdditionalPropertiesRecord(targetClassName, propertyMappings));
				}
			}
			return null;
		}
		return super.generateClassMapping(cd);
	}
	
	@Override
	protected void generateMappingsDoneHook()
	{
		for (AddingAdditionalPropertiesRecord record : addingAdditionalPropertiesRecords.values())
		{
			addAdditionalProperties(record.mappedClassName, record.propertyMappings);
		}
	}
	
	protected static Map<String, String> classNameForHbmSpecialCases = new HashMap<String, String>();
	static
	{
		classNameForHbmSpecialCases.put("ecologylab.semantics.metadata.builtins.declarations.MetadataDeclaration", "ecologylab.semantics.metadata.Metadata");
		classNameForHbmSpecialCases.put("ecologylab.semantics.metadata.builtins.declarations.InformationCompositionDeclaration", null);
	}
	
	@Override
	protected String getClassNameForHbm(ClassDescriptor cd)
	{
		String describedClassName = cd.getDescribedClassName();
		boolean isBuiltinDeclaration = MetadataBuiltinDeclarationsTranslationScope.get()
				.getClassDescriptorByClassName(describedClassName) != null;
		if (isBuiltinDeclaration)
		{
			if (classNameForHbmSpecialCases.containsKey(describedClassName))
				return classNameForHbmSpecialCases.get(describedClassName);
			
			String packageName = cd.getDescribedClassPackageName();
			packageName = packageName.substring(0, packageName.lastIndexOf('.'));
			String classSimpleName = cd.getDescribedClassSimpleName();
			classSimpleName = classSimpleName.substring(0, classSimpleName.lastIndexOf("Declaration"));
			return packageName + "." + classSimpleName;
		}
		else
			return super.getClassNameForHbm(cd);
	}
	
	protected String getClassNameForHbmSpecialCases(String className)
	{
			if (className.equals("ecologylab.semantics.metadata.builtins.declarations.MetadataDeclaration"))
				return "ecologylab.semantics.metadata.Metadata";
			else if (className.equals("ecologylab.semantics.metadata.builtins.declarations.InformationCompositionDeclaration"))
				return null;
			return className;
	}
	
	@Override
	protected HibernateProperty generatePropertyMapping(ClassDescriptor cd, FieldDescriptor fd)
	{
		HibernateProperty prop = super.generatePropertyMapping(cd, fd);

		if (MetadataScalarHibernateType.isRegisteredMetadataScalarHibernateType(prop.getType()))
			prop.setAccess(MetadataScalarAccessor.class.getName());

		if (DEFAULT_LENGTH_BY_TYPE.containsKey(prop.getType()))
			prop.setLength(DEFAULT_LENGTH_BY_TYPE.get(prop.getType()));

		return prop;
	}

	@Override
	protected HibernateList generateListOfElementMapping(ClassDescriptor cd, FieldDescriptor fd)
	{
		// FIXME this is just a workaround before we have a way to specify laziness for these two field!
		// should be removed soon!
		if (fd.getName().equals("mixins") || fd.getName().equals("linkedMetadataList"))
			return null;
		return super.generateListOfElementMapping(cd, fd);
	}

	@Override
	protected String translateType(String typeName)
	{
		Class<? extends UserType> hibernateType = HibernateUserTypeRegistry.getHibernateType(typeName);
		if (hibernateType != null)
			return hibernateType.getName();
		return typeName;
	}

	public void generateMappingsForMetadata() throws FileNotFoundException, SIMPLTranslationException
	{
		SimplTypesScope mappingTScope = SimplTypesScope.get("MMD-ORM", new SimplTypesScope[]
		{ metadataSimplTypesScope });

		// package-wide visible mmds
		for (String packageName : repository.getPackageMmdScopes().keySet())
		{
			MmdScope packageMmdScope = repository.getPackageMmdScopes().get(packageName);
			for (MetaMetadata packageMmd : packageMmdScope.values())
			{
				if (packageMmd.getMetadataClassDescriptor() == null)
					packageMmd.bindMetadataClassDescriptor(mappingTScope);
				addMetadataClassDescriptors(packageMmd, mappingTScope);
			}
		}

		// inline mmds
		for (MetaMetadata mmd : repository.values())
		{
			if (mmd.getMmdScope() != null && mmd.getMmdScope().size() > 0)
				for (MetaMetadata inlineMmd : mmd.getMmdScope().values())
					addMetadataClassDescriptors(inlineMmd, mappingTScope);
		}

		List<String> mappingImports = this.generateMappings(MMD_HBM_DIR, mappingTScope,
				CLASS_NAME_ORM_ID_MAP);
		System.out.println("\n\nAdd the following lines to your hibernate.cfg.xml file:");
		for (String mappingImport : mappingImports)
			System.out.println(mappingImport);
	}

	/**
	 * add the metadata class descriptor of this meta-metadata to the scope, and also recursively
	 * look at inline meta-metadata type definitions from the mmdScope, and possibly add metadata
	 * class descriptors for them (again, recursively).
	 * 
	 * @param mmd
	 * @param mappingTScope
	 */
	private void addMetadataClassDescriptors(MetaMetadata mmd, SimplTypesScope mappingTScope)
	{
		MetadataClassDescriptor metadataClassDescriptor = mmd.getMetadataClassDescriptor();
		mappingTScope.addTranslation(metadataClassDescriptor);
		MmdScope mmdScope = mmd.getMmdScope();
		if (mmdScope != null && mmdScope.size() > 0)
			for (MetaMetadata inlineMmd : mmdScope.values())
				addMetadataClassDescriptors(inlineMmd, mappingTScope);
	}

	public static void main(String[] args) throws FileNotFoundException, SIMPLTranslationException
	{
		HibernateXmlMappingGeneratorForMetaMetadata generator = new HibernateXmlMappingGeneratorForMetaMetadata();
		generator.generateMappingsForMetadata();
	}

}

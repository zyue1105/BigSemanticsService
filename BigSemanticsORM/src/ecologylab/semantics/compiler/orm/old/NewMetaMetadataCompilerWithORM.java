package ecologylab.semantics.compiler.orm.old;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ecologylab.collections.MultiAncestorScope;
import ecologylab.semantics.compiler.CompilerConfig;
import ecologylab.semantics.compiler.NewMetaMetadataCompiler;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.translators.hibernate.HibernateXmlMappingGenerator;
import ecologylab.translators.java.JavaTranslationException;

public class NewMetaMetadataCompilerWithORM extends NewMetaMetadataCompiler
{

//	public static final Map<String, String>											CLASS_NAME_ORM_ID_MAP;
//
//	static
//	{
//		CLASS_NAME_ORM_ID_MAP = new HashMap<String, String>();
//		CLASS_NAME_ORM_ID_MAP.put(Metadata.class.getName(), "ormId");
//	}
//
//	public static Class<? extends HibernateXmlMappingGenerator>	ORM_GENERATOR_CLASS	= HibernateXmlMappingGeneratorForMetaMetadata.class;
//
//	private HibernateXmlMappingGenerator												ormGenerator;
//
//	private File																								hbmDir;
//
//	public NewMetaMetadataCompilerWithORM(File hbmDir)
//	{
//		try
//		{
//			this.ormGenerator = ORM_GENERATOR_CLASS.newInstance();
//		}
//		catch (InstantiationException e)
//		{
//			e.printStackTrace();
//		}
//		catch (IllegalAccessException e)
//		{
//			e.printStackTrace();
//		}
//		this.hbmDir = hbmDir;
//	}
//
//	protected void compilerHook(MetaMetadataRepository repository)
//	{
//		super.compilerHook(repository);
//
//		SimplTypesScope.enableGraphSerialization();
//
//		SimplTypesScope mappingTScope = SimplTypesScope.get("object-relation-mappings",
//				new Class[] {});
//		
//		for (MetaMetadata mmd : repository.values())
//			addMetadataClassDescriptors(mmd, mappingTScope);
//		for (String packageName : repository.getPackageMmdScopes().keySet())
//		{
//			MultiAncestorScope<MetaMetadata> packageMmdScope = repository.getPackageMmdScopes().get(packageName);
//			for (MetaMetadata mmd : packageMmdScope.values())
//			{
//				addMetadataClassDescriptors(mmd, mappingTScope);
//			}
//		}
//
//		try
//		{
//			List<String> mappingImports = ormGenerator.generateMappings(hbmDir, mappingTScope,
//					CLASS_NAME_ORM_ID_MAP);
//			System.out.println("\n\nAdd the following lines to your hibernate.cfg.xml file:");
//			for (String mappingImport : mappingImports)
//				System.out.println(mappingImport);
//		}
//		catch (FileNotFoundException e)
//		{
//			e.printStackTrace();
//		}
//		catch (SIMPLTranslationException e)
//		{
//			e.printStackTrace();
//		}
//	}
//
//	private void addMetadataClassDescriptors(MetaMetadata mmd, SimplTypesScope mappingTScope)
//	{
//		debug("Prepare to generate hibernate mapping for " + mmd);
//		mappingTScope.addTranslation(mmd.getMetadataClassDescriptor());
//		MultiAncestorScope<MetaMetadata> mmdScope = mmd.getMmdScope();
//		if (mmdScope != null && mmdScope.size() > 0)
//			for (MetaMetadata inlineMmd : mmdScope.values())
//				addMetadataClassDescriptors(inlineMmd, mappingTScope);
//	}
//
//	/**
//	 * @param args
//	 * @throws JavaTranslationException
//	 * @throws SIMPLTranslationException
//	 * @throws IOException
//	 */
//	public static void main(String[] args) throws IOException, SIMPLTranslationException,
//			JavaTranslationException
//	{
//		CompilerConfig config = new ORMCompilerConfig();
//		NewMetaMetadataCompilerWithORM compiler = new NewMetaMetadataCompilerWithORM(new File(
//				"../ecologylabGeneratedSemanticsORM/resources/hbm"));
//		compiler.compile(config);
//	}

}

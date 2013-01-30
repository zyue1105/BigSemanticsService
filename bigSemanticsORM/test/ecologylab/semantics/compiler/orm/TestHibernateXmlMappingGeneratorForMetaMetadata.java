package ecologylab.semantics.compiler.orm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.MetaMetadataRepositoryLoader;
import ecologylab.semantics.metametadata.MmdScope;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;
import ecologylab.translators.hibernate.HibernateXmlMappingGenerator;
import ecologylab.translators.hibernate.TestHibernateXmlMappingGenerator;

public class TestHibernateXmlMappingGeneratorForMetaMetadata extends TestHibernateXmlMappingGenerator
{

	private static final String	HBM_FOLDER	= "../testMetaMetadataCompilerWithORM/resources/";
	
	@Override
	protected HibernateXmlMappingGenerator getHibernateXmlMappingGenerator()
	{
		return new HibernateXmlMappingGeneratorForMetaMetadata();
	}
	
	protected void doTest(String hbmFolderPath, String testName, File testingRepository) throws FileNotFoundException, SIMPLTranslationException
	{
		SimplTypesScope.enableGraphSerialization();
		
		System.out.println("\n\nTesting " + testName + "...\n\n");

		MetaMetadataRepositoryLoader repositoryLoader = new MetaMetadataRepositoryLoader();
		List<File> repositoryFiles = Arrays.asList(new File[] { testingRepository });
		MetaMetadataRepository repository = repositoryLoader.loadFromFiles(repositoryFiles, Format.XML);
		repository.traverseAndGenerateTranslationScope(testName + "-repository");

		SimplTypesScope mappingTScope = SimplTypesScope.get(testName, new Class[] {});
		for (MetaMetadata mmd : repository.values())
		{
			addMetadataClassDescriptors(mmd, mappingTScope);
		}

		super.doTest(hbmFolderPath, testName, mappingTScope, null);
	}

	private void addMetadataClassDescriptors(MetaMetadata mmd, SimplTypesScope mappingTScope)
	{
		mappingTScope.addTranslation(mmd.getMetadataClassDescriptor());
		MmdScope mmdScope = mmd.getMmdScope();
		if (mmdScope != null && mmdScope.size() > 0)
			for (MetaMetadata inlineMmd : mmdScope.values())
				addMetadataClassDescriptors(inlineMmd, mappingTScope);
	}

	public void testArticles() throws FileNotFoundException, SIMPLTranslationException
	{
		doTest(HBM_FOLDER, "test-articles", new File("data/testRepository/testArticles.xml"));
	}

	public void testGeneratingBasicTScope() throws FileNotFoundException, SIMPLTranslationException
	{
		doTest(HBM_FOLDER, "test-basic-tscope", new File("data/testRepository/testGeneratingBasicTScope.xml"));
	}

	public void testTypeGraphs() throws FileNotFoundException, SIMPLTranslationException
	{
		doTest(HBM_FOLDER, "test-type-graphs", new File("data/testRepository/testTypeGraphs.xml"));
	}

	public void testInlineMmd() throws FileNotFoundException, SIMPLTranslationException
	{
		doTest(HBM_FOLDER, "test-inline-mmd", new File("data/testRepository/testInlineMmd.xml"));
	}

	public void testScalarCollections() throws FileNotFoundException, SIMPLTranslationException
	{
		doTest(HBM_FOLDER, "test-scalar-collections", new File("data/testRepository/testScalarCollections.xml"));
	}

	public void testPolymorphicFields() throws FileNotFoundException, SIMPLTranslationException
	{
		doTest(HBM_FOLDER, "test-poly-fields", new File("data/testRepository/testPolymorphicFields.xml"));
	}

	public void testOtherTags() throws FileNotFoundException, SIMPLTranslationException
	{
		doTest(HBM_FOLDER, "test-other-tags", new File("data/testRepository/testOtherTags.xml"));
	}

	public void testPolymorphicScope() throws FileNotFoundException, SIMPLTranslationException
	{
		doTest(HBM_FOLDER, "test-poly-scope", new File("data/testRepository/testPolymorphicScope.xml"));
	}

	public static void main(String[] args) throws FileNotFoundException, SIMPLTranslationException
	{
		TestHibernateXmlMappingGeneratorForMetaMetadata test = new TestHibernateXmlMappingGeneratorForMetaMetadata();
		test.testArticles();
	}

}

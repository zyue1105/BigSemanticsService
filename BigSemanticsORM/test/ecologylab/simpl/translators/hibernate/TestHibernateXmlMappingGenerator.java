package ecologylab.simpl.translators.hibernate;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ecologylab.bigsemantics.compiler.orm.example.AcmPaper;
import ecologylab.bigsemantics.compiler.orm.example.Article;
import ecologylab.bigsemantics.compiler.orm.example.Author;
import ecologylab.bigsemantics.compiler.orm.example.BaseEntity;
import ecologylab.bigsemantics.compiler.orm.example.Conference;
import ecologylab.bigsemantics.compiler.orm.example.Paper;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.translators.hibernate.HibernateXmlMappingGenerator;

public class TestHibernateXmlMappingGenerator
{

	private static final String	HBM_FOLDER	= "resources/test-generating-hbm-from-tscope/";

	protected HibernateXmlMappingGenerator getHibernateXmlMappingGenerator()
	{
		return new HibernateXmlMappingGenerator();
	}

	protected void doTest(String hbmFolderPath, String testName, SimplTypesScope tscope, Map<String, String> idFieldNameByClass)
			throws FileNotFoundException, SIMPLTranslationException
	{
		System.out.println("\n\nTesting " + testName + "...\n\n");

		File hbmDir = new File(hbmFolderPath + testName);
		HibernateXmlMappingGenerator gen = getHibernateXmlMappingGenerator();

		List<String> mappingImports = gen.generateMappings(hbmDir, tscope, idFieldNameByClass);
		System.out.println("\n\nAdd the following lines to your hibernate.cfg.xml file:");
		for (String mappingImport : mappingImports)
			System.out.println(mappingImport);
	}

	protected void testArticles() throws FileNotFoundException, SIMPLTranslationException
	{
		String testName = "test-articles";
		SimplTypesScope tscope = SimplTypesScope.get(testName, new Class[] {
				BaseEntity.class,
				Author.class,
				Article.class,
				Conference.class,
				Paper.class,
				AcmPaper.class,
		});
		Map<String, String> idFieldNameByClass = new HashMap<String, String>();
		idFieldNameByClass.put(BaseEntity.class.getName(), "id");
		doTest(HBM_FOLDER, testName, tscope, idFieldNameByClass);
	}

	public static void main(String[] args) throws FileNotFoundException, SIMPLTranslationException
	{
		TestHibernateXmlMappingGenerator test = new TestHibernateXmlMappingGenerator();
		test.testArticles();
	}

}

package ecologylab.semantics.compiler.orm;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Property;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.semantics.metadata.scalar.MetadataParsedURL;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.test.NewMmTest;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.TranslationContext;
import ecologylab.serialization.TranslationContextPool;
import ecologylab.serialization.formatenums.Format;

/**
 * 
 * @author quyin
 * 
 */
public class TestORMWithMetadata extends NewMmTest
{

	private MetadataORMFacade	ormFacade	= MetadataORMFacade.defaultSingleton();
	
	public TestORMWithMetadata() throws SIMPLTranslationException
	{
		super("test-orm-with-metadata");
	}

	@Override
	public synchronized void callback(DocumentClosure incomingClosure)
	{
		super.callback(incomingClosure);

		Document document = incomingClosure.getDocument();

		try
		{
			saveDocumentToFile(document, "before");
		}
		catch (SIMPLTranslationException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		ParsedURL location = document.getLocation();
		assert location != null : "Document with null location!";
		MetadataParsedURL mdLocation = new MetadataParsedURL(location);

		// see if it is already in the database
		Session session = ormFacade.newSession();
		Criteria q = session.createCriteria(Document.class).add(
				Property.forName("location").eq(mdLocation));
		List existingDocuments = q.list();
		if (existingDocuments.size() == 0)
		{
			ormFacade.recursivelySave(session, document);
		}
		else
		{
			warning("Document existing in the database: " + location);
		}
		session.close();

		// load again
		Session session1 = ormFacade.newSession();
		q = session1.createCriteria(Document.class).add(Property.forName("location").eq(mdLocation));
		existingDocuments = q.list();
		if (existingDocuments.size() > 0)
		{
			for (Object docObj : existingDocuments)
			{
				Document doc = (Document) docObj;
				try
				{
					saveDocumentToFile(doc, "after");
				}
				catch (SIMPLTranslationException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
		else
		{
			warning("Document not found in database: " + location);
		}
		session1.close();
	}

	private static final File	METADATA_SAVING_DIR	= new File("../ecologylabSemanticsORM/data/testSaveLoadMetadata");

	private void saveDocumentToFile(Document document, String suffix)
			throws SIMPLTranslationException
	{
		ParsedURL location = document.getLocation();
		int code = location.domain().equals("acm.org") ? getAcmId(location) : location.toString()
				.hashCode();
		String fileName = String.format("%s-%d.%s.xml", location.domain(), code, suffix);
		File file = new File(METADATA_SAVING_DIR, fileName);
		TranslationContext translationContext = TranslationContextPool.get().acquire();
		ormFacade.materialize(document, translationContext, null);
		SimplTypesScope.serialize(document, file, Format.XML, translationContext);
		TranslationContextPool.get().release(translationContext);
	}

	private static Pattern	P_ACM_ID	= Pattern.compile("id=(\\d+)");
	
	private int getAcmId(ParsedURL location)
	{
		Matcher m = P_ACM_ID.matcher(location.query());
		if (m.find())
		{
			return Integer.parseInt(m.group(1));
		}
		return 0;
	}

	public void doTest()
	{
		SimplTypesScope.enableGraphSerialization();
		MetaMetadataRepository.initializeTypes();

		collect(TEST_URLS);
	}

	public static String[]	TEST_URLS	= new String[]
	{
			"http://portal.acm.org/citation.cfm?id=1416955",
			"http://news.blogs.cnn.com/2011/04/14/predator-dinosaurs-may-have-been-night-hunters/?hpt=C2",
			"http://rss.cnn.com/rss/cnn_topstories.rss",
			"http://www.dlese.org/dds/services/ddsws1-1?verb=UserSearch&q=water+on+mars&s=0&n=10&client=ddsws10examples",
			"http://www.dlese.org/dds/services/ddsws1-0?verb=GetRecord&id=DLESE-000-000-000-001",
			"http://where.yahooapis.com/geocode?gflags=R&q=-96.28616666666667,30.604833333333332",
			"http://remodelista.com/products/victoria-and-albert-wessex-bath",
			"//",
	};

	public static void main(String[] args)
	{
		try
		{
			TestORMWithMetadata t = new TestORMWithMetadata();
			t.doTest();
		}
		catch (SIMPLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

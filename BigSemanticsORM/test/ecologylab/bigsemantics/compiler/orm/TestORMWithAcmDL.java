package ecologylab.bigsemantics.compiler.orm;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Session;

import ecologylab.bigsemantics.collecting.DownloadStatus;
import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepository;
import ecologylab.generic.Continuation;
import ecologylab.generic.Debug;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.TranslationContext;
import ecologylab.serialization.TranslationContextPool;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * 
 * @author quyin
 * 
 */
public class TestORMWithAcmDL extends Debug implements Continuation<DocumentClosure>
{

	public static String[]	TEST_URLS	= new String[]
	{
		"http://dl.acm.org/citation.cfm?id=1416955&preflayout=flat",
		"http://dl.acm.org/citation.cfm?id=258563&preflayout=flat",
//		"http://dl.acm.org/citation.cfm?id=1871437.1871580&preflayout=flat",
//		"http://dl.acm.org/citation.cfm?id=1376746&CFID=57965867&CFTOKEN=71807547",
	};

	private SimplTypesScope				metadataTScope;

	private SemanticsSessionScope	semanticsScope;

	private MetadataORMFacade			ormFacade;
	
	public TestORMWithAcmDL()
	{
		super();
		
		SimplTypesScope.enableGraphSerialization();
		MetaMetadataRepository.initializeTypes(); // this has to be before RepositoryMetadataTranslationScope.get()
		
		metadataTScope = RepositoryMetadataTranslationScope.get();
		semanticsScope = new SemanticsSessionScope(metadataTScope, CybernekoWrapper.class);
		
		ormFacade	= MetadataORMFacade.defaultSingleton();
	}

	public void doTest()
	{
		final Session session = ormFacade.newSession();
		
		for (String testUrl : TEST_URLS)
		{
			ParsedURL purl = ParsedURL.getAbsolute(testUrl);
			List<Document> existingDocs = ormFacade.lookupDocumentByLocation(session, purl);
			if (existingDocs.size() > 0)
			{
				boolean oneFullyPopulatedFound = false;
				// location seen and at least one document is complete
				for (Document doc : existingDocs)
				{
					if (doc.getDownloadStatus() == DownloadStatus.DOWNLOAD_DONE)
					{
						TranslationContext translationContext = TranslationContextPool.get().acquire();
						// the materialize() method loads lazy evaluated values (e.g. composite and collection
						// fields) from the database, so that the loaded object can be used as normal.
						ormFacade.materialize(doc, translationContext, null);
						serializeOrException(doc);
						TranslationContextPool.get().release(translationContext);
						oneFullyPopulatedFound = true;
						break;
					}
				}
				if (oneFullyPopulatedFound)
					continue;
			}
			
			// location unseen, or no documents completed
			Document acmDoc = semanticsScope.getOrConstructDocument(purl);
			acmDoc.queueDownload(this);
		}
		
		semanticsScope.getDownloadMonitors().requestStops();
	}

	@Override
	public void callback(DocumentClosure incomingClosure)
	{
		Document doc = incomingClosure.getDocument();
		System.out.println("saving document at " + doc.getLocation());
		Session session = ormFacade.newSession();
		ormFacade.recursivelySave(session, doc);
		System.out.println("ORM ID = " + doc.getOrmId());
		session.close();
	}
	
	private static void serializeOrException(Document doc)
	{
		try
		{
			SimplTypesScope.serialize(doc, System.out, StringFormat.XML);
		}
		catch (SIMPLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	public static void main(String[] args)
	{
			TestORMWithAcmDL t = new TestORMWithAcmDL();
			t.doTest();
	}

}

package ecologylab.semantics.compiler.orm.example;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;

import ecologylab.semantics.compiler.orm.MetadataORMFacade;

public class TestORMExample
{

	static SessionFactory	factory;

	static
	{
		Configuration config = new Configuration();
//		factory = config.configure("hibernate-test_example.cfg.xml").buildSessionFactory();
		factory = config.configure("hibernate-test_orm_generator.cfg.xml").buildSessionFactory();
	}

	static Session newSession()
	{
		return factory.openSession();
	}

	public static void main(String[] args)
	{
		initData();
		readAndMaterializeAndWrite();
		readData();
	}

	private static void initData()
	{
		Session session = newSession();

		Transaction tx = session.beginTransaction();

		BaseEntity be1 = new BaseEntity();
		session.save(be1);

		Author author1 = new Author();
		author1.setFirstName("Bill");
		author1.setLastName("Gates");
		session.save(author1);

		Author author2 = new Author();
		author2.setFirstName("Steve");
		author2.setLastName("Jobs");
		session.save(author2);

		Conference conf = new Conference();
		conf.setName("International Conference");
		conf.setYear(2011);
		session.save(conf);

		Paper paper = new Paper();
		paper.setTitle("a paper");
		paper.setAuthors(new ArrayList<Author>());
		paper.getAuthors().add(author1);
		paper.setConference(conf);
		paper.setKeywords(new ArrayList<String>());
		paper.getKeywords().add("some keyword");
		session.save(paper);

		AcmPaper acmPaper = new AcmPaper();
		acmPaper.setTitle("bill + steve talk");
		acmPaper.setAuthors(new ArrayList<Author>());
		acmPaper.getAuthors().add(author1);
		acmPaper.getAuthors().add(author2);
		acmPaper.setConference(conf);
		acmPaper.setKeywords(new ArrayList<String>());
		acmPaper.getKeywords().add("microsoft");
		acmPaper.getKeywords().add("apple");
		acmPaper.setReferences(new ArrayList<Paper>());
		acmPaper.getReferences().add(paper);
		acmPaper.setAcmId("2357");
		session.save(acmPaper);

		tx.commit();

		session.close();
	}

	private static void readData()
	{
		Session session = newSession();

		List<AcmPaper> targetPapers = session.createCriteria(AcmPaper.class)
				.createCriteria("authors")
				.add(Restrictions.eq("lastName", "Gates"))
				.list();

		for (AcmPaper p : targetPapers)
		{
			System.out.format("%s'%d: (%d) %s, %s, %d references.",
					p.getConference().getName(),
					p.getConference().getYear(),
					p.getId(),
					getAuthorList(p.getAuthors()),
					p.getTitle(),
					p.getReferences().size());
			System.out.println();
		}

		session.close();
	}
	
	private static void readAndMaterializeAndWrite()
	{
		Session session = newSession();

		List<AcmPaper> targetPapers = session.createCriteria(AcmPaper.class)
				.createCriteria("authors")
				.add(Restrictions.eq("lastName", "Gates"))
				.list();
		AcmPaper p = targetPapers.get(0);
		
		Class clazz = p.getClass();
		Field field = lookupField(clazz, "conference");
		field.setAccessible(true);
		try
		{
			Object value = field.get(p);
			System.out.println(value.getClass());
			Object trueValue = MetadataORMFacade.getTrueValueFromProxy(value);
			System.out.println(trueValue.getClass());
			Conference conf = (Conference) trueValue;
			conf.setName(conf.getName() + " MODIFIED!");
			field.set(p, trueValue);
		}
		catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		field = lookupField(clazz, "authors");
		field.setAccessible(true);
		try
		{
			List value = (List) field.get(p);
			System.out.println(value.getClass());
			List trueValue = new ArrayList();
			for (Object item : value)
			{
				trueValue.add(MetadataORMFacade.getTrueValueFromProxy(item));
			}
			field.set(p, trueValue);
		}
		catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		session.beginTransaction();
		session.saveOrUpdate(p);
		session.getTransaction().commit();

		session.close();
	}
	
	private static Field lookupField(Class clazz, String fieldName)
	{
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields)
		{
			if (field.getName().equals(fieldName))
				return field;
		}
		Class superClazz = clazz.getSuperclass();
		return lookupField(superClazz, fieldName);
	}

	private static String getAuthorList(List<Author> authors)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < authors.size(); ++i)
		{
			Author a = authors.get(i);
			String fullName = a.getLastName() + ", " + a.getFirstName();
			if (i > 0)
				sb.append(" and ");
			sb.append(fullName);
		}
		return sb.toString();
	}

}

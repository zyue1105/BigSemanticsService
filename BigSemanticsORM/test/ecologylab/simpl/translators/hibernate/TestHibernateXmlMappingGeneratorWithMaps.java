package ecologylab.simpl.translators.hibernate;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.Test;

import ecologylab.serialization.ElementState;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_map;
import ecologylab.serialization.annotations.simpl_map_key_field;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_tag;
import ecologylab.serialization.formatenums.Format;
import ecologylab.translators.hibernate.HibernateXmlMappingGenerator;

@simpl_inherit
@simpl_tag("test_map")
public class TestHibernateXmlMappingGeneratorWithMaps extends ElementState
{

	private static SessionFactory	factory;

	private static synchronized Session newSession()
	{
		if (factory == null)
		{
			factory = (SessionFactory) (new Configuration()).configure("hibernate-test_maps.cfg.xml")
					.buildSessionFactory();
		}
		return factory.openSession();
	}

	@simpl_inherit
	public static class Item extends ElementState
	{
		@simpl_scalar
		private long		id;

		@simpl_scalar
		private String	name;

		public Item()
		{
			super();
		}

		public Item(String name)
		{
			this.name = name;
		}

		public long getId()
		{
			return id;
		}

		public void setId(long id)
		{
			this.id = id;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}
	}

	@simpl_scalar
	private long													id;

	@simpl_map("float")
	private Map<String, Float>						scalarScalarMap;

	@simpl_map("item")
	@simpl_map_key_field("name")
	private Map<String, Item>							scalarElementMap;

	private HibernateXmlMappingGenerator	generator	= new HibernateXmlMappingGenerator();

	@Test
	public void generateMappings() throws FileNotFoundException, SIMPLTranslationException
	{
		SimplTypesScope testTScope = SimplTypesScope.get("test-maps", new Class[] {
				TestHibernateXmlMappingGeneratorWithMaps.class, Item.class });
		Map<String, String> idFieldNameByClass = new HashMap<String, String>();
		idFieldNameByClass.put(TestHibernateXmlMappingGeneratorWithMaps.class.getName(), "id");
		idFieldNameByClass.put(Item.class.getName(), "id");
		List<String> imports = generator.generateMappings(new File("resources/test-generating-hbm-from-tscope/test-maps"), testTScope, idFieldNameByClass);
		for (String importStatement : imports)
			System.out.println(importStatement);
	}

	@Test
	public void testSavingAndLoading() throws SIMPLTranslationException
	{
		TestHibernateXmlMappingGeneratorWithMaps t = new TestHibernateXmlMappingGeneratorWithMaps();

		t.setScalarScalarMap(new HashMap<String, Float>());
		t.getScalarScalarMap().put("five point nine", 5.9f);
		t.getScalarScalarMap().put("one hundred", 100f);
		t.getScalarScalarMap().put("pi", 3.14f);

		t.setScalarElementMap(new HashMap<String, Item>());
		t.getScalarElementMap().put("item 1", new Item("first item"));
		t.getScalarElementMap().put("item 2", new Item("second item"));
		t.getScalarElementMap().put("item 3", new Item("last item"));

		System.out.println("Before saving to the database:");
		SimplTypesScope.serialize(t, System.out, Format.XML);
		System.out.println("\n");

		Session session = newSession();
		session.beginTransaction();
		for (Item item : t.getScalarElementMap().values())
			session.save(item);
		session.save(t);
		session.getTransaction().commit();
		session.close();

		session = newSession();
		List all = session.createCriteria(TestHibernateXmlMappingGeneratorWithMaps.class).list();
		System.out.println("Loaded from the database:");
		if (all != null)
			for (Object obj : all)
				SimplTypesScope.serialize(obj, System.out, Format.XML);
		System.out.println("\n");
		session.close();
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Map<String, Float> getScalarScalarMap()
	{
		return scalarScalarMap;
	}

	public void setScalarScalarMap(Map<String, Float> scalarScalarMap)
	{
		this.scalarScalarMap = scalarScalarMap;
	}

	public Map<String, Item> getScalarElementMap()
	{
		return scalarElementMap;
	}

	public void setScalarElementMap(Map<String, Item> scalarElementMap)
	{
		this.scalarElementMap = scalarElementMap;
	}

}

package ecologylab.bigsemantics.compiler.orm.scalartypes;

import java.util.ArrayList;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import ecologylab.bigsemantics.metadata.scalar.MetadataInteger;
import ecologylab.bigsemantics.metadata.scalar.MetadataString;
import ecologylab.bigsemantics.metadata.scalar.MetadataStringBuilder;

public class TestMetadataScalarHibernateTypes
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		MyEntity me = new MyEntity();
		me.setId(1);
		me.setNumber(new MetadataInteger(7));
		me.setName(new MetadataString("james bond"));
		me.setOtherNames(new ArrayList<MetadataStringBuilder>());
		me.getOtherNames().add(new MetadataStringBuilder(new StringBuilder("name_a")));
		me.getOtherNames().add(new MetadataStringBuilder(new StringBuilder("name_b")));
		me.getOtherNames().add(new MetadataStringBuilder(new StringBuilder("name_c")));
		
		Configuration config = new Configuration();
		SessionFactory	factory = config.configure("hibernate-test_scalar_types.cfg.xml").buildSessionFactory();
		
		Session session = factory.openSession();
		session.beginTransaction();
		session.save(me);
		session.getTransaction().commit();
		session.close();
		
		session = factory.openSession();
		MyEntity me2 = (MyEntity) session.get(MyEntity.class, 1);
		System.out.println(me2.getName().getValue());
		for (MetadataStringBuilder otherName : me2.getOtherNames())
			System.out.println(otherName.getValue().append(": a name").toString());
		session.close();
	}

}

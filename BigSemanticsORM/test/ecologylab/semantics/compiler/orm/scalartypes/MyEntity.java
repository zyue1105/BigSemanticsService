package ecologylab.semantics.compiler.orm.scalartypes;

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import ecologylab.semantics.metadata.scalar.MetadataInteger;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metadata.scalar.MetadataStringBuilder;

@Entity
@Table(name = "my_entity")
public class MyEntity
{

	@Id
	@Column
	private int													id;

	@Column(name = "name")
	@Type(type = "ecologylab.semantics.compiler.orm.scalartypes.MetadataStringHibernateType")
	private MetadataString							name;

	@Column(name = "number")
	@Type(type = "ecologylab.semantics.compiler.orm.scalartypes.MetadataIntegerHibernateType")
	private MetadataInteger							number;

	@ElementCollection
	@CollectionTable(name = "my_entity__other_names", joinColumns = @JoinColumn(name = "my_entity_id"))
	@Column(name = "other_name")
	@Type(type = "ecologylab.semantics.compiler.orm.scalartypes.MetadataStringBuilderHibernateType")
	private List<MetadataStringBuilder>	otherNames;

	public MyEntity()
	{

	}

	public void setId(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		return id;
	}

	public void setName(MetadataString name)
	{
		this.name = name;
	}

	public MetadataString getName()
	{
		return name;
	}

	public void setNumber(MetadataInteger number)
	{
		this.number = number;
	}

	public MetadataInteger getNumber()
	{
		return number;
	}

	public void setOtherNames(List<MetadataStringBuilder> otherNames)
	{
		this.otherNames = otherNames;
	}

	public List<MetadataStringBuilder> getOtherNames()
	{
		return otherNames;
	}

}

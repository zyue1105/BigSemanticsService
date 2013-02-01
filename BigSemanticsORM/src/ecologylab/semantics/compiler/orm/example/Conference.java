package ecologylab.semantics.compiler.orm.example;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.DiscriminatorOptions;

import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

@Entity
@Table(name = "conference")
@Cacheable
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("Conference")
@DiscriminatorOptions(force = true)
// @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE) // hibernate specific
@simpl_inherit
public class Conference extends BaseEntity
{

	@Column(name = "name")
	@simpl_scalar
	private String	name;

	@Column(name = "year")
	@simpl_scalar
	private int			year;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getYear()
	{
		return year;
	}

	public void setYear(int year)
	{
		this.year = year;
	}

}

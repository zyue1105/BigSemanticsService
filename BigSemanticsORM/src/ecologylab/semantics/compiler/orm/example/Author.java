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
@Table(name = "author")
@Cacheable
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("Author")
@DiscriminatorOptions(force = true)
// @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE) // hibernate specific
@simpl_inherit
public class Author extends BaseEntity
{

	@Column(name = "first_name")
	@simpl_scalar
	private String	firstName;

	@Column(name = "last_name")
	@simpl_scalar
	private String	lastName;

	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

}

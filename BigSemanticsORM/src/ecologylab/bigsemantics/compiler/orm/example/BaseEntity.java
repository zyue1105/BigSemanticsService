package ecologylab.bigsemantics.compiler.orm.example;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import org.hibernate.annotations.DiscriminatorOptions;

import ecologylab.serialization.ElementState;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

@Entity
@Table(name = "base_entity")
@Cacheable
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type_discriminator", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("BaseEntity")
@DiscriminatorOptions(force = true)
// @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE) // hibernate specific
@simpl_inherit
public class BaseEntity extends ElementState
{

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@simpl_scalar
	private int	id;
	
	@Column(name="type_discriminator")
	private String typeDiscriminator;
	
	public BaseEntity()
	{
		this.typeDiscriminator = this.getClassSimpleName();
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		return id;
	}

}

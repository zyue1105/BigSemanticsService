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
@Table(name = "acm_paper")
@Cacheable
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("AcmPaper")
@DiscriminatorOptions(force = true)
// @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE) // hibernate specific
@simpl_inherit
public class AcmPaper extends Paper
{

	@Column(name = "acm_id")
	@simpl_scalar
	private String	acmId;

	public void setAcmId(String acmId)
	{
		this.acmId = acmId;
	}

	public String getAcmId()
	{
		return acmId;
	}

}

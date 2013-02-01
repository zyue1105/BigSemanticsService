package ecologylab.bigsemantics.compiler.orm.example;

import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.DiscriminatorOptions;

import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_inherit;

@Entity
@Table(name = "paper")
@Cacheable
@PrimaryKeyJoinColumn(name = "id")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type_discriminator", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("Paper")
@DiscriminatorOptions(force = true)
// @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE) // hibernate specific
@simpl_inherit
public class Paper extends Article
{

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "conference_id")
	@simpl_composite
	private Conference		conference;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "paper_keywords", joinColumns = @JoinColumn(name = "paper_id"))
	@Column(name = "keyword")
	@simpl_collection("keyword")
	private List<String>	keywords;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "paper_references",
			joinColumns = @JoinColumn(name = "paper_id"),
			inverseJoinColumns = @JoinColumn(name = "reference_id"))
	@simpl_collection("reference")
	private List<Paper>		references;

	public Conference getConference()
	{
		return conference;
	}

	public void setConference(Conference conference)
	{
		this.conference = conference;
	}

	public List<String> getKeywords()
	{
		return keywords;
	}

	public void setKeywords(List<String> keywords)
	{
		this.keywords = keywords;
	}

	public List<Paper> getReferences()
	{
		return references;
	}

	public void setReferences(List<Paper> references)
	{
		this.references = references;
	}

}

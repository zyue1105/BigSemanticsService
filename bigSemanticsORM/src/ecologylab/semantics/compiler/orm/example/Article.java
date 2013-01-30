package ecologylab.semantics.compiler.orm.example;

import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.DiscriminatorOptions;

import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

@Entity
@Table(name = "article")
@Cacheable
@PrimaryKeyJoinColumn(name = "id")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type_discriminator", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("Article")
@DiscriminatorOptions(force = true)
// @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE) // hibernate specific
@simpl_inherit
public class Article extends BaseEntity
{

	@Column(name = "title")
	@simpl_scalar
	private String				title;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "article_authors",
			joinColumns = @JoinColumn(name = "article_id"),
			inverseJoinColumns = @JoinColumn(name = "author_id"))
	@simpl_collection("author")
	private List<Author>	authors;

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public List<Author> getAuthors()
	{
		return authors;
	}

	public void setAuthors(List<Author> authors)
	{
		this.authors = authors;
	}

}

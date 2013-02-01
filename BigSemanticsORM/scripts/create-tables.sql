-- Table: base_entity

-- DROP TABLE base_entity

CREATE TABLE base_entity
(
	id bigserial NOT NULL,
	type_discriminator character varying,
	CONSTRAINT base_entity_pkey PRIMARY KEY (id)
)
WITH (
	OIDS=FALSE
);
ALTER TABLE base_entity OWNER TO postgres;

-- Table: author

-- DROP TABLE author;

CREATE TABLE author
(
  id bigint NOT NULL,
  first_name character varying,
  last_name character varying,
  CONSTRAINT author_pkey PRIMARY KEY (id),
  CONSTRAINT author_subclass_fkey FOREIGN KEY (id)
      REFERENCES base_entity (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE author OWNER TO postgres;

-- Index: author_last_name_index

-- DROP INDEX author_last_name_index;

CREATE INDEX author_last_name_index
  ON author
  USING btree
  (last_name);

-- Table: article

-- DROP TABLE article;

CREATE TABLE article
(
  id bigint NOT NULL,
  title character varying,
  CONSTRAINT article_pkey PRIMARY KEY (id),
  CONSTRAINT article_subclass_fkey FOREIGN KEY (id)
      REFERENCES base_entity (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE article OWNER TO postgres;

-- Index: article_title_index

-- DROP INDEX article_title_index;

CREATE INDEX article_title_index
  ON article
  USING btree
  (title);

-- Table: article_authors

-- DROP TABLE article_authors;

CREATE TABLE article_authors
(
  id bigserial NOT NULL,
  article_id bigint NOT NULL,
  author_id bigint NOT NULL,
  CONSTRAINT article_authors_pkey PRIMARY KEY (id),
  CONSTRAINT article_authors_article_fkey FOREIGN KEY (article_id)
      REFERENCES article (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT article_authors_author_fkey FOREIGN KEY (author_id)
      REFERENCES author (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE article_authors OWNER TO postgres;

-- Table: conference

-- DROP TABLE conference;

CREATE TABLE conference
(
  id bigint NOT NULL,
  "name" character varying,
  "year" integer,
  CONSTRAINT conference_pkey PRIMARY KEY (id),
  CONSTRAINT conference_subclass_fkey FOREIGN KEY (id)
      REFERENCES base_entity (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE conference OWNER TO postgres;

-- Index: conference_name_index

-- DROP INDEX conference_name_index;

CREATE INDEX conference_name_index
  ON conference
  USING btree
  (name);

-- Index: conference_year_index

-- DROP INDEX conference_year_index;

CREATE INDEX conference_year_index
  ON conference
  USING btree
  (year);

-- Table: paper

-- DROP TABLE paper;

CREATE TABLE paper
(
  id bigint NOT NULL,
  conference_id bigint,
  CONSTRAINT paper_pkey PRIMARY KEY (id),
  CONSTRAINT paper_subclass_fkey FOREIGN KEY (id)
      REFERENCES article (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT paper_conference_id_fkey FOREIGN KEY (conference_id)
      REFERENCES conference (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE paper OWNER TO postgres;

-- Table: paper_keywords

-- DROP TABLE paper_keywords;

CREATE TABLE paper_keywords
(
  id bigserial NOT NULL,
  paper_id bigint NOT NULL,
  keyword character varying NOT NULL,
  CONSTRAINT paper_keywords_pkey PRIMARY KEY (id),
  CONSTRAINT paper_keywords_paper_id_fkey FOREIGN KEY (paper_id)
      REFERENCES paper (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE paper_keywords OWNER TO postgres;

-- Index: paper_keywords_keyword_index

-- DROP INDEX paper_keywords_keyword_index;

CREATE INDEX paper_keywords_keyword_index
  ON paper_keywords
  USING btree
  (keyword);

-- Table: paper_references

-- DROP TABLE paper_references;

CREATE TABLE paper_references
(
  id bigserial NOT NULL,
  paper_id bigint NOT NULL,
  reference_id bigint NOT NULL,
  CONSTRAINT paper_references_pkey PRIMARY KEY (id),
  CONSTRAINT paper_references_paper_id_fkey FOREIGN KEY (paper_id)
      REFERENCES paper (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT paper_references_reference_id_fkey FOREIGN KEY (reference_id)
      REFERENCES paper (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE paper_references OWNER TO postgres;

-- Table: acm_paper

-- DROP TABLE acm_paper;

CREATE TABLE acm_paper
(
  id bigint NOT NULL,
  acm_id character varying,
  CONSTRAINT acm_paper_pkey PRIMARY KEY (id),
  CONSTRAINT acm_paper_subclass_fkey FOREIGN KEY (id)
      REFERENCES paper (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE acm_paper OWNER TO postgres;

-- Index: acm_paper_acm_id_index

-- DROP INDEX acm_paper_acm_id_index;

CREATE INDEX acm_paper_acm_id_index
  ON acm_paper
  USING btree
  (acm_id);



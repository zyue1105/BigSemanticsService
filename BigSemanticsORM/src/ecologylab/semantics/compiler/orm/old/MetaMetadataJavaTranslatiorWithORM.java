package ecologylab.semantics.compiler.orm.old;

import java.io.IOException;

import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import ecologylab.semantics.compiler.MetaMetadataJavaTranslator;
import ecologylab.semantics.compiler.orm.scalartypes.HibernateUserTypeRegistry;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.FieldTypes;
import ecologylab.translators.hibernate.DbNameGenerator;
import ecologylab.translators.hibernate.DefaultCachedDbNameGenerator;
import ecologylab.translators.java.JavaTranslationConstants;

public class MetaMetadataJavaTranslatiorWithORM extends MetaMetadataJavaTranslator
		implements JavaTranslationConstants
{
	
//	public static Class<? extends DbNameGenerator>	DB_NAME_GENERATOR_CLASS		= DefaultCachedDbNameGenerator.class;
//
//	public static final String											ROOT_CLASS_NAME						= "ecologylab.semantics.metadata.Metadata";
//
//	public static final String											PKEY_FIELD_NAME						= "ormId";
//
//	public static final String											DISCRIMINATOR_COLUMN_NAME	= "type_discriminator";
//
//	private DbNameGenerator													dbNameGenerator;
//
//	private String																	pkeyColumnName;
//
//	public MetaMetadataJavaTranslatiorWithORM()
//	{
//		super();
//		
//		try
//		{
//			dbNameGenerator = DB_NAME_GENERATOR_CLASS.newInstance();
//		}
//		catch (InstantiationException e)
//		{
//			e.printStackTrace();
//		}
//		catch (IllegalAccessException e)
//		{
//			e.printStackTrace();
//		}
//		pkeyColumnName = dbNameGenerator.getColumnName(PKEY_FIELD_NAME);
//	}
//	
//	protected void appendConstructorHook(String className, Appendable appendable) throws IOException
//	{
//		super.appendConstructorHook(className, appendable);
//		
////		appendAnnotationAndAddDependency(appendable, TAB, Id.class, null);
////		appendAnnotationAndAddDependency(appendable, TAB, Column.class, "name = \"" + pkeyColumnName + "\"");
////		appendAnnotationAndAddDependency(appendable, TAB, GeneratedValue.class, "strategy = GenerationType.IDENTITY", GenerationType.class);
////		appendAnnotationAndAddDependency(appendable, TAB, Override.class, null);
////		appendable.append(TAB).append("public long getOrmId()\n").append(TAB).append("{\n");
////		appendable.append(TAB).append(TAB).append("return super.getOrmId();\n").append(TAB).append("}\n");
//	}
//	
//	private String appendAnnotationAndAddDependency(Appendable appendable, String spacing, Class annotationClass, String annotationArgs, Class... otherDependencies) throws IOException
//	{
//		StringBuilder sb = StringBuilderUtils.acquire();
//		
//		sb.append(ANNOTATION_PREFIX);
//		sb.append(annotationClass.getSimpleName());
//		if (annotationArgs != null)
//			sb.append('(').append(annotationArgs).append(')');
//		String result = sb.toString();
//		
//		StringBuilderUtils.release(sb);
//		
//		if (appendable != null)
//		{
//			appendable.append(spacing);
//			appendable.append(result);
//			appendable.append(SINGLE_LINE_BREAK);
//		}
//		
//		addAnnotationDependency(annotationClass);
//		if (otherDependencies != null)
//			for (Class<?> clazz : otherDependencies)
//				addDependency(clazz.getName());
//					
//		return result;
//	}
//
//	@Override
//	protected void appendFieldMetaInformationHook(ClassDescriptor contextCd, FieldDescriptor fieldDesc, Appendable appendable) throws IOException
//	{
//		super.appendFieldMetaInformationHook(contextCd, fieldDesc, appendable);
//		
//		String columnName = this.dbNameGenerator.getColumnName(fieldDesc);
//		String joinedTableName = this.dbNameGenerator.getAssociationTableName(contextCd, fieldDesc);
//		String metadataScalarTypeName = fieldDesc.getScalarType() == null ? null : fieldDesc.getScalarType().getJavaTypeName();
//		Class hibernateAccessor = metadataScalarTypeName == null ? null : HibernateUserTypeRegistry.getHibernateType(metadataScalarTypeName);
//		String scalarTypeName = hibernateAccessor == null ? null : hibernateAccessor.getName();
//		
//		int fieldType = fieldDesc.getType();
//		switch (fieldType)
//		{
//		case FieldTypes.SCALAR:
//			appendAnnotationAndAddDependency(appendable, spacing, Column.class, "name = \"" + columnName + "\"");
//			assert scalarTypeName != null;
//			appendAnnotationAndAddDependency(appendable, spacing, Type.class, "type = \"" + scalarTypeName + "\"");
//			// this 'if' is actually never used, since we don't generate code for Metadata
//			if (fieldDesc.getName().equals(PKEY_FIELD_NAME))
//			{
//				appendAnnotationAndAddDependency(appendable, spacing, Id.class, null);
//				appendAnnotationAndAddDependency(appendable, spacing, GeneratedValue.class, "strategy = GenerationType.IDENTITY", GenerationType.class);
//			}
//			break;
//		case FieldTypes.COMPOSITE_ELEMENT:
//			String compositeJoinColumnName = this.dbNameGenerator.getAssociationTableColumnName(fieldDesc.getElementClassDescriptor());
//			appendAnnotationAndAddDependency(appendable, spacing, ManyToOne.class, "fetch = FetchType.LAZY", FetchType.class);
//			appendAnnotationAndAddDependency(appendable, spacing, JoinColumn.class, "name = \"" + compositeJoinColumnName + "\"", FetchType.class);
//			break;
//		case FieldTypes.COLLECTION_ELEMENT:
//			appendAnnotationAndAddDependency(appendable, spacing, ManyToMany.class, "fetch = FetchType.LAZY", FetchType.class);
//			String collectionJoinColumnName = this.dbNameGenerator.getAssociationTableColumnName(contextCd);
//			String collectionInverseJoinColumnName = this.dbNameGenerator.getAssociationTableColumnName(fieldDesc.getElementClassDescriptor());
//			String argJoinColumns = appendAnnotationAndAddDependency(null, null, JoinColumn.class, "name = \"" + collectionJoinColumnName + "\"");
//			String argInverseJoinColumns = appendAnnotationAndAddDependency(null, null, JoinColumn.class, "name = \"" + collectionInverseJoinColumnName + "\"");
//			String joinTableArgs = "name = \"" + joinedTableName + "\", joinColumns = " + argJoinColumns + ", inverseJoinColumns = " + argInverseJoinColumns;
//			appendAnnotationAndAddDependency(appendable, spacing, JoinTable.class, joinTableArgs);
//			break;
//		case FieldTypes.COLLECTION_SCALAR:
//			appendAnnotationAndAddDependency(appendable, spacing, ElementCollection.class, "fetch = FetchType.LAZY", FetchType.class);
//			String elementTableJoinColumnName = this.dbNameGenerator.getAssociationTableColumnName(contextCd);
//			String collectionTableArgs = "name = \"" + joinedTableName + "\", joinColumns = " + appendAnnotationAndAddDependency(null, null, JoinColumn.class, "name = \"" + elementTableJoinColumnName + "\"");
//			appendAnnotationAndAddDependency(appendable, spacing, CollectionTable.class, collectionTableArgs);
//			appendAnnotationAndAddDependency(appendable, spacing, Column.class, "name = \"" + columnName + "\"");
//			assert scalarTypeName != null;
//			appendAnnotationAndAddDependency(appendable, spacing, Type.class, "type = \"" + scalarTypeName + "\"");
//			break;
//		}
//	}
//
//	@Override
//	protected void appendClassMetaInformationHook(ClassDescriptor classDesc, Appendable appendable) throws IOException
//	{
//		super.appendClassMetaInformationHook(classDesc, appendable);
//		
//		String tableName = dbNameGenerator.getTableName(classDesc);
//		String discriminatorValue = classDesc.getDescribedClassName();
//
//		appendAnnotationAndAddDependency(appendable, spacing, Entity.class, null);
//		appendAnnotationAndAddDependency(appendable, spacing, Table.class, "name = \"" + tableName + "\"");
//		appendAnnotationAndAddDependency(appendable, spacing, Cacheable.class, null);
//		appendAnnotationAndAddDependency(appendable, spacing, PrimaryKeyJoinColumn.class, "name = \"" + pkeyColumnName + "\"");
//		appendAnnotationAndAddDependency(appendable, spacing, Inheritance.class, "strategy = InheritanceType.JOINED", InheritanceType.class);
//		appendAnnotationAndAddDependency(appendable, spacing, DiscriminatorValue.class, "\"" + discriminatorValue + "\"");
//		
//		if (classDesc.getDescribedClassName().equals(ROOT_CLASS_NAME))
//			appendAnnotationAndAddDependency(appendable, spacing, DiscriminatorColumn.class, "name = \"" + DISCRIMINATOR_COLUMN_NAME + "\", discriminatorType = DiscriminatorType.STRING", DiscriminatorType.class);
//	}
	
}

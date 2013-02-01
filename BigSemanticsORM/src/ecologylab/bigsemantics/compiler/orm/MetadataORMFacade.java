package ecologylab.bigsemantics.compiler.orm;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;

import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.collection.PersistentList;
import org.hibernate.collection.PersistentMap;
import org.hibernate.criterion.Property;
import org.hibernate.proxy.pojo.javassist.JavassistLazyInitializer;

import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.bigsemantics.generated.library.creative_work.CreativeWork;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.scalar.MetadataParsedURL;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepository;
import ecologylab.generic.ResourcePool;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.DeserializationHookStrategy;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.FieldType;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.TranslationContext;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.deserializers.ISimplDeserializationPost;
import ecologylab.serialization.deserializers.ISimplDeserializationPre;
import ecologylab.serialization.formatenums.StringFormat;
import ecologylab.serialization.types.ScalarType;

/**
 * This class provides a set of primitives that can be used for supporting a database backend for
 * meta-metadata.
 * <p />
 * Note that although most of its methods use ElementState as the type, it is specifically designed
 * for meta-metadata only, at least for now. To support general SIMPL objects with database
 * persistence, you need to handle sessions manually.
 * <p />
 * TODO a good API (or a set of primitives).
 * 
 * @author quyin
 * 
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@simpl_inherit
public class MetadataORMFacade extends ElementState
{

	@simpl_scalar
	private String					hibernateConfigResourceName;

	private SessionFactory	factory;

	private ResourcePool<MetadataParsedURL>	metadataPURLPool	= new ResourcePool<MetadataParsedURL>(4, 4)
	{
		@Override
		protected MetadataParsedURL generateNewResource()
		{
			return new MetadataParsedURL();
		}
	
		@Override
		protected void clean(
				MetadataParsedURL objectToClean)
		{
			objectToClean.setValue(null);
		}
	};

	public MetadataORMFacade()
	{
	}

	public MetadataORMFacade(String hibernateConfigResourceName)
	{
		this.hibernateConfigResourceName = hibernateConfigResourceName;
		factory = new Configuration().configure(hibernateConfigResourceName).buildSessionFactory();
	}

	/**
	 * get a new hibernate session.
	 * 
	 * @return
	 */
	public synchronized Session newSession()
	{
		Session session = factory.openSession();
		session.setFlushMode(FlushMode.COMMIT);
		return session;
	}

	/**
	 * if we directly save a transient metadata object, there will be exceptions for composite /
	 * collection elements, since they are transient entities (objects of user-defined, mapped
	 * classes) that need to be saved within the same transaction. thus, we need this method to
	 * recursively save all entities into the session.
	 * 
	 * @param session
	 * @param es
	 */
	public void recursivelySave(Session session, Object es)
	{
		Transaction tx = session.beginTransaction();
		Map<Object, Object> bookKeeper = new HashMap<Object, Object>();
		recursivelySaveHelper(session, es, null, bookKeeper);
		tx.commit();
	}

	/**
	 * this method actually does recursive saving. the caller wraps everything in a database
	 * transaction.
	 * 
	 * @param session
	 * @param state
	 * @param cached
	 * @param bookKeeper
	 */
	private Object recursivelySaveHelper(Session session, Object state, Object cached, Map<Object, Object> bookKeeper)
	{
		if (session.getSessionFactory().getClassMetadata(state.getClass()) == null)
			return state; // this class is not mapped, we don't care about it.
		
		if (bookKeeper.containsKey(state))
			return bookKeeper.get(state); // in processing this object
		
		// try to find cached
		if (cached == null)
		{
			if (session.contains(state))
				cached = state;
		}
		if (cached == null)
		{
			if (state instanceof Document)
			{
				Document doc = (Document) state;
				ParsedURL location = doc.getLocation();
				List<Document> existingDocs = lookupDocumentByLocation(session, location, doc.getClass());
				cached = selectOneFromExistingDocuments(existingDocs);
			}
		}
		if (cached == null)
		{
			session.save(state);
			cached = state;
		}
		
		if (cached != null)
		{
			bookKeeper.put(state, cached);
			if (state == cached)
				saveNew(session, state, bookKeeper);
			else
				mergeOld(session, state, cached, bookKeeper);
			bookKeeper.remove(state);
		}
		
		return cached;
	}

	private void saveNew(Session session, Object state, Map<Object, Object> bookKeeper)
	{
		ClassDescriptor<? extends FieldDescriptor> cd = ClassDescriptor.getClassDescriptor(state);
		for (FieldDescriptor fd : cd)
		{
			FieldType type = fd.getType();
			switch (type)
			{
			case COMPOSITE_ELEMENT:
				Object stateValue = fd.getNested(state);
				Object savedValue = null;
				if (stateValue != null)
					savedValue = recursivelySaveHelper(session, stateValue, null, bookKeeper);
				if (savedValue != stateValue)
					fd.setField(state, savedValue);
				break;
			case COLLECTION_ELEMENT:
				Collection stateCollection = fd.getCollection(state);
				Collection savedCollection = stateCollection == null ? null : fd.getCollectionType().getCollection();
				if (stateCollection != null && stateCollection.size() > 0)
				{
					savedCollection = fd.getCollectionType().getCollection();
					boolean useCachedCollection = false;
					for (Object element : stateCollection)
					{
						Object savedElement = recursivelySaveHelper(session, element, null, bookKeeper);
						if (savedElement != element)
							useCachedCollection = true;
						savedCollection.add(savedElement);
					}
					if (!useCachedCollection)
						savedCollection = stateCollection;
				}
				if (savedCollection != stateCollection)
					fd.setField(state, savedCollection);
				break;
			case MAP_ELEMENT:
				Map stateMap = fd.getMap(state);
				Map savedMap = stateMap == null ? null : fd.getCollectionType().getMap();
				if (stateMap != null && stateMap.size() > 0)
				{
					savedMap = fd.getCollectionType().getMap();
					boolean useCachedMap = false;
					for (Object key : stateMap.keySet())
					{
						Object element = stateMap.get(key);
						Object savedElement = recursivelySaveHelper(session, element, null, bookKeeper);
						if (savedElement != element)
							useCachedMap = true;
						savedMap.put(key, savedElement);
					}
					if (!useCachedMap)
						savedMap = stateMap;
				}
				if (savedMap != stateMap)
					fd.setField(state, savedMap);
				break;
			default:
				// for scalar, collection of scalars, or map of scalars, just keep the value
				break;
			}
		}
	}
	
	private void mergeOld(Session session, Object state, Object cached,
			Map<Object, Object> bookKeeper)
	{
		// FIXME handling null values: when the state has null values but the cached has non-null values,
		//       it is unclear that if we should overwrite non-null values with null values. perhaps a
		//       switch needs to be added to tune this behavior. currently we choose not to.
		//       another way is to allow deletion, so if you want to completely overwrite, you delete first.
		
		ClassDescriptor<? extends FieldDescriptor> cd = ClassDescriptor.getClassDescriptor(cached);
		for (FieldDescriptor fd : cd)
		{
			FieldType type = fd.getType();
			switch (type)
			{
			case COMPOSITE_ELEMENT:
				Object stateValue = fd.getNested(state);
				if (stateValue != null)
				{
					Object cachedValue = fd.getNested(cached);
					Object savedValue = recursivelySaveHelper(session, stateValue, cachedValue, bookKeeper);
					if (cachedValue != savedValue)
						fd.setField(cached, savedValue);
				}
				break;
			case COLLECTION_ELEMENT:
				Collection stateCollection = fd.getCollection(state);
				if (stateCollection != null && stateCollection.size() > 0)
				{
					Collection cachedCollection = fd.getCollection(cached);
					Collection savedCollection = fd.getCollectionType().getCollection();
					boolean useSavedCollection = false;
					Iterator it1 = stateCollection.iterator();
					Iterator it2 = cachedCollection == null ? null : cachedCollection.iterator();
					while (it1.hasNext())
					{
						Object stateElement = it1.next();
						Object cachedElement = it2 == null ? null : it2.hasNext() ? it2.next() : null;
						Object savedElement = recursivelySaveHelper(session, stateElement, cachedElement, bookKeeper);
						savedCollection.add(savedElement);
						if (savedElement != cachedElement)
							useSavedCollection = true;
					}
					if (useSavedCollection)
						fd.setField(cached, savedCollection);
				}
				break;
			case MAP_ELEMENT:
				Map stateMap = fd.getMap(state);
				if (stateMap != null || stateMap.size() > 0)
				{
					Map cachedMap = fd.getMap(cached);
					Map savedMap = fd.getCollectionType().getMap();
					boolean useSavedMap = false;
					for (Object key : stateMap.keySet())
					{
						Object stateElement = stateMap.get(key);
						Object cachedElement = cachedMap.get(key);
						Object savedElement = recursivelySaveHelper(session, stateElement, cachedElement, bookKeeper);
						savedMap.put(key, savedElement);
						if (savedElement != cachedElement)
							useSavedMap = true;
					}
					if (useSavedMap)
						fd.setField(cached, savedMap);
				}
				break;
			default:
				// for scalar, collection of scalars, or map of scalars, just copy the value
				Object value = fd.getValue(state);
				if (fd.getType() == FieldType.SCALAR)
				{
					ScalarType scalarType = fd.getScalarType();
					if (value != null && scalarType != null && !scalarType.isDefaultValue(value.toString()))
							fd.setField(cached, value);
				}
				else
				{
					if (value != null)
						fd.setField(cached, value);
				}
				break;
			}
		}
	}
	
	/**
	 * 
	 * @param existingDocs
	 * @return
	 */
	protected Document selectOneFromExistingDocuments(List<Document> existingDocs)
	{
		return existingDocs.size() > 0 ? existingDocs.get(0) : null;
	}

	/**
	 * this method materializes a mapped ElementState object, i.e. to retrieve true objects from proxy
	 * objects, and execute deserialization hooks (in case that there are some initialization
	 * procedures). NOTE that the session associated with this object must remain alive when this
	 * method is called.
	 * <p />
	 * before the session is closed, the materialized object can still be modified and saved to the
	 * database.
	 * 
	 * @param es
	 * 					the ElementState object that needs to be materialized.
	 * @param translationContext
	 *          the TranslationContext that needs to be used. usually this cannot be null. use pooling
	 *          for performance, see the TranslationContextPool class.
	 * @param deserializationHookStrategy
	 *          this parameter provides an opportunity for calling deserialization hooks in a
	 *          TranslationContext. this could be null if you don't need to use one.
	 */
	public void materialize(Object es, TranslationContext translationContext,
			DeserializationHookStrategy deserializationHookStrategy)
	{
		Set<Object> visited = new HashSet<Object>();
		materializeHelper(es, null, translationContext, deserializationHookStrategy, visited);
	}

	/**
	 * the helper method for materialize().
	 * 
	 * @param es
	 * @param fieldDescriptor
	 * @param translationContext
	 * @param deserializationHookStrategy
	 * @param visited
	 */
	private void materializeHelper(Object es, FieldDescriptor fieldDescriptor,
			TranslationContext translationContext,
			DeserializationHookStrategy deserializationHookStrategy, Set<Object> visited)
	{
		if (es != null)
		{
			if (visited.contains(es))
				return;
			visited.add(es);
			
			if (deserializationHookStrategy != null)
				deserializationHookStrategy.deserializationPreHook(es, fieldDescriptor);
			if (es instanceof ISimplDeserializationPre)
				((ISimplDeserializationPre) es).deserializationPreHook(translationContext);
			
			ClassDescriptor classDescriptor = ClassDescriptor.getClassDescriptor(es);
			// attributes should be primitives and do not need to be materialized
			ArrayList<FieldDescriptor> elementFieldDescriptors = classDescriptor.elementFieldDescriptors();
			for (FieldDescriptor fd : elementFieldDescriptors)
			{
				switch (fd.getType())
				{
				case COMPOSITE_ELEMENT:
					Object value = fd.getValue(es);
					value = getTrueValueFromProxy(value);
					if (value != null)
					{
						fd.setField(es, value);
						materializeHelper(value, fd, translationContext, deserializationHookStrategy, visited);
					}
					break;
				case COLLECTION_ELEMENT:
				case COLLECTION_SCALAR:
					value = fd.getValue(es);
					if (value != null && value instanceof PersistentList)
					{
						Collection newCollection = fd.getCollectionType().getCollection();
						for (Object element : (PersistentList) value)
						{
							element = getTrueValueFromProxy(element);
							newCollection.add(element);
							if (element != null)
							{
								materializeHelper(element, fd, translationContext, deserializationHookStrategy, visited);
							}
						}
						fd.setField(es, newCollection);
					}
					break;
				case MAP_ELEMENT:
				case MAP_SCALAR:
					value = fd.getValue(es);
					if (value != null && value instanceof PersistentMap)
					{
						Map map = (PersistentMap) value;
						Map newMap = fd.getCollectionType().getMap();
						for (Object key : map.keySet())
						{
							Object element = map.get(key);
							element = getTrueValueFromProxy(element);
							newMap.put(key, element);
							if (element != null)
							{
								materializeHelper(element, fd, translationContext, deserializationHookStrategy, visited);
							}
						}
						fd.setField(es, newMap);
					}
					break;
				default:
					break;
				}
			}
			
			if (es instanceof ISimplDeserializationPost)
				((ISimplDeserializationPost) es).deserializationPostHook(translationContext, es);
			if (deserializationHookStrategy != null)
				deserializationHookStrategy.deserializationPostHook(es, fieldDescriptor);
		}
	}
	
	/**
	 * look up documents by location. it returns a list of documents with the specified URL.
	 * <p />
	 * note that although a URL is supposed to uniquely locate a single document, in the metadata
	 * system there could be one URL pointing to different documents, at least for now (e.g. a search
	 * result and the actual document). this behavior might change in the future.
	 * 
	 * @param session
	 * @param location
	 * @return
	 */
	public List<Document> lookupDocumentByLocation(Session session, ParsedURL location, Class<? extends Document> filteringClazz)
	{
		List<Document> results = new ArrayList<Document>();
		if (location == null)
			return results;
		
		MetadataParsedURL mdLocation = metadataPURLPool.acquire();
		mdLocation.setValue(location);
		try
		{
			// first, look up by location
			Criteria q = session.createCriteria(filteringClazz).add(Property.forName("location").eq(mdLocation));
			List existingDocuments = q.list();
			results.addAll(existingDocuments);
			
			// then look up by additional locations
			SQLQuery query = session.createSQLQuery("SELECT document_id FROM document__additional_locations WHERE value=?");
			query.setString(0, location.toString());
			List docIds = query.list();
			if (docIds != null && !docIds.isEmpty())
			{
				Set docIdSet = new HashSet(docIds);
				for (Object docIdObj : docIdSet)
				{
					long docId = 0;
					if (docIdObj instanceof Long)
						docId = (Long) docIdObj;
					else if (docIdObj instanceof BigInteger)
						docId = ((BigInteger) docIdObj).longValue();
					Document result = (Document) session.get(Document.class, docId);
					if (result != null && filteringClazz.isAssignableFrom(result.getClass()))
						results.add(result);
				}
			}
			
			debug(results.size() + " document(s) found for URL " + location);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			metadataPURLPool.release(mdLocation);
		}
		return results;
	}
	
	/**
	 * look up Document objects by location, no filtering.
	 * 
	 * @param session
	 * @param location
	 * @return
	 */
	public List<Document> lookupDocumentByLocation(Session session, ParsedURL location)
	{
		return lookupDocumentByLocation(session, location, Document.class);
	}
	
	/**
	 * utility method to extract true object from proxy object.
	 * 
	 * @param proxy
	 * @return
	 */
	public static Object getTrueValueFromProxy(Object proxy)
	{
		Object value = proxy;
		if (value != null && value instanceof ProxyObject)
		{
			MethodHandler handler = ((ProxyObject) value).getHandler();
			if (handler instanceof JavassistLazyInitializer)
			{
				value = ((JavassistLazyInitializer) handler).getImplementation();
				if (value == null)
					warning(MetadataORMFacade.class, "NULL implementation!");
			}
		}
		return value;
	}


	private static MetadataORMFacade	defaultSingleton	= null;

	/**
	 * get a facade object using default mapping resource name, and set it to default.
	 * 
	 * @return
	 */
	public static MetadataORMFacade defaultSingleton()
	{
		return defaultSingleton("hibernate-allmmd.cfg.xml");
	}

	/**
	 * get a facade object using specified mapping resource name, and set it to default.
	 * <p />
	 * note that once the default facade object is created, it will just return that object for
	 * next calls to this method, no matter what resource name you provide. use constructor if
	 * you need different mapping resources, or resetDefaultSingleton() if you need a different
	 * default one.
	 * 
	 * @param mappingResourceName
	 * @return
	 */
	public static MetadataORMFacade defaultSingleton(String mappingResourceName)
	{
		if (defaultSingleton == null)
		{
			defaultSingleton = new MetadataORMFacade(mappingResourceName);
		}
		return defaultSingleton;
	}
	
	/**
	 * reset the default singleton so you can create a new one.
	 */
	public static void resetDefaultSingleton()
	{
		defaultSingleton = null;
	}
	
	public static void main(String[] args) throws SIMPLTranslationException
	{
		MetaMetadataRepository.initializeTypes();
		SemanticsSessionScope sss = new SemanticsSessionScope(RepositoryMetadataTranslationScope.get(), CybernekoWrapper.class);
		Session session = defaultSingleton("hibernate-allmmd.cfg.xml").newSession();
		List<Document> docs = defaultSingleton("hibernate-allmmd.cfg.xml").lookupDocumentByLocation(session, ParsedURL.getAbsolute("http://portal.acm.org/citation.cfm?id=1416955"));
		for (Document doc : docs)
			if (doc instanceof CreativeWork)
				SimplTypesScope.serialize(doc, System.out, StringFormat.XML);
		session.close();
	}

}

package info.archinnov.achilles.proxy.interceptor;

import info.archinnov.achilles.entity.context.AchillesPersistenceContext;
import info.archinnov.achilles.entity.context.ThriftPersistenceContext;
import info.archinnov.achilles.entity.metadata.EntityMeta;
import info.archinnov.achilles.entity.metadata.PropertyMeta;
import info.archinnov.achilles.validation.Validator;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JpaEntityInterceptorBuilder
 * 
 * @author DuyHai DOAN
 * 
 */
public class JpaEntityInterceptorBuilder<ID, T>
{
	private static final Logger log = LoggerFactory.getLogger(JpaEntityInterceptorBuilder.class);

	private T target;
	private Set<Method> lazyLoaded = new HashSet<Method>();
	private ThriftPersistenceContext<ID> context;

	public static <ID, T> JpaEntityInterceptorBuilder<ID, T> builder(
			AchillesPersistenceContext<ID> context, T entity)
	{
		return new JpaEntityInterceptorBuilder<ID, T>(context, entity);
	}

	public JpaEntityInterceptorBuilder(AchillesPersistenceContext<ID> context, T entity) {
		Validator.validateNotNull(context, "PersistenceContext for interceptor should not be null");
		Validator.validateNotNull(entity, "Target entity for interceptor should not be null");
		this.context = (ThriftPersistenceContext<ID>) context;
		this.target = entity;
	}

	public JpaEntityInterceptorBuilder<ID, T> lazyLoaded(Set<Method> lazyLoaded)
	{
		this.lazyLoaded = lazyLoaded;
		return this;
	}

	public JpaEntityInterceptor<ID, T> build()
	{
		log.debug("Build interceptor for entity of class {}", context.getEntityMeta()
				.getClassName());

		JpaEntityInterceptor<ID, T> interceptor = new JpaEntityInterceptor<ID, T>();

		EntityMeta<ID> entityMeta = context.getEntityMeta();

		Validator.validateNotNull(this.target, "Target object for interceptor of '"
				+ context.getEntityClass().getCanonicalName() + "' should not be null");
		Validator.validateNotNull(entityMeta.getGetterMetas(),
				"Getters metadata for interceptor of '"
						+ context.getEntityClass().getCanonicalName() + "' should not be null");
		Validator.validateNotNull(entityMeta.getSetterMetas(),
				"Setters metadata for interceptor of '"
						+ context.getEntityClass().getCanonicalName() + "'should not be null");
		if (entityMeta.isWideRow())
		{
			Validator.validateNotNull(context.getColumnFamilyDao(), "Column Family Dao for '"
					+ context.getEntityClass().getCanonicalName() + "' should not be null");
		}
		else
		{
			Validator.validateNotNull(context.getEntityDao(), "Entity dao for '"
					+ context.getEntityClass().getCanonicalName() + "' should not be null");
		}
		Validator.validateNotNull(entityMeta.getIdMeta(), "Id metadata for '"
				+ context.getEntityClass().getCanonicalName() + "' should not be null");

		interceptor.setTarget(target);
		interceptor.setContext(context);
		interceptor.setGetterMetas(entityMeta.getGetterMetas());
		interceptor.setSetterMetas(entityMeta.getSetterMetas());
		interceptor.setIdGetter(entityMeta.getIdMeta().getGetter());
		interceptor.setIdSetter(entityMeta.getIdMeta().getSetter());

		if (this.lazyLoaded == null)
		{
			this.lazyLoaded = new HashSet<Method>();
		}
		interceptor.setLazyLoaded(this.lazyLoaded);
		interceptor.setDirtyMap(new HashMap<Method, PropertyMeta<?, ?>>());
		interceptor.setKey(context.getPrimaryKey());

		return interceptor;
	}
}

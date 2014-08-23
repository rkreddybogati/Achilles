/*
 * Copyright (C) 2012-2014 DuyHai DOAN
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package info.archinnov.achilles.persistence;

import com.datastax.driver.core.RegularStatement;
import com.google.common.base.Optional;
import info.archinnov.achilles.exception.AchillesException;
import info.archinnov.achilles.internal.context.BatchingFlushContext;
import info.archinnov.achilles.internal.context.ConfigurationContext;
import info.archinnov.achilles.internal.context.DaoContext;
import info.archinnov.achilles.internal.context.PersistenceContextFactory;
import info.archinnov.achilles.internal.context.facade.PersistenceManagerOperations;
import info.archinnov.achilles.internal.metadata.holder.EntityMeta;
import info.archinnov.achilles.internal.statement.wrapper.NativeQueryLog;
import info.archinnov.achilles.internal.statement.wrapper.NativeStatementWrapper;
import info.archinnov.achilles.internal.utils.UUIDGen;
import info.archinnov.achilles.listener.CASResultListener;
import info.archinnov.achilles.query.cql.NativeQueryValidator;
import info.archinnov.achilles.type.ConsistencyLevel;
import info.archinnov.achilles.type.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static info.archinnov.achilles.internal.consistency.ConsistencyConverter.getCQLLevel;
import static info.archinnov.achilles.type.OptionsBuilder.noOptions;

abstract class CommonBatch extends CommonPersistenceManager {

    private static final Logger log = LoggerFactory.getLogger(CommonBatch.class);

    protected BatchingFlushContext flushContext;
    protected NativeQueryValidator validator = new NativeQueryValidator();
    protected final ConsistencyLevel defaultConsistencyLevel;
    private final boolean orderedBatch;

    CommonBatch(Map<Class<?>, EntityMeta> entityMetaMap, PersistenceContextFactory contextFactory,
                DaoContext daoContext, ConfigurationContext configContext, boolean orderedBatch) {
        super(entityMetaMap, contextFactory, daoContext, configContext);
        this.defaultConsistencyLevel = configContext.getDefaultWriteConsistencyLevel();
        this.orderedBatch = orderedBatch;
        this.flushContext = new BatchingFlushContext(daoContext, defaultConsistencyLevel, Optional.<com.datastax.driver.core.ConsistencyLevel>absent());
    }

    /**
     * Start a batch session.
     */
    public void startBatch() {
        log.debug("Starting batch mode");
        flushContext = flushContext.duplicateWithNoData(defaultConsistencyLevel);
    }

    /**
     * Start a batch session with write consistency level
     */
    public void startBatch(ConsistencyLevel consistencyLevel) {
        log.debug("Starting batch mode with consistency level {}", consistencyLevel.name());
        flushContext = flushContext.duplicateWithNoData(consistencyLevel);
    }

    /**
     * Start a batch session with write consistency level and write serial consistency level
     */
    public void startBatch(ConsistencyLevel consistencyLevel, ConsistencyLevel serialConsistency) {
        log.debug("Starting batch mode with consistency level {}", consistencyLevel.name());
        Optional<com.datastax.driver.core.ConsistencyLevel> serialConsistencyLevel = Optional.absent();
        if (serialConsistency != null) {
            serialConsistencyLevel = Optional.fromNullable(getCQLLevel(serialConsistency));
        }
        flushContext = flushContext.duplicateWithNoData(consistencyLevel, serialConsistencyLevel);
    }


    /**
     * Cleaning all pending statements for the current batch session.
     */
    public void cleanBatch() {
        log.debug("Cleaning all pending statements");
        flushContext = flushContext.duplicateWithNoData(defaultConsistencyLevel);
    }

    /**
     * Batch insert an entity.
     * <p/>
     * <pre class="code"><code class="java">
     * // Insert
     * Batch batch = manager.createBatch();
     * <p/>
     * MyEntity managedEntity = batch.insert(myEntity);
     * <p/>
     * ...
     * <p/>
     * batch.flushBatch();
     * </code></pre>
     *
     * @param entity Entity to be inserted
     * @return proxified entity
     */
    @Override
    public <T> T insert(final T entity) {
        log.debug("Inserting entity '{}'", entity);
        return super.insert(entity, maybeAddTimestampToStatement(noOptions()));
    }

    /**
     * Batch insert an entity with the given options.
     * <p/>
     * <pre class="code"><code class="java">
     * // Insert
     * Batch batch = manager.createBatch();
     * <p/>
     * MyEntity managedEntity = batch.insert(myEntity, OptionsBuilder.withTtl(3600));
     * <p/>
     * ...
     * <p/>
     * batch.flushBatch();
     * </code></pre>
     *
     * @param entity  Entity to be inserted
     * @param options options
     * @return proxified entity
     */
    @Override
    public <T> T insert(final T entity, Options options) {
        log.debug("Inserting entity '{}' and options '{}'", entity, options);
        Options modifiedOptions = adaptOptionsForBatch(options);
        return super.asyncInsert(entity, modifiedOptions).getImmediately();
    }

    /**
     * Batch update a "managed" entity
     * <p/>
     * <pre class="code"><code class="java">
     * Batch batch = manager.createBatch();
     * User managedUser = manager.find(User.class,1L);
     * <p/>
     * user.setFirstname("DuyHai");
     * <p/>
     * batch.update(user);
     * <p/>
     * ...
     * <p/>
     * batch.flushBatch();
     * </code></pre>
     *
     * @param entity Managed entity to be updated
     */
    @Override
    public void update(Object entity) {
        log.debug("Updating entity '{}'", proxifier.getRealObject(entity));
        super.update(entity, maybeAddTimestampToStatement(noOptions()));
    }

    /**
     * Update a "managed" entity with options
     * <p/>
     * <pre class="code"><code class="java">
     * Batch batch = manager.createBatch();
     * User managedUser = manager.find(User.class,1L);
     * <p/>
     * user.setFirstname("DuyHai");
     * <p/>
     * batch.update(user, OptionsBuilder.withTtl(10));
     * <p/>
     * ...
     * <p/>
     * batch.flushBatch();
     * </code></pre>
     *
     * @param entity  Managed entity to be updated
     * @param options options
     */
    @Override
    public void update(Object entity, Options options) {
        log.debug("Updating entity '{}' with options {} ", proxifier.getRealObject(entity), options);
        Options modifiedOptions = adaptOptionsForBatch(options);
        super.asyncUpdate(entity, modifiedOptions).getImmediately();
    }

    /**
     * Batch insert a "transient" entity or update a "managed" entity.
     * <p/>
     * Shorthand to insert() or update()
     *
     * @param entity Managed entity to be inserted/updated
     * @return proxified entity
     */
    public <T> T insertOrUpdate(T entity) {
        log.debug("Inserting or updating entity '{}'", proxifier.getRealObject(entity));
        return super.insertOrUpdate(entity, maybeAddTimestampToStatement(noOptions()));
    }

    /**
     * Batch insert a "transient" entity or update a "managed" entity with options.
     * <p/>
     * Shorthand to insert() or update()
     *
     * @param entity  Managed entity to be inserted/updated
     * @param options options
     */
    public <T> T insertOrUpdate(T entity, Options options) {
        log.debug("Inserting or updating entity '{}' with options {}", proxifier.getRealObject(entity), options);
        Options modifiedOptions = adaptOptionsForBatch(options);
        return super.insertOrUpdate(entity, modifiedOptions);
    }

    /**
     * Batch remove an entity.
     * <p/>
     * <pre class="code"><code class="java">
     * // Simple removal
     * Batch batch = manager.createBatch();
     * User managedUser = manager.find(User.class,1L);
     * <p/>
     * batch.remove(managedUser);
     * <p/>
     * ...
     * <p/>
     * batch.flushBatch();
     * </code></pre>
     *
     * @param entity Entity to be removed
     */
    @Override
    public void remove(final Object entity) {
        log.debug("Removing entity '{}'", proxifier.getRealObject(entity));
        super.remove(entity, maybeAddTimestampToStatement(noOptions()));
    }

    /**
     * Batch remove an entity with the given options.
     * <p/>
     * <pre class="code"><code class="java">
     * // Removal with option
     * Batch batch = manager.createBatch();
     * User managedUser = manager.find(User.class,1L);
     * <p/>
     * batch.remove(managedUser, OptionsBuilder.withTimestamp(20292382030L));
     * <p/>
     * ...
     * <p/>
     * batch.flushBatch();
     * </code></pre>
     *
     * @param entity  Entity to be removed
     * @param options options for consistency level and timestamp
     */
    @Override
    public void remove(final Object entity, Options options) {
        log.debug("Removing entity '{}' with options {}", proxifier.getRealObject(entity), options);
        Options modifiedOptions = adaptOptionsForBatch(options);
        super.asyncRemove(entity, modifiedOptions).getImmediately();
    }

    /**
     * Batch remove an entity by its id.
     * <p/>
     * <pre class="code"><code class="java">
     * // Direct remove without read-before-write
     * Batch batch = manager.createBatch();
     * batch.removeById(User.class,1L);
     * <p/>
     * ...
     * <p/>
     * batch.flushBatch();
     * </code></pre>
     *
     * @param entityClass Entity class
     * @param primaryKey  Primary key
     */
    @Override
    public void removeById(Class<?> entityClass, Object primaryKey) {
        log.debug("Removing entity of type '{}' by its id '{}'", entityClass, primaryKey);
        super.removeById(entityClass, primaryKey, maybeAddTimestampToStatement(noOptions()));
    }

    /**
     * Batch remove an entity by its id with the given options.
     * <p/>
     * <pre class="code"><code class="java">
     * // Direct remove without read-before-write
     * Batch batch = manager.createBatch();
     * batch.removeById(User.class,1L, Options.withTimestamp(32234424234L));
     * <p/>
     * ...
     * <p/>
     * batch.flushBatch();
     * </code></pre>
     *
     * @param entityClass Entity class
     * @param primaryKey  Primary key
     */
    @Override
    public void removeById(Class<?> entityClass, Object primaryKey, Options options) {
        log.debug("Removing entity of type '{}' by its id '{}'", entityClass, primaryKey);
        Options modifiedOptions = maybeAddTimestampToStatement(options);
        super.asyncRemoveById(entityClass, primaryKey, modifiedOptions).getImmediately();
    }

    /**
     * Add a native CQL3 statement to the current batch.
     * <br/>
     * <br/>
     * <strong>This statement should be an INSERT or UPDATE</strong>, otherwise Achilles will raise an exception
     * <p/>
     * <pre class="code"><code class="java">
     * RegularStatement statement = insertInto("MyEntity").value("id",bindMarker()).value("name",bindMarker());
     * batch.batchNativeStatement(statement,10,"John");
     * </code></pre>
     *
     * @param regularStatement native CQL3 statement
     * @param boundValues      optional bound values
     */
    public void batchNativeStatement(RegularStatement regularStatement, Object... boundValues) {
        this.batchNativeStatementWithCASListener(regularStatement, null, boundValues);
    }

    /**
     * <pre class="code"><code class="java">
     * CASResultListener listener = ...
     * RegularStatement statement = insertInto("MyEntity").value("id",bindMarker()).value("name",bindMarker());
     * batch.batchNativeStatementWithCASListener(statement,listener,10,"John");
     * </code></pre>
     *
     * @param regularStatement  native CQL3 statement
     * @param casResultListener result listener for CAS operation
     * @param boundValues       optional bound values
     */
    public void batchNativeStatementWithCASListener(RegularStatement regularStatement, CASResultListener casResultListener, Object... boundValues) {
        log.debug("Batch native statement '{}' with bound values '{}'", regularStatement, boundValues);
        validator.validateUpsertOrDelete(regularStatement);
        final NativeStatementWrapper nativeStatementWrapper = new NativeStatementWrapper(NativeQueryLog.class, regularStatement, boundValues, Optional.fromNullable(casResultListener));
        flushContext.pushStatement(nativeStatementWrapper);
    }

    @Override
    protected PersistenceManagerOperations initPersistenceContext(Class<?> entityClass, Object primaryKey, Options options) {
        log.trace("Initializing new persistence context for entity class {} and primary key {}",
                entityClass.getCanonicalName(), primaryKey);
        return contextFactory.newContextWithFlushContext(entityClass, primaryKey, options, flushContext).getPersistenceManagerFacade();
    }

    @Override
    protected PersistenceManagerOperations initPersistenceContext(Object entity, Options options) {
        log.trace("Initializing new persistence context for entity {}", entity);
        return contextFactory.newContextWithFlushContext(entity, options, flushContext).getPersistenceManagerFacade();
    }

    protected Options adaptOptionsForBatch(Options options) {
        Options modifiedOptions = maybeAddTimestampToStatement(options);
        if (!optionsValidator.isOptionsValidForBatch(modifiedOptions)) {
            flushContext = flushContext.duplicateWithNoData(defaultConsistencyLevel);
            throw new AchillesException("Runtime custom Consistency Level and/or async listeners cannot be set for batch mode. Please set the Consistency Levels at batch start with 'startBatch(consistencyLevel)' and async listener using flushBatch(...)");
        }
        return modifiedOptions;
    }

    protected Options maybeAddTimestampToStatement(Options options) {
        if (orderedBatch)
            return options.duplicateWithNewTimestamp(UUIDGen.increasingMicroTimestamp());
        else
            return options;
    }

}

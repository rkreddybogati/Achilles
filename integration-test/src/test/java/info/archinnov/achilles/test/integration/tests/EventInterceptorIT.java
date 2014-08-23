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
package info.archinnov.achilles.test.integration.tests;

import static com.datastax.driver.core.querybuilder.QueryBuilder.bindMarker;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static info.archinnov.achilles.configuration.ConfigurationParameters.EVENT_INTERCEPTORS;
import static info.archinnov.achilles.configuration.ConfigurationParameters.FORCE_TABLE_CREATION;
import static info.archinnov.achilles.interceptor.Event.POST_LOAD;
import static info.archinnov.achilles.interceptor.Event.POST_PERSIST;
import static info.archinnov.achilles.interceptor.Event.POST_REMOVE;
import static info.archinnov.achilles.interceptor.Event.POST_UPDATE;
import static info.archinnov.achilles.interceptor.Event.PRE_PERSIST;
import static info.archinnov.achilles.interceptor.Event.PRE_REMOVE;
import static info.archinnov.achilles.interceptor.Event.PRE_UPDATE;
import static info.archinnov.achilles.test.integration.entity.CompleteBeanTestBuilder.builder;
import static info.archinnov.achilles.type.CounterBuilder.incr;
import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.common.collect.ImmutableMap;
import info.archinnov.achilles.embedded.CassandraEmbeddedServerBuilder;
import info.archinnov.achilles.interceptor.Event;
import info.archinnov.achilles.interceptor.Interceptor;
import info.archinnov.achilles.persistence.Batch;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.persistence.PersistenceManagerFactory;
import info.archinnov.achilles.test.integration.entity.ClusteredEntity;
import info.archinnov.achilles.test.integration.entity.CompleteBean;

public class EventInterceptorIT {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Interceptor<CompleteBean> prePersist = new Interceptor<CompleteBean>() {

        @Override
        public void onEvent(CompleteBean entity) {
            entity.setName("prePersist");
        }

        @Override
        public List<Event> events() {
            return Arrays.asList(PRE_PERSIST);
        }
    };

    private Interceptor<CompleteBean> postPersist = new Interceptor<CompleteBean>() {
        @Override
        public void onEvent(CompleteBean entity) {
            entity.setLabel("postPersist : " + entity.getLabel());
        }

        @Override
        public List<Event> events() {
            return Arrays.asList(POST_PERSIST);

        }
    };

    private Interceptor<CompleteBean> preUpdate = new Interceptor<CompleteBean>() {
        @Override
        public void onEvent(CompleteBean entity) {
            entity.setName("preUpdate");
        }

        @Override
        public List<Event> events() {
            return Arrays.asList(PRE_UPDATE);
        }
    };

    private Interceptor<CompleteBean> postUpdate = new Interceptor<CompleteBean>() {
        @Override
        public void onEvent(CompleteBean entity) {
            entity.setLabel("postUpdate");
        }

        @Override
        public List<Event> events() {
            return Arrays.asList(POST_UPDATE);
        }
    };

    private Interceptor<CompleteBean> preRemove = new Interceptor<CompleteBean>() {
        @Override
        public void onEvent(CompleteBean entity) {
            entity.setName("preRemove");
        }

        @Override
        public List<Event> events() {
            return Arrays.asList(PRE_REMOVE);
        }
    };

    private Interceptor<CompleteBean> postRemove = new Interceptor<CompleteBean>() {
        @Override
        public void onEvent(CompleteBean entity) {
            entity.setLabel("postRemove");
        }

        @Override
        public List<Event> events() {
            return Arrays.asList(POST_REMOVE);
        }
    };

    private Interceptor<CompleteBean> postLoad = new Interceptor<CompleteBean>() {
        @Override
        public void onEvent(CompleteBean entity) {
            entity.setLabel("postLoad");
        }

        @Override
        public List<Event> events() {
            return Arrays.asList(POST_LOAD);
        }
    };

    private Interceptor<ClusteredEntity> postLoadForClustered = new Interceptor<ClusteredEntity>() {
        @Override
        public void onEvent(ClusteredEntity entity) {
            entity.setValue("postLoad");
        }

        @Override
        public List<Event> events() {
            return Arrays.asList(POST_LOAD);
        }
    };

    private List<Interceptor<CompleteBean>> interceptors = Arrays.asList(prePersist, postPersist, preUpdate,
            postUpdate, preRemove, postLoad);

    private List<Interceptor<CompleteBean>> postRemoveInterceptors = Arrays.asList(postRemove);

    private PersistenceManagerFactory pmf = CassandraEmbeddedServerBuilder
            .withEntities(CompleteBean.class)
            .withKeyspaceName("interceptor_keyspace1")
            .withAchillesConfigParams(ImmutableMap.of(EVENT_INTERCEPTORS, interceptors, FORCE_TABLE_CREATION, true))
            .buildPersistenceManagerFactory();

    private PersistenceManager manager = pmf.createPersistenceManager();
    private Session session = manager.getNativeSession();

    private PersistenceManager manager2 = CassandraEmbeddedServerBuilder
            .withEntities(CompleteBean.class)
            .withKeyspaceName("interceptor_keyspace2")
            .withAchillesConfigParams(ImmutableMap.of(EVENT_INTERCEPTORS, postRemoveInterceptors, FORCE_TABLE_CREATION, true))
            .cleanDataFilesAtStartup(true)
            .buildPersistenceManager();

    private PersistenceManager manager3 = CassandraEmbeddedServerBuilder
            .withEntities(ClusteredEntity.class)
            .withKeyspaceName("interceptor_keyspace3")
            .withAchillesConfigParams(ImmutableMap.of(EVENT_INTERCEPTORS, asList(postLoadForClustered), FORCE_TABLE_CREATION, true))
            .cleanDataFilesAtStartup(true)
            .buildPersistenceManager();

    @Test
    public void should_apply_persist_interceptors() throws Exception {

        CompleteBean entity = builder().randomId().name("DuyHai").label("label").version(incr(2L)).buid();

        manager.insert(entity);

        assertThat(entity.getName()).isEqualTo("prePersist");
        assertThat(entity.getLabel()).isEqualTo("postPersist : label");

        Row row = session.execute("select name,label from CompleteBean where id = " + entity.getId()).one();

        assertThat(row.getString("name")).isEqualTo("prePersist");
        assertThat(row.getString("label")).isEqualTo("label");

    }

    @Test
    public void should_apply_update_interceptors() throws Exception {

        CompleteBean entity = builder().randomId().buid();

        entity = manager.insert(entity);
        entity.setName("DuyHai");
        entity.setLabel("label");

        manager.update(entity);

        Row row = session.execute("select name,label from CompleteBean where id = " + entity.getId()).one();

        assertThat(row.getString("name")).isEqualTo("preUpdate");
        assertThat(row.getString("label")).isEqualTo("label");
        assertThat(entity.getName()).isEqualTo("preUpdate");
        assertThat(entity.getLabel()).isEqualTo("postUpdate");
    }

    @Test
    public void should_apply_pre_remove_interceptors() throws Exception {

        CompleteBean entity = builder().randomId().name("DuyHai").label("label").buid();

        manager.remove(entity);

        assertThat(entity.getName()).isEqualTo("preRemove");
    }

    @Test
    public void should_apply_post_remove_interceptors() throws Exception {

        CompleteBean entity = builder().randomId().name("DuyHai").label("label").buid();

        manager2.remove(entity);

        assertThat(entity.getLabel()).isEqualTo("postRemove");
    }

    @Test
    public void should_apply_post_load_interceptors() throws Exception {

        CompleteBean entity = builder().randomId().name("DuyHai").label("label").buid();

        manager.insert(entity);

        entity = manager.find(CompleteBean.class, entity.getId());

        assertThat(entity.getLabel()).isEqualTo("postLoad");
    }

    @Test
    public void should_apply_interceptors_after_flush_for_batch() throws Exception {
        // Given
        final Batch batchingPM = pmf.createBatch();
        batchingPM.startBatch();

        CompleteBean entity = builder().randomId().name("DuyHai").label("label").buid();

        // When
        batchingPM.insert(entity);

        // Then
        assertThat(entity.getName()).isEqualTo("DuyHai");
        assertThat(entity.getLabel()).isEqualTo("label");

        // When
        batchingPM.flushBatch();

        // Then
        assertThat(entity.getName()).isEqualTo("prePersist");
        assertThat(entity.getLabel()).isEqualTo("postPersist : label");
    }

    @Test
    public void should_apply_post_load_interceptor_on_slice_query() throws Exception {
        // Given
        Long id = RandomUtils.nextLong();
        Integer count = RandomUtils.nextInt();
        String name = RandomStringUtils.randomAlphabetic(10);
        String value = "value_before_load";
        ClusteredEntity entity = new ClusteredEntity(id, count, name, value);

        manager3.insert(entity);

        // When
        final List<ClusteredEntity> clusteredEntities = manager3.sliceQuery(ClusteredEntity.class)
                .forSelect()
                .withPartitionComponents(id)
                .get(10);

        // Then
        assertThat(clusteredEntities.get(0).getValue()).isEqualTo("postLoad");
    }

    @Test
    public void should_apply_post_load_interceptor_on_typed_query() throws Exception {
        // Given
        CompleteBean entity = builder().randomId().name("DuyHai").label("label").buid();

        manager.insert(entity);

        RegularStatement statement = select().from("CompleteBean").where(eq("id",bindMarker()));

        // When
        final CompleteBean actual = manager.typedQuery(CompleteBean.class, statement,entity.getId()).getFirst();

        // Then
        assertThat(actual.getLabel()).isEqualTo("postLoad");
    }

    @Test
    public void should_apply_post_load_interceptor_on_raw_typed_query() throws Exception {
        // Given
        CompleteBean entity = builder().randomId().name("DuyHai").label("label").buid();

        manager.insert(entity);

        RegularStatement statement = select().from("CompleteBean").where(eq("id",bindMarker()));

        // When
        final CompleteBean actual = manager.rawTypedQuery(CompleteBean.class, statement,entity.getId()).getFirst();

        // Then
        assertThat(actual.getLabel()).isEqualTo("postLoad");
    }
}

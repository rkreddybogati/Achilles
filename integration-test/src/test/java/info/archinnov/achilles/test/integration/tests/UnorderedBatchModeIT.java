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

import static org.fest.assertions.api.Assertions.assertThat;
import org.junit.Test;
import info.archinnov.achilles.embedded.CassandraEmbeddedServerBuilder;
import info.archinnov.achilles.persistence.Batch;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.persistence.PersistenceManagerFactory;
import info.archinnov.achilles.test.integration.entity.CompleteBean;
import info.archinnov.achilles.test.integration.entity.CompleteBeanTestBuilder;

public class UnorderedBatchModeIT {

    private PersistenceManager manager = CassandraEmbeddedServerBuilder
            .withEntities(CompleteBean.class).withKeyspaceName("unordered_batch")
            .cleanDataFilesAtStartup(true)
            .buildPersistenceManager();


    @Test
    public void should_not_order_batch_statements_for_insert() throws Exception {
        //Given
        CompleteBean entity = CompleteBeanTestBuilder.builder().randomId().buid();
        Batch batch = manager.createBatch();

        //When
        batch.startBatch();

        entity.setName("name3");
        batch.insert(entity);
        entity.setName("name1");
        batch.insert(entity);

        batch.flushBatch();
        //Then

        CompleteBean actual = manager.find(CompleteBean.class, entity.getId());

        assertThat(actual.getName()).isEqualTo("name3");
    }

    @Test
    public void should_not_order_batch_statements_for_update() throws Exception {
        //Given
        CompleteBean entity = CompleteBeanTestBuilder.builder().randomId().buid();
        Batch batch = manager.createBatch();

        CompleteBean managed = manager.insert(entity);

        //When
        batch.startBatch();

        managed.setName("name3");
        batch.update(managed);
        managed.setName("name1");
        batch.update(managed);

        batch.flushBatch();

        //Then
        manager.refresh(managed);

        assertThat(managed.getName()).isEqualTo("name3");
    }
}

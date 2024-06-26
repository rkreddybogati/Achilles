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
package info.archinnov.achilles.internal.context;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import info.archinnov.achilles.internal.metadata.holder.EntityMeta;
import info.archinnov.achilles.internal.metadata.holder.PropertyMeta;
import info.archinnov.achilles.test.mapping.entity.CompleteBean;
import info.archinnov.achilles.type.ConsistencyLevel;
import info.archinnov.achilles.type.OptionsBuilder;

@RunWith(MockitoJUnitRunner.class)
public class PersistenceContextTest {

    private PersistenceContext context;

    @Mock
    private DaoContext daoContext;

    @Mock
    private AbstractFlushContext flushContext;

    @Mock
    private ConfigurationContext configurationContext;


    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private EntityMeta meta;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PropertyMeta idMeta;

    private Long primaryKey = RandomUtils.nextLong();

    @Before
    public void setUp() throws Exception {
        when(meta.getIdMeta()).thenReturn(idMeta);
        when(meta.<CompleteBean>getEntityClass()).thenReturn(CompleteBean.class);
        when(configurationContext.getDefaultWriteConsistencyLevel()).thenReturn(ConsistencyLevel.ONE);

        context = new PersistenceContext(meta, configurationContext, daoContext, flushContext, CompleteBean.class,
                primaryKey, OptionsBuilder.noOptions());

    }

    @Test
    public void should_duplicate_for_new_entity() throws Exception {
        CompleteBean entity = new CompleteBean();
        entity.setId(primaryKey);
        when(meta.forOperations().getPrimaryKey(entity)).thenReturn(primaryKey);
        when(flushContext.duplicate()).thenReturn(flushContext);

        PersistenceContext duplicateContext = context.duplicate(entity);

        assertThat(duplicateContext.stateHolderFacade.getEntity()).isSameAs(entity);
        assertThat(duplicateContext.stateHolderFacade.getPrimaryKey()).isSameAs(primaryKey);
    }


}

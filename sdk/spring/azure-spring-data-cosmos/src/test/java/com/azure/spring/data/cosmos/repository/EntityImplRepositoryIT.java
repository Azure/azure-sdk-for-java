// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.ResponseDiagnosticsTestUtils;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.Address;
import com.azure.spring.data.cosmos.domain.EntityImpl;
import com.azure.spring.data.cosmos.repository.repository.EntityImplRepository;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class EntityImplRepositoryIT {

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    EntityImplRepository repository;

    @Autowired
    CosmosConfig cosmosConfig;

    @Autowired
    private CosmosTemplate template;

    @Autowired
    private ResponseDiagnosticsTestUtils responseDiagnosticsTestUtils;

    private final EntityImpl ENTITYIMPL_1 = new EntityImpl();
    private final EntityImpl ENTITYIMPL_2 = new EntityImpl();

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, Address.class);
        ENTITYIMPL_1.setId("entityImpl1");
        ENTITYIMPL_2.setId("entityImpl2");
    }

    @Test
    public void testSave() {
        EntityImpl savedEntityImpl = repository.save(ENTITYIMPL_1);
        assertThat(savedEntityImpl.getId()).isEqualTo(ENTITYIMPL_1.getId());

        Iterable<EntityImpl> savedEI = repository.findAll();
        assertThat(savedEI.iterator().next().getId()).isEqualTo(ENTITYIMPL_1.getId());
    }

    @Test
    public void testSaveAll() {
        Iterable<EntityImpl> savedEntityImpl = repository.saveAll(Lists.newArrayList(ENTITYIMPL_1, ENTITYIMPL_2));
        Iterator<EntityImpl> iter = savedEntityImpl.iterator();
        assertThat(iter.next().getId()).isEqualTo(ENTITYIMPL_1.getId());
        assertThat(iter.next().getId()).isEqualTo(ENTITYIMPL_2.getId());

        Iterable<EntityImpl> savedEI = repository.findAll();
        Iterator<EntityImpl> iter2 = savedEI.iterator();
        assertThat(iter2.next().getId()).isEqualTo(ENTITYIMPL_1.getId());
        assertThat(iter2.next().getId()).isEqualTo(ENTITYIMPL_2.getId());
    }
}

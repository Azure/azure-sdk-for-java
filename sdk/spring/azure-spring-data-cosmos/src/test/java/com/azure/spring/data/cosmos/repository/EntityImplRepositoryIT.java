// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.ResponseDiagnosticsTestUtils;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.Address;
import com.azure.spring.data.cosmos.domain.EntityImpl;
import com.azure.spring.data.cosmos.domain.ParentEntity;
import com.azure.spring.data.cosmos.repository.repository.EntityImplRepository;
import com.azure.spring.data.cosmos.repository.repository.ParentEntityRepository;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class EntityImplRepositoryIT {


    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    EntityImplRepository entityImplRepository;

    @Autowired
    ParentEntityRepository parentEntityRepository;

    @Autowired
    CosmosConfig cosmosConfig;

    @Autowired
    private CosmosTemplate template;

    @Autowired
    private ResponseDiagnosticsTestUtils responseDiagnosticsTestUtils;

    private final EntityImpl ENTITYIMPL_1 = new EntityImpl();
    private final EntityImpl ENTITYIMPL_2 = new EntityImpl();

    private final ParentEntity PARENTENTITY_1 = new ParentEntity();
    private final ParentEntity PARENTENTITY_2 = new ParentEntity();

    @BeforeEach
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, Address.class);
        ENTITYIMPL_1.setId("entityImpl1");
        ENTITYIMPL_2.setId("entityImpl2");
        PARENTENTITY_1.setId("parentEntity1");
        PARENTENTITY_2.setId("parentEntity2");
    }

    @Test
    public void testSave() {
        EntityImpl savedEntityImpl = entityImplRepository.save(ENTITYIMPL_1);
        assertThat(savedEntityImpl.getId()).isEqualTo(ENTITYIMPL_1.getId());

        Iterable<EntityImpl> savedEI = entityImplRepository.findAll();
        assertThat(savedEI.iterator().next().getId()).isEqualTo(ENTITYIMPL_1.getId());

        ParentEntity savedParentEntity = parentEntityRepository.save(PARENTENTITY_1);
        assertThat(savedParentEntity.getId()).isEqualTo(PARENTENTITY_1.getId());

        Iterable<ParentEntity> savedPE = parentEntityRepository.findAll();
        assertThat(savedPE.iterator().next().getId()).isEqualTo(PARENTENTITY_1.getId());

    }

    @Test
    public void testSaveAllIdOnGrandparent() {
        Iterable<EntityImpl> savedEntityImpl = entityImplRepository.saveAll(Lists.newArrayList(ENTITYIMPL_1, ENTITYIMPL_2));
        Iterator<EntityImpl> iter = savedEntityImpl.iterator();
        assertThat(iter.next().getId()).isEqualTo(ENTITYIMPL_1.getId());
        assertThat(iter.next().getId()).isEqualTo(ENTITYIMPL_2.getId());

        Iterable<EntityImpl> savedEI = entityImplRepository.findAll();
        Iterator<EntityImpl> iter2 = savedEI.iterator();
        assertThat(iter2.next().getId()).isEqualTo(ENTITYIMPL_1.getId());
        assertThat(iter2.next().getId()).isEqualTo(ENTITYIMPL_2.getId());
    }

    @Test
    public void testSaveAllIdOnParent() {
        Iterable<ParentEntity> savedParentEntity = parentEntityRepository.saveAll(Lists.newArrayList(PARENTENTITY_1, PARENTENTITY_2));
        Iterator<ParentEntity> iter = savedParentEntity.iterator();
        assertThat(iter.next().getId()).isEqualTo(PARENTENTITY_1.getId());
        assertThat(iter.next().getId()).isEqualTo(PARENTENTITY_2.getId());

        Iterable<ParentEntity> savedPE = parentEntityRepository.findAll();
        Iterator<ParentEntity> iter2 = savedPE.iterator();
        assertThat(iter2.next().getId()).isEqualTo(PARENTENTITY_1.getId());
        assertThat(iter2.next().getId()).isEqualTo(PARENTENTITY_2.getId());
    }
}

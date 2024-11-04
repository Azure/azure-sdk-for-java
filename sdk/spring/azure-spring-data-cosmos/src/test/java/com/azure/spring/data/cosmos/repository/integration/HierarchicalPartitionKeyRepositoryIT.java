// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyBuilder;
import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.TestUtils;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.HierarchicalPartitionKeyEntity;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.HierarchicalPartitionKeyRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class HierarchicalPartitionKeyRepositoryIT {

    private static final HierarchicalPartitionKeyEntity HIERARCHICAL_ENTITY_1 =
        new HierarchicalPartitionKeyEntity("id_1", "John", "Doe", "12345");

    private static final HierarchicalPartitionKeyEntity HIERARCHICAL_ENTITY_2 =
            new HierarchicalPartitionKeyEntity("id_2", "Michael", "Smith", "23456");

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    private static CosmosEntityInformation<HierarchicalPartitionKeyEntity, String> hierarchicalPartitionKeyEntityInformation
        = new CosmosEntityInformation<>(HierarchicalPartitionKeyEntity.class);

    @Autowired
    private CosmosTemplate template;

    @Autowired
    HierarchicalPartitionKeyRepository repository;

    @BeforeClass
    public static void init() { }

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, HierarchicalPartitionKeyEntity.class);
    }

    @AfterClass
    public static void cleanUp() {
        collectionManager.deleteContainer(hierarchicalPartitionKeyEntityInformation);
    }

    @Test
    public void testSave() {
        HierarchicalPartitionKeyEntity savedEntity = repository.save(HIERARCHICAL_ENTITY_1);

        assertThat(savedEntity.getId()).isEqualTo(HIERARCHICAL_ENTITY_1.getId());
    }

    @Test
    public void testFindAll() {
        repository.saveAll(Arrays.asList(HIERARCHICAL_ENTITY_1, HIERARCHICAL_ENTITY_2));

        Iterable<HierarchicalPartitionKeyEntity> iterable = repository.findAll();
        List<HierarchicalPartitionKeyEntity> hierarchicalPartitionKeyValues = TestUtils.toList(iterable);
        assertThat(hierarchicalPartitionKeyValues.size()).isEqualTo(2);
        assertThat(hierarchicalPartitionKeyValues.get(0).getId()).isEqualTo("id_1");
        assertThat(hierarchicalPartitionKeyValues.get(1).getId()).isEqualTo("id_2");
    }

    @Test
    public void testFindAllByPartitionKey() {
        repository.saveAll(Arrays.asList(HIERARCHICAL_ENTITY_1, HIERARCHICAL_ENTITY_2));

        PartitionKey pk = new PartitionKeyBuilder()
                .add(HIERARCHICAL_ENTITY_1.getId())
                .add(HIERARCHICAL_ENTITY_1.getFirstName())
                .add(HIERARCHICAL_ENTITY_1.getLastName())
                .build();
        Iterable<HierarchicalPartitionKeyEntity> results = repository.findAll(pk);
        assertThat(TestUtils.toList(results).size()).isEqualTo(1);
        assertThat(results.iterator().hasNext()).isTrue();
        assertThat(results.iterator().next().getId()).isEqualTo(HIERARCHICAL_ENTITY_1.getId());
    }

    @Test
    public void testFindByIdAndPartitionKey() {
        repository.saveAll(Arrays.asList(HIERARCHICAL_ENTITY_1, HIERARCHICAL_ENTITY_2));

        PartitionKey pk = new PartitionKeyBuilder()
                .add(HIERARCHICAL_ENTITY_2.getId())
                .add(HIERARCHICAL_ENTITY_2.getFirstName())
                .add(HIERARCHICAL_ENTITY_2.getLastName())
                .build();
        Optional<HierarchicalPartitionKeyEntity> results = repository.findById(HIERARCHICAL_ENTITY_2.getId(), pk);
        HierarchicalPartitionKeyEntity result = results.get();
        assertThat(result.getId()).isEqualTo(HIERARCHICAL_ENTITY_2.getId());
    }

    @Test
    public void testDeleteByIdAndPartitionKey() {
        repository.saveAll(Arrays.asList(HIERARCHICAL_ENTITY_1, HIERARCHICAL_ENTITY_2));

        PartitionKey pk = new PartitionKeyBuilder()
                .add(HIERARCHICAL_ENTITY_1.getId())
                .add(HIERARCHICAL_ENTITY_1.getFirstName())
                .add(HIERARCHICAL_ENTITY_1.getLastName())
                .build();
        repository.deleteById(HIERARCHICAL_ENTITY_1.getId(), pk);
        Iterable<HierarchicalPartitionKeyEntity> iterable = repository.findAll();
        List<HierarchicalPartitionKeyEntity> hierarchicalPartitionKeyValues = TestUtils.toList(iterable);
        assertThat(hierarchicalPartitionKeyValues.size()).isEqualTo(1);
        assertThat(hierarchicalPartitionKeyValues.get(0).getId()).isEqualTo(HIERARCHICAL_ENTITY_2.getId());
    }

    @Test
    public void testDeleteByEntity() {
        repository.saveAll(Arrays.asList(HIERARCHICAL_ENTITY_1, HIERARCHICAL_ENTITY_2));

        repository.delete(HIERARCHICAL_ENTITY_2);
        Iterable<HierarchicalPartitionKeyEntity> iterable = repository.findAll();
        List<HierarchicalPartitionKeyEntity> hierarchicalPartitionKeyValues = TestUtils.toList(iterable);
        assertThat(hierarchicalPartitionKeyValues.size()).isEqualTo(1);
        assertThat(hierarchicalPartitionKeyValues.get(0).getId()).isEqualTo(HIERARCHICAL_ENTITY_1.getId());
    }
}

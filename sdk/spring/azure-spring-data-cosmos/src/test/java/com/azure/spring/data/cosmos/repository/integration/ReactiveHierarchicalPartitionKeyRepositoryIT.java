// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyBuilder;
import com.azure.spring.data.cosmos.ReactiveIntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.domain.HierarchicalPartitionKeyEntity;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.ReactiveHierarchicalPartitionKeyRepository;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ReactiveHierarchicalPartitionKeyRepositoryIT {

    private static final HierarchicalPartitionKeyEntity HIERARCHICAL_ENTITY_1 =
        new HierarchicalPartitionKeyEntity("id_1", "John", "Doe", "12345");

    private static final HierarchicalPartitionKeyEntity HIERARCHICAL_ENTITY_2 =
        new HierarchicalPartitionKeyEntity("id_2", "Michael", "Smith", "23456");

    @ClassRule
    public static final ReactiveIntegrationTestCollectionManager collectionManager = new ReactiveIntegrationTestCollectionManager();

    private static CosmosEntityInformation<HierarchicalPartitionKeyEntity, String> hierarchicalPartitionKeyEntityInformation
        = new CosmosEntityInformation<>(HierarchicalPartitionKeyEntity.class);

    @Autowired
    private ReactiveCosmosTemplate reactiveTemplate;

    @Autowired
    ReactiveHierarchicalPartitionKeyRepository repository;

    @BeforeClass
    public static void init() { }

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(reactiveTemplate, HierarchicalPartitionKeyEntity.class);
    }

    @AfterClass
    public static void cleanUp() {
        collectionManager.deleteContainer(hierarchicalPartitionKeyEntityInformation);
    }

    @Test
    public void testSave() {
        Mono<HierarchicalPartitionKeyEntity> savedEntity = repository.save(HIERARCHICAL_ENTITY_1);

        assertThat(savedEntity.block().getId()).isEqualTo(HIERARCHICAL_ENTITY_1.getId());
    }

    @Test
    public void testFindAll() {
        Flux<HierarchicalPartitionKeyEntity> saveEntities = repository.saveAll(Arrays.asList(HIERARCHICAL_ENTITY_1, HIERARCHICAL_ENTITY_2));
        StepVerifier.create(saveEntities).expectNextCount(2).verifyComplete();

        Flux<HierarchicalPartitionKeyEntity> resultFlux = repository.findAll();
        StepVerifier.create(resultFlux)
            .assertNext(entity -> assertEquals(entity.getId(), HIERARCHICAL_ENTITY_1.getId()))
            .assertNext(entity -> assertEquals(entity.getId(), HIERARCHICAL_ENTITY_2.getId()))
            .verifyComplete();
    }

    @Test
    public void testFindAllByPartitionKey() {
        Flux<HierarchicalPartitionKeyEntity> saveEntities = repository.saveAll(Arrays.asList(HIERARCHICAL_ENTITY_1, HIERARCHICAL_ENTITY_2));
        StepVerifier.create(saveEntities).expectNextCount(2).verifyComplete();

        PartitionKey pk = new PartitionKeyBuilder()
            .add(HIERARCHICAL_ENTITY_1.getId())
            .add(HIERARCHICAL_ENTITY_1.getFirstName())
            .add(HIERARCHICAL_ENTITY_1.getLastName())
            .build();
        Flux<HierarchicalPartitionKeyEntity> resultFlux = repository.findAll(pk);
        StepVerifier.create(resultFlux)
            .assertNext(entity -> assertEquals(entity.getId(), HIERARCHICAL_ENTITY_1.getId()))
            .verifyComplete();
    }

    @Test
    public void testFindByIdAndPartitionKey() {
        Flux<HierarchicalPartitionKeyEntity> saveEntities = repository.saveAll(Arrays.asList(HIERARCHICAL_ENTITY_1, HIERARCHICAL_ENTITY_2));
        StepVerifier.create(saveEntities).expectNextCount(2).verifyComplete();

        PartitionKey pk = new PartitionKeyBuilder()
            .add(HIERARCHICAL_ENTITY_2.getId())
            .add(HIERARCHICAL_ENTITY_2.getFirstName())
            .add(HIERARCHICAL_ENTITY_2.getLastName())
            .build();
        Mono<HierarchicalPartitionKeyEntity> resultFlux = repository.findById(HIERARCHICAL_ENTITY_2.getId(), pk);
        StepVerifier.create(resultFlux)
            .assertNext(entity -> assertEquals(entity.getId(), HIERARCHICAL_ENTITY_2.getId()))
            .verifyComplete();
    }

    @Test
    public void testDeleteByIdAndPartitionKey() {
        Flux<HierarchicalPartitionKeyEntity> saveEntities = repository.saveAll(Arrays.asList(HIERARCHICAL_ENTITY_1, HIERARCHICAL_ENTITY_2));
        StepVerifier.create(saveEntities).expectNextCount(2).verifyComplete();

        PartitionKey pk = new PartitionKeyBuilder()
            .add(HIERARCHICAL_ENTITY_1.getId())
            .add(HIERARCHICAL_ENTITY_1.getFirstName())
            .add(HIERARCHICAL_ENTITY_1.getLastName())
            .build();
        Mono<Void> deleteMono = repository.deleteById(HIERARCHICAL_ENTITY_1.getId(), pk);
        StepVerifier.create(deleteMono).verifyComplete();

        Flux<HierarchicalPartitionKeyEntity> resultFlux = repository.findAll();
        StepVerifier.create(resultFlux)
            .assertNext(entity -> assertEquals(entity.getId(), HIERARCHICAL_ENTITY_2.getId()))
            .verifyComplete();
    }

    @Test
    public void testDeleteByEntity() {
        Flux<HierarchicalPartitionKeyEntity> saveEntities = repository.saveAll(Arrays.asList(HIERARCHICAL_ENTITY_1, HIERARCHICAL_ENTITY_2));
        StepVerifier.create(saveEntities).expectNextCount(2).verifyComplete();

        Mono<Void> deleteMono = repository.delete(HIERARCHICAL_ENTITY_2);
        StepVerifier.create(deleteMono).verifyComplete();

        Flux<HierarchicalPartitionKeyEntity> resultFlux = repository.findAll();
        StepVerifier.create(resultFlux)
            .assertNext(entity -> assertEquals(entity.getId(), HIERARCHICAL_ENTITY_1.getId()))
            .verifyComplete();
    }
}

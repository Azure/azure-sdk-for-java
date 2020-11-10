// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.NestedEntity;
import com.azure.spring.data.cosmos.domain.NestedPartitionKeyEntity;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.ReactiveNestedPartitionKeyRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ReactiveNestedPartitionKeyRepositoryIT {

    private static final NestedPartitionKeyEntity NESTED_ENTITY_1 =
        new NestedPartitionKeyEntity(null, new NestedEntity("partitionKey1"));

    private static final NestedPartitionKeyEntity NESTED_ENTITY_2 =
        new NestedPartitionKeyEntity(null, new NestedEntity("partitionKey2"));

    private static final CosmosEntityInformation<NestedPartitionKeyEntity, String> entityInformation =
        new CosmosEntityInformation<>(NestedPartitionKeyEntity.class);

    private static CosmosTemplate staticTemplate;
    private static boolean isSetupDone;

    @Autowired
    private CosmosTemplate template;

    @Autowired
    ReactiveNestedPartitionKeyRepository repository;

    @BeforeClass
    public static void init() { }

    @Before
    public void setUp() {
        if (!isSetupDone) {
            staticTemplate = template;
            template.createContainerIfNotExists(entityInformation);
        }
        isSetupDone = true;
    }

    @After
    public void cleanup() {
        repository.deleteAll();
    }

    @AfterClass
    public static void afterClassCleanup() {
        staticTemplate.deleteContainer(entityInformation.getContainerName());
    }

    @Test
    public void testSave() {
        Mono<NestedPartitionKeyEntity> savedEntity = repository.save(NESTED_ENTITY_1);

        StepVerifier.create(savedEntity).expectNext(NESTED_ENTITY_1).verifyComplete();
    }

    @Test
    public void testFindAll() {
        Flux<NestedPartitionKeyEntity> nestedPartitionKeyEntityFlux =
            repository.saveAll(Arrays.asList(NESTED_ENTITY_1, NESTED_ENTITY_2));

        StepVerifier.create(nestedPartitionKeyEntityFlux).expectNextCount(2).verifyComplete();

        Flux<NestedPartitionKeyEntity> iterable = repository.findAll();
        StepVerifier.create(iterable).expectNextCount(2).verifyComplete();
    }

    @Test
    public void testFindAllByPartitionKey() {
        Flux<NestedPartitionKeyEntity> nestedPartitionKeyEntityFlux =
            repository.saveAll(Arrays.asList(NESTED_ENTITY_1, NESTED_ENTITY_2));

        StepVerifier.create(nestedPartitionKeyEntityFlux).expectNextCount(2).verifyComplete();

        Flux<NestedPartitionKeyEntity> fluxEntities = repository.findAll(new PartitionKey("partitionKey2"));
        StepVerifier.create(fluxEntities).expectNext(NESTED_ENTITY_2).verifyComplete();
    }

    @Test
    public void testFindByIdAndPartitionKey() {
        Flux<NestedPartitionKeyEntity> nestedPartitionKeyEntityFlux =
            repository.saveAll(Arrays.asList(NESTED_ENTITY_1, NESTED_ENTITY_2));

        StepVerifier.create(nestedPartitionKeyEntityFlux).expectNextCount(2).verifyComplete();

        Mono<NestedPartitionKeyEntity> nestedEntityMono = repository.findById(NESTED_ENTITY_1.getId(),
            new PartitionKey(NESTED_ENTITY_1.getNestedEntity().getNestedPartitionKey()));
        StepVerifier.create(nestedEntityMono).expectNext(NESTED_ENTITY_1).verifyComplete();
    }

    @Test
    public void testDeleteByIdAndPartitionKey() {
        Flux<NestedPartitionKeyEntity> nestedPartitionKeyEntityFlux =
            repository.saveAll(Arrays.asList(NESTED_ENTITY_1, NESTED_ENTITY_2));

        StepVerifier.create(nestedPartitionKeyEntityFlux).expectNextCount(2).verifyComplete();

        Mono<Void> voidMono = repository.deleteById(NESTED_ENTITY_1.getId(),
            new PartitionKey(NESTED_ENTITY_1.getNestedEntity().getNestedPartitionKey()));
        StepVerifier.create(voidMono).verifyComplete();
        Flux<NestedPartitionKeyEntity> findAllFlux = repository.findAll();

        StepVerifier.create(findAllFlux).expectNextCount(1).verifyComplete();
    }

    @Test
    public void testDeleteByEntity() {
        Flux<NestedPartitionKeyEntity> nestedPartitionKeyEntityFlux =
            repository.saveAll(Arrays.asList(NESTED_ENTITY_1, NESTED_ENTITY_2));

        StepVerifier.create(nestedPartitionKeyEntityFlux).expectNextCount(2).verifyComplete();

        Mono<Void> delete = repository.delete(NESTED_ENTITY_1);
        StepVerifier.create(delete).verifyComplete();
        Flux<NestedPartitionKeyEntity> findFlux = repository.findAll();
        StepVerifier.create(findFlux).expectNext(NESTED_ENTITY_2).verifyComplete();
    }

}

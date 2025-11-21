// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.PersistableEntity;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.exception.CosmosConflictException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.PersistableEntityRepository;
import com.azure.spring.data.cosmos.repository.repository.ReactivePersistableEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class PersistableIT {

    
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    private PersistableEntityRepository repository;

    @Autowired
    private ReactivePersistableEntityRepository reactiveRepository;

    @Autowired
    private CosmosTemplate template;

    

    @BeforeEach
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, PersistableEntity.class);
    }

    @Test
    public void testInsertShouldSaveEntity() {
        final PersistableEntity entity = new PersistableEntity("id", "pk");
        assertTrue(entity.isNew());
        final PersistableEntity savedEntity = repository.save(entity);
        assertNotNull(savedEntity.getVersion());
    }

    @Test
    public void testReactiveInsertShouldSaveEntity() {
        final PersistableEntity entity = new PersistableEntity("id", "pk");
        assertTrue(entity.isNew());
        final Mono<PersistableEntity> savedEntity = reactiveRepository.save(entity);
        StepVerifier.create(savedEntity).expectNext(entity).verifyComplete();
    }

    @Test
    public void testInsertDuplicateShouldThrowConflictException() {
        final PersistableEntity entity = new PersistableEntity("id", "pk");
        assertTrue(entity.isNew());
        repository.save(entity);

        try {
            repository.save(entity);
            fail("expecting conflict exception");
        } catch (CosmosAccessException ex) {
            assertEquals(TestConstants.CONFLICT_STATUS_CODE, ex.getCosmosException().getStatusCode());
        }
    }

    @Test
    public void testReactiveInsertDuplicateShouldThrowConflictException() {
        final PersistableEntity entity = new PersistableEntity("id", "pk");
        assertTrue(entity.isNew());
        reactiveRepository.save(entity).block();

        final Mono<PersistableEntity> saveSecond = reactiveRepository.save(entity);
        StepVerifier.create(saveSecond)
            .expectErrorMatches(ex -> ex instanceof CosmosConflictException && ((CosmosAccessException) ex).getCosmosException().getStatusCode() == TestConstants.CONFLICT_STATUS_CODE)
            .verify();
    }

    @Test
    public void testUpdateShouldSucceedEvenIfEntityDoesNotExist() {
        final PersistableEntity entity = new PersistableEntity("id", "pk", "version");
        assertFalse(entity.isNew());

        final PersistableEntity savedEntity = repository.save(entity);
        assertNotEquals(entity.getVersion(), savedEntity.getVersion());
    }

    @Test
    public void testReactiveUpdateShouldSucceedEvenIfEntityDoesNotExist() {
        final PersistableEntity entity = new PersistableEntity("id", "pk", "version");
        assertFalse(entity.isNew());

        final Mono<PersistableEntity> savedMono = reactiveRepository.save(entity);
        StepVerifier.create(savedMono)
            .assertNext(savedEntity -> assertNotEquals(entity.getVersion(), savedEntity.getVersion()))
            .verifyComplete();
    }

}

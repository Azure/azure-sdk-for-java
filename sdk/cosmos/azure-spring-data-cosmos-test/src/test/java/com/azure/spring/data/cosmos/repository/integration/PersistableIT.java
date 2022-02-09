// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.cosmos.implementation.ConflictException;
import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.PersistableEntity;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.PersistableEntityRepository;
import com.azure.spring.data.cosmos.repository.repository.ReactivePersistableEntityRepository;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class PersistableIT {

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    private PersistableEntityRepository repository;

    @Autowired
    private ReactivePersistableEntityRepository reactiveRepository;

    @Autowired
    private CosmosTemplate template;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
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
            assertTrue(ex.getCosmosException() instanceof ConflictException);
        }
    }

    @Test
    public void testReactiveInsertDuplicateShouldThrowConflictException() {
        final PersistableEntity entity = new PersistableEntity("id", "pk");
        assertTrue(entity.isNew());
        reactiveRepository.save(entity).block();

        final Mono<PersistableEntity> saveSecond = reactiveRepository.save(entity);
        StepVerifier.create(saveSecond)
            .expectErrorMatches(ex -> ex instanceof CosmosAccessException && ((CosmosAccessException) ex).getCosmosException() instanceof ConflictException)
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

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.cosmos.implementation.ConflictException;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.PersistableEntity;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.PersistableEntityRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class PersistableIT {

    private static final CosmosEntityInformation<PersistableEntity, String> entityInformation
        = new CosmosEntityInformation<>(PersistableEntity.class);

    private static CosmosTemplate staticTemplate;
    private static boolean isSetupDone;

    @Autowired
    PersistableEntityRepository repository;

    @Autowired
    private CosmosTemplate template;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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
    public void testInsertShouldSaveEntity() {
        final PersistableEntity entity = new PersistableEntity("id", "pk");
        assertTrue(entity.isNew());
        final PersistableEntity savedEntity = repository.save(entity);
        assertNotNull(savedEntity.getVersion());
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
    public void testUpdateShouldSucceedEvenIfEntityDoesNotExist() {
        final PersistableEntity entity = new PersistableEntity("id", "pk", "version");
        assertFalse(entity.isNew());

        final PersistableEntity savedEntity = repository.save(entity);
        assertNotEquals(entity.getVersion(), savedEntity.getVersion());
    }

}

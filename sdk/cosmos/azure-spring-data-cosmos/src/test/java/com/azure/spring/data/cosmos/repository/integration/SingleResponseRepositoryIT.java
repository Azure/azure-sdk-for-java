// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.SingleResponseEntity;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.SingleResponseRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class SingleResponseRepositoryIT {

    private static final SingleResponseEntity TEST_SINGLE_RESPONSE_ENTITY = new SingleResponseEntity("testId",
        "faketitle");

    private static final CosmosEntityInformation<SingleResponseEntity, String> entityInformation =
        new CosmosEntityInformation<>(SingleResponseEntity.class);

    private static CosmosTemplate staticTemplate;
    private static boolean isSetupDone;

    @Autowired
    private SingleResponseRepository repository;

    @Autowired
    private CosmosTemplate template;

    @Before
    public void setUp() {
        if (!isSetupDone) {
            staticTemplate = template;
            template.createContainerIfNotExists(entityInformation);
        }
        repository.save(TEST_SINGLE_RESPONSE_ENTITY);
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
    public void testShouldFindSingleEntity() {
        final SingleResponseEntity entity =
            repository.findOneByEntityTitle(TEST_SINGLE_RESPONSE_ENTITY.getEntityTitle());

        Assert.assertEquals(TEST_SINGLE_RESPONSE_ENTITY, entity);
    }

    @Test
    public void testShouldFindSingleOptionalEntity() {
        final Optional<SingleResponseEntity> entity =
            repository.findOptionallyByEntityTitle(TEST_SINGLE_RESPONSE_ENTITY.getEntityTitle());
        Assert.assertTrue(entity.isPresent());
        Assert.assertEquals(TEST_SINGLE_RESPONSE_ENTITY, entity.get());

        Assert.assertFalse(repository.findOptionallyByEntityTitle("not here").isPresent());
    }

    @Test(expected = CosmosAccessException.class)
    public void testShouldFailIfMultipleResultsReturned() {
        repository.save(new SingleResponseEntity("testId2", TEST_SINGLE_RESPONSE_ENTITY.getEntityTitle()));

        repository.findOneByEntityTitle(TEST_SINGLE_RESPONSE_ENTITY.getEntityTitle());
    }

    @Test
    public void testShouldAllowListAndIterableResponses() {
        final List<SingleResponseEntity> entityList =
            repository.findByEntityTitle(TEST_SINGLE_RESPONSE_ENTITY.getEntityTitle());
        Assert.assertEquals(TEST_SINGLE_RESPONSE_ENTITY, entityList.get(0));
        Assert.assertEquals(1, entityList.size());

        final Iterator<SingleResponseEntity> entityIterator =
            repository.findByEntityId(TEST_SINGLE_RESPONSE_ENTITY.getEntityId()).iterator();
        Assert.assertTrue(entityIterator.hasNext());
        Assert.assertEquals(TEST_SINGLE_RESPONSE_ENTITY, entityIterator.next());
        Assert.assertFalse(entityIterator.hasNext());
    }

}

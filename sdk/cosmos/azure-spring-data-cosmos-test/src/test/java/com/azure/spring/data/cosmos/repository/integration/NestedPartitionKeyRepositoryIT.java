// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.common.TestUtils;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.NestedEntity;
import com.azure.spring.data.cosmos.domain.NestedPartitionKeyEntityWithGeneratedValue;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.NestedPartitionKeyRepository;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class NestedPartitionKeyRepositoryIT {

    private static final NestedPartitionKeyEntityWithGeneratedValue NESTED_ENTITY_1 =
        new NestedPartitionKeyEntityWithGeneratedValue(null, new NestedEntity("partitionKey1"));

    private static final NestedPartitionKeyEntityWithGeneratedValue NESTED_ENTITY_2 =
        new NestedPartitionKeyEntityWithGeneratedValue(null, new NestedEntity("partitionKey2"));

    private static final CosmosEntityInformation<NestedPartitionKeyEntityWithGeneratedValue, String> entityInformation =
        new CosmosEntityInformation<>(NestedPartitionKeyEntityWithGeneratedValue.class);

    private static CosmosTemplate staticTemplate;
    private static boolean isSetupDone;

    @Autowired
    private CosmosTemplate template;

    @Autowired
    NestedPartitionKeyRepository repository;

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
        NestedPartitionKeyEntityWithGeneratedValue savedEntity = repository.save(NESTED_ENTITY_1);

        assertThat(savedEntity).isEqualTo(NESTED_ENTITY_1);
    }

    @Test
    public void testFindAll() {
        repository.saveAll(Arrays.asList(NESTED_ENTITY_1, NESTED_ENTITY_2));

        Iterable<NestedPartitionKeyEntityWithGeneratedValue> iterable = repository.findAll();
        List<NestedPartitionKeyEntityWithGeneratedValue> nestedPartitionKeyEntityWithGeneratedValues =
            TestUtils.toList(iterable);
        assertThat(nestedPartitionKeyEntityWithGeneratedValues.size()).isEqualTo(2);
    }

    @Test
    public void testFindAllByPartitionKey() {
        repository.saveAll(Arrays.asList(NESTED_ENTITY_1, NESTED_ENTITY_2));

        Iterable<NestedPartitionKeyEntityWithGeneratedValue> partitionKey = repository.findAll(new PartitionKey(
            "partitionKey2"));
        assertThat(partitionKey.iterator().hasNext()).isTrue();
        assertThat(partitionKey.iterator().next()).isEqualTo(NESTED_ENTITY_2);
    }

    @Test
    public void testFindByIdAndPartitionKey() {
        repository.saveAll(Arrays.asList(NESTED_ENTITY_1, NESTED_ENTITY_2));

        Optional<NestedPartitionKeyEntityWithGeneratedValue> nestedEntity = repository.findById(NESTED_ENTITY_1.getId(),
            new PartitionKey(NESTED_ENTITY_1.getNestedEntity().getNestedPartitionKey()));
        assertThat(nestedEntity.isPresent()).isTrue();
        assertThat(nestedEntity.get()).isEqualTo(NESTED_ENTITY_1);
    }

    @Test
    public void testDeleteByIdAndPartitionKey() {
        repository.saveAll(Arrays.asList(NESTED_ENTITY_1, NESTED_ENTITY_2));

        repository.deleteById(NESTED_ENTITY_1.getId(), new PartitionKey(NESTED_ENTITY_1.getNestedEntity().getNestedPartitionKey()));
        Iterable<NestedPartitionKeyEntityWithGeneratedValue> iterable = repository.findAll();
        List<NestedPartitionKeyEntityWithGeneratedValue> nestedPartitionKeyEntityWithGeneratedValues =
            TestUtils.toList(iterable);
        assertThat(nestedPartitionKeyEntityWithGeneratedValues.size()).isEqualTo(1);
        assertThat(nestedPartitionKeyEntityWithGeneratedValues.get(0)).isEqualTo(NESTED_ENTITY_2);
    }

    @Test
    public void testDeleteByEntity() {
        repository.saveAll(Arrays.asList(NESTED_ENTITY_1, NESTED_ENTITY_2));

        repository.delete(NESTED_ENTITY_1);
        Iterable<NestedPartitionKeyEntityWithGeneratedValue> iterable = repository.findAll();
        List<NestedPartitionKeyEntityWithGeneratedValue> nestedPartitionKeyEntityWithGeneratedValues =
            TestUtils.toList(iterable);
        assertThat(nestedPartitionKeyEntityWithGeneratedValues.size()).isEqualTo(1);
        assertThat(nestedPartitionKeyEntityWithGeneratedValues.get(0)).isEqualTo(NESTED_ENTITY_2);
    }

}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.integration;

import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.UniqueKey;
import com.azure.cosmos.models.UniqueKeyPolicy;
import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.domain.CompositeIndexEntity;
import com.azure.spring.data.cosmos.domain.UniqueKeyPolicyEntity;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.UniqueKeyPolicyEntityRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.azure.spring.data.cosmos.repository.support.SimpleCosmosRepository;
import com.azure.spring.data.cosmos.repository.support.SimpleReactiveCosmosRepository;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class UniqueKeyPolicyIT {

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    private static final UniqueKeyPolicyEntity ENTITY_1 = new UniqueKeyPolicyEntity("id-1", "firstName-1", "lastName"
        + "-1", "100", "city-1");
    private static final UniqueKeyPolicyEntity ENTITY_2 = new UniqueKeyPolicyEntity("id-2", "firstName-1", "lastName"
        + "-2", "100", "city-2");
    private static final UniqueKeyPolicyEntity ENTITY_3 = new UniqueKeyPolicyEntity("id-3", "firstName-2", "lastName"
        + "-3", "100", "city-1");
    private static final UniqueKeyPolicyEntity ENTITY_4 = new UniqueKeyPolicyEntity("id-4", "firstName-2", "lastName"
        + "-4", "100", "city-2");
    private static final UniqueKeyPolicyEntity ENTITY_5 = new UniqueKeyPolicyEntity("id-5", "firstName-3", "lastName"
        + "-5", "100", "city-3");

    @Autowired
    UniqueKeyPolicyEntityRepository repository;

    @Autowired
    CosmosTemplate template;

    @Autowired
    ReactiveCosmosTemplate reactiveTemplate;

    CosmosEntityInformation<UniqueKeyPolicyEntity, String> information =
        new CosmosEntityInformation<>(UniqueKeyPolicyEntity.class);

    @Before
    public void setup() {
        collectionManager.ensureContainersCreatedAndEmpty(template, CompositeIndexEntity.class);
        repository.saveAll(Arrays.asList(ENTITY_1, ENTITY_2, ENTITY_3, ENTITY_4, ENTITY_5));
    }

    @Test
    public void canSetUniqueKeyPolicy() {
        new SimpleCosmosRepository<>(information, template);
        CosmosContainerProperties properties = template.getContainerProperties(information.getContainerName());
        UniqueKeyPolicy uniqueKeyPolicy = properties.getUniqueKeyPolicy();
        List<UniqueKey> uniqueKeys = uniqueKeyPolicy.getUniqueKeys();

        assertThat(uniqueKeys.size()).isEqualTo(2);

        assertThat(uniqueKeys.get(0).getPaths().get(0)).isEqualTo("/lastName");
        assertThat(uniqueKeys.get(0).getPaths().get(1)).isEqualTo("/zipCode");

        assertThat(uniqueKeys.get(1).getPaths().get(0)).isEqualTo("/city");
    }

    @Test
    public void canSetUniqueKeyPolicyReactive() {
        new SimpleReactiveCosmosRepository<>(information, reactiveTemplate);
        CosmosContainerProperties properties =
            reactiveTemplate.getContainerProperties(information.getContainerName()).block();
        UniqueKeyPolicy uniqueKeyPolicy = properties.getUniqueKeyPolicy();
        List<UniqueKey> uniqueKeys = uniqueKeyPolicy.getUniqueKeys();

        assertThat(uniqueKeys.size()).isEqualTo(2);

        assertThat(uniqueKeys.get(0).getPaths().get(0)).isEqualTo("/lastName");
        assertThat(uniqueKeys.get(0).getPaths().get(1)).isEqualTo("/zipCode");

        assertThat(uniqueKeys.get(1).getPaths().get(0)).isEqualTo("/city");
    }

    @Test
    public void canSaveNewEntityWithDifferentUniqueKeys() {
        long count = repository.count();
        assertThat(count).isEqualTo(5);
        UniqueKeyPolicyEntity entity = new UniqueKeyPolicyEntity("id-6", "firstName-3", "lastName-6",
            "100", "city-1");
        repository.save(entity);
        count = repository.count();
        assertThat(count).isEqualTo(6);
        repository.deleteById("id-6", new PartitionKey("firstName-3"));
        count = repository.count();
        assertThat(count).isEqualTo(5);
    }

    @Test
    public void cannotSaveNewEntityWithUniqueKeysLastNameAndZipCode() {
        //  save with same lastName and zip code (which already exists in the same logical partition), though with a new id
        UniqueKeyPolicyEntity entity = new UniqueKeyPolicyEntity("id-6", "firstName-3", "lastName-5",
            "100", "city-6");
        try {
            repository.save(entity);
            fail("Save call should have failed with unique constraints exception");
        } catch (CosmosAccessException cosmosAccessException) {
            assertThat(cosmosAccessException.getCosmosException().getStatusCode()).isEqualTo(409);
            assertThat(cosmosAccessException.getCosmosException().getMessage()).contains("Unique index constraint "
                + "violation.");
        }
        //  change logical partition, now the entity should be saved
        entity.setFirstName("firstName-2");
        repository.save(entity);
        long count = repository.count();
        assertThat(count).isEqualTo(6);
        repository.deleteById("id-6", new PartitionKey("firstName-2"));
        count = repository.count();
        assertThat(count).isEqualTo(5);
    }

    @Test
    public void cannotSaveNewEntityWithUniqueKeysCity() {
        //  save with same city (which already exists in the same logical partition), though with a new id
        UniqueKeyPolicyEntity entity = new UniqueKeyPolicyEntity("id-6", "firstName-3", "lastName-6",
            "100", "city-3");
        try {
            repository.save(entity);
            fail("Save call should have failed with unique constraints exception");
        } catch (CosmosAccessException cosmosAccessException) {
            assertThat(cosmosAccessException.getCosmosException().getStatusCode()).isEqualTo(409);
            assertThat(cosmosAccessException.getCosmosException().getMessage()).contains("Unique index constraint "
                + "violation.");
        }
        //  change logical partition, now the entity should be saved
        entity.setFirstName("firstName-2");
        repository.save(entity);
        long count = repository.count();
        assertThat(count).isEqualTo(6);
        repository.deleteById("id-6", new PartitionKey("firstName-2"));
        count = repository.count();
        assertThat(count).isEqualTo(5);
    }

}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.spring.data.cosmos.core.CosmosOperations;
import com.azure.spring.data.cosmos.domain.Person;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.azure.spring.data.cosmos.repository.support.SimpleCosmosRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SimpleCosmosRepositoryIllegalTest {
    private SimpleCosmosRepository<Person, String> repository;

    @Mock
    CosmosOperations dbOperations;
    @Mock
    CosmosEntityInformation<Person, String> entityInformation;

    @BeforeEach
    public void setUp() {
        CosmosContainerProperties containerProperties = new CosmosContainerProperties("", new PartitionKeyDefinition());
        repository = new SimpleCosmosRepository<>(entityInformation, dbOperations);
    }

    @Test
    public void deleteNullShouldFail() {
        repository.delete(null);
    }

    @Test
    public void deleteIterableNullShouldFail() {
        repository.deleteAll(null);
    }

    @Test
    public void deleteNullIdShouldFail() {
        repository.deleteById(null);
    }

    @Test
    public void existsNullIdShouldFail() {
        repository.existsById(null);
    }

    @Test
    public void findNullIterableIdsShouldFail() {
        repository.findAllById(null);
    }

    @Test
    public void findByNullIdShouldFail() {
        repository.findById(null);
    }

    @Test
    public void saveNullShouldFail() {
        repository.save(null);
    }

    @Test
    public void saveNullIterableShouldFail() {
        repository.saveAll(null);
    }
}

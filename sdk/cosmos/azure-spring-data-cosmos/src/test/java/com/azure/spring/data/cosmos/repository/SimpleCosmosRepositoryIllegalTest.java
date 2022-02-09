// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.spring.data.cosmos.core.CosmosOperations;
import com.azure.spring.data.cosmos.domain.Person;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.azure.spring.data.cosmos.repository.support.SimpleCosmosRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SimpleCosmosRepositoryIllegalTest {
    private SimpleCosmosRepository<Person, String> repository;

    @Mock
    CosmosOperations dbOperations;
    @Mock
    CosmosEntityInformation<Person, String> entityInformation;

    @Before
    public void setUp() {
        CosmosContainerProperties containerProperties = new CosmosContainerProperties("", new PartitionKeyDefinition());
        when(entityInformation.getIndexingPolicy()).thenReturn(new IndexingPolicy());
        when(dbOperations.getContainerProperties(any())).thenReturn(containerProperties);
        repository = new SimpleCosmosRepository<>(entityInformation, dbOperations);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteNullShouldFail() {
        repository.delete(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteIterableNullShouldFail() {
        repository.deleteAll(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteNullIdShouldFail() {
        repository.deleteById(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void existsNullIdShouldFail() {
        repository.existsById(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findNullIterableIdsShouldFail() {
        repository.findAllById(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findByNullIdShouldFail() {
        repository.findById(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveNullShouldFail() {
        repository.save(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveNullIterableShouldFail() {
        repository.saveAll(null);
    }
}

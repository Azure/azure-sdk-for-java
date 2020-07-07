// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository;

import com.microsoft.azure.spring.data.cosmosdb.core.CosmosOperations;
import com.microsoft.azure.spring.data.cosmosdb.domain.Person;
import com.microsoft.azure.spring.data.cosmosdb.repository.support.CosmosEntityInformation;
import com.microsoft.azure.spring.data.cosmosdb.repository.support.SimpleCosmosRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SimpleCosmosRepositoryIllegalTest {
    private SimpleCosmosRepository<Person, String> repository;

    @Mock
    CosmosOperations dbOperations;
    @Mock
    CosmosEntityInformation<Person, String> entityInformation;

    @Before
    public void setUp() {
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

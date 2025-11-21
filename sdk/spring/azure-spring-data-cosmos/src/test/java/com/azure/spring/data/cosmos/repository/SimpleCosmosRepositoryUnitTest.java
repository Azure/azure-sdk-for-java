// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository;

import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.CosmosOperations;
import com.azure.spring.data.cosmos.domain.Address;
import com.azure.spring.data.cosmos.domain.Person;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.azure.spring.data.cosmos.repository.support.SimpleCosmosRepository;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SimpleCosmosRepositoryUnitTest {
    private static final Person TEST_PERSON =
            new Person(TestConstants.ID_1, TestConstants.FIRST_NAME, TestConstants.LAST_NAME,
                    TestConstants.HOBBIES, TestConstants.ADDRESSES);

    private static final String PARTITION_VALUE_REQUIRED_MSG =
            "PartitionKey value must be supplied for this operation.";

    private SimpleCosmosRepository<Person, String> repository;
    @Mock
    CosmosOperations cosmosOperations;
    @Mock
    CosmosEntityInformation<Person, String> entityInformation;

    

    @BeforeEach
    public void setUp() {
        when(entityInformation.getJavaType()).thenReturn(Person.class);
        when(entityInformation.getContainerName()).thenReturn(Person.class.getSimpleName());
        when(cosmosOperations.findAll(anyString(), any())).thenReturn(Arrays.asList(TEST_PERSON));

        repository = new SimpleCosmosRepository<>(entityInformation, cosmosOperations);
    }

    @Test
    public void testSave() {
        repository.save(TEST_PERSON);

        final List<Person> result = Lists.newArrayList(repository.findAll());
        assertEquals(1, result.size());
        assertEquals(TEST_PERSON, result.get(0));
    }

    @Test
    public void testFindOne() {
        when(cosmosOperations.findById(anyString(), anyString(), any())).thenReturn(TEST_PERSON);

        repository.save(TEST_PERSON);

        final Person result = repository.findById(TEST_PERSON.getId()).get();
        assertEquals(TEST_PERSON, result);
    }

    @Test
    public void testFindOneExceptionForPartitioned() {
        repository.save(TEST_PERSON);

        when(cosmosOperations.findById(anyString(), anyString(), any()))
                .thenThrow(new UnsupportedOperationException(PARTITION_VALUE_REQUIRED_MSG));

        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> {
            repository.findById(TEST_PERSON.getId()).get();
        });
        assertEquals(PARTITION_VALUE_REQUIRED_MSG, exception.getMessage());
    }

    @Test
    public void testUpdate() {
        final List<Address> updatedAddress =
                Arrays.asList(new Address(TestConstants.POSTAL_CODE, TestConstants.UPDATED_CITY,
                        TestConstants.UPDATED_STREET));
        final Person updatedPerson =
                new Person(TEST_PERSON.getId(), TestConstants.UPDATED_FIRST_NAME, TestConstants.UPDATED_LAST_NAME,
                        TestConstants.UPDATED_HOBBIES, updatedAddress);
        repository.save(updatedPerson);

        when(cosmosOperations.findById(anyString(), anyString(), any())).thenReturn(updatedPerson);

        final Person result = repository.findById(TEST_PERSON.getId()).get();
        assertEquals(updatedPerson, result);
    }
}

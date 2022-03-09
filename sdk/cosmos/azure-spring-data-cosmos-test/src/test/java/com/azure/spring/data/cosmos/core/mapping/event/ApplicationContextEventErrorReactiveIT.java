// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.mapping.event;

import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.Address;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.AddressRepository;
import com.azure.spring.data.cosmos.repository.repository.ReactiveAddressRepository;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.azure.spring.data.cosmos.domain.Address.TEST_ADDRESS1_PARTITION1;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestRepositoryConfig.class, ErrorEventListenerConfig.class})
public class ApplicationContextEventErrorReactiveIT {

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    private ReactiveAddressRepository repository;
    @Autowired
    private CosmosTemplate template;

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, Address.class);
        repository.saveAll(Lists.newArrayList(TEST_ADDRESS1_PARTITION1)).collectList().block();
    }

    @Test(expected = CosmosAccessException.class)
    public void shouldThrowExceptionIfEventListenerThrowsException() {
        repository.findById(TEST_ADDRESS1_PARTITION1.getPostalCode(), new PartitionKey(TEST_ADDRESS1_PARTITION1.getCity())).block();
    }
}

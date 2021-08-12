// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.mapping.event;

import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.Address;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.AddressRepository;
import com.azure.spring.data.cosmos.repository.repository.ReactiveAddressRepository;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static com.azure.spring.data.cosmos.domain.Address.TEST_ADDRESS1_PARTITION1;
import static com.azure.spring.data.cosmos.domain.Address.TEST_ADDRESS1_PARTITION2;
import static com.azure.spring.data.cosmos.domain.Address.TEST_ADDRESS2_PARTITION1;
import static com.azure.spring.data.cosmos.domain.Address.TEST_ADDRESS4_PARTITION3;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ApplicationContextEventReactiveTest {

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    private ReactiveAddressRepository repository;
    @Autowired
    private CosmosTemplate template;
    @Autowired
    private SimpleMappingEventListener simpleMappingEventListener;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, Address.class);
        repository.saveAll(Lists.newArrayList(TEST_ADDRESS1_PARTITION1, TEST_ADDRESS1_PARTITION2,
            TEST_ADDRESS2_PARTITION1)).collectList().block();
        simpleMappingEventListener.onAfterLoadEvents = new ArrayList<>();
    }

    @Test
    public void shouldPublishAfterLoadEventOnFindById() {
        repository.findById(TEST_ADDRESS1_PARTITION1.getPostalCode(), new PartitionKey(TEST_ADDRESS1_PARTITION1.getCity())).block();
        assertThat(simpleMappingEventListener.onAfterLoadEvents).hasSize(1);
        assertThat(simpleMappingEventListener.onAfterLoadEvents.get(0).getContainerName()).isEqualTo("Address");
    }

    @Test
    public void shouldPublishAfterLoadEventOnFindAll() {
        repository.findAll().collectList().block();
        assertThat(simpleMappingEventListener.onAfterLoadEvents).hasSize(3);
        assertThat(simpleMappingEventListener.onAfterLoadEvents.get(0).getContainerName()).isEqualTo("Address");
        assertThat(simpleMappingEventListener.onAfterLoadEvents.get(1).getContainerName()).isEqualTo("Address");
        assertThat(simpleMappingEventListener.onAfterLoadEvents.get(2).getContainerName()).isEqualTo("Address");
    }

    @Test
    public void shouldPublishAfterLoadEventForCustomQueries() {
        List<String> cities = new ArrayList<>();
        cities.add(TEST_ADDRESS1_PARTITION1.getCity());
        repository.findByCityIn(cities).collectList().block();
        assertThat(simpleMappingEventListener.onAfterLoadEvents).hasSize(2);
        assertThat(simpleMappingEventListener.onAfterLoadEvents.get(0).getContainerName()).isEqualTo("Address");
        assertThat(simpleMappingEventListener.onAfterLoadEvents.get(1).getContainerName()).isEqualTo("Address");
    }

    @Test
    public void shouldPublishAfterLoadEventForAnnotatedCustomQueries() {
        repository.annotatedFindListByCity(TEST_ADDRESS1_PARTITION1.getCity()).collectList().block();
        assertThat(simpleMappingEventListener.onAfterLoadEvents).hasSize(2);
        assertThat(simpleMappingEventListener.onAfterLoadEvents.get(0).getContainerName()).isEqualTo("Address");
        assertThat(simpleMappingEventListener.onAfterLoadEvents.get(1).getContainerName()).isEqualTo("Address");
    }

    @Test
    public void shouldNotPublishAfterLoadEventForInserts() {
        repository.save(TEST_ADDRESS4_PARTITION3).block();
        assertThat(simpleMappingEventListener.onAfterLoadEvents.isEmpty()).isTrue();
    }

    @Test
    public void shouldNotPublishAfterLoadEventForUpdates() {
        repository.save(new Address(TEST_ADDRESS1_PARTITION1.getPostalCode(), TestConstants.STREET_0, TEST_ADDRESS1_PARTITION1.getCity())).block();
        assertThat(simpleMappingEventListener.onAfterLoadEvents.isEmpty()).isTrue();
    }

    @Test
    public void shouldNotPublishAfterLoadEventForDeletes() {
        repository.delete(TEST_ADDRESS1_PARTITION1).block();
        assertThat(simpleMappingEventListener.onAfterLoadEvents.isEmpty()).isTrue();
    }

    @Test
    public void shouldNotPublishAfterLoadEventForCustomDeleteQuery() {
        repository.deleteByCity(TEST_ADDRESS1_PARTITION1.getCity()).block();
        assertThat(simpleMappingEventListener.onAfterLoadEvents.isEmpty()).isTrue();
    }
}

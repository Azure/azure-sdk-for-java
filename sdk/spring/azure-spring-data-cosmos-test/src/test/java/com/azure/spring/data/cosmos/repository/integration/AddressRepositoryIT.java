// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.common.TestUtils;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.Address;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.AddressRepository;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.azure.spring.data.cosmos.domain.Address.TEST_ADDRESS1_PARTITION1;
import static com.azure.spring.data.cosmos.domain.Address.TEST_ADDRESS1_PARTITION2;
import static com.azure.spring.data.cosmos.domain.Address.TEST_ADDRESS2_PARTITION1;
import static com.azure.spring.data.cosmos.domain.Address.TEST_ADDRESS4_PARTITION3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class AddressRepositoryIT {

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    AddressRepository repository;

    @Autowired
    private CosmosTemplate template;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, Address.class);
        repository.saveAll(Lists.newArrayList(TEST_ADDRESS1_PARTITION1, TEST_ADDRESS1_PARTITION2,
            TEST_ADDRESS2_PARTITION1, TEST_ADDRESS4_PARTITION3));
    }

    @Test
    public void testFindAll() {
        // findAll cross partition
        final List<Address> result = TestUtils.toList(repository.findAll());

        assertThat(result.size()).isEqualTo(4);
    }

    @Test
    public void testFindByIdWithPartitionKey() {
        final Optional<Address> addressById = repository.findById(TEST_ADDRESS1_PARTITION1.getPostalCode(),
            new PartitionKey(collectionManager.getEntityInformation(Address.class).getPartitionKeyFieldValue(TEST_ADDRESS1_PARTITION1)));

        if (!addressById.isPresent()) {
            fail("address not found");
            return;
        }
        assertThat(addressById.get()).isEqualTo(TEST_ADDRESS1_PARTITION1);
    }

    @Test
    public void testFindByIdForPartitionedCollection() {
        final List<Address> addresses = TestUtils.toList(repository.findByPostalCode(TestConstants.POSTAL_CODE));

        assertThat(addresses.size()).isEqualTo(2);
        assertThat(addresses.get(0).getPostalCode()).isEqualTo(TestConstants.POSTAL_CODE);
        assertThat(addresses.get(1).getPostalCode()).isEqualTo(TestConstants.POSTAL_CODE);
    }

    @Test
    public void testFindByPartitionedCity() {
        final String city = TEST_ADDRESS1_PARTITION1.getCity();
        final List<Address> result = TestUtils.toList(repository.findByCity(city));

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getCity()).isEqualTo(city);
        assertThat(result.get(1).getCity()).isEqualTo(city);
    }

    @Test
    public void testFindByPartitionedCityIn() {
        final String city = TEST_ADDRESS1_PARTITION1.getCity();
        final List<Address> result = TestUtils.toList(repository.findByCityIn(Lists.newArrayList(city)));

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getCity()).isEqualTo(city);
        assertThat(result.get(1).getCity()).isEqualTo(city);
    }

    @Test
    public void testFindByPostalCodeAndCityIn() {
        final String city = TEST_ADDRESS1_PARTITION1.getCity();
        final List<String> postalCodes = Lists.newArrayList(TEST_ADDRESS1_PARTITION1.getPostalCode(),
            TEST_ADDRESS2_PARTITION1.getPostalCode());
        final List<Address> result = TestUtils.toList(repository.findByPostalCodeInAndCity(postalCodes, city));

        assertThat(result.size()).isEqualTo(2);
        assertThat(result).isEqualTo(Lists.newArrayList(TEST_ADDRESS1_PARTITION1, TEST_ADDRESS2_PARTITION1));
    }

    @Test
    public void testFindByStreetOrCity() {
        final String city = TEST_ADDRESS1_PARTITION1.getCity();
        final String street = TEST_ADDRESS1_PARTITION2.getStreet();

        final List<Address> result = TestUtils.toList(repository.findByStreetOrCity(street, city));
        final List<Address> reference = Arrays.asList(
            TEST_ADDRESS1_PARTITION1, TEST_ADDRESS1_PARTITION2, TEST_ADDRESS2_PARTITION1);

        result.sort(Comparator.comparing(Address::getPostalCode));
        reference.sort(Comparator.comparing(Address::getPostalCode));

        Assert.assertEquals(reference.size(), result.size());
        Assert.assertEquals(reference, result);
    }

    @Test
    public void testCount() {
        final long count = repository.count();
        assertThat(count).isEqualTo(4);

        repository.deleteByCity(TestConstants.CITY);
        final long newCount = repository.count();
        assertThat(newCount).isEqualTo(2);
    }

    @Test
    public void deleteWithoutPartitionedColumnShouldFail() {
        expectedException.expect(Exception.class);

        repository.deleteById(TEST_ADDRESS1_PARTITION1.getPostalCode());
    }

    @Test
    public void canDeleteByIdAndPartitionedCity() {
        final long count = repository.count();
        assertThat(count).isEqualTo(4);

        repository.deleteByPostalCodeAndCity(
            TEST_ADDRESS1_PARTITION1.getPostalCode(), TEST_ADDRESS1_PARTITION1.getCity());

        final List<Address> result = TestUtils.toList(repository.findAll());

        assertThat(result.size()).isEqualTo(3);
    }

    @Test
    public void canDeleteByPartitionedCity() {
        final long count = repository.count();
        assertThat(count).isEqualTo(4);

        repository.deleteByCity(TEST_ADDRESS1_PARTITION1.getCity());

        final List<Address> result = TestUtils.toList(repository.findAll());

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getCity()).isNotEqualTo(TEST_ADDRESS1_PARTITION1.getCity());
    }

    @Test
    public void testDeleteByIdAndPartitionKey() {
        final long count = repository.count();
        assertThat(count).isEqualTo(4);

        Optional<Address> addressById = repository.findById(TEST_ADDRESS1_PARTITION1.getPostalCode(),
            new PartitionKey(TEST_ADDRESS1_PARTITION1.getCity()));
        assertThat(addressById.isPresent()).isTrue();

        repository.deleteById(TEST_ADDRESS1_PARTITION1.getPostalCode(),
            new PartitionKey(TEST_ADDRESS1_PARTITION1.getCity()));

        final List<Address> result = TestUtils.toList(repository.findAll());
        assertThat(result.size()).isEqualTo(3);

        addressById = repository.findById(TEST_ADDRESS1_PARTITION1.getPostalCode(),
            new PartitionKey(TEST_ADDRESS1_PARTITION1.getCity()));

        assertThat(addressById.isPresent()).isFalse();
    }

    @Test
    public void testFindAllByPartitionKey() {
        List<Address> findAll =
            TestUtils.toList(repository.findAll(new PartitionKey(TEST_ADDRESS1_PARTITION1.getCity())));
        //  Since there are two addresses with partition1
        assertThat(findAll.size()).isEqualTo(2);
        assertThat(findAll.containsAll(Lists.newArrayList(TEST_ADDRESS1_PARTITION1,
            TEST_ADDRESS2_PARTITION1))).isTrue();

        findAll = TestUtils.toList(repository.findAll(new PartitionKey(TEST_ADDRESS1_PARTITION2.getCity())));
        //  Since there is one address with partition2
        assertThat(findAll.size()).isEqualTo(1);
        assertThat(findAll.contains(TEST_ADDRESS1_PARTITION2)).isTrue();


        findAll = TestUtils.toList(repository.findAll(new PartitionKey(TEST_ADDRESS4_PARTITION3.getCity())));
        //  Since there is one address with partition3
        assertThat(findAll.size()).isEqualTo(1);
        assertThat(findAll.contains(TEST_ADDRESS4_PARTITION3)).isTrue();
    }

    @Test
    public void testUpdateEntity() {
        final Address updatedAddress = new Address(TEST_ADDRESS1_PARTITION1.getPostalCode(), TestConstants.NEW_STREET,
            TEST_ADDRESS1_PARTITION1.getCity());

        repository.save(updatedAddress);

        final List<Address> results =
            TestUtils.toList(repository.findByPostalCodeAndCity(updatedAddress.getPostalCode(),
                updatedAddress.getCity()));

        assertThat(results.size()).isEqualTo(1);
        assertThat(results.get(0).getStreet()).isEqualTo(updatedAddress.getStreet());
        assertThat(results.get(0).getPostalCode()).isEqualTo(updatedAddress.getPostalCode());
    }
}

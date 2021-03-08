// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.common.TestUtils;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import com.azure.spring.data.cosmos.domain.Address;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.PageableAddressRepository;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static com.azure.spring.data.cosmos.common.PageTestUtils.validateLastPage;
import static com.azure.spring.data.cosmos.common.PageTestUtils.validateNonLastPage;
import static com.azure.spring.data.cosmos.common.TestConstants.PAGE_SIZE_1;
import static com.azure.spring.data.cosmos.common.TestConstants.PAGE_SIZE_3;
import static com.azure.spring.data.cosmos.domain.Address.TEST_ADDRESS1_PARTITION1;
import static com.azure.spring.data.cosmos.domain.Address.TEST_ADDRESS1_PARTITION2;
import static com.azure.spring.data.cosmos.domain.Address.TEST_ADDRESS2_PARTITION1;
import static com.azure.spring.data.cosmos.domain.Address.TEST_ADDRESS4_PARTITION3;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class PageableAddressRepositoryIT {

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    private CosmosTemplate template;

    @Autowired
    private PageableAddressRepository repository;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CosmosFactory cosmosFactory;

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, Address.class);
        repository.save(TEST_ADDRESS1_PARTITION1);
        repository.save(TEST_ADDRESS1_PARTITION2);
        repository.save(TEST_ADDRESS2_PARTITION1);
        repository.save(TEST_ADDRESS4_PARTITION3);
        repository.save(new Address(TestConstants.POSTAL_CODE, TestConstants.STREET, TestConstants.CITY_0));
    }

    @Test
    public void testFindAll() {
        final List<Address> result = TestUtils.toList(repository.findAll());

        assertThat(result.size()).isEqualTo(5);
    }

    @Test
    public void testFindAllByPage() {
        final CosmosPageRequest pageRequest = new CosmosPageRequest(0, PAGE_SIZE_3, null);
        final Page<Address> page = repository.findAll(pageRequest);

        assertThat(page.getContent().size()).isLessThanOrEqualTo(PAGE_SIZE_3);
        validateNonLastPage(page, PAGE_SIZE_3);

        final Page<Address> nextPage = repository.findAll(page.nextPageable());
        assertThat(nextPage.getContent().size()).isLessThanOrEqualTo(PAGE_SIZE_3);
        validateLastPage(nextPage, nextPage.getContent().size());
    }

    @Test
    public void testFindWithPartitionKeySinglePage() {
        final CosmosPageRequest pageRequest = new CosmosPageRequest(0, PAGE_SIZE_3, null);
        final Page<Address> page = repository.findByCity(TestConstants.CITY, pageRequest);

        assertThat(page.getContent().size()).isEqualTo(2);
        validateResultCityMatch(page, TestConstants.CITY);
        validateLastPage(page, page.getContent().size());
    }

    @Test
    public void testFindWithPartitionKeyMultiPages() {
        final CosmosPageRequest pageRequest = new CosmosPageRequest(0, PAGE_SIZE_1, null);
        final Page<Address> page = repository.findByCity(TestConstants.CITY, pageRequest);

        assertThat(page.getContent().size()).isEqualTo(PAGE_SIZE_1);
        validateResultCityMatch(page, TestConstants.CITY);
        validateNonLastPage(page, PAGE_SIZE_1);

        final Page<Address> nextPage = repository.findByCity(TestConstants.CITY, page.nextPageable());

        assertThat(nextPage.getContent().size()).isEqualTo(PAGE_SIZE_1);
        validateResultCityMatch(page, TestConstants.CITY);
        validateLastPage(nextPage, PAGE_SIZE_1);
    }

    @Test
    public void testFindWithoutPartitionKeySinglePage() {
        final CosmosPageRequest pageRequest = new CosmosPageRequest(0, PAGE_SIZE_3, null);
        final Page<Address> page = repository.findByStreet(TestConstants.STREET, pageRequest);

        assertThat(page.getContent().size()).isEqualTo(2);
        validateResultStreetMatch(page, TestConstants.STREET);
        validateLastPage(page, page.getContent().size());
    }

    @Test
    public void testFindWithoutPartitionKeyMultiPages() {
        final CosmosPageRequest pageRequest = new CosmosPageRequest(0, PAGE_SIZE_1, null);
        final Page<Address> page = repository.findByStreet(TestConstants.STREET, pageRequest);

        assertThat(page.getContent().size()).isEqualTo(1);
        validateResultStreetMatch(page, TestConstants.STREET);
        validateNonLastPage(page, PAGE_SIZE_1);

        final Page<Address> nextPage = repository.findByStreet(TestConstants.STREET, page.nextPageable());

        assertThat(nextPage.getContent().size()).isEqualTo(PAGE_SIZE_1);
        validateResultStreetMatch(page, TestConstants.STREET);
        validateLastPage(nextPage, PAGE_SIZE_1);
    }

    @Test
    public void testOffsetAndLimit() {
        final int skipCount = 2;
        final int takeCount = 2;
        final List<Address> results = new ArrayList<>();
        final CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setMaxDegreeOfParallelism(2);

        final String query = "SELECT * from c OFFSET " + skipCount + " LIMIT " + takeCount;

        final CosmosAsyncClient cosmosAsyncClient = applicationContext.getBean(CosmosAsyncClient.class);
        final Flux<FeedResponse<Address>> feedResponseFlux =
            cosmosAsyncClient.getDatabase(cosmosFactory.getDatabaseName())
                        .getContainer(collectionManager.getContainerName(Address.class))
                        .queryItems(query, options, Address.class)
                        .byPage();

        StepVerifier.create(feedResponseFlux)
                    .consumeNextWith(cosmosItemPropertiesFeedResponse ->
                        results.addAll(cosmosItemPropertiesFeedResponse.getResults()))
                    .verifyComplete();
        assertThat(results.size()).isEqualTo(takeCount);
    }

    private void validateResultCityMatch(Page<Address> page, String city) {
        assertThat((int) page.getContent()
                            .stream()
                            .filter(address -> address.getCity().equals(city))
                            .count()).isEqualTo(page.getContent().size());
    }

    private void validateResultStreetMatch(Page<Address> page, String street) {
        assertThat((int) page.getContent()
                            .stream()
                            .filter(address -> address.getStreet().equals(street))
                            .count()).isEqualTo(page.getContent().size());
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import com.azure.spring.data.cosmos.domain.Address;
import com.azure.spring.data.cosmos.domain.AuditableEntity;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.AddressRepository;
import com.azure.spring.data.cosmos.repository.repository.AuditableRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.azure.spring.data.cosmos.common.PageTestUtils.validateLastPage;
import static com.azure.spring.data.cosmos.common.PageTestUtils.validateNonLastPage;
import static com.azure.spring.data.cosmos.common.TestConstants.PAGE_SIZE_1;
import static com.azure.spring.data.cosmos.common.TestConstants.PAGE_SIZE_2;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class AnnotatedQueryIT {

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    private CosmosTemplate template;

    @Autowired
    private AuditableRepository auditableRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, Address.class, AuditableEntity.class);
    }

    @Test
    public void testAnnotatedQuery() {
        addressRepository.saveAll(Arrays.asList(Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS1_PARTITION2));

        final List<Address> result = addressRepository.annotatedFindListByCity(Address.TEST_ADDRESS1_PARTITION1.getCity());
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0)).isEqualTo(Address.TEST_ADDRESS1_PARTITION1);
    }

    @Test
    public void testAnnotatedQueryWithOptionalParam() {
        addressRepository.saveAll(Arrays.asList(Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS1_PARTITION2));

        Optional<String> city = Optional.ofNullable(TestConstants.CITY);
        final List<Address> result = addressRepository.annotatedFindListByCityOptional(city);
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0)).isEqualTo(Address.TEST_ADDRESS1_PARTITION1);
    }

    @Test
    public void testAnnotatedQueryWithOptionalParamEmpty() {
        addressRepository.saveAll(Arrays.asList(Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS1_PARTITION2));

        final List<Address> result = addressRepository.annotatedFindListByCityOptional(Optional.empty());
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0)).isEqualTo(Address.TEST_ADDRESS1_PARTITION1);
        assertThat(result.get(1)).isEqualTo(Address.TEST_ADDRESS1_PARTITION2);
    }

    @Test
    public void testAnnotatedQueryWithReturnTypeContainingLocalDateTime() {
        final AuditableEntity entity = new AuditableEntity();
        entity.setId(UUID.randomUUID().toString());

        final AuditableEntity savedEntity = auditableRepository.save(entity);

        final List<AuditableEntity> result = auditableRepository.annotatedFindById(savedEntity.getId());
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo(entity.getId());
    }

    @Test
    public void testAnnotatedQueryWithPageable() {
        addressRepository.saveAll(Arrays.asList(Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1));

        final PageRequest pageRequest = CosmosPageRequest.of(0, PAGE_SIZE_1);
        final Page<Address> page = addressRepository.annotatedFindByCity(TestConstants.CITY, pageRequest);

        assertThat(page.getContent().size()).isEqualTo(PAGE_SIZE_1);
        validateResultStreetMatch(page, Address.TEST_ADDRESS1_PARTITION1.getStreet());
        validateNonLastPage(page, PAGE_SIZE_1);

        final Page<Address> nextPage = addressRepository.annotatedFindByCity(TestConstants.CITY, page.nextPageable());

        assertThat(nextPage.getContent().size()).isEqualTo(PAGE_SIZE_1);
        validateResultStreetMatch(nextPage, Address.TEST_ADDRESS2_PARTITION1.getStreet());
        validateLastPage(nextPage, PAGE_SIZE_1);
    }

    private void validateResultStreetMatch(Page<Address> page, String street) {
        for (Address result : page.getContent()) {
            assertThat(result.getStreet()).isEqualTo(street);
        }
    }

    @Test
    public void testAnnotatedQueryWithSort() {
        addressRepository.saveAll(Arrays.asList(Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1));

        final List<Address> resultsAsc = addressRepository.annotatedFindByCity(TestConstants.CITY, Sort.by(Sort.Direction.ASC, "street"));
        assertAddressOrder(resultsAsc, Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1);

        final List<Address> resultsDesc = addressRepository.annotatedFindByCity(TestConstants.CITY, Sort.by(Sort.Direction.DESC, "street"));
        assertAddressOrder(resultsDesc, Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION1);
    }

    @Test
    public void testAnnotatedQueryWithNewLinesInQuery() {
        addressRepository.saveAll(Arrays.asList(Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION2));

        final List<Address> resultsAsc = addressRepository.annotatedFindByCityWithSort(TestConstants.CITY,
            Sort.by(Sort.Direction.ASC, "postalCode"));
        assertAddressOrder(resultsAsc, Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION1);
    }

    @Test
    public void testAnnotatedQueryWithNewLinesInQuery2() {
        addressRepository.saveAll(Arrays.asList(Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION2));

        final List<Address> resultsAsc = addressRepository.annotatedFindByCityWithSort2(TestConstants.CITY,
            Sort.by(Sort.Direction.ASC, "postalCode"));
        assertAddressOrder(resultsAsc, Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION1);
    }

    @Test
    public void testAnnotatedQueryWithValueAsPage() {
        final List<Address> addresses = Arrays.asList(Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1);
        addressRepository.saveAll(addresses);

        final PageRequest cosmosPageRequest = CosmosPageRequest.of(0, 10);
        final Page<String> postalCodes = addressRepository.annotatedFindPostalCodeValuesByCity(TestConstants.CITY,
        cosmosPageRequest);

        assertAddressPostalCodesUnordered(postalCodes.getContent(), addresses);
    }

    @Test
    public void testAnnotatedQueryWithValueAsPageTwoPages() {
        Address testAddress = new Address(TestConstants.POSTAL_CODE_1, TestConstants.STREET_0, TestConstants.CITY);
        final List<Address> addresses = Arrays.asList(Address.TEST_ADDRESS1_PARTITION1,
            Address.TEST_ADDRESS2_PARTITION1, testAddress);
        addressRepository.saveAll(addresses);

        final PageRequest cosmosPageRequest = CosmosPageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "postalCode"));
        final Page<String> postalCodes = addressRepository.annotatedFindPostalCodeValuesByCity(TestConstants.CITY,
            cosmosPageRequest);
        assertAddressPostalCodesUnordered(postalCodes.getContent(),
            Arrays.asList(Address.TEST_ADDRESS2_PARTITION1, testAddress));

        final PageRequest cosmosPageRequest2 = cosmosPageRequest.next();
        final Page<String> postalCodes2 = addressRepository.annotatedFindPostalCodeValuesByCity(TestConstants.CITY,
            cosmosPageRequest2);
        assertAddressPostalCodesUnordered(postalCodes2.getContent(), Arrays.asList(Address.TEST_ADDRESS1_PARTITION1));
    }

    @Test
    public void testAnnotatedQueryWithValueAsPageFromPageOneToThree() {
        Address testAddress = new Address(TestConstants.POSTAL_CODE_1, TestConstants.STREET_0, TestConstants.CITY);
        final List<Address> addresses = Arrays.asList(Address.TEST_ADDRESS1_PARTITION1,
            Address.TEST_ADDRESS2_PARTITION1, testAddress);
        addressRepository.saveAll(addresses);

        final PageRequest cosmosPageRequest = CosmosPageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "postalCode"));
        final Page<String> postalCodes = addressRepository.annotatedFindPostalCodeValuesByCity(TestConstants.CITY,
            cosmosPageRequest);
        assertAddressPostalCodesUnordered(postalCodes.getContent(), Arrays.asList(Address.TEST_ADDRESS2_PARTITION1));

        final PageRequest cosmosPageRequest2 = CosmosPageRequest.of(2, 1, Sort.by(Sort.Direction.ASC, "postalCode"));
        final Page<String> postalCodes2 = addressRepository.annotatedFindPostalCodeValuesByCity(TestConstants.CITY,
            cosmosPageRequest2);
        assertAddressPostalCodesUnordered(postalCodes2.getContent(), Arrays.asList(Address.TEST_ADDRESS1_PARTITION1));
    }

    @Test
    public void testAnnotatedQueryWithValueAsPageMultiplePageSizes() {
        /*
         * Will have 6 total results in the following order:
         * 11111, 22222, 333333, 444444, 55555, 98052
         */
        Address testAddress1 = new Address("22222", TestConstants.STREET_0, TestConstants.CITY);
        Address testAddress2 = new Address("33333", TestConstants.STREET_0, TestConstants.CITY);
        Address testAddress3 = new Address("44444", TestConstants.STREET_0, TestConstants.CITY);
        Address testAddress4 = new Address("55555", TestConstants.STREET_0, TestConstants.CITY);
        final List<Address> addresses = Arrays.asList(Address.TEST_ADDRESS1_PARTITION1,
            Address.TEST_ADDRESS2_PARTITION1, testAddress1, testAddress2, testAddress3, testAddress4);
        addressRepository.saveAll(addresses);

        final PageRequest cosmosPageRequest = CosmosPageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "postalCode"));
        final Page<String> postalCodes = addressRepository.annotatedFindPostalCodeValuesByCity(TestConstants.CITY,
            cosmosPageRequest);
        assertAddressPostalCodesUnordered(postalCodes.getContent(), Arrays.asList(Address.TEST_ADDRESS2_PARTITION1));

        final PageRequest cosmosPageRequest2 = CosmosPageRequest.of(1, 3, Sort.by(Sort.Direction.ASC, "postalCode"));
        final Page<String> postalCodes2 = addressRepository.annotatedFindPostalCodeValuesByCity(TestConstants.CITY,
            cosmosPageRequest2);
        assertAddressPostalCodesUnordered(postalCodes2.getContent(), Arrays.asList(testAddress3, testAddress4,
            Address.TEST_ADDRESS1_PARTITION1));

        final PageRequest cosmosPageRequest3 = CosmosPageRequest.of(2, 2, Sort.by(Sort.Direction.ASC, "postalCode"));
        final Page<String> postalCodes3 = addressRepository.annotatedFindPostalCodeValuesByCity(TestConstants.CITY,
            cosmosPageRequest3);
        assertAddressPostalCodesUnordered(postalCodes3.getContent(), Arrays.asList(testAddress4,
            Address.TEST_ADDRESS1_PARTITION1));
    }

    @Test
    public void testAnnotatedQueryWithValueAsList() {
        final List<Address> addresses = Arrays.asList(Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1);
        addressRepository.saveAll(addresses);

        final List<String> postalCodes = addressRepository.annotatedFindPostalCodeValuesByCity(TestConstants.CITY);

        assertAddressPostalCodesUnordered(postalCodes, addresses);
    }

    @Test
    public void testAnnotatedQueryWithJsonNodeAsPage() {
        final List<Address> addresses = Arrays.asList(Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1);
        addressRepository.saveAll(addresses);

        final PageRequest cosmosPageRequest = CosmosPageRequest.of(0, 10);
        final Page<JsonNode> postalCodes = addressRepository.annotatedFindPostalCodesByCity(TestConstants.CITY,
        cosmosPageRequest);
        final List<String> actualPostalCodes = postalCodes.getContent()
                                                          .stream()
                                                          .map(jsonNode -> jsonNode.get("postalCode").asText())
                                                          .collect(Collectors.toList());
        assertAddressPostalCodesUnordered(actualPostalCodes, addresses);
    }

    @Test
    public void testAnnotatedQueryWithJsonNodeAsSlice() {
        final List<Address> addresses = Arrays.asList(Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1);
        addressRepository.saveAll(addresses);

        final PageRequest cosmosPageRequest = CosmosPageRequest.of(0, 10);
        final Slice<JsonNode> postalCodes = addressRepository.annotatedFindPostalCodesByCityAsSlice(TestConstants.CITY,
            cosmosPageRequest);
        final List<String> actualPostalCodes = postalCodes.getContent()
            .stream()
            .map(jsonNode -> jsonNode.get("postalCode").asText())
            .collect(Collectors.toList());
        assertAddressPostalCodesUnordered(actualPostalCodes, addresses);
    }

    private void assertAddressPostalCodesUnordered(List<String> postalCodes, List<Address> expectedResults) {
        List<String> expectedPostalCodes = expectedResults.stream()
                                                          .map(Address::getPostalCode)
                                                          .collect(Collectors.toList());

        assertThat(postalCodes).hasSize(expectedPostalCodes.size()).hasSameElementsAs(expectedPostalCodes);
    }

    private void assertAddressOrder(List<Address> actualResults, Address ... expectedResults) {
        assertThat(actualResults.size()).isEqualTo(expectedResults.length);
        for (int i = 0; i < expectedResults.length; i++) {
            assertThat(expectedResults[i]).isEqualTo(actualResults.get(i));
        }
    }

    @Test
    public void testAnnotatedQueryWithPageableSort() {
        addressRepository.saveAll(Arrays.asList(Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1));

        final PageRequest ascPageRequest = CosmosPageRequest.of(0, PAGE_SIZE_2, Sort.by(Sort.Direction.ASC, "street"));
        final Page<Address> ascPage = addressRepository.annotatedFindByCity(TestConstants.CITY, ascPageRequest);
        assertAddressOrder(ascPage.getContent(), Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1);

        final PageRequest descPageRequest = CosmosPageRequest.of(0, PAGE_SIZE_2, Sort.by(Sort.Direction.DESC, "street"));
        final Page<Address> descPage = addressRepository.annotatedFindByCity(TestConstants.CITY, descPageRequest);
        assertAddressOrder(descPage.getContent(), Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION1);
    }

    @Test
    public void testAnnotatedQueryWithMultipleCitiesAndSort() {
        final List<Address> addresses = Arrays.asList(Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION2);
        addressRepository.saveAll(addresses);

        List<String> cities = new ArrayList<>();
        cities.add(TestConstants.CITY);
        final List<Address> resultsAsc = addressRepository.annotatedFindByCityIn(cities, Sort.by(Sort.Direction.ASC, "postalCode"));
        assertAddressOrder(resultsAsc, Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION1);

        List<String> cities2 = new ArrayList<>();
        cities2.add(TestConstants.CITY);
        cities2.add(TestConstants.CITY_0);
        final List<Address> resultsAsc2 = addressRepository.annotatedFindByCityIn(cities2, Sort.by(Sort.Direction.ASC, "postalCode"));
        assertAddressOrder(resultsAsc2, Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION2, Address.TEST_ADDRESS1_PARTITION1);
    }

    @Test
    public void testAnnotatedQueryWithInLongParameters() {
        Address.TEST_ADDRESS1_PARTITION1.setLongId(TestConstants.LONG_ID_1);
        Address.TEST_ADDRESS2_PARTITION1.setLongId(TestConstants.LONG_ID_1);
        Address.TEST_ADDRESS1_PARTITION2.setLongId(TestConstants.LONG_ID_2);
        Address.TEST_ADDRESS4_PARTITION3.setLongId(TestConstants.LONG_ID_3);

        final List<Long> longListForIn = Arrays.asList(TestConstants.LONG_ID_1, TestConstants.LONG_ID_2);

        final List<Address> addresses = Arrays.asList(Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION2, Address.TEST_ADDRESS4_PARTITION3);
        addressRepository.saveAll(addresses);

        final List<Address> resultsAsc = addressRepository.annotatedFindByInLongParameters(longListForIn, Sort.by(Sort.Direction.ASC, "longId"));
        assertAddressOrder(resultsAsc, Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION2);
    }

    @Test
    public void testAnnotatedQueryWithInIntParameters() {
        Address.TEST_ADDRESS1_PARTITION1.setHomeNumber(TestConstants.HOME_NUMBER_1);
        Address.TEST_ADDRESS2_PARTITION1.setHomeNumber(TestConstants.HOME_NUMBER_1);
        Address.TEST_ADDRESS1_PARTITION2.setHomeNumber(TestConstants.HOME_NUMBER_2);
        Address.TEST_ADDRESS4_PARTITION3.setHomeNumber(TestConstants.HOME_NUMBER_3);

        final List<Integer> homeNumbersForIn = Arrays.asList(TestConstants.HOME_NUMBER_1, TestConstants.HOME_NUMBER_2);

        final List<Address> addresses = Arrays.asList(Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION2, Address.TEST_ADDRESS4_PARTITION3);
        addressRepository.saveAll(addresses);

        final List<Address> resultsAsc = addressRepository.annotatedFindByInHomeNumberParameters(homeNumbersForIn, Sort.by(Sort.Direction.ASC, "longId"));
        assertAddressOrder(resultsAsc, Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION2);
    }

    @Test
    public void testAnnotatedQueryWithInDateParameters() {
        Address.TEST_ADDRESS1_PARTITION1.setRegistrationDate(TestConstants.REGISTRATION_TIME_1D_AGO);
        Address.TEST_ADDRESS2_PARTITION1.setRegistrationDate(TestConstants.REGISTRATION_TIME_1D_AGO);
        Address.TEST_ADDRESS1_PARTITION2.setRegistrationDate(TestConstants.REGISTRATION_TIME_1M_AGO);
        Address.TEST_ADDRESS4_PARTITION3.setRegistrationDate(TestConstants.REGISTRATION_TIME_1W_AGO);

        final List<LocalDate> datesForIn = Arrays.asList(TestConstants.REGISTRATION_TIME_1D_AGO, TestConstants.REGISTRATION_TIME_1M_AGO);

        final List<Address> addresses = Arrays.asList(Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION2, Address.TEST_ADDRESS4_PARTITION3);
        addressRepository.saveAll(addresses);

        final List<Address> resultsAsc = addressRepository.annotatedFindByInRegistrationDateParameters(datesForIn, Sort.by(Sort.Direction.ASC, "longId"));
        assertAddressOrder(resultsAsc, Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION2);
    }

    @Test
    public void testAnnotatedQueryWithInBooleanParameters() {
        Address.TEST_ADDRESS1_PARTITION1.setIsOffice(true);
        Address.TEST_ADDRESS4_PARTITION3.setIsOffice(true);

        final List<Boolean> boolForIn = Arrays.asList(true);

        final List<Address> addresses = Arrays.asList(Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION2, Address.TEST_ADDRESS4_PARTITION3);
        addressRepository.saveAll(addresses);

        final List<Address> resultsAsc = addressRepository.annotatedFindByInIsOfficeParameters(boolForIn, Sort.by(Sort.Direction.ASC, "longId"));
        assertAddressOrder(resultsAsc, Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS4_PARTITION3);
    }

    @Test
    public void testAnnotatedQueryWithArrayContains() {
        final List<Address> addresses = Arrays.asList(Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION2);
        addressRepository.saveAll(addresses);

        List<String> cities = new ArrayList<>();
        cities.add(TestConstants.CITY);
        final List<Address> resultsAsc = addressRepository.annotatedFindByCities(cities);
        assertAddressOrder(resultsAsc, Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1);

        List<String> cities2 = new ArrayList<>();
        cities2.add(TestConstants.CITY);
        cities2.add(TestConstants.CITY_0);
        final List<Address> resultsAsc2 = addressRepository.annotatedFindByCities(cities2);
        assertAddressOrder(resultsAsc2, Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION2);

    }

    @Test
    public void testAnnotatedQueryWithArrayContainsAndSort() {
        final List<Address> addresses = Arrays.asList(Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION2);
        addressRepository.saveAll(addresses);

        List<String> cities = new ArrayList<>();
        cities.add(TestConstants.CITY);
        final List<Address> resultsAsc = addressRepository.annotatedFindByCitiesWithSort(cities, Sort.by(Sort.Direction.ASC, "postalCode"));
        assertAddressOrder(resultsAsc, Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION1);

        List<String> cities2 = new ArrayList<>();
        cities2.add(TestConstants.CITY);
        cities2.add(TestConstants.CITY_0);
        final List<Address> resultsAsc2 = addressRepository.annotatedFindByCitiesWithSort(cities2, Sort.by(Sort.Direction.ASC, "postalCode"));
        assertAddressOrder(resultsAsc2, Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION2, Address.TEST_ADDRESS1_PARTITION1);
    }

    @Test
    public void testAnnotatedFindAllWithSortAsc() {
        final List<Address> addresses = Arrays.asList(Address.TEST_ADDRESS1_PARTITION1,
            Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION2);
        addressRepository.saveAll(addresses);

        final List<Address> resultsAsc = addressRepository.annotatedFindAllWithSort(Sort.by(Sort.Direction.ASC, "postalCode"));
        assertAddressOrder(resultsAsc, Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION2, Address.TEST_ADDRESS1_PARTITION1);
    }

    @Test
    public void testAnnotatedFindAllWithSortDesc() {
        final List<Address> addresses = Arrays.asList(Address.TEST_ADDRESS1_PARTITION1,
            Address.TEST_ADDRESS2_PARTITION1, Address.TEST_ADDRESS1_PARTITION2);
        addressRepository.saveAll(addresses);

        final List<Address> resultsAsc = addressRepository.annotatedFindAllWithSort(Sort.by(Sort.Direction.DESC, "postalCode"));
        assertAddressOrder(resultsAsc, Address.TEST_ADDRESS1_PARTITION1, Address.TEST_ADDRESS1_PARTITION2, Address.TEST_ADDRESS2_PARTITION1);
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import com.azure.spring.data.cosmos.domain.Address;
import com.azure.spring.data.cosmos.domain.AuditableEntity;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.AddressRepository;
import com.azure.spring.data.cosmos.repository.repository.AuditableRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.azure.spring.data.cosmos.common.PageTestUtils.validateLastPage;
import static com.azure.spring.data.cosmos.common.PageTestUtils.validateNonLastPage;
import static com.azure.spring.data.cosmos.common.TestConstants.PAGE_SIZE_1;
import static com.azure.spring.data.cosmos.common.TestConstants.PAGE_SIZE_2;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class AnnotatedQueryIT {

    private static final CosmosEntityInformation<Address, String> entityInformation =
        new CosmosEntityInformation<>(Address.class);

    private static CosmosTemplate staticTemplate;
    private static boolean isSetupDone;

    @Autowired
    private CosmosTemplate template;

    @Autowired
    private AuditableRepository auditableRepository;

    @Autowired
    private AddressRepository addressRepository;

    @AfterClass
    public static void afterClassCleanup() {
        staticTemplate.deleteContainer(entityInformation.getContainerName());
    }

    @Before
    public void setUp() {
        if (!isSetupDone) {
            staticTemplate = template;
            template.createContainerIfNotExists(entityInformation);
        }
        isSetupDone = true;
    }

    @After
    public void cleanup() {
        auditableRepository.deleteAll();
        addressRepository.deleteAll();
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

}

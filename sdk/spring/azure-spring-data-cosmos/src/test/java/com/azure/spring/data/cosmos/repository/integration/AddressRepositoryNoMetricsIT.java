// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.ResponseDiagnosticsTestUtils;
import com.azure.spring.data.cosmos.common.TestUtils;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.Address;
import com.azure.spring.data.cosmos.repository.TestRepositoryNoMetricsConfig;
import com.azure.spring.data.cosmos.repository.repository.AddressRepository;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static com.azure.spring.data.cosmos.domain.Address.TEST_ADDRESS1_PARTITION1;
import static com.azure.spring.data.cosmos.domain.Address.TEST_ADDRESS1_PARTITION2;
import static com.azure.spring.data.cosmos.domain.Address.TEST_ADDRESS2_PARTITION1;
import static com.azure.spring.data.cosmos.domain.Address.TEST_ADDRESS4_PARTITION3;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryNoMetricsConfig.class)
public class AddressRepositoryNoMetricsIT {

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    AddressRepository repository;

    @Autowired
    CosmosConfig cosmosConfig;

    @Autowired
    private CosmosTemplate template;

    @Autowired
    private ResponseDiagnosticsTestUtils responseDiagnosticsTestUtils;


    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, Address.class);
        repository.saveAll(Lists.newArrayList(TEST_ADDRESS1_PARTITION1, TEST_ADDRESS1_PARTITION2,
            TEST_ADDRESS2_PARTITION1, TEST_ADDRESS4_PARTITION3));
    }

    @Test
    public void queryDatabaseWithQueryMetricsDisabled() {
        // Test flag is true
        assertThat(cosmosConfig.isQueryMetricsEnabled()).isFalse();

        // Make sure a query runs
        final List<Address> result = TestUtils.toList(repository.findAll());
        assertThat(result.size()).isEqualTo(4);

        String queryDiagnostics = responseDiagnosticsTestUtils.getCosmosDiagnostics().toString();

        assertThat(queryDiagnostics).doesNotContain("retrievedDocumentCount");
        assertThat(queryDiagnostics).doesNotContain("queryPreparationTimes");
        assertThat(queryDiagnostics).doesNotContain("runtimeExecutionTimes");
        assertThat(queryDiagnostics).doesNotContain("fetchExecutionRanges");
    }

    @Test
    public void queryDatabaseWithIndexMetricsDisabled() {
        // Test flag is true
        assertThat(cosmosConfig.isIndexMetricsEnabled()).isFalse();

        // Make sure a query runs
        final List<Address> result = TestUtils.toList(repository.findAll());
        assertThat(result.size()).isEqualTo(4);

        String queryDiagnostics = responseDiagnosticsTestUtils.getCosmosDiagnostics().toString();

        assertThat(queryDiagnostics).doesNotContain("\"indexUtilizationInfo\"");
        assertThat(queryDiagnostics).doesNotContain("\"UtilizedSingleIndexes\"");
        assertThat(queryDiagnostics).doesNotContain("\"PotentialSingleIndexes\"");
        assertThat(queryDiagnostics).doesNotContain("\"UtilizedCompositeIndexes\"");
        assertThat(queryDiagnostics).doesNotContain("\"PotentialCompositeIndexes\"");
    }
}

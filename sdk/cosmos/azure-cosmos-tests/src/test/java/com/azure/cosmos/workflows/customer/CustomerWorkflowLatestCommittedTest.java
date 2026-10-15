// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.workflows.customer;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.ReadConsistencyStrategy;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosReadManyRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerWorkflowLatestCommittedTest extends CustomerWorkflowTestBase {

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public CustomerWorkflowLatestCommittedTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"fi-customer-workflows"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        initializeSharedSinglePartitionContainer("Customer latest-committed workflow tests", true);
    }

    @AfterClass(groups = {"fi-customer-workflows"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        closeClient();
    }

    @Test(groups = {"fi-customer-workflows"}, timeOut = TIMEOUT)
    public void latestCommittedAndExcludedRegionsFlowAcrossReadOperations() {
        List<String> excludedRegions = excludeFirstWritableRegion();
        TestObject item = TestObject.create();

        CosmosItemResponse<TestObject> createResponse = this.container
            .createItem(item, new CosmosItemRequestOptions().setExcludedRegions(excludedRegions))
            .block();

        assertThat(createResponse).isNotNull();
        registerForCleanup(item);
        CosmosDiagnosticsContext createDiagnostics = createResponse.getDiagnostics().getDiagnosticsContext();
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CREATED);
        assertThat(createDiagnostics.getEffectiveReadConsistencyStrategy()).isEqualTo(ReadConsistencyStrategy.DEFAULT);
        assertExcludedRegions(createDiagnostics, excludedRegions);
        assertDidNotContactExcludedRegions(createDiagnostics, excludedRegions);

        CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
            .setExcludedRegions(excludedRegions)
            .setKeywordIdentifiers(Collections.singleton("latest-committed-read"))
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        CosmosItemResponse<TestObject> readResponse = this.container
            .readItem(item.getId(), partitionKey(item), readOptions, TestObject.class)
            .block();

        assertThat(readResponse).isNotNull();
        CosmosDiagnosticsContext readDiagnostics = readResponse.getDiagnostics().getDiagnosticsContext();
        assertThat(readResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);
        assertThat(readDiagnostics.getEffectiveReadConsistencyStrategy()).isEqualTo(ReadConsistencyStrategy.LATEST_COMMITTED);
        assertThat(readDiagnostics.getTotalRequestCharge()).isGreaterThan(0);
        assertKeywordIdentifier(readDiagnostics, "latest-committed-read");
        assertExcludedRegions(readDiagnostics, excludedRegions);
        assertDidNotContactExcludedRegions(readDiagnostics, excludedRegions);

        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions()
            .setExcludedRegions(excludedRegions)
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED)
            .setQueryName("LatestCommittedCustomerWorkflowQuery");

        FeedResponse<TestObject> queryResponse = this.container
            .queryItems(String.format("SELECT * FROM c WHERE c.id = '%s'", item.getId()), queryOptions, TestObject.class)
            .byPage()
            .blockFirst();

        assertThat(queryResponse).isNotNull();
        assertThat(queryResponse.getResults()).hasSize(1);
        CosmosDiagnosticsContext queryDiagnostics = queryResponse.getCosmosDiagnostics().getDiagnosticsContext();
        assertThat(queryDiagnostics.getEffectiveReadConsistencyStrategy()).isEqualTo(ReadConsistencyStrategy.LATEST_COMMITTED);
        assertExcludedRegions(queryDiagnostics, excludedRegions);

        CosmosReadManyRequestOptions readManyOptions = new CosmosReadManyRequestOptions()
            .setExcludedRegions(excludedRegions)
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        FeedResponse<TestObject> readManyResponse = this.container
            .readMany(Collections.singletonList(new CosmosItemIdentity(partitionKey(item), item.getId())), readManyOptions, TestObject.class)
            .block();

        assertThat(readManyResponse).isNotNull();
        assertThat(readManyResponse.getResults()).hasSize(1);
        CosmosDiagnosticsContext readManyDiagnostics = readManyResponse.getCosmosDiagnostics().getDiagnosticsContext();
        assertThat(readManyDiagnostics.getEffectiveReadConsistencyStrategy()).isEqualTo(ReadConsistencyStrategy.LATEST_COMMITTED);
        assertExcludedRegions(readManyDiagnostics, excludedRegions);
        assertDidNotContactExcludedRegions(readManyDiagnostics, excludedRegions);

        CosmosChangeFeedRequestOptions changeFeedOptions = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRange.forLogicalPartition(partitionKey(item)))
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED)
            .setExcludedRegions(excludedRegions);

        FeedResponse<TestObject> changeFeedResponse = this.container
            .queryChangeFeed(changeFeedOptions, TestObject.class)
            .byPage()
            .blockFirst();

        assertThat(changeFeedResponse)
            .as("change feed query should return at least one page before reading diagnostics")
            .isNotNull();
        CosmosDiagnosticsContext changeFeedDiagnostics = changeFeedResponse.getCosmosDiagnostics().getDiagnosticsContext();
        assertThat(changeFeedDiagnostics.getEffectiveReadConsistencyStrategy()).isEqualTo(ReadConsistencyStrategy.LATEST_COMMITTED);
        assertExcludedRegions(changeFeedDiagnostics, excludedRegions);
    }

    @Test(groups = {"fi-customer-workflows"}, timeOut = TIMEOUT)
    public void latestCommittedReadWithRegionalLeaseNotFoundFault() {
        TestObject item = TestObject.create();
        this.container.createItem(item).block();
        registerForCleanup(item);

        FaultInjectionRule leaseNotFoundRule = configureServerErrorRule(
            this.container,
            FaultInjectionOperationType.READ_ITEM,
            FaultInjectionServerErrorType.LEASE_NOT_FOUND,
            this.writableRegions.get(0),
            currentFaultInjectionConnectionType(),
            1);

        try {
            CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
                .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED)
                .setKeywordIdentifiers(Collections.singleton("latest-committed-fault-read"));

            CosmosDiagnosticsContext diagnosticsContext;
            try {
                CosmosItemResponse<TestObject> readResponse = this.container
                    .readItem(item.getId(), partitionKey(item), readOptions, TestObject.class)
                    .block();

                assertThat(readResponse).isNotNull();
                diagnosticsContext = readResponse.getDiagnostics().getDiagnosticsContext();
            } catch (CosmosException error) {
                diagnosticsContext = error.getDiagnostics().getDiagnosticsContext();
            }

            assertThat(diagnosticsContext).isNotNull();
            assertThat(diagnosticsContext.getEffectiveReadConsistencyStrategy()).isEqualTo(ReadConsistencyStrategy.LATEST_COMMITTED);
            assertFaultInjectedOperation(diagnosticsContext, leaseNotFoundRule);
        } finally {
            leaseNotFoundRule.disable();
        }
    }
}

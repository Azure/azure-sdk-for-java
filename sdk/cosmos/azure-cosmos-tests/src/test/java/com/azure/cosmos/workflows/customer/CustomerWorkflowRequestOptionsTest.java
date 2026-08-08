// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.workflows.customer;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.ReadConsistencyStrategy;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.OverridableRequestOptions;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosReadManyRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerWorkflowRequestOptionsTest extends CustomerWorkflowTestBase {
    @Factory(dataProvider = "clientBuildersWithDirectTcpSession")
    public CustomerWorkflowRequestOptionsTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"fi-customer-workflows"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        initializeSharedSinglePartitionContainer("Customer workflow request option tests", true);
    }

    @AfterClass(groups = {"fi-customer-workflows"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        closeClient();
    }

    @Test(groups = {"fi-customer-workflows"}, timeOut = TIMEOUT)
    public void excludedRegionAndKeywordIdentifiersFlowAcrossOperations() {
        String excludedRegion = this.writableRegions.get(0);
        List<String> excludedRegions = Collections.singletonList(excludedRegion);
        TestObject item = TestObject.create();

        CosmosItemRequestOptions createOptions = new CosmosItemRequestOptions()
            .setKeywordIdentifiers(Collections.singleton("customer-create"))
            .setContentResponseOnWriteEnabled(true)
            .setExcludedRegions(excludedRegions);

        CosmosItemResponse<TestObject> createResponse = this.container
            .createItem(item, createOptions)
            .block();

        assertThat(createResponse).isNotNull();
        registerForCleanup(item);
        assertThat(createResponse.getStatusCode()).isEqualTo(201);
        assertKeywordIdentifier(createResponse.getDiagnostics().getDiagnosticsContext(), "customer-create");
        assertExcludedRegions(createResponse.getDiagnostics().getDiagnosticsContext(), excludedRegions);
        assertDidNotContactExcludedRegions(createResponse.getDiagnostics().getDiagnosticsContext(), excludedRegions);

        CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
            .setKeywordIdentifiers(Collections.singleton("customer-read"))
            .setExcludedRegions(excludedRegions)
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        CosmosItemResponse<TestObject> readResponse = this.container
            .readItem(item.getId(), new PartitionKey(item.getMypk()), readOptions, TestObject.class)
            .block();

        assertThat(readResponse).isNotNull();
        CosmosDiagnosticsContext readDiagnostics = readResponse.getDiagnostics().getDiagnosticsContext();
        assertThat(readResponse.getStatusCode()).isEqualTo(200);
        assertThat(readDiagnostics.getEffectiveReadConsistencyStrategy()).isEqualTo(ReadConsistencyStrategy.LATEST_COMMITTED);
        assertKeywordIdentifier(readDiagnostics, "customer-read");
        assertExcludedRegions(readDiagnostics, excludedRegions);
        assertDidNotContactExcludedRegions(readDiagnostics, excludedRegions);

        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions()
            .setKeywordIdentifiers(Collections.singleton("customer-query"))
            .setExcludedRegions(excludedRegions)
            .setConsistencyLevel(ConsistencyLevel.EVENTUAL)
            .setQueryMetricsEnabled(true)
            .setQueryName("CustomerWorkflowQuery");

        String query = String.format("SELECT * FROM c WHERE c.id = '%s'", item.getId());
        FeedResponse<TestObject> queryResponse = this.container
            .queryItems(query, queryOptions, TestObject.class)
            .byPage()
            .blockFirst();

        assertThat(queryResponse).isNotNull();
        assertThat(queryResponse.getResults()).hasSize(1);
        CosmosDiagnosticsContext queryDiagnostics = queryResponse.getCosmosDiagnostics().getDiagnosticsContext();
        assertKeywordIdentifier(queryDiagnostics, "customer-query");
        assertExcludedRegions(queryDiagnostics, excludedRegions);
        OverridableRequestOptions queryRequestOptions = getRequestOptions(queryDiagnostics);
        assertThat(queryRequestOptions.getConsistencyLevel()).isEqualTo(ConsistencyLevel.EVENTUAL);
        assertThat(queryRequestOptions.isQueryMetricsEnabled()).isTrue();
        assertThat(queryRequestOptions.getQueryNameOrDefault(null)).isEqualTo("CustomerWorkflowQuery");

        CosmosReadManyRequestOptions readManyOptions = new CosmosReadManyRequestOptions()
            .setKeywordIdentifiers(Collections.singleton("customer-read-many"))
            .setExcludedRegions(excludedRegions)
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        FeedResponse<TestObject> readManyResponse = this.container
            .readMany(
                Arrays.asList(new CosmosItemIdentity(new PartitionKey(item.getMypk()), item.getId())),
                readManyOptions,
                TestObject.class)
            .block();

        assertThat(readManyResponse).isNotNull();
        assertThat(readManyResponse.getResults()).hasSize(1);
        CosmosDiagnosticsContext readManyDiagnostics = readManyResponse.getCosmosDiagnostics().getDiagnosticsContext();
        assertThat(readManyDiagnostics.getEffectiveReadConsistencyStrategy()).isEqualTo(ReadConsistencyStrategy.LATEST_COMMITTED);
        assertKeywordIdentifier(readManyDiagnostics, "customer-read-many");
        assertExcludedRegions(readManyDiagnostics, excludedRegions);
        assertDidNotContactExcludedRegions(readManyDiagnostics, excludedRegions);

        item.setStringProp("updated-" + item.getStringProp());
        CosmosItemRequestOptions upsertOptions = new CosmosItemRequestOptions()
            .setKeywordIdentifiers(Collections.singleton("customer-upsert"))
            .setExcludedRegions(excludedRegions)
            .setContentResponseOnWriteEnabled(true);

        CosmosItemResponse<TestObject> upsertResponse = this.container
            .upsertItem(item, upsertOptions)
            .block();

        assertThat(upsertResponse).isNotNull();
        assertThat(upsertResponse.getStatusCode()).isEqualTo(200);
        assertKeywordIdentifier(upsertResponse.getDiagnostics().getDiagnosticsContext(), "customer-upsert");
        assertExcludedRegions(upsertResponse.getDiagnostics().getDiagnosticsContext(), excludedRegions);
        assertDidNotContactExcludedRegions(upsertResponse.getDiagnostics().getDiagnosticsContext(), excludedRegions);

        CosmosItemRequestOptions deleteOptions = new CosmosItemRequestOptions()
            .setKeywordIdentifiers(Collections.singleton("customer-delete"))
            .setExcludedRegions(excludedRegions);

        CosmosItemResponse<Object> deleteResponse = this.container
            .deleteItem(item.getId(), new PartitionKey(item.getMypk()), deleteOptions)
            .block();

        assertThat(deleteResponse).isNotNull();
        assertThat(deleteResponse.getStatusCode()).isEqualTo(204);
        assertKeywordIdentifier(deleteResponse.getDiagnostics().getDiagnosticsContext(), "customer-delete");
        assertExcludedRegions(deleteResponse.getDiagnostics().getDiagnosticsContext(), excludedRegions);
        assertDidNotContactExcludedRegions(deleteResponse.getDiagnostics().getDiagnosticsContext(), excludedRegions);
    }
}

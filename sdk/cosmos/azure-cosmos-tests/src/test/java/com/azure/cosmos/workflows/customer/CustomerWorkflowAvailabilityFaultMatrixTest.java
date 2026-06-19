// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.workflows.customer;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosReadManyRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerWorkflowAvailabilityFaultMatrixTest extends CustomerWorkflowTestBase {

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public CustomerWorkflowAvailabilityFaultMatrixTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"fi-customer-workflows"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        initializeSharedSinglePartitionContainer("Customer availability fault workflow tests");
    }

    @AfterClass(groups = {"fi-customer-workflows"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        closeClient();
    }

    @DataProvider(name = "availabilityFaultScenarios")
    public Object[][] availabilityFaultScenarios() {
        return new Object[][]{
            {"read", FaultInjectionOperationType.READ_ITEM, FaultInjectionServerErrorType.GONE},
            {"read", FaultInjectionOperationType.READ_ITEM, FaultInjectionServerErrorType.TIMEOUT},
            {"read", FaultInjectionOperationType.READ_ITEM, FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE},
            {"read", FaultInjectionOperationType.READ_ITEM, FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR},
            {"query", FaultInjectionOperationType.QUERY_ITEM, FaultInjectionServerErrorType.SERVICE_UNAVAILABLE},
            {"query", FaultInjectionOperationType.QUERY_ITEM, FaultInjectionServerErrorType.GONE},
            {"query", FaultInjectionOperationType.QUERY_ITEM, FaultInjectionServerErrorType.TIMEOUT},
            {"query", FaultInjectionOperationType.QUERY_ITEM, FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR},
            {"readMany", FaultInjectionOperationType.QUERY_ITEM, FaultInjectionServerErrorType.GONE},
            {"readMany", FaultInjectionOperationType.QUERY_ITEM, FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE},
            {"readMany", FaultInjectionOperationType.QUERY_ITEM, FaultInjectionServerErrorType.TOO_MANY_REQUEST},
            {"create", FaultInjectionOperationType.CREATE_ITEM, FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR},
            {"create", FaultInjectionOperationType.CREATE_ITEM, FaultInjectionServerErrorType.TOO_MANY_REQUEST},
            {"create", FaultInjectionOperationType.CREATE_ITEM, FaultInjectionServerErrorType.TIMEOUT},
            {"create", FaultInjectionOperationType.CREATE_ITEM, FaultInjectionServerErrorType.RETRY_WITH},
            {"create", FaultInjectionOperationType.CREATE_ITEM, FaultInjectionServerErrorType.PARTITION_IS_MIGRATING},
            {"upsert", FaultInjectionOperationType.UPSERT_ITEM, FaultInjectionServerErrorType.SERVICE_UNAVAILABLE},
            {"upsert", FaultInjectionOperationType.UPSERT_ITEM, FaultInjectionServerErrorType.PARTITION_IS_MIGRATING},
            {"upsert", FaultInjectionOperationType.UPSERT_ITEM, FaultInjectionServerErrorType.TOO_MANY_REQUEST},
            {"replace", FaultInjectionOperationType.REPLACE_ITEM, FaultInjectionServerErrorType.GONE},
            {"replace", FaultInjectionOperationType.REPLACE_ITEM, FaultInjectionServerErrorType.TIMEOUT},
            {"replace", FaultInjectionOperationType.REPLACE_ITEM, FaultInjectionServerErrorType.SERVICE_UNAVAILABLE},
            {"delete", FaultInjectionOperationType.DELETE_ITEM, FaultInjectionServerErrorType.SERVICE_UNAVAILABLE},
            {"delete", FaultInjectionOperationType.DELETE_ITEM, FaultInjectionServerErrorType.GONE},
            {"delete", FaultInjectionOperationType.DELETE_ITEM, FaultInjectionServerErrorType.TIMEOUT},
            {"patch", FaultInjectionOperationType.PATCH_ITEM, FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR},
            {"patch", FaultInjectionOperationType.PATCH_ITEM, FaultInjectionServerErrorType.SERVICE_UNAVAILABLE},
            {"patch", FaultInjectionOperationType.PATCH_ITEM, FaultInjectionServerErrorType.GONE}
        };
    }

    @Test(groups = {"fi-customer-workflows"}, dataProvider = "availabilityFaultScenarios", timeOut = TIMEOUT)
    public void representativeDirectMultiMasterFaultWorkflow(
        String operation,
        FaultInjectionOperationType faultInjectionOperationType,
        FaultInjectionServerErrorType errorType) {

        skipIfNotDirectMode("Customer availability fault workflow (direct multi-master)");

        TestObject item = TestObject.create();
        if (!"create".equals(operation)) {
            this.container.createItem(item).block();
            registerForCleanup(item);
        }

        List<FaultInjectionRule> faultRules = "readMany".equals(operation)
            ? configureReadManyServerErrorRules(this.container, errorType, this.writableRegions.get(0), 1)
            : Collections.singletonList(configureServerErrorRule(
                this.container,
                faultInjectionOperationType,
                errorType,
                this.writableRegions.get(0),
                currentFaultInjectionConnectionType(),
                1));

        try {
            CosmosDiagnosticsContext diagnosticsContext = executeOperation(operation, item);

            assertFaultInjectedOperation(diagnosticsContext, faultRules);
            assertThat(diagnosticsContext.getDuration()).isNotNull();
        } finally {
            faultRules.forEach(FaultInjectionRule::disable);
        }
    }

    private CosmosDiagnosticsContext executeOperation(String operation, TestObject item) {
        try {
            if ("read".equals(operation)) {
                CosmosItemResponse<TestObject> response = this.container
                    .readItem(item.getId(), partitionKey(item), new CosmosItemRequestOptions(), TestObject.class)
                    .block();

                return response.getDiagnostics().getDiagnosticsContext();
            }

            if ("query".equals(operation)) {
                FeedResponse<TestObject> response = this.container
                    .queryItems(
                        String.format("SELECT * FROM c WHERE c.id = '%s'", item.getId()),
                        new CosmosQueryRequestOptions().setQueryName("AvailabilityFaultWorkflowQuery"),
                        TestObject.class)
                    .byPage()
                    .blockFirst();

                return response.getCosmosDiagnostics().getDiagnosticsContext();
            }

            if ("readMany".equals(operation)) {
                FeedResponse<TestObject> response = this.container
                    .readMany(
                        Collections.singletonList(new CosmosItemIdentity(partitionKey(item), item.getId())),
                        new CosmosReadManyRequestOptions(),
                        TestObject.class)
                    .block();

                return response.getCosmosDiagnostics().getDiagnosticsContext();
            }

            if ("upsert".equals(operation)) {
                item.setStringProp("fault-upsert-" + item.getStringProp());
                CosmosItemResponse<TestObject> response = this.container
                    .upsertItem(item, new CosmosItemRequestOptions().setContentResponseOnWriteEnabled(true))
                    .block();

                return response.getDiagnostics().getDiagnosticsContext();
            }

            if ("replace".equals(operation)) {
                item.setStringProp("fault-replace-" + item.getStringProp());
                CosmosItemResponse<TestObject> response = this.container
                    .replaceItem(item, item.getId(), partitionKey(item), new CosmosItemRequestOptions())
                    .block();

                return response.getDiagnostics().getDiagnosticsContext();
            }

            if ("delete".equals(operation)) {
                CosmosItemResponse<Object> response = this.container
                    .deleteItem(item.getId(), partitionKey(item), new CosmosItemRequestOptions())
                    .block();

                return response.getDiagnostics().getDiagnosticsContext();
            }

            if ("patch".equals(operation)) {
                CosmosItemResponse<TestObject> response = this.container
                    .patchItem(
                        item.getId(),
                        partitionKey(item),
                        CosmosPatchOperations.create().set("/stringProp", "fault-patch-" + item.getStringProp()),
                        TestObject.class)
                    .block();

                return response.getDiagnostics().getDiagnosticsContext();
            }

            CosmosItemResponse<TestObject> response = this.container
                .createItem(item, new CosmosItemRequestOptions().setContentResponseOnWriteEnabled(true))
                .block();

            registerForCleanup(item);
            return response.getDiagnostics().getDiagnosticsContext();
        } catch (CosmosException error) {
            CosmosDiagnosticsContext diagnosticsContext = error.getDiagnostics().getDiagnosticsContext();
            assertThat(error.getStatusCode()).isGreaterThanOrEqualTo(HttpConstants.StatusCodes.BADREQUEST);
            return diagnosticsContext;
        }
    }
}

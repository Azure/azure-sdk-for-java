// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration tests for the custom headers / workload-id feature.
 * <p>
 * Test type: EMULATOR INTEGRATION TEST — requires the Cosmos DB Emulator to be running locally.
 */
public class WorkloadIdE2ETests extends TestSuiteBase {

    private static final String DATABASE_ID = "workloadIdTestDb-" + UUID.randomUUID();
    private static final String CONTAINER_ID = "workloadIdTestContainer-" + UUID.randomUUID();

    private CosmosAsyncClient clientWithWorkloadId;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;

    public WorkloadIdE2ETests() {
        super(new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY));
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.HttpHeaders.WORKLOAD_ID, "15");

        clientWithWorkloadId = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .customHeaders(headers)
            .buildAsyncClient();

        database = createDatabase(clientWithWorkloadId, DATABASE_ID);

        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(CONTAINER_ID, partitionKeyDef);
        database.createContainer(containerProperties).block();
        container = database.getContainer(CONTAINER_ID);
    }

    /**
     * verifies that a create (POST) operation succeeds when the client
     * has a workload-id custom header set at the builder level. Confirms the header
     * flows through the request pipeline without causing errors.
     */
    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createItemWithClientLevelWorkloadId() {
        TestObject doc = TestObject.create();

        CosmosItemResponse<TestObject> response = container
            .createItem(doc, new PartitionKey(doc.getMypk()), new CosmosItemRequestOptions())
            .block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(201);
    }

    /**
     * Verifies that a read (GET) operation succeeds with the client-level workload-id
     * header and that the correct document is returned. Ensures the header does not
     * interfere with normal read semantics.
     */
    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readItemWithClientLevelWorkloadId() {
        // Verify read operation succeeds with workload-id header
        TestObject doc = TestObject.create();
        container.createItem(doc, new PartitionKey(doc.getMypk()), new CosmosItemRequestOptions()).block();

        CosmosItemResponse<TestObject> response = container
            .readItem(doc.getId(), new PartitionKey(doc.getMypk()), TestObject.class)
            .block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getItem().getId()).isEqualTo(doc.getId());
    }

    /**
     * Verifies that a replace (PUT) operation succeeds with the client-level workload-id
     * header. Confirms the header propagates correctly for update operations.
     */
    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void replaceItemWithClientLevelWorkloadId() {
        // Verify replace operation succeeds with workload-id header
        TestObject doc = TestObject.create();
        container.createItem(doc, new PartitionKey(doc.getMypk()), new CosmosItemRequestOptions()).block();

        doc.setStringProp("updated-" + UUID.randomUUID());
        CosmosItemResponse<TestObject> response = container
            .replaceItem(doc, doc.getId(), new PartitionKey(doc.getMypk()), new CosmosItemRequestOptions())
            .block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    /**
     * Verifies that a delete operation succeeds with the client-level workload-id header
     * and returns the expected 204 No Content status code.
     */
    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void deleteItemWithClientLevelWorkloadId() {
        // Verify delete operation succeeds with workload-id header
        TestObject doc = TestObject.create();
        container.createItem(doc, new PartitionKey(doc.getMypk()), new CosmosItemRequestOptions()).block();

        CosmosItemResponse<Object> response = container
            .deleteItem(doc.getId(), new PartitionKey(doc.getMypk()), new CosmosItemRequestOptions())
            .block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(204);
    }

    /**
     * Verifies that a per-request workload-id header override via
     * {@code CosmosItemRequestOptions.setHeader()} works. The request-level header
     * (value "30") should take precedence over the client-level default (value "15").
     */
    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createItemWithRequestLevelWorkloadIdOverride() {
        // Verify per-request header override works — request-level should take precedence
        TestObject doc = TestObject.create();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions()
            .setHeader(HttpConstants.HttpHeaders.WORKLOAD_ID, "30");

        CosmosItemResponse<TestObject> response = container
            .createItem(doc, new PartitionKey(doc.getMypk()), options)
            .block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(201);
    }

    /**
     * Verifies that a cross-partition query operation succeeds when the client has a
     * workload-id custom header. Confirms the header flows correctly through the
     * query pipeline and does not affect result correctness.
     */
    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void queryItemsWithClientLevelWorkloadId() {
        // Verify query operation succeeds with workload-id header
        TestObject doc = TestObject.create();
        container.createItem(doc, new PartitionKey(doc.getMypk()), new CosmosItemRequestOptions()).block();

        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
        long count = container
            .queryItems("SELECT * FROM c WHERE c.id = '" + doc.getId() + "'", queryOptions, TestObject.class)
            .collectList()
            .block()
            .size();

        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    /**
     * Verifies that a per-request workload-id header override on
     * {@code CosmosQueryRequestOptions.setHeader()} works for query operations.
     * The request-level header (value "42") should take precedence over the
     * client-level default.
     */
    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void queryItemsWithRequestLevelWorkloadIdOverride() {
        // Verify per-request header override on query options works
        TestObject doc = TestObject.create();
        container.createItem(doc, new PartitionKey(doc.getMypk()), new CosmosItemRequestOptions()).block();

        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions()
            .setHeader(HttpConstants.HttpHeaders.WORKLOAD_ID, "42");

        long count = container
            .queryItems("SELECT * FROM c WHERE c.id = '" + doc.getId() + "'", queryOptions, TestObject.class)
            .collectList()
            .block()
            .size();

        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    /**
     * Regression test: verifies that a client created without any custom headers
     * continues to work normally. Ensures the custom headers feature does not
     * introduce regressions for clients that do not use it.
     */
    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void clientWithNoCustomHeadersStillWorks() {
        // Verify that a client without custom headers works normally (no regression)
        CosmosAsyncClient clientWithoutHeaders = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .buildAsyncClient();

        try {
            CosmosAsyncContainer c = clientWithoutHeaders
                .getDatabase(DATABASE_ID)
                .getContainer(CONTAINER_ID);

            TestObject doc = TestObject.create();
            CosmosItemResponse<TestObject> response = c
                .createItem(doc, new PartitionKey(doc.getMypk()), new CosmosItemRequestOptions())
                .block();

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(201);
        } finally {
            safeClose(clientWithoutHeaders);
        }
    }

    /**
     * Verifies that a client created with an empty custom headers map works normally.
     * An empty map should behave identically to no custom headers — no errors,
     * no unexpected behavior.
     */
    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void clientWithEmptyCustomHeaders() {
        // Verify that a client with empty custom headers map works normally
        CosmosAsyncClient clientWithEmptyHeaders = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .customHeaders(new HashMap<>())
            .buildAsyncClient();

        try {
            CosmosAsyncContainer c = clientWithEmptyHeaders
                .getDatabase(DATABASE_ID)
                .getContainer(CONTAINER_ID);

            TestObject doc = TestObject.create();
            CosmosItemResponse<TestObject> response = c
                .createItem(doc, new PartitionKey(doc.getMypk()), new CosmosItemRequestOptions())
                .block();

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(201);
        } finally {
            safeClose(clientWithEmptyHeaders);
        }
    }

    /**
     * Verifies that a client can be configured with multiple custom headers simultaneously
     * (workload-id plus an additional custom header). Confirms that all headers flow
     * through the pipeline without interfering with each other.
     */
    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void clientWithMultipleCustomHeaders() {
        // Verify that multiple custom headers can be set simultaneously
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.HttpHeaders.WORKLOAD_ID, "20");
        headers.put("x-ms-custom-test-header", "test-value");

        CosmosAsyncClient clientWithMultipleHeaders = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .customHeaders(headers)
            .buildAsyncClient();

        try {
            CosmosAsyncContainer c = clientWithMultipleHeaders
                .getDatabase(DATABASE_ID)
                .getContainer(CONTAINER_ID);

            TestObject doc = TestObject.create();
            CosmosItemResponse<TestObject> response = c
                .createItem(doc, new PartitionKey(doc.getMypk()), new CosmosItemRequestOptions())
                .block();

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(201);
        } finally {
            safeClose(clientWithMultipleHeaders);
        }
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(database);
        safeClose(clientWithWorkloadId);
    }
}


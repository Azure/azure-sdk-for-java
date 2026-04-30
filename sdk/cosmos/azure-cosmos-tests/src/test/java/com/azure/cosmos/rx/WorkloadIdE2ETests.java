// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosAdditionalHeaderName;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end smoke tests for the additional headers / workload-id feature in Gateway mode.
 * <p>
 * Test type: EMULATOR INTEGRATION TEST — requires a Cosmos DB account or emulator.
 * <p>
 * Uses {@code @Factory(dataProvider = "simpleClientBuilderGatewaySession")} to run all tests
 * in Gateway mode with Session consistency.
 * <p>
 * These are <b>smoke tests</b> — they verify CRUD/query operations succeed (status code
 * 200/201/204) when the workload-id header is set. They prove the header doesn't break
 * anything but do NOT assert the header is actually present on the wire request.
 * <p>
 * For wire-level assertion tests that verify the header is actually present on
 * {@link com.azure.cosmos.implementation.RxDocumentServiceRequest}, see
 * {@link WorkloadIdDirectInterceptorTests} which runs in Direct mode (RNTBD) using
 * the transport client interceptor.
 */
public class WorkloadIdE2ETests extends TestSuiteBase {

    private static final String DATABASE_ID = "workloadIdTestDb-" + UUID.randomUUID();
    private static final String CONTAINER_ID = "workloadIdTestContainer-" + UUID.randomUUID();

    private CosmosAsyncClient clientWithWorkloadId;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;

    @Factory(dataProvider = "simpleClientBuilderGatewaySession")
    public WorkloadIdE2ETests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        Map<CosmosAdditionalHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosAdditionalHeaderName.WORKLOAD_ID, "15");

        // Clone the shared builder before setting additionalHeaders.
        // getClientBuilder() returns the same mutable instance from the data provider.
        // Calling .additionalHeaders() directly on it would mutate the shared builder,
        clientWithWorkloadId = copyCosmosClientBuilder(getClientBuilder())
            .additionalHeaders(headers)
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
     * has a workload-id additional header set at the builder level. Confirms the header
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
     * {@code CosmosItemRequestOptions.setAdditionalHeaders()} works. The request-level header
     * (value "30") should take precedence over the client-level default (value "15").
     */
    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createItemWithRequestLevelWorkloadIdOverride() {
        // Verify per-request header override works — request-level should take precedence
        TestObject doc = TestObject.create();

        Map<CosmosAdditionalHeaderName, String> requestHeaders = new HashMap<>();
        requestHeaders.put(CosmosAdditionalHeaderName.WORKLOAD_ID, "30");

        CosmosItemRequestOptions options = new CosmosItemRequestOptions()
            .setAdditionalHeaders(requestHeaders);

        CosmosItemResponse<TestObject> response = container
            .createItem(doc, new PartitionKey(doc.getMypk()), options)
            .block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(201);
    }

    /**
     * Verifies that a cross-partition query operation succeeds when the client has a
     * workload-id additional header. Confirms the header flows correctly through the
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
     * {@code CosmosQueryRequestOptions.setAdditionalHeaders()} works for query operations.
     * The request-level header (value "42") should take precedence over the
     * client-level default.
     */
    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void queryItemsWithRequestLevelWorkloadIdOverride() {
        // Verify per-request header override on query options works
        TestObject doc = TestObject.create();
        container.createItem(doc, new PartitionKey(doc.getMypk()), new CosmosItemRequestOptions()).block();

        Map<CosmosAdditionalHeaderName, String> requestHeaders = new HashMap<>();
        requestHeaders.put(CosmosAdditionalHeaderName.WORKLOAD_ID, "42");

        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions()
            .setAdditionalHeaders(requestHeaders);

        long count = container
            .queryItems("SELECT * FROM c WHERE c.id = '" + doc.getId() + "'", queryOptions, TestObject.class)
            .collectList()
            .block()
            .size();

        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    /**
     * Regression test: verifies that a client created without any additional headers
     * continues to work normally. Ensures the additional headers feature does not
     * introduce regressions for clients that do not use it.
     */
    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void clientWithNoAdditionalHeadersStillWorks() {
        // Verify that a client without additional headers works normally (no regression)
        CosmosAsyncClient clientWithoutHeaders = copyCosmosClientBuilder(getClientBuilder())
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
     * Verifies that a client created with an empty additional headers map works normally.
     * An empty map should behave identically to no additional headers — no errors,
     * no unexpected behavior.
     */
    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void clientWithEmptyAdditionalHeaders() {
        // Verify that a client with empty additional headers map works normally
        CosmosAsyncClient clientWithEmptyHeaders = copyCosmosClientBuilder(getClientBuilder())
            .additionalHeaders(new HashMap<>())
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
     * Verifies that the {@code CosmosClientBuilder.additionalHeaders()} API accepts
     * a map keyed by {@link CosmosAdditionalHeaderName} constants.
     * <p>
     * {@link CosmosAdditionalHeaderName} has a private constructor and no public factory method,
     * so callers can only use the predefined constants (e.g., {@link CosmosAdditionalHeaderName#WORKLOAD_ID}).
     * This test verifies the positive case: a map with known header constants
     * is accepted by the builder without error.
     */
    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void additionalHeadersOnlyAcceptKnownCosmosAdditionalHeaderNames() {
        // The type system enforces that only CosmosAdditionalHeaderName instances can be used as keys.
        // Since CosmosAdditionalHeaderName has a private constructor and no public fromString(),
        // the only valid keys are the predefined constants like WORKLOAD_ID.
        Map<CosmosAdditionalHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosAdditionalHeaderName.WORKLOAD_ID, "15");

        // Verify builder accepts a map with known header constants — no exception thrown
        CosmosClientBuilder builder = new CosmosClientBuilder()
            .endpoint("https://localhost:8081")
            .key("dGVzdEtleQ==")
            .additionalHeaders(headers);

        assertThat(builder).isNotNull();
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(database);
        safeClose(clientWithWorkloadId);
    }
}


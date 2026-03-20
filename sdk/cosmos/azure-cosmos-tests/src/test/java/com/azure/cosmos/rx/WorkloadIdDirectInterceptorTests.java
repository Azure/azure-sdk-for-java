// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosHeaderName;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.test.implementation.interceptor.CosmosInterceptorHelper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Interceptor-based integration tests for the workload-id / additional headers feature
 * running in <b>Direct mode (RNTBD)</b>.
 * <p>
 * These tests use {@link CosmosInterceptorHelper#registerTransportClientInterceptor} to
 * capture {@link RxDocumentServiceRequest} objects at the TransportClient level and assert
 * that the {@code x-ms-cosmos-workload-id} header is present with the correct value on
 * wire requests. The interceptor only fires in Direct mode because Gateway mode data-plane
 * requests go through {@code RxGatewayStoreModel → HttpClient}, bypassing the
 * TransportClient entirely.
 * <p>
 * Uses {@code @Factory(dataProvider = "simpleClientBuildersWithJustDirectTcp")} to ensure
 * all tests run exclusively in Direct/TCP mode where the transport interceptor is active.
 * <p>
 * Gateway-mode header injection is separately verified by:
 * <ul>
 *   <li>Unit tests in {@code RxGatewayStoreModelTest} (data-plane)</li>
 *   <li>Unit tests in {@code GatewayAddressCacheTest} (metadata requests)</li>
 *   <li>Smoke tests in {@link WorkloadIdE2ETests} (Gateway mode end-to-end)</li>
 * </ul>
 */
public class WorkloadIdDirectInterceptorTests extends TestSuiteBase {

    private static final String DATABASE_ID = "workloadIdDirectTestDb-" + UUID.randomUUID();
    private static final String CONTAINER_ID = "workloadIdDirectTestContainer-" + UUID.randomUUID();

    private CosmosAsyncClient clientWithWorkloadId;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;

    @Factory(dataProvider = "simpleClientBuildersWithJustDirectTcp")
    public WorkloadIdDirectInterceptorTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        Map<CosmosHeaderName, String> headers = new HashMap<>();
        headers.put(CosmosHeaderName.WORKLOAD_ID, "15");

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
     * Verifies that the client-level workload-id header (value "15") is present on the
     * {@link RxDocumentServiceRequest} for create (data-plane) operation in Direct mode.
     * <p>
     * Registers a transport client interceptor that captures every request before it hits
     * the RNTBD wire. After performing a createItem, asserts that at least one captured
     * request with {@code ResourceType.Document} carries the {@code x-ms-cosmos-workload-id}
     * header with value {@code "15"}.
     */
    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void verifyWorkloadIdHeaderPresentOnDataPlaneRequest() {
        ConcurrentLinkedQueue<RxDocumentServiceRequest> capturedRequests = new ConcurrentLinkedQueue<>();

        CosmosInterceptorHelper.registerTransportClientInterceptor(
            clientWithWorkloadId,
            (request, storeResponse) -> {
                capturedRequests.add(request);
                return storeResponse;
            }
        );

        TestObject doc = TestObject.create();
        container.createItem(doc, new PartitionKey(doc.getMypk()), new CosmosItemRequestOptions()).block();

        // In Direct mode, the interceptor MUST capture requests — fail if it didn't
        assertThat(capturedRequests)
            .as("Transport interceptor should capture requests in Direct mode")
            .isNotEmpty();

        // Assert that at least one Document-type request carries the workload-id header
        boolean foundWorkloadIdOnDocument = capturedRequests.stream()
            .filter(r -> r.getResourceType() == ResourceType.Document)
            .anyMatch(r -> "15".equals(r.getHeaders().get(HttpConstants.HttpHeaders.WORKLOAD_ID)));

        assertThat(foundWorkloadIdOnDocument)
            .as("Expected workload-id header '15' on at least one Document request")
            .isTrue();
    }

    /**
     * Verifies that a per-request workload-id override (value "30") is present on the wire
     * request instead of the client-level default (value "15").
     * <p>
     * This confirms that the request-level header set via
     * {@link CosmosItemRequestOptions#setAdditionalHeaders(Map)} takes precedence over
     * the client-level header set via {@link CosmosClientBuilder#additionalHeaders(Map)}
     * in Direct mode (RNTBD).
     */
    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void verifyRequestLevelOverrideOnWire() {
        ConcurrentLinkedQueue<RxDocumentServiceRequest> capturedRequests = new ConcurrentLinkedQueue<>();

        CosmosInterceptorHelper.registerTransportClientInterceptor(
            clientWithWorkloadId,
            (request, storeResponse) -> {
                capturedRequests.add(request);
                return storeResponse;
            }
        );

        Map<CosmosHeaderName, String> requestHeaders = new HashMap<>();
        requestHeaders.put(CosmosHeaderName.WORKLOAD_ID, "30");

        CosmosItemRequestOptions options = new CosmosItemRequestOptions()
            .setAdditionalHeaders(requestHeaders);

        TestObject doc = TestObject.create();
        container.createItem(doc, new PartitionKey(doc.getMypk()), options).block();

        // In Direct mode, the interceptor MUST capture requests — fail if it didn't
        assertThat(capturedRequests)
            .as("Transport interceptor should capture requests in Direct mode")
            .isNotEmpty();

        // Assert that the Document request carries the overridden value "30", not the client default "15"
        boolean foundOverriddenWorkloadId = capturedRequests.stream()
            .filter(r -> r.getResourceType() == ResourceType.Document)
            .anyMatch(r -> "30".equals(r.getHeaders().get(HttpConstants.HttpHeaders.WORKLOAD_ID)));

        assertThat(foundOverriddenWorkloadId)
            .as("Expected workload-id header '30' (request-level override) on Document request")
            .isTrue();
    }

    /**
     * Negative test: verifies that a client created WITHOUT additional headers does NOT
     * have the workload-id header on wire requests in Direct mode. Ensures the header is
     * only present when explicitly configured.
     */
    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void verifyNoWorkloadIdHeaderWhenNotConfigured() {
        CosmosAsyncClient clientWithoutHeaders = copyCosmosClientBuilder(getClientBuilder())
            .buildAsyncClient();

        try {
            ConcurrentLinkedQueue<RxDocumentServiceRequest> capturedRequests = new ConcurrentLinkedQueue<>();

            CosmosInterceptorHelper.registerTransportClientInterceptor(
                clientWithoutHeaders,
                (request, storeResponse) -> {
                    capturedRequests.add(request);
                    return storeResponse;
                }
            );

            CosmosAsyncContainer c = clientWithoutHeaders
                .getDatabase(DATABASE_ID)
                .getContainer(CONTAINER_ID);

            TestObject doc = TestObject.create();
            c.createItem(doc, new PartitionKey(doc.getMypk()), new CosmosItemRequestOptions()).block();

            // In Direct mode, the interceptor MUST capture requests
            assertThat(capturedRequests)
                .as("Transport interceptor should capture requests in Direct mode")
                .isNotEmpty();

            // Assert that NO Document-type request carries the workload-id header
            boolean anyDocRequestHasWorkloadId = capturedRequests.stream()
                .filter(r -> r.getResourceType() == ResourceType.Document)
                .anyMatch(r -> r.getHeaders().containsKey(HttpConstants.HttpHeaders.WORKLOAD_ID));

            assertThat(anyDocRequestHasWorkloadId)
                .as("Expected NO workload-id header on Document requests when not configured")
                .isFalse();
        } finally {
            safeClose(clientWithoutHeaders);
        }
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(database);
        safeClose(clientWithWorkloadId);
    }
}


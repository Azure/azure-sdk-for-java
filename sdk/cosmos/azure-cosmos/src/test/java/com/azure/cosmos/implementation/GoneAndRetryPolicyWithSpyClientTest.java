// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.ConsistencyWriter;
import com.azure.cosmos.implementation.directconnectivity.Protocol;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.ReplicatedResourceClient;
import com.azure.cosmos.implementation.directconnectivity.TransportClient;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.AsyncDocumentClient.Builder;
import com.azure.cosmos.implementation.http.HttpResponse;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

/**
 * The purpose of the tests in this class is to ensure the request are routed through direct connectivity stack.
 * The tests in other test classes validate the actual behaviour and different scenarios.
 */
public class GoneAndRetryPolicyWithSpyClientTest extends TestSuiteBase {

    private final static String PARTITION_KEY_FIELD_NAME = "mypk";

    private static Database createdDatabase;
    private static DocumentCollection createdCollection;

    private SpyClientUnderTestFactory.ClientUnderTest client;

    @DataProvider
    public static Object[][] directClientBuilder() {
        return new Object[][] { { createDCBuilder(Protocol.TCP) } };
    }

    static Builder createDCBuilder(Protocol protocol) {

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        connectionPolicy.setRequestTimeout(Duration.ofSeconds(5));
        Configs configs = spy(new Configs());
        doAnswer((Answer<Protocol>) invocation -> protocol).when(configs).getProtocol();

        return new Builder()
            .withServiceEndpoint(TestConfigurations.HOST)
            .withConfigs(configs)
            .withConnectionPolicy(connectionPolicy)
            .withConsistencyLevel(ConsistencyLevel.SESSION)
            .withContentResponseOnWriteEnabled(true)
            .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY);
    }

    @Factory(dataProvider = "directClientBuilder")
    public GoneAndRetryPolicyWithSpyClientTest(Builder clientBuilder) {
        super(clientBuilder);
    }

    private String headersToString(HttpHeaders headers) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> map = headers.toMap();
        for(String key : map.keySet()) {
            sb.append(String.format("%s: %s", key, map.get(key)));
            sb.append("\n");
        }

        return sb.toString();
    }

    private Mono<HttpResponse> captureAndLogRequest(
        InvocationOnMock invocation,
        HttpClient originalClient,
        AtomicBoolean forceRefreshHeaderSeen,
        AtomicBoolean forceCollectionRoutingMapRefreshHeaderSeen) {

        final HttpRequest request =
            invocation.getArgument(0, HttpRequest.class);

        String forceRefreshAddressHeader = request
            .headers()
            .toMap()
            .get(HttpConstants.HttpHeaders.FORCE_REFRESH);

        String forceCollectionRoutingMapRefreshHeader = request
            .headers()
            .toMap()
            .get(HttpConstants.HttpHeaders.FORCE_COLLECTION_ROUTING_MAP_REFRESH);

        Mono<HttpResponse> responseObservable;
        if (invocation.getArguments().length == 2) {
            responseObservable = originalClient.send(request, invocation.getArgument(1, Duration.class));
        } else {
            responseObservable = originalClient.send(request);
        }

        if ("true".equalsIgnoreCase(forceRefreshAddressHeader)) {

            forceRefreshHeaderSeen.set(true);

            if (forceCollectionRoutingMapRefreshHeaderSeen != null &&
                "true".equalsIgnoreCase(forceCollectionRoutingMapRefreshHeader)) {
                forceCollectionRoutingMapRefreshHeaderSeen.set(true);
            }

            logger.info(
                String.format("Force refresh request %s Headers: \n%s", request.uri().toString(), headersToString(request.headers())));
                return responseObservable;
        } else {
            logger.info(
                String.format("Other HTTP request %s Headers: \n%s", request.uri().toString(), headersToString(request.headers())));
        }

        return responseObservable;
    }

    /**
     * Tests document creation through direct mode
     */
    @Test(groups = { "direct" }, timeOut = TIMEOUT)
    public void createRecoversFrom410GoneOnPartitionMigration() {

        // PartitionMigration means there was no split/merge but some replica
        // of a partition have been moved to another machine (for example during deployments)
        // in this error condition recovery is possible when forcing a partition address refresh in the Gateway

        HttpClient origHttpClient = this.client.getOrigHttpClient();
        HttpClient spyHttpClient = this.client.getSpyHttpClient();
        final AtomicBoolean forceRefreshHeaderSeen = new AtomicBoolean(false);

        doAnswer(invocation -> this.captureAndLogRequest(
            invocation, origHttpClient, forceRefreshHeaderSeen, null))
        .when(spyHttpClient)
        .send(
            Mockito.any(HttpRequest.class),
            Mockito.any(Duration.class));

        doAnswer(invocation -> this.captureAndLogRequest(
            invocation, origHttpClient, forceRefreshHeaderSeen, null))
            .when(spyHttpClient)
            .send(
                Mockito.any(HttpRequest.class));

        ReplicatedResourceClient replicatedResourceClient = ReflectionUtils
            .getReplicatedResourceClient(
                ReflectionUtils.getStoreClient(this.client)
            );
        ConsistencyWriter writer = ReflectionUtils.getConsistencyWriter(replicatedResourceClient);
        TransportClient originalTransportClient = ReflectionUtils.getTransportClient(writer);
        TransportClient spyTransportClient = spy(originalTransportClient);
        ReflectionUtils.setTransportClient(writer, spyTransportClient);

        doAnswer(invocation -> {
            final Uri physicalAddress =
                invocation.getArgument(0, Uri.class);
            final RxDocumentServiceRequest rxServiceRequest =
                invocation.getArgument(1, RxDocumentServiceRequest.class);

            if (StringUtils.isEmpty(rxServiceRequest.requestContext.resourcePhysicalAddress)) {
                rxServiceRequest.requestContext.resourcePhysicalAddress = physicalAddress.toString();
            }

            if (forceRefreshHeaderSeen.get() ||
                rxServiceRequest.getResourceType() != ResourceType.Document ||
                rxServiceRequest.getOperationType() != OperationType.Create) {

                return originalTransportClient.invokeResourceOperationAsync(physicalAddress, rxServiceRequest);
            }

            GoneException gone = new GoneException("Mocked Gone");
            gone.setIsBasedOn410ResponseFromService();
            BridgeInternal.setSendingRequestStarted(gone, true);
            throw gone;
        })
        .when(spyTransportClient)
        .invokeResourceOperationAsync(
            Mockito.any(Uri.class),
            Mockito.any(RxDocumentServiceRequest.class));

        final Document docDefinition = getDocumentDefinition();
        Mono<ResourceResponse<Document>> createObservable = this
            .client
            .createDocument(
            this.getCollectionLink(), docDefinition, null, false);

        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
            .withId(docDefinition.getId())
            .build();

        AtomicInteger numberOfRequestsWithForceRefreshHeader = new AtomicInteger(0);
        try {
            validateSuccess(createObservable, validator, TIMEOUT * 1000);
        }
        catch(Exception someError)
        {
            logger.error(someError.toString());

            client
                .capturedRequestResponseHeaderPairs()
                .forEach(
                    p -> {
                        HttpRequest request = p.getLeft();
                        HttpResponse response = null;
                        try {
                            response = p.getRight().get();
                        } catch (InterruptedException | ExecutionException e) {
                            logger.error("Failed to retrieve response", e);
                        }

                        String forceRefreshAddressHeader = request
                            .headers()
                            .toMap()
                            .get(HttpConstants.HttpHeaders.FORCE_REFRESH);

                        if (forceRefreshAddressHeader != null) {
                            int forceRefreshRequestIndex = numberOfRequestsWithForceRefreshHeader.incrementAndGet();

                            logger.info(String.format(
                                "Force Refresh request #%d: Request: %s, Response: %s %s",
                                forceRefreshRequestIndex,
                                headersToString(request.headers()),
                                headersToString(response.headers()),
                                response.bodyAsString().block()
                            ));
                        }
                    });

            throw someError;
        }
    }

    /**
     * Tests document creation through direct mode
     */
    @Test(groups = { "direct" }, timeOut = TIMEOUT * 10)
    public void createRecoversFrom410GoneFromServiceOnPartitionSplitDuringIdleTime() throws Exception {
        executeCreateRecoversFrom410GoneOnPartitionSplitDuringIdleTime(true);
    }

    /**
     * Tests document creation through direct mode
     */
    @Test(groups = { "direct" }, timeOut = TIMEOUT * 10)
    public void createRecoversFrom410GoneClientGeneratedOnPartitionSplitDuringIdleTime() throws Exception {
        executeCreateRecoversFrom410GoneOnPartitionSplitDuringIdleTime(false);
    }

    private void executeCreateRecoversFrom410GoneOnPartitionSplitDuringIdleTime(
        boolean isBasedOn410ResponseFromService) throws InterruptedException {

        // Usually on a partition split a 410 with SubStatusCode 1002, 1007 or 1008 is seen and will
        // trigger a refresh of the PKRangeCache
        // In cases where the client is idle during the split it is possible that a request is still made to the
        // parent partition (which doesn't exist anymore)
        // If the Node has another replica listening on the target endpoint+port it will return a 410/1002 and the
        // client will recover
        // If the Node is up but does not have any replica listening on the target endpoint+port a 410/0 is returned.
        // In this case either the PKRangeCache or the CollectionRoutingMap needs to be refreshed to be able to recover.
        // If the endpoint+port is down (no-one listening on it) a client-side generated 410/0 is created and would also
        // require either the PKRangeCache or the CollectionRoutingMap to be refreshed to be able to recover.

        HttpClient origHttpClient = this.client.getOrigHttpClient();
        HttpClient spyHttpClient = this.client.getSpyHttpClient();
        final AtomicBoolean forceRefreshHeaderSeen = new AtomicBoolean(false);
        final AtomicBoolean forceCollectionRoutingMapRefreshHeaderSeen = new AtomicBoolean(false);

        doAnswer(invocation -> this.captureAndLogRequest(
            invocation, origHttpClient, forceRefreshHeaderSeen, forceCollectionRoutingMapRefreshHeaderSeen))
            .when(spyHttpClient)
            .send(
                Mockito.any(HttpRequest.class),
                Mockito.any(Duration.class));

        doAnswer(invocation -> this.captureAndLogRequest(
            invocation, origHttpClient, forceRefreshHeaderSeen, forceCollectionRoutingMapRefreshHeaderSeen))
            .when(spyHttpClient)
            .send(
                Mockito.any(HttpRequest.class));

        ReplicatedResourceClient replicatedResourceClient = ReflectionUtils
            .getReplicatedResourceClient(
                ReflectionUtils.getStoreClient(this.client)
            );
        ConsistencyWriter writer = ReflectionUtils.getConsistencyWriter(replicatedResourceClient);
        TransportClient originalTransportClient = ReflectionUtils.getTransportClient(writer);
        TransportClient spyTransportClient = spy(originalTransportClient);
        ReflectionUtils.setTransportClient(writer, spyTransportClient);

        doAnswer(invocation -> {
            final Uri physicalAddress =
                invocation.getArgument(0, Uri.class);
            final RxDocumentServiceRequest rxServiceRequest =
                invocation.getArgument(1, RxDocumentServiceRequest.class);

            if (StringUtils.isEmpty(rxServiceRequest.requestContext.resourcePhysicalAddress)) {
                rxServiceRequest.requestContext.resourcePhysicalAddress = physicalAddress.toString();
            }

            if (forceCollectionRoutingMapRefreshHeaderSeen.get() ||
                rxServiceRequest.getResourceType() != ResourceType.Document ||
                rxServiceRequest.getOperationType() != OperationType.Create) {

                return originalTransportClient.invokeResourceOperationAsync(physicalAddress, rxServiceRequest);
            }

            GoneException gone = new GoneException("Mocked Gone");
            if (isBasedOn410ResponseFromService) {
                gone.setIsBasedOn410ResponseFromService();
            }

            BridgeInternal.setSendingRequestStarted(gone, true);
            throw gone;
        })
            .when(spyTransportClient)
            .invokeResourceOperationAsync(
                Mockito.any(Uri.class),
                Mockito.any(RxDocumentServiceRequest.class));

        final Document docDefinition = getDocumentDefinition();
        Mono<ResourceResponse<Document>> createObservable = this
            .client
            .createDocument(
                this.getCollectionLink(), docDefinition, null, false);

        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
            .withId(docDefinition.getId())
            .build();

        AtomicInteger numberOfRequestsWithForceCollectionRoutingMapRefreshHeader = new AtomicInteger(0);

        int allowedNumberOfFailures = isBasedOn410ResponseFromService ? 1 : 3;
        boolean durationOverridden = false;

        while (allowedNumberOfFailures > 0) {
            try {
                validateSuccess(createObservable, validator, TIMEOUT * 1000);
                return;
            } catch (AssertionError error) {
                logger.info(error.toString());
                allowedNumberOfFailures--;

                if (!durationOverridden) {
                    ReflectionUtils.setDefaultMinDurationBeforeEnforcingCollectionRoutingMapRefreshDuration(
                        Duration.ofSeconds(1));
                    durationOverridden = true;

                    Thread.sleep(1_500);
                }
            }
        }

        try {
            validateSuccess(createObservable, validator, TIMEOUT * 1000);
        } catch (Exception someError) {
            logger.error(someError.toString());

            client
                .capturedRequestResponseHeaderPairs()
                .forEach(
                    p -> {
                        HttpRequest request = p.getLeft();
                        HttpResponse response = null;
                        try {
                            response = p.getRight().get();
                        } catch (InterruptedException | ExecutionException e) {
                            logger.error("Failed to retrieve response", e);
                        }

                        String forceRefreshCollectionRoutingMapHeader = request
                            .headers()
                            .toMap()
                            .get(HttpConstants.HttpHeaders.FORCE_COLLECTION_ROUTING_MAP_REFRESH);

                        if (forceRefreshCollectionRoutingMapHeader != null) {
                            int forceRefreshRequestIndex =
                                numberOfRequestsWithForceCollectionRoutingMapRefreshHeader.incrementAndGet();

                            logger.info(String.format(
                                "Force Refresh request #%d: Request: %s, Response: %s %s",
                                forceRefreshRequestIndex,
                                headersToString(request.headers()),
                                headersToString(response.headers()),
                                response.bodyAsString().block()
                            ));
                        }
                    });

            throw someError;
        } finally {
            if (durationOverridden) {
                ReflectionUtils.setDefaultMinDurationBeforeEnforcingCollectionRoutingMapRefreshDuration(
                    Duration.ofSeconds(30));
            }
        }
    }

    @AfterMethod(groups = { "direct" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterMethod() {
        deleteCollectionIfExists(client, SHARED_DATABASE.getId(), createdCollection.getId());
        safeClose(client);
    }

    @BeforeMethod(groups = { "direct" })
    public void beforeMethod(Method method) {
        RequestOptions options = new RequestOptions();
        options.setOfferThroughput(40100);
        createdDatabase = SHARED_DATABASE;
        createdCollection = createCollection(createdDatabase.getId(), getCollectionDefinition(), options);
        client = SpyClientUnderTestFactory.createClientUnderTest(clientBuilder());
    }

    private String getCollectionLink() {
        return String.format("/dbs/%s/colls/%s", createdDatabase.getId(), createdCollection.getId());
    }

    private Document getDocumentDefinition() {
        Document doc = new Document();
        doc.setId(UUID.randomUUID().toString());
        BridgeInternal.setProperty(doc, PARTITION_KEY_FIELD_NAME, UUID.randomUUID().toString());
        BridgeInternal.setProperty(doc, "name", "Hafez");
        return doc;
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpClientUnderTestWrapper;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.MetadataRequestRetryPolicy;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.ShouldRetryValidator;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpClientConfig;
import com.azure.cosmos.implementation.http.HttpTimeoutPolicyControlPlaneHotPath;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorResult;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import io.reactivex.subscribers.TestSubscriber;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.SocketException;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;

public class MetadataRequestRetryPolicyTests extends TestSuiteBase {

    private CosmosClientBuilder clientBuilder;
    private Map<String, String> writeRegionMap;
    private Map<String, String> readRegionMap;

    @Factory(dataProvider = "clientBuilderSolelyDirectWithSessionConsistency")
    public MetadataRequestRetryPolicyTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"multi-region"})
    public void beforeClass() {

        CosmosAsyncClient client = null;

        try {
            this.clientBuilder = getClientBuilder();
            client = this.clientBuilder.buildAsyncClient();
            AsyncDocumentClient documentClient = BridgeInternal.getContextClient(client);

            GlobalEndpointManager globalEndpointManager = documentClient.getGlobalEndpointManager();
            DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

            this.writeRegionMap = getRegionsMap(databaseAccount, true);
            this.readRegionMap = getRegionsMap(databaseAccount, false);
        } finally {
            safeClose(client);
        }
    }

    @DataProvider(name = "operationContext")
    public Object[][] operationContext() {
        return new Object[][]{
            // 1. operation to fault inject on
            // 2. operation type corresponding to the fault injection operation type
            // 3. fault to inject
            // 4. a boolean representing whether an operation is a write operation
            {FaultInjectionOperationType.READ_ITEM, OperationType.Read, FaultInjectionServerErrorType.CONNECTION_DELAY, false},
            {FaultInjectionOperationType.CREATE_ITEM, OperationType.Create, FaultInjectionServerErrorType.CONNECTION_DELAY, true},
            {FaultInjectionOperationType.REPLACE_ITEM, OperationType.Replace, FaultInjectionServerErrorType.CONNECTION_DELAY, true},
            {FaultInjectionOperationType.UPSERT_ITEM, OperationType.Upsert, FaultInjectionServerErrorType.CONNECTION_DELAY, true},
            {FaultInjectionOperationType.QUERY_ITEM, OperationType.Query, FaultInjectionServerErrorType.CONNECTION_DELAY, false},
            {FaultInjectionOperationType.DELETE_ITEM, OperationType.Delete, FaultInjectionServerErrorType.CONNECTION_DELAY, true},
            {FaultInjectionOperationType.PATCH_ITEM, OperationType.Patch, FaultInjectionServerErrorType.CONNECTION_DELAY, true},
            {FaultInjectionOperationType.READ_ITEM, OperationType.Read, FaultInjectionServerErrorType.GONE, false},
            {FaultInjectionOperationType.CREATE_ITEM, OperationType.Create, FaultInjectionServerErrorType.GONE, true},
            {FaultInjectionOperationType.REPLACE_ITEM, OperationType.Replace, FaultInjectionServerErrorType.GONE, true},
            {FaultInjectionOperationType.UPSERT_ITEM, OperationType.Upsert, FaultInjectionServerErrorType.GONE, true},
            {FaultInjectionOperationType.QUERY_ITEM, OperationType.Query, FaultInjectionServerErrorType.GONE, false},
            {FaultInjectionOperationType.DELETE_ITEM, OperationType.Delete, FaultInjectionServerErrorType.GONE, true},
            {FaultInjectionOperationType.PATCH_ITEM, OperationType.Patch, FaultInjectionServerErrorType.GONE, true},
            {FaultInjectionOperationType.READ_ITEM, OperationType.Read, FaultInjectionServerErrorType.PARTITION_IS_MIGRATING, false},
            {FaultInjectionOperationType.CREATE_ITEM, OperationType.Create, FaultInjectionServerErrorType.PARTITION_IS_MIGRATING, true},
            {FaultInjectionOperationType.REPLACE_ITEM, OperationType.Replace, FaultInjectionServerErrorType.PARTITION_IS_MIGRATING, true},
            {FaultInjectionOperationType.UPSERT_ITEM, OperationType.Upsert, FaultInjectionServerErrorType.PARTITION_IS_MIGRATING, true},
            {FaultInjectionOperationType.QUERY_ITEM, OperationType.Query, FaultInjectionServerErrorType.PARTITION_IS_MIGRATING, false},
            {FaultInjectionOperationType.DELETE_ITEM, OperationType.Delete, FaultInjectionServerErrorType.PARTITION_IS_MIGRATING, true},
            {FaultInjectionOperationType.PATCH_ITEM, OperationType.Patch, FaultInjectionServerErrorType.PARTITION_IS_MIGRATING, true},
        };
    }

    @DataProvider(name = "metadataRetryPolicyTestContext")
    public Object[][] metadataRetryPolicyTestContext() {

        return new Object[][] {
            {
                new SocketException("Socket has been closed"),
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE,
                createRequest(OperationType.Create, ResourceType.Document, true, true),
                true /* isNetworkFailure */
            },
            {
                new SocketException("Socket has been closed"),
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE,
                createRequest(OperationType.Read, ResourceType.Document, true, true),
                true /* isNetworkFailure */
            },
            {
                new SocketException("Socket has been closed"),
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE,
                createRequest(OperationType.Read, ResourceType.DatabaseAccount, false, true),
                true /* isNetworkFailure */
            },
            {
                new NotFoundException(),
                HttpConstants.StatusCodes.NOTFOUND,
                HttpConstants.SubStatusCodes.UNKNOWN,
                createRequest(OperationType.Read, ResourceType.Document, true, true),
                false /* isNetworkFailure */
            },
            {
                new NotFoundException(),
                HttpConstants.StatusCodes.NOTFOUND,
                HttpConstants.SubStatusCodes.UNKNOWN,
                createRequest(OperationType.Create, ResourceType.Document, true, true),
                false /* isNetworkFailure */
            }
        };
    }

    // this test does the following
    // Case 1: Region outage
    // 1. Simulate a region issue by injecting connection establishment failures with replicas.
    // 2. Keep a low end-to-end timeout but a high connectTimeout - this will cause the operation
    // to be cancelled before connectTimeout is reached.
    // 3. This will help us to verify two things:
    //      3.1 Connection establishment is not affected by cancellation of the operation.
    //      3.2 Using a cancellation status on the request associated with the operation
    //          we can trigger force address refresh calls in the background when connectionTimeout
    //          is also hit.
    // Case 2: Possibility of stale caches when requests get cancelled due to end-to-end timeout and
    //         server-generated 410s are received by the SDK
    // 1. Inject server-generated 410 exceptions
    // 2. Keep a low end-to-end timeout but a high amount of time for which the 410 fault is applied. This will lead
    // to the request to be cancelled.
    // 3. This will help us to verify two things:
    //      3.2 Using a cancellation status on the request associated with the operation
    //          we can trigger force address refresh calls in the background when server-side generated 410s are thrown.
    @Test(groups = {"multi-region"}, dataProvider = "operationContext", timeOut = TIMEOUT)
    public void forceBackgroundAddressRefresh_onConnectionTimeoutAndRequestCancellation_test(
        FaultInjectionOperationType faultInjectionOperationType,
        OperationType operationType,
        FaultInjectionServerErrorType faultInjectionServerErrorType,
        boolean isWrite) {

        // Get preferred regions
        List<String> preferredRegions = (isWrite) ? this.writeRegionMap.keySet().stream().collect(Collectors.toList())
            : this.readRegionMap.keySet().stream().collect(Collectors.toList());

        DirectConnectionConfig directConnectionConfig = new DirectConnectionConfig();

        if (faultInjectionServerErrorType == FaultInjectionServerErrorType.CONNECTION_DELAY) {
            directConnectionConfig.setConnectTimeout(Duration.ofSeconds(3));
            directConnectionConfig.setIdleConnectionTimeout(Duration.ofSeconds(1));
        }

        CosmosAsyncClient client = null;
        CosmosAsyncDatabase database = null;
        CosmosAsyncContainer container = null;
        FaultInjectionRule faultInjectionRule = null;

        try {

            client = this.clientBuilder
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .directMode(directConnectionConfig)
                .consistencyLevel(ConsistencyLevel.SESSION)
                .preferredRegions(preferredRegions)
                .endpointDiscoveryEnabled(true)
                .buildAsyncClient();

            Configs configs = new Configs();

            RxDocumentClientImpl asyncDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(client);
            GlobalAddressResolver globalAddressResolver = ReflectionUtils.getGlobalAddressResolver(asyncDocumentClient);
            GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(asyncDocumentClient);

            List<RegionalRoutingContext> readEndpoints = globalEndpointManager.getReadEndpoints();

            Map<URI, GlobalAddressResolver.EndpointCache> endpointCacheByURIMap = globalAddressResolver.addressCacheByEndpoint;

            Map<String, HttpClientUnderTestWrapper> httpClientWrapperByRegionMap = new ConcurrentHashMap<>();

            for (int i = 0; i < preferredRegions.size(); i++) {
                URI readEndpoint = readEndpoints.get(i).getGatewayRegionalEndpoint();
                GlobalAddressResolver.EndpointCache endpointCache = endpointCacheByURIMap.get(readEndpoint);
                GatewayAddressCache gatewayAddressCache = endpointCache.addressCache;
                HttpClientUnderTestWrapper httpClientUnderTestWrapper = getHttpClientUnderTestWrapper(configs);
                ReflectionUtils.setHttpClient(gatewayAddressCache, httpClientUnderTestWrapper.getSpyHttpClient());
                httpClientWrapperByRegionMap.put(preferredRegions.get(i), httpClientUnderTestWrapper);
            }

            String faultInjectedRegion = preferredRegions.get(0);
            String dbId = UUID.randomUUID().toString();
            String containerId = UUID.randomUUID().toString();

            client.createDatabase(dbId).block();
            database = client.getDatabase(dbId);

            database.createContainer(containerId, "/mypk").block();
            container = database.getContainer(containerId);

            // fault injection setup to inject a connection delay
            // this connection delay injection will trigger connectTimeoutExceptions
            FaultInjectionCondition faultInjectionCondition = new FaultInjectionConditionBuilder()
                .connectionType(FaultInjectionConnectionType.DIRECT)
                .region(faultInjectedRegion)
                .operationType(faultInjectionOperationType)
                .build();

            FaultInjectionServerErrorResult faultInjectionServerErrorResult;

            if (faultInjectionServerErrorType == FaultInjectionServerErrorType.CONNECTION_DELAY) {
                faultInjectionServerErrorResult = FaultInjectionResultBuilders
                    .getResultBuilder(FaultInjectionServerErrorType.CONNECTION_DELAY)
                    .delay(Duration.ofSeconds(45))
                    .build();
            } else {
                faultInjectionServerErrorResult = FaultInjectionResultBuilders
                    .getResultBuilder(faultInjectionServerErrorType)
                    .build();
            }

            faultInjectionRule = new FaultInjectionRuleBuilder("connection-delay-" + UUID.randomUUID())
                .condition(faultInjectionCondition)
                .result(faultInjectionServerErrorResult)
                .duration(Duration.ofMinutes(5))
                .build();

            // keep a low operation-level e2e timeout when compared to connection timeout
            CosmosEndToEndOperationLatencyPolicyConfig cosmosEndToEndOperationLatencyPolicyConfigForFaultyOperation
                = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofMillis(500)).build();

            TestObject testItem = new TestObject(UUID.randomUUID().toString(), UUID.randomUUID().toString(), Arrays.asList(), UUID.randomUUID().toString());

            performDocumentOperation(
                container,
                testItem,
                operationType,
                faultInjectionRule,
                httpClientWrapperByRegionMap.get(faultInjectedRegion),
                cosmosEndToEndOperationLatencyPolicyConfigForFaultyOperation);

            // allow enough time for operation and connection establishment to timeout
            Thread.sleep(6000);

            assertThat(faultInjectionRule.getHitCount()).isGreaterThanOrEqualTo(1);

            // track if force address refresh calls have been made
            assertThat(httpClientWrapperByRegionMap.get(faultInjectedRegion).capturedRequests).isNotNull();
            assertThat(httpClientWrapperByRegionMap.get(faultInjectedRegion).capturedRequests.size()).isGreaterThanOrEqualTo(1);
        } catch (InterruptedException e) {
            logger.error("InterruptedException thrown...");
        } finally {
            safeDeleteCollection(container);
            safeDeleteDatabase(database);
            safeClose(client);
        }
    }

    @Test(groups = {"unit"}, dataProvider = "metadataRetryPolicyTestContext", timeOut = TIMEOUT)
    public void metadataRetryPolicyTest(
        Exception exception,
        int statusCode,
        int subStatusCode,
        RxDocumentServiceRequest request,
        boolean isNetworkFailure) {

        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        MetadataRequestRetryPolicy metadataRequestRetryPolicy = new MetadataRequestRetryPolicy(globalEndpointManagerMock);

        metadataRequestRetryPolicy.onBeforeSendRequest(request);

        CosmosException cosmosException = BridgeInternal.createCosmosException(null, statusCode, exception);
        BridgeInternal.setSubStatusCode(cosmosException, subStatusCode);

        if (isNetworkFailure) {
            int totalRetryCount = HttpTimeoutPolicyControlPlaneHotPath.INSTANCE.totalRetryCount();
            for (int i = 0; i <= totalRetryCount; i++) {
                Mono<ShouldRetryResult> shouldRetry = metadataRequestRetryPolicy.shouldRetry(cosmosException);
                if (i < totalRetryCount) {
                    validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                        .shouldRetry(true)
                        .build());
                } else {
                    validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                        .shouldRetry(false)
                        .withException(cosmosException)
                        .build());

                    int desiredInvocationCount = request.requestContext.regionalRoutingContextToRoute == null ? 0 : 1;

                    if (request.isReadOnlyRequest()) {
                        Mockito
                            .verify(globalEndpointManagerMock, Mockito.times(desiredInvocationCount))
                            .markEndpointUnavailableForRead(Mockito.any());
                    } else {
                        Mockito
                            .verify(globalEndpointManagerMock, Mockito.times(desiredInvocationCount))
                            .markEndpointUnavailableForWrite(Mockito.any());
                    }
                }
            }
        } else {
            Mono<ShouldRetryResult> shouldRetry = metadataRequestRetryPolicy.shouldRetry(cosmosException);
            validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                .shouldRetry(false)
                .withException(cosmosException)
                .build());
            Mockito
                .verify(globalEndpointManagerMock, Mockito.times(0))
                .markEndpointUnavailableForRead(Mockito.any());
            Mockito
                .verify(globalEndpointManagerMock, Mockito.times(0))
                .markEndpointUnavailableForWrite(Mockito.any());
        }
    }

    private void performDocumentOperation(
        CosmosAsyncContainer faultInjectedContainer,
        TestObject testItem,
        OperationType faultInjectedOperationType,
        FaultInjectionRule connectionDelayFault,
        HttpClientUnderTestWrapper httpClientUnderTestWrapper,
        CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfigForFaultyOperation) throws InterruptedException {

        final int idleTimeInMillis = 5000;

        // allow collection to be available for read
        Thread.sleep(5_000);

        if (faultInjectedOperationType == OperationType.Query) {
            faultInjectedContainer
                .createItem(testItem, new PartitionKey(testItem.getMypk()), new CosmosItemRequestOptions())
                .block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(faultInjectedContainer, Arrays.asList(connectionDelayFault)).block();

            httpClientUnderTestWrapper.capturedRequests.clear();

            // allow enough time for connections to be deemed unhealthy and their closure
            // due to idleConnectionTimeout being reached
            Thread.sleep(idleTimeInMillis);

            String query = String.format("SELECT * FROM c WHERE c.id = '%s'", testItem.getId());

            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions()
                .setCosmosEndToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfigForFaultyOperation);

            SqlQuerySpec sqlQuerySpec = new SqlQuerySpec(query);
            faultInjectedContainer
                .queryItems(sqlQuerySpec, queryRequestOptions, TestObject.class)
                .byPage()
                .subscribe();
        } else if (faultInjectedOperationType == OperationType.Read) {

            faultInjectedContainer
                .createItem(testItem, new PartitionKey(testItem.getMypk()), new CosmosItemRequestOptions())
                .block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(faultInjectedContainer, Arrays.asList(connectionDelayFault)).block();

            httpClientUnderTestWrapper.capturedRequests.clear();
            Thread.sleep(idleTimeInMillis);

            CosmosItemRequestOptions requestOptionsForRead = new CosmosItemRequestOptions()
                .setCosmosEndToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfigForFaultyOperation);

            faultInjectedContainer
                .readItem(testItem.getId(), new PartitionKey(testItem.getMypk()), requestOptionsForRead, TestObject.class)
                .subscribe();
        } else if (faultInjectedOperationType == OperationType.Create) {

            CosmosFaultInjectionHelper.configureFaultInjectionRules(faultInjectedContainer, Arrays.asList(connectionDelayFault)).block();

            CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions()
                .setCosmosEndToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfigForFaultyOperation);

            faultInjectedContainer
                .createItem(testItem, new PartitionKey(testItem.getMypk()), itemRequestOptions)
                .subscribe();
        } else if (faultInjectedOperationType == OperationType.Replace) {

            faultInjectedContainer
                .createItem(testItem, new PartitionKey(testItem.getMypk()), new CosmosItemRequestOptions())
                .block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(faultInjectedContainer, Arrays.asList(connectionDelayFault)).block();

            httpClientUnderTestWrapper.capturedRequests.clear();
            Thread.sleep(idleTimeInMillis);

            CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions()
                .setCosmosEndToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfigForFaultyOperation);

            faultInjectedContainer
                .replaceItem(testItem, testItem.getId(), new PartitionKey(testItem.getMypk()), cosmosItemRequestOptions)
                .subscribe();
        } else if (faultInjectedOperationType == OperationType.Delete) {

            faultInjectedContainer
                .createItem(testItem, new PartitionKey(testItem.getMypk()), new CosmosItemRequestOptions())
                .block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(faultInjectedContainer, Arrays.asList(connectionDelayFault)).block();

            httpClientUnderTestWrapper.capturedRequests.clear();
            Thread.sleep(idleTimeInMillis);

            CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions()
                .setCosmosEndToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfigForFaultyOperation);

            faultInjectedContainer
                .deleteItem(testItem.getId(), new PartitionKey(testItem.getMypk()), cosmosItemRequestOptions)
                .subscribe();
        } else if (faultInjectedOperationType == OperationType.Upsert) {
            CosmosFaultInjectionHelper.configureFaultInjectionRules(faultInjectedContainer, Arrays.asList(connectionDelayFault)).block();

            CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions()
                .setCosmosEndToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfigForFaultyOperation);

            faultInjectedContainer
                .upsertItem(testItem, new PartitionKey(testItem.getMypk()), cosmosItemRequestOptions)
                .subscribe();
        } else if (faultInjectedOperationType == OperationType.Patch) {
            CosmosPatchOperations patchOperations = CosmosPatchOperations.create().add("/" + "newProperty", "newVal");
            CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();

            faultInjectedContainer
                .createItem(testItem, new PartitionKey(testItem.getMypk()), itemRequestOptions)
                .block();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(faultInjectedContainer, Arrays.asList(connectionDelayFault)).block();

            httpClientUnderTestWrapper.capturedRequests.clear();
            Thread.sleep(idleTimeInMillis);

            faultInjectedContainer
                .patchItem(testItem.getId(), new PartitionKey(testItem.getMypk()), patchOperations, TestObject.class)
                .subscribe();
        }
    }

    private HttpClientUnderTestWrapper getHttpClientUnderTestWrapper(Configs configs) {
        HttpClient origHttpClient = getHttpClient(configs);
        return new HttpClientUnderTestWrapper(origHttpClient);
    }

    private HttpClient getHttpClient(Configs configs) {
        return HttpClient.createFixed(new HttpClientConfig(configs));
    }

    private Map<String, String> getRegionsMap(DatabaseAccount databaseAccount, boolean isWriteableRegions) {

        Map<String, String> regionMap = new ConcurrentHashMap<>();

        Iterator<DatabaseAccountLocation> databaseAccountLocationIterator = isWriteableRegions ?
            databaseAccount.getWritableLocations().iterator() : databaseAccount.getReadableLocations().iterator();

        while (databaseAccountLocationIterator.hasNext()) {
            DatabaseAccountLocation databaseAccountLocation = databaseAccountLocationIterator.next();
            regionMap.put(databaseAccountLocation.getName(), databaseAccountLocation.getEndpoint());
        }

        return regionMap;
    }

    public static void validateSuccess(Mono<ShouldRetryResult> single,
                                       ShouldRetryValidator validator) {
        validateSuccess(single, validator, TIMEOUT);
    }

    public static void validateSuccess(Mono<ShouldRetryResult> single,
                                       ShouldRetryValidator validator,
                                       long timeout) {
        TestSubscriber<ShouldRetryResult> testSubscriber = new TestSubscriber<>();

        single.flux().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
        validator.validate(testSubscriber.values().get(0));
    }

    private static RxDocumentServiceRequest createRequest(
        OperationType operationType,
        ResourceType resourceType,
        boolean hasLocationEndpointToRoute,
        boolean isAddressRefresh) {

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(), operationType, resourceType);

        assert request.requestContext != null;

        URI locationEndpointToRoute
            = URI.create("https://account-name-some-region.documents.azure.com:443");

        if (hasLocationEndpointToRoute) {
            request.requestContext.regionalRoutingContextToRoute = new RegionalRoutingContext(locationEndpointToRoute);
        } else {
            request.setEndpointOverride(locationEndpointToRoute);
        }

        if (isAddressRefresh) {
            request.setAddressRefresh(true, true);
        }

        return request;
    }
}

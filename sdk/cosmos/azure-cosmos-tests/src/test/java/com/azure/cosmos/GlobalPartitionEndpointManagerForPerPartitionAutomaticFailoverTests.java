// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.StoreResponseBuilder;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.math.util.Pair;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.directconnectivity.ConsistencyReader;
import com.azure.cosmos.implementation.directconnectivity.ConsistencyWriter;
import com.azure.cosmos.implementation.directconnectivity.GatewayAddressCache;
import com.azure.cosmos.implementation.directconnectivity.GlobalAddressResolver;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.ReplicatedResourceClient;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.directconnectivity.StoreClient;
import com.azure.cosmos.implementation.directconnectivity.StoreReader;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.TransportClient;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.cosmos.implementation.guava25.base.Function;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosReadManyRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobalPartitionEndpointManagerForPerPartitionAutomaticFailoverTests extends TestSuiteBase {

    private CosmosAsyncClient cosmosAsyncClient;
    private CosmosAsyncDatabase sharedDatabase;
    private CosmosAsyncContainer sharedSinglePartitionContainer;
    private AccountLevelLocationContext accountLevelLocationReadableLocationContext;
    private static final ImplementationBridgeHelpers.CosmosClientBuilderHelper.CosmosClientBuilderAccessor COSMOS_CLIENT_BUILDER_ACCESSOR
        = ImplementationBridgeHelpers.CosmosClientBuilderHelper.getCosmosClientBuilderAccessor();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasTwoRegions = (ctx) -> {
        assertThat(ctx).isNotNull();
        assertThat(ctx.getContactedRegionNames()).isNotNull();
        assertThat(ctx.getContactedRegionNames().size()).isLessThanOrEqualTo(2);
    };

    Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasOneRegion = (ctx) -> {
        assertThat(ctx).isNotNull();
        assertThat(ctx.getContactedRegionNames()).isNotNull();
        assertThat(ctx.getContactedRegionNames().size()).isLessThanOrEqualTo(1);
    };

    Consumer<ResponseWrapper<?>> validateResponseHasSuccess = (responseWrapper) -> {

        assertThat(responseWrapper.cosmosException).isNull();

        if (responseWrapper.feedResponse != null) {
            assertThat(responseWrapper.feedResponse.getCosmosDiagnostics()).isNotNull();
            assertThat(responseWrapper.feedResponse.getCosmosDiagnostics().getDiagnosticsContext()).isNotNull();

            CosmosDiagnosticsContext diagnosticsContext = responseWrapper.feedResponse.getCosmosDiagnostics().getDiagnosticsContext();

            assertThat(diagnosticsContext.getStatusCode() == HttpConstants.StatusCodes.OK || diagnosticsContext.getStatusCode() == HttpConstants.StatusCodes.NOT_MODIFIED).isTrue();
        } else if (responseWrapper.cosmosItemResponse != null) {
            assertThat(responseWrapper.cosmosItemResponse.getDiagnostics()).isNotNull();
            assertThat(responseWrapper.cosmosItemResponse.getDiagnostics().getDiagnosticsContext()).isNotNull();

            CosmosDiagnosticsContext diagnosticsContext = responseWrapper.cosmosItemResponse.getDiagnostics().getDiagnosticsContext();

            assertThat(HttpConstants.StatusCodes.OK <= diagnosticsContext.getStatusCode() && diagnosticsContext.getStatusCode() <= HttpConstants.StatusCodes.NO_CONTENT).isTrue();
        } else if (responseWrapper.batchResponse != null) {
            assertThat(responseWrapper.batchResponse.getDiagnostics()).isNotNull();
            assertThat(responseWrapper.batchResponse.getDiagnostics().getDiagnosticsContext()).isNotNull();

            CosmosDiagnosticsContext diagnosticsContext = responseWrapper.batchResponse.getDiagnostics().getDiagnosticsContext();

            assertThat(HttpConstants.StatusCodes.OK <= diagnosticsContext.getStatusCode() && diagnosticsContext.getStatusCode() <= HttpConstants.StatusCodes.NO_CONTENT).isTrue();
        }
    };

    @Factory(dataProvider = "clientBuildersWithDirectSession")
    public GlobalPartitionEndpointManagerForPerPartitionAutomaticFailoverTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"multi-region"})
    public void beforeClass() {
        this.cosmosAsyncClient = getClientBuilder().buildAsyncClient();
        this.sharedDatabase = getSharedCosmosDatabase(this.cosmosAsyncClient);
        this.sharedSinglePartitionContainer = getSharedSinglePartitionCosmosContainer(this.cosmosAsyncClient);

        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(this.cosmosAsyncClient);
        GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
        DatabaseAccount databaseAccountSnapshot = globalEndpointManager.getLatestDatabaseAccount();

        this.accountLevelLocationReadableLocationContext = getAccountLevelLocationContext(databaseAccountSnapshot, false);
    }

    @DataProvider(name = "ppafWithServiceUnavailableConfigs")
    public Object[][] ppafWithServiceUnavailableConfigs() {

        return new Object[][]{
            {
                OperationType.Create,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410
            },
            {
                OperationType.Replace,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410
            },
            {
                OperationType.Upsert,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410
            },
            {
                OperationType.Delete,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410
            },
            {
                OperationType.Patch,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410
            }
        };
    }

    @Test(groups = {"multi-region"}, dataProvider = "ppafWithServiceUnavailableConfigs")
    public void testPpafWithServiceUnavailable() throws URISyntaxException, JsonProcessingException {

        TransportClient transportClientMock = Mockito.mock(TransportClient.class);
        List<String> preferredRegions = this.accountLevelLocationReadableLocationContext.serviceOrderedReadableRegions;
        Map<String, String> readableRegionNameToEndpoint = this.accountLevelLocationReadableLocationContext.regionNameToEndpoint;


        if (COSMOS_CLIENT_BUILDER_ACCESSOR.getConnectionPolicy(getClientBuilder()).getConnectionMode() == ConnectionMode.GATEWAY) {
            return;
        }

        try {

            // warm up client
            CosmosContainerProactiveInitConfig proactiveInitConfig = new CosmosContainerProactiveInitConfigBuilder(
                Arrays.asList(new CosmosContainerIdentity(this.sharedDatabase.getId(), this.sharedSinglePartitionContainer.getId())))
                .setProactiveConnectionRegionsCount(2)
                .build();

            CosmosClientBuilder cosmosClientBuilder = getClientBuilder()
                .openConnectionsAndInitCaches(proactiveInitConfig)
                .preferredRegions(preferredRegions);

            COSMOS_CLIENT_BUILDER_ACCESSOR.setPerPartitionAutomaticFailoverEnabled(cosmosClientBuilder, true);

            CosmosAsyncClient asyncClient = getClientBuilder().buildAsyncClient();

            CosmosAsyncContainer asyncContainer = asyncClient
                .getDatabase(this.sharedDatabase.getId())
                .getContainer(this.sharedSinglePartitionContainer.getId());

            RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(asyncClient);

            StoreClient storeClient = ReflectionUtils.getStoreClient(rxDocumentClient);
            ReplicatedResourceClient replicatedResourceClient = ReflectionUtils.getReplicatedResourceClient(storeClient);
            ConsistencyReader consistencyReader = ReflectionUtils.getConsistencyReader(replicatedResourceClient);
            StoreReader storeReader = ReflectionUtils.getStoreReader(consistencyReader);

            ConsistencyWriter consistencyWriter = ReflectionUtils.getConsistencyWriter(replicatedResourceClient);

            RntbdTransportClient rntbdTransportClient = (RntbdTransportClient) ReflectionUtils.getTransportClient(replicatedResourceClient);
            RntbdEndpoint.Provider provider = ReflectionUtils.getRntbdEndpointProvider(rntbdTransportClient);

            GlobalAddressResolver globalAddressResolver = ReflectionUtils.getGlobalAddressResolver(rxDocumentClient);
            RxPartitionKeyRangeCache rxPartitionKeyRangeCache = ReflectionUtils.getPartitionKeyRangeCache(rxDocumentClient);

            Utils.ValueHolder<List<PartitionKeyRange>> partitionKeyRangesForContainer
                = getPartitionKeyRangesForContainer(asyncContainer, rxDocumentClient).block();

            assertThat(partitionKeyRangesForContainer.v).isNotNull();
            assertThat(partitionKeyRangesForContainer.v.size()).isGreaterThanOrEqualTo(1);

            PartitionKeyRange partitionKeyRangeWithIssues = partitionKeyRangesForContainer.v.get(0);

            List<Pair<GatewayAddressCache, String>> orderedGatewayAddressCacheToRegion = new ArrayList<>();

            for (String preferredRegion : preferredRegions) {
                String endpoint = readableRegionNameToEndpoint.get(preferredRegion);
                GatewayAddressCache gatewayAddressCache = globalAddressResolver.getGatewayAddressCache(new URI(endpoint));
                orderedGatewayAddressCacheToRegion.add(new Pair<>(gatewayAddressCache, preferredRegion));
            }

            assertThat(preferredRegions).isNotNull();
            assertThat(preferredRegions.size()).isGreaterThanOrEqualTo(1);

            String regionWithIssues = preferredRegions.get(0);
            URI locationEndpointWithIssues = new URI(readableRegionNameToEndpoint.get(regionWithIssues));

            ReflectionUtils.setTransportClient(storeReader, transportClientMock);
            ReflectionUtils.setTransportClient(consistencyWriter, transportClientMock);

            setupTransportClientToReturnSuccessResponse(transportClientMock, constructStoreResponse(201));
            setupTransportClientToThrowCosmosException(
                transportClientMock,
                partitionKeyRangeWithIssues,
                locationEndpointWithIssues,
                new GoneException("", HttpConstants.SubStatusCodes.SERVER_GENERATED_410));

            TestItem testItem = TestItem.createNewItem();

            CosmosItemResponse<TestItem> createItemResponseBeforeFailover = asyncContainer.createItem(testItem).block();

            assertThat(createItemResponseBeforeFailover).isNotNull();
            validateDiagnosticsContext(createItemResponseBeforeFailover.getDiagnostics(), 2, HttpConstants.StatusCodes.CREATED);

            CosmosItemResponse<TestItem> createItemResponseAfterFailover = asyncContainer.createItem(testItem).block();
            assertThat(createItemResponseAfterFailover).isNotNull();
            validateDiagnosticsContext(createItemResponseAfterFailover.getDiagnostics(), 1, HttpConstants.StatusCodes.CREATED);
        } finally {
        }
    }

    private void setupTransportClientToThrowCosmosException(
        TransportClient transportClientMock,
        PartitionKeyRange partitionKeyRange,
        URI locationEndpointToRoute,
        CosmosException cosmosException) {

        Mockito.when(
            transportClientMock.invokeResourceOperationAsync(
                Mockito.any(),
                Mockito.argThat(argument ->
                    argument.requestContext.resolvedPartitionKeyRange
                        .getId()
                        .equals(partitionKeyRange.getId()) &&
                        argument.requestContext.locationEndpointToRoute.equals(locationEndpointToRoute))))
            .thenReturn(Mono.error(cosmosException));
    }

    private void setupTransportClientToReturnSuccessResponse(
        TransportClient transportClientMock,
        StoreResponse storeResponse) {

        Mockito.when(transportClientMock.invokeResourceOperationAsync(Mockito.any(), Mockito.any())).thenReturn(Mono.just(storeResponse));
    }

    private Mono<Utils.ValueHolder<List<PartitionKeyRange>>> getPartitionKeyRangesForContainer(
        CosmosAsyncContainer cosmosAsyncContainer, RxDocumentClientImpl rxDocumentClient) {
        return Mono.just(cosmosAsyncContainer)
            .flatMap(CosmosAsyncContainer::read)
            .flatMap(containerResponse -> rxDocumentClient
                .getPartitionKeyRangeCache()
                .tryGetOverlappingRangesAsync(
                    null,
                    containerResponse.getProperties().getResourceId(),
                    PartitionKeyInternalHelper.FullRange,
                    false,
                    null));
    }

    private AccountLevelLocationContext getAccountLevelLocationContext(DatabaseAccount databaseAccount, boolean writeOnly) {
        Iterator<DatabaseAccountLocation> locationIterator =
            writeOnly ? databaseAccount.getWritableLocations().iterator() : databaseAccount.getReadableLocations().iterator();

        List<String> serviceOrderedReadableRegions = new ArrayList<>();
        List<String> serviceOrderedWriteableRegions = new ArrayList<>();
        Map<String, String> regionMap = new ConcurrentHashMap<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            regionMap.put(accountLocation.getName(), accountLocation.getEndpoint());

            if (writeOnly) {
                serviceOrderedWriteableRegions.add(accountLocation.getName());
            } else {
                serviceOrderedReadableRegions.add(accountLocation.getName());
            }
        }

        return new AccountLevelLocationContext(
            serviceOrderedReadableRegions,
            serviceOrderedWriteableRegions,
            regionMap);
    }

    private void validateDiagnosticsContext(
        CosmosDiagnostics actualCosmosDiagnostics,
        int expectedRegionsContacted,
        int expectedStatusCode) {

        assertThat(actualCosmosDiagnostics).isNotNull();
        assertThat(actualCosmosDiagnostics.getDiagnosticsContext()).isNotNull();
        assertThat(actualCosmosDiagnostics.getDiagnosticsContext().getContactedRegionNames()).isNotNull();
        assertThat(actualCosmosDiagnostics.getDiagnosticsContext().getContactedRegionNames()).isNotEmpty();
        assertThat(actualCosmosDiagnostics.getDiagnosticsContext().getContactedRegionNames()).hasSize(expectedRegionsContacted);
        assertThat(actualCosmosDiagnostics.getDiagnosticsContext().getStatusCode()).isEqualTo(expectedStatusCode);
    }

    private StoreResponse constructStoreResponse(int statusCode) throws JsonProcessingException {
        return StoreResponseBuilder.create()
            .withContent(OBJECT_MAPPER.writeValueAsString(getTestPojoObject()))
            .withStatus(statusCode)
            .build();
    }

    private static class AccountLevelLocationContext {
        private final List<String> serviceOrderedReadableRegions;
        private final List<String> serviceOrderedWriteableRegions;
        private final Map<String, String> regionNameToEndpoint;

        public AccountLevelLocationContext(
            List<String> serviceOrderedReadableRegions,
            List<String> serviceOrderedWriteableRegions,
            Map<String, String> regionNameToEndpoint) {

            this.serviceOrderedReadableRegions = serviceOrderedReadableRegions;
            this.serviceOrderedWriteableRegions = serviceOrderedWriteableRegions;
            this.regionNameToEndpoint = regionNameToEndpoint;
        }
    }

    private TestPojo getTestPojoObject() {
        TestPojo testPojo = new TestPojo();
        String uuid = UUID.randomUUID().toString();
        testPojo.setId(uuid);
        testPojo.setMypk(uuid);
        return testPojo;
    }

    private static Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> resolveDataPlaneOperation(OperationType operationType) {

        switch (operationType) {
            case Read:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestObject createdTestObject = paramsWrapper.createdTestObject;
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<TestObject> readItemResponse = asyncContainer.readItem(
                                createdTestObject.getId(),
                                new PartitionKey(createdTestObject.getId()),
                                itemRequestOptions,
                                TestObject.class)
                            .block();

                        return new ResponseWrapper<>(readItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case Upsert:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestObject createdTestObject = paramsWrapper.createdTestObject;
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<TestObject> upsertItemResponse = asyncContainer.upsertItem(
                                createdTestObject,
                                new PartitionKey(createdTestObject.getId()),
                                itemRequestOptions)
                            .block();

                        return new ResponseWrapper<>(upsertItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case Create:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestObject createdTestObject = TestObject.create();
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<TestObject> createItemResponse = asyncContainer.createItem(
                                createdTestObject,
                                new PartitionKey(createdTestObject.getId()),
                                itemRequestOptions)
                            .block();

                        return new ResponseWrapper<>(createItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case Delete:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestObject createdTestObject = paramsWrapper.createdTestObject;
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<Object> deleteItemResponse = asyncContainer.deleteItem(
                                createdTestObject.getId(),
                                new PartitionKey(createdTestObject.getId()),
                                itemRequestOptions)
                            .block();

                        return new ResponseWrapper<>(deleteItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case Patch:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestObject createdTestObject = paramsWrapper.createdTestObject;
                    CosmosPatchItemRequestOptions patchItemRequestOptions = (CosmosPatchItemRequestOptions) paramsWrapper.patchItemRequestOptions;

                    CosmosPatchOperations patchOperations = CosmosPatchOperations.create().add("/number", 555);

                    try {

                        CosmosItemResponse<TestObject> patchItemResponse = asyncContainer.patchItem(
                                createdTestObject.getId(),
                                new PartitionKey(createdTestObject.getId()),
                                patchOperations,
                                patchItemRequestOptions,
                                TestObject.class)
                            .block();

                        return new ResponseWrapper<>(patchItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case Query:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    CosmosQueryRequestOptions queryRequestOptions = paramsWrapper.queryRequestOptions == null ? new CosmosQueryRequestOptions() : paramsWrapper.queryRequestOptions;
                    queryRequestOptions = paramsWrapper.feedRangeForQuery == null ? queryRequestOptions.setFeedRange(FeedRange.forFullRange()) : queryRequestOptions.setFeedRange(paramsWrapper.feedRangeForQuery);

                    try {

                        FeedResponse<TestObject> queryItemResponse = asyncContainer.queryItems(
                                "SELECT * FROM C",
                                queryRequestOptions,
                                TestObject.class)
                            .byPage()
                            .blockLast();

                        return new ResponseWrapper<>(queryItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case Replace:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestObject createdTestObject = paramsWrapper.createdTestObject;
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<TestObject> deleteItemResponse = asyncContainer.replaceItem(
                                createdTestObject,
                                createdTestObject.getId(),
                                new PartitionKey(createdTestObject.getId()),
                                itemRequestOptions)
                            .block();

                        return new ResponseWrapper<>(deleteItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case Batch:
                return (paramsWrapper) -> {

                    TestObject testObject = TestObject.create();
                    CosmosBatch batch = CosmosBatch.createCosmosBatch(new PartitionKey(testObject.getId()));
                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;

                    batch.createItemOperation(testObject);
                    batch.readItemOperation(testObject.getId());

                    try {
                        CosmosBatchResponse batchResponse = asyncContainer.executeCosmosBatch(batch).block();
                        return new ResponseWrapper<>(batchResponse);
                    } catch (Exception ex) {
                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case ReadFeed:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;

                    try {

                        FeedResponse<TestObject> feedResponseFromChangeFeed = asyncContainer.queryChangeFeed(
                                CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(paramsWrapper.feedRangeToDrainForChangeFeed == null ? FeedRange.forFullRange() : paramsWrapper.feedRangeToDrainForChangeFeed),
                                TestObject.class)
                            .byPage()
                            .blockLast();

                        return new ResponseWrapper<>(feedResponseFromChangeFeed);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            default:
                throw new UnsupportedOperationException(String.format("Operation of type : %s is not supported", operationType));
        }
    }


    private static class ResponseWrapper<T> {

        private final CosmosItemResponse<T> cosmosItemResponse;
        private final CosmosException cosmosException;
        private final FeedResponse<T> feedResponse;
        private final CosmosBatchResponse batchResponse;

        ResponseWrapper(FeedResponse<T> feedResponse) {
            this.feedResponse = feedResponse;
            this.cosmosException = null;
            this.cosmosItemResponse = null;
            this.batchResponse = null;
        }

        ResponseWrapper(CosmosItemResponse<T> cosmosItemResponse) {
            this.cosmosItemResponse = cosmosItemResponse;
            this.cosmosException = null;
            this.feedResponse = null;
            this.batchResponse = null;
        }

        ResponseWrapper(CosmosException cosmosException) {
            this.cosmosException = cosmosException;
            this.cosmosItemResponse = null;
            this.feedResponse = null;
            this.batchResponse = null;
        }

        ResponseWrapper(CosmosBatchResponse batchResponse) {
            this.cosmosException = null;
            this.cosmosItemResponse = null;
            this.feedResponse = null;
            this.batchResponse = batchResponse;
        }
    }

    private static class OperationInvocationParamsWrapper {
        public CosmosAsyncContainer asyncContainer;
        public TestObject createdTestObject;
        public CosmosItemRequestOptions itemRequestOptions;
        public CosmosQueryRequestOptions queryRequestOptions;
        public CosmosReadManyRequestOptions readManyRequestOptions;
        public CosmosItemRequestOptions patchItemRequestOptions;
        public FeedRange feedRangeToDrainForChangeFeed;
        public FeedRange feedRangeForQuery;
        public List<CosmosItemIdentity> itemIdentitiesForReadManyOperation;
        public PartitionKey partitionKeyForReadAllOperation;
        public String containerIdToTarget;
        public int itemCountToBootstrapContainerFrom;
        public FeedRange faultyFeedRange;
        public List<TestObject> testObjectsForDataPlaneOperationToWorkWith;
    }
}

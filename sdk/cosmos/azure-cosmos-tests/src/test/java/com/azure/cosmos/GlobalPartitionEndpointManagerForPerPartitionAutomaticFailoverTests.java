// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
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
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
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

import static org.assertj.core.api.Assertions.assertThat;

public class GlobalPartitionEndpointManagerForPerPartitionAutomaticFailoverTests extends TestSuiteBase {

    private CosmosAsyncClient cosmosAsyncClient;
    private CosmosAsyncDatabase sharedDatabase;
    private CosmosAsyncContainer sharedSinglePartitionContainer;
    private AccountLevelLocationContext accountLevelLocationReadableLocationContext;
    private static final ImplementationBridgeHelpers.CosmosClientBuilderHelper.CosmosClientBuilderAccessor COSMOS_CLIENT_BUILDER_ACCESSOR
        = ImplementationBridgeHelpers.CosmosClientBuilderHelper.getCosmosClientBuilderAccessor();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

    @Test(groups = {"multi-region"})
    public void testPpafWithServiceUnavailable() throws URISyntaxException, JsonProcessingException {

        TransportClient transportClientMock = Mockito.mock(TransportClient.class);
        List<String> preferredRegions = this.accountLevelLocationReadableLocationContext.serviceOrderedReadableRegions;
        Map<String, String> readableRegionNameToEndpoint = this.accountLevelLocationReadableLocationContext.regionNameToEndpoint;

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
}

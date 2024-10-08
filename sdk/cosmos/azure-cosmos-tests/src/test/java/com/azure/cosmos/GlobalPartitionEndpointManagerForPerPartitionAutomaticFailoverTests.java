// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.apachecommons.math.util.Pair;
import com.azure.cosmos.implementation.directconnectivity.ConsistencyReader;
import com.azure.cosmos.implementation.directconnectivity.GatewayAddressCache;
import com.azure.cosmos.implementation.directconnectivity.GlobalAddressResolver;
import com.azure.cosmos.implementation.directconnectivity.IAddressResolver;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.ReplicatedResourceClient;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.directconnectivity.StoreClient;
import com.azure.cosmos.implementation.directconnectivity.StoreReader;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.azure.cosmos.rx.TestSuiteBase;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalPartitionEndpointManagerForPerPartitionAutomaticFailoverTests extends TestSuiteBase {

    private CosmosAsyncClient cosmosAsyncClient;
    private CosmosAsyncDatabase sharedDatabase;
    private CosmosAsyncContainer sharedSinglePartitionContainer;
    private AccountLevelLocationContext accountLevelLocationReadableLocationContext;


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
    public void testPpafWithServiceUnavailable() throws URISyntaxException {

        RntbdTransportClient rntbdTransportClientMock = Mockito.mock(RntbdTransportClient.class);
        List<String> preferredRegions = this.accountLevelLocationReadableLocationContext.serviceOrderedReadableRegions;
        Map<String, String> readableRegionNameToEndpoint = this.accountLevelLocationReadableLocationContext.regionNameToEndpoint;

        try {

            // warm up client
            CosmosContainerProactiveInitConfig proactiveInitConfig = new CosmosContainerProactiveInitConfigBuilder(
                Arrays.asList(new CosmosContainerIdentity(this.sharedDatabase.getId(), this.sharedSinglePartitionContainer.getId())))
                .setProactiveConnectionRegionsCount(2)
                .build();

            CosmosAsyncClient asyncClient = getClientBuilder()
                .openConnectionsAndInitCaches(proactiveInitConfig)
                .preferredRegions(preferredRegions)
                .buildAsyncClient();

            RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(asyncClient);

            StoreClient storeClient = ReflectionUtils.getStoreClient(rxDocumentClient);
            ReplicatedResourceClient replicatedResourceClient = ReflectionUtils.getReplicatedResourceClient(storeClient);
            ConsistencyReader consistencyReader = ReflectionUtils.getConsistencyReader(replicatedResourceClient);
            StoreReader storeReader = ReflectionUtils.getStoreReader(consistencyReader);
            RntbdTransportClient rntbdTransportClient = (RntbdTransportClient) ReflectionUtils.getTransportClient(replicatedResourceClient);
            RntbdEndpoint.Provider provider = ReflectionUtils.getRntbdEndpointProvider(rntbdTransportClient);

            GlobalAddressResolver globalAddressResolver = ReflectionUtils.getGlobalAddressResolver(rxDocumentClient);
            List<Pair<GatewayAddressCache, String>> orderedGatewayAddressCacheToRegion = new ArrayList<>();

            for (String preferredRegion : preferredRegions) {
                String endpoint = readableRegionNameToEndpoint.get(preferredRegion);
                GatewayAddressCache gatewayAddressCache = globalAddressResolver.getGatewayAddressCache(new URI(endpoint));

                orderedGatewayAddressCacheToRegion.add(new Pair<>(gatewayAddressCache, preferredRegion));
            }

            ReflectionUtils.setTransportClient(storeReader, rntbdTransportClientMock);

        } finally {

        }
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
}

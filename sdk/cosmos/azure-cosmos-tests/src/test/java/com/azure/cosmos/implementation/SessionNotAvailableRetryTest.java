// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.apachecommons.lang.NotImplementedException;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.ConsistencyReader;
import com.azure.cosmos.implementation.directconnectivity.ConsistencyWriter;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.ReplicatedResourceClient;
import com.azure.cosmos.implementation.directconnectivity.StoreClient;
import com.azure.cosmos.implementation.directconnectivity.StoreReader;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.TransportClient;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.directconnectivity.rntbd.ProactiveOpenConnectionsProcessor;
import com.azure.cosmos.implementation.faultinjection.IFaultInjectorProvider;
import com.azure.cosmos.implementation.routing.LocationCache;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.doAnswer;

public class SessionNotAvailableRetryTest extends TestSuiteBase {
    private static final int TIMEOUT = 60000;
    private CosmosAsyncClient client;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private DatabaseAccount databaseAccount;

    @BeforeClass(groups = {"multi-region", "multi-master"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .directMode()
            .buildAsyncClient();
        AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(client);
        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
        GlobalEndpointManager globalEndpointManager =
            ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();
        this.databaseAccount = databaseAccount;

    }

    @AfterClass(groups = {"multi-region", "multi-master"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    @DataProvider(name = "preferredRegions")
    private Object[][] preferredRegions() {
        List<String> preferredLocations1 = new ArrayList<>();
        List<String> preferredLocations2 = new ArrayList<>();
        Iterator<DatabaseAccountLocation> locationIterator = this.databaseAccount.getReadableLocations().iterator();
        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            preferredLocations1.add(accountLocation.getName());
        }

        //putting preferences in opposite direction than what came from database account api
        for (int i = preferredLocations1.size() - 1; i >= 0; i--) {
            preferredLocations2.add(preferredLocations1.get(i));
        }

        return new Object[][]{
            new Object[]{preferredLocations1, OperationType.Read},
            new Object[]{preferredLocations2, OperationType.Read},
            new Object[]{preferredLocations1, OperationType.Query},
            new Object[]{preferredLocations2, OperationType.Query},
            new Object[]{preferredLocations1, OperationType.Create},
            new Object[]{preferredLocations2, OperationType.Create},
        };
    }

    @DataProvider(name = "operations")
    private Object[][] operations() {
        return new Object[][]{
            new Object[]{OperationType.Read},
            new Object[]{OperationType.Query},
            new Object[]{OperationType.Create},
        };
    }

    @Test(groups = {"multi-master"}, dataProvider = "preferredRegions", timeOut = TIMEOUT)
    public void sessionNotAvailableRetryMultiMaster(
        List<String> preferredLocations,
        OperationType operationType) {

        List<String> preferredLocationsWithLowerCase =
            preferredLocations.stream().map(location -> location.toLowerCase(Locale.ROOT)).collect(Collectors.toList());
        CosmosAsyncClient preferredListClient = null;
        // inject 404/1002 into all regions
        FaultInjectionRule sessionNotAvailableRule = new FaultInjectionRuleBuilder("sessionNotAvailableRuleMultiMaster-" + UUID.randomUUID())
            .condition(new FaultInjectionConditionBuilder().build())
            .result(
                FaultInjectionResultBuilders
                    .getResultBuilder(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                    .build())
            .build();

        try {
            preferredListClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .preferredRegions(preferredLocations)
                .buildAsyncClient();

            cosmosAsyncContainer = getSharedMultiPartitionCosmosContainer(preferredListClient);
            CosmosFaultInjectionHelper.configureFaultInjectionRules(cosmosAsyncContainer, Arrays.asList(sessionNotAvailableRule)).block();

            try {
                PartitionKey partitionKey = new PartitionKey("Test");
                if (operationType.equals(OperationType.Read)) {
                    cosmosAsyncContainer.readItem("Test", partitionKey, TestItem.class).block();
                } else if (operationType.equals(OperationType.Query)) {
                    String query = "Select * from C";
                    CosmosQueryRequestOptions requestOptions = new CosmosQueryRequestOptions();
                    requestOptions.setPartitionKey(partitionKey);
                    cosmosAsyncContainer.queryItems(query, requestOptions, TestItem.class).byPage().blockFirst();
                } else if (operationType.equals(OperationType.Create)) {
                    TestItem item = new TestItem();
                    item.setId("Test");
                    item.setMypk("Test");
                    cosmosAsyncContainer.createItem(item, partitionKey, new CosmosItemRequestOptions()).block();
                }

                fail("Request should fail with 404/1002 error");
            } catch (CosmosException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
                assertThat(ex.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE);
                assertThat(ex.getDiagnostics().getContactedRegionNames().size()).isEqualTo(preferredLocations.size());
                assertThat(ex.getDiagnostics().getContactedRegionNames().containsAll(preferredLocationsWithLowerCase)).isTrue();

                // validate the contacted regions follow the preferredRegion sequence
                List<String> contactedRegions = new ArrayList<>();
                String previousContactedRegion = StringUtils.EMPTY;
                ClientSideRequestStatistics clientSideRequestStatistics = BridgeInternal.getClientSideRequestStatics(ex.getDiagnostics());
                for (ClientSideRequestStatistics.StoreResponseStatistics storeResponseStatistics : clientSideRequestStatistics.getResponseStatisticsList()) {
                    if (!storeResponseStatistics.getRegionName().equalsIgnoreCase(previousContactedRegion)) {
                        contactedRegions.add(storeResponseStatistics.getRegionName().toLowerCase(Locale.ROOT));
                        previousContactedRegion = storeResponseStatistics.getRegionName().toLowerCase(Locale.ROOT);
                    }
                }
                List<String> expectedContactedRegions = new ArrayList<>();
                expectedContactedRegions.addAll(preferredLocationsWithLowerCase);
                // SDK will do one more round retry in first preferred region due to RenameCollectionAwareClientRetryPolicy
                expectedContactedRegions.add(preferredLocationsWithLowerCase.get(0));
                assertThat(contactedRegions.size()).isEqualTo(expectedContactedRegions.size());
                assertThat(contactedRegions.containsAll(expectedContactedRegions)).isTrue();
            }
        } finally {
            sessionNotAvailableRule.disable();
            safeClose(preferredListClient);
        }
    }

    @Test(groups = {"multi-region"}, dataProvider = "preferredRegions", timeOut = TIMEOUT)
    public void sessionNotAvailableRetrySingleMaster(
        List<String> preferredLocations,
        OperationType operationType) {

        CosmosAsyncClient preferredListClient = null;

        List<String> preferredLocationsWithLowerCase =
            preferredLocations.stream().map(location -> location.toLowerCase(Locale.ROOT)).collect(Collectors.toList());
        // inject 404/1002 into all regions
        FaultInjectionRule sessionNotAvailableRule = new FaultInjectionRuleBuilder("sessionNotAvailableRuleSingleMaster-" + UUID.randomUUID())
            .condition(new FaultInjectionConditionBuilder().build())
            .result(
                FaultInjectionResultBuilders
                    .getResultBuilder(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                    .build())
            .build();

        try {
            preferredListClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .preferredRegions(preferredLocations)
                .buildAsyncClient();

            cosmosAsyncContainer = getSharedMultiPartitionCosmosContainer(preferredListClient);
            CosmosFaultInjectionHelper.configureFaultInjectionRules(cosmosAsyncContainer, Arrays.asList(sessionNotAvailableRule)).block();

            PartitionKey partitionKey = new PartitionKey("Test");
            try {
                if (operationType.equals(OperationType.Read)) {
                    cosmosAsyncContainer.readItem("TestId", partitionKey, TestItem.class).block();
                } else if (operationType.equals(OperationType.Query)) {
                    String query = "Select * from C";
                    CosmosQueryRequestOptions requestOptions = new CosmosQueryRequestOptions();
                    requestOptions.setPartitionKey(new PartitionKey("Test"));
                    cosmosAsyncContainer.queryItems(query, requestOptions, TestItem.class).byPage().blockFirst();
                } else if (operationType.equals(OperationType.Create)) {
                    TestItem item = new TestItem();
                    item.setId("Test");
                    item.setMypk("Test");
                    cosmosAsyncContainer.createItem(item, partitionKey, new CosmosItemRequestOptions()).block();
                }

                fail("Request should fail with 404/1002 error");
            } catch (CosmosException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
                assertThat(ex.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE);

                Map<String, String> writeRegionMap = this.getRegionMap(databaseAccount, true);
                assertThat(writeRegionMap.size()).isEqualTo(1);

                List<String> writeRegionList =
                    writeRegionMap
                        .keySet()
                        .stream()
                        .map(regionName -> regionName.toLowerCase(Locale.ROOT))
                        .collect(Collectors.toList());

                // for single master, when retrying 404/1002, it will retry on the write region
                // so for write operation or if the first preferred region is the same as write region, the contracted region count should 1
                if (operationType.isWriteOperation()
                    || preferredLocationsWithLowerCase.get(0).equalsIgnoreCase(writeRegionList.get(0))) {
                    assertThat(ex.getDiagnostics().getContactedRegionNames().size()).isEqualTo(1);
                } else {
                    assertThat(ex.getDiagnostics().getContactedRegionNames().size()).isEqualTo(2);

                    // validate the contacted region sequence
                    List<String> contactedRegions = new ArrayList<>();
                    String previousContactedRegion = StringUtils.EMPTY;
                    ClientSideRequestStatistics clientSideRequestStatistics = BridgeInternal.getClientSideRequestStatics(ex.getDiagnostics());
                    for (ClientSideRequestStatistics.StoreResponseStatistics storeResponseStatistics : clientSideRequestStatistics.getResponseStatisticsList()) {
                        if (!storeResponseStatistics.getRegionName().equalsIgnoreCase(previousContactedRegion)) {
                            contactedRegions.add(storeResponseStatistics.getRegionName().toLowerCase(Locale.ROOT));
                            previousContactedRegion = storeResponseStatistics.getRegionName().toLowerCase(Locale.ROOT);
                        }
                    }

                    List<String> expectedContactedRegions = new ArrayList<>();
                    expectedContactedRegions.add(preferredLocationsWithLowerCase.get(0));
                    expectedContactedRegions.addAll(writeRegionList);
                    // SDK will do one more round retry in first preferred region due to RenameCollectionAwareClientRetryPolicy
                    expectedContactedRegions.add(preferredLocationsWithLowerCase.get(0));
                    assertThat(contactedRegions.size()).isEqualTo(expectedContactedRegions.size());
                    assertThat(contactedRegions.containsAll(expectedContactedRegions)).isTrue();
                }
            }
        } finally {
            sessionNotAvailableRule.disable();
            safeClose(preferredListClient);
        }
    }

    @Test(groups = {"multi-region", "multi-master"}, dataProvider = "operations", timeOut = TIMEOUT)
    public void sessionNotAvailableRetryWithoutPreferredList(OperationType operationType) throws Exception {
        CosmosAsyncClient preferredListClient = null;
        try {
            preferredListClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .buildAsyncClient();

            AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(preferredListClient);
            RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
            StoreClient storeClient = ReflectionUtils.getStoreClient(rxDocumentClient);
            ReplicatedResourceClient replicatedResourceClient =
                ReflectionUtils.getReplicatedResourceClient(storeClient);
            ConsistencyReader consistencyReader = ReflectionUtils.getConsistencyReader(replicatedResourceClient);
            ConsistencyWriter consistencyWriter = ReflectionUtils.getConsistencyWriter(replicatedResourceClient);
            StoreReader storeReader = ReflectionUtils.getStoreReader(consistencyReader);

            GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);

            RntbdTransportClientTest rntbdTransportClient = new RntbdTransportClientTest(globalEndpointManager);
            RntbdTransportClientTest spyRntbdTransportClient = Mockito.spy(rntbdTransportClient);
            ReflectionUtils.setTransportClient(storeReader, spyRntbdTransportClient);
            ReflectionUtils.setTransportClient(consistencyWriter, spyRntbdTransportClient);

            cosmosAsyncContainer = getSharedMultiPartitionCosmosContainer(preferredListClient);

            List<String> uris = new ArrayList<>();
            doAnswer((Answer<Mono<StoreResponse>>) invocationOnMock -> {
                RxDocumentServiceRequest serviceRequest = invocationOnMock.getArgument(1,
                    RxDocumentServiceRequest.class);
                uris.add(serviceRequest.requestContext.locationEndpointToRoute.toString());
                CosmosException cosmosException = BridgeInternal.createCosmosException(404);
                @SuppressWarnings("unchecked")
                Map<String, String> responseHeaders = (Map<String, String>) FieldUtils.readField(cosmosException,
                    "responseHeaders", true);
                responseHeaders.put(HttpConstants.HttpHeaders.SUB_STATUS, "1002");
                return Mono.error(cosmosException);
            }).when(spyRntbdTransportClient).invokeStoreAsync(Mockito.any(Uri.class),
                Mockito.any(RxDocumentServiceRequest.class));
            try {
                PartitionKey partitionKey = new PartitionKey("Test");
                if (operationType.equals(OperationType.Read)) {
                    cosmosAsyncContainer.readItem("Test", partitionKey, TestItem.class).block();
                } else if (operationType.equals(OperationType.Query)) {
                    String query = "Select * from C";
                    CosmosQueryRequestOptions requestOptions = new CosmosQueryRequestOptions();
                    requestOptions.setPartitionKey(partitionKey);
                    cosmosAsyncContainer.queryItems(query, requestOptions, TestItem.class).byPage().blockFirst();
                } else if (operationType.equals(OperationType.Create)) {
                    TestItem item = new TestItem();
                    item.setId("Test");
                    item.setMypk("Test");
                    cosmosAsyncContainer.createItem(item, partitionKey, new CosmosItemRequestOptions()).block();
                }

                fail("Request should fail with 404/1002 error");
            } catch (CosmosException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
                assertThat(ex.getDiagnostics().getContactedRegionNames().size()).isEqualTo(1);
                String regionName = getRegionNames(rxDocumentClient).iterator().next();
                assertThat(ex.getDiagnostics().getContactedRegionNames().iterator().next()).isEqualTo(regionName.toLowerCase());
            }

            HashSet<String> uniqueHost = new HashSet<>();
            for (String uri : uris) {
                uniqueHost.add(uri);
            }
            // Verifying we are only retrying in masterOrHub region
            assertThat(uniqueHost.size()).isEqualTo(1);

            String masterOrHubRegion =databaseAccount.getWritableLocations().iterator().next().getEndpoint();

            // First regional retries in originating region , then retrying in master as per clientRetryPolicy and 1
            // retry in the
            // last as per RenameCollectionAwareClientRetryPolicy after clearing session token
            int numberOfRegionRetried = 3;

            // Calculating avg number of retries in each region
            int averageRetryBySessionRetryPolicyInOneRegion = uris.size() / numberOfRegionRetried;

            int totalRetries = averageRetryBySessionRetryPolicyInOneRegion;
            // First regional retries should be in master region
            if(!(uris.get(totalRetries / 2).equals(masterOrHubRegion) || uris.get(totalRetries / 2).equals(TestConfigurations.HOST))){
                fail(String.format("%s is not equal to master region", uris.get(totalRetries / 2)));
            }

            // Retrying again in master region
            if(!(uris.get(totalRetries + (averageRetryBySessionRetryPolicyInOneRegion) / 2).equals(masterOrHubRegion) || uris.get(totalRetries / 2).equals(TestConfigurations.HOST))){
                fail(String.format("%s is not equal to master region", uris.get(totalRetries + (averageRetryBySessionRetryPolicyInOneRegion) / 2)));
            }
            totalRetries = totalRetries + averageRetryBySessionRetryPolicyInOneRegion;

            // Last region retries should be master region
            if(!(uris.get(totalRetries + (averageRetryBySessionRetryPolicyInOneRegion) / 2).equals(masterOrHubRegion) || uris.get(totalRetries / 2).equals(TestConfigurations.HOST))){
                fail(String.format("%s is not equal to master region", uris.get(totalRetries + (averageRetryBySessionRetryPolicyInOneRegion) / 2)));
            }
        } finally {
            safeClose(preferredListClient);
        }
    }

    private Map<String, String> getRegionMap(DatabaseAccount databaseAccount, boolean writeOnly) {
        Iterator<DatabaseAccountLocation> locationIterator =
            writeOnly ? databaseAccount.getWritableLocations().iterator() : databaseAccount.getReadableLocations().iterator();
        Map<String, String> regionMap = new ConcurrentHashMap<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            regionMap.put(accountLocation.getName(), accountLocation.getEndpoint());
        }

        return regionMap;
    }

    private List<String> getRegionNames(RxDocumentClientImpl rxDocumentClient) throws Exception {
        GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
        LocationCache locationCache = ReflectionUtils.getLocationCache(globalEndpointManager);

        Field locationInfoField = LocationCache.class.getDeclaredField("locationInfo");
        locationInfoField.setAccessible(true);
        Object locationInfo = locationInfoField.get(locationCache);

        Class<?> DatabaseAccountLocationsInfoClass = Class.forName("com.azure.cosmos.implementation.routing" +
            ".LocationCache$DatabaseAccountLocationsInfo");

        Field availableWriteEndpointByLocation = DatabaseAccountLocationsInfoClass.getDeclaredField(
            "availableWriteLocations");
        availableWriteEndpointByLocation.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) availableWriteEndpointByLocation.get(locationInfo);
        return list;
    }

    private class RntbdTransportClientTest extends TransportClient {

        GlobalEndpointManager globalEndpointManager;
        RntbdTransportClientTest(GlobalEndpointManager globalEndpointManager) {
            this.globalEndpointManager = globalEndpointManager;
        }

        @Override
        protected Mono<StoreResponse> invokeStoreAsync(Uri physicalAddress, RxDocumentServiceRequest request) {
            return Mono.empty();
        }

        @Override
        public void configureFaultInjectorProvider(IFaultInjectorProvider injectorProvider) {
            throw new NotImplementedException("configureFaultInjectionRuleProvider is not supported in RntbdTransportClientTest");
        }

        @Override
        public void close() {
        }

        @Override
        protected GlobalEndpointManager getGlobalEndpointManager() {
            return this.globalEndpointManager;
        }

        @Override
        public ProactiveOpenConnectionsProcessor getProactiveOpenConnectionsProcessor() {
            throw new NotImplementedException("getOpenConnectionsProcessor is not supported in RntbdTransportClientTest");
        }

        @Override
        public void recordOpenConnectionsAndInitCachesCompleted(List<CosmosContainerIdentity> cosmosContainerIdentities) {
            throw new NotImplementedException("recordOpenConnectionsAndInitCachesCompleted is not supported in RntbdTransportClientTest");
        }

        @Override
        public void recordOpenConnectionsAndInitCachesStarted(List<CosmosContainerIdentity> cosmosContainerIdentities) {
            throw new NotImplementedException("recordOpenConnectionsAndInitCachesStarted is not supported in RntbdTransportClientTest");
        }
    }

    private class TestItem {
        private String id;
        private String mypk;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMypk() {
            return mypk;
        }

        public void setMypk(String mypk) {
            this.mypk = mypk;
        }
    }
}

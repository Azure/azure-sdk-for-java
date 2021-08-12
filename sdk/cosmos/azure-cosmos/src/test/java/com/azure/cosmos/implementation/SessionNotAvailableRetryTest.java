// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.directconnectivity.ConsistencyReader;
import com.azure.cosmos.implementation.directconnectivity.ConsistencyWriter;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.ReplicatedResourceClient;
import com.azure.cosmos.implementation.directconnectivity.StoreClient;
import com.azure.cosmos.implementation.directconnectivity.StoreReader;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.TransportClient;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        List<String> regionalSuffix1 = new ArrayList<>();
        List<String> preferredLocations2 = new ArrayList<>();
        List<String> regionalSuffix2 = new ArrayList<>();
        Iterator<DatabaseAccountLocation> locationIterator = this.databaseAccount.getReadableLocations().iterator();
        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            preferredLocations1.add(accountLocation.getName());
            regionalSuffix1.add(getRegionalSuffix(accountLocation.getEndpoint(), TestConfigurations.HOST));
        }

        //putting preferences in opposite direction than what came from database account api
        for (int i = preferredLocations1.size() - 1; i >= 0; i--) {
            preferredLocations2.add(preferredLocations1.get(i));
            regionalSuffix2.add(regionalSuffix1.get(i));
        }

        return new Object[][]{
            new Object[]{preferredLocations1, regionalSuffix1, OperationType.Read},
            new Object[]{preferredLocations2, regionalSuffix2, OperationType.Read},
            new Object[]{preferredLocations1, regionalSuffix1, OperationType.Query},
            new Object[]{preferredLocations2, regionalSuffix2, OperationType.Query},
            new Object[]{preferredLocations1, regionalSuffix1, OperationType.Create},
            new Object[]{preferredLocations2, regionalSuffix2, OperationType.Create},
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
    public void sessionNotAvailableRetryMultiMaster(List<String> preferredLocations, List<String> regionalSuffix,
                                                    OperationType operationType) {
        CosmosAsyncClient preferredListClient = null;
        try {
            preferredListClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .preferredRegions(preferredLocations)
                .buildAsyncClient();

            AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(preferredListClient);
            RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
            StoreClient storeClient = ReflectionUtils.getStoreClient(rxDocumentClient);
            ReplicatedResourceClient replicatedResourceClient =
                ReflectionUtils.getReplicatedResourceClient(storeClient);
            ConsistencyReader consistencyReader = ReflectionUtils.getConsistencyReader(replicatedResourceClient);
            ConsistencyWriter consistencyWriter = ReflectionUtils.getConsistencyWriter(replicatedResourceClient);
            StoreReader storeReader = ReflectionUtils.getStoreReader(consistencyReader);

            RntbdTransportClientTest rntbdTransportClient = new RntbdTransportClientTest();
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
            }

            HashSet<String> uniqueHost = new HashSet<>();
            for (String uri : uris) {
                uniqueHost.add(uri);
            }
            // First verify we are retrying in each region
            assertThat(uniqueHost.size()).isEqualTo(preferredLocations.size());


            // First regional retries in originating region , then retrying per region in clientRetryPolicy and 1
            // retry in the
            // last as per RenameCollectionAwareClientRetryPolicy after clearing session token
            int numberOfRegionRetried = preferredLocations.size() + 2;

            // Calculating avg number of retries in each region
            int averageRetryBySessionRetryPolicyInOneRegion = uris.size() / numberOfRegionRetried;

            int totalRetries = averageRetryBySessionRetryPolicyInOneRegion;
            // First regional retries should be in the first preferred region
            assertThat(uris.get(totalRetries / 2)).contains(regionalSuffix.get(0));

            for (int i = 1; i <= preferredLocations.size(); i++) {
                // Retrying in each region as per preferred region
                assertThat(uris.get(totalRetries + (averageRetryBySessionRetryPolicyInOneRegion) / 2)).contains(regionalSuffix.get(i % regionalSuffix.size()));
                totalRetries = totalRetries + averageRetryBySessionRetryPolicyInOneRegion;
            }

            // Last region retries should be in first preferred region
            assertThat(uris.get(totalRetries + (averageRetryBySessionRetryPolicyInOneRegion) / 2)).contains(regionalSuffix.get(0));
        } finally {
            safeClose(preferredListClient);
        }
    }

    @Test(groups = {"multi-region"}, dataProvider = "preferredRegions", timeOut = TIMEOUT)
    public void sessionNotAvailableRetrySingleMaster(List<String> preferredLocations, List<String> regionalSuffix,
                                                     OperationType operationType) {
        CosmosAsyncClient preferredListClient = null;
        try {
            preferredListClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .preferredRegions(preferredLocations)
                .buildAsyncClient();

            AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(preferredListClient);
            RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
            StoreClient storeClient = ReflectionUtils.getStoreClient(rxDocumentClient);
            ReplicatedResourceClient replicatedResourceClient =
                ReflectionUtils.getReplicatedResourceClient(storeClient);
            ConsistencyReader consistencyReader = ReflectionUtils.getConsistencyReader(replicatedResourceClient);
            ConsistencyWriter consistencyWriter = ReflectionUtils.getConsistencyWriter(replicatedResourceClient);
            StoreReader storeReader = ReflectionUtils.getStoreReader(consistencyReader);

            RntbdTransportClientTest rntbdTransportClient = new RntbdTransportClientTest();
            RntbdTransportClientTest spyRntbdTransportClient = Mockito.spy(rntbdTransportClient);
            ReflectionUtils.setTransportClient(storeReader, spyRntbdTransportClient);
            ReflectionUtils.setTransportClient(consistencyWriter, spyRntbdTransportClient);

            cosmosAsyncContainer = getSharedMultiPartitionCosmosContainer(preferredListClient);

            PartitionKey partitionKey = new PartitionKey("Test");
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
                FieldUtils.writeField(cosmosException, "responseHeaders", responseHeaders, true);
                return Mono.error(cosmosException);
            }).when(spyRntbdTransportClient).invokeStoreAsync(Mockito.any(Uri.class),
                Mockito.any(RxDocumentServiceRequest.class));
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
            }

            HashSet<String> uniqueHost = new HashSet<>();
            for (String uri : uris) {
                uniqueHost.add(uri);
            }

            String masterOrHubRegionSuffix =
                getRegionalSuffix(databaseAccount.getWritableLocations().iterator().next().getEndpoint(),
                    TestConfigurations.HOST);
            // First regional retries in originating region, then retrying in master/hub region and 1 retry at the
            // last from
            // RenameCollectionAwareClientRetryPolicy after clearing session token
            int numberOfRegionRetried = 3;

            // Calculating approx avg number of retries in each region
            int averageRetryBySessionRetryPolicyInOneRegion = uris.size() / numberOfRegionRetried;

            int totalRetries = averageRetryBySessionRetryPolicyInOneRegion;

            if (operationType.equals(OperationType.Create)) {
                assertThat(uniqueHost.size()).isEqualTo(1); // always goes to master region

                //First region retries should be in masterOrHubRegionSuffix
                assertThat(uris.get(totalRetries / 2)).contains(masterOrHubRegionSuffix);

                // Second region retries should be in masterOrHubRegionSuffix
                assertThat(uris.get(totalRetries + (averageRetryBySessionRetryPolicyInOneRegion) / 2)).contains(masterOrHubRegionSuffix);
                totalRetries = totalRetries + averageRetryBySessionRetryPolicyInOneRegion;

                //Last region retries should be in masterOrHubRegionSuffix
                assertThat(uris.get(totalRetries + (averageRetryBySessionRetryPolicyInOneRegion) / 2)).contains(masterOrHubRegionSuffix);
            } else {
                if (regionalSuffix.get(0).equals(masterOrHubRegionSuffix)) {
                    //Verify we are retrying only in master region
                    assertThat(uniqueHost.size()).isEqualTo(1);
                } else {
                    //Verify we are retrying in first preferred region and master region
                    assertThat(uniqueHost.size()).isEqualTo(2);
                }

                //First region retries should be in first preferred region
                assertThat(uris.get(totalRetries / 2)).contains(regionalSuffix.get(0));

                // Second region retries should be in masterOrHubRegion
                assertThat(uris.get(totalRetries + (averageRetryBySessionRetryPolicyInOneRegion) / 2)).contains(masterOrHubRegionSuffix);
                totalRetries = totalRetries + averageRetryBySessionRetryPolicyInOneRegion;

                //Last region retries should be in first preferred region
                assertThat(uris.get(totalRetries + (averageRetryBySessionRetryPolicyInOneRegion) / 2)).contains(regionalSuffix.get(0));
            }
        } finally {
            safeClose(preferredListClient);
        }
    }

    @Test(groups = {"multi-region", "multi-master"}, dataProvider = "operations", timeOut = TIMEOUT)
    public void sessionNotAvailableRetryWithoutPreferredList(OperationType operationType) {
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

            RntbdTransportClientTest rntbdTransportClient = new RntbdTransportClientTest();
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

    private String getRegionalSuffix(String str1, String str2) {
        int initialIndex = findInitialIndex(str1, str2);
        int indexFromLast = findIndexFromLast(str1, str2);
        return str1.substring(initialIndex + 1, str1.length() - indexFromLast);
    }

    private int findInitialIndex(String str1, String str2) {
        int counter = 0;
        while (str1.charAt(counter) == str2.charAt(counter)) {
            counter++;
        }
        return counter;
    }

    private int findIndexFromLast(String str1, String str2) {
        int length1 = str1.length();
        int length2 = str2.length();
        int counter = 0;
        while (str1.charAt(length1 - 1 - counter) == str2.charAt(length2 - 1 - counter)) {
            counter++;
        }
        return counter;
    }

    private class RntbdTransportClientTest extends TransportClient {

        @Override
        protected Mono<StoreResponse> invokeStoreAsync(Uri physicalAddress, RxDocumentServiceRequest request) {
            return Mono.empty();
        }

        @Override
        public void close() {
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

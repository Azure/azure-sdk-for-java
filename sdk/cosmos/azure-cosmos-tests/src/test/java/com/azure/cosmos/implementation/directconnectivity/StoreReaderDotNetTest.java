// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.AuthorizationTokenType;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DocumentServiceRequestContext;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.ISessionContainer;
import com.azure.cosmos.implementation.InvalidPartitionException;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.ReplicationPolicy;
import com.azure.cosmos.implementation.RequestChargeTracker;
import com.azure.cosmos.implementation.RequestVerb;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.ServiceUnavailableException;
import com.azure.cosmos.implementation.SessionContainer;
import com.azure.cosmos.implementation.StoreResponseBuilder;
import com.azure.cosmos.implementation.Strings;
import io.reactivex.subscribers.TestSubscriber;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;

public class StoreReaderDotNetTest {
    private static final Logger logger = LoggerFactory.getLogger(StoreReaderDotNetTest.class);
    @Test(groups = "unit")
    public void addressCache() {
        // create a real document service request
        RxDocumentServiceRequest entity = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);

        // setup mocks for address information
        AddressInformation[] addressInformation = new AddressInformation[3];
        for (int i = 0; i < 3; i++) {
            addressInformation[i] = new AddressInformation(true, true, "http://replica-" + i, Protocol.HTTPS);
        }

        IAddressResolver mockAddressCache = Mockito.mock(IAddressResolver.class);

        Mockito.doReturn(Mono.just(addressInformation))
                .when(mockAddressCache)
                .resolveAsync(Mockito.any(RxDocumentServiceRequest.class), Mockito.eq(false));

        // validate that the mock works
        AddressInformation[] addressInfo = mockAddressCache.resolveAsync(entity, false).block();
        assertThat(addressInfo[0]).isEqualTo(addressInformation[0]);
    }

    /**
     * Tests for TransportClient
     */
    @Test(groups = "unit")
    public void transportClient() {
        // create a real document service request
        RxDocumentServiceRequest entity = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);

        // setup mocks for address information
        AddressInformation[] addressInformation = new AddressInformation[3];

        // construct URIs that look like the actual uri
        // rntbd://yt1prdddc01-docdb-1.documents.azure.com:14003/apps/ce8ab332-f59e-4ce7-a68e-db7e7cfaa128/services/68cc0b50-04c6-4716-bc31-2dfefd29e3ee/partitions/5604283d-0907-4bf4-9357-4fa9e62de7b5/replicas/131170760736528207s/
        for (int i = 0; i < 3; i++) {
            String physicalUri =
                    "rntbd://dummytenant.documents.azure.com:14003/apps/APPGUID/services/SERVICEGUID/partitions/PARTITIONGUID/replicas/"
                            + Integer.toString(i) + (i == 0 ? "p" : "s") + "/";
            addressInformation[i] = new AddressInformation(true, true, physicalUri, Protocol.TCP);

        }

        // create objects for all the dependencies of the StoreReader
        TransportClient mockTransportClient = Mockito.mock(TransportClient.class);

        // create mock store response object
        StoreResponseBuilder srb = new StoreResponseBuilder();


        // set lsn and activityid on the store response.
        srb.withHeader(WFConstants.BackendHeaders.ACTIVITY_ID, "ACTIVITYID1_1" );
        srb.withHeader(WFConstants.BackendHeaders.LSN, "50");

        // setup mock transport client
        Mockito.doReturn(Mono.just(srb.build()))
                .when(mockTransportClient)
                .invokeResourceOperationAsync(
                        Mockito.eq(addressInformation[0].getPhysicalUri()),
                        Mockito.any(RxDocumentServiceRequest.class));



        // get response from mock object
        StoreResponse response = mockTransportClient.invokeResourceOperationAsync(addressInformation[0].getPhysicalUri(), entity).block();

        // validate that the LSN matches
        // validate that the ActivityId Matches

        StoreResponseValidator validator = StoreResponseValidator.create().withBELSN(50).withBEActivityId("ACTIVITYID1_1").build();
        validator.validate(response);
    }

    private TransportClient getMockTransportClientDuringUpgrade(AddressInformation[] addressInformation) {
        // create objects for all the dependencies of the StoreReader
        TransportClient mockTransportClient = Mockito.mock(TransportClient.class);

        // create mock store response object
        // set lsn and activityid on the store response.
        StoreResponse mockStoreResponseFast = StoreResponseBuilder.create()
                .withHeader(WFConstants.BackendHeaders.LSN, "50")
                .withHeader(WFConstants.BackendHeaders.ACTIVITY_ID, "ACTIVITYID1_1")
                .build();

        StoreResponse mockStoreResponseSlow = StoreResponseBuilder.create()
                .withHeader(WFConstants.BackendHeaders.LSN, "30")
                .withHeader(WFConstants.BackendHeaders.ACTIVITY_ID, "ACTIVITYID1_1")
                .build();

        // setup mock transport client for the first replica
        Mockito.doReturn(Mono.just(mockStoreResponseFast))
                .when(mockTransportClient)
                .invokeResourceOperationAsync(Mockito.eq(addressInformation[0].getPhysicalUri()), Mockito.any(RxDocumentServiceRequest.class));


        // setup mock transport client with a sequence of outputs
        Mockito.doReturn(Mono.just(mockStoreResponseFast))     // initial read response
                .doReturn(Mono.just(mockStoreResponseFast))    // barrier retry, count 1
                .doReturn(Mono.just(mockStoreResponseFast))    // barrier retry, count 2
                .doReturn(Mono.error(new InvalidPartitionException())) // throw invalid partition exception to simulate collection recreate with same name
                .doReturn(Mono.just(mockStoreResponseFast))    // new read
                .doReturn(Mono.just(mockStoreResponseFast))    // subsequent barriers
                .doReturn(Mono.just(mockStoreResponseFast))
                .doReturn(Mono.just(mockStoreResponseFast))
                .when(mockTransportClient).invokeResourceOperationAsync(
                        Mockito.eq(addressInformation[1].getPhysicalUri()),
                        Mockito.any(RxDocumentServiceRequest.class));

        // After this, the product code should reset target identity, and lsn response
        Queue<StoreResponse> queueOfResponses = new ArrayDeque<>();

        // let the first 10 responses be slow, and then fast
        for (int i = 0; i < 20; i++) {
            queueOfResponses.add(i <= 2 ? mockStoreResponseSlow : mockStoreResponseFast);
        }

        // setup mock transport client with a sequence of outputs, for the second replica
        // This replica behaves in the following manner:
        // calling InvokeResourceOperationAsync
        // 1st time: returns valid LSN
        // 2nd time: returns InvalidPartitionException
            // initial read response

        Mockito.doAnswer((params) -> Mono.just(queueOfResponses.poll()))
                .when(mockTransportClient).invokeResourceOperationAsync(
                        Mockito.eq(addressInformation[2].getPhysicalUri()),
                        Mockito.any(RxDocumentServiceRequest.class));

        return mockTransportClient;
    }

    private enum ReadQuorumResultKind {
        QuorumMet,
        QuorumSelected,
        QuorumNotSelected
    }

    private TransportClient getMockTransportClientForGlobalStrongReads(AddressInformation[] addressInformation, ReadQuorumResultKind result) {
        // create objects for all the dependencies of the StoreReader
        TransportClient mockTransportClient = Mockito.mock(TransportClient.class);

        // create mock store response object

        StoreResponse mockStoreResponse1 = StoreResponseBuilder.create()
                .withHeader(WFConstants.BackendHeaders.LSN, "100")
                .withHeader(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, "90")
                .withHeader(WFConstants.BackendHeaders.ACTIVITY_ID, "ACTIVITYID1_1")
                .withHeader(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS, "1")
                .build();

        StoreResponse mockStoreResponse2 = StoreResponseBuilder.create()
                .withHeader(WFConstants.BackendHeaders.LSN, "90")
                .withHeader(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, "90")
                .withHeader(WFConstants.BackendHeaders.ACTIVITY_ID, "ACTIVITYID1_2")
                .withHeader(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS, "1")
                .build();


        StoreResponse mockStoreResponse3 = StoreResponseBuilder.create()
                .withHeader(WFConstants.BackendHeaders.LSN, "92")
                .withHeader(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, "90")
                .withHeader(WFConstants.BackendHeaders.ACTIVITY_ID, "ACTIVITYID1_3")
                .withHeader(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS, "1")
                .build();

        StoreResponse mockStoreResponse4 = StoreResponseBuilder.create()
                .withHeader(WFConstants.BackendHeaders.LSN, "100")
                .withHeader(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, "92")
                .withHeader(WFConstants.BackendHeaders.ACTIVITY_ID, "ACTIVITYID1_3")
                .withHeader(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS, "1")
                .build();

        StoreResponse mockStoreResponse5 = StoreResponseBuilder.create()
                .withHeader(WFConstants.BackendHeaders.LSN, "100")
                .withHeader(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, "100")
                .withHeader(WFConstants.BackendHeaders.ACTIVITY_ID, "ACTIVITYID1_3")
                .withHeader(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS, "1")
                .withHeader(WFConstants.BackendHeaders.CURRENT_REPLICA_SET_SIZE, "1")
                .withHeader(WFConstants.BackendHeaders.QUORUM_ACKED_LSN, "100")
                .build();
        // set lsn and activityid on the store response.

        StoreResponse mockStoreResponseFast = StoreResponseBuilder.create()
                .withHeader(WFConstants.BackendHeaders.LSN, "50")
                .withHeader(WFConstants.BackendHeaders.ACTIVITY_ID, "ACTIVITYID1_1")
                .build();

        if(result == ReadQuorumResultKind.QuorumMet) {
            // setup mock transport client for the first replica
            Mockito.doReturn(Mono.just(mockStoreResponse5))
                    .when(mockTransportClient).invokeResourceOperationAsync(
                            Mockito.eq(addressInformation[0].getPhysicalUri()), Mockito.any(RxDocumentServiceRequest.class));

            Mockito.doReturn(Mono.just(mockStoreResponse1))
                    .doReturn(Mono.just(mockStoreResponse1))
                    .doReturn(Mono.just(mockStoreResponse1))
                    .doReturn(Mono.just(mockStoreResponse1))
                    .doReturn(Mono.just(mockStoreResponse1))
                    .doReturn(Mono.just(mockStoreResponse5))
                    .when(mockTransportClient).invokeResourceOperationAsync(
                            Mockito.eq(addressInformation[1].getPhysicalUri()),
                            Mockito.any(RxDocumentServiceRequest.class));

            Mockito.doReturn(Mono.just(mockStoreResponse2))
                    .doReturn(Mono.just(mockStoreResponse2))
                    .doReturn(Mono.just(mockStoreResponse2))
                    .doReturn(Mono.just(mockStoreResponse1))
                    .doReturn(Mono.just(mockStoreResponse4))
                    .doReturn(Mono.just(mockStoreResponse5))
                    .when(mockTransportClient).invokeResourceOperationAsync(
                            Mockito.eq(addressInformation[2].getPhysicalUri()),
                            Mockito.any(RxDocumentServiceRequest.class));
        }

        if (result == ReadQuorumResultKind.QuorumSelected) {
            // setup mock transport client for the first replica
            Mockito.doReturn(Mono.just(mockStoreResponse2))
                    .when(mockTransportClient).invokeResourceOperationAsync(
                            Mockito.eq(addressInformation[0].getPhysicalUri()), Mockito.any(RxDocumentServiceRequest.class));

            // setup mock transport client with a sequence of outputs
            Mockito.doReturn(Mono.just(mockStoreResponse1))
                    .when(mockTransportClient).invokeResourceOperationAsync(
                            Mockito.eq(addressInformation[1].getPhysicalUri()), Mockito.any(RxDocumentServiceRequest.class));


            // setup mock transport client with a sequence of outputs
            Mockito.doReturn(Mono.just(mockStoreResponse2))
                    .when(mockTransportClient).invokeResourceOperationAsync(
                            Mockito.eq(addressInformation[2].getPhysicalUri()), Mockito.any(RxDocumentServiceRequest.class));
        } else if (result == ReadQuorumResultKind.QuorumNotSelected) {
            // setup mock transport client for the first replica

            Mockito.doReturn(Mono.just(mockStoreResponse5))
                    .when(mockTransportClient).invokeResourceOperationAsync(
                    Mockito.eq(addressInformation[0].getPhysicalUri()), Mockito.any(RxDocumentServiceRequest.class));

            Mockito.doReturn(Mono.just(mockStoreResponse5))
                    .when(mockTransportClient).invokeResourceOperationAsync(
                    Mockito.eq(addressInformation[1].getPhysicalUri()), Mockito.any(RxDocumentServiceRequest.class));

            Mockito.doReturn(Mono.error(new GoneException("test")))
                    .when(mockTransportClient).invokeResourceOperationAsync(
                    Mockito.eq(addressInformation[2].getPhysicalUri()), Mockito.any(RxDocumentServiceRequest.class));
        }

        return mockTransportClient;
    }

    private TransportClient getMockTransportClientForGlobalStrongWrites(
            AddressInformation[] addressInformation,
            int indexOfCaughtUpReplica,
            boolean undershootGlobalCommittedLsnDuringBarrier,
            boolean overshootLsnDuringBarrier,
            boolean overshootGlobalCommittedLsnDuringBarrier)
    {
        TransportClient mockTransportClient = Mockito.mock(TransportClient.class);

        // create mock store response object

        // set lsn and activityid on the store response.
        StoreResponse mockStoreResponse1 = StoreResponseBuilder.create()
                .withHeader(WFConstants.BackendHeaders.LSN, "100")
                .withHeader(WFConstants.BackendHeaders.ACTIVITY_ID, "ACTIVITYID1_1")
                .withHeader(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, "90")
                .withHeader(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS, "1")
                .build();

        StoreResponse mockStoreResponse2 = StoreResponseBuilder.create()
                .withHeader(WFConstants.BackendHeaders.LSN, "100")
                .withHeader(WFConstants.BackendHeaders.ACTIVITY_ID, "ACTIVITYID1_2")
                .withHeader(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, "100")
                .withHeader(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS, "1")
                .build();

        StoreResponse mockStoreResponse3 = StoreResponseBuilder.create()
                .withHeader(WFConstants.BackendHeaders.LSN, "103")
                .withHeader(WFConstants.BackendHeaders.ACTIVITY_ID, "ACTIVITYID1_3")
                .withHeader(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, "100")
                .withHeader(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS, "1")
                .build();

        StoreResponse mockStoreResponse4 = StoreResponseBuilder.create()
                .withHeader(WFConstants.BackendHeaders.LSN, "103")
                .withHeader(WFConstants.BackendHeaders.ACTIVITY_ID, "ACTIVITYID1_3")
                .withHeader(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, "103")
                .withHeader(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS, "1")
                .build();

        StoreResponse mockStoreResponse5 = StoreResponseBuilder.create()
                .withHeader(WFConstants.BackendHeaders.LSN, "106")
                .withHeader(WFConstants.BackendHeaders.ACTIVITY_ID, "ACTIVITYID1_3")
                .withHeader(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, "103")
                .withHeader(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS, "1")
                .build();

        StoreResponse finalResponse = null;
        if (undershootGlobalCommittedLsnDuringBarrier) {
            finalResponse = mockStoreResponse1;
        } else {
            if (overshootLsnDuringBarrier) {
                if (overshootGlobalCommittedLsnDuringBarrier) {
                    finalResponse = mockStoreResponse5;
                } else {
                    finalResponse = mockStoreResponse3;
                }
            } else {
                if (overshootGlobalCommittedLsnDuringBarrier) {
                    finalResponse = mockStoreResponse4;
                } else {
                    finalResponse = mockStoreResponse2;
                }
            }
        }

        for (int i = 0; i < addressInformation.length; i++) {
            if (i == indexOfCaughtUpReplica) {
                Mockito.doReturn(Mono.just(mockStoreResponse1))
                    .doReturn(Mono.just(mockStoreResponse1))
                    .doReturn(Mono.just(mockStoreResponse1))
                    .doReturn(Mono.just(mockStoreResponse1))
                    .doReturn(Mono.just(finalResponse))
                        .when(mockTransportClient).invokeResourceOperationAsync(
                            Mockito.eq(addressInformation[i].getPhysicalUri()), Mockito.any(RxDocumentServiceRequest.class));

            } else {
                Mockito.doReturn(Mono.just(mockStoreResponse1))
                        .doReturn(Mono.just(mockStoreResponse1))
                        .doReturn(Mono.just(mockStoreResponse1))
                        .doReturn(Mono.just(mockStoreResponse1))
                        .doReturn(Mono.just(mockStoreResponse1))
                        .doReturn(Mono.just(mockStoreResponse1))
                        .when(mockTransportClient).invokeResourceOperationAsync(
                        Mockito.eq(addressInformation[i].getPhysicalUri()), Mockito.any(RxDocumentServiceRequest.class));
            }
        }

        return mockTransportClient;
    }

    /**
     * We are simulating upgrade scenario where one of the secondary replicas is down.
     * And one of the other secondary replicas is an XP Primary (lagging behind).
     * Dyanmic Quorum is in effect, so Write Quorum = 2
     * @return array of AddressInformation
     */
    private AddressInformation[] getMockAddressInformationDuringUpgrade() {
        // setup mocks for address information
        AddressInformation[] addressInformation = new AddressInformation[3];

        // construct URIs that look like the actual uri
        // rntbd://yt1prdddc01-docdb-1.documents.azure.com:14003/apps/ce8ab332-f59e-4ce7-a68e-db7e7cfaa128/services/68cc0b50-04c6-4716-bc31-2dfefd29e3ee/partitions/5604283d-0907-4bf4-9357-4fa9e62de7b5/replicas/131170760736528207s/
        for (int i = 0; i <= 2; i++) {
            String physicalUri =
                    "rntbd://dummytenant.documents.azure.com:14003/apps/APPGUID/services/SERVICEGUID/partitions/PARTITIONGUID/replicas/"
                            + Integer.toString(i) + (i == 0 ? "p" : "s") + "/";
            addressInformation[i] = new AddressInformation(true, i == 0 ? true : false, physicalUri, Protocol.TCP);
        }

        return addressInformation;
    }

    /**
     * Given an array of address information, gives mock address cache.
     * @param addressInformation
     * @return
     */
    private IAddressResolver getMockAddressCache(AddressInformation[] addressInformation)
    {
        // Address Selector is an internal sealed class that can't be mocked, but its dependency
        // AddressCache can be mocked.
        IAddressResolver mockAddressCache = Mockito.mock(IAddressResolver.class);

        Mockito.doReturn(Mono.just(addressInformation)).when(mockAddressCache)
                .resolveAsync(Mockito.any(RxDocumentServiceRequest.class), Mockito.eq(false) /*forceRefresh*/);

        Mockito.doReturn(Mono.just(new AddressInformation[0])).when(mockAddressCache)
                .resolveAsync(Mockito.any(RxDocumentServiceRequest.class), Mockito.eq(true) /*forceRefresh*/);

        return mockAddressCache;
    }

    /**
     * Tests for {@link StoreReader}
     */
    @Test(groups = "unit")
    public void storeReaderBarrier() {
        // create a real document service request
        RxDocumentServiceRequest entity = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);

        // set request charge tracker -  this is referenced in store reader (ReadMultipleReplicaAsync)
        DocumentServiceRequestContext requestContext = new DocumentServiceRequestContext();
       // requestContext.ClientRequestStatistics = new ClientSideRequestStatistics();
        requestContext.requestChargeTracker = new RequestChargeTracker();
        entity.requestContext = requestContext;

        // also setup timeout helper, used in store reader
        // entity.requestContext.timeoutHelper = new TimeoutHelper(new TimeSpan(2, 2, 2));
        entity.requestContext.timeoutHelper = Mockito.mock(TimeoutHelper.class);

        // when the store reader throws INVALID Partition exception, the higher layer should
        // clear this target identity.
        // entity.requestContext.TargetIdentity = new ServiceIdentity("dummyTargetIdentity1", new Uri("http://dummyTargetIdentity1"), false);
        entity.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange();

        AddressInformation[] addressInformation = getMockAddressInformationDuringUpgrade();
        IAddressResolver mockAddressCache = getMockAddressCache(addressInformation);

        // validate that the mock works
        AddressInformation[] addressInfo = mockAddressCache.resolveAsync(entity, false).block();

        assertThat(addressInfo[0]).isEqualTo(addressInformation[0]);

        AddressSelector addressSelector = new AddressSelector(mockAddressCache, Protocol.TCP);
        Uri primaryAddress = addressSelector.resolvePrimaryUriAsync(entity, false /*forceAddressRefresh*/).block();

        // check if the address return from Address Selector matches the original address info
        assertThat(primaryAddress).isEqualTo(addressInformation[0].getPhysicalUri());

        // get mock transport client that returns a sequence of responses to simulate upgrade
        TransportClient mockTransportClient = getMockTransportClientDuringUpgrade(addressInformation);

        // get response from mock object
        StoreResponse response = mockTransportClient.invokeResourceOperationAsync(addressInformation[0].getPhysicalUri(), entity).block();

        // validate that the LSN matches
        assertThat(response.getLSN()).isEqualTo(50);

        String activityId = response.getHeaderValue(WFConstants.BackendHeaders.ACTIVITY_ID);

        // validate that the ActivityId Matches
        assertThat(activityId).isEqualTo("ACTIVITYID1_1");

        // create a real session container - we don't need session for this test anyway
        ISessionContainer sessionContainer = new SessionContainer(Strings.Emtpy);

        // create store reader with mock transport client, real address selector (that has mock address cache), and real session container
        StoreReader storeReader =
                new StoreReader(mockTransportClient,
                        addressSelector,
                        sessionContainer);

        // reads always go to read quorum (2) replicas
        int replicaCountToRead = 2;

        List<StoreResult> result = storeReader.readMultipleReplicaAsync(
                entity,
                false /*includePrimary*/,
                replicaCountToRead,
                true /*requiresValidLSN*/,
                false /*useSessionToken*/,
                ReadMode.Strong).block();

        // make sure we got 2 responses from the store reader
        Assertions.assertThat(result).hasSize(2);
    }

    public static void validateSuccess(Mono<List<StoreResult>> single,
                                       MultiStoreResultValidator validator) {
        validateSuccess(single, validator, 10000);
    }

    public static void validateSuccess(Mono<List<StoreResult>> single,
                                       MultiStoreResultValidator validator, long timeout) {
        TestSubscriber<List<StoreResult>> testSubscriber = new TestSubscriber<>();

        single.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();
        testSubscriber.assertValueCount(1);
        validator.validate(testSubscriber.values().get(0));
    }

    public static void validateSuccess(Mono<StoreResult> single,
                                       StoreResultValidator validator) {
        validateSuccess(single, validator, 10000);
    }

    public static void validateSuccess(Mono<StoreResult> single,
                                       StoreResultValidator validator, long timeout) {
        TestSubscriber<StoreResult> testSubscriber = new TestSubscriber<>();

        single.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();
        testSubscriber.assertValueCount(1);
        validator.validate(testSubscriber.values().get(0));
    }

    public static void validateException(Mono<StoreResult> single,
                                         FailureValidator validator) {
        validateException(single, validator, 10000);
    }

    public static void validateException(Mono<StoreResult> single,
                                         FailureValidator validator, long timeout) {
        TestSubscriber<StoreResult> testSubscriber = new TestSubscriber<>();

        single.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotComplete();
        testSubscriber.assertTerminated();
        assertThat(testSubscriber.errorCount()).isEqualTo(1);
        validator.validate(testSubscriber.errors().get(0));
    }

    /**
     * StoreClient uses ReplicatedResourceClient uses ConsistencyReader uses QuorumReader uses StoreReader uses TransportClient uses RntbdConnection
     */
    @Test(groups = "unit", enabled = false)
    @SuppressWarnings("unchecked")
    public void storeClient() throws URISyntaxException {
        // create a real document service request (with auth token level = god)
        RxDocumentServiceRequest entity = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        entity.authorizationTokenType = AuthorizationTokenType.PrimaryMasterKey;

        // set request charge tracker -  this is referenced in store reader (ReadMultipleReplicaAsync)
        DocumentServiceRequestContext requestContext = new DocumentServiceRequestContext();
        requestContext.requestChargeTracker = new RequestChargeTracker();
        entity.requestContext = requestContext;

        // set a dummy resource id on the request.
        entity.setResourceId("1-MxAPlgMgA=");

        // set consistency level on the request to Bounded Staleness
        entity.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.BOUNDED_STALENESS.toString());

        // also setup timeout helper, used in store reader
        entity.requestContext.timeoutHelper = new TimeoutHelper(Duration.ofSeconds(2 * 60 * 60 + 2 * 60 + 2));

        // when the store reader throws INVALID Partition exception, the higher layer should
        entity.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange();

        AddressInformation[] addressInformations = getMockAddressInformationDuringUpgrade();
        IAddressResolver mockAddressCache = getMockAddressCache(addressInformations);

        // validate that the mock works
        AddressInformation[] addressInfo = mockAddressCache.resolveAsync(entity, false).block();
        assertThat(addressInfo[0]).isEqualTo(addressInformations[0]);

        AddressSelector addressSelector = new AddressSelector(mockAddressCache, Protocol.TCP);
        Uri primaryAddress = addressSelector.resolvePrimaryUriAsync(entity, false).block();

        // check if the address return from Address Selector matches the original address info
        assertThat(primaryAddress.toString()).isEqualTo(addressInformations[0].getPhysicalUri());

        // get mock transport client that returns a sequence of responses to simulate upgrade
        TransportClient mockTransportClient = getMockTransportClientDuringUpgrade(addressInformations);

        // get response from mock object
        StoreResponse response = mockTransportClient.invokeResourceOperationAsync(addressInformations[0].getPhysicalUri(), entity).block();

        // validate that the LSN matches
        assertThat(response.getLSN()).isEqualTo(50);

        String activityId = response.getHeaderValue(WFConstants.BackendHeaders.ACTIVITY_ID);
        // validate that the ActivityId Matches
        assertThat(activityId).isEqualTo("ACTIVITYID1_1");

        // create a real session container - we don't need session for this test anyway
        SessionContainer sessionContainer = new SessionContainer(StringUtils.EMPTY);

        // create store reader with mock transport client, real address selector (that has mock address cache), and real session container
        StoreReader storeReader = new StoreReader(mockTransportClient, addressSelector, sessionContainer);

        IAuthorizationTokenProvider mockAuthorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        Mockito.when(mockAuthorizationTokenProvider.getUserAuthorizationToken(ArgumentMatchers.anyString(), ArgumentMatchers.any(), ArgumentMatchers.any(RequestVerb.class), ArgumentMatchers.anyMap(),
                ArgumentMatchers.any(), ArgumentMatchers.anyMap())).thenReturn("dummyauthtoken");

        // setup max replica set size on the config reader
        ReplicationPolicy replicationPolicy = new ReplicationPolicy();
        GatewayServiceConfigurationReader mockServiceConfigReader = Mockito.mock(GatewayServiceConfigurationReader.class);
        Mockito.when(mockServiceConfigReader.getUserReplicationPolicy()).thenReturn(replicationPolicy);

        try {
            StoreClient storeClient = new StoreClient(mockDiagnosticsClientContext(), new Configs(),mockAddressCache, sessionContainer, mockServiceConfigReader, mockAuthorizationTokenProvider, mockTransportClient, false);

            ServerStoreModel storeModel = new ServerStoreModel(storeClient);
            Mono<RxDocumentServiceResponse> result = storeModel.processMessage(entity).single();
            result.block();

            // if we have reached this point, there was a successful request.
            // validate if the target identity has been cleared out.
            // If the target identity is null and the request still succeeded, it means
            // that the very first read succeeded without a barrier request.
            assertThat(entity.requestContext.resolvedPartitionKeyRange).isNotNull();
        } catch (Exception e) {
            assertThat(e instanceof ServiceUnavailableException
                    || e instanceof IllegalArgumentException
                    || e instanceof NullPointerException
                    || e instanceof NoSuchElementException).isTrue();
        }
    }

    /**
     * test consistency writer for global strong
     */
    @Test(groups = "unit")
    @SuppressWarnings("unchecked")
    public void globalStrongConsistentWrite() {
        // create a real document service request (with auth token level = god)
        RxDocumentServiceRequest entity = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Document);
        entity.authorizationTokenType = AuthorizationTokenType.PrimaryMasterKey;

        // set request charge tracker -  this is referenced in store reader (ReadMultipleReplicaAsync)
        DocumentServiceRequestContext requestContext = new DocumentServiceRequestContext();
        requestContext.requestChargeTracker = new RequestChargeTracker();
        entity.requestContext = requestContext;

        // set a dummy resource id on the request.
        entity.setResourceId("1-MxAPlgMgA=");

        // set consistency level on the request to Bounded Staleness
        entity.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.STRONG.toString());

        // also setup timeout helper, used in store reader
        entity.requestContext.timeoutHelper = new TimeoutHelper(Duration.ofSeconds(2 * 60 * 60 + 2 * 60 + 2));

        // when the store reader throws INVALID Partition exception, the higher layer should
        // clear this target identity.
        entity.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange();

        AddressInformation[] addressInformations = getMockAddressInformationDuringUpgrade();
        IAddressResolver mockAddressCache = getMockAddressCache(addressInformations);

        // validate that the mock works
        AddressInformation[] addressInfo = mockAddressCache.resolveAsync(entity, false).block();
        assertThat(addressInformations[0]).isEqualTo(addressInfo[0]);

        AddressSelector addressSelector = new AddressSelector(mockAddressCache, Protocol.TCP);
        Uri primaryAddress = addressSelector.resolvePrimaryUriAsync(entity, false).block();

        // check if the address return from Address Selector matches the original address info
        assertThat(primaryAddress).isEqualTo(addressInformations[0].getPhysicalUri());

        // create a real session container - we don't need session for this test anyway
        SessionContainer sessionContainer = new SessionContainer(StringUtils.EMPTY);
        GatewayServiceConfigurationReader serviceConfigurationReader = Mockito.mock(GatewayServiceConfigurationReader.class);

        IAuthorizationTokenProvider mockAuthorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        Mockito.when(mockAuthorizationTokenProvider.getUserAuthorizationToken(ArgumentMatchers.anyString(), ArgumentMatchers.any(), ArgumentMatchers.any(RequestVerb.class), ArgumentMatchers.anyMap(),
                ArgumentMatchers.any(), ArgumentMatchers.anyMap())).thenReturn("dummyauthtoken");

        for (int i = 0; i < addressInformations.length; i++) {
            TransportClient mockTransportClient = getMockTransportClientForGlobalStrongWrites(addressInformations, i, false, false, false);
            StoreReader storeReader = new StoreReader(mockTransportClient, addressSelector, sessionContainer);
            ConsistencyWriter consistencyWriter = new ConsistencyWriter(mockDiagnosticsClientContext(), addressSelector, sessionContainer, mockTransportClient, mockAuthorizationTokenProvider, serviceConfigurationReader, false);
            StoreResponse response = consistencyWriter.writeAsync(entity, new TimeoutHelper(Duration.ofSeconds(30)), false).block();
            assertThat(response.getLSN()).isEqualTo(100);

            //globalCommittedLsn never catches up in this case
            mockTransportClient = getMockTransportClientForGlobalStrongWrites(addressInformations, i, true, false, false);
            consistencyWriter = new ConsistencyWriter(mockDiagnosticsClientContext(), addressSelector, sessionContainer, mockTransportClient, mockAuthorizationTokenProvider, serviceConfigurationReader, false);
            try {
                response = consistencyWriter.writeAsync(entity, new TimeoutHelper(Duration.ofSeconds(30)), false).block();
                // fail("it should throw exception");
            } catch (Exception e) {
            }

            mockTransportClient = getMockTransportClientForGlobalStrongWrites(addressInformations, i, false, true, false);
            storeReader = new StoreReader(mockTransportClient, addressSelector, sessionContainer);
            consistencyWriter = new ConsistencyWriter(mockDiagnosticsClientContext(), addressSelector, sessionContainer, mockTransportClient, mockAuthorizationTokenProvider, serviceConfigurationReader, false);
            response = consistencyWriter.writeAsync(entity, new TimeoutHelper(Duration.ofSeconds(30)), false).block();
            assertThat(response.getLSN()).isEqualTo(100);

            mockTransportClient = getMockTransportClientForGlobalStrongWrites(addressInformations, i, false, true, true);
            storeReader = new StoreReader(mockTransportClient, addressSelector, sessionContainer);
            consistencyWriter = new ConsistencyWriter(mockDiagnosticsClientContext(), addressSelector, sessionContainer, mockTransportClient, mockAuthorizationTokenProvider, serviceConfigurationReader, false);
            response = consistencyWriter.writeAsync(entity, new TimeoutHelper(Duration.ofSeconds(30)), false).block();
            assertThat(response.getLSN()).isEqualTo(100);


            mockTransportClient = getMockTransportClientForGlobalStrongWrites(addressInformations, i, false, false, true);
            storeReader = new StoreReader(mockTransportClient, addressSelector, sessionContainer);
            consistencyWriter = new ConsistencyWriter(mockDiagnosticsClientContext(), addressSelector, sessionContainer, mockTransportClient, mockAuthorizationTokenProvider, serviceConfigurationReader, false);
            response = consistencyWriter.writeAsync(entity, new TimeoutHelper(Duration.ofSeconds(30)), false).block();
            assertThat(response.getLSN()).isEqualTo(100);

        }
    }

    /**
     * Mocking Consistency
     */
    @Test(groups = "unit", priority = 1)
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void globalStrongConsistency() {
        // create a real document service request (with auth token level = god)
        RxDocumentServiceRequest entity = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        entity.authorizationTokenType = AuthorizationTokenType.PrimaryMasterKey;

        // set request charge tracker -  this is referenced in store reader (ReadMultipleReplicaAsync)
        DocumentServiceRequestContext requestContext = new DocumentServiceRequestContext();
        requestContext.requestChargeTracker = new RequestChargeTracker();
        entity.requestContext = requestContext;

        // set a dummy resource id on the request.
        entity.setResourceId("1-MxAPlgMgA=");

        // set consistency level on the request to Bounded Staleness
        entity.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.BOUNDED_STALENESS.toString());

        // also setup timeout helper, used in store reader
        entity.requestContext.timeoutHelper = new TimeoutHelper(Duration.ofSeconds(2 * 60 * 60 + 2 * 60 + 2));

        // when the store reader throws INVALID Partition exception, the higher layer should
        // clear this target identity.
        entity.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange();

        AddressInformation[] addressInformations = getMockAddressInformationDuringUpgrade();
        IAddressResolver mockAddressCache = getMockAddressCache(addressInformations);

        // validate that the mock works
        AddressInformation[] addressInfo = mockAddressCache.resolveAsync(entity, false).block();
        assertThat(addressInfo[0]).isEqualTo(addressInformations[0]);

        AddressSelector addressSelector = new AddressSelector(mockAddressCache, Protocol.TCP);
        Uri primaryAddress = addressSelector.resolvePrimaryUriAsync(entity, false).block();

        // check if the address return from Address Selector matches the original address info
        assertThat(primaryAddress).isEqualTo(addressInformations[0].getPhysicalUri());

        // Quorum Met scenario Start
        {
            // get mock transport client that returns a sequence of responses to simulate upgrade
            TransportClient mockTransportClient = getMockTransportClientForGlobalStrongReads(addressInformations, ReadQuorumResultKind.QuorumMet);

            // create a real session container - we don't need session for this test anyway
            SessionContainer sessionContainer = new SessionContainer(StringUtils.EMPTY);

            // create store reader with mock transport client, real address selector (that has mock address cache), and real session container
            StoreReader storeReader = new StoreReader(mockTransportClient, addressSelector, sessionContainer);

            IAuthorizationTokenProvider mockAuthorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
            Mockito.when(mockAuthorizationTokenProvider.getUserAuthorizationToken(ArgumentMatchers.anyString(), ArgumentMatchers.any(), ArgumentMatchers.any(RequestVerb.class), ArgumentMatchers.anyMap(),
                    ArgumentMatchers.any(), ArgumentMatchers.anyMap())).thenReturn("dummyauthtoken");

            // setup max replica set size on the config reader
            ReplicationPolicy replicationPolicy = new ReplicationPolicy();
            GatewayServiceConfigurationReader mockServiceConfigReader = Mockito.mock(GatewayServiceConfigurationReader.class);
            Mockito.when(mockServiceConfigReader.getUserReplicationPolicy()).thenReturn(replicationPolicy);

            QuorumReader reader = new QuorumReader(mockDiagnosticsClientContext(), new Configs(),mockTransportClient, addressSelector, storeReader, mockServiceConfigReader, mockAuthorizationTokenProvider);

            entity.requestContext.originalRequestConsistencyLevel = ConsistencyLevel.STRONG;

            StoreResponse result = reader.readStrongAsync(mockDiagnosticsClientContext(), entity, 2, ReadMode.Strong).block();
            assertThat(result.getLSN()).isEqualTo(100);

            String globalCommitedLSN = result.getHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN);

            long nGlobalCommitedLSN = Long.parseLong(globalCommitedLSN);
            assertThat(nGlobalCommitedLSN).isEqualTo(90);
        }

        // Quorum Selected scenario
        {
            // get mock transport client that returns a sequence of responses to simulate upgrade
            TransportClient mockTransportClient = getMockTransportClientForGlobalStrongReads(addressInformations, ReadQuorumResultKind.QuorumSelected);

            // create a real session container - we don't need session for this test anyway
            SessionContainer sessionContainer = new SessionContainer(StringUtils.EMPTY);

            // create store reader with mock transport client, real address selector (that has mock address cache), and real session container
            StoreReader storeReader = new StoreReader(mockTransportClient, addressSelector, sessionContainer);

            IAuthorizationTokenProvider mockAuthorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
            Mockito.when(mockAuthorizationTokenProvider.getUserAuthorizationToken(ArgumentMatchers.anyString(), ArgumentMatchers.any(), ArgumentMatchers.any(RequestVerb.class), ArgumentMatchers.anyMap(),
                    ArgumentMatchers.any(), ArgumentMatchers.anyMap())).thenReturn("dummyauthtoken");

            // setup max replica set size on the config reader
            ReplicationPolicy replicationPolicy = new ReplicationPolicy();
            BridgeInternal.setMaxReplicaSetSize(replicationPolicy,4);

            GatewayServiceConfigurationReader mockServiceConfigReader = Mockito.mock(GatewayServiceConfigurationReader.class);
            Mockito.when(mockServiceConfigReader.getUserReplicationPolicy()).thenReturn(replicationPolicy);
            Mockito.when(mockServiceConfigReader.getDefaultConsistencyLevel()).thenReturn(ConsistencyLevel.STRONG);

            QuorumReader reader = new QuorumReader(mockDiagnosticsClientContext(), new Configs(), mockTransportClient, addressSelector, storeReader, mockServiceConfigReader, mockAuthorizationTokenProvider);
            entity.requestContext.originalRequestConsistencyLevel = ConsistencyLevel.STRONG;
            entity.requestContext.quorumSelectedLSN = -1;
            entity.requestContext.globalCommittedSelectedLSN = -1;
            try {
                StoreResponse result = reader.readStrongAsync(mockDiagnosticsClientContext(), entity, 2, ReadMode.Strong).block();
                assertThat(false).isTrue();
            } catch (Exception ex) {
                if (ex.getCause() instanceof GoneException) {
                    logger.info("Gone exception expected!");
                }
            }

            assertThat(entity.requestContext.quorumSelectedLSN).isEqualTo(100);
            assertThat(entity.requestContext.globalCommittedSelectedLSN).isEqualTo(100);
        }

        // Quorum not met scenario
        {
            // get mock transport client that returns a sequence of responses to simulate upgrade
            TransportClient mockTransportClient = getMockTransportClientForGlobalStrongReads(addressInformations, ReadQuorumResultKind.QuorumNotSelected);

            // create a real session container - we don't need session for this test anyway
            SessionContainer sessionContainer = new SessionContainer(StringUtils.EMPTY);

            // create store reader with mock transport client, real address selector (that has mock address cache), and real session container
            StoreReader storeReader =
                    new StoreReader(mockTransportClient,
                            addressSelector,
                            sessionContainer);

            IAuthorizationTokenProvider mockAuthorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
            Mockito.when(mockAuthorizationTokenProvider.getUserAuthorizationToken(ArgumentMatchers.anyString(), ArgumentMatchers.any(), ArgumentMatchers.any(RequestVerb.class), ArgumentMatchers.anyMap(),
                    ArgumentMatchers.any(), ArgumentMatchers.anyMap())).thenReturn("dummyauthtoken");
            // setup max replica set size on the config reader
            ReplicationPolicy replicationPolicy = new ReplicationPolicy();
            BridgeInternal.setMaxReplicaSetSize(replicationPolicy,4);

            GatewayServiceConfigurationReader mockServiceConfigReader = Mockito.mock(GatewayServiceConfigurationReader.class);
            Mockito.when(mockServiceConfigReader.getUserReplicationPolicy()).thenReturn(replicationPolicy);
            Mockito.when(mockServiceConfigReader.getDefaultConsistencyLevel()).thenReturn(ConsistencyLevel.STRONG);

            QuorumReader reader = new QuorumReader(mockDiagnosticsClientContext(), new Configs(), mockTransportClient, addressSelector, storeReader, mockServiceConfigReader, mockAuthorizationTokenProvider);
            entity.requestContext.originalRequestConsistencyLevel = ConsistencyLevel.STRONG;
            entity.requestContext.performLocalRefreshOnGoneException = true;

            StoreResponse result = reader.readStrongAsync(mockDiagnosticsClientContext(), entity, 2, ReadMode.Strong).block();
            assertThat(result.getLSN()).isEqualTo(100);

            String globalCommitedLSN;
            globalCommitedLSN = result.getHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN);
            long nGlobalCommitedLSN = Long.parseLong(globalCommitedLSN);
            assertThat(nGlobalCommitedLSN).isEqualTo(90);
        }

    }

    // TODO: more mocking unit tests for different scenarios in StoreReader
    // TODO: more mocking tests on how session work for StoreReader
}

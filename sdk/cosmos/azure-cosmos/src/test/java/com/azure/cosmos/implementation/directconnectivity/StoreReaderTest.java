// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.DocumentServiceRequestContext;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ISessionContainer;
import com.azure.cosmos.implementation.ISessionToken;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionIsMigratingException;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.PartitionKeyRangeGoneException;
import com.azure.cosmos.implementation.PartitionKeyRangeIsSplittingException;
import com.azure.cosmos.implementation.RequestChargeTracker;
import com.azure.cosmos.implementation.RequestRateTooLargeException;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.StoreResponseBuilder;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.VectorSessionToken;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import io.reactivex.subscribers.TestSubscriber;
import org.assertj.core.api.AssertionsForClassTypes;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.azure.cosmos.implementation.HttpConstants.StatusCodes.GONE;
import static com.azure.cosmos.implementation.HttpConstants.SubStatusCodes.COMPLETING_PARTITION_MIGRATION;
import static com.azure.cosmos.implementation.HttpConstants.SubStatusCodes.COMPLETING_SPLIT;
import static com.azure.cosmos.implementation.HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE;
import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static com.azure.cosmos.implementation.TestUtils.mockDocumentServiceRequest;
import static org.assertj.core.api.Assertions.assertThat;

public class StoreReaderTest {
    private final static DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();

    private static final int TIMEOUT = 30000;


    /**
     * Tests for {@link StoreReader}
     */
    @Test(groups = "unit")
    public void startBackgroundAddressRefresh() throws Exception {
        TransportClient transportClient = Mockito.mock(TransportClient.class);
        AddressSelector addressSelector = Mockito.mock(AddressSelector.class);
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);

        StoreReader storeReader = new StoreReader(transportClient, addressSelector, sessionContainer);

        CyclicBarrier b = new CyclicBarrier(2);
        DirectProcessor<List<Uri>> subject = DirectProcessor.create();
        CountDownLatch c = new CountDownLatch(1);

        List<Uri> uris = ImmutableList.of(Uri.create("https://localhost:5050"), Uri.create("https://localhost:5051"),
                                          Uri.create("https://localhost:50502"), Uri.create("https://localhost:5053"));

        Mockito.doAnswer(invocationOnMock -> subject.single().doOnSuccess(x -> c.countDown()).doAfterTerminate(() -> new Thread() {
            @Override
            public void run() {
                try {
                    b.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start())).when(addressSelector).resolveAllUriAsync(Mockito.any(RxDocumentServiceRequest.class), Mockito.eq(true), Mockito.eq(true));
        RxDocumentServiceRequest request = mockDocumentServiceRequest(clientContext);
        storeReader.startBackgroundAddressRefresh(request);

        subject.onNext(uris);
        subject.onComplete();

        TimeUnit.MILLISECONDS.sleep(500);
        AssertionsForClassTypes.assertThat(c.getCount()).isEqualTo(0);
        AssertionsForClassTypes.assertThat(b.getNumberWaiting()).isEqualTo(1);
        b.await(1000, TimeUnit.MILLISECONDS);
    }

    @DataProvider(name = "verifyCanContinueOnExceptionArgProvider")
    public Object[][] verifyCanContinueOnExceptionArgProvider() {
        return new Object[][]{
                {new PartitionKeyRangeGoneException(), false,},
                {new PartitionKeyRangeIsSplittingException(), false,},
                {new PartitionKeyRangeGoneException(), false,},
                {new PartitionIsMigratingException(), false,},
                {new GoneException(), true,},
                {ExceptionBuilder.create().withHeader(HttpConstants.HttpHeaders.REQUEST_VALIDATION_FAILURE, "").asGoneException(), true,},
                {ExceptionBuilder.create().withHeader(HttpConstants.HttpHeaders.REQUEST_VALIDATION_FAILURE, "0").asGoneException(), true,},
                {ExceptionBuilder.create().withHeader(HttpConstants.HttpHeaders.REQUEST_VALIDATION_FAILURE, "1").asGoneException(), false,},
        };
    }

    @Test(groups = "unit", dataProvider = "verifyCanContinueOnExceptionArgProvider")
    public void verifyCanContinueOnException(CosmosException dce, Boolean shouldVerify) {
        CosmosException capturedFailure = null;
        try {
            StoreReader.verifyCanContinueOnException(dce);
        } catch (CosmosException e) {
            capturedFailure = e;
        }

        if (shouldVerify) {
            assertThat(capturedFailure).isNull();
        } else {
            assertThat(capturedFailure).isEqualTo(dce);
        }
    }

    @DataProvider(name = "exceptionArgProvider")
    public Object[][] exceptionArgProvider() {
        return new Object[][]{
                // exception to be thrown from transportClient, expected (exception type, status, subStatus)
                { new PartitionKeyRangeGoneException(), PartitionKeyRangeGoneException.class, GONE, PARTITION_KEY_RANGE_GONE, },
                { new PartitionKeyRangeIsSplittingException() , PartitionKeyRangeIsSplittingException.class, GONE, COMPLETING_SPLIT, },
                { new PartitionIsMigratingException(), PartitionIsMigratingException.class, GONE, COMPLETING_PARTITION_MIGRATION, },
        };
    }

    @DataProvider(name = "storeResponseArgProvider")
    public Object[][] storeResponseArgProvider() {
        return new Object[][]{
            { new PartitionKeyRangeGoneException(), null, },
            { new PartitionKeyRangeIsSplittingException() , null, },
            { new PartitionIsMigratingException(), null, },
            { new GoneException(), null, },
            { null, Mockito.mock(StoreResponse.class), }
        };
    }

    @Test(groups = "unit", dataProvider = "exceptionArgProvider")
    public void exception(Exception ex, Class<Exception> klass, int expectedStatusCode, Integer expectedSubStatusCode) {
        TransportClientWrapper transportClientWrapper = new TransportClientWrapper.Builder.ReplicaResponseBuilder
                .SequentialBuilder()
                .then(ex)
                .build();

        Uri primaryUri = Uri.create("primary");
        Uri secondaryUri1 = Uri.create("secondary1");
        Uri secondaryUri2 = Uri.create("secondary2");
        Uri secondaryUri3 = Uri.create("secondary3");

        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create()
                .withPrimary(primaryUri)
                .withSecondary(ImmutableList.of(secondaryUri1, secondaryUri2, secondaryUri3))
                .build();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        StoreReader storeReader = new StoreReader(transportClientWrapper.transportClient, addressSelectorWrapper.addressSelector, sessionContainer);

        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.requestContext = Mockito.mock(DocumentServiceRequestContext.class);
        dsr.requestContext.timeoutHelper = timeoutHelper;
        dsr.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId("1");
        Mono<List<StoreResult>> res = storeReader.readMultipleReplicaAsync(dsr, true, 3, true, true, ReadMode.Strong);

        FailureValidator failureValidator = FailureValidator.builder()
                .instanceOf(klass)
                .statusCode(expectedStatusCode)
                .subStatusCode(expectedSubStatusCode)
                .build();

        TestSubscriber<List<StoreResult>> subscriber = new TestSubscriber<>();
        res.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNotComplete();
        assertThat(subscriber.errorCount()).isEqualTo(1);
        failureValidator.validate(subscriber.errors().get(0));
    }

    /**
     * reading in session consistency, if the requested session token cannot be supported by some replicas
     * tries others till we find a replica which can support the given session token
     */
    @Test(groups = "unit")
    public void sessionNotAvailableFromSomeReplicas_FindReplicaSatisfyingRequestedSession() {
        long slowReplicaLSN = 651175;
        long globalCommittedLsn = 651174;
        String partitionKeyRangeId = "73";
        NotFoundException foundException = new NotFoundException();
        foundException.getResponseHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, partitionKeyRangeId + ":-1#" + slowReplicaLSN);
        foundException.getResponseHeaders().put(WFConstants.BackendHeaders.LSN, Long.toString(slowReplicaLSN));
        foundException.getResponseHeaders().put(WFConstants.BackendHeaders.LOCAL_LSN, Long.toString(slowReplicaLSN));
        foundException.getResponseHeaders().put(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, Long.toString(globalCommittedLsn));

        long fasterReplicaLSN = 651176;

        StoreResponse storeResponse = StoreResponseBuilder.create()
                .withSessionToken(partitionKeyRangeId + ":-1#" + fasterReplicaLSN)
                .withLSN(fasterReplicaLSN)
                .withLocalLSN(fasterReplicaLSN)
                .withQuorumAckecdLsn(fasterReplicaLSN)
                .withQuorumAckecdLocalLsn(fasterReplicaLSN)
                .withGlobalCommittedLsn(-1)
                .withItemLocalLSN(fasterReplicaLSN)
                .withRequestCharge(1.1)
                .build();

        TransportClientWrapper transportClientWrapper = new TransportClientWrapper.Builder.ReplicaResponseBuilder
                .SequentialBuilder()
                .then(foundException) // 1st replica read returns not found with lower lsn
                .then(foundException) // 2nd replica read returns not found with lower lsn
                .then(foundException) // 3rd replica read returns not found with lower lsn
                .then(storeResponse)  // 4th replica read returns storeResponse satisfying requested session token
                .build();

        Uri primaryUri = Uri.create("primary");
        Uri secondaryUri1 = Uri.create("secondary1");
        Uri secondaryUri2 = Uri.create("secondary2");
        Uri secondaryUri3 = Uri.create("secondary3");

        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create()
                .withPrimary(primaryUri)
                .withSecondary(ImmutableList.of(secondaryUri1, secondaryUri2, secondaryUri3))
                .build();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        StoreReader storeReader = new StoreReader(transportClientWrapper.transportClient, addressSelectorWrapper.addressSelector, sessionContainer);

        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.SESSION.toString());
        dsr.requestContext = new DocumentServiceRequestContext();
        Utils.ValueHolder<ISessionToken> sessionToken = Utils.ValueHolder.initialize(null);
        dsr.requestContext.sessionToken = sessionToken.v;
        dsr.requestContext.timeoutHelper = timeoutHelper;
        dsr.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId(partitionKeyRangeId);
        dsr.requestContext.requestChargeTracker = new RequestChargeTracker();
        assertThat(VectorSessionToken.tryCreate("-1#" + fasterReplicaLSN , sessionToken)).isTrue();

        Mockito.doReturn(sessionToken.v).when(sessionContainer).resolvePartitionLocalSessionToken(Mockito.eq(dsr), Mockito.anyString());

        Mono<List<StoreResult>> readResult = storeReader.readMultipleReplicaAsync(
                dsr,
                /* includePrimary */ true,
                /* replicaCountToRead */ 1,
                /* requiresValidLSN */ true,
                /* useSessionToken */ true,
                /* readMode */ ReadMode.Any,
                /* checkMinLsn */ true,
                /* forceReadAll */ false);

        MultiStoreResultValidator validator = MultiStoreResultValidator.create()
                .withSize(1)
                .validateEachWith(StoreResultValidator.create()
                                          .isValid()
                                          .noException()
                                          .withStoreResponse(StoreResponseValidator.create()
                                                                     .isSameAs(storeResponse)
                                                                     .build())
                                          .build())
                .build();
        validateSuccess(readResult, validator);

        addressSelectorWrapper.validate()
                .verifyNumberOfForceCachRefresh(0)
                .verifyVesolvePrimaryUriAsyncCount(0)
                .verifyTotalInvocations(1);
    }

    /**
     * Reading with session consistency, replicas have session token with higher than requested and return not found
     */
    @Test(groups = "unit")
    public void sessionRead_LegitimateNotFound() {
        long lsn = 651175;
        long globalCommittedLsn = 651174;
        String partitionKeyRangeId = "73";

        NotFoundException foundException = new NotFoundException();
        foundException.getResponseHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, partitionKeyRangeId + ":-1#" + lsn);
        foundException.getResponseHeaders().put(WFConstants.BackendHeaders.LSN, Long.toString(lsn));
        foundException.getResponseHeaders().put(WFConstants.BackendHeaders.LOCAL_LSN, Long.toString(lsn));
        foundException.getResponseHeaders().put(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, Long.toString(globalCommittedLsn));

        TransportClientWrapper transportClientWrapper = new TransportClientWrapper.Builder.ReplicaResponseBuilder
                .SequentialBuilder()
                .then(foundException) // 1st replica read returns not found
                .then(foundException) // 2nd replica read returns not found
                .then(foundException) // 3rd replica read returns not found
                .then(foundException) // 4th replica read returns not found
                .build();

        Uri primaryUri = Uri.create("primary");
        Uri secondaryUri1 = Uri.create("secondary1");
        Uri secondaryUri2 = Uri.create("secondary2");
        Uri secondaryUri3 = Uri.create("secondary3");

        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create()
                .withPrimary(primaryUri)
                .withSecondary(ImmutableList.of(secondaryUri1, secondaryUri2, secondaryUri3))
                .build();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        StoreReader storeReader = new StoreReader(transportClientWrapper.transportClient, addressSelectorWrapper.addressSelector, sessionContainer);

        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.SESSION.toString());
        dsr.requestContext = new DocumentServiceRequestContext();
        Utils.ValueHolder<ISessionToken> sessionToken = Utils.ValueHolder.initialize(null);
        dsr.requestContext.sessionToken = sessionToken.v;
        dsr.requestContext.timeoutHelper = timeoutHelper;
        dsr.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId(partitionKeyRangeId);
        dsr.requestContext.requestChargeTracker = new RequestChargeTracker();
        assertThat(VectorSessionToken.tryCreate("-1#" + (lsn - 1) , sessionToken)).isTrue();

        Mockito.doReturn(sessionToken.v).when(sessionContainer).resolvePartitionLocalSessionToken(Mockito.eq(dsr), Mockito.anyString());

        Mono<List<StoreResult>> readResult = storeReader.readMultipleReplicaAsync(
                dsr,
                /* includePrimary */ true,
                /* replicaCountToRead */ 1,
                /* requiresValidLSN */ true,
                /* useSessionToken */ true,
                /* readMode */ ReadMode.Any,
                /* checkMinLsn */ true,
                /* forceReadAll */ false);

        MultiStoreResultValidator validator = MultiStoreResultValidator.create()
                .withSize(1)
                .validateEachWith(StoreResultValidator.create()
                                          .isValid()
                                          .withException(FailureValidator.builder().instanceOf(NotFoundException.class).build())
                                          .build())
                .build();
        validateSuccess(readResult, validator);
    }

    /**
     * reading in session consistency, none of the replicas can support the requested session token.
     */
    @Test(groups = "unit")
    public void sessionRead_ReplicasDoNotHaveTheRequestedLSN_NoResult() {
        long lsn = 651175;
        long globalCommittedLsn = 651174;
        String partitionKeyRangeId = "73";

        NotFoundException foundException = new NotFoundException();
        foundException.getResponseHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, partitionKeyRangeId + ":-1#" + lsn);
        foundException.getResponseHeaders().put(WFConstants.BackendHeaders.LSN, Long.toString(lsn));
        foundException.getResponseHeaders().put(WFConstants.BackendHeaders.LOCAL_LSN, Long.toString(lsn));
        foundException.getResponseHeaders().put(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, Long.toString(globalCommittedLsn));

        TransportClientWrapper transportClientWrapper = new TransportClientWrapper.Builder.ReplicaResponseBuilder
                .SequentialBuilder()
                .then(foundException) // 1st replica read returns not found
                .then(foundException) // 2nd replica read returns not found
                .then(foundException) // 3rd replica read returns not found
                .then(foundException) // 4th replica read returns not found
                .build();

        Uri primaryUri = Uri.create("primary");
        Uri secondaryUri1 = Uri.create("secondary1");
        Uri secondaryUri2 = Uri.create("secondary2");
        Uri secondaryUri3 = Uri.create("secondary3");

        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create()
                .withPrimary(primaryUri)
                .withSecondary(ImmutableList.of(secondaryUri1, secondaryUri2, secondaryUri3))
                .build();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        StoreReader storeReader = new StoreReader(transportClientWrapper.transportClient, addressSelectorWrapper.addressSelector, sessionContainer);

        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.SESSION.toString());
        dsr.requestContext = new DocumentServiceRequestContext();
        Utils.ValueHolder<ISessionToken> sessionToken = Utils.ValueHolder.initialize(null);
        dsr.requestContext.sessionToken = sessionToken.v;
        dsr.requestContext.timeoutHelper = timeoutHelper;
        dsr.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId(partitionKeyRangeId);
        dsr.requestContext.requestChargeTracker = new RequestChargeTracker();
        assertThat(VectorSessionToken.tryCreate("-1#" + (lsn + 1) , sessionToken)).isTrue();

        Mockito.doReturn(sessionToken.v).when(sessionContainer).resolvePartitionLocalSessionToken(Mockito.eq(dsr), Mockito.anyString());

        Mono<List<StoreResult>> readResult = storeReader.readMultipleReplicaAsync(
                dsr,
                /* includePrimary */ true,
                /* replicaCountToRead */ 1,
                /* requiresValidLSN */ true,
                /* useSessionToken */ true,
                /* readMode */ ReadMode.Any,
                /* checkMinLsn */ true,
                /* forceReadAll */ false);

        MultiStoreResultValidator validator = MultiStoreResultValidator.create()
                .withSize(0)
                .build();
        validateSuccess(readResult, validator);
    }

    @Test(groups = "unit")
    public void requestRateTooLarge_BubbleUp() {
        long lsn = 1045395;
        long globalCommittedLsn = 1045395;
        String partitionKeyRangeId = "257";

        RequestRateTooLargeException requestRateTooLargeException = new RequestRateTooLargeException();
        requestRateTooLargeException.getResponseHeaders().put(HttpConstants.HttpHeaders.LSN, Long.toString(lsn));
        requestRateTooLargeException.getResponseHeaders().put(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, Long.toString(globalCommittedLsn));
        requestRateTooLargeException.getResponseHeaders().put(WFConstants.BackendHeaders.LOCAL_LSN, Long.toString(lsn));
        requestRateTooLargeException.getResponseHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, partitionKeyRangeId + ":-1#" + lsn);

        TransportClientWrapper transportClientWrapper = new TransportClientWrapper.Builder.ReplicaResponseBuilder
                .SequentialBuilder()
                .then(requestRateTooLargeException) // 1st replica read returns 429
                .then(requestRateTooLargeException) // 2nd replica read returns 429
                .then(requestRateTooLargeException) // 3rd replica read returns 429
                .then(requestRateTooLargeException) // 4th replica read returns 429
                .build();

        Uri primaryUri = Uri.create("primary");
        Uri secondaryUri1 = Uri.create("secondary1");
        Uri secondaryUri2 = Uri.create("secondary2");
        Uri secondaryUri3 = Uri.create("secondary3");

        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create()
                .withPrimary(primaryUri)
                .withSecondary(ImmutableList.of(secondaryUri1, secondaryUri2, secondaryUri3))
                .build();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        StoreReader storeReader = new StoreReader(transportClientWrapper.transportClient, addressSelectorWrapper.addressSelector, sessionContainer);

        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.SESSION.toString());
        dsr.requestContext = new DocumentServiceRequestContext();
        Utils.ValueHolder<ISessionToken> sessionToken = Utils.ValueHolder.initialize(null);
        dsr.requestContext.sessionToken = sessionToken.v;
        dsr.requestContext.timeoutHelper = timeoutHelper;
        dsr.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId("1");
        dsr.requestContext.requestChargeTracker = new RequestChargeTracker();
        assertThat(VectorSessionToken.tryCreate("-1#" + (lsn - 1)  , sessionToken)).isTrue();

        Mockito.doReturn(sessionToken.v).when(sessionContainer).resolvePartitionLocalSessionToken(Mockito.eq(dsr), Mockito.anyString());

        Mono<List<StoreResult>> readResult = storeReader.readMultipleReplicaAsync(
                dsr,
                /* includePrimary */ true,
                /* replicaCountToRead */ 1,
                /* requiresValidLSN */ true,
                /* useSessionToken */ true,
                /* readMode */ ReadMode.Any,
                /* checkMinLsn */ true,
                /* forceReadAll */ false);

        MultiStoreResultValidator validator = MultiStoreResultValidator.create()
                .withSize(1)
                .validateEachWith(FailureValidator.builder().instanceOf(RequestRateTooLargeException.class).build())
                .build();
        validateSuccess(readResult, validator);
    }

    @Test(groups = "unit")
    public void readPrimaryAsync() {
        TransportClient transportClient = Mockito.mock(TransportClient.class);
        AddressSelector addressSelector = Mockito.mock(AddressSelector.class);
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);

        Uri primaryURI = Uri.create("primaryLoc");

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);

        request.requestContext = Mockito.mock(DocumentServiceRequestContext.class);
        request.requestContext.timeoutHelper = Mockito.mock(TimeoutHelper.class);
        request.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId("12");
        request.requestContext.requestChargeTracker = new RequestChargeTracker();

        Mockito.doReturn(Mono.just(primaryURI)).when(addressSelector).resolvePrimaryUriAsync(
              Mockito.eq(request) , Mockito.eq(false));

        StoreResponse storeResponse = Mockito.mock(StoreResponse.class);
        Mockito.doReturn(Mono.just(storeResponse)).when(transportClient).invokeResourceOperationAsync(Mockito.eq(primaryURI), Mockito.eq(request));

        StoreReader storeReader = new StoreReader(transportClient, addressSelector, sessionContainer);

        Mono<StoreResult> readResult = storeReader.readPrimaryAsync(request, true, true);
        StoreResultValidator validator = StoreResultValidator.create()
                .withStoreResponse(StoreResponseValidator.create().isSameAs(storeResponse).build())
                .build();
        validateSuccess(readResult, validator);
    }

    @Test(groups = "unit")
    public void readPrimaryAsync_GoneFromReplica() {
        TransportClient transportClient = Mockito.mock(TransportClient.class);
        AddressSelector addressSelector = Mockito.mock(AddressSelector.class);
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);

        Uri primaryURI = Uri.create("primaryLoc");

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);

        request.requestContext = Mockito.mock(DocumentServiceRequestContext.class);
        request.requestContext.timeoutHelper = Mockito.mock(TimeoutHelper.class);
        request.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId("12");
        request.requestContext.requestChargeTracker = new RequestChargeTracker();

        Mockito.doReturn(Mono.just(primaryURI)).when(addressSelector).resolvePrimaryUriAsync(
                Mockito.eq(request) , Mockito.eq(false));

        Mockito.doReturn(Mono.error(ExceptionBuilder.create().asGoneException())).when(transportClient).invokeResourceOperationAsync(Mockito.eq(primaryURI), Mockito.eq(request));
        StoreReader storeReader = new StoreReader(transportClient, addressSelector, sessionContainer);
        Mono<StoreResult> readResult = storeReader.readPrimaryAsync(request, true, true);

        FailureValidator validator = FailureValidator.builder().instanceOf(GoneException.class).build();
        validateException(readResult, validator);
    }

    @Test(groups = "unit")
    public void readPrimaryAsync_GoneExceptionOnTimeout() {
        TransportClient transportClient = Mockito.mock(TransportClient.class);
        AddressSelector addressSelector = Mockito.mock(AddressSelector.class);
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);

        Uri primaryURI = Uri.create("primaryLoc");

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);

        request.requestContext = Mockito.mock(DocumentServiceRequestContext.class);
        request.requestContext.timeoutHelper = Mockito.mock(TimeoutHelper.class);
        Mockito.doReturn(true).when(request.requestContext.timeoutHelper).isElapsed();
        request.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId("12");
        request.requestContext.requestChargeTracker = new RequestChargeTracker();

        Mockito.doReturn(Mono.just(primaryURI)).when(addressSelector).resolvePrimaryUriAsync(
                Mockito.eq(request) , Mockito.eq(false));

        StoreResponse storeResponse = Mockito.mock(StoreResponse.class);
        Mockito.doReturn(Mono.just(storeResponse)).when(transportClient).invokeResourceOperationAsync(Mockito.eq(primaryURI), Mockito.eq(request));

        StoreReader storeReader = new StoreReader(transportClient, addressSelector, sessionContainer);

        Mono<StoreResult> readResult = storeReader.readPrimaryAsync(request, true, true);
        FailureValidator validator = FailureValidator.builder().instanceOf(GoneException.class).build();
        validateException(readResult, validator);
    }

    @Test(groups = "unit")
    public void canParseLongLsn() {
        TransportClient transportClient = Mockito.mock(TransportClient.class);
        AddressSelector addressSelector = Mockito.mock(AddressSelector.class);
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);

        Uri primaryURI = Uri.create("primaryLoc");

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);

        request.requestContext = Mockito.mock(DocumentServiceRequestContext.class);
        request.requestContext.timeoutHelper = Mockito.mock(TimeoutHelper.class);
        Mockito.doReturn(true).when(request.requestContext.timeoutHelper).isElapsed();
        request.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId("12");
        request.requestContext.requestChargeTracker = new RequestChargeTracker();

        Mockito.doReturn(Mono.just(primaryURI)).when(addressSelector).resolvePrimaryUriAsync(
                Mockito.eq(request) , Mockito.eq(false));


        StoreReader storeReader = new StoreReader(transportClient, addressSelector, sessionContainer);

        long bigLsn = 3629783308L;
        StoreResponse storeResponse = StoreResponseBuilder.create()
                .withLSN(bigLsn)
                .withLocalLSN(bigLsn)
                .withGlobalCommittedLsn(bigLsn)
                .build();

        StoreResult result = storeReader.createStoreResult(storeResponse, null, false, false, null);
        assertThat(result.globalCommittedLSN).isEqualTo(bigLsn);
        assertThat(result.lsn).isEqualTo(bigLsn);
    }

    @DataProvider(name = "readPrimaryAsync_RetryOnGoneArgProvider")
    public Object[][] readPrimaryAsync_RetryOnGoneArgProvider() {
        return new Object[][]{
                // first exception from TransportClient,
                // performLocalRefreshOnGoneException,
                // retry with force refresh expected,
                // validator for expected Exception from Single<StoreResult>
                // StoreResult has a successful StoreResponse
                {
                        // partition moved, refresh replica address cache and retry
                        ExceptionBuilder.create().asGoneException(), true, true, null, true
                },

                {
                        // partition moved, refresh replica address cache is not requested, fail
                        ExceptionBuilder.create().asGoneException(), false, false, FailureValidator.builder().instanceOf(GoneException.class).build(), false
                },

                {
                        // invalid partition exception represents collection stale, cannot succeed, propagate failure
                        ExceptionBuilder.create().asInvalidPartitionException(), true, false, null, false
                },

                {
                        // cannot continue on partition key range gone, require address cache refresh
                        ExceptionBuilder.create().asPartitionKeyRangeGoneException(), true, false,
                        FailureValidator.builder().instanceOf(PartitionKeyRangeGoneException.class).build(), true
                },

                {
                        // cannot continue on partition split, require address cache refresh
                        ExceptionBuilder.create().asPartitionKeyRangeIsSplittingException(), true, false,
                        FailureValidator.builder().instanceOf(PartitionKeyRangeIsSplittingException.class).build(), true
                },

                {
                        // cannot continue on partition split, require address cache refresh
                        ExceptionBuilder.create().asPartitionIsMigratingException(), true, false,
                        FailureValidator.builder().instanceOf(PartitionIsMigratingException.class).build(), true
                },
        };
    }

    @Test(groups = "unit", dataProvider = "readPrimaryAsync_RetryOnGoneArgProvider")
    public void readPrimaryAsync_RetryOnPrimaryReplicaMove(Exception firstExceptionFromTransport,
                                                           boolean performLocalRefreshOnGoneException,
                                                           boolean retryWithForceRefreshExpected,
                                                           FailureValidator failureFromSingle,
                                                           boolean expectedStoreResponseInStoredReadResult) {
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        StoreResponse response = StoreResponseBuilder.create().build();

        TransportClientWrapper transportClientWrapper = TransportClientWrapper.Builder.sequentialBuilder()
                .then(firstExceptionFromTransport)
                .then(response)
                .build();

        Uri primaryURIPriorToRefresh = Uri.create("stale");
        Uri primaryURIAfterRefresh = Uri.create("new");

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);

        request.requestContext.performLocalRefreshOnGoneException = performLocalRefreshOnGoneException;
        request.requestContext.timeoutHelper = Mockito.mock(TimeoutHelper.class);
        request.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId("12");
        request.requestContext.requestChargeTracker = new RequestChargeTracker();

        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.PrimaryReplicaMoveBuilder.create(Protocol.HTTPS)
                .withPrimaryReplicaMove(primaryURIPriorToRefresh, primaryURIAfterRefresh).build();
        StoreReader storeReader = new StoreReader(transportClientWrapper.transportClient, addressSelectorWrapper.addressSelector, sessionContainer);

        Mono<StoreResult> readResult = storeReader.readPrimaryAsync(request, true, true);

        if (failureFromSingle == null) {
            StoreResultValidator validator;
            if (expectedStoreResponseInStoredReadResult) {
                validator = StoreResultValidator.create().withStoreResponse(StoreResponseValidator.create().isSameAs(response).build()).build();
            } else {
                validator = StoreResultValidator.create().withException(FailureValidator.builder().sameAs(firstExceptionFromTransport).build()).build();
            }

           validateSuccess(readResult, validator);
        } else {
            validateException(readResult, failureFromSingle);
        }

        int numberOfAttempts = 1 + (retryWithForceRefreshExpected ? 1: 0);

        transportClientWrapper.validate()
                .verifyNumberOfInvocations(numberOfAttempts);

        addressSelectorWrapper.validate()
                .verifyResolveAddressesAsync(0)
                .verifyResolveAllUriAsync(0)
                .verifyVesolvePrimaryUriAsyncCount(numberOfAttempts)
                .verifyNumberOfForceCachRefresh(retryWithForceRefreshExpected ? 1: 0);
    }

    @DataProvider(name = "readMultipleReplicasAsyncArgProvider")
    public Object[][] readMultipleReplicasAsyncArgProvider() {
        return new Object[][]{
                // boolean includePrimary, int replicaCountToRead, ReadMode.STRONG
                { false, 3, ReadMode.Strong },
                { true, 3, ReadMode.Strong },
                { false, 3, ReadMode.Any },
                { true, 3, ReadMode.Any },
                { true, 2, ReadMode.Any },
                { false, 2, ReadMode.Any },
                { true, 1, ReadMode.Any },
                { false, 1, ReadMode.Any },
        };
    }

    @Test(groups = "unit", dataProvider = "readMultipleReplicasAsyncArgProvider")
    public void readMultipleReplicasAsync(boolean includePrimary, int replicaCountToRead, ReadMode readMode) {
        // This adds basic tests for StoreReader.readMultipleReplicasAsync(.) without failure
        // TODO: add some tests for readMultipleReplicasAsync which mock behaviour of failure of reading from a replica
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        Uri primaryReplicaURI = Uri.create("primary");
        ImmutableList<Uri> secondaryReplicaURIs = ImmutableList.of(Uri.create("secondary1"), Uri.create("secondary2"), Uri.create("secondary3"));
        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create()
                .withPrimary(primaryReplicaURI)
                .withSecondary(secondaryReplicaURIs)
                .build();

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);

        request.requestContext = Mockito.mock(DocumentServiceRequestContext.class);
        request.requestContext.timeoutHelper = Mockito.mock(TimeoutHelper.class);
        request.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId("12");

        request.requestContext.requestChargeTracker = new RequestChargeTracker();

        double requestChargePerRead = 1.1;

        StoreResponse primaryResponse = StoreResponseBuilder.create()
                .withLSN(51)
                .withLocalLSN(18)
                .withRequestCharge(requestChargePerRead)
                .build();
        StoreResponse secondaryResponse1 = StoreResponseBuilder.create()
                .withLSN(50)
                .withLocalLSN(17)
                .withRequestCharge(requestChargePerRead)
                .build();
        StoreResponse secondaryResponse2 = StoreResponseBuilder.create()
                .withLSN(49)
                .withLocalLSN(16)
                .withRequestCharge(requestChargePerRead)
                .build();
        StoreResponse secondaryResponse3 = StoreResponseBuilder.create()
                .withLSN(48)
                .withLocalLSN(15)
                .withRequestCharge(requestChargePerRead)
                .build();

        List<StoreResponse> responseList = ImmutableList.of(primaryResponse, secondaryResponse1, secondaryResponse2, secondaryResponse3);

        TransportClientWrapper transportClientWrapper = TransportClientWrapper.Builder.uriToResultBuilder()
                .storeResponseOn(primaryReplicaURI, OperationType.Read, ResourceType.Document, primaryResponse, false)
                .storeResponseOn(secondaryReplicaURIs.get(0), OperationType.Read, ResourceType.Document, secondaryResponse1, false)
                .storeResponseOn(secondaryReplicaURIs.get(1), OperationType.Read, ResourceType.Document, secondaryResponse2, false)
                .storeResponseOn(secondaryReplicaURIs.get(2), OperationType.Read, ResourceType.Document, secondaryResponse3, false)
                .build();

        StoreReader storeReader = new StoreReader(transportClientWrapper.transportClient, addressSelectorWrapper.addressSelector, sessionContainer);

        Mono<List<StoreResult>> readResult = storeReader.readMultipleReplicaAsync(request, includePrimary, replicaCountToRead, true, true, readMode);

        long expectedMinLsn =
                responseList
                        .stream()
                        .filter(sr -> (sr != primaryResponse || includePrimary))
                        .mapToLong(sr ->
                                   {
                                       String value = (ReadMode.Strong == readMode)?
                                               sr.getHeaderValue(WFConstants.BackendHeaders.LSN) :
                                               sr.getHeaderValue(WFConstants.BackendHeaders.LOCAL_LSN);
                                       return Long.parseLong(value);
                                   })
                        .min().orElse(-1);


        MultiStoreResultValidator validator = MultiStoreResultValidator.create()
                .withSize(replicaCountToRead)
                .withMinimumLSN(expectedMinLsn)
                .noFailure()
                .withTotalRequestCharge(requestChargePerRead * replicaCountToRead)
                .build();
        validateSuccess(readResult, validator);

        transportClientWrapper.validate()
                .verifyNumberOfInvocations(replicaCountToRead);
        addressSelectorWrapper.validate()
                .verifyNumberOfForceCachRefresh(0)
                .verifyVesolvePrimaryUriAsyncCount(0)
                .verifyTotalInvocations(1);
    }

    @Test(groups = "unit", dataProvider = "storeResponseArgProvider")
    public void storeResponseRecordedOnException(Exception ex, StoreResponse storeResponse) {
        TransportClientWrapper transportClientWrapper;

        if (ex != null) {
            transportClientWrapper = new TransportClientWrapper.Builder.ReplicaResponseBuilder
                .SequentialBuilder()
                .then(ex)
                .then(ex)
                .then(ex)
                .then(ex)
                .build();
        } else {
            transportClientWrapper = new TransportClientWrapper.Builder.ReplicaResponseBuilder
                .SequentialBuilder()
                .then(storeResponse)
                .then(storeResponse)
                .then(storeResponse)
                .then(storeResponse)
                .build();
        }

        Uri primaryUri = Uri.create("primary");
        Uri secondaryUri1 = Uri.create("secondary1");
        Uri secondaryUri2 = Uri.create("secondary2");
        Uri secondaryUri3 = Uri.create("secondary3");

        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create()
            .withPrimary(primaryUri)
            .withSecondary(ImmutableList.of(secondaryUri1, secondaryUri2, secondaryUri3))
            .build();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        StoreReader storeReader = new StoreReader(transportClientWrapper.transportClient, addressSelectorWrapper.addressSelector, sessionContainer);

        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.requestContext = new DocumentServiceRequestContext();
        dsr.requestContext.timeoutHelper = timeoutHelper;
        dsr.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId("1");

        try {
            storeReader.readMultipleReplicaAsync(dsr, true, 3, true, true, ReadMode.Strong).subscribe();
        } catch (Exception e) {
            // catch any exceptions here
        }

        String cosmosDiagnostics = dsr.requestContext.cosmosDiagnostics.toString();
        assertThat(this.getMatchingElementCount(cosmosDiagnostics, "storeResult") >= 1).isTrue();
    }

    public static void validateSuccess(Mono<List<StoreResult>> single,
                                       MultiStoreResultValidator validator) {
        validateSuccess(single, validator, 10000);
    }

    public static void validateSuccess(Mono<List<StoreResult>> single,
                                       MultiStoreResultValidator validator, long timeout) {
        TestSubscriber<List<StoreResult>> testSubscriber = new TestSubscriber<>();

        single.flux().subscribe(testSubscriber);
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

    public static <T> void validateException(Mono<T> single,
                                             FailureValidator validator, long timeout) {
        TestSubscriber<T> testSubscriber = new TestSubscriber<>();

        single.flux().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotComplete();
        testSubscriber.assertTerminated();
        assertThat(testSubscriber.errorCount()).isEqualTo(1);
        validator.validate((Throwable) testSubscriber.getEvents().get(1).get(0));
    }

    public static <T> void validateException(Mono<T> single,
                                            FailureValidator validator) {
        validateException(single, validator, TIMEOUT);
    }

    private int getMatchingElementCount(String cosmosDiagnostics, String regex) {
        Pattern storeResultPattern = Pattern.compile(regex);
        Matcher matcher = storeResultPattern.matcher(cosmosDiagnostics);

        int count = 0;
        while (matcher.find()) {
            count++;
        }

        return count;
    }

    private PartitionKeyRange partitionKeyRangeWithId(String id) {
        PartitionKeyRange partitionKeyRange = Mockito.mock(PartitionKeyRange.class);
        Mockito.doReturn(id).when(partitionKeyRange).getId();
        return partitionKeyRange;
    }
}

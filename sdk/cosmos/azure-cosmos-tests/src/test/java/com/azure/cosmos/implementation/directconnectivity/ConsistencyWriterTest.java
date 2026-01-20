// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.AuthorizationTokenType;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants.SubStatusCodes;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.ISessionContainer;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionIsMigratingException;
import com.azure.cosmos.implementation.PartitionKeyRangeGoneException;
import com.azure.cosmos.implementation.PartitionKeyRangeIsSplittingException;
import com.azure.cosmos.implementation.RequestTimeoutException;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RetryContext;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.SessionTokenHelper;
import com.azure.cosmos.implementation.StoreResponseBuilder;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.subscriber.TestSubscriber;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import static com.azure.cosmos.implementation.HttpConstants.StatusCodes.GONE;
import static com.azure.cosmos.implementation.HttpConstants.SubStatusCodes.COMPLETING_PARTITION_MIGRATION;
import static com.azure.cosmos.implementation.HttpConstants.SubStatusCodes.COMPLETING_SPLIT_OR_MERGE;
import static com.azure.cosmos.implementation.HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE;
import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static com.azure.cosmos.implementation.TestUtils.mockDocumentServiceRequest;
import static org.assertj.core.api.Assertions.assertThat;

public class ConsistencyWriterTest {
    private final static DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();

    private AddressSelector addressSelector;
    private ISessionContainer sessionContainer;
    private TransportClient transportClient;
    private GatewayServiceConfigurationReader serviceConfigReader;
    private ConsistencyWriter consistencyWriter;

    @DataProvider(name = "exceptionArgProvider")
    public Object[][] exceptionArgProvider() {
        return new Object[][]{
                // exception to be thrown from transportClient, expected (exception type, status, subStatus)
                { new PartitionKeyRangeGoneException(), PartitionKeyRangeGoneException.class, GONE, PARTITION_KEY_RANGE_GONE, },
                { new PartitionKeyRangeIsSplittingException() , PartitionKeyRangeIsSplittingException.class, GONE, COMPLETING_SPLIT_OR_MERGE, },
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
        sessionContainer = Mockito.mock(ISessionContainer.class);
        IAuthorizationTokenProvider authorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        serviceConfigReader = Mockito.mock(GatewayServiceConfigurationReader.class);

        consistencyWriter = new ConsistencyWriter(clientContext,
                addressSelectorWrapper.addressSelector,
                sessionContainer,
                transportClientWrapper.transportClient,
                authorizationTokenProvider,
                serviceConfigReader,
                false,
                null);

        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        RxDocumentServiceRequest dsr = mockDocumentServiceRequest(clientContext);

        Mono<StoreResponse> res = consistencyWriter.writeAsync(dsr, timeoutHelper, false);

        FailureValidator failureValidator = FailureValidator.builder()
                .instanceOf(klass)
                .statusCode(expectedStatusCode)
                .subStatusCode(expectedSubStatusCode)
                .build();

        StepVerifier.create(res).verifyErrorSatisfies(failureValidator::validate);
    }

    @Test(groups = "unit")
    public void writeAsync_Error() {
        TransportClientWrapper transportClientWrapper = new TransportClientWrapper.Builder.ReplicaResponseBuilder
            .SequentialBuilder()
            .then(Mockito.mock(StoreResponse.class))
            .build();

        Uri primaryUri = Uri.create("primary");
        Uri secondaryUri1 = Uri.create("secondary1");
        Uri secondaryUri2 = Uri.create("secondary2");
        Uri secondaryUri3 = Uri.create("secondary3");

        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create()
                                                                                             .withPrimary(primaryUri)
                                                                                             .withSecondary(ImmutableList.of(secondaryUri1, secondaryUri2, secondaryUri3))
                                                                                             .build();
        sessionContainer = Mockito.mock(ISessionContainer.class);
        IAuthorizationTokenProvider authorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        serviceConfigReader = Mockito.mock(GatewayServiceConfigurationReader.class);
        MockedStatic<SessionTokenHelper> sessionTokenHelperMockedStatic = Mockito.mockStatic(SessionTokenHelper.class);

        consistencyWriter = new ConsistencyWriter(clientContext,
            addressSelectorWrapper.addressSelector,
            sessionContainer,
            transportClientWrapper.transportClient,
            authorizationTokenProvider,
            serviceConfigReader,
            false,
            null);

        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        RxDocumentServiceRequest dsr = mockDocumentServiceRequest(clientContext);
        String outOfMemoryError = "Custom out of memory error";
        Mockito.doThrow(new OutOfMemoryError(outOfMemoryError)).when(SessionTokenHelper.class);
        SessionTokenHelper.setOriginalSessionToken(dsr, null);

        Mono<StoreResponse> res = consistencyWriter.writeAsync(dsr, timeoutHelper, false);

        FailureValidator validator = FailureValidator.builder().instanceOf(OutOfMemoryError.class).errorMessageContains(outOfMemoryError).build();
        validateError(res, validator);
        //  Finally, close the mocked static thread
        sessionTokenHelperMockedStatic.close();
    }

    @Test(groups = "unit")
    public void startBackgroundAddressRefresh() throws Exception {
        initializeConsistencyWriter(false);

        CyclicBarrier b = new CyclicBarrier(2);
        DirectProcessor<Uri> directProcessor = DirectProcessor.create();
        CountDownLatch c = new CountDownLatch(1);

        Uri uri = Uri.create("https://localhost:5050");

        List<InvocationOnMock> invocationOnMocks = Collections.synchronizedList(new ArrayList<>());
        Mockito.doAnswer(invocationOnMock -> {
            invocationOnMocks.add(invocationOnMock);
            return directProcessor.single().doOnSuccess(x -> c.countDown()).doAfterTerminate(() -> new Thread() {
                @Override
                public void run() {
                    try {
                        b.await();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start());
        }).when(addressSelector).resolvePrimaryUriAsync(Mockito.any(RxDocumentServiceRequest.class), Mockito.anyBoolean());
        RxDocumentServiceRequest request = mockDocumentServiceRequest(clientContext);
        consistencyWriter.startBackgroundAddressRefresh(request);

        directProcessor.onNext(uri);
        directProcessor.onComplete();

        TimeUnit.MILLISECONDS.sleep(1000);
        assertThat(c.getCount()).isEqualTo(0);
        assertThat(b.getNumberWaiting()).isEqualTo(1);
        b.await(1000, TimeUnit.MILLISECONDS);
        assertThat(invocationOnMocks).hasSize(1);
        assertThat(invocationOnMocks.get(0).getArgument(1, Boolean.class)).isTrue();
    }

    @Test(groups = "unit")
    public void getLsnAndGlobalCommittedLsn() {
        Map<String, String> headers = new HashMap<>();
        headers.put(WFConstants.BackendHeaders.LSN, "3");
        headers.put(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, "2");

        StoreResponse sr = new StoreResponse(null, 0, headers, null, 0);
        Utils.ValueHolder<Long> lsn = Utils.ValueHolder.initialize(-2l);
        Utils.ValueHolder<Long> globalCommittedLsn = Utils.ValueHolder.initialize(-2l);
        ConsistencyWriter.getLsnAndGlobalCommittedLsn(sr, lsn, globalCommittedLsn);
        assertThat(lsn.v).isEqualTo(3);
        assertThat(globalCommittedLsn.v).isEqualTo(2);
    }


    @Test(groups = "unit")
    public void timeout1() throws Exception {
        initializeConsistencyWriter(false);
        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        Mockito.doReturn(true).when(timeoutHelper).isElapsed();
        ConsistencyWriter spyConsistencyWriter = Mockito.spy(this.consistencyWriter);
        RxDocumentServiceRequest request = mockDocumentServiceRequest(clientContext);
        ClientSideRequestStatistics clientSideRequestStatistics = BridgeInternal.getClientSideRequestStatics(request.requestContext.cosmosDiagnostics);
        RetryContext retryContext = Mockito.mock(RetryContext.class);
        ReflectionUtils.setRetryContext(clientSideRequestStatistics, retryContext);
        Mockito.doReturn(2).when(retryContext).getRetryCount();

        StepVerifier.create(spyConsistencyWriter.writeAsync(request, timeoutHelper, false))
            .expectError(RequestTimeoutException.class)
            .verify(Duration.ofMillis(10));
    }

    @Test(groups = "unit")
    public void timeout2() throws Exception {
        initializeConsistencyWriter(false);
        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        Mockito.doReturn(false).doReturn(true).when(timeoutHelper).isElapsed();
        ConsistencyWriter spyConsistencyWriter = Mockito.spy(this.consistencyWriter);
        RxDocumentServiceRequest request = mockDocumentServiceRequest(clientContext);
        ClientSideRequestStatistics clientSideRequestStatistics = BridgeInternal.getClientSideRequestStatics(request.requestContext.cosmosDiagnostics);
        RetryContext retryContext = Mockito.mock(RetryContext.class);
        ReflectionUtils.setRetryContext(clientSideRequestStatistics, retryContext);
        Mockito.doReturn(2).when(retryContext).getRetryCount();

        StepVerifier.create(spyConsistencyWriter.writeAsync(request, timeoutHelper, false))
            .expectError(RequestTimeoutException.class)
            .verify(Duration.ofMillis(10));
    }

    @Test(groups = "unit", dataProvider = "storeResponseArgProvider")
    public void storeResponseRecordedOnException(Exception ex, StoreResponse storeResponse) {
        DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();
        TransportClientWrapper transportClientWrapper;

        if (ex != null) {
            transportClientWrapper = new TransportClientWrapper.Builder.ReplicaResponseBuilder
                .SequentialBuilder()
                .then(ex)
                .build();
        } else {
            transportClientWrapper = new TransportClientWrapper.Builder.ReplicaResponseBuilder
                .SequentialBuilder()
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
        sessionContainer = Mockito.mock(ISessionContainer.class);
        IAuthorizationTokenProvider authorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        serviceConfigReader = Mockito.mock(GatewayServiceConfigurationReader.class);

        consistencyWriter = new ConsistencyWriter(clientContext,
            addressSelectorWrapper.addressSelector,
            sessionContainer,
            transportClientWrapper.transportClient,
            authorizationTokenProvider,
            serviceConfigReader,
            false,
            null);

        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        RxDocumentServiceRequest dsr = mockDocumentServiceRequest(clientContext);

        //  do nothing
        consistencyWriter.writeAsync(dsr, timeoutHelper, false).onErrorResume(Mono::error).subscribe();

        String cosmosDiagnostics = dsr.requestContext.cosmosDiagnostics.toString();
        assertThat(cosmosDiagnostics).containsOnlyOnce("storeResult");
    }

    @DataProvider(name = "globalStrongArgProvider")
    public Object[][] globalStrongArgProvider() {
        return new Object[][]{
                {
                        ConsistencyLevel.SESSION,
                        mockDocumentServiceRequest(clientContext),
                        Mockito.mock(StoreResponse.class),

                        false,
                },
                {
                        ConsistencyLevel.EVENTUAL,
                        mockDocumentServiceRequest(clientContext),
                        Mockito.mock(StoreResponse.class),

                        false,
                },
                {

                        ConsistencyLevel.EVENTUAL,
                        mockDocumentServiceRequest(clientContext),
                        StoreResponseBuilder.create()
                                .withHeader(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS, Integer.toString(5))
                                .build(),
                        false,
                },
                {

                        ConsistencyLevel.STRONG,
                        mockDocumentServiceRequest(clientContext),
                        StoreResponseBuilder.create()
                                .withHeader(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS, Integer.toString(5))
                                .build(),
                        true,
                },
                {

                        ConsistencyLevel.STRONG,
                        mockDocumentServiceRequest(clientContext),
                        StoreResponseBuilder.create()
                                .withHeader(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS, Integer.toString(0))
                                .build(),
                        false,
                }
        };
    }

    @Test(groups = "unit", dataProvider = "globalStrongArgProvider")
    public void isGlobalStrongRequest(ConsistencyLevel defaultConsistencyLevel, RxDocumentServiceRequest req, StoreResponse storeResponse, boolean isGlobalStrongExpected) {
        initializeConsistencyWriter(false);
        Mockito.doReturn(defaultConsistencyLevel).when(this.serviceConfigReader).getDefaultConsistencyLevel();


        assertThat(consistencyWriter.isGlobalStrongRequest(req, storeResponse)).isEqualTo(isGlobalStrongExpected);
    }

    @Test(groups = "unit")
    public void writeAsyncGlobalStrongRequest() {
        runWriteAsyncBarrierableRequestTest(true, true);
    }

    @Test(groups = "unit")
    public void writeAsyncGlobalStrongRequestFailed() {
        runWriteAsyncBarrierableRequestTest(true, false);
    }

    @Test(groups = "unit")
    public void writeAsyncNRegionCommitRequest() {
        runWriteAsyncBarrierableRequestTest(false, true);
    }

    @Test(groups = "unit")
    public void writeAsyncNRegionCommitRequestFailed() {
        runWriteAsyncBarrierableRequestTest(false, false);
    }

    @Test
    public void writeAsyncNoBarrierRequest() {
        initializeConsistencyWriter(false);
        RxDocumentServiceRequest request = mockDocumentServiceRequest(clientContext);
        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        Mockito.doReturn(false).when(timeoutHelper).isElapsed();
        StoreResponse storeResponse = Mockito.mock(StoreResponse.class);
        Mockito.doReturn("0").when(storeResponse).getHeaderValue(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS);
        Mockito.doReturn(ConsistencyLevel.SESSION).when(serviceConfigReader).getDefaultConsistencyLevel();
        ConsistencyWriter spyWriter = Mockito.spy(this.consistencyWriter);
        Mockito.doReturn(Mono.just(storeResponse)).when(spyWriter).barrierForGlobalStrong(Mockito.any(), Mockito.any(), Mockito.any());
        AddressInformation addressInformation = Mockito.mock(AddressInformation.class);
        Uri primaryUri = Mockito.mock(Uri.class);
        Mockito.doReturn(true).when(primaryUri).isPrimary();
        Mockito.doReturn("Healthy").when(primaryUri).getHealthStatusDiagnosticString();
        Mockito.doReturn(primaryUri).when(addressInformation).getPhysicalUri();
        List<AddressInformation> addressList = Collections.singletonList(addressInformation);
        Mockito.doReturn(Mono.just(addressList)).when(addressSelector).resolveAddressesAsync(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(Mono.just(storeResponse)).when(transportClient).invokeResourceOperationAsync(Mockito.any(Uri.class), Mockito.any(RxDocumentServiceRequest.class));
        Mono<StoreResponse> result = spyWriter.writeAsync(request, timeoutHelper, false);
        TestSubscriber<StoreResponse> subscriber = new TestSubscriber<>();
        result.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertValue(storeResponse);
    }

    @Test
    public void isBarrierRequest() {
        // Setup ConsistencyWriter with useMultipleWriteLocations false
        initializeConsistencyWriter(false);
        ConsistencyWriter writer = this.consistencyWriter;
        RxDocumentServiceRequest request = mockDocumentServiceRequest(clientContext);
        StoreResponse response = Mockito.mock(StoreResponse.class);

        // 1. Global strong enabled and isGlobalStrongRequest returns true
        try (MockedStatic<ReplicatedResourceClient> replicatedResourceClientMock = Mockito.mockStatic(ReplicatedResourceClient.class)) {
            replicatedResourceClientMock.when(ReplicatedResourceClient::isGlobalStrongEnabled).thenReturn(true);
            ConsistencyWriter spyWriter = Mockito.spy(writer);
            Mockito.doReturn(true).when(spyWriter).isGlobalStrongRequest(request, response);
            boolean result = spyWriter.isBarrierRequest(request, response);
            assertThat(result).isTrue();
        }

        // 2. NRegionSynchronousCommitEnabled path
        // Setup request.requestContext.getNRegionSynchronousCommitEnabled() to true
        request.requestContext.setNRegionSynchronousCommitEnabled(true);
        // useMultipleWriteLocations is already false
        Mockito.doReturn("123").when(response).getHeaderValue(WFConstants.BackendHeaders.GLOBAL_N_REGION_COMMITTED_GLSN);
        Mockito.doReturn(2L).when(response).getNumberOfReadRegions();
        boolean nRegionResult = writer.isBarrierRequest(request, response);
        assertThat(nRegionResult).isTrue();

        // 3. Negative case: NRegionSynchronousCommitEnabled false
        request.requestContext.setNRegionSynchronousCommitEnabled(false);
        boolean negativeResult = writer.isBarrierRequest(request, response);
        assertThat(negativeResult).isFalse();

        // 4. Negative case: useMultipleWriteLocations true
        initializeConsistencyWriter(true);
        writer = this.consistencyWriter;
        request.requestContext.setNRegionSynchronousCommitEnabled(true);
        boolean negativeResult2 = writer.isBarrierRequest(request, response);
        assertThat(negativeResult2).isFalse();

        // 5. Negative case: GLOBAL_NREGION_COMMITTED_LSN header missing
        initializeConsistencyWriter(false);
        writer = this.consistencyWriter;
        request.requestContext.setNRegionSynchronousCommitEnabled(true);
        Mockito.doReturn(null).when(response).getHeaderValue(WFConstants.BackendHeaders.GLOBAL_N_REGION_COMMITTED_GLSN);
        boolean negativeResult3 = writer.isBarrierRequest(request, response);
        assertThat(negativeResult3).isFalse();

        // 6. Negative case: NUMBER_OF_READ_REGIONS header missing or zero
        Mockito.doReturn(0L).when(response).getNumberOfReadRegions();
        boolean negativeResult4 = writer.isBarrierRequest(request, response);
        assertThat(negativeResult4).isFalse();
    }

    private void runWriteAsyncBarrierableRequestTest(boolean globalStrong, boolean barrierMet) {
        RxDocumentServiceRequest request = setupRequest(!globalStrong);
        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        Mockito.doReturn(false).when(timeoutHelper).isElapsed();
        StoreResponse storeResponse = setupStoreResponse(!globalStrong);
        List<AddressInformation> addressList = setupAddressList();
        List<StoreResult> storeResults = new ArrayList<>();
        if (barrierMet) {
            storeResults.add(getStoreResult(storeResponse, 1L));
            storeResults.add(getStoreResult(storeResponse, 2L));
        } else {
            storeResults.add(getStoreResult(storeResponse, 1L));
        }
        StoreReader storeReader = setupStoreReader(storeResults);
        initializeConsistencyWriterWithStoreReader(false, storeReader);
        ConsistencyWriter spyWriter = Mockito.spy(this.consistencyWriter);
        Mockito.doReturn(globalStrong ? ConsistencyLevel.STRONG : ConsistencyLevel.SESSION)
            .when(serviceConfigReader).getDefaultConsistencyLevel();
        Mockito.doReturn(Mono.just(addressList)).when(addressSelector).resolveAddressesAsync(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(Mono.just(storeResponse)).when(transportClient).invokeResourceOperationAsync(Mockito.any(Uri.class), Mockito.any(RxDocumentServiceRequest.class));
        Mono<StoreResponse> result = spyWriter.writeAsync(request, timeoutHelper, false);
        TestSubscriber<StoreResponse> subscriber = new TestSubscriber<>();
        result.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        if (!barrierMet) {
            subscriber.assertError(GoneException.class);
            FailureValidator failureValidator = FailureValidator.builder()
                .instanceOf(GoneException.class)
                .statusCode(GONE)
                .subStatusCode(globalStrong? SubStatusCodes.GLOBAL_STRONG_WRITE_BARRIER_NOT_MET : SubStatusCodes.GLOBAL_N_REGION_COMMIT_WRITE_BARRIER_NOT_MET)
                .build();
            assertThat(subscriber.errorCount()).isEqualTo(1);
            failureValidator.validate(subscriber.errors().getFirst());
        } else {
            subscriber.assertNoErrors();
            subscriber.assertValue(storeResponse);
        }
    }

    private RxDocumentServiceRequest setupRequest(boolean nRegionCommit) {
        RxDocumentServiceRequest request = mockDocumentServiceRequest(clientContext);
        if (nRegionCommit) {
            request.requestContext.setNRegionSynchronousCommitEnabled(true);
        }
        Mockito.doReturn(ResourceType.Document).when(request).getResourceType();
        Mockito.doReturn(OperationType.Create).when(request).getOperationType();
        Mockito.doReturn("1-MxAPlgMgA=").when(request).getResourceId();
        request.authorizationTokenType = AuthorizationTokenType.PrimaryMasterKey;
        return request;
    }

    private StoreResponse setupStoreResponse(boolean nRegionCommit) {
        StoreResponse storeResponse = Mockito.mock(StoreResponse.class);
        Mockito.doReturn(1L).when(storeResponse).getNumberOfReadRegions();
        Mockito.doReturn("1").when(storeResponse).getHeaderValue(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS);
        if (nRegionCommit) {
            Mockito.doReturn("1").when(storeResponse).getHeaderValue(WFConstants.BackendHeaders.GLOBAL_N_REGION_COMMITTED_GLSN);
        } else {
            Mockito.doReturn("1").when(storeResponse).getHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN);
        }
        Mockito.doReturn("2").when(storeResponse).getHeaderValue(WFConstants.BackendHeaders.LSN);
        return storeResponse;
    }

    private List<AddressInformation> setupAddressList() {
        AddressInformation addressInformation = Mockito.mock(AddressInformation.class);
        Uri primaryUri = Mockito.mock(Uri.class);
        Mockito.doReturn(true).when(primaryUri).isPrimary();
        Mockito.doReturn("Healthy").when(primaryUri).getHealthStatusDiagnosticString();
        Mockito.doReturn(primaryUri).when(addressInformation).getPhysicalUri();
        return Collections.singletonList(addressInformation);
    }

    private StoreReader setupStoreReader(List<StoreResult> storeResults) {
        StoreReader storeReader = Mockito.mock(StoreReader.class);
        Mono<List<StoreResult>>[] monos = storeResults.stream()
            .map(Collections::singletonList)
            .map(Mono::just)
            .toArray(Mono[]::new);
        Mockito.when(storeReader.readMultipleReplicaAsync(
                Mockito.any(),
                Mockito.anyBoolean(),
                Mockito.anyInt(),
                Mockito.anyBoolean(),
                Mockito.anyBoolean(),
                Mockito.any(),
                Mockito.anyBoolean(),
                Mockito.anyBoolean()))
            .thenReturn(monos.length > 0 ? monos[0] : Mono.empty(),
                Arrays.copyOfRange(monos, 1, monos.length));
        return storeReader;
    }

    private StoreResult getStoreResult(StoreResponse storeResponse, long globalCommittedLsn) {
        return new StoreResult(
            storeResponse,
            null,
            "1",
            1,
            1,
            1.0,
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            4,
            2,
            true,
            null,
            globalCommittedLsn,
            1,
            1,
            null,
            0.3,
            90.0);
    }




    private void initializeConsistencyWriter(boolean useMultipleWriteLocation) {
        addressSelector = Mockito.mock(AddressSelector.class);
        sessionContainer = Mockito.mock(ISessionContainer.class);
        transportClient = Mockito.mock(TransportClient.class);
        IAuthorizationTokenProvider authorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        serviceConfigReader = Mockito.mock(GatewayServiceConfigurationReader.class);

        consistencyWriter = new ConsistencyWriter(clientContext,
                addressSelector,
                sessionContainer,
                transportClient,
                authorizationTokenProvider,
                serviceConfigReader,
                useMultipleWriteLocation,
                null);
    }

    private void initializeConsistencyWriterWithStoreReader(boolean useMultipleWriteLocation, StoreReader reader) {
        addressSelector = Mockito.mock(AddressSelector.class);
        sessionContainer = Mockito.mock(ISessionContainer.class);
        transportClient = Mockito.mock(TransportClient.class);
        IAuthorizationTokenProvider authorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        serviceConfigReader = Mockito.mock(GatewayServiceConfigurationReader.class);

        consistencyWriter = new ConsistencyWriter(clientContext,
            addressSelector,
            sessionContainer,
            transportClient,
            authorizationTokenProvider,
            serviceConfigReader,
            useMultipleWriteLocation,
            reader,
            null);
    }

    public static <T> void validateError(Mono<T> single,
                                         FailureValidator validator) {
        TestSubscriber<T> testSubscriber = TestSubscriber.create();

        try {
            single.flux().subscribe(testSubscriber);
        } catch (Throwable throwable) {
            assertThat(throwable).isInstanceOf(Error.class);
            validator.validate(throwable);
        }
    }

    // TODO: add more mocking unit tests for Global STRONG (mocking unit tests)
    // TODO: add more tests for SESSION behaviour (mocking unit tests)
    // TODO: add more tests for error handling behaviour (mocking unit tests)
    // TODO: add tests for replica catch up (request barrier while loop) (mocking unit tests)
    // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/320977
}

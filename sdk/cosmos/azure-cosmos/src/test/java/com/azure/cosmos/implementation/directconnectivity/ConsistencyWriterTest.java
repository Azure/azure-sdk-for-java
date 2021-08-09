// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.ISessionContainer;
import com.azure.cosmos.implementation.PartitionIsMigratingException;
import com.azure.cosmos.implementation.PartitionKeyRangeGoneException;
import com.azure.cosmos.implementation.PartitionKeyRangeIsSplittingException;
import com.azure.cosmos.implementation.RequestTimeoutException;
import com.azure.cosmos.implementation.RetryContext;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.StoreResponseBuilder;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import io.reactivex.subscribers.TestSubscriber;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Mono;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import static com.azure.cosmos.implementation.HttpConstants.StatusCodes.GONE;
import static com.azure.cosmos.implementation.HttpConstants.SubStatusCodes.COMPLETING_PARTITION_MIGRATION;
import static com.azure.cosmos.implementation.HttpConstants.SubStatusCodes.COMPLETING_SPLIT;
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
        sessionContainer = Mockito.mock(ISessionContainer.class);
        IAuthorizationTokenProvider authorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        serviceConfigReader = Mockito.mock(GatewayServiceConfigurationReader.class);

        consistencyWriter = new ConsistencyWriter(clientContext,
                addressSelectorWrapper.addressSelector,
                sessionContainer,
                transportClientWrapper.transportClient,
                authorizationTokenProvider,
                serviceConfigReader,
                false);

        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        RxDocumentServiceRequest dsr = mockDocumentServiceRequest(clientContext);

        Mono<StoreResponse> res = consistencyWriter.writeAsync(dsr, timeoutHelper, false);

        FailureValidator failureValidator = FailureValidator.builder()
                .instanceOf(klass)
                .statusCode(expectedStatusCode)
                .subStatusCode(expectedSubStatusCode)
                .build();

        TestSubscriber<StoreResponse> subscriber = new TestSubscriber<>();
        res.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNotComplete();
        assertThat(subscriber.errorCount()).isEqualTo(1);
        failureValidator.validate(subscriber.errors().get(0));
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
        ImmutableList.Builder<Map.Entry<String, String>> builder = new ImmutableList.Builder<>();
        builder.add(new AbstractMap.SimpleEntry<>(WFConstants.BackendHeaders.LSN, "3"));
        builder.add(new AbstractMap.SimpleEntry<>(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, "2"));
        ImmutableList<Map.Entry<String, String>> headers = builder.build();

        StoreResponse sr = new StoreResponse(0, headers, null);
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
        TestSubscriber<StoreResponse> subscriber = new TestSubscriber<>();
        RxDocumentServiceRequest request = mockDocumentServiceRequest(clientContext);
        ClientSideRequestStatistics clientSideRequestStatistics = BridgeInternal.getClientSideRequestStatics(request.requestContext.cosmosDiagnostics);
        RetryContext retryContext = Mockito.mock(RetryContext.class);
        ReflectionUtils.setRetryContext(clientSideRequestStatistics, retryContext);
        Mockito.doReturn(2).when(retryContext).getRetryCount();

        spyConsistencyWriter.writeAsync(request, timeoutHelper, false)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent(10, TimeUnit.MILLISECONDS);
        subscriber.assertNoValues();

        subscriber.assertError(RequestTimeoutException.class);
    }

    @Test(groups = "unit")
    public void timeout2() throws Exception {
        initializeConsistencyWriter(false);
        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        Mockito.doReturn(false).doReturn(true).when(timeoutHelper).isElapsed();
        ConsistencyWriter spyConsistencyWriter = Mockito.spy(this.consistencyWriter);
        TestSubscriber<StoreResponse> subscriber = new TestSubscriber<>();
        RxDocumentServiceRequest request = mockDocumentServiceRequest(clientContext);
        ClientSideRequestStatistics clientSideRequestStatistics = BridgeInternal.getClientSideRequestStatics(request.requestContext.cosmosDiagnostics);
        RetryContext retryContext = Mockito.mock(RetryContext.class);
        ReflectionUtils.setRetryContext(clientSideRequestStatistics, retryContext);
        Mockito.doReturn(2).when(retryContext).getRetryCount();

        spyConsistencyWriter.writeAsync(request, timeoutHelper, false)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent(10, TimeUnit.MILLISECONDS);
        subscriber.assertError(RequestTimeoutException.class);
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
            false);

        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        RxDocumentServiceRequest dsr = mockDocumentServiceRequest(clientContext);

        consistencyWriter.writeAsync(dsr, timeoutHelper, false).subscribe();

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
                useMultipleWriteLocation);
    }

    // TODO: add more mocking unit tests for Global STRONG (mocking unit tests)
    // TODO: add more tests for SESSION behaviour (mocking unit tests)
    // TODO: add more tests for error handling behaviour (mocking unit tests)
    // TODO: add tests for replica catch up (request barrier while loop) (mocking unit tests)
    // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/320977
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.internal.ISessionContainer;
import com.azure.data.cosmos.PartitionKeyRangeGoneException;
import com.azure.data.cosmos.RequestTimeoutException;
import com.azure.data.cosmos.internal.*;
import com.azure.data.cosmos.PartitionIsMigratingException;
import com.azure.data.cosmos.PartitionKeyRangeIsSplittingException;
import com.google.common.collect.ImmutableList;
import io.reactivex.subscribers.TestSubscriber;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import static com.azure.data.cosmos.internal.HttpConstants.StatusCodes.GONE;
import static com.azure.data.cosmos.internal.HttpConstants.SubStatusCodes.COMPLETING_PARTITION_MIGRATION;
import static com.azure.data.cosmos.internal.HttpConstants.SubStatusCodes.COMPLETING_SPLIT;
import static com.azure.data.cosmos.internal.HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE;
import static org.assertj.core.api.Assertions.assertThat;

public class ConsistencyWriterTest {

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

    @Test(groups = "unit", dataProvider = "exceptionArgProvider")
    public void exception(Exception ex, Class<Exception> klass, int expectedStatusCode, Integer expectedSubStatusCode) {
        TransportClientWrapper transportClientWrapper = new TransportClientWrapper.Builder.ReplicaResponseBuilder
                .SequentialBuilder()
                .then(ex)
                .build();

        URI primaryUri = URI.create("primary");
        URI secondaryUri1 = URI.create("secondary1");
        URI secondaryUri2 = URI.create("secondary2");
        URI secondaryUri3 = URI.create("secondary3");

        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create()
                .withPrimary(primaryUri)
                .withSecondary(ImmutableList.of(secondaryUri1, secondaryUri2, secondaryUri3))
                .build();
        sessionContainer = Mockito.mock(ISessionContainer.class);
        IAuthorizationTokenProvider authorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        serviceConfigReader = Mockito.mock(GatewayServiceConfigurationReader.class);

        consistencyWriter = new ConsistencyWriter(
                addressSelectorWrapper.addressSelector,
                sessionContainer,
                transportClientWrapper.transportClient,
                authorizationTokenProvider,
                serviceConfigReader,
                false);

        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        RxDocumentServiceRequest dsr = Mockito.mock(RxDocumentServiceRequest.class);
        dsr.requestContext = Mockito.mock(DocumentServiceRequestContext.class);

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
        DirectProcessor<URI> directProcessor = DirectProcessor.create();
        CountDownLatch c = new CountDownLatch(1);

        URI uri = URI.create("https://localhost:5050");

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
        RxDocumentServiceRequest request = Mockito.mock(RxDocumentServiceRequest.class);
        consistencyWriter.startBackgroundAddressRefresh(request);

        directProcessor.onNext(uri);
        directProcessor.onComplete();

        TimeUnit.MILLISECONDS.sleep(1000);
        assertThat(c.getCount()).isEqualTo(0);
        assertThat(b.getNumberWaiting()).isEqualTo(1);
        b.await(1000, TimeUnit.MILLISECONDS);
        assertThat(invocationOnMocks).hasSize(1);
        assertThat(invocationOnMocks.get(0).getArgumentAt(1, Boolean.class)).isTrue();
    }

    @Test(groups = "unit")
    public void getLsnAndGlobalCommittedLsn() {
        ImmutableList.Builder<Map.Entry<String, String>> builder = new ImmutableList.Builder<>();
        builder.add(new AbstractMap.SimpleEntry<>(WFConstants.BackendHeaders.LSN, "3"));
        builder.add(new AbstractMap.SimpleEntry<>(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, "2"));
        ImmutableList<Map.Entry<String, String>> headers = builder.build();

        StoreResponse sr = new StoreResponse(0, headers, (String) null);
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
        TestSubscriber<StoreResponse> subscriber = new TestSubscriber();

        spyConsistencyWriter.writeAsync(Mockito.mock(RxDocumentServiceRequest.class), timeoutHelper, false)
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
        TestSubscriber<StoreResponse> subscriber = new TestSubscriber();

        spyConsistencyWriter.writeAsync(Mockito.mock(RxDocumentServiceRequest.class), timeoutHelper, false)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent(10, TimeUnit.MILLISECONDS);
        subscriber.assertError(RequestTimeoutException.class);
    }

    @DataProvider(name = "globalStrongArgProvider")
    public Object[][] globalStrongArgProvider() {
        return new Object[][]{
                {
                        ConsistencyLevel.SESSION,
                        Mockito.mock(RxDocumentServiceRequest.class),
                        Mockito.mock(StoreResponse.class),

                        false,
                },
                {
                        ConsistencyLevel.EVENTUAL,
                        Mockito.mock(RxDocumentServiceRequest.class),
                        Mockito.mock(StoreResponse.class),

                        false,
                },
                {

                        ConsistencyLevel.EVENTUAL,
                        Mockito.mock(RxDocumentServiceRequest.class),
                        StoreResponseBuilder.create()
                                .withHeader(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS, Integer.toString(5))
                                .build(),
                        false,
                },
                {

                        ConsistencyLevel.STRONG,
                        Mockito.mock(RxDocumentServiceRequest.class),
                        StoreResponseBuilder.create()
                                .withHeader(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS, Integer.toString(5))
                                .build(),
                        true,
                },
                {

                        ConsistencyLevel.STRONG,
                        Mockito.mock(RxDocumentServiceRequest.class),
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

        consistencyWriter = new ConsistencyWriter(
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

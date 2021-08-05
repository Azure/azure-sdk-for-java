// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DocumentServiceRequestContext;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RetryWithException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.SessionContainer;
import com.azure.cosmos.implementation.StoreResponseBuilder;
import com.azure.cosmos.implementation.http.HttpHeaders;
import io.reactivex.subscribers.TestSubscriber;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import static org.assertj.core.api.Assertions.assertThat;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;

public class ReplicatedResourceClientRetryWithTest {
    protected static final int TIMEOUT = 120000;

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void retryWith_RetrySucceeds() throws URISyntaxException {
        Uri primaryAddress = Uri.create("http://primary/");
        List<Uri> secondaryAddresses = new ArrayList<>();
        secondaryAddresses.add(Uri.create("http://secondary-1/"));
        secondaryAddresses.add(Uri.create("http://secondary-2/"));
        secondaryAddresses.add(Uri.create("http://secondary-3/"));

        String partitionKeyRangeId = "1";

        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper
            .Builder
            .Simple
            .create()
            .withPrimary(primaryAddress)
            .withSecondary(secondaryAddresses)
            .build();

        long lsn = 54;
        long localLsn = 18;

        StoreResponse primaryResponse = StoreResponseBuilder
            .create()
            .withLSN(lsn)
            .withLocalLSN(localLsn)
            .withHeader(WFConstants.BackendHeaders.QUORUM_ACKED_LOCAL_LSN, Long.toString(localLsn))
            .withHeader(WFConstants.BackendHeaders.CURRENT_REPLICA_SET_SIZE, partitionKeyRangeId)
            .withRequestCharge(5)
            .build();

        TransportClientWrapper.Builder.UriToResultBuilder transportClientWrapperBuilder =
            TransportClientWrapper.Builder.uriToResultBuilder();

        transportClientWrapperBuilder
            .exceptionOn(
                primaryAddress,
                OperationType.Create,
                ResourceType.Document,
                new RetryWithException("Simultated 449 conflict", new HttpHeaders(), new URI("http://localhost")),
                false)
            .exceptionOn(
                primaryAddress,
                OperationType.Create,
                ResourceType.Document,
                new RetryWithException("Simultated 449 conflict", new HttpHeaders(), new URI("http://localhost")),
                false)
            .exceptionOn(
                primaryAddress,
                OperationType.Create,
                ResourceType.Document,
                new RetryWithException("Simultated 449 conflict", new HttpHeaders(), new URI("http://localhost")),
                false)
            .storeResponseOn(
                primaryAddress,
                OperationType.Create,
                ResourceType.Document,
                primaryResponse,
                true);

        TransportClientWrapper transportClientWrapper = transportClientWrapperBuilder.build();

        GatewayServiceConfiguratorReaderMock gatewayServiceConfigurationReaderWrapper =
            GatewayServiceConfiguratorReaderMock.from(ConsistencyLevel.STRONG,
            4,
            3,
            4,
            3);

        SessionContainer sessionContainer = new SessionContainer("test");

        IAuthorizationTokenProvider authorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        ReplicatedResourceClient resourceClient = new ReplicatedResourceClient(mockDiagnosticsClientContext(),
            new Configs(),
            addressSelectorWrapper.addressSelector,
            sessionContainer,
            transportClientWrapper.transportClient,
            gatewayServiceConfigurationReaderWrapper.gatewayServiceConfigurationReader,
            authorizationTokenProvider,
            false,
            false);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Create, "/dbs/db/colls/col/docs", ResourceType.Document);
        request.requestContext = new DocumentServiceRequestContext();
        request.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId(partitionKeyRangeId);
        request.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.SESSION.toString());

        Function<RxDocumentServiceRequest, Mono<RxDocumentServiceRequest>> prepareRequestAsyncDelegate = null;

        Instant start = Instant.now();
        Mono<StoreResponse> storeResponseObs = resourceClient.invokeAsync(request, prepareRequestAsyncDelegate);

        StoreResponseValidator validator = StoreResponseValidator.create()
                                                                 .withBELSN(lsn)
                                                                 .withRequestCharge(5)
                                                                 .build();
        validateSuccess(storeResponseObs, validator);

        Instant end = Instant.now();

        Duration elapsedTime = Duration.between(start, end);

        // Initially the retry logic for 449 was immediate initial retry then retries
        // after 1 second 2 seconds etc. Now we changed the retry policy
        // to start with 10 ms, then 20ms etc.
        // Testing that 3 retries all finish within one second - that should
        // leave enough buffer to not make the test flaky even on overloaded machines
        assertThat(elapsedTime).isLessThanOrEqualTo(Duration.ofSeconds(1));

        addressSelectorWrapper.verifyNumberOfForceCacheRefreshGreaterThanOrEqualTo(0);
    }

    public static void validateSuccess(Mono<List<StoreResult>> single,
                                       MultiStoreResultValidator validator) {
        validateSuccess(single, validator, TIMEOUT);
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

    public static void validateSuccess(Mono<StoreResponse> single,
                                       StoreResponseValidator validator) {
        validateSuccess(single, validator, TIMEOUT);
    }

    public static void validateSuccess(Mono<StoreResponse> single,
                                       StoreResponseValidator validator, long timeout) {
        TestSubscriber<StoreResponse> testSubscriber = new TestSubscriber<>();

        single.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();
        testSubscriber.assertValueCount(1);
        validator.validate(testSubscriber.values().get(0));
    }

    private PartitionKeyRange partitionKeyRangeWithId(String id) {
        PartitionKeyRange partitionKeyRange = Mockito.mock(PartitionKeyRange.class);
        Mockito.doReturn(id).when(partitionKeyRange).getId();
        return partitionKeyRange;
    }
}

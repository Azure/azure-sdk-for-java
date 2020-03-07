// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ThrottlingRetryOptions;
import io.netty.handler.timeout.ReadTimeoutException;
import io.reactivex.subscribers.TestSubscriber;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class ClientRetryPolicyTest {
    private final static int TIMEOUT = 10000;

    @Test(groups = "unit")
    public void networkFailureOnRead() throws Exception {
        ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(endpointManager, true, throttlingRetryOptions);

        Exception exception = ReadTimeoutException.INSTANCE;

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.requestContext = Mockito.mock(DocumentServiceRequestContext.class);

        clientRetryPolicy.onBeforeSendRequest(dsr);

        for (int i = 0; i < 10; i++) {
            Mono<IRetryPolicy.ShouldRetryResult> shouldRetry = clientRetryPolicy.shouldRetry(exception);

            validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                    .nullException()
                    .shouldRetry(true)
                    .backOfTime(Duration.ofMillis(ClientRetryPolicy.RetryIntervalInMS))
                    .build());

            Mockito.verify(endpointManager, Mockito.times(i + 1)).markEndpointUnavailableForRead(Mockito.any());
            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForWrite(Mockito.any());
        }
    }

    @Test(groups = "unit")
    public void networkFailureOnWrite() throws Exception {
        ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(endpointManager, true, throttlingRetryOptions);

        Exception exception = ReadTimeoutException.INSTANCE;

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(
                OperationType.Create, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.requestContext = Mockito.mock(DocumentServiceRequestContext.class);

        clientRetryPolicy.onBeforeSendRequest(dsr);
        for (int i = 0; i < 10; i++) {
            Mono<IRetryPolicy.ShouldRetryResult> shouldRetry = clientRetryPolicy.shouldRetry(exception);
            validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                    .nullException()
                    .shouldRetry(false)
                    .build());

            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForRead(Mockito.any());
            Mockito.verify(endpointManager, Mockito.times(i + 1)).markEndpointUnavailableForWrite(Mockito.any());
        }
    }

    @Test(groups = "unit")
    public void networkFailureOnUpsert() throws Exception {
        ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(endpointManager, true, throttlingRetryOptions);

        Exception exception = ReadTimeoutException.INSTANCE;

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(
            OperationType.Upsert, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.requestContext = Mockito.mock(DocumentServiceRequestContext.class);

        clientRetryPolicy.onBeforeSendRequest(dsr);
        for (int i = 0; i < 10; i++) {
            Mono<IRetryPolicy.ShouldRetryResult> shouldRetry = clientRetryPolicy.shouldRetry(exception);
            validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                                                             .nullException()
                                                             .shouldRetry(false)
                                                             .build());

            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForRead(Mockito.any());
            Mockito.verify(endpointManager, Mockito.times(i + 1)).markEndpointUnavailableForWrite(Mockito.any());
        }
    }

    @Test(groups = "unit")
    public void networkFailureOnDelete() throws Exception {
        ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(endpointManager, true, throttlingRetryOptions);

        Exception exception = ReadTimeoutException.INSTANCE;

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(
            OperationType.Delete, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.requestContext = Mockito.mock(DocumentServiceRequestContext.class);

        clientRetryPolicy.onBeforeSendRequest(dsr);
        for (int i = 0; i < 10; i++) {
            Mono<IRetryPolicy.ShouldRetryResult> shouldRetry = clientRetryPolicy.shouldRetry(exception);
            validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                                                             .nullException()
                                                             .shouldRetry(false)
                                                             .build());

            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForRead(Mockito.any());
            Mockito.verify(endpointManager, Mockito.times(i + 1)).markEndpointUnavailableForWrite(Mockito.any());
        }
    }

    @Test(groups = "unit")
    public void onBeforeSendRequestNotInvoked() {
        ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);

        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(endpointManager, true, throttlingRetryOptions);

        Exception exception = ReadTimeoutException.INSTANCE;

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(
                OperationType.Create, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.requestContext = Mockito.mock(DocumentServiceRequestContext.class);

        Mono<IRetryPolicy.ShouldRetryResult> shouldRetry = clientRetryPolicy.shouldRetry(exception);
        validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                .withException(exception)
                .shouldRetry(false)
                .build());

        Mockito.verifyZeroInteractions(endpointManager);
    }

    public static void validateSuccess(Mono<IRetryPolicy.ShouldRetryResult> single,
                                       ShouldRetryValidator validator) {

        validateSuccess(single, validator, TIMEOUT);
    }

    public static void validateSuccess(Mono<IRetryPolicy.ShouldRetryResult> single,
                                       ShouldRetryValidator validator,
                                       long timeout) {
        TestSubscriber<IRetryPolicy.ShouldRetryResult> testSubscriber = new TestSubscriber<>();

        single.flux().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
        validator.validate(testSubscriber.values().get(0));
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.ThrottlingRetryOptions;
import io.netty.handler.timeout.ReadTimeoutException;
import io.reactivex.subscribers.TestSubscriber;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import javax.net.ssl.SSLHandshakeException;
import java.net.SocketException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;

public class ClientRetryPolicyTest {
    private final static int TIMEOUT = 10000;

    @Test(groups = "unit")
    public void networkFailureOnRead() throws Exception {
        ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(mockDiagnosticsClientContext(), endpointManager, true, throttlingRetryOptions, null);

        Exception exception = new SocketException("Dummy SocketException");
        CosmosException cosmosException = BridgeInternal.createCosmosException(null, HttpConstants.StatusCodes.SERVICE_UNAVAILABLE, exception);
        BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.requestContext = new DocumentServiceRequestContext();

        clientRetryPolicy.onBeforeSendRequest(dsr);

        for (int i = 0; i < 10; i++) {
            Mono<ShouldRetryResult> shouldRetry = clientRetryPolicy.shouldRetry(cosmosException);

            validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                    .nullException()
                    .shouldRetry(true)
                    .backOfTime(Duration.ofMillis(ClientRetryPolicy.RetryIntervalInMS))
                    .build());

            Mockito.verify(endpointManager, Mockito.times(i + 1)).markEndpointUnavailableForRead(Mockito.any());
            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForWrite(Mockito.any());

            Assert.assertTrue(clientRetryPolicy.canUsePreferredLocations());
        }
    }

    @Test(groups = "unit")
    public void shouldRetryOnGatewayTimeout() throws Exception {
        ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(true));
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(mockDiagnosticsClientContext(), endpointManager, true, throttlingRetryOptions, null);

        Exception exception = ReadTimeoutException.INSTANCE;
        CosmosException cosmosException = BridgeInternal.createCosmosException(null, HttpConstants.StatusCodes.REQUEST_TIMEOUT, exception);
        BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT);

        RxDocumentServiceRequest dsr;
        Mono<ShouldRetryResult> shouldRetry;

        //Data Plane Read
        dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Read, "/dbs/db/colls/col/docs/doc", ResourceType.Document);

        clientRetryPolicy.onBeforeSendRequest(dsr);
        shouldRetry = clientRetryPolicy.shouldRetry(cosmosException);

        validateSuccess(shouldRetry, ShouldRetryValidator.builder()
            .nullException()
            .shouldRetry(true)
            .backOfTime(Duration.ofMillis(1000))
            .build());

        //Metadata Read
        dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Read, "/dbs/db/clls/col/docs/doc", ResourceType.Database);

        clientRetryPolicy.onBeforeSendRequest(dsr);

        shouldRetry = clientRetryPolicy.shouldRetry(cosmosException);

        validateSuccess(shouldRetry, ShouldRetryValidator.builder()
            .nullException()
            .shouldRetry(true)
            .backOfTime(Duration.ofMillis(1000))
            .build());

        //Query Plan
        dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.QueryPlan, "/dbs/db/colls/col/docs/doc", ResourceType.Document);

        clientRetryPolicy.onBeforeSendRequest(dsr);

        shouldRetry = clientRetryPolicy.shouldRetry(cosmosException);

        validateSuccess(shouldRetry, ShouldRetryValidator.builder()
            .nullException()
            .shouldRetry(true)
            .backOfTime(Duration.ofMillis(1000))
            .build());

        //Data Plane Write - Should not retry
        dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Create, "/dbs/db/colls/col/docs/doc", ResourceType.Document);

        clientRetryPolicy.onBeforeSendRequest(dsr);
        shouldRetry = clientRetryPolicy.shouldRetry(cosmosException);

        validateSuccess(shouldRetry, ShouldRetryValidator.builder()
            .nullException()
            .shouldRetry(false)
            .build());

        //Metadata Write - Should not Retry
        dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Create, "/dbs/db/colls/col/docs/docId", ResourceType.Database);

        clientRetryPolicy.onBeforeSendRequest(dsr);

        shouldRetry = clientRetryPolicy.shouldRetry(cosmosException);

        validateSuccess(shouldRetry, ShouldRetryValidator.builder()
            .nullException()
            .shouldRetry(false)
            .build());
    }

    @Test(groups = "unit")
    public void tcpNetworkFailureOnRead() throws Exception {
        ThrottlingRetryOptions retryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        Mockito.doReturn(2).when(endpointManager).getPreferredLocationCount();
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(mockDiagnosticsClientContext(), endpointManager, true, retryOptions, null);

        Exception exception = ReadTimeoutException.INSTANCE;
        GoneException goneException = new GoneException(exception);
        CosmosException cosmosException =
            BridgeInternal.createCosmosException(null, HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                goneException);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.requestContext = new DocumentServiceRequestContext();

        clientRetryPolicy.onBeforeSendRequest(dsr);

        for (int i = 0; i < 10; i++) {
            Mono<ShouldRetryResult> shouldRetry = clientRetryPolicy.shouldRetry(cosmosException);

            if (i < 2) {
                validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                    .nullException()
                    .shouldRetry(true)
                    .backOfTime(Duration.ofMillis(0))
                    .build());

                Assert.assertTrue(clientRetryPolicy.canUsePreferredLocations());
            } else {
                validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                    .nullException()
                    .shouldRetry(false)
                    .build());

                Assert.assertFalse(clientRetryPolicy.canUsePreferredLocations());
            }

            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForRead(Mockito.any());
            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForWrite(Mockito.any());
        }
    }

    @Test(groups = "unit")
    public void networkFailureOnWrite() throws Exception {
        ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(mockDiagnosticsClientContext(), endpointManager, true, throttlingRetryOptions, null);

        Exception exception = new SocketException("Dummy SocketException");;
        CosmosException cosmosException = BridgeInternal.createCosmosException(null, HttpConstants.StatusCodes.SERVICE_UNAVAILABLE, exception);
        BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Create, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.requestContext = new DocumentServiceRequestContext();

        clientRetryPolicy.onBeforeSendRequest(dsr);
        for (int i = 0; i < 10; i++) {
            Mono<ShouldRetryResult> shouldRetry = clientRetryPolicy.shouldRetry(cosmosException);
            validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                    .nullException()
                    .shouldRetry(false)
                    .build());

            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForRead(Mockito.any());
            Mockito.verify(endpointManager, Mockito.times(i + 1)).markEndpointUnavailableForWrite(Mockito.any());
        }
    }

    @Test(groups = "unit")
    public void tcpNetworkFailureOnWrite() throws Exception {
        ThrottlingRetryOptions retryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        Mockito.doReturn(2).when(endpointManager).getPreferredLocationCount();
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(mockDiagnosticsClientContext(), endpointManager, true, retryOptions, null);

        //Non retribale exception for write
        Exception exception = new SocketException("Dummy SocketException");;
        GoneException goneException = new GoneException(exception);
        CosmosException cosmosException = BridgeInternal.createCosmosException(null, HttpConstants.StatusCodes.SERVICE_UNAVAILABLE, goneException);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Create, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.requestContext = new DocumentServiceRequestContext();

        clientRetryPolicy.onBeforeSendRequest(dsr);

        for (int i = 0; i < 10; i++) {
            Mono<ShouldRetryResult> shouldRetry = clientRetryPolicy.shouldRetry(cosmosException);
            //  We don't want to retry writes on network failure with non retriable exception
            validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                .nullException()
                .shouldRetry(false)
                .build());

            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForRead(Mockito.any());
            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForWrite(Mockito.any());
        }

        // Retriable exception scenario
        exception = new SSLHandshakeException("test");
        goneException = new GoneException(exception);
        cosmosException = BridgeInternal.createCosmosException(null, HttpConstants.StatusCodes.SERVICE_UNAVAILABLE, goneException);

        Mockito.doReturn(true).when(endpointManager).canUseMultipleWriteLocations(Mockito.any(RxDocumentServiceRequest.class));
        clientRetryPolicy = new ClientRetryPolicy(mockDiagnosticsClientContext(), endpointManager, true, retryOptions, null);
        clientRetryPolicy.onBeforeSendRequest(dsr);

        for (int i = 0; i < 10; i++) {
            Mono<ShouldRetryResult> shouldRetry = clientRetryPolicy.shouldRetry(cosmosException);
            //  We want to retry writes on network failure with retriable exception
            if (i < 2) {
                validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                    .nullException()
                    .shouldRetry(true)
                    .backOfTime(Duration.ofMillis(0))
                    .build());

                Assert.assertTrue(clientRetryPolicy.canUsePreferredLocations());
            } else {
                validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                    .nullException()
                    .shouldRetry(false)
                    .build());

                Assert.assertFalse(clientRetryPolicy.canUsePreferredLocations());
            }

            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForRead(Mockito.any());
            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForWrite(Mockito.any());
        }
    }

    @Test(groups = "unit")
    public void networkFailureOnUpsert() throws Exception {
        ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(mockDiagnosticsClientContext(), endpointManager, true, throttlingRetryOptions, null);

        Exception exception = new SocketException("Dummy SocketException");
        CosmosException cosmosException = BridgeInternal.createCosmosException(null, HttpConstants.StatusCodes.SERVICE_UNAVAILABLE, exception);
        BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Upsert, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.requestContext = new DocumentServiceRequestContext();

        clientRetryPolicy.onBeforeSendRequest(dsr);
        for (int i = 0; i < 10; i++) {
            Mono<ShouldRetryResult> shouldRetry = clientRetryPolicy.shouldRetry(cosmosException);
            validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                                                             .nullException()
                                                             .shouldRetry(false)
                                                             .build());

            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForRead(Mockito.any());
            Mockito.verify(endpointManager, Mockito.times(i + 1)).markEndpointUnavailableForWrite(Mockito.any());

            Assert.assertFalse(clientRetryPolicy.canUsePreferredLocations());
        }
    }

    @Test(groups = "unit")
    public void tcpNetworkFailureOnUpsert() throws Exception {
        ThrottlingRetryOptions retryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        Mockito.doReturn(2).when(endpointManager).getPreferredLocationCount();
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(mockDiagnosticsClientContext(), endpointManager, true, retryOptions, null);

        Exception exception = new SocketException("Dummy SocketException");
        GoneException goneException = new GoneException(exception);
        CosmosException cosmosException = BridgeInternal.createCosmosException(null, HttpConstants.StatusCodes.SERVICE_UNAVAILABLE, goneException);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Upsert, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.requestContext = new DocumentServiceRequestContext();

        clientRetryPolicy.onBeforeSendRequest(dsr);

        for (int i = 0; i < 10; i++) {
            Mono<ShouldRetryResult> shouldRetry = clientRetryPolicy.shouldRetry(cosmosException);
            //  We don't want to retry writes on network failure with non retriable exception
            validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                .nullException()
                .shouldRetry(false)
                .build());

            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForRead(Mockito.any());
            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForWrite(Mockito.any());

            Assert.assertFalse(clientRetryPolicy.canUsePreferredLocations());
        }
    }

    @Test(groups = "unit")
    public void networkFailureOnDelete() throws Exception {
        ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(mockDiagnosticsClientContext(), endpointManager, true, throttlingRetryOptions, null);

        Exception exception = ReadTimeoutException.INSTANCE;
        CosmosException cosmosException = BridgeInternal.createCosmosException(
            null, HttpConstants.StatusCodes.SERVICE_UNAVAILABLE, exception);
        BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Delete, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.requestContext = new DocumentServiceRequestContext();

        clientRetryPolicy.onBeforeSendRequest(dsr);
        for (int i = 0; i < 10; i++) {
            Mono<ShouldRetryResult> shouldRetry = clientRetryPolicy.shouldRetry(cosmosException);
            validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                                                             .nullException()
                                                             .shouldRetry(false)
                                                             .build());

            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForRead(Mockito.any());
            Mockito.verify(endpointManager, Mockito.times(i + 1)).markEndpointUnavailableForWrite(Mockito.any());

            Assert.assertFalse(clientRetryPolicy.canUsePreferredLocations());
        }
    }

    @Test(groups = "unit")
    public void tcpNetworkFailureOnDelete() throws Exception {
        ThrottlingRetryOptions retryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        Mockito.doReturn(2).when(endpointManager).getPreferredLocationCount();
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(mockDiagnosticsClientContext(), endpointManager, true, retryOptions, null);

        Exception exception = ReadTimeoutException.INSTANCE;
        GoneException goneException = new GoneException(exception);
        CosmosException cosmosException = BridgeInternal.createCosmosException(null, HttpConstants.StatusCodes.SERVICE_UNAVAILABLE, goneException);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Delete, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.requestContext = new DocumentServiceRequestContext();

        clientRetryPolicy.onBeforeSendRequest(dsr);

        for (int i = 0; i < 10; i++) {
            Mono<ShouldRetryResult> shouldRetry = clientRetryPolicy.shouldRetry(cosmosException);
            //  We don't want to retry writes on network failure with non retriable exception
            validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                .nullException()
                .shouldRetry(false)
                .build());

            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForRead(Mockito.any());
            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForWrite(Mockito.any());

            Assert.assertFalse(clientRetryPolicy.canUsePreferredLocations());
        }
    }

    @Test(groups = "unit")
    public void httpNetworkFailureOnQueryPlan() throws Exception {
        ThrottlingRetryOptions retryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(true));
        Mockito.doReturn(2).when(endpointManager).getPreferredLocationCount();
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(mockDiagnosticsClientContext(), endpointManager, true, retryOptions, null);

        Exception exception = ReadTimeoutException.INSTANCE;
        CosmosException cosmosException =
            BridgeInternal.createCosmosException(null, HttpConstants.StatusCodes.REQUEST_TIMEOUT, exception);
        BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.QueryPlan, "/dbs/db/colls/col/docs/", ResourceType.Document);
        dsr.requestContext = new DocumentServiceRequestContext();

        clientRetryPolicy.onBeforeSendRequest(dsr);

        for (int i = 0; i < 10; i++) {
            Mono<ShouldRetryResult> shouldRetry = clientRetryPolicy.shouldRetry(cosmosException);

            validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                .nullException()
                .shouldRetry(true)
                .backOfTime(Duration.ofMillis(1000))
                .build());

            Mockito.verify(endpointManager, Mockito.times(i+1)).markEndpointUnavailableForRead(Mockito.any());
            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForWrite(Mockito.any());
        }
    }

    @Test(groups = "unit")
    public void httpNetworkFailureOnAddressRefresh() throws Exception {
        ThrottlingRetryOptions retryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        Mockito.doReturn(2).when(endpointManager).getPreferredLocationCount();
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(mockDiagnosticsClientContext(), endpointManager, true, retryOptions, null);

        Exception exception = ReadTimeoutException.INSTANCE;
        CosmosException cosmosException =
            BridgeInternal.createCosmosException(null, HttpConstants.StatusCodes.REQUEST_TIMEOUT, exception);
        BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Read, "/dbs/db/colls/col/docs/", ResourceType.Document);
        dsr.setAddressRefresh(true, false);
        dsr.requestContext = new DocumentServiceRequestContext();

        clientRetryPolicy.onBeforeSendRequest(dsr);

        for (int i = 0; i < 10; i++) {
            Mono<ShouldRetryResult> shouldRetry = clientRetryPolicy.shouldRetry(cosmosException);

            if (i < 3) {
                validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                                                 .nullException()
                                                 .shouldRetry(true)
                                                 .backOfTime(Duration.ofMillis(0))
                                                 .build());
            } else {
                validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                                                 .nullException()
                                                 .shouldRetry(false)
                                                 .build());
            }

            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForRead(Mockito.any());
            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForWrite(Mockito.any());
        }
    }


    @Test(groups = "unit")
    public void onBeforeSendRequestNotInvoked() {
        ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);

        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(mockDiagnosticsClientContext(), endpointManager, true, throttlingRetryOptions, null);

        Exception exception = ReadTimeoutException.INSTANCE;

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Create, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.requestContext = new DocumentServiceRequestContext();

        Mono<ShouldRetryResult> shouldRetry = clientRetryPolicy.shouldRetry(exception);
        validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                .withException(exception)
                .shouldRetry(false)
                .build());

        Mockito.verifyNoInteractions(endpointManager);
    }

    public static void validateSuccess(Mono<ShouldRetryResult> single,
                                       ShouldRetryValidator validator) {

        validateSuccess(single, validator, TIMEOUT);
    }

    public static void validateSuccess(Mono<ShouldRetryResult> single,
                                       ShouldRetryValidator validator,
                                       long timeout) {
        TestSubscriber<ShouldRetryResult> testSubscriber = new TestSubscriber<>();

        single.flux().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
        validator.validate(testSubscriber.values().get(0));
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.circuitBreaker.GlobalPartitionEndpointManagerForCircuitBreaker;
import com.azure.cosmos.implementation.directconnectivity.ChannelAcquisitionException;
import com.azure.cosmos.implementation.perPartitionAutomaticFailover.GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover;
import io.netty.handler.timeout.ReadTimeoutException;
import io.reactivex.subscribers.TestSubscriber;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
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
    private final static String TEST_DOCUMENT_PATH = "/dbs/db/colls/col/docs/doc";
    private final static String TEST_DATABASE_PATH = "/dbs/db";

    @DataProvider(name = "operationProvider")
    public static Object[][] operationProvider() {
        return new Object[][]{
            // OperationType, ResourceType, isAddressRequest, RequestFullPath, ShouldRetryCrossRegion
            { OperationType.Read, ResourceType.Document, Boolean.FALSE, TEST_DOCUMENT_PATH, Boolean.TRUE },
            { OperationType.Read, ResourceType.Document, Boolean.TRUE, TEST_DOCUMENT_PATH, Boolean.FALSE },
            { OperationType.Create, ResourceType.Document, Boolean.FALSE, TEST_DOCUMENT_PATH, Boolean.FALSE },
            { OperationType.Read, ResourceType.Database, Boolean.FALSE, TEST_DATABASE_PATH, Boolean.TRUE },
            { OperationType.Create, ResourceType.Database, Boolean.FALSE, TEST_DATABASE_PATH, Boolean.FALSE },
            { OperationType.QueryPlan, ResourceType.Document, Boolean.FALSE, TEST_DOCUMENT_PATH, Boolean.TRUE }
        };
    }

    @DataProvider(name = "tcpNetworkFailureOnWriteArgProvider")
    public static Object[][] tcpNetworkFailureOnWriteArgProvider() {
        return new Object[][]{
            // internal exception, canUseMultipleWriteLocations, nonIdempotentWriteRetriesEnabled, shouldRetry
            { new SocketException("Dummy socket exception"), false, true, false },
            { new SSLHandshakeException("test"), false, true, false },
            { new ChannelAcquisitionException("test channel acquisition failed"), false, true, false },

            // when canUseMultipleWriteLocations
            { new SocketException("Dummy socket exception"), true, false, false },
            { new SSLHandshakeException("test"), true, false, true },
            { new ChannelAcquisitionException("test channel acquisition failed"), true, false, true },
            { new SocketException("Dummy socket exception"), true, true, true },
            { new SSLHandshakeException("test"), true, true, true },
            { new ChannelAcquisitionException("test channel acquisition failed"), true, true, true }
        };
    }

    @Test(groups = "unit")
    public void networkFailureOnRead() throws Exception {
        ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManagerForCircuitBreaker
            = Mockito.mock(GlobalPartitionEndpointManagerForCircuitBreaker.class);
        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover
            = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class);

        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(
            mockDiagnosticsClientContext(),
            endpointManager,
            true,
            throttlingRetryOptions,
            null,
            globalPartitionEndpointManagerForCircuitBreaker,
            globalPartitionEndpointManagerForPerPartitionAutomaticFailover);

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
                    .backOffTime(Duration.ofMillis(ClientRetryPolicy.RetryIntervalInMS))
                    .build());

            Mockito.verify(endpointManager, Mockito.times(i + 1)).markEndpointUnavailableForRead(Mockito.any());
            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForWrite(Mockito.any());

            Assert.assertTrue(clientRetryPolicy.canUsePreferredLocations());
        }
    }

    @Test(groups = { "unit" }, dataProvider = "operationProvider")
    public void shouldRetryOnGatewayTimeout(
        OperationType operationType,
        ResourceType resourceType,
        boolean isAddressRefresh,
        String resourceFullPath,
        boolean shouldCrossRegionRetry) throws Exception {
        ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManagerForCircuitBreaker
            = Mockito.mock(GlobalPartitionEndpointManagerForCircuitBreaker.class);
        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover
            = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class);

        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(true));
        ClientRetryPolicy clientRetryPolicy =
            new ClientRetryPolicy(
                mockDiagnosticsClientContext(),
                endpointManager,
                true,
                throttlingRetryOptions,
                null,
                globalPartitionEndpointManagerForCircuitBreaker,
                globalPartitionEndpointManagerForPerPartitionAutomaticFailover);

        Exception exception = ReadTimeoutException.INSTANCE;
        CosmosException cosmosException = BridgeInternal.createCosmosException(null, HttpConstants.StatusCodes.REQUEST_TIMEOUT, exception);
        BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT);

        RxDocumentServiceRequest dsr;
        Mono<ShouldRetryResult> shouldRetry;

        dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(), operationType, resourceFullPath, resourceType);
        dsr.setAddressRefresh(isAddressRefresh, false);

        clientRetryPolicy.onBeforeSendRequest(dsr);
        shouldRetry = clientRetryPolicy.shouldRetry(cosmosException);
        if (shouldCrossRegionRetry) {
            validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                .nullException()
                .shouldRetry(true)
                .backOffTime(Duration.ofMillis(1000))
                .build());
        } else {
            validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                .nullException()
                .shouldRetry(false)
                .build());
        }
    }

    @Test(groups = "unit")
    public void tcpNetworkFailureOnRead() throws Exception {
        ThrottlingRetryOptions retryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManagerForCircuitBreaker
            = Mockito.mock(GlobalPartitionEndpointManagerForCircuitBreaker.class);
        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover
            = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class);

        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        Mockito.doReturn(2).when(endpointManager).getPreferredLocationCount();
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(
            mockDiagnosticsClientContext(),
            endpointManager,
            true,
            retryOptions,
            null,
            globalPartitionEndpointManagerForCircuitBreaker,
            globalPartitionEndpointManagerForPerPartitionAutomaticFailover);

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
                    .backOffTime(Duration.ofMillis(0))
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
        GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManagerForCircuitBreaker
            = Mockito.mock(GlobalPartitionEndpointManagerForCircuitBreaker.class);
        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover
            = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class);

        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(
            mockDiagnosticsClientContext(),
            endpointManager,
            true,
            throttlingRetryOptions,
            null,
            globalPartitionEndpointManagerForCircuitBreaker,
            globalPartitionEndpointManagerForPerPartitionAutomaticFailover);

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

    @Test(groups = "unit", dataProvider = "tcpNetworkFailureOnWriteArgProvider")
    public void tcpNetworkFailureOnWrite(
        Exception exception,
        boolean canUseMultiWriteLocations,
        boolean nonIdempotentWriteRetriesEnabled,
        boolean shouldRetry) throws Exception {
        ThrottlingRetryOptions retryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManagerForCircuitBreaker
            = Mockito.mock(GlobalPartitionEndpointManagerForCircuitBreaker.class);
        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover
            = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class);

        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        Mockito.doReturn(2).when(endpointManager).getPreferredLocationCount();
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(
            mockDiagnosticsClientContext(),
            endpointManager,
            true,
            retryOptions,
            null,
            globalPartitionEndpointManagerForCircuitBreaker,
            globalPartitionEndpointManagerForPerPartitionAutomaticFailover);

        //Non retribale exception for write
        GoneException goneException = new GoneException(exception);
        CosmosException cosmosException = BridgeInternal.createCosmosException(null, HttpConstants.StatusCodes.SERVICE_UNAVAILABLE, goneException);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Create, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.requestContext = new DocumentServiceRequestContext();
        dsr.setNonIdempotentWriteRetriesEnabled(nonIdempotentWriteRetriesEnabled);
        Mockito.when(endpointManager.canUseMultipleWriteLocations(dsr)).thenReturn(canUseMultiWriteLocations);

        clientRetryPolicy.onBeforeSendRequest(dsr);
        if (shouldRetry) {
            for (int i = 0; i < 10; i++) {
                Mono<ShouldRetryResult> retryResult = clientRetryPolicy.shouldRetry(cosmosException);
                if (i < 2) {
                    validateSuccess(retryResult, ShouldRetryValidator.builder()
                        .nullException()
                        .shouldRetry(true)
                        .backOffTime(Duration.ofMillis(0))
                        .build());

                    Assert.assertTrue(clientRetryPolicy.canUsePreferredLocations());
                } else {
                    validateSuccess(retryResult, ShouldRetryValidator.builder()
                        .nullException()
                        .shouldRetry(false)
                        .build());

                    Assert.assertFalse(clientRetryPolicy.canUsePreferredLocations());
                }

                Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForRead(Mockito.any());
                Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForWrite(Mockito.any());
            }
        } else {
            for (int i = 0; i < 10; i++) {
                Mono<ShouldRetryResult> retryResult = clientRetryPolicy.shouldRetry(cosmosException);
                //  We don't want to retry writes on network failure with non retriable exception
                validateSuccess(retryResult, ShouldRetryValidator.builder()
                    .nullException()
                    .shouldRetry(false)
                    .build());

                Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForRead(Mockito.any());
                Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForWrite(Mockito.any());
            }
        }
    }

    @Test(groups = "unit")
    public void networkFailureOnUpsert() throws Exception {
        ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManagerForCircuitBreaker
            = Mockito.mock(GlobalPartitionEndpointManagerForCircuitBreaker.class);
        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover
            = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class);

        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(
            mockDiagnosticsClientContext(),
            endpointManager,
            true,
            throttlingRetryOptions,
            null,
            globalPartitionEndpointManagerForCircuitBreaker,
            globalPartitionEndpointManagerForPerPartitionAutomaticFailover);

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
        GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManagerForCircuitBreaker
            = Mockito.mock(GlobalPartitionEndpointManagerForCircuitBreaker.class);
        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover
            = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class);

        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        Mockito.doReturn(2).when(endpointManager).getPreferredLocationCount();
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(
            mockDiagnosticsClientContext(),
            endpointManager,
            true,
            retryOptions,
            null,
            globalPartitionEndpointManagerForCircuitBreaker,
            globalPartitionEndpointManagerForPerPartitionAutomaticFailover);

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
        GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManagerForCircuitBreaker
            = Mockito.mock(GlobalPartitionEndpointManagerForCircuitBreaker.class);
        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover
            = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class);

        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(
            mockDiagnosticsClientContext(),
            endpointManager,
            true,
            throttlingRetryOptions,
            null,
            globalPartitionEndpointManagerForCircuitBreaker,
            globalPartitionEndpointManagerForPerPartitionAutomaticFailover);

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
        GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManagerForCircuitBreaker
            = Mockito.mock(GlobalPartitionEndpointManagerForCircuitBreaker.class);
        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover
            = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class);

        Mockito.doReturn(new URI("http://localhost")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        Mockito.doReturn(2).when(endpointManager).getPreferredLocationCount();
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(
            mockDiagnosticsClientContext(),
            endpointManager,
            true,
            retryOptions,
            null,
            globalPartitionEndpointManagerForCircuitBreaker,
            globalPartitionEndpointManagerForPerPartitionAutomaticFailover);

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
    public void onBeforeSendRequestNotInvoked() {
        ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManagerForCircuitBreaker
            = Mockito.mock(GlobalPartitionEndpointManagerForCircuitBreaker.class);
        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover
            = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class);

        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(
            mockDiagnosticsClientContext(),
            endpointManager,
            true,
            throttlingRetryOptions,
            null,
            globalPartitionEndpointManagerForCircuitBreaker,
            globalPartitionEndpointManagerForPerPartitionAutomaticFailover);

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

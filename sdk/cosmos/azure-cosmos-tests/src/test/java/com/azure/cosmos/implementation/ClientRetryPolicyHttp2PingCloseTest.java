// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.http.Http2PingTimeoutChannelClosedException;
import com.azure.cosmos.implementation.perPartitionAutomaticFailover.GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.URI;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;

/**
 * Verifies the {@link ClientRetryPolicy} H3 branch that handles channel
 * closures driven by {@code Http2PingHandler} consecutive PING ACK timeouts
 * (sub-status {@code GATEWAY_HTTP2_PING_TIMEOUT_CHANNEL_CLOSED} = 10006).
 * <p>
 * Invariant under test: a PING-driven local transport failure MUST NOT cause
 * {@code markEndpointUnavailableForRead} or {@code markEndpointUnavailableForWrite}
 * to be invoked on {@link GlobalEndpointManager}. The remote gateway is not
 * known to be unhealthy; we route via {@code shouldRetryOnGatewayTimeout}
 * (bounded same-region retry for safely-retriable reads; noRetry for writes)
 * instead of the GATEWAY_ENDPOINT_UNAVAILABLE path that does mark the
 * endpoint down.
 */
public class ClientRetryPolicyHttp2PingCloseTest {

    private static final int ITERATIONS = 10;

    @Test(groups = "unit")
    public void pingTimeoutClose_onRead_retriesInRegionWithoutEndpointMarkDown() throws Exception {
        ThrottlingRetryOptions retryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker globalPartitionEndpointManagerForPerPartitionCircuitBreaker
            = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.class);
        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover
            = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class);

        Mockito.doReturn(new RegionalRoutingContext(new URI("http://localhost")))
            .when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        Mockito.doReturn(2).when(endpointManager).getPreferredLocationCount();

        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(
            mockDiagnosticsClientContext(),
            endpointManager,
            true,
            retryOptions,
            globalPartitionEndpointManagerForPerPartitionCircuitBreaker,
            globalPartitionEndpointManagerForPerPartitionAutomaticFailover,
            false);

        CosmosException cosmosException = buildPingTimeoutCloseCosmosException();

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.requestContext = new DocumentServiceRequestContext();
        clientRetryPolicy.onBeforeSendRequest(dsr);

        for (int i = 0; i < ITERATIONS; i++) {
            Mono<ShouldRetryResult> shouldRetry = clientRetryPolicy.shouldRetry(cosmosException);

            // For Read requests the H3 branch routes to shouldRetryOnGatewayTimeout's
            // canPerformCrossRegionRetryOnGatewayReadTimeout=true arm, which returns
            // retryAfter(endpointFailoverRetryIntervalInMs) until failoverRetryCount
            // exceeds endpointFailoverMaxRetryCount (default 120). Within ITERATIONS
            // we expect every call to retry.
            ClientRetryPolicyTest.validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                .nullException()
                .shouldRetry(true)
                .build());

            // The critical invariant: PING-driven close MUST NOT mark the regional
            // endpoint unavailable. shouldRetryOnGatewayTimeout never calls
            // markEndpointUnavailableFor*, so these counts stay at zero across
            // every iteration.
            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForRead(Mockito.any());
            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForWrite(Mockito.any());
        }
    }

    @Test(groups = "unit")
    public void pingTimeoutClose_onWrite_doesNotRetryAndDoesNotMarkDown() throws Exception {
        ThrottlingRetryOptions retryOptions = new ThrottlingRetryOptions();
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker globalPartitionEndpointManagerForPerPartitionCircuitBreaker
            = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.class);
        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover
            = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class);

        Mockito.doReturn(new RegionalRoutingContext(new URI("http://localhost")))
            .when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));

        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(
            mockDiagnosticsClientContext(),
            endpointManager,
            true,
            retryOptions,
            globalPartitionEndpointManagerForPerPartitionCircuitBreaker,
            globalPartitionEndpointManagerForPerPartitionAutomaticFailover,
            false);

        CosmosException cosmosException = buildPingTimeoutCloseCosmosException();

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Create, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.requestContext = new DocumentServiceRequestContext();
        clientRetryPolicy.onBeforeSendRequest(dsr);

        for (int i = 0; i < ITERATIONS; i++) {
            Mono<ShouldRetryResult> shouldRetry = clientRetryPolicy.shouldRetry(cosmosException);

            // For Create requests canRequestToGatewayBeSafelyRetriedOnReadTimeout
            // returns false (request.isReadOnly() == false), so the H3 branch
            // falls through shouldRetryOnGatewayTimeout to NO_RETRY (PPAF mock
            // returns false by default).
            ClientRetryPolicyTest.validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                .nullException()
                .shouldRetry(false)
                .build());

            // Same invariant as the read case: no region mark-down on a
            // local-transport PING-driven failure, regardless of write semantics.
            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForRead(Mockito.any());
            Mockito.verify(endpointManager, Mockito.times(0)).markEndpointUnavailableForWrite(Mockito.any());
        }
    }

    private static CosmosException buildPingTimeoutCloseCosmosException() {
        // Http2PingTimeoutChannelClosedException extends ClosedChannelException so
        // WebExceptionUtility.isNetworkFailure(...) returns true (gate 1 of H3).
        // BridgeInternal.createCosmosException wraps it as the inner exception so
        // Utils.as(e, CosmosException.class) yields a non-null clientException
        // (gate 2). Setting the sub-status code to 10006 satisfies gate 3 and
        // routes execution into the H3 branch at ClientRetryPolicy.java:142.
        Http2PingTimeoutChannelClosedException pingClose =
            new Http2PingTimeoutChannelClosedException("ping ack timeout (test)", null);
        CosmosException cosmosException = BridgeInternal.createCosmosException(
            null,
            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
            pingClose);
        BridgeInternal.setSubStatusCode(cosmosException,
            HttpConstants.SubStatusCodes.GATEWAY_HTTP2_PING_TIMEOUT_CHANNEL_CLOSED);
        return cosmosException;
    }
}

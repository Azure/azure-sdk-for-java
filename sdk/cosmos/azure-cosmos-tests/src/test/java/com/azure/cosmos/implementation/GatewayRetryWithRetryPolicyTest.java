// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import io.netty.channel.ConnectTimeoutException;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.net.URI;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;

public class GatewayRetryWithRetryPolicyTest {
    private static final int TIMEOUT = 60000;

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void shouldRetryGenericRetryWithException() throws Exception {
        GatewayRetryWithRetryPolicy retryPolicy = new GatewayRetryWithRetryPolicy(
            createRequest(),
            Mockito.mock(GlobalEndpointManager.class),
            30);

        CosmosException retryWithException = BridgeInternal.createCosmosException(HttpConstants.StatusCodes.RETRY_WITH);

        ShouldRetryResult shouldRetryResult = retryPolicy.shouldRetry(retryWithException).block();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(shouldRetryResult.nonRelatedException).isFalse();
        assertThat(shouldRetryResult.policyArg.getValue0()).isFalse();
        assertThat(shouldRetryResult.policyArg.getValue1()).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue3()).isEqualTo(1);
        validateRetryWithTimeRange(10, shouldRetryResult, 5);

        shouldRetryResult = retryPolicy.shouldRetry(retryWithException).block();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue3()).isEqualTo(2);
        validateRetryWithTimeRange(20, shouldRetryResult, 5);
    }

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void shouldNotRetryGoneException() throws Exception {
        GatewayRetryWithRetryPolicy retryPolicy = new GatewayRetryWithRetryPolicy(
            createRequest(),
            Mockito.mock(GlobalEndpointManager.class),
            30);

        CosmosException goneException = BridgeInternal.createCosmosException(HttpConstants.StatusCodes.GONE);

        ShouldRetryResult shouldRetryResult = retryPolicy.shouldRetry(goneException).block();
        assertThat(shouldRetryResult.shouldRetry).isFalse();
        assertThat(shouldRetryResult.nonRelatedException).isTrue();
        assertThat(shouldRetryResult.exception).isSameAs(goneException);
    }

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void shouldNotHandleThrottlingException() throws Exception {
        GatewayRetryWithRetryPolicy retryPolicy = new GatewayRetryWithRetryPolicy(
            createRequest(),
            Mockito.mock(GlobalEndpointManager.class),
            30);

        CosmosException throttlingException = BridgeInternal.createCosmosException(HttpConstants.StatusCodes.TOO_MANY_REQUESTS);

        ShouldRetryResult shouldRetryResult = retryPolicy.shouldRetry(throttlingException).block();
        assertThat(shouldRetryResult.shouldRetry).isFalse();
        assertThat(shouldRetryResult.nonRelatedException).isTrue();
        assertThat(shouldRetryResult.exception).isSameAs(throttlingException);
    }

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void shouldDelegateRetryableNetworkExceptionToMetadataPolicy() throws Exception {
        GatewayRetryWithRetryPolicy retryPolicy = new GatewayRetryWithRetryPolicy(
            createRequest(),
            Mockito.mock(GlobalEndpointManager.class),
            30);

        ShouldRetryResult shouldRetryResult = retryPolicy.shouldRetry(new ConnectTimeoutException()).block();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(shouldRetryResult.nonRelatedException).isFalse();
        assertThat(shouldRetryResult.exception).isNull();
        assertThat(shouldRetryResult.backOffTime).isNotNull();
    }

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void shouldStopRetryingRetryWithAfterTimeout() throws Exception {
        GatewayRetryWithRetryPolicy retryPolicy = new GatewayRetryWithRetryPolicy(
            createRequest(),
            Mockito.mock(GlobalEndpointManager.class),
            0);

        CosmosException retryWithException = BridgeInternal.createCosmosException(HttpConstants.StatusCodes.RETRY_WITH);

        ShouldRetryResult shouldRetryResult = retryPolicy.shouldRetry(retryWithException).block();
        assertThat(shouldRetryResult.shouldRetry).isFalse();
        assertThat(shouldRetryResult.exception).isSameAs(retryWithException);
        assertThat(shouldRetryResult.nonRelatedException).isFalse();
    }

    private static RxDocumentServiceRequest createRequest() throws Exception {
        DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            clientContext,
            OperationType.Read,
            ResourceType.Document);

        request.requestContext.cosmosDiagnostics = clientContext.createDiagnostics();
        request.requestContext.regionalRoutingContextToRoute = new RegionalRoutingContext(new URI("https://localhost"));
        return request;
    }

    private static void validateRetryWithTimeRange(
        int expectedDelayInMs,
        ShouldRetryResult retryResult,
        int saltValueInMs) {

        assertThat(retryResult.backOffTime.toMillis()).isGreaterThan(expectedDelayInMs - saltValueInMs);
        assertThat(retryResult.backOffTime.toMillis()).isLessThan(expectedDelayInMs + saltValueInMs);
    }
}

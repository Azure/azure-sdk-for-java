// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import io.netty.handler.timeout.ReadTimeoutException;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.net.URI;
import java.time.Duration;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;


public class HttpTimeoutPolicyTest {
    private final static String BACKOFF_TIME_PROPERTY_NAME = "backoffSecondsTimeout";

    @Test(groups = "unit")
    public void verifyHttpTimeoutPolicyResponseTimeout() throws Exception {
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new URI("http://localhost:")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(true));


        Exception exception = ReadTimeoutException.INSTANCE;
        CosmosException cosmosException = BridgeInternal.createCosmosException(null, HttpConstants.StatusCodes.REQUEST_TIMEOUT, exception);
        BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT);

        RxDocumentServiceRequest dsr;

        // Query Plan - HttpTimeoutPolicy Control Plane Hot Path
        dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.QueryPlan, "/dbs/db/colls/col/docs/doc", ResourceType.Document);
        RetryContext retryContext = new RetryContext();
        WebExceptionRetryPolicy retryPolicy = new WebExceptionRetryPolicy(retryContext);

        retryPolicy.onBeforeSendRequest(dsr);
        assertThat(dsr.getResponseTimeout()).isEqualTo(Duration.ofMillis(500));
        assertThat(getBackOffTime(retryPolicy)).isEqualTo(0);

        retryContext.addStatusAndSubStatusCode(408, 10002);
        retryPolicy.onBeforeSendRequest(dsr);
        assertThat(dsr.getResponseTimeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(getBackOffTime(retryPolicy)).isEqualTo(1);

        retryContext.addStatusAndSubStatusCode(408, 10002);
        retryPolicy.onBeforeSendRequest(dsr);
        assertThat(dsr.getResponseTimeout()).isEqualTo(Duration.ofSeconds(10));
        assertThat(getBackOffTime(retryPolicy)).isEqualTo(0);


        // Read - HttpTimeoutPolicy Default
        dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Read, "/dbs/db/colls/col/docs/doc", ResourceType.Document);
        retryContext = new RetryContext();
        retryPolicy = new WebExceptionRetryPolicy(retryContext);

        retryPolicy.onBeforeSendRequest(dsr);
        assertThat(dsr.getResponseTimeout()).isEqualTo(Duration.ofSeconds(60));
        assertThat(getBackOffTime(retryPolicy)).isEqualTo(0);


        retryContext.addStatusAndSubStatusCode(408, 10002);
        retryPolicy.onBeforeSendRequest(dsr);
        assertThat(dsr.getResponseTimeout()).isEqualTo(Duration.ofSeconds(60));
        assertThat(getBackOffTime(retryPolicy)).isEqualTo(1);

        retryContext.addStatusAndSubStatusCode(408, 10002);
        retryPolicy.onBeforeSendRequest(dsr);
        assertThat(dsr.getResponseTimeout()).isEqualTo(Duration.ofSeconds(60));
        assertThat(getBackOffTime(retryPolicy)).isEqualTo(0);


        // Database Account - HtHttpTimeoutPolicy Control Plane Read
        dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Read, "/dbs/db/colls/col/docs/doc", ResourceType.DatabaseAccount);
        retryContext = new RetryContext();
        retryPolicy = new WebExceptionRetryPolicy(retryContext);

        retryPolicy.onBeforeSendRequest(dsr);
        assertThat(dsr.getResponseTimeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(getBackOffTime(retryPolicy)).isEqualTo(0);


        retryContext.addStatusAndSubStatusCode(408, 10002);
        retryPolicy.onBeforeSendRequest(dsr);
        assertThat(dsr.getResponseTimeout()).isEqualTo(Duration.ofSeconds(10));
        assertThat(getBackOffTime(retryPolicy)).isEqualTo(1);

        retryContext.addStatusAndSubStatusCode(408, 10002);
        retryPolicy.onBeforeSendRequest(dsr);
        assertThat(dsr.getResponseTimeout()).isEqualTo(Duration.ofSeconds(20));
        assertThat(getBackOffTime(retryPolicy)).isEqualTo(0);
    }

    public int getBackOffTime(WebExceptionRetryPolicy retryPolicy) throws Exception {
        Field field = retryPolicy.getClass().getDeclaredField(BACKOFF_TIME_PROPERTY_NAME);
        field.setAccessible(true);
        return (int) field.get(retryPolicy);
    }
}

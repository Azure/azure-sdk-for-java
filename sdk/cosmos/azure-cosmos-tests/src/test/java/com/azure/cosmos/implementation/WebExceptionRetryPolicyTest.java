// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import io.netty.handler.timeout.ReadTimeoutException;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Duration;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;

public class WebExceptionRetryPolicyTest extends TestSuiteBase {

    @DataProvider(name = "operationTypeProvider")
    public static Object[][] operationTypeProvider() {
        return new Object[][]{
            // OperationType
            { OperationType.Read },
            { OperationType.Replace },
            { OperationType.Create },
            { OperationType.Delete },
            { OperationType.Query },
            { OperationType.Patch }
        };
    }

    @DataProvider(name = "readOperationTypeProvider")
    public static Object[][] readOperationTypeProvider() {
        return new Object[][]{
            // OperationType, useThinClientMode, expectedTimeout1, expectedTimeout2, expectedTimeout3, backoff1, backoff2

            // Regular Gateway mode - Read (uses HttpTimeoutPolicyDefault)
            { OperationType.Read, false, Duration.ofSeconds(60), Duration.ofSeconds(60), Duration.ofSeconds(60), Duration.ZERO, Duration.ofSeconds(1) },

            // Thin Client mode - Point Read (uses HttpTimeoutPolicyForGatewayV2.INSTANCE_FOR_POINT_READ)
            { OperationType.Read, true, Duration.ofSeconds(6), Duration.ofSeconds(6), Duration.ofSeconds(10), Duration.ZERO, Duration.ZERO },

            // Thin Client mode - Query (uses HttpTimeoutPolicyForGatewayV2.INSTANCE_FOR_QUERY_AND_CHANGE_FEED)
            { OperationType.Query, true, Duration.ofSeconds(6), Duration.ofSeconds(6), Duration.ofSeconds(10), Duration.ZERO, Duration.ZERO }
        };
    }

    @Test(groups = {"unit"}, dataProvider = "readOperationTypeProvider")
    public void shouldRetryOnTimeoutForReadOperations(
            OperationType operationType,
            boolean useThinClientMode,
            Duration expectedTimeout1,
            Duration expectedTimeout2,
            Duration expectedTimeout3,
            Duration backoff1,
            Duration backoff2) throws Exception {
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);

        RegionalRoutingContext regionalRoutingContext = new RegionalRoutingContext(new URI("http://localhost:"));

        Mockito.doReturn(regionalRoutingContext).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(true));

        RetryContext retryContext = new RetryContext();
        WebExceptionRetryPolicy webExceptionRetryPolicy = new WebExceptionRetryPolicy(retryContext);
        Exception exception = ReadTimeoutException.INSTANCE;
        CosmosException cosmosException = BridgeInternal.createCosmosException(null, HttpConstants.StatusCodes.REQUEST_TIMEOUT, exception);
        BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT);

        RxDocumentServiceRequest dsr;
        Mono<ShouldRetryResult> shouldRetry;

        dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            operationType, "/dbs/db/colls/col/docs/doc", ResourceType.Document);
        dsr.useThinClientMode = useThinClientMode;
        dsr.requestContext.regionalRoutingContextToRoute = regionalRoutingContext;

        // 1st Attempt
        webExceptionRetryPolicy.onBeforeSendRequest(dsr);
        assertThat(dsr.getResponseTimeout()).isEqualTo(expectedTimeout1);
        shouldRetry = webExceptionRetryPolicy.shouldRetry(cosmosException);

        validateSuccess(shouldRetry, ShouldRetryValidator.builder().
            nullException().
            shouldRetry(true).
            backOffTime(backoff1).
            build());

        // 2nd Attempt
        retryContext.addStatusAndSubStatusCode(408, 10002);
        assertThat(dsr.getResponseTimeout()).isEqualTo(expectedTimeout2);
        shouldRetry = webExceptionRetryPolicy.shouldRetry(cosmosException);

        validateSuccess(shouldRetry, ShouldRetryValidator.builder().
            nullException().
            shouldRetry(true).
            backOffTime(backoff2).
            build());

        // 3rd Attempt
        retryContext.addStatusAndSubStatusCode(408, 10002);
        assertThat(dsr.getResponseTimeout()).isEqualTo(expectedTimeout3);
        shouldRetry = webExceptionRetryPolicy.shouldRetry(cosmosException);

        validateSuccess(shouldRetry, ShouldRetryValidator.builder().
            nullException().
            shouldRetry(false).
            build());
    }

    @Test(groups = {"unit"})
    public void shouldRetryOnTimeoutForMetaDataReadOperations() throws Exception {
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);

        RegionalRoutingContext regionalRoutingContext = new RegionalRoutingContext(new URI("http://localhost:"));

        Mockito.doReturn(regionalRoutingContext).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(true));

        RetryContext retryContext = new RetryContext();
        WebExceptionRetryPolicy webExceptionRetryPolicy = new WebExceptionRetryPolicy(retryContext);
        Exception exception = ReadTimeoutException.INSTANCE;
        CosmosException cosmosException = BridgeInternal.createCosmosException(null, HttpConstants.StatusCodes.REQUEST_TIMEOUT, exception);
        BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT);

        RxDocumentServiceRequest dsr;
        Mono<ShouldRetryResult> shouldRetry;

        //Default HttpTimeout Policy
        dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Read, "/dbs/db", ResourceType.DatabaseAccount);
        dsr.requestContext.regionalRoutingContextToRoute = regionalRoutingContext;

        // 1st Attempt
        webExceptionRetryPolicy.onBeforeSendRequest(dsr);
        assertThat(dsr.getResponseTimeout()).isEqualTo(Duration.ofSeconds(5));
        shouldRetry = webExceptionRetryPolicy.shouldRetry(cosmosException);

        validateSuccess(shouldRetry, ShouldRetryValidator.builder().
            nullException().
            shouldRetry(true).
            backOffTime(Duration.ofSeconds(0)).
            build());

        // 2nd Attempt
        retryContext.addStatusAndSubStatusCode(408, 10002);
        assertThat(dsr.getResponseTimeout()).isEqualTo(Duration.ofSeconds(10));
        shouldRetry = webExceptionRetryPolicy.shouldRetry(cosmosException);


        validateSuccess(shouldRetry, ShouldRetryValidator.builder().
            nullException().
            shouldRetry(true).
            backOffTime(Duration.ofSeconds(1)).
            build());

        //3rd Attempt
        retryContext.addStatusAndSubStatusCode(408, 10002);
        assertThat(dsr.getResponseTimeout()).isEqualTo(Duration.ofSeconds(20));
        shouldRetry = webExceptionRetryPolicy.shouldRetry(cosmosException);


        validateSuccess(shouldRetry, ShouldRetryValidator.builder().
            nullException().
            shouldRetry(false).
            build());
    }

    @Test(groups = {"unit"})
    public void shouldRetryOnTimeoutForQueryPlanOperations() throws Exception {
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);

        RegionalRoutingContext regionalRoutingContext = new RegionalRoutingContext(new URI("http://localhost:"));

        Mockito.doReturn(regionalRoutingContext).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(true));

        RetryContext retryContext = new RetryContext();
        WebExceptionRetryPolicy webExceptionRetryPolicy = new WebExceptionRetryPolicy(retryContext);
        Exception exception = ReadTimeoutException.INSTANCE;
        CosmosException cosmosException = BridgeInternal.createCosmosException(null, HttpConstants.StatusCodes.REQUEST_TIMEOUT, exception);
        BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT);

        RxDocumentServiceRequest dsr;
        Mono<ShouldRetryResult> shouldRetry;

        //Default HttpTimeout Policy
        dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.QueryPlan, "/dbs/db/colls/col/docs/doc", ResourceType.Document);
        dsr.requestContext.regionalRoutingContextToRoute = regionalRoutingContext;

        // 1st Attempt
        webExceptionRetryPolicy.onBeforeSendRequest(dsr);
        assertThat(dsr.getResponseTimeout()).isEqualTo(Duration.ofMillis(500));
        shouldRetry = webExceptionRetryPolicy.shouldRetry(cosmosException);

        validateSuccess(shouldRetry, ShouldRetryValidator.builder().
            nullException().
            shouldRetry(true).
            backOffTime(Duration.ofSeconds(0)).
            build());

        // 2nd Attempt
        retryContext.addStatusAndSubStatusCode(408, 10002);
        assertThat(dsr.getResponseTimeout()).isEqualTo(Duration.ofSeconds(5));
        shouldRetry = webExceptionRetryPolicy.shouldRetry(cosmosException);

        validateSuccess(shouldRetry, ShouldRetryValidator.builder().
            nullException().
            shouldRetry(true).
            backOffTime(Duration.ofSeconds(1)).
            build());

        //3rd Attempt - retry is set to false, as we only make 2 retry attempts for now.
        retryContext.addStatusAndSubStatusCode(408, 10002);
        assertThat(dsr.getResponseTimeout()).isEqualTo(Duration.ofSeconds(10));
        shouldRetry = webExceptionRetryPolicy.shouldRetry(cosmosException);

        validateSuccess(shouldRetry, ShouldRetryValidator.builder().
            nullException().
            shouldRetry(false).
            build());
    }

    @Test(groups = "unit")
    public void shouldNotRetryOnTimeoutForWriteOperations() throws Exception {
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);

        RegionalRoutingContext regionalRoutingContext = new RegionalRoutingContext(new URI("http://localhost:"));

        Mockito.doReturn(regionalRoutingContext).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(true));


        Exception exception = ReadTimeoutException.INSTANCE;
        CosmosException cosmosException = BridgeInternal.createCosmosException(null, HttpConstants.StatusCodes.REQUEST_TIMEOUT, exception);
        BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT);

        RxDocumentServiceRequest dsr;
        Mono<ShouldRetryResult> shouldRetry;

        //Data Plane Write - Should not retry
        dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Create, "/dbs/db/colls/col/docs/doc", ResourceType.Document);
        dsr.requestContext.regionalRoutingContextToRoute = regionalRoutingContext;

        WebExceptionRetryPolicy webExceptionRetryPolicy = new WebExceptionRetryPolicy(new RetryContext());
        webExceptionRetryPolicy.onBeforeSendRequest(dsr);
        shouldRetry = webExceptionRetryPolicy.shouldRetry(cosmosException);

        validateSuccess(shouldRetry, ShouldRetryValidator.builder()
            .nullException()
            .shouldRetry(false)
            .build());

        //Metadata Write - Should not Retry
        dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Create, "/dbs/db", ResourceType.DatabaseAccount);
        dsr.requestContext.regionalRoutingContextToRoute = regionalRoutingContext;

        webExceptionRetryPolicy = new WebExceptionRetryPolicy(new RetryContext());
        webExceptionRetryPolicy.onBeforeSendRequest(dsr);
        shouldRetry = webExceptionRetryPolicy.shouldRetry(cosmosException);

        validateSuccess(shouldRetry, ShouldRetryValidator.builder()
            .nullException()
            .shouldRetry(false)
            .build());

        //Data Plane Write with Thin Client Mode - Should still not retry
        dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Create, "/dbs/db/colls/col/docs/doc", ResourceType.Document);
        dsr.useThinClientMode = true;
        dsr.requestContext.regionalRoutingContextToRoute = regionalRoutingContext;

        webExceptionRetryPolicy = new WebExceptionRetryPolicy(new RetryContext());
        webExceptionRetryPolicy.onBeforeSendRequest(dsr);
        shouldRetry = webExceptionRetryPolicy.shouldRetry(cosmosException);

        validateSuccess(shouldRetry, ShouldRetryValidator.builder()
            .nullException()
            .shouldRetry(false)
            .build());
    }

    @Test(groups = "unit", dataProvider = "operationTypeProvider")
    public void httpNetworkFailureOnAddressRefresh(OperationType operationType) throws Exception {
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);

        RegionalRoutingContext regionalRoutingContext = new RegionalRoutingContext(new URI("http://localhost:"));

        Mockito.doReturn(regionalRoutingContext).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        Mockito.doReturn(2).when(endpointManager).getPreferredLocationCount();

        RetryContext retryContext = new RetryContext();
        WebExceptionRetryPolicy webExceptionRetryPolicy = new WebExceptionRetryPolicy(retryContext);
        Exception exception = ReadTimeoutException.INSTANCE;
        CosmosException cosmosException = BridgeInternal.createCosmosException(null, HttpConstants.StatusCodes.REQUEST_TIMEOUT, exception);
        BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            operationType, "/dbs/db/colls/col/docs/", ResourceType.Document);
        dsr.setAddressRefresh(true, false);
        dsr.requestContext = new DocumentServiceRequestContext();
        dsr.requestContext.regionalRoutingContextToRoute = regionalRoutingContext;

        // 1st Attempt
        webExceptionRetryPolicy.onBeforeSendRequest(dsr);
        assertThat(dsr.getResponseTimeout()).isEqualTo(Duration.ofMillis(500));
        Mono<ShouldRetryResult> shouldRetry = webExceptionRetryPolicy.shouldRetry(cosmosException);

        validateSuccess(shouldRetry, ShouldRetryValidator.builder()
            .nullException()
            .shouldRetry(true)
            .backOffTime(Duration.ofSeconds(0))
            .build());

        // 2nd Attempt
        assertThat(dsr.getResponseTimeout()).isEqualTo(Duration.ofSeconds(5));
        shouldRetry = webExceptionRetryPolicy.shouldRetry(cosmosException);

        validateSuccess(shouldRetry, ShouldRetryValidator.builder()
            .nullException()
            .shouldRetry(true)
            .backOffTime(Duration.ofSeconds(1))
            .build());


        // 3rd Attempt
        assertThat(dsr.getResponseTimeout()).isEqualTo(Duration.ofSeconds(10));
        shouldRetry = webExceptionRetryPolicy.shouldRetry(cosmosException);

        validateSuccess(shouldRetry, ShouldRetryValidator.builder()
            .nullException()
            .shouldRetry(false)
            .build());
    }

    public static void validateSuccess(Mono<ShouldRetryResult> single,
                                       ShouldRetryValidator validator) {

        validateSuccess(single, validator, TIMEOUT);
    }

    public static void validateSuccess(Mono<ShouldRetryResult> single,
                                       ShouldRetryValidator validator,
                                       long timeout) {
        StepVerifier.create(single)
            .assertNext(validator::validate)
            .expectComplete()
            .verify(Duration.ofMillis(timeout));
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import io.netty.handler.timeout.ReadTimeoutException;
import io.reactivex.subscribers.TestSubscriber;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

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

    @Test(groups = {"unit"})
    public void shouldRetryOnTimeoutForReadOperations() throws Exception {
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new URI("http://localhost:")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
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
            OperationType.Read, "/dbs/db/colls/col/docs/doc", ResourceType.Document);

        // 1st Attempt
        webExceptionRetryPolicy.onBeforeSendRequest(dsr);
        assertThat(dsr.getResponseTimeout()).isEqualTo(Duration.ofSeconds(60));
        shouldRetry = webExceptionRetryPolicy.shouldRetry(cosmosException);

        validateSuccess(shouldRetry, ShouldRetryValidator.builder().
            nullException().
            shouldRetry(true).
            backOffTime(Duration.ofSeconds(0)).
            build());

        // 2nd Attempt
        retryContext.addStatusAndSubStatusCode(408, 10002);
        assertThat(dsr.getResponseTimeout()).isEqualTo(Duration.ofSeconds(60));
        shouldRetry = webExceptionRetryPolicy.shouldRetry(cosmosException);

        validateSuccess(shouldRetry, ShouldRetryValidator.builder().
            nullException().
            shouldRetry(true).
            backOffTime(Duration.ofSeconds(1)).
            build());

        // 3rd Attempt
        retryContext.addStatusAndSubStatusCode(408, 10002);
        assertThat(dsr.getResponseTimeout()).isEqualTo(Duration.ofSeconds(60));
        shouldRetry = webExceptionRetryPolicy.shouldRetry(cosmosException);

        validateSuccess(shouldRetry, ShouldRetryValidator.builder().
            nullException().
            shouldRetry(false).
            build());
    }

    @Test(groups = {"unit"})
    public void shouldRetryOnTimeoutForMetaDataReadOperations() throws Exception {
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new URI("http://localhost:")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
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
        Mockito.doReturn(new URI("http://localhost:")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
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
        Mockito.doReturn(new URI("http://localhost:")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(true));


        Exception exception = ReadTimeoutException.INSTANCE;
        CosmosException cosmosException = BridgeInternal.createCosmosException(null, HttpConstants.StatusCodes.REQUEST_TIMEOUT, exception);
        BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT);

        RxDocumentServiceRequest dsr;
        Mono<ShouldRetryResult> shouldRetry;

        //Data Plane Write - Should not retry
        dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Create, "/dbs/db/colls/col/docs/doc", ResourceType.Document);

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
        Mockito.doReturn(new URI("http://localhost:")).when(endpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
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
        TestSubscriber<ShouldRetryResult> testSubscriber = new TestSubscriber<>();

        single.flux().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
        validator.validate(testSubscriber.values().get(0));
    }
}

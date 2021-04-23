// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.BackoffRetryUtility;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentClientRetryPolicy;
import com.azure.cosmos.implementation.IRetryPolicy;
import com.azure.cosmos.implementation.IRetryPolicyFactory;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.Quadruple;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.RxStoreModel;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.directconnectivity.AddressSelector;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import io.reactivex.subscribers.TestSubscriber;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static com.azure.cosmos.implementation.Utils.getUTF8BytesOrNull;
import static org.assertj.core.api.Assertions.assertThat;

public class RetryContextOnDiagnosticTest extends TestSuiteBase {
    private final static String exceptionText = "TestException";
    private final static String responseText = "TestResponse";

    private IRetryPolicy retryPolicy;
    private RxDocumentServiceRequest serviceRequest;
    private AddressSelector addressSelector;

    @Test(groups = {"simple"})
    public void backoffRetryUtilityExecuteRetry() throws Exception {
        @SuppressWarnings("unchecked")
        Callable<Mono<StoreResponse>> callbackMethod = Mockito.mock(Callable.class);
        serviceRequest = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        retryPolicy = new TestRetryPolicy();
        addressSelector = Mockito.mock(AddressSelector.class);
        CosmosException exception = new CosmosException(410, exceptionText);
        Mockito.when(callbackMethod.call()).thenThrow(exception, exception, exception, exception, exception)
            .thenReturn(Mono.just(new StoreResponse(200, new ArrayList<>(), getUTF8BytesOrNull(responseText))));
        Mono<StoreResponse> monoResponse = BackoffRetryUtility.executeRetry(callbackMethod, retryPolicy);
        StoreResponse response = validateSuccess(monoResponse);

        assertThat(response.getResponseBody()).isEqualTo(getUTF8BytesOrNull(responseText));
        assertThat(retryPolicy.getRetryCount()).isEqualTo(5);
        assertThat(retryPolicy.getStatusAndSubStatusCodes().size()).isEqualTo(retryPolicy.getRetryCount());
    }

    @Test(groups = {"simple"})
    public void backoffRetryUtilityExecuteRetryWithFailure() throws Exception {
        @SuppressWarnings("unchecked")
        Callable<Mono<StoreResponse>> callbackMethod = Mockito.mock(Callable.class);
        serviceRequest = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        retryPolicy = new TestRetryPolicy();
        addressSelector = Mockito.mock(AddressSelector.class);
        CosmosException exception = new CosmosException(410, exceptionText);
        Mockito.when(callbackMethod.call()).thenThrow(exception);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            ((TestRetryPolicy) retryPolicy).noRetry = true;
        }, 10, TimeUnit.SECONDS);
        Mono<StoreResponse> monoResponse = BackoffRetryUtility.executeRetry(callbackMethod, retryPolicy);
        validateFailure(monoResponse);

        assertThat(retryPolicy.getRetryCount()).isGreaterThanOrEqualTo(5);
        assertThat(retryPolicy.getStatusAndSubStatusCodes().size()).isEqualTo(retryPolicy.getRetryCount() + 1);
    }

    @Test(groups = {"simple"})
    @SuppressWarnings("unchecked")
    public void backoffRetryUtilityExecuteAsync() {
        Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<StoreResponse>> inBackoffAlternateCallbackMethod = Mockito.mock(Function.class);
        Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<StoreResponse>> parameterizedCallbackMethod = Mockito.mock(Function.class);
        serviceRequest = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        retryPolicy = new TestRetryPolicy();
        addressSelector = Mockito.mock(AddressSelector.class);
        CosmosException exception = new CosmosException(410, exceptionText);
        Mono<StoreResponse> exceptionMono = Mono.error(exception);
        Mockito.when(parameterizedCallbackMethod.apply(ArgumentMatchers.any())).thenReturn(exceptionMono, exceptionMono, exceptionMono, exceptionMono, exceptionMono)
            .thenReturn(Mono.just(new StoreResponse(200, new ArrayList<>(), getUTF8BytesOrNull(responseText))));
        Mono<StoreResponse> monoResponse = BackoffRetryUtility.executeAsync(
            parameterizedCallbackMethod,
            retryPolicy,
            inBackoffAlternateCallbackMethod,
            Duration.ofSeconds(5),
            serviceRequest,
            addressSelector);
        StoreResponse response = validateSuccess(monoResponse);

        assertThat(serviceRequest.requestContext.retryContext.retryCount).isEqualTo(5);
        assertThat(response.getResponseBody()).isEqualTo(getUTF8BytesOrNull(responseText));
        assertThat(retryPolicy.getRetryCount()).isEqualTo(5);
        assertThat(retryPolicy.getStatusAndSubStatusCodes().size()).isEqualTo(retryPolicy.getRetryCount());
    }

    @Test(groups = {"simple"})
    @SuppressWarnings("unchecked")
    public void backoffRetryUtilityExecuteAsyncWithFailure() {
        Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<StoreResponse>> inBackoffAlternateCallbackMethod = Mockito.mock(Function.class);
        Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<StoreResponse>> parameterizedCallbackMethod = Mockito.mock(Function.class);
        serviceRequest = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        retryPolicy = new TestRetryPolicy();
        CosmosException exception = new CosmosException(410, exceptionText);
        Mono<StoreResponse> exceptionMono = Mono.error(exception);
        Mockito.when(parameterizedCallbackMethod.apply(ArgumentMatchers.any())).thenReturn(exceptionMono);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            ((TestRetryPolicy) retryPolicy).noRetry = true;
        }, 10, TimeUnit.SECONDS);
        Mono<StoreResponse> monoResponse = BackoffRetryUtility.executeAsync(
            parameterizedCallbackMethod,
            retryPolicy,
            inBackoffAlternateCallbackMethod,
            Duration.ofSeconds(5),
            serviceRequest,
            addressSelector);
        validateFailure(monoResponse);

        assertThat(serviceRequest.requestContext.retryContext.retryCount).isGreaterThanOrEqualTo(5);
        assertThat(retryPolicy.getRetryCount()).isGreaterThanOrEqualTo(5);
        assertThat(retryPolicy.getStatusAndSubStatusCodes().size()).isEqualTo(retryPolicy.getRetryCount() + 1);
    }

    @Test(groups = {"simple"})
    public void retryContextMockTestOnCRUDOperation() throws NoSuchFieldException, IllegalAccessException {
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY);
        CosmosClient cosmosClient = cosmosClientBuilder.directMode(DirectConnectionConfig.getDefaultConfig()).buildClient();
        CosmosAsyncContainer cosmosAsyncContainer = getSharedMultiPartitionCosmosContainer(cosmosClient.asyncClient());
        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl)cosmosClient.asyncClient().getContextClient();
        RxStoreModel mockStoreModel = Mockito.mock(RxStoreModel.class);
        RxDocumentServiceResponse mockRxDocumentServiceResponse = Mockito.mock(RxDocumentServiceResponse.class);

        Field storeModelField = RxDocumentClientImpl.class.getDeclaredField("storeModel");
        storeModelField.setAccessible(true);
        storeModelField.set(rxDocumentClient, mockStoreModel);

        IRetryPolicyFactory mockRetryFactory = Mockito.mock(IRetryPolicyFactory.class);
        Field resetSessionTokenRetryPolicyField = RxDocumentClientImpl.class.getDeclaredField("resetSessionTokenRetryPolicy");
        resetSessionTokenRetryPolicyField.setAccessible(true);
        resetSessionTokenRetryPolicyField.set(rxDocumentClient, mockRetryFactory);

        TestRetryPolicy retryPolicy = Mockito.mock(TestRetryPolicy.class);
        Mockito.when(retryPolicy.getRetryCount()).thenReturn(1);

        Mockito.when(mockRetryFactory.getRequestPolicy()).thenReturn(retryPolicy);
        Mockito.when(mockStoreModel.processMessage(ArgumentMatchers.any(RxDocumentServiceRequest.class))).thenReturn(Mono.just(mockRxDocumentServiceResponse));
        Mockito.when(mockRxDocumentServiceResponse.getResource(Document.class)).thenReturn(new Document());
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setPartitionKey(new PartitionKey("TestPk"));
        String itemSelfLink = cosmosAsyncContainer.getLink()+"/docs/TestDoc";
        Mono<ResourceResponse<Document>>  responseFlux = rxDocumentClient.createDocument(cosmosAsyncContainer.getLink(), new Document(), requestOptions, false);
        validateServiceResponseSuccess(responseFlux);

        Mockito.verify(retryPolicy, Mockito.times(2)).getRetryCount();
        Mockito.verify(retryPolicy, Mockito.times(3)).getStatusAndSubStatusCodes();

        retryPolicy = Mockito.mock(TestRetryPolicy.class);
        Mockito.when(retryPolicy.getRetryCount()).thenReturn(1);
        Mockito.when(mockRetryFactory.getRequestPolicy()).thenReturn(retryPolicy);
        responseFlux = rxDocumentClient.readDocument(itemSelfLink,requestOptions);
        validateServiceResponseSuccess(responseFlux);

        Mockito.verify(retryPolicy, Mockito.times(2)).getRetryCount();
        Mockito.verify(retryPolicy, Mockito.times(3)).getStatusAndSubStatusCodes();

        retryPolicy = Mockito.mock(TestRetryPolicy.class);
        Mockito.when(retryPolicy.getRetryCount()).thenReturn(1);
        Mockito.when(mockRetryFactory.getRequestPolicy()).thenReturn(retryPolicy);
        responseFlux = rxDocumentClient.deleteDocument(itemSelfLink,requestOptions);
        validateServiceResponseSuccess(responseFlux);

        Mockito.verify(retryPolicy, Mockito.times(2)).getRetryCount();
        Mockito.verify(retryPolicy, Mockito.times(3)).getStatusAndSubStatusCodes();

        retryPolicy = Mockito.mock(TestRetryPolicy.class);
        Mockito.when(retryPolicy.getRetryCount()).thenReturn(1);
        Mockito.when(mockRetryFactory.getRequestPolicy()).thenReturn(retryPolicy);
        responseFlux = rxDocumentClient.replaceDocument(itemSelfLink, new Document(),requestOptions);
        validateServiceResponseSuccess(responseFlux);

        Mockito.verify(retryPolicy, Mockito.times(2)).getRetryCount();
        Mockito.verify(retryPolicy, Mockito.times(3)).getStatusAndSubStatusCodes();

        retryPolicy = Mockito.mock(TestRetryPolicy.class);
        Mockito.when(retryPolicy.getRetryCount()).thenReturn(1);
        Mockito.when(mockRetryFactory.getRequestPolicy()).thenReturn(retryPolicy);
        responseFlux = rxDocumentClient.upsertDocument(itemSelfLink, new Document(),requestOptions, false);
        validateServiceResponseSuccess(responseFlux);

        Mockito.verify(retryPolicy, Mockito.times(2)).getRetryCount();
        Mockito.verify(retryPolicy, Mockito.times(3)).getStatusAndSubStatusCodes();

        cosmosClient.close();
    }

    private StoreResponse validateSuccess(Mono<StoreResponse> storeResponse) {
        TestSubscriber<StoreResponse> testSubscriber = new TestSubscriber<>();
        storeResponse.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(60000, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();
        testSubscriber.assertValueCount(1);
        return testSubscriber.values().get(0);
    }

    private void validateFailure(Mono<StoreResponse> storeResponse) {
        TestSubscriber<StoreResponse> testSubscriber = new TestSubscriber<>();
        storeResponse.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(60000, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotComplete();
        testSubscriber.assertTerminated();
    }

    private void validateServiceResponseSuccess(Mono<ResourceResponse<Document>> documentServiceResponseMono) {
        TestSubscriber<ResourceResponse<Document>> testSubscriber = new TestSubscriber<>();
        documentServiceResponseMono.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(60000, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();
        testSubscriber.assertValueCount(1);
    }

    private class TestRetryPolicy extends DocumentClientRetryPolicy {
        boolean noRetry;

        @Override
        public Mono<ShouldRetryResult> shouldRetry(Exception e) {
            if (noRetry) {
                return Mono.just(ShouldRetryResult.noRetry());
            }
            return Mono.just(ShouldRetryResult.retryAfter(Duration.ofSeconds(2)));
        }

        @Override
        public void onBeforeSendRequest(RxDocumentServiceRequest request) {
            //no implementation needed for mock test
        }
    }
}

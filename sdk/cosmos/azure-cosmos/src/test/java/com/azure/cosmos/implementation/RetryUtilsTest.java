// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.directconnectivity.AddressSelector;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.StoreResponseValidator;
import io.reactivex.subscribers.TestSubscriber;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static com.azure.cosmos.implementation.TestUtils.mockDocumentServiceRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class RetryUtilsTest {
    IRetryPolicy retryPolicy;
    RxDocumentServiceRequest request;
    Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<StoreResponse>> callbackMethod;
    Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<StoreResponse>> inBackoffAlternateCallbackMethod;
    private final static DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();
    private static final Duration minBackoffForInBackoffCallback = Duration.ofMillis(10);
    private static final int TIMEOUT = 30000;
    private static final Duration BACK_OFF_DURATION = Duration.ofMillis(20);
    private StoreResponse storeResponse;
    private AddressSelector addressSelector;

    @BeforeClass(groups = { "unit" })
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void before_RetryUtilsTest() throws Exception {
        retryPolicy = Mockito.mock(IRetryPolicy.class);
        Mockito.doReturn(new RetryContext()).when(retryPolicy).getRetryContext();
        request =  mockDocumentServiceRequest(clientContext);
        callbackMethod = Mockito.mock(Function.class);
        inBackoffAlternateCallbackMethod = Mockito.mock(Function.class);
        storeResponse = getStoreResponse();
        addressSelector = Mockito.mock(AddressSelector.class);
    }

    /**
     * This method will make sure we are throwing original exception in case of
     * ShouldRetryResult.noRetry() instead of Single.error(null).
     */
    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void toRetryWithAlternateFuncWithNoRetry() {
        Function<Throwable, Mono<StoreResponse>> onErrorFunc = RetryUtils.toRetryWithAlternateFunc(callbackMethod,
                retryPolicy, inBackoffAlternateCallbackMethod, minBackoffForInBackoffCallback, request, addressSelector);
        Mockito.when(retryPolicy.shouldRetry(ArgumentMatchers.any())).thenReturn(Mono.just(ShouldRetryResult.noRetry()));
        Mono<StoreResponse> response = onErrorFunc.apply(new GoneException());
        validateFailure(response, TIMEOUT, GoneException.class);
    }

    /**
     * This method will test retries on callbackMethod, eventually returning success
     * response after some failures and making sure it failed for at least specific
     * number before passing.
     */
    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void toRetryWithAlternateFuncTestingMethodOne() {
        Function<Throwable, Mono<StoreResponse>> onErrorFunc = RetryUtils.toRetryWithAlternateFunc(callbackMethod,
                retryPolicy, null, minBackoffForInBackoffCallback, request, addressSelector);

        toggleMockFuncBtwFailureSuccess(callbackMethod);
        Mockito.when(retryPolicy.shouldRetry(ArgumentMatchers.any()))
                .thenReturn(Mono.just(ShouldRetryResult.retryAfter(BACK_OFF_DURATION)));
        Mono<StoreResponse> response = onErrorFunc.apply(new GoneException());
        StoreResponseValidator validator = StoreResponseValidator.create().withStatus(storeResponse.getStatus())
                .withContent(storeResponse.getResponseBody()).build();
        validateSuccess(response, validator, TIMEOUT);
        Mockito.verify(callbackMethod, Mockito.times(4)).apply(ArgumentMatchers.any());
    }

    /**
     * This method will test retries on inBackoffAlternateCallbackMethod, eventually
     * returning success response after some failures and making sure it failed for
     * at least specific number before passing.
     */
    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void toRetryWithAlternateFuncTestingMethodTwo() {
        Function<Throwable, Mono<StoreResponse>> onErrorFunc = RetryUtils.toRetryWithAlternateFunc(callbackMethod,
                retryPolicy, inBackoffAlternateCallbackMethod, minBackoffForInBackoffCallback, request, addressSelector);
        Mockito.when(callbackMethod.apply(ArgumentMatchers.any())).thenReturn(Mono.error(new GoneException()));
        toggleMockFuncBtwFailureSuccess(inBackoffAlternateCallbackMethod);
        Mockito.when(retryPolicy.shouldRetry(ArgumentMatchers.any()))
                .thenReturn(Mono.just(ShouldRetryResult.retryAfter(BACK_OFF_DURATION)));
        Mono<StoreResponse> response = onErrorFunc.apply(new GoneException());
        StoreResponseValidator validator = StoreResponseValidator.create().withStatus(storeResponse.getStatus())
                .withContent(storeResponse.getResponseBody()).build();
        validateSuccess(response, validator, TIMEOUT);
        Mockito.verify(inBackoffAlternateCallbackMethod, Mockito.times(4)).apply(ArgumentMatchers.any());
    }

    private void validateFailure(Mono<StoreResponse> single, long timeout, Class<? extends Throwable> class1) {

        TestSubscriber<StoreResponse> testSubscriber = new TestSubscriber<>();
        single.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotComplete();
        testSubscriber.assertTerminated();
        assertThat(testSubscriber.errorCount()).isEqualTo(1);
        Throwable throwable = Exceptions.unwrap(testSubscriber.errors().get(0));
        if (!(throwable.getClass().equals(class1))) {
            fail("Not expecting " + testSubscriber.getEvents().get(1).get(0));
        }
    }

    private void validateSuccess(Mono<StoreResponse> single, StoreResponseValidator validator, long timeout) {

        TestSubscriber<StoreResponse> testSubscriber = new TestSubscriber<>();
        single.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        assertThat(testSubscriber.valueCount()).isEqualTo(1);
        validator.validate(testSubscriber.values().get(0));
    }

    private void toggleMockFuncBtwFailureSuccess(
            Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<StoreResponse>> method) {
        Mockito.when(method.apply(ArgumentMatchers.any())).thenAnswer(new Answer<Mono<StoreResponse>>() {

            private int count = 0;

            @Override
            public Mono<StoreResponse> answer(InvocationOnMock invocation) throws Throwable {
                if (count++ < 3) {
                    return Mono.error(new GoneException());
                }
                return Mono.just(storeResponse);
            }
        });
    }

    private StoreResponse getStoreResponse() {
        StoreResponseBuilder storeResponseBuilder = new StoreResponseBuilder().withContent("Test content")
                .withStatus(200);
        return storeResponseBuilder.build();
    }
}

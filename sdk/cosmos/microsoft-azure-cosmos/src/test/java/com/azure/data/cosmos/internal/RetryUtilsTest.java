// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.GoneException;
import com.azure.data.cosmos.internal.directconnectivity.StoreResponse;
import com.azure.data.cosmos.internal.directconnectivity.StoreResponseValidator;
import com.azure.data.cosmos.internal.IRetryPolicy.ShouldRetryResult;
import io.reactivex.subscribers.TestSubscriber;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class RetryUtilsTest {
    IRetryPolicy retryPolicy;
    Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<StoreResponse>> callbackMethod;
    Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<StoreResponse>> inBackoffAlternateCallbackMethod;
    private static final Duration minBackoffForInBackoffCallback = Duration.ofMillis(10);
    private static final int TIMEOUT = 30000;
    private static final Duration BACK_OFF_DURATION = Duration.ofMillis(20);
    private StoreResponse storeResponse;

    @BeforeClass(groups = { "unit" })
    public void beforeClass() throws Exception {
        retryPolicy = Mockito.mock(IRetryPolicy.class);
        callbackMethod = Mockito.mock(Function.class);
        inBackoffAlternateCallbackMethod = Mockito.mock(Function.class);
        storeResponse = getStoreResponse();
    }

    /**
     * This method will make sure we are throwing original exception in case of
     * ShouldRetryResult.noRetry() instead of Single.error(null).
     */
    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void toRetryWithAlternateFuncWithNoRetry() {
        Function<Throwable, Mono<StoreResponse>> onErrorFunc = RetryUtils.toRetryWithAlternateFunc(callbackMethod,
                retryPolicy, inBackoffAlternateCallbackMethod, minBackoffForInBackoffCallback);
        Mockito.when(retryPolicy.shouldRetry(Matchers.any())).thenReturn(Mono.just(ShouldRetryResult.noRetry()));
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
                retryPolicy, null, minBackoffForInBackoffCallback);

        toggleMockFuncBtwFailureSuccess(callbackMethod);
        Mockito.when(retryPolicy.shouldRetry(Matchers.any()))
                .thenReturn(Mono.just(ShouldRetryResult.retryAfter(BACK_OFF_DURATION)));
        Mono<StoreResponse> response = onErrorFunc.apply(new GoneException());
        StoreResponseValidator validator = StoreResponseValidator.create().withStatus(storeResponse.getStatus())
                .withContent(storeResponse.getResponseBody()).build();
        validateSuccess(response, validator, TIMEOUT);
        Mockito.verify(callbackMethod, Mockito.times(4)).apply(Matchers.any());
    }

    /**
     * This method will test retries on inBackoffAlternateCallbackMethod, eventually
     * returning success response after some failures and making sure it failed for
     * at least specific number before passing.
     */
    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void toRetryWithAlternateFuncTestingMethodTwo() {
        Function<Throwable, Mono<StoreResponse>> onErrorFunc = RetryUtils.toRetryWithAlternateFunc(callbackMethod,
                retryPolicy, inBackoffAlternateCallbackMethod, minBackoffForInBackoffCallback);
        Mockito.when(callbackMethod.apply(Matchers.any())).thenReturn(Mono.error(new GoneException()));
        toggleMockFuncBtwFailureSuccess(inBackoffAlternateCallbackMethod);
        Mockito.when(retryPolicy.shouldRetry(Matchers.any()))
                .thenReturn(Mono.just(ShouldRetryResult.retryAfter(BACK_OFF_DURATION)));
        Mono<StoreResponse> response = onErrorFunc.apply(new GoneException());
        StoreResponseValidator validator = StoreResponseValidator.create().withStatus(storeResponse.getStatus())
                .withContent(storeResponse.getResponseBody()).build();
        validateSuccess(response, validator, TIMEOUT);
        Mockito.verify(inBackoffAlternateCallbackMethod, Mockito.times(4)).apply(Matchers.any());
    }

    private void validateFailure(Mono<StoreResponse> single, long timeout, Class<? extends Throwable> class1) {

        TestSubscriber<StoreResponse> testSubscriber = new TestSubscriber<>();
        single.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotComplete();
        testSubscriber.assertTerminated();
        assertThat(testSubscriber.errorCount()).isEqualTo(1);
        if (!(testSubscriber.getEvents().get(1).get(0).getClass().equals(class1))) {
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
        Mockito.when(method.apply(Matchers.any())).thenAnswer(new Answer<Mono<StoreResponse>>() {

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

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.directconnectivity.AddressSelector;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class BackoffRetryUtility {

    // transforms a retryFunc to a function which can be used by Observable.retryWhen(.)
    // also it invokes preRetryCallback prior to doing retry.
    public static final Quadruple<Boolean, Boolean, Duration, Integer> InitialArgumentValuePolicyArg = Quadruple.with(false, false,
            Duration.ofSeconds(60), 0);

    // a helper method for invoking callback method given the retry policy.
    // it also invokes the pre retry callback prior to retrying

    // a helper method for invoking callback method given the retry policy

    // a helper method for invoking callback method given the retry policy
    static public <T> Mono<T> executeRetry(Callable<Mono<T>> callbackMethod,
                                           IRetryPolicy retryPolicy) {

        return Mono.defer(() -> {
            try {
                return callbackMethod.call();
            } catch (Exception e) {
                return Mono.error(e);
            }
        }).retryWhen(Retry.withThrowable(RetryUtils.toRetryWhenFunc(retryPolicy)));
    }

    static public <T> Flux<T> fluxExecuteRetry(Callable<Flux<T>> callbackMethod, IRetryPolicy retryPolicy) {

        return Flux.defer(() -> {
            try {
                return callbackMethod.call();
            } catch (Exception e) {
                return Flux.error(e);
            }
        }).retryWhen(Retry.withThrowable(RetryUtils.toRetryWhenFunc(retryPolicy)));
    }

    static public <T> Mono<T> executeAsync(
        Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<T>> callbackMethod, IRetryPolicy retryPolicy,
        Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<T>> inBackoffAlternateCallbackMethod,
        Duration minBackoffForInBackoffCallback,
        RxDocumentServiceRequest request,
        AddressSelector addressSelector) {

        return Mono.defer(() -> {
            return callbackMethod.apply(InitialArgumentValuePolicyArg).onErrorResume(
                RetryUtils.toRetryWithAlternateFunc(
                    callbackMethod,
                    retryPolicy,
                    inBackoffAlternateCallbackMethod,
                    minBackoffForInBackoffCallback,
                    request,
                    addressSelector));
        });
    }

}

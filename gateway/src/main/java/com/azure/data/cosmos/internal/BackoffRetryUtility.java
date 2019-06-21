/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos.internal;

import reactor.core.publisher.Mono;

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
            // TODO: is defer required?
            try {
                return callbackMethod.call();
            } catch (Exception e) {
                return Mono.error(e);
            }
        }).retryWhen(RetryUtils.toRetryWhenFunc(retryPolicy));
    }

    static public <T> Mono<T> executeAsync(
            Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<T>> callbackMethod, IRetryPolicy retryPolicy,
            Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<T>> inBackoffAlternateCallbackMethod,
            Duration minBackoffForInBackoffCallback) {

        return Mono.defer(() -> {
            // TODO: is defer required?
            return callbackMethod.apply(InitialArgumentValuePolicyArg).onErrorResume(
                    RetryUtils.toRetryWithAlternateFunc(callbackMethod, retryPolicy, inBackoffAlternateCallbackMethod,minBackoffForInBackoffCallback));
        });
    }

}

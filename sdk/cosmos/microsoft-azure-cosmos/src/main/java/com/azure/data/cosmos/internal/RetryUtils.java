// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Function;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class RetryUtils {
    private final static Logger logger = LoggerFactory.getLogger(BackoffRetryUtility.class);

    static Function<Flux<Throwable>, Flux<Long>> toRetryWhenFunc(IRetryPolicy policy) {
        return throwableFlux -> throwableFlux.flatMap(t -> {
            Exception e = Utils.as(t, Exception.class);
            if (e == null) {
                return Flux.error(t);
            }
            Flux<IRetryPolicy.ShouldRetryResult> shouldRetryResultFlux = policy.shouldRetry(e).flux();
            return shouldRetryResultFlux.flatMap(s -> {

                if (s.backOffTime != null) {
                    return Mono.delay(Duration.ofMillis(s.backOffTime.toMillis())).flux();
                } else if (s.exception != null) {
                    return Flux.error(s.exception);
                } else {
                    // NoRetry return original failure
                    return Flux.error(t);
                }
            });
        });
    }

    /**
     * This method will be called after getting error on callbackMethod , and then keep trying between
     * callbackMethod and inBackoffAlternateCallbackMethod until success or as stated in
     * retry policy.
     * @param callbackMethod The callbackMethod
     * @param retryPolicy Retry policy
     * @param inBackoffAlternateCallbackMethod The inBackoffAlternateCallbackMethod
     * @param minBackoffForInBackoffCallback Minimum backoff for InBackoffCallbackMethod
     * @return
     */

    public static <T> Function<Throwable, Mono<T>> toRetryWithAlternateFunc(Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<T>> callbackMethod, IRetryPolicy retryPolicy, Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<T>> inBackoffAlternateCallbackMethod, Duration minBackoffForInBackoffCallback) {
        return throwable -> {
            Exception e = Utils.as(throwable, Exception.class);
            if (e == null) {
                return Mono.error(throwable);
            }

            Flux<IRetryPolicy.ShouldRetryResult> shouldRetryResultFlux = retryPolicy.shouldRetry(e).flux();
            return shouldRetryResultFlux.flatMap(shouldRetryResult -> {
                if (!shouldRetryResult.shouldRetry) {
                    if(shouldRetryResult.exception == null) {
                        return Mono.error(e);
                    } else {
                        return Mono.error(shouldRetryResult.exception);
                    }
                }

                if (inBackoffAlternateCallbackMethod != null
                        && shouldRetryResult.backOffTime.compareTo(minBackoffForInBackoffCallback) > 0) {
                    StopWatch stopwatch = new StopWatch();
                    startStopWatch(stopwatch);
                    return inBackoffAlternateCallbackMethod.apply(shouldRetryResult.policyArg)
                            .onErrorResume(recurrsiveWithAlternateFunc(callbackMethod, retryPolicy,
                                    inBackoffAlternateCallbackMethod, shouldRetryResult, stopwatch,
                                    minBackoffForInBackoffCallback));
                } else {
                    return recurrsiveFunc(callbackMethod, retryPolicy, inBackoffAlternateCallbackMethod,
                            shouldRetryResult, minBackoffForInBackoffCallback)
                            .delaySubscription(Duration.ofMillis(shouldRetryResult.backOffTime.toMillis()));
                }
            }).single();
        };
    }

    private static <T> Mono<T> recurrsiveFunc(Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<T>> callbackMethod, IRetryPolicy retryPolicy, Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<T>> inBackoffAlternateCallbackMethod, IRetryPolicy.ShouldRetryResult shouldRetryResult, Duration minBackoffForInBackoffCallback) {
        return callbackMethod.apply(shouldRetryResult.policyArg).onErrorResume(toRetryWithAlternateFunc(
                callbackMethod, retryPolicy, inBackoffAlternateCallbackMethod, minBackoffForInBackoffCallback));
    }

    private static <T> Function<Throwable, Mono<T>> recurrsiveWithAlternateFunc(Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<T>> callbackMethod, IRetryPolicy retryPolicy, Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<T>> inBackoffAlternateCallbackMethod, IRetryPolicy.ShouldRetryResult shouldRetryResult, StopWatch stopwatch, Duration minBackoffForInBackoffCallback) {
        return throwable -> {
            Exception e = Utils.as(throwable, Exception.class);
            if (e == null) {
                return Mono.error(throwable);
            }

            stopStopWatch(stopwatch);
            logger.info("Failed inBackoffAlternateCallback with {}, proceeding with retry. Time taken: {}ms",
                    e.toString(), stopwatch.getTime());
            Duration backoffTime = shouldRetryResult.backOffTime.toMillis() > stopwatch.getTime()
                    ? Duration.ofMillis(shouldRetryResult.backOffTime.toMillis() - stopwatch.getTime())
                    : Duration.ZERO;
            return recurrsiveFunc(callbackMethod, retryPolicy, inBackoffAlternateCallbackMethod, shouldRetryResult,
                    minBackoffForInBackoffCallback)
                    .delaySubscription(Flux.just(0L).delayElements(Duration.ofMillis(backoffTime.toMillis())));
        };
    }

    private static void stopStopWatch(StopWatch stopwatch) {
        synchronized (stopwatch) {
            stopwatch.stop();
        }
    }

    private static void startStopWatch(StopWatch stopwatch) {
        synchronized (stopwatch) {
            stopwatch.start();
        }
    }
}

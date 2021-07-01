// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.apachecommons.lang.time.StopWatch;
import com.azure.cosmos.implementation.directconnectivity.AddressSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
            RetryContext retryContext = policy.getRetryContext();
            if (retryContext != null) {
                retryContext.captureStartTimeIfNotSet();
            }

            Flux<ShouldRetryResult> shouldRetryResultFlux = policy.shouldRetry(e).flux();
            return shouldRetryResultFlux.flatMap(s -> {
                CosmosException clientException = Utils.as(e, CosmosException.class);
                    addStatusSubStatusCodeOnRetryContext(retryContext, clientException, s.nonRelatedException);

                if (s.backOffTime != null) {
                    return Mono.delay(Duration.ofMillis(s.backOffTime.toMillis()), CosmosSchedulers.COSMOS_PARALLEL).flux();
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
    public static <T> Function<Throwable, Mono<T>> toRetryWithAlternateFunc(Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<T>> callbackMethod,
                                                                            IRetryPolicy retryPolicy,
                                                                            Function<Quadruple<Boolean, Boolean, Duration, Integer>,
                                                                            Mono<T>> inBackoffAlternateCallbackMethod, Duration minBackoffForInBackoffCallback,
                                                                            RxDocumentServiceRequest rxDocumentServiceRequest,
                                                                            AddressSelector addressSelector) {
        return throwable -> {
            RetryContext retryContext = retryPolicy.getRetryContext();
            if (retryContext != null) {
                retryContext.captureStartTimeIfNotSet();
                if (retryContext.getRetryCount() > 0) {
                    retryContext.updateEndTime();
                }
            }

            Exception e = Utils.as(throwable, Exception.class);
            if (e == null) {
                return Mono.error(throwable);
            }

            Mono<ShouldRetryResult> shouldRetryResultFlux = retryPolicy.shouldRetry(e);
            return shouldRetryResultFlux.flatMap(shouldRetryResult -> {
                if (retryContext != null) {
                    CosmosException clientException = Utils.as(e, CosmosException.class);
                    addStatusSubStatusCodeOnRetryContext(retryContext, clientException, shouldRetryResult.nonRelatedException);
                    retryContext.updateEndTime();
                }

                if (!shouldRetryResult.shouldRetry) {
                    if (retryContext != null) {
                        retryContext.updateEndTime();
                    }

                    final Throwable errorToReturn = shouldRetryResult.exception != null ? shouldRetryResult.exception : e;
                    final Mono<T> failure = Mono.error(errorToReturn);

                    if (shouldRetryResult.policyArg != null) {
                        Boolean forceAddressRefresh = shouldRetryResult.policyArg.getValue0();

                        if (forceAddressRefresh != null && forceAddressRefresh) {
                            startBackgroundAddressRefresh(rxDocumentServiceRequest, addressSelector);
                        }
                    }

                    return failure;
                }

                if (inBackoffAlternateCallbackMethod != null
                        && shouldRetryResult.backOffTime.compareTo(minBackoffForInBackoffCallback) > 0) {
                    StopWatch stopwatch = new StopWatch();
                    startStopWatch(stopwatch);
                    return inBackoffAlternateCallbackMethod.apply(shouldRetryResult.policyArg)
                            .onErrorResume(recursiveWithAlternateFunc(callbackMethod, retryPolicy,
                                    inBackoffAlternateCallbackMethod, shouldRetryResult, stopwatch,
                                    minBackoffForInBackoffCallback, rxDocumentServiceRequest, addressSelector));
                } else if (shouldRetryResult.backOffTime == Duration.ZERO) {
                    return recursiveFunc(callbackMethod, retryPolicy, inBackoffAlternateCallbackMethod,
                            shouldRetryResult, minBackoffForInBackoffCallback, rxDocumentServiceRequest, addressSelector);
                } else {
                    return recursiveFunc(callbackMethod, retryPolicy, inBackoffAlternateCallbackMethod,
                        shouldRetryResult, minBackoffForInBackoffCallback, rxDocumentServiceRequest, addressSelector)
                        .delaySubscription(
                            Duration.ofMillis(shouldRetryResult.backOffTime.toMillis()),
                            CosmosSchedulers.COSMOS_PARALLEL);
                }
            });
        };
    }

    private static void startBackgroundAddressRefresh(
        RxDocumentServiceRequest request,
        AddressSelector addressSelector) {

        addressSelector.resolveAddressesAsync(request, true)
                       .publishOn(Schedulers.boundedElastic())
                       .subscribe(
                           r -> {
                           },
                           e -> logger.warn(
                               "Background refresh of addresses failed with {}", e.getMessage(), e)
                       );
    }

    private static <T> Mono<T> recursiveFunc(
        Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<T>> callbackMethod,
        IRetryPolicy retryPolicy,
        Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<T>> inBackoffAlternateCallbackMethod,
        ShouldRetryResult shouldRetryResult,
        Duration minBackoffForInBackoffCallback,
        RxDocumentServiceRequest rxDocumentServiceRequest,
        AddressSelector addressSelector) {

        return callbackMethod.apply(shouldRetryResult.policyArg).onErrorResume(toRetryWithAlternateFunc(
                callbackMethod, retryPolicy, inBackoffAlternateCallbackMethod, minBackoffForInBackoffCallback, rxDocumentServiceRequest, addressSelector));
    }

    private static <T> Function<Throwable, Mono<T>> recursiveWithAlternateFunc(
        Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<T>> callbackMethod,
        IRetryPolicy retryPolicy,
        Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<T>> inBackoffAlternateCallbackMethod,
        ShouldRetryResult shouldRetryResult,
        StopWatch stopwatch,
        Duration minBackoffForInBackoffCallback,
        RxDocumentServiceRequest rxDocumentServiceRequest,
        AddressSelector addressSelector) {

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
            return recursiveFunc(callbackMethod, retryPolicy, inBackoffAlternateCallbackMethod, shouldRetryResult,
                    minBackoffForInBackoffCallback, rxDocumentServiceRequest, addressSelector)
                    .delaySubscription(
                        Duration.ofMillis(backoffTime.toMillis()),
                        CosmosSchedulers.COSMOS_PARALLEL);
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

    private static void addStatusSubStatusCodeOnRetryContext(RetryContext retryContext,
                                                             CosmosException clientException,
                                                             boolean isNonRelatedException) {
        if (!isNonRelatedException) {
            if (retryContext != null && clientException != null) {
                retryContext.addStatusAndSubStatusCode(clientException.getStatusCode(),
                    clientException.getSubStatusCode());
            }
        }
    }
}

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

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Single;
import rx.functions.Func1;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class RetryUtils {
    private final static Logger logger = LoggerFactory.getLogger(BackoffRetryUtility.class);

    public static Func1<Observable<? extends Throwable>, Observable<Long>> toRetryWhenFunc(IRetryPolicy policy) {
        return new Func1<Observable<? extends Throwable>, Observable<Long>>() {

            @Override
            public Observable<Long> call(Observable<? extends Throwable> throwableObs) {
                return throwableObs.flatMap( t -> {
                    Exception e = Utils.as(t, Exception.class);
                    if (e == null) {
                        return Observable.error(t);
                    }

                    return policy.shouldRetry(e).toObservable().flatMap(s -> {

                        if (s.backOffTime != null) {
                            return Observable.timer(s.backOffTime.toMillis(), TimeUnit.MILLISECONDS);
                        } else if (s.exception != null) {
                            return Observable.error(s.exception);
                        } else {
                            // NoRetry return original failure
                            return Observable.error(t);
                        }
                    });
                });
            }
        };
    }

    /**
     * This method will be called after getting error on callbackMethod , and then keep trying between
     * callbackMethod and inBackoffAlternateCallbackMethod until success or as stated in
     * retry policy.
     * @param callbackMethod The callbackMethod
     * @param policy Retry policy
     * @param inBackoffAlternateCallbackMethod The inBackoffAlternateCallbackMethod
     * @param minBackoffForInBackoffCallback Minimum backoff for InBackoffCallbackMethod
     * @return
     */
    static <T> Func1<Throwable, Single<T>> toRetryWithAlternateFunc(
            Func1<Quadruple<Boolean, Boolean, Duration, Integer>, Single<T>> callbackMethod, IRetryPolicy policy,
            Func1<Quadruple<Boolean, Boolean, Duration, Integer>, Single<T>> inBackoffAlternateCallbackMethod,
            Duration minBackoffForInBackoffCallback) {
        return new Func1<Throwable, Single<T>>() {

            @Override
            public Single<T> call(Throwable t) {
                Exception e = Utils.as(t, Exception.class);
                if (e == null) {
                    return Single.error(t);
                }

                return policy.shouldRetry(e).flatMap(shouldRetryResult -> {
                    if (!shouldRetryResult.shouldRetry) {
                        if(shouldRetryResult.exception == null) {
                            return Single.error(e);
                        } else {
                            return Single.error(shouldRetryResult.exception);
                        }
                    }

                    if (inBackoffAlternateCallbackMethod != null
                            && shouldRetryResult.backOffTime.compareTo(minBackoffForInBackoffCallback) > 0) {
                        StopWatch stopwatch = new StopWatch();
                        startStopWatch(stopwatch);
                        return inBackoffAlternateCallbackMethod.call(shouldRetryResult.policyArg)
                                .onErrorResumeNext(recurrsiveWithAlternateFunc(callbackMethod, policy,
                                        inBackoffAlternateCallbackMethod, shouldRetryResult, stopwatch,
                                        minBackoffForInBackoffCallback));
                    } else {
                        return recurrsiveFunc(callbackMethod, policy, inBackoffAlternateCallbackMethod,
                                shouldRetryResult, minBackoffForInBackoffCallback)
                                        .delaySubscription(Observable.timer(shouldRetryResult.backOffTime.toMillis(),
                                                TimeUnit.MILLISECONDS));
                    }
                });
            }
        };

    }

    private static <T> Single<T> recurrsiveFunc(
            Func1<Quadruple<Boolean, Boolean, Duration, Integer>, Single<T>> callbackMethod, IRetryPolicy policy,
            Func1<Quadruple<Boolean, Boolean, Duration, Integer>, Single<T>> inBackoffAlternateCallbackMethod,
            IRetryPolicy.ShouldRetryResult shouldRetryResult, Duration minBackoffForInBackoffCallback) {
        return callbackMethod.call(shouldRetryResult.policyArg).onErrorResumeNext(toRetryWithAlternateFunc(
                callbackMethod, policy, inBackoffAlternateCallbackMethod, minBackoffForInBackoffCallback));

    }

    private static <T> Func1<Throwable, Single<T>> recurrsiveWithAlternateFunc(
            Func1<Quadruple<Boolean, Boolean, Duration, Integer>, Single<T>> callbackMethod, IRetryPolicy policy,
            Func1<Quadruple<Boolean, Boolean, Duration, Integer>, Single<T>> inBackoffAlternateCallbackMethod,
            IRetryPolicy.ShouldRetryResult shouldRetryResult, StopWatch stopwatch, Duration minBackoffForInBackoffCallback) {
        return new Func1<Throwable, Single<T>>() {

            @Override
            public Single<T> call(Throwable t) {

                Exception e = Utils.as(t, Exception.class);
                if (e == null) {
                    return Single.error(t);
                }

                stopStopWatch(stopwatch);
                logger.info("Failed inBackoffAlternateCallback with {}, proceeding with retry. Time taken: {}ms",
                        e.toString(), stopwatch.getTime());
                Duration backoffTime = shouldRetryResult.backOffTime.toMillis() > stopwatch.getTime()
                        ? Duration.ofMillis(shouldRetryResult.backOffTime.toMillis() - stopwatch.getTime())
                        : Duration.ZERO;
                return recurrsiveFunc(callbackMethod, policy, inBackoffAlternateCallbackMethod, shouldRetryResult,
                        minBackoffForInBackoffCallback)
                                .delaySubscription(Observable.timer(backoffTime.toMillis(), TimeUnit.MILLISECONDS));
            }

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

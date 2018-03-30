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
package com.microsoft.azure.cosmosdb.rx.internal;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class RetryUtils {
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
}

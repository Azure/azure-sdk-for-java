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

import rx.Observable;
import rx.Single;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class BackoffRetryUtility {
    
    // transforms a retryFunc to a function which can be used by Observable.retryWhen(.)
    // also it invokes preRetryCallback prior to doing retry.
    static Func1<Observable<? extends Throwable>, Observable<Long>> toRetryWhenFunc(
            Func1<Exception, Single<Long>> retryFunc, Action1<Exception> preRetryCallback) {

        return new Func1<Observable<? extends Throwable>, Observable<Long>>() {

            @Override
            public Observable<Long> call(Observable<? extends Throwable> t) {

                return t.flatMap(f -> {
                    Exception e = Utils.as(f, Exception.class);
                    if (e instanceof Exception) {
                        if (preRetryCallback != null) {

                            // TODO: is retry callback invoked immediately on the same thread?
                            // we should verify this
                            return retryFunc.call(e).doOnSuccess(v -> preRetryCallback.call(e)).toObservable();
                        } else {
                            return retryFunc.call(e).toObservable();
                        }
                    } else {
                        return Observable.error(f);
                    }
                });
            }
        };
    }

    @SuppressWarnings("unused")
    static private <T> Single<T> executeRetry(Func0<Single<T>> callbackMethod,
            Func1<Exception, Single<Long>> callShouldRetry, Action1<Exception> preRetryCallback) {

        return Single.defer(() -> {
            return callbackMethod.call();

        }).retryWhen(toRetryWhenFunc(callShouldRetry, preRetryCallback));
    }

    // a helper method for invoking callback method given the retry policy.
    // it also invokes the pre retry callback prior to retrying
    static public <T> Single<T> executeRetry(Func0<Single<T>> callbackMethod,
            IRetryPolicy retryPolicy,
            Action1<Throwable> preRetryCallback) {

        return Single.defer(() -> {
            // TODO: is defer required?
            return callbackMethod.call();
        }).retryWhen(RetryUtils.toRetryWhenFunc(retryPolicy));
    }

    // a helper method for invoking callback method given the retry policy
    static public <T> Single<T> executeRetry(Func0<Single<T>> callbackMethod,
            IRetryPolicy retryPolicy) {

        return Single.defer(() -> {
            // TODO: is defer required?
            return callbackMethod.call();
        }).retryWhen(RetryUtils.toRetryWhenFunc(retryPolicy));
    }
        
    private BackoffRetryUtility() {}
}

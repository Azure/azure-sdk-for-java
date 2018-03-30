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
package com.microsoft.azure.cosmosdb.rx.internal.caches;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Single;
import rx.functions.Func0;

class AsyncLazy<TValue> {

    private final static Logger logger = LoggerFactory.getLogger(AsyncLazy.class);

    private final Single<TValue> single;

    private volatile boolean succeeded;
    private volatile boolean failed;

    public AsyncLazy(Func0<Single<TValue>> func) {
        this(Single.defer(() -> {
            logger.debug("using Func0<Single<TValue>> {}", func);
            return func.call();
        }));
    }

    public AsyncLazy(TValue value) {
        this.single = Single.just(value);
        this.succeeded = true;
        this.failed = false;
    }

    private AsyncLazy(Single<TValue> single) {
        logger.debug("constructor");
        this.single = single
                .doOnSuccess(v -> this.succeeded = true)
                .doOnError(e -> this.failed = true)
                .cache();
    }

    public Single<TValue> single() {
        return single;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public boolean isFaulted() {
        return failed;
    }
}

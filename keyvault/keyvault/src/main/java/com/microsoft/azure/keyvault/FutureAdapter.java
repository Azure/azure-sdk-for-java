/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.microsoft.azure.keyvault;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

abstract class FutureAdapter<A, B> implements Future<B> {

    private final Future<A> inner;

    protected FutureAdapter(Future<A> inner) {
        this.inner = inner;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return inner.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return inner.isCancelled();
    }

    @Override
    public boolean isDone() {
        return inner.isDone();
    }

    @Override
    public B get() throws InterruptedException, ExecutionException {
        try {
            return translate(inner.get());
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public B get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            return translate(inner.get(timeout, unit));
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
    }

    protected abstract B translate(A a) throws IOException;

}
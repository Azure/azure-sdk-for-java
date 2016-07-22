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

package com.microsoft.azure.keyvault.extensions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class FutureImmediate<T> extends FutureBase<T> {

    private final T _result;

    FutureImmediate(T result) {
        super(true);

        _result = result;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {

        // throw if cancelled
        if (isCancelled()) {
            throw new InterruptedException();
        }

        return _result;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {

        // throw if cancelled
        if (isCancelled()) {
            throw new InterruptedException();
        }

        return _result;
    }
}

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

package com.microsoft.azure.cosmosdb.internal.query;

import java.util.concurrent.ExecutionException;

class ExceptionHelper {

    private ExceptionHelper() {}

    public static Throwable unwrap(Throwable e) {
        if (e.getCause() == null) {
            return e;
        }
        if (e instanceof IllegalStateException || e instanceof ExecutionException) {
            return unwrap(e.getCause());
        }
        return e;
    }

    public static Throwable unwrapIllegalStateException(Exception e) {
        if (e instanceof IllegalStateException && e.getCause() != null) {
            return e.getCause();
        }
        return e;
    }

    public static Throwable unwrapExecutionException(Exception e) {
        if (e instanceof RuntimeException && e.getCause() != null) {
            return e.getCause();
        }
        return e;
    }

    public static RuntimeException toRuntimeException(Throwable e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        throw new IllegalStateException(e);
    }

}

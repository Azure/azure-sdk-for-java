// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.query;

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

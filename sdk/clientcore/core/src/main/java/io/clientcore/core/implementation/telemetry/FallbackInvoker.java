// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.telemetry;

import io.clientcore.core.implementation.ReflectiveInvoker;

import java.util.function.Consumer;

public class FallbackInvoker {
    private final ReflectiveInvoker inner;
    private final Object fallback;
    private final Consumer<Throwable> errorCallback;

    public FallbackInvoker(ReflectiveInvoker inner, Consumer<Throwable> errorCallback) {
        this(inner, null, errorCallback);
    }

    public FallbackInvoker(ReflectiveInvoker inner, Object fallback, Consumer<Throwable> errorCallback) {
        this.inner = inner;
        this.fallback = fallback;
        this.errorCallback = errorCallback;
    }

    public Object invoke() {
        try {
            return inner.invoke();
        } catch (Throwable t) {
            errorCallback.accept(t);
        }
        return fallback;
    }

    public Object invoke(Object argOrTarget) {
        try {
            return inner.invoke(argOrTarget);
        } catch (Throwable t) {
            errorCallback.accept(t);
        }
        return fallback;
    }

    public Object invoke(Object argOrTarget, Object arg1) {
        try {
            return inner.invoke(argOrTarget, arg1);
        } catch (Throwable t) {
            errorCallback.accept(t);
        }
        return fallback;
    }

    public Object invoke(Object argOrTarget, Object arg1, Object arg2) {
        try {
            return inner.invoke(argOrTarget, arg1, arg2);
        } catch (Throwable t) {
            errorCallback.accept(t);
        }
        return fallback;
    }

    public Object invoke(Object argOrTarget, Object arg1, Object arg2, Object arg3) {
        try {
            return inner.invoke(argOrTarget, arg1, arg2, arg3);
        } catch (Throwable t) {
            errorCallback.accept(t);
        }
        return fallback;
    }

    public int getParameterCount() {
        return inner.getParameterCount();
    }
}

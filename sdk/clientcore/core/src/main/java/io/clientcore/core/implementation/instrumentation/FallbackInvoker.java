// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation;

import io.clientcore.core.implementation.ReflectiveInvoker;

import java.util.function.Consumer;

/**
 * A wrapper around a {@link ReflectiveInvoker} that provides a fallback value if the invocation fails
 * and suppresses exceptions.
 */
public class FallbackInvoker {
    private final ReflectiveInvoker inner;
    private final Object fallback;
    private final Consumer<Throwable> errorCallback;

    /**
     * Creates a new instance of {@link FallbackInvoker}.
     * @param inner the inner invoker
     * @param errorCallback the error callback
     */
    public FallbackInvoker(ReflectiveInvoker inner, Consumer<Throwable> errorCallback) {
        this(inner, null, errorCallback);
    }

    /**
     * Creates a new instance of {@link FallbackInvoker}.
     *
     * @param inner the inner invoker
     * @param fallback the fallback value
     * @param errorCallback the error callback
     */
    public FallbackInvoker(ReflectiveInvoker inner, Object fallback, Consumer<Throwable> errorCallback) {
        this.inner = inner;
        this.fallback = fallback;
        this.errorCallback = errorCallback;
    }

    /**
     * Invokes the inner invoker and returns the fallback value if the invocation fails.
     * @return the result of the invocation or the fallback value
     */
    public Object invoke() {
        try {
            return inner.invoke();
        } catch (Throwable t) {
            errorCallback.accept(t);
        }
        return fallback;
    }

    /**
     * Invokes the inner invoker and returns the fallback value if the invocation fails.
     * @param argOrTarget the argument or target
     * @return the result of the invocation or the fallback value
     */
    public Object invoke(Object argOrTarget) {
        try {
            return inner.invoke(argOrTarget);
        } catch (Throwable t) {
            errorCallback.accept(t);
        }
        return fallback;
    }

    /**
     * Invokes the inner invoker and returns the fallback value if the invocation fails.
     * @param argOrTarget the argument or target
     * @param arg1 the first argument
     * @return the result of the invocation or the fallback value
     */
    public Object invoke(Object argOrTarget, Object arg1) {
        try {
            return inner.invoke(argOrTarget, arg1);
        } catch (Throwable t) {
            errorCallback.accept(t);
        }
        return fallback;
    }

    /**
     * Invokes the inner invoker and returns the fallback value if the invocation fails.
     * @param argOrTarget the argument or target
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @return the result of the invocation or the fallback value
     */
    public Object invoke(Object argOrTarget, Object arg1, Object arg2) {
        try {
            return inner.invoke(argOrTarget, arg1, arg2);
        } catch (Throwable t) {
            errorCallback.accept(t);
        }
        return fallback;
    }

    /**
     * Invokes the inner invoker and returns the fallback value if the invocation fails.
     * @param argOrTarget the argument or target
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @param arg3 the third argument
     * @return the result of the invocation or the fallback value
     */
    public Object invoke(Object argOrTarget, Object arg1, Object arg2, Object arg3) {
        try {
            return inner.invoke(argOrTarget, arg1, arg2, arg3);
        } catch (Throwable t) {
            errorCallback.accept(t);
        }
        return fallback;
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel;

import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.instrumentation.logging.ClientLogger;

/**
 * A wrapper around a {@link ReflectiveInvoker} that provides a fallback value if the invocation fails,
 * reports the error and suppresses exceptions.
 */
public class FallbackInvoker {
    private final ReflectiveInvoker inner;
    private final Object fallback;
    private final ClientLogger logger;

    /**
     * Creates a new instance of {@link FallbackInvoker}.
     * @param inner the inner invoker
     * @param logger the logger to log error on.
     */
    public FallbackInvoker(ReflectiveInvoker inner, ClientLogger logger) {
        this(inner, null, logger);
    }

    /**
     * Creates a new instance of {@link FallbackInvoker}.
     *
     * @param inner the inner invoker
     * @param fallback the fallback value
     * @param logger the logger to log error on.
     */
    public FallbackInvoker(ReflectiveInvoker inner, Object fallback, ClientLogger logger) {
        this.inner = inner;
        this.fallback = fallback;
        this.logger = logger;
    }

    /**
     * Invokes the inner invoker and returns the fallback value if the invocation fails.
     *
     * @return the result of the invocation or the fallback value
     */
    public Object invoke() {
        try {
            return inner.invoke();
        } catch (Throwable t) {
            OTelInitializer.runtimeError(logger, t);
        }
        return fallback;
    }

    /**
     * Invokes the inner invoker and returns the fallback value if the invocation fails.
     *
     * @param argOrTarget the argument or target
     * @return the result of the invocation or the fallback value
     */
    public Object invoke(Object argOrTarget) {
        try {
            return inner.invoke(argOrTarget);
        } catch (Throwable t) {
            OTelInitializer.runtimeError(logger, t);
        }
        return fallback;
    }

    /**
     * Invokes the inner invoker and returns the fallback value if the invocation fails.
     *
     * @param argOrTarget the argument or target
     * @param arg1 the first argument
     * @return the result of the invocation or the fallback value
     */
    public Object invoke(Object argOrTarget, Object arg1) {
        try {
            return inner.invoke(argOrTarget, arg1);
        } catch (Throwable t) {
            OTelInitializer.runtimeError(logger, t);
        }
        return fallback;
    }

    /**
     * Invokes the inner invoker and returns the fallback value if the invocation fails.
     *
     * @param argOrTarget the argument or target
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @return the result of the invocation or the fallback value
     */
    public Object invoke(Object argOrTarget, Object arg1, Object arg2) {
        try {
            return inner.invoke(argOrTarget, arg1, arg2);
        } catch (Throwable t) {
            OTelInitializer.runtimeError(logger, t);
        }
        return fallback;
    }

    /**
     * Invokes the inner invoker and returns the fallback value if the invocation fails.
     *
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
            OTelInitializer.runtimeError(logger, t);
        }
        return fallback;
    }
}

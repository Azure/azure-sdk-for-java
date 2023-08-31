// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.reflection;

import com.azure.core.implementation.Invoker;

import java.lang.invoke.MethodHandle;

/**
 * {@link MethodHandle}-based implementation of {@link Invoker}.
 */
final class MethodHandleInvoker implements Invoker {
    private final MethodHandle methodHandle;

    MethodHandleInvoker(MethodHandle methodHandle) {
        this.methodHandle = methodHandle;
    }

    @Override
    public Object invoke(Object obj, Object[] args) throws Throwable {
        return methodHandle.invoke(args);
    }

    @Override
    public Object invokeWithArguments(Object obj, Object... args) throws Throwable {
        return methodHandle.invokeWithArguments(args);
    }

    @Override
    public Object invokeExact(Object obj, Object... args) throws Throwable {
        return methodHandle.invokeExact(args);
    }

    @Override
    public int getParameterCount() {
        return methodHandle.type().parameterCount();
    }
}

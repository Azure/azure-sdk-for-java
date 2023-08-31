// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.reflection;

import com.azure.core.implementation.Invoker;

import java.lang.reflect.Constructor;

/**
 * {@link Constructor}-based implementation of {@link Invoker}.
 */
final class ConstructorInvoker implements Invoker {
    private final Constructor<?> constructor;

    ConstructorInvoker(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    @Override
    public Object invoke(Object obj, Object... args) throws Throwable {
        return constructor.newInstance(args);
    }

    @Override
    public Object invokeWithArguments(Object obj, Object... args) throws Throwable {
        return constructor.newInstance(args);
    }

    @Override
    public Object invokeExact(Object obj, Object... args) throws Throwable {
        return constructor.newInstance(args);
    }

    @Override
    public int getParameterCount() {
        return constructor.getParameterCount();
    }
}

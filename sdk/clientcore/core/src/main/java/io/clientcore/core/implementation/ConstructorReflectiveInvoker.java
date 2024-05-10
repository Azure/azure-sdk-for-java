// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation;

import java.lang.reflect.Constructor;

/**
 * {@link Constructor}-based implementation of {@link ReflectiveInvoker}.
 */
final class ConstructorReflectiveInvoker implements ReflectiveInvoker {
    private final Constructor<?> constructor;

    ConstructorReflectiveInvoker(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    @Override
    public Object invokeStatic(Object... args) throws Exception {
        return constructor.newInstance(args);
    }

    @Override
    public Object invokeWithArguments(Object target, Object... args) throws Exception {
        return constructor.newInstance(args);
    }

    @Override
    public int getParameterCount() {
        return constructor.getParameterCount();
    }
}

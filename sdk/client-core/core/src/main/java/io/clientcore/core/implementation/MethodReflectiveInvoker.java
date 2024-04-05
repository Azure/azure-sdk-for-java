// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation;

import java.lang.reflect.Method;

/**
 * {@link Method}-based implementation of {@link ReflectiveInvoker}.
 */
final class MethodReflectiveInvoker implements ReflectiveInvoker {
    private final Method method;

    MethodReflectiveInvoker(Method method) {
        this.method = method;
    }

    @Override
    public Object invokeStatic(Object... args) throws Exception {
        return method.invoke(null, args);
    }

    @Override
    public Object invokeWithArguments(Object target, Object... args) throws Exception {
        return method.invoke(target, args);
    }

    @Override
    public int getParameterCount() {
        return method.getParameterCount();
    }
}

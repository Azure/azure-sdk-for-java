// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation;

import java.lang.reflect.Method;

/**
 * {@link Method}-based implementation of {@link Invoker}.
 */
final class MethodInvoker implements Invoker {
    private final Method method;

    MethodInvoker(Method method) {
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

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

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
}

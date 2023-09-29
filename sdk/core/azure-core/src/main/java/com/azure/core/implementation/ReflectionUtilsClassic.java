// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Implementation for {@link ReflectionUtilsApi} using {@code java.lang.reflect} to handle reflectively invoking APIs.
 */
final class ReflectionUtilsClassic implements ReflectionUtilsApi {
    @Override
    public ReflectiveInvoker getMethodInvoker(Class<?> targetClass, Method method, boolean scopeToAzureCore) {
        return new MethodReflectiveInvoker(method);
    }

    @Override
    public ReflectiveInvoker getConstructorInvoker(Class<?> targetClass, Constructor<?> constructor, boolean scopeToAzureCore) {
        return new ConstructorReflectiveInvoker(constructor);
    }

    @Override
    public boolean isModuleBased() {
        return false;
    }
}

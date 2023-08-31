// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.reflection;

import com.azure.core.implementation.Invoker;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Implementation for {@link ReflectionUtilsApi} using {@code java.lang.reflect} to handle reflectively invoking APIs.
 */
public final class ReflectionUtilsClassic implements ReflectionUtilsApi {
    @Override
    public Invoker getMethodInvoker(Class<?> targetClass, Method method, boolean scopeToAzureCore) {
        return new MethodInvoker(method);
    }

    @Override
    public Invoker getConstructorInvoker(Class<?> targetClass, Constructor<?> constructor, boolean scopeToAzureCore) {
        return new ConstructorInvoker(constructor);
    }

    @Override
    public Invoker getSetterFieldInvoker(Class<?> targetClass, Field setter, boolean scopeToAzureCore) {
        return new FieldSetterInvoker(setter);
    }

    @Override
    public boolean isModuleBased() {
        return false;
    }
}

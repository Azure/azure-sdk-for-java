// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.reflection;

import com.azure.core.implementation.Invoker;

import java.lang.reflect.Field;

/**
 * Setter {@link Field}-based implementation of {@link Invoker}.
 */
final class FieldSetterInvoker implements Invoker {
    private final Field setterField;

    FieldSetterInvoker(Field setterField) {
        this.setterField = setterField;
    }

    @Override
    public Object invoke(Object target, Object... args) throws Throwable {
        setterField.set(target, args[0]);
        return null;
    }

    @Override
    public Object invokeWithArguments(Object target, Object... args) throws Throwable {
        setterField.set(target, args[0]);
        return null;
    }

    @Override
    public Object invokeExact(Object target, Object... args) throws Throwable {
        setterField.set(target, args[0]);
        return null;
    }

    @Override
    public int getParameterCount() {
        return 1;
    }
}

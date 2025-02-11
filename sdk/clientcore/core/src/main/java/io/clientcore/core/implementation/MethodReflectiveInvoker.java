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
    public Object invoke() throws Exception {
        return method.invoke(null);
    }

    @Override
    public Object invoke(Object argOrTarget) throws Exception {
        return method.invoke(argOrTarget);
    }

    @Override
    public Object invoke(Object argOrTarget, Object arg1) throws Exception {
        return method.invoke(argOrTarget, arg1);
    }

    @Override
    public Object invoke(Object argOrTarget, Object arg1, Object arg2) throws Exception {
        return method.invoke(argOrTarget, arg1, arg2);
    }

    @Override
    public Object invoke(Object argOrTarget, Object arg1, Object arg2, Object arg3) throws Exception {
        return method.invoke(argOrTarget, arg1, arg2, arg3);
    }

    @Override
    public int getParameterCount() {
        return method.getParameterCount();
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation;

import java.lang.reflect.Constructor;
import static io.clientcore.core.implementation.MethodHandleReflectiveInvoker.createFinalArgs;

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
        return constructor.newInstance(createFinalArgs(args));
    }

    @Override
    public Object invoke() throws Exception {
        return constructor.newInstance();
    }

    @Override
    public Object invoke(Object argOrTarget) throws Exception {
        return constructor.newInstance(argOrTarget);
    }

    @Override
    public Object invoke(Object argOrTarget, Object arg1) throws Exception {
        return constructor.newInstance(argOrTarget, arg1);
    }

    @Override
    public Object invoke(Object argOrTarget, Object arg1, Object arg2) throws Exception {
        return constructor.newInstance(argOrTarget, arg1, arg2);
    }

    @Override
    public Object invoke(Object argOrTarget, Object arg1, Object arg2, Object arg3) throws Exception {
        return constructor.newInstance(argOrTarget, arg1, arg2, arg3);
    }

    @Override
    public int getParameterCount() {
        return constructor.getParameterCount();
    }
}

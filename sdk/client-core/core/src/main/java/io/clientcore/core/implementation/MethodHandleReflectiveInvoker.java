// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation;

import java.lang.invoke.MethodHandle;

/**
 * {@link MethodHandle}-based implementation of {@link ReflectiveInvoker}.
 */
final class MethodHandleReflectiveInvoker implements ReflectiveInvoker {
    private static final Object[] NO_ARGS = new Object[0];

    private final MethodHandle methodHandle;

    MethodHandleReflectiveInvoker(MethodHandle methodHandle) {
        this.methodHandle = methodHandle;
    }

    @Override
    public Object invokeStatic(Object... args) throws Exception {
        try {
            return methodHandle.invokeWithArguments(args);
        } catch (Throwable throwable) {
            if (throwable instanceof Error) {
                throw (Error) throwable;
            } else {
                throw (Exception) throwable;
            }
        }
    }

    @Override
    public Object invokeWithArguments(Object target, Object... args) throws Exception {
        try {
            return methodHandle.invokeWithArguments(createFinalArgs(target, args));
        } catch (Throwable throwable) {
            if (throwable instanceof Error) {
                throw (Error) throwable;
            } else {
                throw (Exception) throwable;
            }
        }
    }

    @Override
    public int getParameterCount() {
        return methodHandle.type().parameterCount();
    }

    private static Object[] createFinalArgs(Object target, Object... args) {
        if (target == null && (args == null || args.length == 0)) {
            return NO_ARGS;
        }

        if (args == null || args.length == 0) {
            return new Object[] { target };
        }

        Object[] finalArgs = new Object[args.length + 1];
        finalArgs[0] = target;
        System.arraycopy(args, 0, finalArgs, 1, args.length);

        return finalArgs;
    }
}

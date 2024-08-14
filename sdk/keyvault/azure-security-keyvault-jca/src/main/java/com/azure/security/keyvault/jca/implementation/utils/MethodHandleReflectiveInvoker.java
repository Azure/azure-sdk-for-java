// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import java.lang.invoke.MethodHandle;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

/**
 * {@link MethodHandle}-based implementation of {@link ReflectiveInvoker}.
 */
final class MethodHandleReflectiveInvoker implements ReflectiveInvoker {
    private static final Logger LOGGER = Logger.getLogger(MethodHandleReflectiveInvoker.class.getName());

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
            LOGGER.log(SEVERE, "Found an issue when reflectively invoking a static method", throwable);

            if (throwable instanceof Error) {
                throw (Error) throwable;
            } else {
                throw (Exception) throwable;
            }
        }
    }

    static Object[] createFinalArgs(Object target, Object... args) {
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

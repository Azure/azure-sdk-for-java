// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Utility class for {@code AccessController} APIs.
 * <p>
 * This class exists to isolate usages of {@code java.security} APIs into a separate class. If this class has loading
 * issues, usages of {@code AccessController} APIs will be skipped.
 */
public final class AccessControllerUtils {
    private static final boolean CAN_BE_USED;

    static {
        ClassLoader classLoader = AccessControllerUtils.class.getClassLoader();
        boolean canBeUsed = false;
        try {
            Class.forName("java.security.AccessController", true, classLoader);
            Class.forName("java.security.PrivilegedAction", true, classLoader);
            Class.forName("java.security.PrivilegedExceptionAction", true, classLoader);

            canBeUsed = true;
        } catch (LinkageError | ReflectiveOperationException ignored) {
        }

        CAN_BE_USED = canBeUsed;
    }

    /**
     * Wrapper method around reflective invocations of {@code AccessController.doPrivileged(PrivilegedAction)} which
     * checks for the ability to actually run privileged actions.
     * <p>
     * If it's not possible to run actions with privileged, {@link Supplier#get()} will be called directly.
     *
     * @param <T> The return value of the action.
     * @param privilegedAction The privileged action to run.
     * @return The results of running the action.
     * @throws RuntimeException If an error occurs while running the action.
     */
    @SuppressWarnings({ "removal" })
    public static <T> T doPrivileged(Supplier<T> privilegedAction) {
        if (!CAN_BE_USED) {
            // Can't run privileged actions, invoke the Supplier directly.
            return privilegedAction.get();
        }

        return java.security.AccessController.doPrivileged((java.security.PrivilegedAction<T>) privilegedAction::get);
    }

    /**
     * Wrapper method around reflective invocations of {@code AccessController.doPrivileged(PrivilegedExceptionAction)}
     * which checks for the ability to actually run privileged actions.
     * <p>
     * If it's not possible to run actions with privileged, {@link Supplier#get()} will be called directly.
     *
     * @param <T> The return value of the action.
     * @param privilegedActionException The privileged action that can throw an {@link Exception} to run.
     * @return The results of running the action.
     * @throws Exception If an error occurs while running the action.
     */
    @SuppressWarnings({ "removal" })
    public static <T> T doPrivilegedException(Callable<T> privilegedActionException) throws Exception {
        if (!CAN_BE_USED) {
            // Can't run privileged actions, invoke the Supplier directly.
            return privilegedActionException.call();
        }

        return java.security.AccessController
            .doPrivileged((java.security.PrivilegedExceptionAction<T>) privilegedActionException::call);
    }

    private AccessControllerUtils() {
    }
}

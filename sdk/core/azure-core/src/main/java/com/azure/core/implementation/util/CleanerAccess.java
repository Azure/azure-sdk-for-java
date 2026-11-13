// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.implementation.ReflectiveInvoker;
import com.azure.core.implementation.ReflectionUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;

import java.util.concurrent.ThreadFactory;

/**
 * Cleaner access selected for the running Java version.
 */
final class CleanerAccess {
    private static final ClientLogger LOGGER = new ClientLogger(CleanerAccess.class);

    private final Object cleaner;
    private final ReflectiveInvoker register;

    private CleanerAccess(Object cleaner, ReflectiveInvoker register) {
        this.cleaner = cleaner;
        this.register = register;
    }

    static CleanerAccess create(ThreadFactory threadFactory) {
        try {
            Class<?> cleanerClass = Class.forName("java.lang.ref.Cleaner");
            Object cleaner = cleanerClass.getDeclaredMethod("create", ThreadFactory.class).invoke(null, threadFactory);
            ReflectiveInvoker register = ReflectionUtils.getMethodInvoker(cleanerClass,
                cleanerClass.getDeclaredMethod("register", Object.class, Runnable.class), false);
            return new CleanerAccess(cleaner, register);
        } catch (Exception exception) {
            LOGGER.log(LogLevel.VERBOSE, () -> "Unable to use java.lang.ref.Cleaner to manage references.", exception);
            return null;
        }
    }

    void register(Object object, Runnable cleanupAction) throws Exception {
        register.invokeWithArguments(cleaner, object, cleanupAction);
    }
}

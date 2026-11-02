// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;

import java.lang.ref.Cleaner;
import java.util.concurrent.ThreadFactory;

/**
 * Cleaner access selected for the running Java version.
 */
final class CleanerAccess {
    private static final ClientLogger LOGGER = new ClientLogger(CleanerAccess.class);

    private final Cleaner cleaner;

    private CleanerAccess(Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    static CleanerAccess create(ThreadFactory threadFactory) {
        try {
            return new CleanerAccess(Cleaner.create(threadFactory));
        } catch (RuntimeException exception) {
            LOGGER.log(LogLevel.VERBOSE, () -> "Unable to use java.lang.ref.Cleaner to manage references.", exception);
            return null;
        }
    }

    void register(Object object, Runnable cleanupAction) throws Exception {
        cleaner.register(object, cleanupAction);
    }
}

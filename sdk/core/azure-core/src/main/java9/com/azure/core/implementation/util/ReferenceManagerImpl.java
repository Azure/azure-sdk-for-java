// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.ReferenceManager;

import java.lang.ref.Cleaner;

// This is the Java 9 implementation of ReferenceManager, there is base implementation in /src/main/java.
/**
 * Implementation of {@link ReferenceManager}.
 */
public final class ReferenceManagerImpl implements ReferenceManager {
    // Base name for ResourceManager threads.
    private static final String BASE_THREAD_NAME = "azure-sdk-referencemanager";

    // Create the constant Cleaner with a simple thread factory that is used to set the name of the Cleaner thread.
    // This creates thread name consistency between Java 8 and Java 9+ implementations.
    private static final Cleaner CLEANER = Cleaner.create(r ->
        new Thread(Thread.currentThread().getThreadGroup(), r, BASE_THREAD_NAME));

    @Override
    public void register(Object object, Runnable cleanupAction) {
        CLEANER.register(object, cleanupAction);
    }

    static int getJavaImplementationMajorVersion() {
        return 9;
    }
}

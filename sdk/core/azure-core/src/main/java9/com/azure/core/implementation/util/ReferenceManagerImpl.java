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
    private static final Cleaner CLEANER = Cleaner.create();

    @Override
    public void register(Object object, Runnable cleanupAction) {
        CLEANER.register(object, cleanupAction);
    }

    static int getJavaImplementationMajorVersion() {
        return 9;
    }
}

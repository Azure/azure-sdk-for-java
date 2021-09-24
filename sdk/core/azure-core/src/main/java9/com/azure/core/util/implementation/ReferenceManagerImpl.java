// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.implementation;

import com.azure.core.util.ReferenceManager;

import java.lang.ref.Cleaner;

/**
 * Implementation of {@link ReferenceManager}.
 */
public final class ReferenceManagerImpl implements ReferenceManager {
    private static final Cleaner CLEANER = Cleaner.create();

    @Override
    public void register(Object object, Runnable cleanupAction) {
        CLEANER.register(object, cleanupAction);
    }

    public static int getJavaImplementationMajorVersion() {
        return 9;
    }
}

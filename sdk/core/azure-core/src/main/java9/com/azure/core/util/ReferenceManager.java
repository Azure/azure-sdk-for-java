// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.lang.ref.Cleaner;

/**
 * This class handles managing references to {@link Object Objects} and providing the ability to run a cleaning
 * operation once the object is no longer able to be reference.
 */
public final class ReferenceManager {
    private final Cleaner cleaner;

    /**
     * Creates a new instance of {@link ReferenceManager}.
     *
     * @return A new instance of {@link ReferenceManager}.
     */
    public static ReferenceManager create() {
        return new ReferenceManager();
    }

    /**
     * Registers the {@code object} anc the cleaning action to run once the object becomes phantom reachable.
     * <p>
     * The {@code cleanupAction} cannot have a reference to the {@code object}, otherwise the object will never be able
     * to become phantom reachable.
     * <p>
     * Exceptions thrown by {@code cleanupAction} are ignored.
     *
     * @param object The object to monitor.
     * @param cleanupAction The cleanup action to perform when the {@code object} becomes phantom reachable.
     * @throws NullPointerException If either {@code object} or {@code cleanupAction} are null.
     */
    public void register(Object object, Runnable cleanupAction) {
        cleaner.register(object, cleanupAction);
    }

    private ReferenceManager() {
        this.cleaner = Cleaner.create();
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.util.implementation.ReferenceManagerImpl;

/**
 * This interface represents the managing references to {@link Object Objects} and providing the ability to run a
 * cleaning operation once the object is no longer able to be reference.
 * <p>
 * Expected usage of this is through {@link ReferenceManager#INSTANCE}.
 */
public interface ReferenceManager {
    /**
     * The global instance of {@link ReferenceManager} that should be used to maintain object references.
     */
    ReferenceManager INSTANCE = new ReferenceManagerImpl();

    /**
     * Registers the {@code object} and the cleaning action to run once the object becomes phantom reachable.
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
    void register(Object object, Runnable cleanupAction);
}

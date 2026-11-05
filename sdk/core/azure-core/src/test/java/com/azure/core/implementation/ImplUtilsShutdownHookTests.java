// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that {@link ImplUtils#addShutdownHookSafely(Thread)} and {@link ImplUtils#removeShutdownHookSafely(Thread)}
 * tolerate the JVM already shutting down.
 * <p>
 * {@link Runtime#addShutdownHook(Thread)} and {@link Runtime#removeShutdownHook(Thread)} throw
 * {@link IllegalStateException} once shutdown has begun. The JVM cannot be put into that state from a test, so the
 * registration is passed in here the same way the production overloads pass the real one.
 */
public class ImplUtilsShutdownHookTests {
    private static final String SHUTDOWN_IN_PROGRESS = "Shutdown in progress";

    @Test
    public void addReturnsThreadWhenRegistrationSucceeds() {
        Thread shutdownThread = new Thread(() -> {
        });
        AtomicReference<Thread> registered = new AtomicReference<>();

        Thread returned = ImplUtils.addShutdownHookSafely(shutdownThread, registered::set);

        assertSame(shutdownThread, returned);
        assertSame(shutdownThread, registered.get());
    }

    @Test
    public void addReturnsNullWhenJvmIsShuttingDown() {
        Thread shutdownThread = new Thread(() -> {
        });

        Thread returned = assertDoesNotThrow(() -> ImplUtils.addShutdownHookSafely(shutdownThread, ignored -> {
            throw new IllegalStateException(SHUTDOWN_IN_PROGRESS);
        }));

        // Null tells the caller no hook was registered, so there is nothing to remove later.
        assertNull(returned);
    }

    @Test
    public void addDoesNotSwallowOtherFailures() {
        Thread shutdownThread = new Thread(() -> {
        });

        // A thread that is already registered raises IllegalArgumentException, which is a caller error rather than
        // the JVM shutting down, and must still surface.
        assertThrows(IllegalArgumentException.class, () -> ImplUtils.addShutdownHookSafely(shutdownThread, ignored -> {
            throw new IllegalArgumentException("Hook previously registered");
        }));
    }

    @Test
    public void removeDoesNotThrowWhenJvmIsShuttingDown() {
        Thread shutdownThread = new Thread(() -> {
        });

        assertDoesNotThrow(() -> ImplUtils.removeShutdownHookSafely(shutdownThread, ignored -> {
            throw new IllegalStateException(SHUTDOWN_IN_PROGRESS);
        }));
    }

    @Test
    public void removeDoesNotSwallowOtherFailures() {
        Thread shutdownThread = new Thread(() -> {
        });

        assertThrows(SecurityException.class, () -> ImplUtils.removeShutdownHookSafely(shutdownThread, ignored -> {
            throw new SecurityException("no permission");
        }));
    }

    @Test
    public void nullThreadIsIgnored() {
        AtomicReference<Boolean> called = new AtomicReference<>(false);

        assertNull(ImplUtils.addShutdownHookSafely(null, ignored -> called.set(true)));
        assertDoesNotThrow(() -> ImplUtils.removeShutdownHookSafely(null, ignored -> called.set(true)));

        assertTrue(!called.get(), "the JVM should not be touched for a null thread");
    }

    @Test
    public void realHookRoundTripIsUnchanged() {
        Thread shutdownThread = new Thread(() -> {
        });

        assertSame(shutdownThread, ImplUtils.addShutdownHookSafely(shutdownThread));
        assertDoesNotThrow(() -> ImplUtils.removeShutdownHookSafely(shutdownThread));
    }
}

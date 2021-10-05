// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * Contains utility methods that aid in testing.
 */
public final class AmqpTestUtils {

    /**
     * Configures a {@link StepVerifier} to verify the completions of a reactive stream with a given timeout {@link
     * Duration}.
     * <p>
     * If the {@code stepVerifier} doesn't complete before the given {@code timeout} completes an {@link AssertionError}
     * will be thrown.
     *
     * @param stepVerifier The {@link StepVerifier} that tests a reactive stream.
     * @param timeout How long the {@link StepVerifier} should wait for the reactive stream to complete before throwing
     * an error.
     * @return How long it took for the reactive stream to actually complete.
     * @throws AssertionError If the {@code stepVerifier} doesn't complete before {@code timeout} completes.
     */
    public static Duration verifyWithTimeout(StepVerifier stepVerifier, Duration timeout) {
        return stepVerifier.verify(timeout);
    }

    private AmqpTestUtils() {
    }
}

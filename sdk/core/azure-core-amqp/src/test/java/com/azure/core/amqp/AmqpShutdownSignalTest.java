// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class AmqpShutdownSignalTest {

    /**
     * Verifies that the correct properties are set when we create an AmqpShutdownSignal.
     */
    @Test
    public void constructor() {
        boolean isTransient = true;
        boolean isInitiatedByClient = true;
        String message = "Some message.";

        AmqpShutdownSignal shutdownSignal = new AmqpShutdownSignal(isTransient, isInitiatedByClient, message);

        assertTrue(shutdownSignal.isTransient());
        assertTrue(shutdownSignal.isInitiatedByClient());

        String contents = shutdownSignal.toString();
        assertNotNull(contents);
        assertTrue(contents.contains(message));
    }
}

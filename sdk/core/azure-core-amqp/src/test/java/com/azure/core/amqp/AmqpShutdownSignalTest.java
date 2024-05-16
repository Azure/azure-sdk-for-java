// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import org.junit.jupiter.api.Assertions;
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

        Assertions.assertTrue(shutdownSignal.isTransient());
        Assertions.assertTrue(shutdownSignal.isInitiatedByClient());

        String contents = shutdownSignal.toString();
        Assertions.assertNotNull(contents);
        Assertions.assertTrue(contents.contains(message));
    }
}

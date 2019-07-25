// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import org.junit.Assert;
import org.junit.Test;

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

        Assert.assertTrue(shutdownSignal.isTransient());
        Assert.assertTrue(shutdownSignal.isInitiatedByClient());

        String contents = shutdownSignal.toString();
        Assert.assertNotNull(contents);
        Assert.assertTrue(contents.contains(message));
    }
}

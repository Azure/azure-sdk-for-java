// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.tracing.opentelemetry.implementation;

import io.opentelemetry.trace.Status;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class AmqpTraceUtilTest {

    @Test
    public void parseUnknownStatusMessage() {
        // Act

        Status status = AmqpTraceUtil.parseStatusMessage("", null);

        // Assert
        assertNotNull(status);
        assertEquals(Status.UNKNOWN.withDescription(""), status);
    }

    @Test
    public void parseSuccessStatusMessage() {
        // Act

        Status status = AmqpTraceUtil.parseStatusMessage("success", null);

        // Assert
        assertNotNull(status);
        assertEquals(Status.OK, status);
    }

    @Test
    public void parseStatusMessageOnError() {
        // Act

        Status status = AmqpTraceUtil.parseStatusMessage("", new Error("testError"));

        // Assert
        assertNotNull(status);
        assertEquals(Status.UNKNOWN.withDescription("testError"), status);
    }
}

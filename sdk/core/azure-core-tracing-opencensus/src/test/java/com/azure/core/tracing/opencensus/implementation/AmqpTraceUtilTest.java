// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.tracing.opencensus.implementation;

import io.opencensus.trace.Status;
import org.junit.Assert;
import org.junit.Test;

public class AmqpTraceUtilTest {

    @Test
    public void parseUnknownStatusMessage() {
        // Act

        Status status = AmqpTraceUtil.parseStatusMessage("", null);

        // Assert
        Assert.assertNotNull(status);
        Assert.assertEquals(Status.UNKNOWN.withDescription(""), status);
    }

    @Test
    public void parseSuccessStatusMessage() {
        // Act

        Status status = AmqpTraceUtil.parseStatusMessage("success", null);

        // Assert
        Assert.assertNotNull(status);
        Assert.assertEquals(Status.OK, status);
    }

    @Test
    public void parseStatusMessageOnError() {
        // Act

        Status status = AmqpTraceUtil.parseStatusMessage("", new Error("testError"));

        // Assert
        Assert.assertNotNull(status);
        Assert.assertEquals(Status.UNKNOWN.withDescription("testError"), status);
    }
}

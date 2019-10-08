// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.tracing.opencensus.implementation;

import io.opencensus.trace.Status;
import org.junit.Assert;
import org.junit.Test;

public class HttpTraceUtilTest {
    @Test
    public void parseUnknownStatusCode() {
        // Act

        Status status = HttpTraceUtil.parseResponseStatus(1, null);

        // Assert
        Assert.assertNotNull(status);
        Assert.assertEquals(Status.UNKNOWN.withDescription(null), status);
    }

    @Test
    public void parseUnauthenticatedStatusCode() {
        //Arrange
        final String errorMessage = "unauthenticated test user";

        // Act
        Status status = HttpTraceUtil.parseResponseStatus(401, new Error(errorMessage));

        // Assert
        Assert.assertNotNull(status);
        Assert.assertEquals(Status.UNAUTHENTICATED.withDescription(errorMessage), status);
    }

    @Test
    public void parseNullError() {
        // Act
        Status status = HttpTraceUtil.parseResponseStatus(504, null);

        // Assert
        Assert.assertNotNull(status);
        Assert.assertEquals(Status.DEADLINE_EXCEEDED.withDescription(null), status);
    }
}

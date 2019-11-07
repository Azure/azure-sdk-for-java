// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.tracing.opencensus.implementation;

import io.opencensus.trace.Status;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HttpTraceUtilTest {
    @Test
    public void parseUnknownStatusCode() {
        // Act

        Status status = HttpTraceUtil.parseResponseStatus(1, null);

        // Assert
        Assertions.assertNotNull(status);
        Assertions.assertEquals(Status.UNKNOWN.withDescription(null), status);
    }

    @Test
    public void parseUnauthenticatedStatusCode() {
        //Arrange
        final String errorMessage = "unauthenticated test user";

        // Act
        Status status = HttpTraceUtil.parseResponseStatus(401, new Error(errorMessage));

        // Assert
        Assertions.assertNotNull(status);
        Assertions.assertEquals(Status.UNAUTHENTICATED.withDescription(errorMessage), status);
    }

    @Test
    public void parseNullError() {
        // Act
        Status status = HttpTraceUtil.parseResponseStatus(504, null);

        // Assert
        Assertions.assertNotNull(status);
        Assertions.assertEquals(Status.DEADLINE_EXCEEDED.withDescription(null), status);
    }
}

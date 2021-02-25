// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.tracing.opentelemetry.implementation;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.trace.ReadableSpan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Class to test methods of HttpTraceUtil.
 */
public class HttpTraceUtilTest {

    @Mock
    private Span parentSpan;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void parseUnknownStatusCode() {
        // Act
        HttpTraceUtil.setSpanStatus(parentSpan, 1, null);

        // Assert
        verify(parentSpan, times(1))
            .setStatus(StatusCode.UNSET);
    }

    @Test
    public void parseUnauthenticatedStatusCode() {

        // Act
        HttpTraceUtil.setSpanStatus(parentSpan, 401, null);

        // Assert
        verify(parentSpan, times(1))
            .setStatus(StatusCode.ERROR, "Unauthorized");
    }

    @Test
    public void parseNullError() {
        // Act
        ReadableSpan span2 = (ReadableSpan) HttpTraceUtil.setSpanStatus(parentSpan, 504, null);

        // Assert
        verify(parentSpan, times(1))
            .setStatus(StatusCode.ERROR, "Gateway Timeout");
    }
}

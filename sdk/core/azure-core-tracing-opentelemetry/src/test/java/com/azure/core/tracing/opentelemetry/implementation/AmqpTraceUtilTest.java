// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.tracing.opentelemetry.implementation;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class AmqpTraceUtilTest {

    @Mock
    private Span parentSpan;

    private AutoCloseable openMocks;

    @BeforeEach
    public void setup() {
        this.openMocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.openMocks.close();
    }

    @Test
    public void parseUnknownStatusMessage() {
        // Act
        AmqpTraceUtil.parseStatusMessage(parentSpan, "", null);

        // Assert
        verify(parentSpan, times(1))
            .setStatus(StatusCode.UNSET, "");

    }

    @Test
    public void parseSuccessStatusMessage() {
        // Act

        AmqpTraceUtil.parseStatusMessage(parentSpan, "success", null);

        // Assert
        verify(parentSpan, times(1))
            .setStatus(StatusCode.OK);
    }

    @Test
    public void parseStatusMessageOnError() {
        Error error = new Error("testError");

        // Act
        AmqpTraceUtil.parseStatusMessage(parentSpan, "", error);

        // Assert
        verify(parentSpan, times(1))
            .recordException(error);
    }
}

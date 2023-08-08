// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.trace.sleuth.implementation;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.sleuth.Span;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class HttpTraceUtilTests {

    @Test
    void setSpanStatusWithException() {
        Span span = mock(Span.class);
        Throwable exception = new IllegalArgumentException();
        HttpTraceUtil.setSpanStatus(span, 500, exception);
        verify(span, times(1)).error(exception);
    }

    @Test
    void setSpanStatusWithNormalStatusCode() {
        Span span = mock(Span.class);
        assertSetSpanStatus(span, 200, 0);
        assertSetSpanStatus(span, 204, 0);
        assertSetSpanStatus(span, 308, 0);
    }

    private void assertSetSpanStatus(Span span, int statusCode, int invocations) {
        HttpStatus status = HttpStatus.resolve(statusCode);
        HttpTraceUtil.setSpanStatus(span, statusCode, null);
        assertNotNull(status);
        verify(span, times(invocations)).tag("http.status_message", status.getReasonPhrase());
    }

    @Test
    void setSpanStatusUsesTagMethodWhenStatusAbnormal() {
        assertSetSpanStatus(mock(Span.class), 401, 1);
    }
}

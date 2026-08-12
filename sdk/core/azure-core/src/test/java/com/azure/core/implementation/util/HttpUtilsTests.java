// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpUtilsTests {
    @Test
    public void acceptsTextEventStreamIgnoresDisabledMediaRange() {
        assertFalse(HttpUtils.acceptsTextEventStream("text/event-stream;q=0"));
        assertTrue(HttpUtils.acceptsTextEventStream("application/json, text/event-stream;q=0.5"));
    }

    @Test
    public void acceptsTextEventStreamRequiresValidPositiveQuality() {
        assertTrue(HttpUtils.acceptsTextEventStream("text/event-stream;q=0.001"));
        assertTrue(HttpUtils.acceptsTextEventStream("text/event-stream;q=1.000"));
        assertTrue(HttpUtils.acceptsTextEventStream("text/event-stream;q=1."));
        assertFalse(HttpUtils.acceptsTextEventStream("text/event-stream;q=0.000"));
        assertFalse(HttpUtils.acceptsTextEventStream("text/event-stream;q=1.001"));
        assertFalse(HttpUtils.acceptsTextEventStream("text/event-stream;q=0.0000"));
        assertFalse(HttpUtils.acceptsTextEventStream("text/event-stream;q=invalid"));
        assertFalse(HttpUtils.acceptsTextEventStream("text/event-stream;q = 0.5"));
    }

    @Test
    public void textEventStreamContentTypeRequiresSingleMediaType() {
        assertTrue(HttpUtils.isTextEventStreamContentType("Text/Event-Stream; charset=utf-8"));
        assertTrue(HttpUtils.isTextEventStreamContentType("text/event-stream; charset=\"UTF-8\""));
        assertFalse(HttpUtils.isTextEventStreamContentType("text/event-stream; charset=iso-8859-1"));
        assertFalse(HttpUtils.isTextEventStreamContentType("text/event-stream; charset=utf-16"));
        assertFalse(HttpUtils.isTextEventStreamContentType("application/json, text/event-stream"));
    }
}

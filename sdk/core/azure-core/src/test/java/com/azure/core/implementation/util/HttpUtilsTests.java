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
    public void textEventStreamContentTypeRequiresSingleMediaType() {
        assertTrue(HttpUtils.isTextEventStreamContentType("Text/Event-Stream; charset=utf-8"));
        assertFalse(HttpUtils.isTextEventStreamContentType("application/json, text/event-stream"));
    }
}

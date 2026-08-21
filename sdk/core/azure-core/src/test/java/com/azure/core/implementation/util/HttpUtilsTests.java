// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpUtilsTests {
    @Test
    public void textEventStreamContentTypeRequiresSingleMediaType() {
        assertTrue(HttpUtils.isTextEventStreamContentType("Text/Event-Stream; charset=utf-8"));
        assertTrue(HttpUtils.isTextEventStreamContentType("text/event-stream; charset=\"UTF-8\""));
        assertTrue(HttpUtils.isTextEventStreamContentType("text/event-stream; charset=iso-8859-1"));
        assertTrue(HttpUtils.isTextEventStreamContentType("text/event-stream; charset=utf-16"));
        assertTrue(HttpUtils.isTextEventStreamContentType("text/event-stream; charset=not-a-charset; charset=utf-16"));
        assertFalse(HttpUtils.isTextEventStreamContentType("application/json, text/event-stream"));
        assertTrue(HttpUtils.isTextEventStreamContentType("text/event-stream; note=\"x,y;z\""));
        assertFalse(HttpUtils.isTextEventStreamContentType("text/event-stream; note=\"unterminated"));
    }
}

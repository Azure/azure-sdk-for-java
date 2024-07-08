// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.util;

import com.azure.core.v2.util.tracing.SpanKind;
import com.azure.core.v2.util.tracing.StartSpanOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("deprecation")
public class StartSpanOptionsTests {
    @Test
    public void kindCannotBeNull() {
        assertThrows(NullPointerException.class, () -> new StartSpanOptions(null));
    }

    @Test
    public void internalSpan() {
        StartSpanOptions options = new StartSpanOptions(SpanKind.INTERNAL);

        assertEquals(SpanKind.INTERNAL, options.getSpanKind());
        assertNull(options.getAttributes());
    }

    @Test
    public void clientSpan() {
        StartSpanOptions options = new StartSpanOptions(SpanKind.CLIENT);

        assertEquals(SpanKind.CLIENT, options.getSpanKind());
        assertNull(options.getAttributes());
    }

    @Test
    public void setAttributes() {
        StartSpanOptions options
            = new StartSpanOptions(SpanKind.CLIENT).setAttribute("foo", "bar").setAttribute("1", 1);

        assertEquals(SpanKind.CLIENT, options.getSpanKind());
        assertEquals(2, options.getAttributes().size());
        assertEquals("bar", options.getAttributes().get("foo"));
        assertEquals(1, options.getAttributes().get("1"));
    }
}

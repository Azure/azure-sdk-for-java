// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.util;

import com.azure.core.util.tracing.StartSpanOptions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StartSpanOptionsTests {
    @Test
    public void nameOnly() {
        StartSpanOptions options = new StartSpanOptions("foo");

        assertEquals("foo", options.getSpanName());
        assertEquals(StartSpanOptions.Kind.INTERNAL, options.getSpanKind());
        assertNull(options.getAttributes());
        assertFalse(options.getMakeCurrent());
    }

    @Test
    public void nameAndClient() {
        StartSpanOptions options = new StartSpanOptions("foo", StartSpanOptions.Kind.CLIENT);

        assertEquals("foo", options.getSpanName());
        assertEquals(StartSpanOptions.Kind.CLIENT, options.getSpanKind());
        assertNull(options.getAttributes());
        assertFalse(options.getMakeCurrent());
    }

    @Test
    public void setMakeCurrent() {
        StartSpanOptions options = new StartSpanOptions("foo", StartSpanOptions.Kind.CLIENT)
            .setMakeCurrent(true);
        assertTrue(options.getMakeCurrent());
    }

    @Test
    public void setAttributes() {
        StartSpanOptions options = new StartSpanOptions("foo", StartSpanOptions.Kind.CLIENT)
            .setAttribute("foo", "bar")
            .setAttribute("1", 1);

        assertEquals(2, options.getAttributes().size());
        assertEquals("bar", options.getAttributes().get("foo"));
        assertEquals(1, options.getAttributes().get("1"));
    }
}

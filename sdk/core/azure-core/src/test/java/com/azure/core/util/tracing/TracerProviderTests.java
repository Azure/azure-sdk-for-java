// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.tracing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TracerProviderTests {
    @Test
    public void noopTracer() {
        Tracer tracer = TracerProvider.getDefaultProvider().createTracer("foo", null, null, null);
        assertNotNull(tracer);
        assertFalse(tracer.isEnabled());
    }

    @Test
    public void invalidParams() {
        assertThrows(NullPointerException.class, () -> TracerProvider.getDefaultProvider().createTracer(null, null, null, null));
    }
}

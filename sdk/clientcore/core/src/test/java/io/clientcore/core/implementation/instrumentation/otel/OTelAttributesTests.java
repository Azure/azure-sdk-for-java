// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.instrumentation.otel;

import io.clientcore.core.implementation.instrumentation.NoopAttributes;
import io.clientcore.core.instrumentation.InstrumentationAttributes;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for the OTelAttributes class when OpenTelemetry isn't on the classpath.
 */
public class OTelAttributesTests {
    @Test
    public void createWithInvalidValue() {
        // This value would normally throw an exception if OTel is on the classpath.
        assertDoesNotThrow(() -> OTelAttributes.create(Collections.singletonMap(null, null)));
    }

    @Test
    public void createWithNull() {
        InstrumentationAttributes attributes = assertDoesNotThrow(() -> OTelAttributes.create(null));
        OTelAttributes oTelAttributes = assertInstanceOf(OTelAttributes.class, attributes);
        assertNull(oTelAttributes.getOTelAttributes());
    }

    @Test
    public void putReturnsNoop() {
        assertSame(NoopAttributes.INSTANCE, OTelAttributes.create(null).put("key", "value"));
    }
}

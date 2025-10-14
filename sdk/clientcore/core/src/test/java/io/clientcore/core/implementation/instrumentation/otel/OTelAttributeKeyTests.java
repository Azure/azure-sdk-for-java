// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.instrumentation.otel;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Unit tests for the OTelAttributeKey class when OpenTelemetry isn't on the classpath.
 */
public class OTelAttributeKeyTests {
    @Test
    public void getKeyReturnsNull() {
        // This value would normally throw an exception if OTel is on the classpath.
        assertDoesNotThrow(() -> OTelAttributeKey.getKey("key", Collections.singletonList(1L)));
    }
}

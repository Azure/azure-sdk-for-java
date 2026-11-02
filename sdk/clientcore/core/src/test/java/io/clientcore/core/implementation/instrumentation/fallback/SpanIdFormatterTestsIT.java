// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.fallback;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises fixed-width span identifiers using the packaged Java 17 formatter.
 */
@EnabledForJreRange(min = JRE.JAVA_17)
public class SpanIdFormatterTestsIT extends SpanIdFormatterTests {
    @Test
    public void selectsPackagePrivateSpanIdFormatter() {
        assertFalse(Modifier.isPublic(SpanIdFormatter.class.getModifiers()));
        assertTrue(
            SpanIdFormatter.class.getResource("SpanIdFormatter.class").toString().contains("META-INF/versions/17/"));
    }
}

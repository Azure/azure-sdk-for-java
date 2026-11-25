// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the package-private helpers loaded from the packaged multi-release JAR.
 */
@EnabledForJreRange(min = JRE.JAVA_11)
public class JdkApiTestsIT extends JdkApiTests {
    @Test
    public void selectsPackagePrivateRangeParser() {
        assertFalse(Modifier.isPublic(LongParser.class.getModifiers()));
        assertTrue(LongParser.class.getResource("LongParser.class").toString().contains("META-INF/versions/11/"));
    }

    @Test
    @EnabledForJreRange(min = JRE.JAVA_17)
    public void selectsPackagePrivateHexFormatter() {
        assertFalse(Modifier.isPublic(HexEncoding.class.getModifiers()));
        assertTrue(HexEncoding.class.getResource("HexEncoding.class").toString().contains("META-INF/versions/17/"));
    }
}

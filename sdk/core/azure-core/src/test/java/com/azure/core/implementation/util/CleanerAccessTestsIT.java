// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the package-private direct Cleaner implementation in the packaged JAR.
 */
@EnabledForJreRange(min = JRE.JAVA_11)
public class CleanerAccessTestsIT extends CleanerAccessTests {
    @Test
    public void selectsPackagePrivateCleanerAccess() {
        assertFalse(Modifier.isPublic(CleanerAccess.class.getModifiers()));
        assertTrue(CleanerAccess.class.getResource("CleanerAccess.class").toString().contains("META-INF/versions/11/"));
        assertTrue(ReferenceManagerImpl.isCleanerUsed());
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CoreUtilsTests {
    @Test
    public void findFirstOfTypeEmptyArgs() {
        assertNull(CoreUtils.findFirstOfType(null, Integer.class));
    }

    @Test
    public void findFirstOfTypeWithOneOfType() {
        int expected = 1;
        Object[] args = { "string", expected };
        int actual = CoreUtils.findFirstOfType(args, Integer.class);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void findFirstOfTypeWithMultipleOfType() {
        int expected = 1;
        Object[] args = { "string", expected, 10 };
        int actual = CoreUtils.findFirstOfType(args, Integer.class);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void findFirstOfTypeWithNoneOfType() {
        Object[] args = { "string", "anotherString" };
        assertNull(CoreUtils.findFirstOfType(args, Integer.class));
    }

    @Test
    public void testProperties() {
        assertNotNull(CoreUtils.getProperties("azure-core.properties").get("version"));
        assertNotNull(CoreUtils.getProperties("azure-core.properties").get("name"));
        assertTrue(CoreUtils.getProperties("azure-core.properties").get("version")
            .matches("\\d.\\d.\\d([-a-zA-Z0-9.])*"));
    }

    @Test
    public void testMissingProperties() {
        assertNotNull(CoreUtils.getProperties("foo.properties"));
        assertTrue(CoreUtils.getProperties("foo.properties").isEmpty());
        assertNull(CoreUtils.getProperties("azure-core.properties").get("foo"));
    }
}

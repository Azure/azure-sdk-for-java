// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
        UserAgentProperties properties =
            CoreUtils.getUserAgentPropertiesFromProperties("azure-core.properties");
        assertFalse(properties.getName().matches("UnknownName"));
        assertTrue(CoreUtils.getUserAgentPropertiesFromProperties("azure-core.properties").getVersion()
            .matches("\\d.\\d.\\d([-a-zA-Z0-9.])*"));
    }

    @Test
    public void testMissingProperties() {
        assertTrue(CoreUtils.getUserAgentPropertiesFromProperties("foo.properties")
            .getVersion().matches("UnknownVersion"));
        assertTrue(CoreUtils.getUserAgentPropertiesFromProperties("foo.properties")
            .getName().matches("UnknownName"));
    }
}

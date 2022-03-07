// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConfigurationBuilderTests {

    @Test
    public void buildRoot() {
        ConfigurationBuilder builder = new ConfigurationBuilder(new TestConfigurationSource("foo", "bar"));
        assertEquals("bar", builder.build().get("foo"));

        // check if we can get root again
        assertEquals("bar", builder.build().get("foo"));
        assertEquals("bar", builder.root(null).build().get("foo"));
        assertEquals("bar", builder.root("az").root(null).build().get("foo"));
    }

    @Test
    public void buildRootWithPath() {
        ConfigurationBuilder builder = new ConfigurationBuilder(new TestConfigurationSource("az.foo", "az", "xyz.foo", "xyz"))
            .root("az");
        assertEquals("az", builder.build().get("foo"));

        builder.root("xyz");
        assertEquals("xyz", builder.build().get("foo"));
    }

    @Test
    public void buildSection() {
        ConfigurationBuilder builder = new ConfigurationBuilder(new TestConfigurationSource("az.foo", "az", "az.local.bar", "az.local"))
            .root("az");
        assertEquals("az.local", builder.buildSection("local").get("bar"));

        ConfigurationProperty<String> prop = ConfigurationProperty.stringPropertyBuilder("foo").shared(true).build();
        assertEquals("az", builder.buildSection("another").get(prop));
    }

    @Test
    public void nullOrEmptyProps() {
        ConfigurationBuilder builder = new ConfigurationBuilder(new TestConfigurationSource("null", null, "empty", ""));
        assertFalse(builder.build().contains("null"));
        assertFalse(builder.build().contains("empty"));
    }

    @Test
    public void nullSource() {
        assertThrows(NullPointerException.class,  () -> new ConfigurationBuilder(null));
    }

    @Test
    public void nullSection() {
        ConfigurationBuilder builder = new ConfigurationBuilder(new TestConfigurationSource());
        assertThrows(NullPointerException.class,  () -> builder.buildSection(null));
    }
}

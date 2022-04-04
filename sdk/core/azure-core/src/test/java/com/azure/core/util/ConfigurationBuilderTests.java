// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConfigurationBuilderTests {
    private static final ConfigurationProperty<String> FOO_PROPERTY = ConfigurationPropertyBuilder.ofString("foo").build();
    private static final ConfigurationSource EMPTY_SOURCE = new TestConfigurationSource();

    @Test
    public void buildRoot() {
        ConfigurationBuilder builder = new ConfigurationBuilder(new TestConfigurationSource("foo", "bar"));
        assertEquals("bar", builder.build().get(FOO_PROPERTY));

        // check if we can get root again
        assertEquals("bar", builder.build().get(FOO_PROPERTY));
        assertEquals("bar", builder.root(null).build().get(FOO_PROPERTY));
        assertEquals("bar", builder.root("az").root(null).build().get(FOO_PROPERTY));
    }

    @Test
    public void buildRootWithPath() {
        ConfigurationBuilder builder = new ConfigurationBuilder(new TestConfigurationSource("az.foo", "az", "xyz.foo", "xyz"))
            .root("az");
        assertEquals("az", builder.build().get(FOO_PROPERTY));

        builder.root("xyz");
        assertEquals("xyz", builder.build().get(FOO_PROPERTY));
    }

    @Test
    public void buildSection() {
        ConfigurationBuilder builder = new ConfigurationBuilder(new TestConfigurationSource("az.foo", "az", "az.local.bar", "az.local"))
            .root("az");

        ConfigurationProperty<String> bar = ConfigurationPropertyBuilder.ofString("bar").build();
        assertEquals("az.local", builder.buildSection("local").get(bar));

        ConfigurationProperty<String> prop = ConfigurationPropertyBuilder.ofString("foo").shared(true).build();
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

    @Test
    public void emptySourceAddProperty() {
        ConfigurationBuilder builder = new ConfigurationBuilder()
            .addProperty("foo", "bar");
        assertEquals("bar", builder.build().get(FOO_PROPERTY));
    }

    @Test
    public void sourceAddProperty() {
        ConfigurationProperty<String> property1 = ConfigurationPropertyBuilder.ofString("foo1").build();
        ConfigurationProperty<String> property2 = ConfigurationPropertyBuilder.ofString("foo2").shared(true).build();

        ConfigurationBuilder builder = new ConfigurationBuilder();
        assertNull(builder.build().get(property1));

        builder.addProperty("az.foo2", "az.bar2");
        assertEquals("az.bar2", builder.buildSection("az").get(property2));
        assertEquals("az.bar2", builder.root("az").build().get(property2));
    }

    @Test
    public void sourceAddPropertyBuildSection() {
        ConfigurationBuilder builder = new ConfigurationBuilder(new TestConfigurationSource("az.foo1", "bar1"))
            .addProperty("az.foo2", "bar2");
        assertEquals("bar1", builder.buildSection("az").get(ConfigurationPropertyBuilder.ofString("foo1").build()));
        assertEquals("bar2", builder.buildSection("az").get(ConfigurationPropertyBuilder.ofString("foo2").build()));
    }

    @Test
    public void sourceAddPropertySameName() {
        ConfigurationBuilder builder = new ConfigurationBuilder(new TestConfigurationSource("foo", "bar1"))
            .addProperty("foo", "bar2");
        assertEquals("bar2", builder.build().get(FOO_PROPERTY));
    }

    @Test
    public void environmentSource() {
        ConfigurationBuilder builder = new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE, new TestConfigurationSource("foo", "bar"));

        Configuration root = builder.build();
        assertNull(root.get(FOO_PROPERTY));
        assertEquals("bar", root.get("foo"));

        Configuration section = builder.buildSection("any");
        assertNull(section.get(FOO_PROPERTY));
        assertEquals("bar", section.get("foo"));
    }

    @Test
    public void systemPropertiesSource() {
        ConfigurationBuilder builder = new ConfigurationBuilder(EMPTY_SOURCE, new TestConfigurationSource("foo", "bar"), EMPTY_SOURCE);

        Configuration root = builder.build();
        assertNull(root.get(FOO_PROPERTY));
        assertEquals("bar", root.get("foo"));

        Configuration section = builder.buildSection("any");
        assertNull(section.get(FOO_PROPERTY));
        assertEquals("bar", section.get("foo"));
    }

    @Test
    public void systemPropertiesOverEnvironmentVariablesSource() {
        ConfigurationBuilder builder = new ConfigurationBuilder(EMPTY_SOURCE,
            new TestConfigurationSource()
                    .add("fooSys", "s1")
                    .add("foo", "sys"),
            new TestConfigurationSource()
                .add("fooEnv", "e1")
                .add("foo", "env"));

        Configuration root = builder.build();
        assertEquals("sys", root.get("foo"));
        assertEquals("e1", root.get("fooEnv"));
        assertEquals("s1", root.get("fooSys"));
    }

    @Test
    public void invalidArguments() {
        assertThrows(NullPointerException.class, () -> new ConfigurationBuilder(null));
        assertThrows(NullPointerException.class, () -> new ConfigurationBuilder(null, EMPTY_SOURCE, EMPTY_SOURCE));
        assertThrows(NullPointerException.class, () -> new ConfigurationBuilder(EMPTY_SOURCE, null, EMPTY_SOURCE));
        assertThrows(NullPointerException.class, () -> new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE, null));
        assertThrows(NullPointerException.class, () -> new ConfigurationBuilder().addProperty(null, "val"));
        assertThrows(NullPointerException.class, () -> new ConfigurationBuilder().addProperty("key", null));
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.v2.implementation.util.EnvironmentConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigurationTests {
    private static final ConfigurationSource EMPTY_SOURCE = new TestConfigurationSource();
    private static final ConfigurationSource FOO_SOURCE = new TestConfigurationSource().put("foo", "bar");
    private static final ConfigurationProperty<String> FOO_PROPERTY
        = ConfigurationPropertyBuilder.ofString("foo").build();

    @Test
    public void environmentConfigurationDefaultSources() {
        String sysPropName = UUID.randomUUID().toString();
        System.setProperty(sysPropName, "value");
        EnvironmentConfiguration envConfiguration = new EnvironmentConfiguration(null, null);
        Configuration configuration = new Configuration(EMPTY_SOURCE, envConfiguration, null, null);

        assertNull(configuration.get("foo"));
        assertFalse(configuration.contains("foo"));
        assertEquals("value", configuration.get(sysPropName));
        assertTrue(configuration.contains(sysPropName));

        System.clearProperty(sysPropName);
    }

    @Test
    public void environmentConfigurationEnvVar() {
        EnvironmentConfiguration envConfiguration = new EnvironmentConfiguration(FOO_SOURCE, null);
        Configuration configuration = new Configuration(EMPTY_SOURCE, envConfiguration, null, null);
        assertEquals("bar", configuration.get("foo"));
        assertTrue(configuration.contains("foo"));

        assertNull(configuration.get(FOO_PROPERTY));
        assertFalse(configuration.contains(FOO_PROPERTY));
    }

    @Test
    public void environmentConfigurationSystemProperty() {
        EnvironmentConfiguration envConfiguration = new EnvironmentConfiguration(null, FOO_SOURCE);
        Configuration configuration = new Configuration(EMPTY_SOURCE, envConfiguration, null, null);
        assertEquals("bar", configuration.get("foo"));
        assertTrue(configuration.contains("foo"));

        assertNull(configuration.get(FOO_PROPERTY));
        assertFalse(configuration.contains(FOO_PROPERTY));
    }

    @Test
    public void environmentConfigurationNotFound() {
        EnvironmentConfiguration envConfiguration = new EnvironmentConfiguration(EMPTY_SOURCE, null);
        Configuration configuration = new Configuration(EMPTY_SOURCE, envConfiguration, null, null);
        assertNull(configuration.get("foo"));
        assertFalse(configuration.contains("foo"));
    }

    @Test
    public void environmentConfigurationEnvVarDefaultValue() {
        EnvironmentConfiguration envConfiguration
            = new EnvironmentConfiguration(new TestConfigurationSource().put("foo", "42"), null);
        Configuration configuration = new Configuration(EMPTY_SOURCE, envConfiguration, null, null);
        assertEquals(42, configuration.get("foo", 0));
        assertEquals(0, configuration.get("foo-not-found", 0));
    }

    @Test
    public void environmentConfigurationSysPropDefaultValue() {
        EnvironmentConfiguration envConfiguration
            = new EnvironmentConfiguration(null, new TestConfigurationSource().put("foo", "42"));
        Configuration configuration = new Configuration(EMPTY_SOURCE, envConfiguration, null, null);
        assertEquals(42, configuration.get("foo", 0));
        assertEquals(0, configuration.get("foo-not-found", 0));
    }

    @Test
    public void environmentConfigurationEnvVarConverter() {
        Map<String, String> props = new HashMap<>();
        props.put("foo", "42");
        props.put("bar", "forty two");

        EnvironmentConfiguration envConfiguration = new EnvironmentConfiguration(EMPTY_SOURCE,
            new TestConfigurationSource().put("foo", "42").put("bar", "forty two"));
        Configuration configuration = new Configuration(EMPTY_SOURCE, envConfiguration, null, null);
        Function<String, Integer> converter = Integer::parseInt;
        assertEquals(42, configuration.get("foo", converter));
        assertThrows(NumberFormatException.class, () -> configuration.get("bar", Integer::parseInt));
    }

    @Test
    public void environmentConfigurationSysPropConverter() {
        Map<String, String> props = new HashMap<>();
        props.put("foo", "42");
        props.put("bar", "forty two");

        EnvironmentConfiguration envConfiguration = new EnvironmentConfiguration(
            new TestConfigurationSource().put("foo", "42").put("bar", "forty two"), EMPTY_SOURCE);
        Configuration configuration = new Configuration(EMPTY_SOURCE, envConfiguration, null, null);
        Function<String, Integer> converter = Integer::parseInt;
        assertEquals(42, configuration.get("foo", converter));
        assertThrows(NumberFormatException.class, () -> configuration.get("bar", Integer::parseInt));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void environmentConfigurationCanNotRemoveEnvVar() {
        EnvironmentConfiguration envConfiguration = new EnvironmentConfiguration(EMPTY_SOURCE, FOO_SOURCE);
        Configuration configuration = new Configuration(EMPTY_SOURCE, envConfiguration, null, null);
        assertEquals("bar", configuration.get("foo"));

        configuration.remove("foo");

        // environment variables are reloaded, so it's impossible to remove
        assertEquals("bar", envConfiguration.getEnvironmentVariable("foo"));
        assertEquals("bar", configuration.get("foo"));
        assertTrue(configuration.contains("foo"));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void environmentConfigurationCanNotRemoveSystemProperty() {
        EnvironmentConfiguration envConfiguration = new EnvironmentConfiguration(FOO_SOURCE, EMPTY_SOURCE);
        Configuration configuration = new Configuration(EMPTY_SOURCE, envConfiguration, null, null);
        assertEquals("bar", configuration.get("foo"));

        configuration.remove("foo");

        // system properties are reloaded, so it's impossible to remove
        assertEquals("bar", envConfiguration.getSystemProperty("foo"));
        assertEquals("bar", configuration.get("foo"));
        assertTrue(configuration.contains("foo"));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void environmentConfigurationCanRemoveExplicit() {
        EnvironmentConfiguration envConfiguration = new EnvironmentConfiguration(EMPTY_SOURCE, EMPTY_SOURCE);
        Configuration configuration = new Configuration(EMPTY_SOURCE, envConfiguration, null, null);

        configuration.put("foo", "bar");
        assertTrue(configuration.contains("foo"));

        configuration.remove("foo");

        // environment variables are reloaded, so it's imposible to remove
        assertNull(envConfiguration.getEnvironmentVariable("foo"));
        assertNull(configuration.get("foo"));
        assertFalse(configuration.contains("foo"));
    }

    @Test
    public void environmentGetByNameBasicBuilder() {
        Configuration configuration
            = new ConfigurationBuilder(EMPTY_SOURCE, new TestConfigurationSource().put("fooSys", "barSys"),
                new TestConfigurationSource().put("fooEnv", "barEnv")).build();
        assertEquals("barEnv", configuration.get("fooEnv"));
        assertTrue(configuration.contains("fooEnv"));

        assertEquals("barSys", configuration.get("fooSys"));
        assertTrue(configuration.contains("fooSys"));
    }

    @Test
    public void environmentGetByNameBasicBuilderNotFound() {
        Configuration configuration = new ConfigurationBuilder().build();
        assertNull(configuration.get("foo"));
        assertFalse(configuration.contains("foo"));
    }

    @ParameterizedTest
    @MethodSource("getOrDefaultSupplier")
    public void getByNameImplicitConverter(String configurationValue, Object defaultValue, Object expectedValue) {
        Configuration configuration = new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE,
            new TestConfigurationSource().put("foo", configurationValue)).build();

        assertEquals(expectedValue, configuration.get("foo", defaultValue));
    }

    @Test
    public void environmentGetByNameFallbackToDefault() {
        Configuration configuration = new ConfigurationBuilder().build();
        assertEquals("0", configuration.get("foo", "0"));
        assertEquals(0, configuration.get("foo", 0));
    }

    @Test
    public void environmentGetByNameImplicitConverterThrows() {
        Configuration configuration
            = new ConfigurationBuilder(EMPTY_SOURCE, new TestConfigurationSource().put("fooSys", "forty two"),
                new TestConfigurationSource().put("fooEnv", "forty two")).build();
        assertThrows(NumberFormatException.class, () -> configuration.get("fooEnv", 0));
        assertThrows(NumberFormatException.class, () -> configuration.get("fooSys", 0));
    }

    @Test
    public void getByNameConverter() {
        Configuration configuration = new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE,
            new TestConfigurationSource().put("foo", "42").put("bar", "forty two")).build();
        Function<String, Integer> converter = Integer::parseInt;
        assertEquals(42, configuration.get("foo", converter));
        assertThrows(NumberFormatException.class, () -> configuration.get("bar", Integer::parseInt));
    }

    @Test
    public void getByPropertyVsEnvVarName() {
        EnvironmentConfiguration envConfiguration = new EnvironmentConfiguration(FOO_SOURCE, EMPTY_SOURCE);

        ConfigurationSource testSource = new TestConfigurationSource().put("foo", "some value");
        Configuration configuration = new Configuration(testSource, envConfiguration, null, null);

        assertEquals("bar", configuration.get("foo"));
        assertEquals("some value", configuration.get(FOO_PROPERTY));
    }

    @Test
    public void getByPropertyVsSysPropName() {
        EnvironmentConfiguration envConfiguration = new EnvironmentConfiguration(EMPTY_SOURCE, FOO_SOURCE);

        ConfigurationSource testSource = new TestConfigurationSource().put("foo", "some value");
        Configuration configuration = new Configuration(testSource, envConfiguration, null, null);

        assertEquals("bar", configuration.get("foo"));
        assertEquals("some value", configuration.get(FOO_PROPERTY));
    }

    @Test
    public void getExplicitPropertyWithSystemPropertyAndEnvVar() {
        ConfigurationProperty<String> property = ConfigurationPropertyBuilder.ofString("foo")
            .systemPropertyName("sys.foo")
            .environmentVariableName("env.foo")
            .defaultValue("bar")
            .build();

        assertEquals("bar", new ConfigurationBuilder().build().get(property));
        assertEquals("explicit", new ConfigurationBuilder().putProperty("foo", "explicit").build().get(property));
    }

    @Test
    public void getSystemPropertyWithSystemPropertyAndEnvVar() {
        ConfigurationProperty<String> property = ConfigurationPropertyBuilder.ofString("foo")
            .systemPropertyName("sys.foo")
            .environmentVariableName("env.foo")
            .defaultValue("bar")
            .build();

        Configuration envOnlyConfig
            = new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE, new TestConfigurationSource().put("env.foo", "env"))
                .build();
        Configuration envAndSysConfig
            = new ConfigurationBuilder(EMPTY_SOURCE, new TestConfigurationSource().put("sys.foo", "sys"),
                new TestConfigurationSource().put("env.foo", "env")).build();

        assertEquals("env", envOnlyConfig.get(property));
        assertEquals("sys", envAndSysConfig.get(property));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void removeOnlyAffectsEnvironmentForBackwardCompatibility() {
        EnvironmentConfiguration envConfiguration = new EnvironmentConfiguration(EMPTY_SOURCE, null);

        ConfigurationSource testSource = new TestConfigurationSource().put("foo", "bar");
        Configuration configuration = new Configuration(testSource, envConfiguration, null, null);

        envConfiguration.put("foo", "barEnv");
        assertEquals("barEnv", configuration.get("foo"));
        assertEquals("bar", configuration.get(FOO_PROPERTY));

        configuration.remove("foo");
        assertNull(envConfiguration.get("foo"));
        assertFalse(configuration.contains("foo"));
        assertTrue(configuration.contains(FOO_PROPERTY));
        assertEquals("bar", configuration.get(FOO_PROPERTY));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void putOnlyAffectsEnvironmentForBackwardCompatibility() {
        EnvironmentConfiguration envConfiguration = new EnvironmentConfiguration(EMPTY_SOURCE, null);

        ConfigurationSource testSource = new TestConfigurationSource().put("foo", "bar");
        Configuration configuration = new Configuration(testSource, envConfiguration, null, null);

        configuration.put("foo", "newBar").put("baz", "42");

        assertTrue(configuration.contains("foo"));
        assertTrue(configuration.contains("baz"));
        assertEquals("newBar", configuration.get("foo"));
        assertEquals("bar", configuration.get(FOO_PROPERTY));
        assertEquals("42", configuration.get("baz"));
    }

    @Test
    public void getLocalPropertyFromSection() {
        Configuration config = new ConfigurationBuilder().putProperty("appconfiguration.prop", "foo")
            .putProperty("prop", "bar")
            .putProperty("prop2", "baz")
            .buildSection("appconfiguration");

        ConfigurationProperty<String> localProp = ConfigurationPropertyBuilder.ofString("prop").build();
        ConfigurationProperty<String> localPropFullName
            = ConfigurationPropertyBuilder.ofString("appconfiguration.prop").build();
        ConfigurationProperty<String> globalProp = ConfigurationPropertyBuilder.ofString("prop").shared(true).build();
        ConfigurationProperty<String> globalProp2 = ConfigurationPropertyBuilder.ofString("prop2").shared(true).build();

        assertEquals("foo", config.get(localProp));
        assertEquals("foo", config.get(globalProp));
        assertEquals("baz", config.get(globalProp2));
        assertNull(config.get(localPropFullName));
    }

    @Test
    public void getGlobalPropertyFromDefaultsSection() {
        Configuration config = new ConfigurationBuilder().putProperty("appconfiguration.prop", "local")
            .putProperty("global.prop", "default")
            .buildSection("global");

        ConfigurationProperty<String> localProp = ConfigurationPropertyBuilder.ofString("prop").build();
        ConfigurationProperty<String> localPropFullName
            = ConfigurationPropertyBuilder.ofString("appconfiguration.prop").build();
        ConfigurationProperty<String> globalProp = ConfigurationPropertyBuilder.ofString("prop").shared(true).build();

        assertEquals("default", config.get(localProp));
        assertEquals("default", config.get(globalProp));
        assertNull(config.get(localPropFullName));
    }

    @Test
    public void getGlobalPropertyFromDefaultsAndRootSection() {
        Configuration config = new ConfigurationBuilder().putProperty("appconfiguration.prop", "local")
            .putProperty("prop", "root")
            .build();

        ConfigurationProperty<String> localProp = ConfigurationPropertyBuilder.ofString("prop").build();
        ConfigurationProperty<String> localPropFullName
            = ConfigurationPropertyBuilder.ofString("appconfiguration.prop").build();
        ConfigurationProperty<String> globalProp = ConfigurationPropertyBuilder.ofString("prop").shared(true).build();

        assertEquals("root", config.get(localProp));
        assertEquals("root", config.get(globalProp));
        assertEquals("local", config.get(localPropFullName));
    }

    @Test
    public void getPropertyWithAlias() {
        ConfigurationProperty<String> prop
            = ConfigurationPropertyBuilder.ofString("prop").aliases("alias1", "alias2").build();

        Configuration config1 = new ConfigurationBuilder().putProperty("alias2", "a2").build();
        assertTrue(config1.contains(prop));
        assertEquals("a2", config1.get(prop));

        Configuration config2
            = new ConfigurationBuilder().putProperty("alias1", "a1").putProperty("alias2", "a2").build();
        assertEquals("a1", config2.get(prop));

        Configuration config3 = new ConfigurationBuilder().putProperty("prop", "p").putProperty("alias1", "a1").build();

        assertEquals("p", config3.get(prop));
    }

    @Test
    public void getPropertyWithEnvVar() {
        ConfigurationProperty<String> prop
            = ConfigurationPropertyBuilder.ofString("prop").environmentVariableName("prop").build();

        EnvironmentConfiguration envConfig
            = new EnvironmentConfiguration(EMPTY_SOURCE, new TestConfigurationSource().put("prop", "env"));
        Configuration config = new Configuration(EMPTY_SOURCE, envConfig, null, null);
        assertTrue(config.contains(prop));
        assertEquals("env", config.get(prop));
    }

    @Test
    public void getPropertyWithSysProperty() {
        ConfigurationProperty<String> prop = ConfigurationPropertyBuilder.ofString("prop")
            .environmentVariableName("prop")
            .systemPropertyName("prop")
            .build();

        EnvironmentConfiguration envConfig = new EnvironmentConfiguration(
            new TestConfigurationSource().put("prop", "sys"), new TestConfigurationSource().put("prop", "env"));
        Configuration config = new Configuration(EMPTY_SOURCE, envConfig, null, null);
        assertTrue(config.contains(prop));
        assertEquals("sys", config.get(prop));
    }

    @ParameterizedTest
    @MethodSource("properties")
    public void getProperty(ConfigurationProperty<?> prop, String actual, Object expected, Object defaultValue) {
        Configuration config = new ConfigurationBuilder().putProperty("foo", actual).build();
        assertTrue(config.contains(prop));
        assertEquals(expected, config.get(prop));
    }

    @ParameterizedTest
    @MethodSource("properties")
    public void getMissingProperty(ConfigurationProperty<?> missingProp, String actual, Object expected,
        Object defaultValue) {
        Configuration config = new ConfigurationBuilder().putProperty("foo", actual).buildSection("az");
        assertFalse(config.contains(missingProp));
        assertEquals(defaultValue, config.get(missingProp));
    }

    @Test
    public void getRequiredMissingProperty() {
        Configuration config = new ConfigurationBuilder().build();
        assertThrows(IllegalArgumentException.class,
            () -> config.get(ConfigurationPropertyBuilder.ofString("foo").required(true).build()));
    }

    @ParameterizedTest
    @MethodSource("validIntStrings")
    public void getValidIntProperty(String value, Integer expected) {
        Configuration config = new ConfigurationBuilder().putProperty("az.foo", value).buildSection("az");
        assertEquals(expected, config.get(ConfigurationPropertyBuilder.ofInteger("foo").build()));
    }

    @ParameterizedTest
    @MethodSource("invalidIntStrings")
    public void getInvalidIntProperty(String value) {
        Configuration config = new ConfigurationBuilder().putProperty("az.foo", value).buildSection("az");
        assertThrows(NumberFormatException.class,
            () -> config.get(ConfigurationPropertyBuilder.ofInteger("foo").build()));
    }

    @ParameterizedTest
    @MethodSource("validDurationStrings")
    public void getValidDurationProperty(String value, Duration expected) {
        Configuration config = new ConfigurationBuilder().putProperty("az.foo", value).buildSection("az");
        assertEquals(expected, config.get(ConfigurationPropertyBuilder.ofDuration("foo").build()));
    }

    @ParameterizedTest
    @MethodSource("invalidDurationStrings")
    public void getInvalidDurationProperty(String value) {
        Configuration config = new ConfigurationBuilder().putProperty("az.foo", value).buildSection("az");

        if (value.startsWith("-")) {
            assertThrows(IllegalArgumentException.class,
                () -> config.get(ConfigurationPropertyBuilder.ofDuration("foo").build()));
        } else {
            assertThrows(NumberFormatException.class,
                () -> config.get(ConfigurationPropertyBuilder.ofDuration("foo").build()));
        }
    }

    @Test
    public void getBooleanProperty() {

        Map<String, String> configurations = new HashMap<>();
        configurations.put("prop", "p");

        Configuration config = new ConfigurationBuilder().putProperty("az.true", "true")
            .putProperty("az.false", "false")
            .putProperty("az.anything-else", "anything-else")
            .buildSection("az");

        assertTrue(config.get(ConfigurationPropertyBuilder.ofBoolean("true").build()));
        assertFalse(config.get(ConfigurationPropertyBuilder.ofBoolean("false").build()));
        assertFalse(config.get(ConfigurationPropertyBuilder.ofBoolean("anything-else").build()));
        assertNull(config.get(ConfigurationPropertyBuilder.ofBoolean("not-found").build()));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void cloneConfiguration() {
        EnvironmentConfiguration envConfig = new EnvironmentConfiguration(
            new TestConfigurationSource().put("envVar1", "envVar1").put("envVar2", "envVar2"), EMPTY_SOURCE);

        ConfigurationSource sysPropSource = new TestConfigurationSource().put("prop1", "prop1").put("prop2", "prop2");

        Configuration configuration = new Configuration(sysPropSource, envConfig, null, null);

        Configuration configurationClone = configuration.clone();

        // Verify that the clone has the expected values.
        assertEquals(configuration.get("envVar1"), configurationClone.get("envVar1"));
        assertEquals(configuration.get("envVar2"), configurationClone.get("envVar2"));

        // The clone should be a separate instance, verify its modifications won't affect the original copy.
        configurationClone.remove("envVar2");
        assertTrue(configuration.contains("envVar2"));
    }

    private static Stream<Arguments> properties() {
        return Stream.of(Arguments.of(ConfigurationPropertyBuilder.ofString("foo").build(), "bar", "bar", null),
            Arguments.of(ConfigurationPropertyBuilder.ofInteger("foo").build(), "42", 42, null),
            Arguments.of(ConfigurationPropertyBuilder.ofDuration("foo").build(), "2", Duration.ofMillis(2), null),
            Arguments.of(ConfigurationPropertyBuilder.ofBoolean("foo").build(), "true", true, null),
            Arguments.of(ConfigurationPropertyBuilder.ofString("foo").defaultValue("foo").build(), "bar", "bar", "foo"),
            Arguments.of(ConfigurationPropertyBuilder.ofInteger("foo").defaultValue(37).build(), "42", 42, 37),
            Arguments.of(ConfigurationPropertyBuilder.ofDuration("foo").defaultValue(Duration.ofMillis(1)).build(), "2",
                Duration.ofMillis(2), Duration.ofMillis(1)),
            Arguments.of(ConfigurationPropertyBuilder.ofBoolean("foo").defaultValue(false).build(), "true", true,
                false),
            Arguments.of(new ConfigurationProperty<Double>("foo", 0.1, false, v -> Double.parseDouble(v), false, null,
                null, null, null), "0.2", 0.2, 0.1));
    }

    private static Stream<Arguments> validIntStrings() {
        return Stream.of(Arguments.of("132", 132), Arguments.of("-321", -321), Arguments.of("0", 0),
            Arguments.of("2147483647", Integer.MAX_VALUE));
    }

    private static Stream<Arguments> invalidIntStrings() {
        return Stream.of(Arguments.of("0x5"), Arguments.of("not-a-string"), Arguments.of("2147483648"));
    }

    private static Stream<Arguments> validDurationStrings() {
        return Stream.of(Arguments.of("0", Duration.ofMillis(0)), Arguments.of("132", Duration.ofMillis(132)),
            Arguments.of("2147483648", Duration.ofMillis(2147483648L)));
    }

    private static Stream<Arguments> invalidDurationStrings() {
        return Stream.of(Arguments.of("-1", Duration.ofMillis(0)), Arguments.of("foo", Duration.ofMillis(123)),
            Arguments.of("9223372036854775808", Duration.ofMillis(2147483648L)));
    }

    private static Stream<Arguments> getOrDefaultSupplier() {
        return Stream.of(Arguments.of(String.valueOf((byte) 42), (byte) 12, (byte) 42),
            Arguments.of(String.valueOf((short) 42), (short) 12, (short) 42), Arguments.of(String.valueOf(42), 12, 42),
            Arguments.of(String.valueOf(42L), 12L, 42L), Arguments.of(String.valueOf(42F), 12F, 42F),
            Arguments.of(String.valueOf(42D), 12D, 42D), Arguments.of(String.valueOf(true), false, true),
            Arguments.of("42", "12", "42"));
    }
}

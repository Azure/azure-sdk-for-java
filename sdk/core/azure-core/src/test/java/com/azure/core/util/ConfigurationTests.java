// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.implementation.util.EnvironmentConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigurationTests {

    private static final Map<String, String> ENV_PROPS = new HashMap<String, String>() {{
            put("foo", "bar");
        }};

    @Test
    public void environmentConfigurationFallback() {
        EnvironmentConfiguration envConfiguration = new EnvironmentConfiguration(ENV_PROPS);
        Configuration configuration = new Configuration(Collections.emptyMap(), envConfiguration, null, null);
        assertEquals("bar", configuration.get("foo"));
        assertTrue(configuration.contains("foo"));
    }

    @Test
    public void environmentConfigurationFallbackNotFound() {
        EnvironmentConfiguration envConfiguration = new EnvironmentConfiguration(Collections.emptyMap());
        Configuration configuration = new Configuration(Collections.emptyMap(), envConfiguration, null, null);
        assertNull(configuration.get("foo"));
        assertFalse(configuration.contains("foo"));
    }

    @Test
    public void environmentConfigurationFallbackDefaultValue() {
        Map<String, String> props = new HashMap<>();
        props.put("foo", "42");

        EnvironmentConfiguration envConfiguration = new EnvironmentConfiguration(props);
        Configuration configuration = new Configuration(Collections.emptyMap(), envConfiguration, null, null);
        assertEquals(42, configuration.get("foo", 0));
        assertEquals(0, configuration.get("foo-not-found", 0));
    }

    @Test
    public void environmentConfigurationFallbackConverter() {
        Map<String, String> props = new HashMap<>();
        props.put("foo", "42");
        props.put("bar", "forty two");

        EnvironmentConfiguration envConfiguration = new EnvironmentConfiguration(props);
        Configuration configuration = new Configuration(Collections.emptyMap(), envConfiguration, null, null);
        Function<String, Integer> converter = Integer::parseInt;
        assertEquals(42, configuration.get("foo", converter));
        assertThrows(NumberFormatException.class, () -> configuration.get("bar", Integer::parseInt));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void environmentConfigurationFallbackRemove() {
        EnvironmentConfiguration envConfiguration = new EnvironmentConfiguration(ENV_PROPS);
        Configuration configuration = new Configuration(Collections.emptyMap(), envConfiguration, null, null);
        assertEquals("bar", configuration.get("foo"));

        configuration.remove("foo");
        assertFalse(envConfiguration.contains("foo"));
        assertFalse(configuration.contains("foo"));
        assertNull(envConfiguration.get("foo"));
        assertNull(configuration.get("foo"));
    }

    @Test
    public void getByNameBasic() {
        Configuration configuration = new TestConfigurationBuilder("foo", "bar").build();
        assertEquals("bar", configuration.get("foo"));
        assertTrue(configuration.contains("foo"));
    }

    @Test
    public void getByNameBasicNotFound() {
        Configuration configuration = new TestConfigurationBuilder().build();
        assertNull(configuration.get("foo"));
        assertFalse(configuration.contains("foo"));
    }

    @ParameterizedTest
    @MethodSource("getOrDefaultSupplier")
    public void getByNameImplicitConverter(String configurationValue, Object defaultValue, Object expectedValue) {
        Configuration configuration = new TestConfigurationBuilder("foo", configurationValue).build();

        assertEquals(expectedValue, configuration.get("foo", defaultValue));
    }

    @Test
    public void getByNameFallbackToDefault() {
        Configuration configuration = new TestConfigurationBuilder().build();
        assertEquals("0", configuration.get("foo", "0"));
        assertEquals(0, configuration.get("foo", 0));
    }

    @Test
    public void getByNameImplicitConverterThrows() {
        Configuration configuration = new TestConfigurationBuilder("foo", "forty two").build();
        assertThrows(NumberFormatException.class, () -> configuration.get("foo", 0));
    }

    @Test
    public void getByNameConverter() {
        Configuration configuration = new TestConfigurationBuilder("foo", "42", "bar", "forty two").build();
        Function<String, Integer> converter = Integer::parseInt;
        assertEquals(42, configuration.get("foo", converter));
        assertThrows(NumberFormatException.class, () -> configuration.get("bar", Integer::parseInt));
    }

    @Test
    public void getByNameFallbackToEnv() {
        Map<String, String> props = new HashMap<>();
        props.put("foo", "some value");
        props.put("bar", "baz");

        EnvironmentConfiguration envConfiguration = new EnvironmentConfiguration(props);

        Map<String, String> configurations = new HashMap<>();
        configurations.put("foo", "42");
        Configuration configuration = new Configuration(configurations, envConfiguration, null, null);

        assertEquals("42", configuration.get("foo"));
        assertEquals(42, configuration.get("foo", 0));
        Function<String, Integer> converter = Integer::parseInt;
        assertEquals(42, configuration.get("foo", converter));
        assertEquals("baz", configuration.get("bar"));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void removeOnlyAffectsEnvironmentForBackwardCompatibility() {
        Map<String, String> props = new HashMap<>();
        props.put("foo", "barEnv");
        EnvironmentConfiguration envConfiguration = new EnvironmentConfiguration(props);

        Map<String, String> configurations = new HashMap<>();
        configurations.put("foo", "bar");
        Configuration configuration = new Configuration(configurations, envConfiguration, null, null);

        configuration.remove("foo");
        assertFalse(envConfiguration.contains("foo"));
        assertTrue(configuration.contains("foo"));
        assertEquals("bar", configuration.get("foo"));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void putOnlyAffectsEnvironmentForBackwardCompatibility() {
        EnvironmentConfiguration envConfiguration = new EnvironmentConfiguration(Collections.emptyMap());

        Map<String, String> configurations = new HashMap<>();
        configurations.put("foo", "bar");
        Configuration configuration = new Configuration(configurations, envConfiguration, null, null);

        configuration
            .put("foo", "newBar")
            .put("baz", "42");

        assertTrue(configuration.contains("foo"));
        assertTrue(configuration.contains("baz"));
        assertEquals("bar", configuration.get("foo"));
        assertEquals("42", configuration.get("baz"));
    }

    @Test
    public void getLocalPropertyFromSection() {
        Configuration config = new TestConfigurationBuilder("appconfiguration.prop", "foo", "prop", "bar", "prop2", "baz")
            .buildSection("appconfiguration");

        ConfigurationProperty<String> localProp = ConfigurationProperty.stringPropertyBuilder("prop").build();
        ConfigurationProperty<String> localPropFullName = ConfigurationProperty.stringPropertyBuilder("appconfiguration.prop").build();
        ConfigurationProperty<String> globalProp = ConfigurationProperty.stringPropertyBuilder("prop").shared(true).build();
        ConfigurationProperty<String> globalProp2 = ConfigurationProperty.stringPropertyBuilder("prop2").shared(true).build();

        assertEquals("foo", config.get(localProp));
        assertEquals("foo", config.get(globalProp));
        assertEquals("baz", config.get(globalProp2));
        assertNull(config.get(localPropFullName));
    }

    @Test
    public void getGlobalPropertyFromDefaultsSection() {
        Configuration config = new TestConfigurationBuilder(
            "appconfiguration.prop", "local",
            "global.prop", "default")
            .buildSection("global");

        ConfigurationProperty<String> localProp = ConfigurationProperty.stringPropertyBuilder("prop").build();
        ConfigurationProperty<String> localPropFullName = ConfigurationProperty.stringPropertyBuilder("appconfiguration.prop").build();
        ConfigurationProperty<String> globalProp = ConfigurationProperty.stringPropertyBuilder("prop").shared(true).build();

        assertEquals("default", config.get(localProp));
        assertEquals("default", config.get(globalProp));
        assertNull(config.get(localPropFullName));
    }

    @Test
    public void getGlobalPropertyFromDefaultsAndRootSection() {
        Configuration config = new TestConfigurationBuilder("appconfiguration.prop", "local", "prop", "root")
            .build();

        ConfigurationProperty<String> localProp = ConfigurationProperty.stringPropertyBuilder("prop").build();
        ConfigurationProperty<String> localPropFullName = ConfigurationProperty.stringPropertyBuilder("appconfiguration.prop").build();
        ConfigurationProperty<String> globalProp = ConfigurationProperty.stringPropertyBuilder("prop").shared(true).build();

        assertEquals("root", config.get(localProp));
        assertEquals("root", config.get(globalProp));
        assertEquals("local", config.get(localPropFullName));
    }

    @Test
    public void getPropertyWithAlias() {
        ConfigurationProperty<String> prop = ConfigurationProperty.stringPropertyBuilder("prop").aliases("alias1", "alias2").build();

        Configuration config1 = new TestConfigurationBuilder("alias2", "a2")
            .build();
        assertTrue(config1.contains(prop));
        assertEquals("a2", config1.get(prop));

        Configuration config2 = new TestConfigurationBuilder("alias1", "a1", "alias2", "a2")
            .build();
        assertEquals("a1", config2.get(prop));

        Configuration config3 = new TestConfigurationBuilder("prop", "p", "alias1", "a1")
            .build();

        assertEquals("p", config3.get(prop));
    }

    @Test
    public void getPropertyWithEnvVar() {
        Map<String, String> props = new HashMap<>();
        props.put("env1", "e1");

        ConfigurationProperty<String> prop = ConfigurationProperty.stringPropertyBuilder("prop")
            .environmentAliases("env2", "env1")
            .build();

        EnvironmentConfiguration envConfig = new EnvironmentConfiguration(props);
        Configuration config1 = new Configuration(Collections.emptyMap(), envConfig, null, null);
        assertTrue(config1.contains(prop));
        assertEquals("e1", config1.get(prop));

        envConfig.put("env2", "e2");
        assertTrue(config1.contains(prop));
        assertEquals("e2", config1.get(prop));

        Map<String, String> configurations = new HashMap<>();
        configurations.put("prop", "p");

        Configuration config2 = new Configuration(configurations, envConfig, null, null);
        assertTrue(config2.contains(prop));
        assertEquals("p", config2.get(prop));
    }

    @ParameterizedTest
    @MethodSource("properties")
    public void getProperty(ConfigurationProperty<?> prop, String actual, Object expected, Object defaultValue) {
        Configuration config = new TestConfigurationBuilder("foo",  actual).build();
        assertTrue(config.contains(prop));
        assertEquals(expected, config.get(prop));
    }

    @ParameterizedTest
    @MethodSource("properties")
    public void getMissingProperty(ConfigurationProperty<?> missingProp, String actual, Object expected, Object defaultValue) {
        Configuration config = new TestConfigurationBuilder("foo",  actual).buildSection("az");
        assertFalse(config.contains(missingProp));
        assertEquals(defaultValue, config.get(missingProp));
    }

    @Test
    public void getRequiredMissingProperty() {
        Configuration config = new TestConfigurationBuilder().build();
        assertThrows(IllegalArgumentException.class, () -> config.get(ConfigurationProperty.stringPropertyBuilder("foo").required(true).build()));
    }

    @ParameterizedTest
    @MethodSource("validIntStrings")
    public void getValidIntProperty(String value, Integer expected) {
        Configuration config = new TestConfigurationBuilder("az.foo", value).buildSection("az");
        assertEquals(expected, config.get(ConfigurationProperty.integerPropertyBuilder("foo").build()));
    }

    @ParameterizedTest
    @MethodSource("invalidIntStrings")
    public void getInvalidIntProperty(String value) {
        Configuration config = new TestConfigurationBuilder("az.foo", value).buildSection("az");
        assertThrows(NumberFormatException.class, () -> config.get(ConfigurationProperty.integerPropertyBuilder("foo").build()));
    }

    @ParameterizedTest
    @MethodSource("validDurationStrings")
    public void getValidDurationProperty(String value, Duration expected) {
        Configuration config = new TestConfigurationBuilder("az.foo", value).buildSection("az");
        assertEquals(expected, config.get(ConfigurationProperty.durationPropertyBuilder("foo").build()));
    }

    @ParameterizedTest
    @MethodSource("invalidDurationStrings")
    public void getInvalidDurationProperty(String value) {
        Configuration config = new TestConfigurationBuilder("az.foo", value).buildSection("az");

        if (value.startsWith("-")) {
            assertThrows(IllegalArgumentException.class, () -> config.get(ConfigurationProperty.durationPropertyBuilder("foo").build()));
        } else {
            assertThrows(NumberFormatException.class, () -> config.get(ConfigurationProperty.durationPropertyBuilder("foo").build()));
        }
    }

    @Test
    public void getBooleanProperty() {

        Map<String, String> configurations = new HashMap<>();
        configurations.put("prop", "p");

        Configuration config = new TestConfigurationBuilder("az.true", "true", "az.false", "false", "az.anything-else", "anything-else").buildSection("az");

        assertTrue(config.get(ConfigurationProperty.booleanPropertyBuilder("true").build()));
        assertFalse(config.get(ConfigurationProperty.booleanPropertyBuilder("false").build()));
        assertFalse(config.get(ConfigurationProperty.booleanPropertyBuilder("anything-else").build()));
        assertNull(config.get(ConfigurationProperty.booleanPropertyBuilder("not-found").build()));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void cloneConfiguration() {
        Map<String, String> envConfigurations = new HashMap<>();
        envConfigurations.put("envVar1", "envVar1");
        envConfigurations.put("envVar2", "envVar2");

        EnvironmentConfiguration envConfig = new EnvironmentConfiguration(envConfigurations);
        Map<String, String> configurations = new HashMap<>();
        configurations.put("prop1", "prop1");
        configurations.put("prop2", "prop2");

        Configuration configuration = new Configuration(configurations, envConfig, null, null);

        Configuration configurationClone = configuration.clone();

        // Verify that the clone has the expected values.
        assertEquals(configuration.get("envVar1"), configurationClone.get("envVar1"));
        assertEquals(configuration.get("envVar2"), configurationClone.get("envVar2"));

        // The clone should be a separate instance, verify its modifications won't affect the original copy.
        configurationClone.remove("envVar2");
        assertTrue(configuration.contains("envVar2"));
    }

    private static Stream<Arguments> properties() {
        return Stream.of(
            Arguments.of(ConfigurationProperty.stringPropertyBuilder("foo").build(), "bar", "bar", null),
            Arguments.of(ConfigurationProperty.integerPropertyBuilder("foo").build(), "42", 42, null),
            Arguments.of(ConfigurationProperty.durationPropertyBuilder("foo").build(),  "2", Duration.ofMillis(2), null),
            Arguments.of(ConfigurationProperty.booleanPropertyBuilder("foo").build(), "true", true, null),
            Arguments.of(ConfigurationProperty.stringPropertyBuilder("foo").defaultValue("foo").build(), "bar", "bar", "foo"),
            Arguments.of(ConfigurationProperty.integerPropertyBuilder("foo").defaultValue(37).build(), "42", 42, 37),
            Arguments.of(ConfigurationProperty.durationPropertyBuilder("foo").defaultValue(Duration.ofMillis(1)).build(), "2", Duration.ofMillis(2), Duration.ofMillis(1)),
            Arguments.of(ConfigurationProperty.booleanPropertyBuilder("foo").defaultValue(false).build(), "true", true, false),
            Arguments.of(new ConfigurationProperty<Double>("foo", 0.1, false, v -> Double.parseDouble(v), false, null, null, false), "0.2", 0.2, 0.1)
        );
    }

    private static Stream<Arguments> validIntStrings() {
        return Stream.of(
            Arguments.of("123", 123),
            Arguments.of("-321", -321),
            Arguments.of("0", 0),
            Arguments.of("2147483647", Integer.MAX_VALUE)
        );
    }

    private static Stream<Arguments> invalidIntStrings() {
        return Stream.of(
            Arguments.of("0x5"),
            Arguments.of("not-a-string"),
            Arguments.of("2147483648")
        );
    }

    private static Stream<Arguments> validDurationStrings() {
        return Stream.of(
            Arguments.of("0", Duration.ofMillis(0)),
            Arguments.of("123", Duration.ofMillis(123)),
            Arguments.of("2147483648", Duration.ofMillis(2147483648L))
        );
    }

    private static Stream<Arguments> invalidDurationStrings() {
        return Stream.of(
            Arguments.of("-1", Duration.ofMillis(0)),
            Arguments.of("foo", Duration.ofMillis(123)),
            Arguments.of("9223372036854775808", Duration.ofMillis(2147483648L))
        );
    }


    private static Stream<Arguments> getOrDefaultSupplier() {
        return Stream.of(
            Arguments.of(String.valueOf((byte) 42), (byte) 12, (byte) 42),
            Arguments.of(String.valueOf((short) 42), (short) 12, (short) 42),
            Arguments.of(String.valueOf(42), 12, 42),
            Arguments.of(String.valueOf(42L), 12L, 42L),
            Arguments.of(String.valueOf(42F), 12F, 42F),
            Arguments.of(String.valueOf(42D), 12D, 42D),
            Arguments.of(String.valueOf(true), false, true),
            Arguments.of("42", "12", "42")
        );
    }
}

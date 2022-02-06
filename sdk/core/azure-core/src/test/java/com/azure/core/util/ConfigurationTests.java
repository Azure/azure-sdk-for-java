// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.stream.Stream;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_TRACING_DISABLED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the configuration API.
 */
public class ConfigurationTests {
    private static final String MY_CONFIGURATION = "myConfigurationABC123";
    private static final String EXPECTED_VALUE = "aConfigurationValueAbc123";
    private static final String UNEXPECTED_VALUE = "notMyConfigurationValueDef456";
    private static final String DEFAULT_VALUE = "theDefaultValueGhi789";

    private Configuration getEnvironmentTestConfiguration(String... props) {
        return new TestConfigurationBuilder().setEnv(props).build();
    }

    /**
     * Verifies that a runtime parameter is able to be retrieved.
     */
    @Test
    public void runtimeConfigurationFound() {
        assertEquals(EXPECTED_VALUE, getEnvironmentTestConfiguration(MY_CONFIGURATION, EXPECTED_VALUE).get(MY_CONFIGURATION));
    }

    /**
     * Verifies that null is returned when a configuration isn't found.
     */
    @Test
    public void configurationNotFound() {
        assertNull(getEnvironmentTestConfiguration().get(MY_CONFIGURATION));
    }


    /**
     * Verifies that a found configuration value is preferred over the default value.
     */
    @Test
    public void foundConfigurationPreferredOverDefault() {
        Configuration configuration = getEnvironmentTestConfiguration(MY_CONFIGURATION, EXPECTED_VALUE);

        assertEquals(EXPECTED_VALUE, configuration.get(MY_CONFIGURATION, DEFAULT_VALUE));
    }

    /**
     * Verifies that when a configuration value isn't found the default will be returned.
     */
    @Test
    public void fallbackToDefaultConfiguration() {
        assertEquals(DEFAULT_VALUE, getEnvironmentTestConfiguration().get(MY_CONFIGURATION, DEFAULT_VALUE));
    }

    /**
     * Verifies that a found configuration value is able to be mapped.
     */
    @Test
    public void foundConfigurationIsConverted() {
        Configuration configuration = getEnvironmentTestConfiguration(MY_CONFIGURATION, EXPECTED_VALUE);

        assertEquals(EXPECTED_VALUE.toUpperCase(), configuration.get(MY_CONFIGURATION, String::toUpperCase));
    }

    /**
     * Verifies that when a configuration isn't found the converter returns null.
     */
    @Test
    public void notFoundConfigurationIsConvertedToNull() {
        assertNull(getEnvironmentTestConfiguration().get(MY_CONFIGURATION, String::toUpperCase));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void cloneConfiguration() {
        Configuration configuration = new Configuration()
            .put("variable1", "value1")
            .put("variable2", "value2");

        Configuration configurationClone = configuration.clone();

        configuration.put("variable3", "value3");

        // Verify that the clone has the expected values.
        assertEquals(configuration.get("variable1"), configurationClone.get("variable1"));
        assertEquals(configuration.get("variable2"), configurationClone.get("variable2"));
        assertEquals("value3", configuration.get("variable3"));
        assertFalse(configurationClone.contains("variable3"));
        assertNull(configurationClone.get("variable3"));

        // The clone should be a separate instance, verify its modifications won't affect the original copy.
        configurationClone.remove("variable2");
        assertTrue(configuration.contains("variable2"));

        assertFalse(configurationClone.contains("variable2"));
        assertNull(configurationClone.get("variable2"));
    }

    @Test
    public void loadValueTwice() {
        Configuration configuration = getEnvironmentTestConfiguration();
        String tracingDisabled = configuration.get(PROPERTY_AZURE_TRACING_DISABLED);
        String tracingDisabled2 = configuration.get(PROPERTY_AZURE_TRACING_DISABLED);

        assertEquals(tracingDisabled, tracingDisabled2);
    }

    @ParameterizedTest
    @MethodSource("getOrDefaultSupplier")
    public void getOrDefault(String configurationValue, Object defaultValue, Object expectedValue) {
        Configuration configuration = getEnvironmentTestConfiguration("getOrDefault", configurationValue);
        assertEquals(expectedValue, configuration.get("getOrDefault", defaultValue));
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

    @Test
    public void getOrDefaultReturnsDefault() {
        assertEquals("42", getEnvironmentTestConfiguration().get("empty", "42"));
    }

    @Test
    public void getLocalPropertyFromSection() {
        Configuration config = getPropertyConfiguration("appconfiguration", "appconfiguration.prop", "foo", "prop", "bar", "prop2", "baz");

        ConfigurationProperty<String> localProp = ConfigurationProperty.stringPropertyBuilder("prop").build();
        ConfigurationProperty<String> localPropFullName = ConfigurationProperty.stringPropertyBuilder("appconfiguration.prop").build();
        ConfigurationProperty<String> globalProp = ConfigurationProperty.stringPropertyBuilder("prop").global(true).build();
        ConfigurationProperty<String> globalProp2 = ConfigurationProperty.stringPropertyBuilder("prop2").global(true).build();

        assertEquals("foo", config.get(localProp));
        assertEquals("foo", config.get(globalProp));
        assertEquals("baz", config.get(globalProp2));
        assertNull(config.get(localPropFullName));
    }

    @Test
    public void getGlobalPropertyFromDefaultsSection() {
        Configuration config = getPropertyConfiguration("defaults", "appconfiguration.prop", "local", "defaults.prop", "default", "root.prop", "root");

        ConfigurationProperty<String> localProp = ConfigurationProperty.stringPropertyBuilder("prop").build();
        ConfigurationProperty<String> localPropFullName = ConfigurationProperty.stringPropertyBuilder("appconfiguration.prop").build();
        ConfigurationProperty<String> globalProp = ConfigurationProperty.stringPropertyBuilder("prop").global(true).build();

        assertEquals("default", config.get(localProp));
        assertEquals("default", config.get(globalProp));
        assertNull(config.get(localPropFullName));
    }

    @Test
    public void getGlobalPropertyFromDefaultsAndRootSection() {
        Configuration config = getPropertyConfiguration(null, "appconfiguration.prop", "local", "prop", "root");

        ConfigurationProperty<String> localProp = ConfigurationProperty.stringPropertyBuilder("prop").build();
        ConfigurationProperty<String> localPropFullName = ConfigurationProperty.stringPropertyBuilder("appconfiguration.prop").build();
        ConfigurationProperty<String> globalProp = ConfigurationProperty.stringPropertyBuilder("prop").global(true).build();

        assertEquals("root", config.get(localProp));
        assertEquals("root", config.get(globalProp));
        assertEquals("local", config.get(localPropFullName));
    }

    @Test
    public void getPropertyWithAlias() {
        ConfigurationProperty<String> prop = ConfigurationProperty.stringPropertyBuilder("prop").aliases("alias1", "alias2").build();

        Configuration config1 = getPropertyConfiguration(null, "alias2", "a2");
        assertTrue(config1.contains(prop));
        assertEquals("a2", config1.get(prop));

        Configuration config2 = getPropertyConfiguration(null, "alias1", "a1", "alias2", "a2");
        assertTrue(config2.contains(prop));
        assertEquals("a1", config2.get(prop));

        Configuration config3 = getPropertyConfiguration(null, "prop", "p", "alias1", "a1");
        assertTrue(config3.contains(prop));
        assertEquals("p", config3.get(prop));
    }

    @Test
    public void getPropertyWithEnvVar() {

        ConfigurationProperty<String> prop = ConfigurationProperty.stringPropertyBuilder("prop").environmentVariables("env1", "env2").build();
        Configuration config1 = new TestConfigurationBuilder().setEnv("env2", "e2").build();
        assertTrue(config1.contains(prop));
        assertEquals("e2", config1.get(prop));

        Configuration config2 = new TestConfigurationBuilder().setEnv("env1", "e1", "env2", "e2").build();
        assertTrue(config2.contains(prop));
        assertEquals("e1", config2.get(prop));

        Configuration config3 = new TestConfigurationBuilder("prop", "p").setEnv("env1", "e1").build();
        assertTrue(config3.contains(prop));
        assertEquals("p", config3.get(prop));
    }

    @ParameterizedTest
    @MethodSource("properties")
    public void getProperty(ConfigurationProperty<?> prop, String actual, Object expected, Object defaultValue) {
        Configuration config = getPropertyConfiguration(null, "foo",  actual);
        assertTrue(config.contains(prop));
        assertEquals(expected, config.get(prop));
    }

    @ParameterizedTest
    @MethodSource("properties")
    public void getMissingProperty(ConfigurationProperty<?> missingProp, String actual, Object expected, Object defaultValue) {
        Configuration config = getPropertyConfiguration("az");
        assertFalse(config.contains(missingProp));
        assertEquals(defaultValue, config.get(missingProp));
    }

    @Test
    public void getRequiredMissingProperty() {
        Configuration config = getPropertyConfiguration("az");
        //TODO better exception
        assertThrows(IllegalArgumentException.class, () -> config.get(ConfigurationProperty.stringPropertyBuilder("foo").required(true).build()));
    }

    @ParameterizedTest
    @MethodSource("validIntStrings")
    public void getValidIntProperty(String value, Integer expected) {
        Configuration config = getPropertyConfiguration("az", "az.foo", value);
        assertEquals(expected, config.get(ConfigurationProperty.integerPropertyBuilder("foo").build()));
    }

    @ParameterizedTest
    @MethodSource("invalidIntStrings")
    public void getInvalidIntProperty(String value) {
        Configuration config = getPropertyConfiguration("az", "az.foo", value);
        assertThrows(NumberFormatException.class, () -> config.get(ConfigurationProperty.integerPropertyBuilder("foo").build()));
    }


    @ParameterizedTest
    @MethodSource("validDurationStrings")
    public void getValidDurationProperty(String value, Duration expected) {
        Configuration config = getPropertyConfiguration("az", "az.foo", value);
        assertEquals(expected, config.get(ConfigurationProperty.durationPropertyBuilder("foo").build()));
    }

    @ParameterizedTest
    @MethodSource("invalidDurationStrings")
    public void getInvalidDurationProperty(String value) {
        Configuration config = getPropertyConfiguration("az", "az.foo", value);

        if (value.startsWith("-")) {
            // todo better exception
            assertThrows(IllegalArgumentException.class, () -> config.get(ConfigurationProperty.durationPropertyBuilder("foo").build()));
        } else {
            assertThrows(NumberFormatException.class, () -> config.get(ConfigurationProperty.durationPropertyBuilder("foo").build()));
        }
    }

    @Test
    public void getBooleanProperty() {
        Configuration config = getPropertyConfiguration("az", "az.true", "true", "az.false", "false", "az.anything-else", "anything-else", "az.null", null);

        assertTrue(config.get(ConfigurationProperty.booleanPropertyBuilder("true").build()));
        assertFalse(config.get(ConfigurationProperty.booleanPropertyBuilder("false").build()));
        assertFalse(config.get(ConfigurationProperty.booleanPropertyBuilder("anything-else").build()));
        assertNull(config.get(ConfigurationProperty.booleanPropertyBuilder("null").build()));
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
            Arguments.of(""),
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

    private Configuration getPropertyConfiguration(String section, String... testData) {
        ConfigurationSource source = new TestConfigurationSource(testData);
        ConfigurationBuilder configBuilder = new ConfigurationBuilder(source);
        if (section == null) {
            return configBuilder.build();
        }

        return configBuilder.buildSection(section);
    }
}

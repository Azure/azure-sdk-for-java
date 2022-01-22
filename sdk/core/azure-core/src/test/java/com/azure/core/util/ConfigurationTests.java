// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.http.policy.RetryPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_TRACING_DISABLED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        // TODO test env source
        assertEquals(EXPECTED_VALUE, getEnvironmentTestConfiguration(MY_CONFIGURATION, EXPECTED_VALUE).get(MY_CONFIGURATION));
    }

    /**
     * Verifies that null is returned when a configuration isn't found.
     */
    @Test
    public void configurationNotFound() {
        assertNull(new ConfigurationBuilder(new TestConfigurationSource()).build().get(MY_CONFIGURATION));
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
        Configuration configuration = new Configuration();

        assertEquals(DEFAULT_VALUE, configuration.get(MY_CONFIGURATION, DEFAULT_VALUE));
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
        Configuration configuration = getEnvironmentTestConfiguration("variable1", "value1", "variable2", "value2");

        Configuration configurationClone = configuration.clone();

        // Verify that the clone has the expected values.
        assertEquals(configuration.get("variable1"), configurationClone.get("variable1"));
        assertEquals(configuration.get("variable2"), configurationClone.get("variable2"));

        // The clone should be a separate instance, verify its modifications won't affect the original copy.
        configurationClone.remove("variable2");
        assertTrue(configuration.contains("variable2"));
    }

    @Test
    public void loadValueTwice() {
        Configuration configuration = new Configuration();
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
        assertEquals("42", new Configuration().get("empty", "42"));
    }

    @Test
    public void getProperty() {
        ConfigurationSource source = new TestConfigurationSource("appconfiguration.prop", "local-prop-value");
        ConfigurationBuilder configBuilder = new ConfigurationBuilder(source);

        Configuration root = configBuilder.build();
        Configuration appconfigSection = configBuilder.clientSection("appconfiguration").build();

        ConfigurationProperty<String> localProp = ConfigurationProperty.stringPropertyBuilder("prop").build();
        ConfigurationProperty<String> localPropFullName = ConfigurationProperty.stringPropertyBuilder("appconfiguration.prop").build();
        ConfigurationProperty<String> globalProp = ConfigurationProperty.stringPropertyBuilder("prop").global(true).build();

        assertNull(root.get(localProp));
        assertEquals("local-prop-value", appconfigSection.get(localProp));

        assertEquals("local-prop-value", root.get(localPropFullName));
        assertNull(appconfigSection.get(localPropFullName));

        assertNull(root.get(globalProp));
        assertEquals("local-prop-value", appconfigSection.get(globalProp));
    }

    @Test
    public void getMissingProperty() {
        ConfigurationSource source = new TestConfigurationSource("az.appconfiguration.prop", "local-prop-value");
        ConfigurationBuilder configBuilder = new ConfigurationBuilder(source)
            .root("az");

        Configuration root = configBuilder.build();
        Configuration appconfigSection = configBuilder.clientSection("appconfiguration").build();

        ConfigurationProperty<String> localProp = ConfigurationProperty.stringPropertyBuilder("foo").build();
        ConfigurationProperty<String> localPropFullName = ConfigurationProperty.stringPropertyBuilder("storage.prop").build();

        assertNull(root.get(localProp));
        assertNull(root.get(localPropFullName));

        assertNull(appconfigSection.get(localProp));
        assertNull(appconfigSection.get(localPropFullName));
    }

    @Test
    public void localPropertyGoesFirst() {
        ConfigurationSource source = new TestConfigurationSource("az.foo.prop", "local", "az.prop", "global");
        ConfigurationBuilder configBuilder = new ConfigurationBuilder(source)
            .root("az");

        Configuration root = configBuilder.build();
        Configuration appconfigSection = configBuilder
            .clientSection("foo")
            .build();

        ConfigurationProperty<String> localProp = ConfigurationProperty.stringPropertyBuilder("prop").build();
        ConfigurationProperty<String> localPropFullName = ConfigurationProperty.stringPropertyBuilder("foo.prop").build();
        ConfigurationProperty<String> globalProp = ConfigurationProperty.stringPropertyBuilder("prop").build();

        assertEquals("global", root.get(localProp));
        assertEquals("local", root.get(localPropFullName));
        assertEquals("global", root.get(globalProp));

        assertEquals("local", appconfigSection.get(localProp));
        assertNull(appconfigSection.get(localPropFullName));
        assertEquals("local", appconfigSection.get(globalProp));
    }

    @Test
    public void getGlobalProperty() {
        ConfigurationSource source = new TestConfigurationSource("az.storage.prop", "local", "az.prop", "global");

        ConfigurationBuilder configBuilder = new ConfigurationBuilder(source)
            .root("az");

        Configuration root = configBuilder.build();
        Configuration appconfigSection = configBuilder
            .clientSection("appconfiguration")
            .build();

        ConfigurationProperty<String> globalProp = ConfigurationProperty.stringPropertyBuilder("prop").global(true).build();
        ConfigurationProperty<String> globalPropFullName = ConfigurationProperty.stringPropertyBuilder("appconfiguration.prop").global(true).build();

        assertEquals("global", root.get(globalProp));
        assertNull(root.get(globalPropFullName));

        assertEquals("global", appconfigSection.get(globalProp));
        assertNull(appconfigSection.get(globalPropFullName));
    }

    @Test
    public void multipleNestedSections() {
        ConfigurationSource source = new TestConfigurationSource(
            "http-retry.mode", "fixed",
            "appconfiguration.http-retry.fixed.max-retries", "1",
            "appconfiguration.http-retry.fixed.delay", "1000");

        ConfigurationBuilder configBuilder = new ConfigurationBuilder(source);
        Configuration root = configBuilder.build();
        Configuration appconfigSection = configBuilder.clientSection("appconfiguration").build();

        ConfigurationProperty<String> globalProp = ConfigurationProperty.stringPropertyBuilder("http-retry.mode").global(true).build();
        ConfigurationProperty<String> globalMissingProp = ConfigurationProperty.stringPropertyBuilder("mode").global(true).build();
        ConfigurationProperty<String> globalMaxTries = ConfigurationProperty.stringPropertyBuilder("http-retry.fixed.max-retries").global(true).build();

        assertNull(root.get(globalMissingProp));
        assertEquals("fixed", root.get(globalProp));
        assertEquals("fixed", appconfigSection.get(globalProp));

        assertNull(root.get(globalMaxTries));
        assertEquals("1", appconfigSection.get(globalMaxTries));

        assertNotNull(RetryPolicy.fromConfiguration(appconfigSection, null));

        // todo : should throw
        assertThrows(Throwable.class, () -> RetryPolicy.fromConfiguration(root, null));
    }
}

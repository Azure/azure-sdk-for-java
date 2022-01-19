// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.http.policy.RetryPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_TRACING_DISABLED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests the configuration API.
 */
public class ConfigurationTests {
    private static final String MY_CONFIGURATION = "myConfigurationABC123";
    private static final String EXPECTED_VALUE = "aConfigurationValueAbc123";
    private static final String UNEXPECTED_VALUE = "notMyConfigurationValueDef456";
    private static final String DEFAULT_VALUE = "theDefaultValueGhi789";

    /**
     * Verifies that a runtime parameter is able to be retrieved.
     */
    @Test
    public void runtimeConfigurationFound() {
        Configuration configuration = spy(Configuration.class);
        when(configuration.loadFromProperties(MY_CONFIGURATION)).thenReturn(EXPECTED_VALUE);
        when(configuration.loadFromEnvironment(MY_CONFIGURATION)).thenReturn(null);

        assertEquals(EXPECTED_VALUE, configuration.get(MY_CONFIGURATION));
    }

    /**
     * Verifies that an environment variable is able to be retrieved.
     */
    @Test
    public void environmentConfigurationFound() {
        Configuration configuration = spy(Configuration.class);
        when(configuration.loadFromProperties(MY_CONFIGURATION)).thenReturn(null);
        when(configuration.loadFromEnvironment(MY_CONFIGURATION)).thenReturn(EXPECTED_VALUE);

        assertEquals(EXPECTED_VALUE, configuration.get(MY_CONFIGURATION));
    }

    /**
     * Verifies that null is returned when a configuration isn't found.
     */
    @Test
    public void configurationNotFound() {
        Configuration configuration = new Configuration();
        assertNull(configuration.get(MY_CONFIGURATION));
    }

    /**
     * Verifies that runtime parameters are preferred over environment variables.
     */
    @Test
    public void runtimeConfigurationPreferredOverEnvironmentConfiguration() {
        Configuration configuration = spy(Configuration.class);
        when(configuration.loadFromProperties(MY_CONFIGURATION)).thenReturn(EXPECTED_VALUE);
        when(configuration.loadFromEnvironment(MY_CONFIGURATION)).thenReturn(UNEXPECTED_VALUE);

        assertEquals(EXPECTED_VALUE, configuration.get(MY_CONFIGURATION));
    }

    /**
     * Verifies that a found configuration value is preferred over the default value.
     */
    @Test
    public void foundConfigurationPreferredOverDefault() {
        Configuration configuration = spy(Configuration.class);
        when(configuration.loadFromEnvironment(MY_CONFIGURATION)).thenReturn(EXPECTED_VALUE);

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
        Configuration configuration = spy(Configuration.class);
        when(configuration.loadFromProperties(MY_CONFIGURATION)).thenReturn(EXPECTED_VALUE);

        assertEquals(EXPECTED_VALUE.toUpperCase(), configuration.get(MY_CONFIGURATION, String::toUpperCase));
    }

    /**
     * Verifies that when a configuration isn't found the converter returns null.
     */
    @Test
    public void notFoundConfigurationIsConvertedToNull() {
        assertNull(new Configuration().get(MY_CONFIGURATION, String::toUpperCase));
    }

    @Test
    public void cloneConfiguration() {
        Configuration configuration = new Configuration()
            .put("variable1", "value1")
            .put("variable2", "value2");

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
        Configuration configuration = new Configuration()
            .put("getOrDefault", configurationValue);

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
        ConfigurationProvider provider = new ConfigurationProvider(new TestConfigurationSource("appconfiguration.prop", "local-prop-value"));
        ImmutableConfiguration defaults = provider.getDefaultsSection(null);
        ImmutableConfiguration appconfigSection = provider.getClientSection("appconfiguration", defaults);

        ConfigurationProperty<String> localProp = ConfigurationProperty.stringLocalProperty("prop", null);
        ConfigurationProperty<String> localPropFullName = ConfigurationProperty.stringLocalProperty("appconfiguration.prop", null);
        ConfigurationProperty<String> globalProp = ConfigurationProperty.stringGlobalProperty("prop", null);

        assertNull(defaults.get(localProp));
        assertEquals("local-prop-value", appconfigSection.get(localProp));

        assertEquals("local-prop-value", defaults.get(localPropFullName));
        assertNull(appconfigSection.get(localPropFullName));

        assertNull(defaults.get(globalProp));
        assertEquals("local-prop-value", appconfigSection.get(globalProp));
    }

    @Test
    public void getMissingProperty() {
        ConfigurationProvider provider = new ConfigurationProvider(new TestConfigurationSource("az.appconfiguration.prop", "local-prop-value"));
        ImmutableConfiguration defaults = provider.getDefaultsSection("az");
        ImmutableConfiguration appconfigSection = provider.getClientSection("az.appconfiguration", defaults);

        ConfigurationProperty<String> localProp = ConfigurationProperty.stringLocalProperty("foo", null);
        ConfigurationProperty<String> localPropFullName = ConfigurationProperty.stringLocalProperty("storage.prop", null);

        assertNull(defaults.get(localProp));
        assertNull(defaults.get(localPropFullName));

        assertNull(appconfigSection.get(localProp));
        assertNull(appconfigSection.get(localPropFullName));
    }

    @Test
    public void localPropertyGoesFirst() {
        ConfigurationProvider provider = new ConfigurationProvider(new TestConfigurationSource("az.appconfiguration.prop", "local", "az.prop", "global"));
        ImmutableConfiguration defaults = provider.getDefaultsSection("az");
        ImmutableConfiguration appconfigSection = provider.getClientSection("az.appconfiguration", defaults);

        ConfigurationProperty<String> localProp = ConfigurationProperty.stringLocalProperty("prop", null);
        ConfigurationProperty<String> localPropFullName = ConfigurationProperty.stringLocalProperty("appconfiguration.prop", null);
        ConfigurationProperty<String> globalProp = ConfigurationProperty.stringGlobalProperty("prop", null);

        assertEquals("global", defaults.get(localProp));
        assertEquals("local", defaults.get(localPropFullName));
        assertEquals("global", defaults.get(globalProp));

        assertEquals("local", appconfigSection.get(localProp));
        assertNull(appconfigSection.get(localPropFullName));
        assertEquals("local", appconfigSection.get(globalProp));
    }

    @Test
    public void getGlobalProperty() {
        ConfigurationProvider provider = new ConfigurationProvider(new TestConfigurationSource("az.storage.prop", "local", "az.prop", "global"));
        ImmutableConfiguration defaults = provider.getDefaultsSection("az");
        ImmutableConfiguration appconfigSection = provider.getClientSection("az.appconfiguration", defaults);

        ConfigurationProperty<String> globalProp = ConfigurationProperty.stringGlobalProperty("prop", null);
        ConfigurationProperty<String> globalPropFullName = ConfigurationProperty.stringGlobalProperty("appconfiguration.prop", null);

        assertEquals("global", defaults.get(globalProp));
        assertNull(defaults.get(globalPropFullName));

        assertEquals("global", appconfigSection.get(globalProp));
        assertNull(appconfigSection.get(globalPropFullName));
    }

    @Test
    public void multipleNestedSections() throws Exception {
        ConfigurationProvider provider = new ConfigurationProvider(new TestConfigurationSource()
            .put("http-retry.mode", "fixed")
            .put("appconfiguration.http-retry.fixed.max-retries", "1")
            .put("appconfiguration.http-retry.fixed.delay", "PT1S"));

        ImmutableConfiguration defaults = provider.getDefaultsSection(null);
        ImmutableConfiguration appconfigSection = provider.getClientSection("appconfiguration", defaults);

        ConfigurationProperty<String> globalProp = ConfigurationProperty.stringGlobalProperty("http-retry.mode", null);
        ConfigurationProperty<String> globalMissingProp = ConfigurationProperty.stringGlobalProperty("mode", null);
        ConfigurationProperty<String> globalMaxTries = ConfigurationProperty.stringGlobalProperty("http-retry.fixed.max-retries", null);

        assertNull(defaults.get(globalMissingProp));
        assertEquals("fixed", defaults.get(globalProp));
        assertEquals("fixed", appconfigSection.get(globalProp));

        assertNull(defaults.get(globalMaxTries));
        assertEquals("1", appconfigSection.get(globalMaxTries));

        assertNotNull(RetryPolicy.fromConfiguration(appconfigSection, null));

        // todo : should throw
        assertThrows(Throwable.class, () -> RetryPolicy.fromConfiguration(defaults, null));
    }
/*
    @Test
    public void getPropertySanityTODO() {
        ImmutableConfiguration configuration = Configuration.fromSource(new TestConfigurationSource("az.appconfiguration.prop", "local-prop-value", "az.prop", "global"), "az");

        ImmutableConfiguration appconfigSection = configuration.getSection("appconfiguration");

        ConfigurationProperty<String> appconfigProp = new ConfigurationProperty<>("prop", v -> v, true, null, null);
        ConfigurationProperty<String> globalProp = new ConfigurationProperty<>("prop", v -> v, false, null, null);

        assertEquals("local-prop-value", configuration.get(appconfigProp));
        assertEquals("global", configuration.get(globalProp));

        assertEquals("local-prop-value", appconfigSection.get(appconfigProp));
        assertEquals("local-prop-value", appconfigSection.get(globalProp));

        ImmutableConfiguration configuration2 = Configuration.fromSource(new TestConfigurationSource("az.prop", "global"), "az");
        ImmutableConfiguration appconfigSection2 = configuration2.getSection("appconfiguration");
        assertEquals("global", configuration2.get(globalProp));
        assertNull(configuration2.get(appconfigProp));
        assertNull(appconfigSection2.get(appconfigProp));
    }



    @Test
    public void readComplexObjectSanityCheck() throws Exception {
        ImmutableConfiguration configuration = new ImmutableConfiguration(new TestConfigurationSource()
            .put("http-retry.mode", "exponential")
            .put("http-retry.exponential.max-retries", "7")
            .put("http-retry.exponential.base-delay", "PT1S")
            .put("http-retry.retry-after-header", "retry-after")
            .put("http-retry.retry-after-time-unit", "MILLIS"), null);

        RetryPolicy retryPolicy = RetryPolicy.fromConfiguration(configuration, null);

        ImmutableConfiguration appconfig = new ImmutableConfiguration(new TestConfigurationSource()
                .put("http-retry.mode", "fixed")
                .put("appconfiguration.http-retry.fixed.max-retries", "1")
                .put("appconfiguration.http-retry.fixed.delay", "PT1S"), null)
            .getSection("appconfiguration");

        RetryPolicy retryPolicyAppConfig = RetryPolicy.fromConfiguration(appconfig, null);

        // compare in debug
    }*/

    private static class TestConfigurationSource implements ConfigurationSource {
        private Map<String, String> testData;

        public TestConfigurationSource(Map<String, String> testData) {
            this.testData = testData;
        }

        public TestConfigurationSource(String... testData) {
            this.testData = new HashMap<>();
            for (int i = 0; i < testData.length; i +=2) {
                this.testData.put(testData[i], testData[i + 1]);
            }
        }

        @Override
        public Iterable<String> getValues(String prefix) {
            if (prefix == null) {
                return testData.keySet();
            }
            return testData.keySet().stream().filter(k -> k.startsWith(prefix + ".")).collect(Collectors.toList());
        }

        @Override
        public String getValue(String propertyName) {
            return testData.get(propertyName);
        }

        public TestConfigurationSource put(String key, String value) {
            testData.put(key, value);
            return this;
        }
    }
}

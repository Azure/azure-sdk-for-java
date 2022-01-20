// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.logging.ClientLogger;
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

/**
 * Tests the configuration API.
 */
public class ConfigurationTests {
    private static final String MY_CONFIGURATION = "myConfigurationABC123";
    private static final String EXPECTED_VALUE = "aConfigurationValueAbc123";
    private static final String UNEXPECTED_VALUE = "notMyConfigurationValueDef456";
    private static final String DEFAULT_VALUE = "theDefaultValueGhi789";

    private static final ClientLogger LOGGER = new ClientLogger(ConfigurationTests.class);
    /**
     * Verifies that a runtime parameter is able to be retrieved.
     */
    @Test
    public void runtimeConfigurationFound() {
        // TODO test env source
        Configuration configuration = new ConfigurationProvider(new TestConfigurationSource()
            .put(MY_CONFIGURATION, EXPECTED_VALUE))
            .getClientSection(null);

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
     * Verifies that a found configuration value is preferred over the default value.
     */
    @Test
    public void foundConfigurationPreferredOverDefault() {
        Configuration configuration = new ConfigurationProvider(new TestConfigurationSource()
            .put(MY_CONFIGURATION, EXPECTED_VALUE))
            .getClientSection(null);

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
        Configuration configuration = new ConfigurationProvider(new TestConfigurationSource()
            .put(MY_CONFIGURATION, EXPECTED_VALUE))
            .getClientSection(null);

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
    @SuppressWarnings("deprecation")
    public void cloneConfiguration() {
        Configuration configuration = new ConfigurationProvider(new TestConfigurationSource()
            .put("variable1", "value1")
            .put("variable2", "value2"))
            .getClientSection(null);

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
        Configuration configuration = new ConfigurationProvider(new TestConfigurationSource()
            .put("getOrDefault", configurationValue))
            .getClientSection(null);

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
        Configuration defaults = provider.getClientSection(null);
        Configuration appconfigSection = provider.getClientSection("appconfiguration");

        ConfigurationProperty<String> localProp = ConfigurationProperty.stringLocalProperty("prop", null, LOGGER);
        ConfigurationProperty<String> localPropFullName = ConfigurationProperty.stringLocalProperty("appconfiguration.prop", null, LOGGER);
        ConfigurationProperty<String> globalProp = ConfigurationProperty.stringProperty("prop", null, null, LOGGER);

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
        Configuration defaults = provider.getClientSection("az");
        Configuration appconfigSection = provider.getClientSection("az.appconfiguration");

        ConfigurationProperty<String> localProp = ConfigurationProperty.stringLocalProperty("foo", null, LOGGER);
        ConfigurationProperty<String> localPropFullName = ConfigurationProperty.stringLocalProperty("storage.prop", null, LOGGER);

        assertNull(defaults.get(localProp));
        assertNull(defaults.get(localPropFullName));

        assertNull(appconfigSection.get(localProp));
        assertNull(appconfigSection.get(localPropFullName));
    }

    @Test
    public void localPropertyGoesFirst() {
        ConfigurationProvider provider = new ConfigurationProvider(new TestConfigurationSource("az.appconfiguration.prop", "local", "az.prop", "global"));
        Configuration defaults = provider.getClientSection("az");
        Configuration appconfigSection = provider.getClientSection("az.appconfiguration");

        ConfigurationProperty<String> localProp = ConfigurationProperty.stringLocalProperty("prop", null, LOGGER);
        ConfigurationProperty<String> localPropFullName = ConfigurationProperty.stringLocalProperty("appconfiguration.prop", null, LOGGER);
        ConfigurationProperty<String> globalProp = ConfigurationProperty.stringProperty("prop", null, null, LOGGER);

        assertEquals("global", defaults.get(localProp));
        assertEquals("local", defaults.get(localPropFullName));
        assertEquals("global", defaults.get(globalProp));

        assertEquals("local", appconfigSection.get(localProp));
        assertNull(appconfigSection.get(localPropFullName));
        assertEquals("local", appconfigSection.get(globalProp));
    }

    @Test
    public void getGlobalProperty() {
        ConfigurationProvider provider = new ConfigurationProvider(new TestConfigurationSource("az.storage.prop", "local", "az.prop", "global"), "az");
        Configuration defaults = provider.getClientSection("az");
        Configuration appconfigSection = provider.getClientSection("az.appconfiguration");

        ConfigurationProperty<String> globalProp = ConfigurationProperty.stringProperty("prop", null, null, LOGGER);
        ConfigurationProperty<String> globalPropFullName = ConfigurationProperty.stringProperty("appconfiguration.prop", null, null, LOGGER);

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
            .put("appconfiguration.http-retry.fixed.delay", "1000"));

        Configuration defaults = provider.getClientSection(null);
        Configuration appconfigSection = provider.getClientSection("appconfiguration");

        ConfigurationProperty<String> globalProp = ConfigurationProperty.stringProperty("http-retry.mode", null, null, LOGGER);
        ConfigurationProperty<String> globalMissingProp = ConfigurationProperty.stringProperty("mode", null, null, LOGGER);
        ConfigurationProperty<String> globalMaxTries = ConfigurationProperty.stringProperty("http-retry.fixed.max-retries", null, null, LOGGER);

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
        Configuration configuration = Configuration.fromSource(new TestConfigurationSource("az.appconfiguration.prop", "local-prop-value", "az.prop", "global"), "az");

        Configuration appconfigSection = configuration.getSection("appconfiguration");

        ConfigurationProperty<String> appconfigProp = new ConfigurationProperty<>("prop", v -> v, true, null, null);
        ConfigurationProperty<String> globalProp = new ConfigurationProperty<>("prop", v -> v, false, null, null);

        assertEquals("local-prop-value", configuration.get(appconfigProp));
        assertEquals("global", configuration.get(globalProp));

        assertEquals("local-prop-value", appconfigSection.get(appconfigProp));
        assertEquals("local-prop-value", appconfigSection.get(globalProp));

        Configuration configuration2 = Configuration.fromSource(new TestConfigurationSource("az.prop", "global"), "az");
        Configuration appconfigSection2 = configuration2.getSection("appconfiguration");
        assertEquals("global", configuration2.get(globalProp));
        assertNull(configuration2.get(appconfigProp));
        assertNull(appconfigSection2.get(appconfigProp));
    }



    @Test
    public void readComplexObjectSanityCheck() throws Exception {
        Configuration configuration = new Configuration(new TestConfigurationSource()
            .put("http-retry.mode", "exponential")
            .put("http-retry.exponential.max-retries", "7")
            .put("http-retry.exponential.base-delay", "PT1S")
            .put("http-retry.retry-after-header", "retry-after")
            .put("http-retry.retry-after-time-unit", "MILLIS"), null);

        RetryPolicy retryPolicy = RetryPolicy.fromConfiguration(configuration, null);

        Configuration appconfig = new Configuration(new TestConfigurationSource()
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

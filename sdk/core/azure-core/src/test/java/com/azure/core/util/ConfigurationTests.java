// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.RetryStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_TRACING_DISABLED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the configuration API.
 */
public class ConfigurationTests {
    private static final String MY_CONFIGURATION = "myConfigurationABC123";
    private static final String EXPECTED_VALUE = "aConfigurationValueAbc123";
    private static final String UNEXPECTED_VALUE = "notMyConfigurationValueDef456";
    private static final String DEFAULT_VALUE = "theDefaultValueGhi789";

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
        public String getValue(String propertyName) {
            return testData.get(propertyName);
        }
    }

    private final static TestConfigurationSource DEFAULT_TEST_DATA_SOURCE = new TestConfigurationSource(MY_CONFIGURATION, EXPECTED_VALUE);

    /**
     * Verifies that a runtime parameter is able to be retrieved.
     */
    @Test
    public void configurationFound() {
        Configuration configuration = new Configuration(DEFAULT_TEST_DATA_SOURCE);

        assertEquals(EXPECTED_VALUE, configuration.get(MY_CONFIGURATION));
    }

    /**
     * Verifies that an environment variable is able to be retrieved.
     */
    /*@Test
    public void environmentConfigurationFound() {
        Configuration configuration = spy(Configuration.class);
        when(configuration.loadFromProperties(MY_CONFIGURATION)).thenReturn(null);
        when(configuration.loadFromEnvironment(MY_CONFIGURATION)).thenReturn(EXPECTED_VALUE);

        assertEquals(EXPECTED_VALUE, configuration.get(MY_CONFIGURATION));
    }*/

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
   /* @Test
    public void runtimeConfigurationPreferredOverEnvironmentConfiguration() {
        Configuration configuration = spy(Configuration.class);
        when(configuration.loadFromProperties(MY_CONFIGURATION)).thenReturn(EXPECTED_VALUE);
        when(configuration.loadFromEnvironment(MY_CONFIGURATION)).thenReturn(UNEXPECTED_VALUE);

        assertEquals(EXPECTED_VALUE, configuration.get(MY_CONFIGURATION));
    }*/

    /**
     * Verifies that a found configuration value is preferred over the default value.
     */
    @Test
    public void foundConfigurationPreferredOverDefault() {
        Configuration configuration = new Configuration(DEFAULT_TEST_DATA_SOURCE);

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
        Configuration configuration = new Configuration(DEFAULT_TEST_DATA_SOURCE);

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
    public void getLocalProperty() {
        Configuration configuration = new Configuration()
            .put("appconfiguration.connection-string", "connection-string-value")
            .put("connection-string", "not-azure-sdk-config");

        ConfigurationProperty appConfigCs = new ConfigurationProperty("appconfiguration", "connection-string", true);

        assertEquals("connection-string-value", configuration.get("appconfiguration.connection-string"));
        assertEquals("connection-string-value", configuration.get(appConfigCs));

        configuration.remove("appconfiguration.connection-string");

        assertNull(configuration.get("appconfiguration.connection-string"));
        assertNull(configuration.get(appConfigCs));
    }

    @Test
    public void getGlobalProperty() {
        Configuration configuration = new Configuration()
            .put("appconfiguration.http-client.application-id", "appconfig-app-id")
            .put("http-client.application-id", "http-app-id");

        assertEquals("appconfig-app-id", configuration.get(new ConfigurationProperty("appconfiguration", "http-client.application-id", false)));
        assertEquals("http-app-id", configuration.get(new ConfigurationProperty(null, "http-client.application-id", false)));

        configuration.remove("appconfiguration.http-client.application-id");
        assertEquals("http-app-id", configuration.get(new ConfigurationProperty("appconfiguration", "http-client.application-id", false)));
        assertEquals("http-app-id", configuration.get(new ConfigurationProperty(null, "http-client.application-id", false)));
    }


    @Test
    public void readComplexObject() throws Exception {
        Configuration configuration = new Configuration()
            .put("http.retry.strategy", "exponential")
            .put("http.retry.strategy.exponential.max-retries", "7")
            .put("http.retry.strategy.exponential.base-delay", "PT1S")
            .put("http.retry.strategy.exponential.max-delay", "PT2S")
            .put("http.retry.retry-after-header", "retry-after")
            .put("http.retry.retry-after-time-unit", "MILLIS");

        RetryPolicy retryPolicy = configureRetryPolicy(configuration);
    }

    private static RetryPolicy configureRetryPolicy(Configuration configuration) throws Exception {
        // belong to RetryPolicy
        ConfigurationProperty retryHeaderProp = new ConfigurationProperty(null, "http.retry.retry-after-header", false);
        ConfigurationProperty retryTimeUnitProp = new ConfigurationProperty(null, "http.retry.retry-after-time-unit", true);

        String retryHeader = configuration.get(retryHeaderProp);
        ChronoUnit baseDelay = configuration.get(retryTimeUnitProp, timeUnitStr -> ChronoUnit.valueOf(timeUnitStr));

        ConfigurationProperty retryStrategyProp = new ConfigurationProperty(null, "http.retry.strategy", false);
        String strategyString = configuration.get(retryStrategyProp);

        RetryStrategy retryStrategy;
        if ("fixed".equals(strategyString)) {
            retryStrategy = configureFixedRetryStrategy(configuration);
        } else {
            retryStrategy = configureExponentialRetryStrategy(configuration);
        }

        return  new RetryPolicy(retryStrategy, retryHeader, baseDelay);
    }

    private static RetryStrategy configureFixedRetryStrategy(Configuration configuration) throws Exception {
        ConfigurationProperty fixedMaxRetriesProp = new ConfigurationProperty(null, "http.retry.strategy.fixed.max-retries", false);
        ConfigurationProperty fixedDelayProp = new ConfigurationProperty(null, "http.retry.strategy.fixed.delay", false);

        Integer maxRetries = configuration.get(fixedMaxRetriesProp, (Integer)null);
        Duration maxDelay = configuration.get(fixedDelayProp, maxDelayStr -> Duration.parse(maxDelayStr)); // parse exceptions TODO
        if (maxRetries != null && maxDelay != null) {
            return new FixedDelay(maxRetries, maxDelay);
        }

        // log
        throw new Exception("bad configuration");
    }

    private static RetryStrategy configureExponentialRetryStrategy(Configuration configuration) throws Exception {
        ConfigurationProperty exponentialMaxRetriesProp = new ConfigurationProperty(null, "http.retry.strategy.exponential.max-retries", false);
        ConfigurationProperty exponentialBaseDelayProp = new ConfigurationProperty(null, "http.retry.strategy.exponential.base-delay", false);
        ConfigurationProperty exponentialMaxDelayProp = new ConfigurationProperty(null, "http.retry.strategy.exponential.max-delay", true);

        // pass it over to ExponentialBackoff to read
        Integer maxRetries = configuration.get(exponentialMaxRetriesProp, -1);
        Duration baseDelay = configuration.get(exponentialBaseDelayProp, baseDelayStr -> Duration.parse(baseDelayStr));
        Duration maxDelay = configuration.get(exponentialMaxDelayProp, maxDelayStr -> Duration.parse(maxDelayStr));

        if (maxRetries > 0 && maxDelay != null && baseDelay != null) {
            return new ExponentialBackoff(maxRetries, baseDelay, maxDelay);
        } else if (maxRetries < 0 && maxDelay == null && baseDelay == null) {
            return new ExponentialBackoff();
        } else {
            // support all possible defaults ?
            // log error?
            return new ExponentialBackoff();
        }
    }
}

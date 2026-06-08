// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.http.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.ConfigurationSource;
import com.azure.spring.cloud.appconfiguration.config.implementation.RequestTracingConstants;

public class TracingInfoTest {

    @Test
    public void getValueTest() {
        Configuration configuration = getConfiguration("false");

        TracingInfo tracingInfo = new TracingInfo(false, 0, configuration);
        assertEquals("RequestType=Startup", tracingInfo.getValue(false, false, null));
        assertEquals("RequestType=Watch", tracingInfo.getValue(true, false, null));

        tracingInfo = new TracingInfo(true, 0, configuration);
        assertEquals("RequestType=Startup,UsesKeyVault", tracingInfo.getValue(false, false, null));

        tracingInfo = new TracingInfo(false, 1, configuration);
        assertEquals("RequestType=Startup,ReplicaCount=1", tracingInfo.getValue(false, false, null));

        tracingInfo = new TracingInfo(true, 0, configuration);

        FeatureFlagTracing ffTracing = new FeatureFlagTracing();

        ffTracing.updateFeatureFilterTelemetry("Random");
        assertEquals("RequestType=Startup,Filter=CSTM,UsesKeyVault", tracingInfo.getValue(false, false, ffTracing));

        assertEquals("RequestType=Startup,Filter=CSTM,UsesKeyVault,PushRefresh", tracingInfo.getValue(false, true, null));

    }

    @Test
    public void disableTracingTest() {
        TracingInfo tracingInfo = new TracingInfo(false, 0, getConfiguration(null));
        assertNotEquals("", tracingInfo.getValue(false, false, null));

        tracingInfo = new TracingInfo(false, 0, getConfiguration(""));
        assertNotEquals("", tracingInfo.getValue(false, false, null));

        tracingInfo = new TracingInfo(false, 0, getConfiguration("true"));
        assertEquals("", tracingInfo.getValue(false, false, null));

        tracingInfo = new TracingInfo(false, 0, getConfiguration("false"));
        assertNotEquals("", tracingInfo.getValue(false, false, null));

        tracingInfo = new TracingInfo(false, 0, getConfiguration("random string"));
        assertNotEquals("", tracingInfo.getValue(false, false, null));
    }

    @Test
    public void loadBalancingTracingTest() {
        Configuration configuration = getConfiguration("false");
        TracingInfo tracingInfo = new TracingInfo(false, 0, configuration);
        tracingInfo.setUsesLoadBalancing();
        String value = tracingInfo.getValue(false, false, null);
        assertTrue(value.contains("Features=LB"));
    }

    @Test
    public void aiConfigurationTracingTest() {
        Configuration configuration = getConfiguration("false");
        TracingInfo tracingInfo = new TracingInfo(false, 0, configuration);
        tracingInfo.updateAiConfigurationTracing("application/json; profile=\"https://azconfig.io/mime-profiles/ai\"");
        String value = tracingInfo.getValue(false, false, null);
        assertTrue(value.contains("Features=AI"));
    }

    @Test
    public void aiChatCompletionTracingTest() {
        Configuration configuration = getConfiguration("false");
        TracingInfo tracingInfo = new TracingInfo(false, 0, configuration);
        tracingInfo.updateAiConfigurationTracing(
            "application/json; profile=\"https://azconfig.io/mime-profiles/ai-chat-completion\"");
        String value = tracingInfo.getValue(false, false, null);
        assertTrue(value.contains("AI+AICC"));
    }

    @Test
    public void aiConfigurationTracingNullContentTypeTest() {
        Configuration configuration = getConfiguration("false");
        TracingInfo tracingInfo = new TracingInfo(false, 0, configuration);
        tracingInfo.updateAiConfigurationTracing(null);
        String value = tracingInfo.getValue(false, false, null);
        assertEquals("RequestType=Startup", value);
    }

    @Test
    public void aiConfigurationTracingResetTest() {
        Configuration configuration = getConfiguration("false");
        TracingInfo tracingInfo = new TracingInfo(false, 0, configuration);
        tracingInfo.updateAiConfigurationTracing("application/json; profile=\"https://azconfig.io/mime-profiles/ai\"");
        tracingInfo.resetAiConfigurationTracing();
        String value = tracingInfo.getValue(false, false, null);
        assertEquals("RequestType=Startup", value);
    }

    @Test
    public void aiConfigurationTracingNonJsonContentTypeTest() {
        Configuration configuration = getConfiguration("false");
        TracingInfo tracingInfo = new TracingInfo(false, 0, configuration);
        tracingInfo.updateAiConfigurationTracing("text/plain; profile=\"https://azconfig.io/mime-profiles/ai\"");
        String value = tracingInfo.getValue(false, false, null);
        assertEquals("RequestType=Startup", value);
    }

    @Test
    public void aiConfigurationTracingNoProfileParameterTest() {
        Configuration configuration = getConfiguration("false");
        TracingInfo tracingInfo = new TracingInfo(false, 0, configuration);
        tracingInfo.updateAiConfigurationTracing("application/json; charset=utf-8");
        String value = tracingInfo.getValue(false, false, null);
        assertEquals("RequestType=Startup", value);
    }

    @Test
    public void aiConfigurationTracingFeatureFlagContentTypeTest() {
        Configuration configuration = getConfiguration("false");
        TracingInfo tracingInfo = new TracingInfo(false, 0, configuration);
        tracingInfo.updateAiConfigurationTracing(
            "application/vnd.microsoft.appconfig.ff+json;charset=utf-8");
        String value = tracingInfo.getValue(false, false, null);
        assertEquals("RequestType=Startup", value);
    }

    @Test
    public void multipleFeaturesTracingTest() {
        Configuration configuration = getConfiguration("false");
        TracingInfo tracingInfo = new TracingInfo(false, 0, configuration);
        tracingInfo.setUsesLoadBalancing();
        tracingInfo.updateAiConfigurationTracing("application/json; profile=\"https://azconfig.io/mime-profiles/ai\"");
        String value = tracingInfo.getValue(false, false, null);
        assertTrue(value.contains("Features=LB+AI"));
    }

    @Test
    public void failoverTracingTest() {
        Configuration configuration = getConfiguration("false");
        TracingInfo tracingInfo = new TracingInfo(false, 0, configuration);
        tracingInfo.setFailoverRequest();
        String value = tracingInfo.getValue(false, false, null);
        assertTrue(value.contains("Failover"));
    }

    @Test
    public void maxVariantsTracingTest() {
        Configuration configuration = getConfiguration("false");
        TracingInfo tracingInfo = new TracingInfo(false, 0, configuration);
        FeatureFlagTracing ffTracing = new FeatureFlagTracing();
        ffTracing.updateMaxVariants(5);
        String value = tracingInfo.getValue(false, false, ffTracing);
        assertTrue(value.contains("MaxVariants=5"));
    }

    @Test
    public void ffFeaturesTracingTest() {
        Configuration configuration = getConfiguration("false");
        TracingInfo tracingInfo = new TracingInfo(false, 0, configuration);
        FeatureFlagTracing ffTracing = new FeatureFlagTracing();
        ffTracing.setUsesTelemetry();
        ffTracing.setUsesSeed();
        String value = tracingInfo.getValue(false, false, ffTracing);
        assertTrue(value.contains("FFFeatures=Seed+Telemetry"));
    }

    @Test
    public void fullCorrelationContextTest() {
        Configuration configuration = getConfiguration("false");
        TracingInfo tracingInfo = new TracingInfo(true, 2, configuration);
        tracingInfo.setUsesLoadBalancing();
        tracingInfo.setFailoverRequest();

        FeatureFlagTracing ffTracing = new FeatureFlagTracing();
        ffTracing.updateFeatureFilterTelemetry("Targeting");
        ffTracing.setUsesTelemetry();
        ffTracing.updateMaxVariants(3);

        String value = tracingInfo.getValue(false, true, ffTracing);

        // Verify ordering: key-value pairs first, then tags
        assertTrue(value.startsWith("RequestType=Startup"));
        assertTrue(value.contains("ReplicaCount=2"));
        assertTrue(value.contains("Filter=TRGT"));
        assertTrue(value.contains("MaxVariants=3"));
        assertTrue(value.contains("FFFeatures=Telemetry"));
        assertTrue(value.contains("Features=LB"));
        assertTrue(value.contains("UsesKeyVault"));
        assertTrue(value.contains("PushRefresh"));
        assertTrue(value.contains("Failover"));
    }

    private static final ConfigurationSource EMPTY_SOURCE = new ConfigurationSource() {
        @Override
        public Map<String, String> getProperties(String source) {
            return Collections.emptyMap();
        }
    };

    private Configuration getConfiguration(String value) {
        return new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE, new TestConfigurationSource()
            .put(RequestTracingConstants.REQUEST_TRACING_DISABLED_ENVIRONMENT_VARIABLE.toString(), value)).build();
    }

    private final class TestConfigurationSource implements ConfigurationSource {
        private final Map<String, String> testData;

        /**
         * Creates TestConfigurationSource with given property names and values.
         */
        TestConfigurationSource() {
            this.testData = new HashMap<>();
        }

        /**
         * Adds property name and value to the source.
         *
         * @param name property name
         * @param value property value
         * @return this {@code TestConfigurationSource} for chaining.
         */
        public TestConfigurationSource put(String name, String value) {
            this.testData.put(name, value);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Map<String, String> getProperties(String path) {
            if (path == null) {
                return testData;
            }
            return testData.entrySet().stream()
                .filter(prop -> prop.getKey().startsWith(path + "."))
                .collect(Collectors.toMap(Map.Entry<String, String>::getKey, Map.Entry<String, String>::getValue));
        }
    }

}

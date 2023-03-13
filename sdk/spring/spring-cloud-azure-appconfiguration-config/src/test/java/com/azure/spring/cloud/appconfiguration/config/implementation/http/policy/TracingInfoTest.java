// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.http.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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

        TracingInfo tracingInfo = new TracingInfo(false, false, 0, configuration);
        assertEquals("RequestType=Startup", tracingInfo.getValue(false));
        assertEquals("RequestType=Watch", tracingInfo.getValue(true));

        tracingInfo = new TracingInfo(true, false, 0, configuration);
        assertEquals("RequestType=Startup,Env=Dev", tracingInfo.getValue(false));

        tracingInfo = new TracingInfo(false, true, 0, configuration);
        assertEquals("RequestType=Startup,UsesKeyVault", tracingInfo.getValue(false));

        tracingInfo = new TracingInfo(false, false, 1, configuration);
        assertEquals("RequestType=Startup,ReplicaCount=1", tracingInfo.getValue(false));

        tracingInfo = new TracingInfo(false, false, 0, configuration);

        tracingInfo.getFeatureFlagTracing().updateFeatureFilterTelemetry("Random");
        assertEquals("RequestType=Startup,Filter=CSTM", tracingInfo.getValue(false));
    }

    @Test
    public void disableTracingTest() {
        TracingInfo tracingInfo = new TracingInfo(false, false, 0, getConfiguration(null));
        assertNotEquals("", tracingInfo.getValue(false));
        
        tracingInfo = new TracingInfo(false, false, 0, getConfiguration(""));
        assertNotEquals("", tracingInfo.getValue(false));
        
        tracingInfo = new TracingInfo(false, false, 0, getConfiguration("true"));
        assertEquals("", tracingInfo.getValue(false));
        
        tracingInfo = new TracingInfo(false, false, 0, getConfiguration("false"));
        assertNotEquals("", tracingInfo.getValue(false));
        
        tracingInfo = new TracingInfo(false, false, 0, getConfiguration("random string"));
        assertNotEquals("", tracingInfo.getValue(false));
    }

    private static final ConfigurationSource EMPTY_SOURCE = new ConfigurationSource() {
        @Override
        public Map<String, String> getProperties(String source) {
            return Collections.emptyMap();
        }
    };

    private Configuration getConfiguration(String value) {
        return new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE, new TestConfigurationSource().put(RequestTracingConstants.REQUEST_TRACING_DISABLED_ENVIRONMENT_VARIABLE.toString(), value)).build();
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

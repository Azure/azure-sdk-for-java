// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.ConfigurationSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TestConfigurationBuilder {
    private TestConfigurationSource source;

    public TestConfigurationBuilder(String... testProps) {
        this.source = new TestConfigurationSource(testProps);
    }

    public TestConfigurationBuilder add(String key, String value) {
        source.add(key, value);
        return this;
    }

    public Configuration build() {
        return new ConfigurationBuilder(source).build();
    }

    public Configuration buildSection(String section) {
        return new ConfigurationBuilder(source).buildSection(section);
    }

    private static class TestConfigurationSource implements ConfigurationSource {
        private Map<String, String> testData;

        public TestConfigurationSource(String... testData) {
            this.testData = new HashMap<>();

            if (testData == null) {
                return;
            }

            for (int i = 0; i < testData.length; i += 2) {
                this.testData.put(testData[i], testData[i + 1]);
            }
        }

        void add(String key, String value) {
            this.testData.put(key, value);
        }

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

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.utils;

import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.ConfigurationSource;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Allows building test configuration including explicit and environment sources.
 */
public class TestConfigurationBuilder {
    private final TestConfigurationSource source;
    private final TestConfigurationSource envConfig;

    /**
     * Created {@link TestConfigurationBuilder}
     */
    public TestConfigurationBuilder() {
        this.source = new TestConfigurationSource();
        this.envConfig = new TestConfigurationSource();
    }

    /**
     * Adds configuration property.
     *
     * @param key Property name.
     * @param value Property value.
     * @return the updated TestConfigurationBuilder object.
     */
    public TestConfigurationBuilder add(String key, String value) {
        source.add(key, value);
        return this;
    }

    /**
     * Adds configuration property to environment.
     *
     * @param key Property name.
     * @param value Property value.
     * @return the updated TestConfigurationBuilder object.
     */
    public TestConfigurationBuilder addEnv(String key, String value) {
        envConfig.add(key, value);
        return this;
    }

    /**
     * Builds shared {@link Configuration} section.
     *
     * @return {@link Configuration} object.
     */
    public Configuration build() {
        return getBuilder().build();
    }

    /**
     * Builds {@link Configuration} section.
     *
     * @param section relative section path.
     * @return {@link Configuration} object.
     */
    public Configuration buildSection(String section) {
        return getBuilder().buildSection(section);
    }

    private ConfigurationBuilder getBuilder() {
        try {
            Constructor<?> ctor = ConfigurationBuilder.class.getDeclaredConstructor(ConfigurationSource.class, ConfigurationSource.class);
            ctor.setAccessible(true);
            return (ConfigurationBuilder) ctor.newInstance(source, envConfig);
        } catch (Throwable t) {
            fail(t);
        }

        throw new IllegalStateException("Failed to build Configuration");
    }

    private static final class TestConfigurationSource implements ConfigurationSource {
        private final Map<String, String> testData;

        TestConfigurationSource(String... testData) {
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

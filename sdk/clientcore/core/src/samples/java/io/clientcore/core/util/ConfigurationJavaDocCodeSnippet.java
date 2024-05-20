// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util;

import io.clientcore.core.util.configuration.Configuration;
import io.clientcore.core.util.configuration.ConfigurationBuilder;
import io.clientcore.core.util.configuration.ConfigurationProperty;
import io.clientcore.core.util.configuration.ConfigurationPropertyBuilder;
import io.clientcore.core.util.configuration.ConfigurationSource;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Codesnippets for {@link Configuration}
 */
public class ConfigurationJavaDocCodeSnippet {
    public static final class SampleSource implements ConfigurationSource {
        private final Map<String, String> configurations;

        public SampleSource(Map<String, String> configurations) {
            this.configurations = Collections.unmodifiableMap(configurations);
        }

        @Override
        public Map<String, String> getProperties(String path) {
            if (path == null) {
                return configurations;
            }

            return configurations.entrySet().stream()
                .filter(prop -> prop.getKey().startsWith(path + "."))
                .collect(Collectors.toMap(Map.Entry<String, String>::getKey, Map.Entry<String, String>::getValue));
        }
    }

    /**
     * Codesnippets for {@link ConfigurationBuilder}.
     */
    public void configurationBuilderUsage() {
        Map<String, String> properties = new HashMap<>();
        properties.put("azure.sdk.client-name.http.proxy.port", "8080");
        properties.put("azure.sdk.http.proxy.hostname", "<host");
        properties.put("azure.sdk.http.proxy.username", "user");
        properties.put("azure.sdk.http.proxy.password", "pwd");

        // BEGIN: io.clientcore.core.util.Configuration
        Configuration configuration = new ConfigurationBuilder(new SampleSource(properties))
            .root("azure.sdk")
            .buildSection("client-name");

        ConfigurationProperty<String> proxyHostnameProperty = ConfigurationPropertyBuilder.ofString("http.proxy.hostname")
            .shared(true)
            .build();
        System.out.println(configuration.get(proxyHostnameProperty));
        // END: io.clientcore.core.util.Configuration

        // BEGIN: io.clientcore.core.util.ConfigurationBuilder#putProperty
        configuration = new ConfigurationBuilder()
            .putProperty("azure.sdk.client-name.connection-string", "...")
            .root("azure.sdk")
            .buildSection("client-name");

        ConfigurationProperty<String> connectionStringProperty = ConfigurationPropertyBuilder.ofString("connection-string")
            .build();

        System.out.println(configuration.get(connectionStringProperty));
        // END: io.clientcore.core.util.ConfigurationBuilder#putProperty

        // BEGIN: io.clientcore.core.util.ConfigurationBuilder#buildSection
        // Builds Configuration for <client-name> with fallback to shared properties.
        configuration = new ConfigurationBuilder(new SampleSource(properties))
            .root("azure.sdk")
            .buildSection("client-name");
        // END: io.clientcore.core.util.ConfigurationBuilder#buildSection

        // BEGIN: io.clientcore.core.util.ConfigurationBuilder#build
        // Builds shared Configuration only.
        Configuration sharedConfiguration = new ConfigurationBuilder(new SampleSource(properties))
            .root("azure.sdk")
            .build();
        // END: io.clientcore.core.util.ConfigurationBuilder#build
    }

    /**
     * Codesnippets for {@link Configuration}.
     */
    public void configurationUsage() {
        Map<String, String> properties = new HashMap<>();
        properties.put("azure.sdk.<client-name>.http.proxy.port", "8080");
        properties.put("azure.sdk.http.proxy.hostname", "<host");
        properties.put("azure.sdk.http.proxy.username", "user");
        properties.put("azure.sdk.http.proxy.password", "pwd");

        Configuration configuration = new ConfigurationBuilder(new SampleSource(properties))
            .root("azure.sdk")
            .buildSection("<client-name>");

        // BEGIN: io.clientcore.core.util.Configuration.get#ConfigurationProperty
        ConfigurationProperty<String> property = ConfigurationPropertyBuilder.ofString("http.proxy.hostname")
            .shared(true)
            .logValue(true)
            .systemPropertyName("http.proxyHost")
            .build();

        // attempts to get local `azure.sdk.<client-name>.http.proxy.host` property and falls back to
        // shared azure.sdk.http.proxy.port
        System.out.println(configuration.get(property));
        // END: io.clientcore.core.util.Configuration.get#ConfigurationProperty
    }

    /**
     * Codesnippets for {@link ConfigurationProperty}.
     */
    public void configurationPropertyUsage() {
        Map<String, String> properties = new HashMap<>();
        properties.put("azure.sdk.sample.timeout", "1000");
        properties.put("azure.sdk.sample.retry-count", "3");
        properties.put("azure.sdk.sample.is-enabled", "true");
        properties.put("azure.sdk.sample.mode", "mode1");

        Configuration configuration = new ConfigurationBuilder(new SampleSource(properties))
            .root("azure.sdk")
            .buildSection("sample");

        // BEGIN: io.clientcore.core.util.ConfigurationPropertyBuilder.ofDuration
        ConfigurationProperty<Duration> timeoutProperty = ConfigurationPropertyBuilder.ofDuration("timeout")
            .build();
        System.out.println(configuration.get(timeoutProperty));
        // END: io.clientcore.core.util.ConfigurationPropertyBuilder.ofDuration

        // BEGIN: io.clientcore.core.util.ConfigurationPropertyBuilder
        ConfigurationProperty<SampleEnumProperty> modeProperty =
            new ConfigurationPropertyBuilder<>("mode", SampleEnumProperty::fromString)
                .logValue(true)
                .defaultValue(SampleEnumProperty.MODE_1)
                .build();
        System.out.println(configuration.get(modeProperty));
        // END: io.clientcore.core.util.ConfigurationPropertyBuilder

        // BEGIN: io.clientcore.core.util.ConfigurationPropertyBuilder.ofBoolean
        ConfigurationProperty<Boolean> booleanProperty = ConfigurationPropertyBuilder.ofBoolean("is-enabled")
            .build();
        System.out.println(configuration.get(booleanProperty));
        // END: io.clientcore.core.util.ConfigurationPropertyBuilder.ofBoolean

        // BEGIN: io.clientcore.core.util.ConfigurationPropertyBuilder.ofInteger
        ConfigurationProperty<Integer> integerProperty = ConfigurationPropertyBuilder.ofInteger("retry-count")
            .build();
        System.out.println(configuration.get(integerProperty));
        // END: io.clientcore.core.util.ConfigurationPropertyBuilder.ofInteger
    }

    public static final class SampleEnumProperty implements ExpandableEnum<String> {
        private static final Map<String, SampleEnumProperty> VALUES = new ConcurrentHashMap<>();

        private final String value;

        private SampleEnumProperty(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        public static final SampleEnumProperty MODE_1 = fromString("mode1");
        public static final SampleEnumProperty MODE_2 = fromString("mode2");

        public static SampleEnumProperty fromString(String str) {
            if (str == null) {
                return null;
            }

            SampleEnumProperty sampleEnumProperty = VALUES.get(str);

            if (sampleEnumProperty != null) {
                return sampleEnumProperty;
            }

            return VALUES.computeIfAbsent(str, SampleEnumProperty::new);
        }

        @Override
        public int hashCode() {
            return Objects.hash(SampleEnumProperty.class, value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof SampleEnumProperty)) {
                return false;
            }

            SampleEnumProperty other = (SampleEnumProperty) obj;
            return Objects.equals(value, other.value);
        }

        @Override
        public String toString() {
            return value;
        }
    }
}

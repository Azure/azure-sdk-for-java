// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.implementation.util.EnvironmentConfiguration;
import com.azure.core.util.logging.ClientLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Builds {@link Configuration} with external source.
 */
public class ConfigurationBuilder {
    private static final Map<String, String> EMPTY_MAP = new HashMap<>();
    private static final ClientLogger LOGGER = new ClientLogger(ConfigurationBuilder.class);

    private final ConfigurationSource source;
    private final EnvironmentConfiguration environmentConfiguration;
    private String rootPath;
    private Configuration sharedConfiguration;

    /**
     * Creates {@code ConfigurationBuilder}.
     *
     * <!-- src_embed com.azure.core.util.ConfigurationBuilder -->
     * <pre>
     * &#47;&#47; Creates ConfigurationBuilder with configured root path to shared properties
     * ConfigurationBuilder configurationBuilder = new ConfigurationBuilder&#40;new SampleSource&#40;properties&#41;&#41;
     *     .root&#40;&quot;azure.sdk&quot;&#41;; &#47;&#47; shared properties' absolute path
     * </pre>
     * <!-- end com.com.azure.core.util.ConfigurationBuilder -->
     * @param source Custom {@link ConfigurationSource} containing known Azure SDK configuration properties
     */
    public ConfigurationBuilder(ConfigurationSource source) {
        this(source, EnvironmentConfiguration.getGlobalConfiguration());
    }

    /**
     * Creates {@code ConfigurationBuilder} with custom environment configuration. Used to test configuration.
     *
     * @param source {@link ConfigurationSource} instance containing known Azure SDK configuration properties
     * @param environmentConfiguration Instance of {@link EnvironmentConfiguration}.
     */
    ConfigurationBuilder(ConfigurationSource source, EnvironmentConfiguration environmentConfiguration) {
        this.source = Objects.requireNonNull(source, "'source' cannot be null");
        this.environmentConfiguration = environmentConfiguration;
    }

    /**
     * Sets path to root configuration properties where shared Azure SDK properties are defined.
     * When local per-client property is missing, {@link Configuration} falls back to shared properties.
     *
     * <!-- src_embed com.azure.core.util.ConfigurationBuilder -->
     * <pre>
     * &#47;&#47; Creates ConfigurationBuilder with configured root path to shared properties
     * ConfigurationBuilder configurationBuilder = new ConfigurationBuilder&#40;new SampleSource&#40;properties&#41;&#41;
     *     .root&#40;&quot;azure.sdk&quot;&#41;; &#47;&#47; shared properties' absolute path
     * </pre>
     * <!-- end com.com.azure.core.util.ConfigurationBuilder -->
     *
     * @param rootPath absolute root path, can be {@code null}.
     * @return {@code ConfigurationBuilder} instance for chaining.
     */
    public ConfigurationBuilder root(String rootPath) {
        this.rootPath = rootPath;
        this.sharedConfiguration = null;
        return this;
    }

    /**
     * Builds root {@link Configuration} section. Use it for shared properties only. To read client-specific configuration,
     * use {@link ConfigurationBuilder#buildSection(String)} which can read per-client and shared properties.
     *
     * <!-- src_embed com.azure.core.util.ConfigurationBuilder#build -->
     * <pre>
     * &#47;&#47; Builds shared Configuration only.
     * Configuration sharedConfiguration = new ConfigurationBuilder&#40;new SampleSource&#40;properties&#41;&#41;
     *     .root&#40;&quot;azure.sdk&quot;&#41;
     *     .build&#40;&#41;;
     * </pre>
     * <!-- end com.com.azure.core.util.ConfigurationBuilder#build -->
     *
     * @return Root {@link Configuration} with shared properties.
     */
    public Configuration build() {
        if (sharedConfiguration == null) {
            // defaults can be reused to get different client sections.
            sharedConfiguration = new Configuration(readConfigurations(this.source, rootPath), environmentConfiguration, rootPath, null);
        }

        return sharedConfiguration;
    }

    /**
     * Builds {@link Configuration} section that supports retrieving properties from client-specific section with fallback to root section for
     * properties that can be shared between clients.
     *
     * <!-- src_embed com.azure.core.util.ConfigurationBuilder#buildSection -->
     * <pre>
     * &#47;&#47; Builds Configuration for &lt;client-name&gt; with fallback to shared properties.
     * Configuration configuration = new ConfigurationBuilder&#40;new SampleSource&#40;properties&#41;&#41;
     *     .root&#40;&quot;azure.sdk&quot;&#41;
     *     .buildSection&#40;&quot;client-name&quot;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.ConfigurationBuilder#buildSection -->
     *
     * @param path relative path from {@link ConfigurationBuilder#root(String)} to client section.
     * @return Client {@link Configuration} capable of reading client-specific and shared properties.
     */
    public Configuration buildSection(String path) {
        Objects.requireNonNull(path, "'path' cannot be null");
        if (sharedConfiguration == null) {
            // sharedConfiguration can be reused to build different client sections.
            sharedConfiguration = new Configuration(readConfigurations(this.source, rootPath), environmentConfiguration, rootPath, null);
        }

        String absolutePath = getAbsolutePath(rootPath, path);
        return new Configuration(readConfigurations(this.source, absolutePath), environmentConfiguration, absolutePath, sharedConfiguration);
    }

    private Map<String, String> readConfigurations(ConfigurationSource source, String path) {
        Map<String, String> configs = source.getProperties(path);

        if (configs == null || configs.isEmpty()) {
            return EMPTY_MAP;
        }

        Map<String, String> props = new HashMap<>();

        for (Map.Entry<String, String> prop : configs.entrySet()) {
            String key = CoreUtils.isNullOrEmpty(path) ? prop.getKey() : prop.getKey().substring(path.length() + 1);
            String value = prop.getValue();

            LOGGER.atVerbose()
                .addKeyValue("name", prop.getKey())
                .addKeyValue("value", value)
                .log("Got property from configuration source.");

            if (!CoreUtils.isNullOrEmpty(value)) {
                props.put(key, value);
            } else {
                LOGGER.atWarning()
                    .addKeyValue("name", prop.getKey())
                    .log("Property value is null");
            }
        }

        return props;
    }

    private static String getAbsolutePath(String root, String relative) {
        if (CoreUtils.isNullOrEmpty(root)) {
            return relative;
        }

        return root + "." + relative;
    }
}

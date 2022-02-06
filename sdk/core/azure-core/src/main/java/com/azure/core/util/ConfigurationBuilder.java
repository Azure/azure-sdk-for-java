package com.azure.core.util;

import com.azure.core.implementation.util.EnvironmentConfiguration;
import com.azure.core.util.logging.ClientLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ConfigurationBuilder {

    private final static Map<String, String> EMPTY_MAP = new HashMap<>();

    private final ConfigurationSource source;
    private final EnvironmentConfiguration environmentConfiguration;
    private final ClientLogger logger;
    private String rootPath;
    private Configuration defaults;

    public ConfigurationBuilder(ConfigurationSource source) {
        this.source = Objects.requireNonNull(source, "'source' cannot be null");
        this.environmentConfiguration = EnvironmentConfiguration.getGlobalConfiguration();
        this.logger = new ClientLogger(ConfigurationBuilder.class);
    }

    // for tests
    ConfigurationBuilder(ConfigurationSource source, ConfigurationSource environmentSource) {
        Objects.requireNonNull(environmentSource, "'environmentSource' cannot be null");
        this.source = Objects.requireNonNull(source, "'source' cannot be null");
        this.environmentConfiguration = new EnvironmentConfiguration(environmentSource);
        this.logger = new ClientLogger(ConfigurationBuilder.class);
    }

    public ConfigurationBuilder root(String rootPath) {
        this.rootPath = Objects.requireNonNull(rootPath, "'rootPath' cannot be null");
        this.defaults = null;
        return this;
    }

    public Configuration build() {
        if (defaults == null) {
            // defaults can be reused to get different client sections.
            defaults = new Configuration(readConfigurations(this.source, rootPath), environmentConfiguration, rootPath, null);
        }
        return defaults;
    }

    public Configuration buildSection(String path) {
        Objects.requireNonNull(path, "'path' cannot be null");
        if (defaults == null) {
            // defaults can be reused to get different client sections.
            defaults = new Configuration(readConfigurations(this.source, rootPath), environmentConfiguration, rootPath, null);
        }

        if (CoreUtils.isNullOrEmpty(path)) {
            logger.warning("Client section is not set, falling back to root section");
            return defaults;
        }

        String absolutePath = getAbsolutePath(rootPath, path);
        return new Configuration(readConfigurations(this.source, absolutePath), environmentConfiguration, absolutePath, defaults);
    }

    private Map<String, String> readConfigurations(ConfigurationSource source, String path) {
        Map<String, String> configs = null;
        Set<String> children = source.getChildKeys(path);

        if (children == null || children.isEmpty()) {
            return EMPTY_MAP;
        }

        configs = new HashMap<>();
        for (String child : children) {
            String key = CoreUtils.isNullOrEmpty(path) ? child : child.substring(path.length() + 1);
            String value = source.getValue(child);

            logger.atVerbose()
                .addKeyValue("path", path)
                .addKeyValue("key", key)
                .addKeyValue("value", value)
                .log("Got property from configuration source.");

            if (value == null) {
                logger.atWarning()
                    .addKeyValue("path", path)
                    .addKeyValue("key", key)
                    .log("Property value is null");
            }

            configs.put(key, value);
        }

        return configs;
    }

    private static String getAbsolutePath(String root, String relative) {
        if (CoreUtils.isNullOrEmpty(relative)) {
            return root;
        }

        if (CoreUtils.isNullOrEmpty(root)) {
            return relative;
        }

        return root + "." + relative;
    }

}

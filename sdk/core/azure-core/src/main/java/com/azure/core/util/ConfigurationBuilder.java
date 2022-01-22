package com.azure.core.util;

import com.azure.core.implementation.util.EnvironmentConfiguration;
import com.azure.core.implementation.util.EnvironmentConfigurationSource;
import com.azure.core.util.logging.ClientLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConfigurationBuilder {

    private final static Map<String, String> EMPTY_MAP = new HashMap<>();
    private final static EnvironmentConfigurationSource ENVIRONMENT_SOURCE = new EnvironmentConfigurationSource();

    private final ConfigurationSource source;
    private final EnvironmentConfiguration environmentConfiguration;
    private final ClientLogger logger;
    private String rootPath;
    private Configuration defaults;
    private String clientPath;

    public ConfigurationBuilder(ConfigurationSource source) {
        this.source = source;
        this.environmentConfiguration = EnvironmentConfiguration.getGlobalConfiguration();
        this.logger = new ClientLogger(ConfigurationBuilder.class);
    }

    // for tests
    ConfigurationBuilder(ConfigurationSource source, ConfigurationSource environmentSource) {
        this.source = source;
        this.environmentConfiguration = new EnvironmentConfiguration(environmentSource);
        this.logger = new ClientLogger(ConfigurationBuilder.class);
    }

    public ConfigurationBuilder root(String rootPath) {
        this.rootPath = rootPath;
        return this;
    }

    public ConfigurationBuilder defaultsSection(String defaultsPath) {
        String absoluteDefaultsPath = getAbsolutePath(rootPath, defaultsPath);
        defaults = new Configuration(absoluteDefaultsPath, readConfigurations(this.source, absoluteDefaultsPath), null, environmentConfiguration);
        return this;
    }

    public ConfigurationBuilder clientSection(String clientPath) {
        this.clientPath = clientPath;
        return this;
    }

    private String getAbsolutePath(String root, String relative) {
        if (relative == null) {
            return root;
        }

        if (root == null) {
            return relative;
        }

        return root + "." + relative;
    }

    public Configuration build() {
        if (defaults == null) {
            defaults = new Configuration(rootPath, readConfigurations(this.source, rootPath), null, environmentConfiguration);
        }

        if (clientPath == null) {
            return defaults;
        }

        String absoluteClientPath = getAbsolutePath(rootPath, clientPath);
        return new Configuration(absoluteClientPath, readConfigurations(this.source, absoluteClientPath), defaults, environmentConfiguration);
    }

    private static Map<String, String> readConfigurations(ConfigurationSource source, String path) {
        Map<String, String> configs = null;
        Set<String> children = source.getChildKeys(path);

        if (children == null || children.isEmpty()) {
            return EMPTY_MAP;
        }

        configs = new HashMap<>();
        for (String child : children) {
            // todo log if contains
            configs.putIfAbsent(path == null ? child : child.substring(path.length() + 1), source.getValue(child));
        }

        return configs;
    }
}

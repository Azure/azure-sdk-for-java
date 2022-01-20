package com.azure.core.util;

import com.azure.core.util.logging.ClientLogger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConfigurationProvider {

    private final static ConcurrentMap<String, String> EMPTY_MAP = new ConcurrentHashMap<>();

    private final ConfigurationSource source;
    private final ClientLogger logger;
    private final Configuration defaults;

    private static ConcurrentMap<String, String> readConfigurations(ConfigurationSource source, String prefix) {
        ConcurrentMap<String, String> configs = null;
        Iterable<String> children = source.getValues(prefix);

        if (children == null) {
            return EMPTY_MAP;
        }

        configs = new ConcurrentHashMap<>();
        for (String child : children) {
            // todo log if contains
            configs.putIfAbsent(child, source.getValue(child));
        }

        return configs;
    }

    public ConfigurationProvider(ConfigurationSource source) {
        this.source = source;
        this.logger = new ClientLogger(Configuration.class);
        // defaults in root
        this.defaults = new Configuration(null, readConfigurations(this.source, null), null);
    }

    public ConfigurationProvider(ConfigurationSource source, String defaultsConfigurationPath) {
        this.source = source;
        this.logger = new ClientLogger(Configuration.class);
        this.defaults = new Configuration(defaultsConfigurationPath, readConfigurations(this.source, defaultsConfigurationPath), null);
    }

    public Configuration getClientSection(String clientSectionPrefix) {
        return new Configuration(clientSectionPrefix, readConfigurations(this.source, clientSectionPrefix), defaults);
    }
}

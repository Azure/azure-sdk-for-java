package com.azure.core.util;

import com.azure.core.util.logging.ClientLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationProvider {

    private final ConfigurationSource source;
    private final ClientLogger logger;

    private static Map<String, String> readConfigurations(ConfigurationSource source, String prefix) {
        Map<String, String> configs = null;
        Iterable<String> children = source.getValues(prefix);

        if (children == null) {
            return Collections.emptyMap();
        }

        configs = new HashMap<>();
        for (String child : children) {
            // todo log if contains
            configs.putIfAbsent(child, source.getValue(child));
        }

        return configs;
    }

    public ConfigurationProvider(ConfigurationSource source) {
        this.source = source;
        this.logger = new ClientLogger(Configuration.class);
    }

    ImmutableConfiguration getDefaultsSection(String sectionName) {
        return new ImmutableConfiguration(sectionName, readConfigurations(this.source, sectionName), null);
    }

    ImmutableConfiguration getClientSection(String sectionName, ImmutableConfiguration defaults) {
        return new ImmutableConfiguration(sectionName, readConfigurations(this.source, sectionName), defaults);
    }
}

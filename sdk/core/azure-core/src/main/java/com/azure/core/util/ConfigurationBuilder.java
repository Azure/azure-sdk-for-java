package com.azure.core.util;

import com.azure.core.util.logging.ClientLogger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConfigurationBuilder {

    private final static ConcurrentMap<String, String> EMPTY_MAP = new ConcurrentHashMap<>();

    private ConfigurationSource source;
    private final ClientLogger logger;
    private String rootPath;
    private String defaultsPath;
    private String clientPath;

    public ConfigurationBuilder() {
        this.logger = new ClientLogger(Configuration.class);
    }

    // Q: should we move it to builder ctor since it mandatory?
    public ConfigurationBuilder source(ConfigurationSource source) {
        this.source = source;
        return this;
    }

    public ConfigurationBuilder root(String rootPath) {
        this.rootPath = rootPath;
        return this;
    }

    public ConfigurationBuilder defaultsSection(String defaultsPath) {
        this.defaultsPath = defaultsPath;
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
        // we can cache defaults across builders by path for better perf
        String absoluteDefaultsPath = getAbsolutePath(rootPath, defaultsPath);
        Configuration defaults = new Configuration(absoluteDefaultsPath, readConfigurations(this.source, absoluteDefaultsPath), null);

        String absoluteClientPath = getAbsolutePath(rootPath, clientPath);
        return new Configuration(absoluteClientPath, readConfigurations(this.source, absoluteClientPath), defaults);
    }

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
}

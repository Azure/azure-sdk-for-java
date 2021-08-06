// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Contains configuration information that is used during construction of client libraries.
 */
public class Configuration implements Cloneable {

    // Default properties - these are what we read from the environment
    /**
     * URL of the proxy for HTTP connections.
     */
    public static final String PROPERTY_HTTP_PROXY = "HTTP_PROXY";

    /**
     * URL of the proxy for HTTPS connections.
     */
    public static final String PROPERTY_HTTPS_PROXY = "HTTPS_PROXY";

    /**
     * Endpoint to connect to when using Azure Active Directory managed service identity (MSI).
     */
    public static final String PROPERTY_IDENTITY_ENDPOINT = "IDENTITY_ENDPOINT";

    /**
     * Header when connecting to Azure Active Directory using managed service identity (MSI).
     */
    public static final String PROPERTY_IDENTITY_HEADER = "IDENTITY_HEADER";

    /**
     * A list of hosts or CIDR to not use proxy HTTP/HTTPS connections through.
     */
    public static final String PROPERTY_NO_PROXY = "NO_PROXY";

    /**
     * Endpoint to connect to when using Azure Active Directory managed service identity (MSI).
     */
    public static final String PROPERTY_MSI_ENDPOINT = "MSI_ENDPOINT";

    /**
     * Secret when connecting to Azure Active Directory using managed service identity (MSI).
     */
    public static final String PROPERTY_MSI_SECRET = "MSI_SECRET";

    /**
     * Subscription id to use when connecting to Azure resources.
     */
    public static final String PROPERTY_AZURE_SUBSCRIPTION_ID = "AZURE_SUBSCRIPTION_ID";

    /**
     * Username to use when performing username/password authentication with Azure.
     */
    public static final String PROPERTY_AZURE_USERNAME = "AZURE_USERNAME";

    /**
     * Username to use when performing username/password authentication with Azure.
     */
    public static final String PROPERTY_AZURE_PASSWORD = "AZURE_PASSWORD";

    /**
     * Client id to use when performing service principal authentication with Azure.
     */
    public static final String PROPERTY_AZURE_CLIENT_ID = "AZURE_CLIENT_ID";

    /**
     * Client secret to use when performing service principal authentication with Azure.
     */
    public static final String PROPERTY_AZURE_CLIENT_SECRET = "AZURE_CLIENT_SECRET";

    /**
     * Tenant id for the Azure resources.
     */
    public static final String PROPERTY_AZURE_TENANT_ID = "AZURE_TENANT_ID";

    /**
     * Path of a PEM certificate file to use when performing service principal authentication with Azure.
     */
    public static final String PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH = "AZURE_CLIENT_CERTIFICATE_PATH";

    /**
     * Flag to disable the CP1 client capabilities in Azure Identity Token credentials.
     */
    public static final String PROPERTY_AZURE_IDENTITY_DISABLE_CP1 = "AZURE_IDENTITY_DISABLE_CP1";

    /**
     * URL used by Bridge To Kubernetes to redirect IMDS calls in the development environment.
     */
    public static final String PROPERTY_AZURE_POD_IDENTITY_TOKEN_URL = "AZURE_POD_IDENTITY_TOKEN_URL";

    /*
     * Name of Azure AAD regional authority.
     */
    public static final String PROPERTY_AZURE_REGIONAL_AUTHORITY_NAME = "AZURE_REGIONAL_AUTHORITY_NAME";

    /**
     * Name of the Azure resource group.
     */
    public static final String PROPERTY_AZURE_RESOURCE_GROUP = "AZURE_RESOURCE_GROUP";

    /**
     * Name of the Azure cloud to connect to.
     */
    public static final String PROPERTY_AZURE_CLOUD = "AZURE_CLOUD";

    /**
     * The Azure Active Directory endpoint to connect to.
     */
    public static final String PROPERTY_AZURE_AUTHORITY_HOST = "AZURE_AUTHORITY_HOST";

    /**
     * Disables telemetry collection.
     */
    public static final String PROPERTY_AZURE_TELEMETRY_DISABLED = "AZURE_TELEMETRY_DISABLED";

    /**
     * Enables logging by setting a log level.
     */
    public static final String PROPERTY_AZURE_LOG_LEVEL = "AZURE_LOG_LEVEL";

    /**
     * Enables HTTP request/response logging by setting an HTTP log detail level.
     */
    public static final String PROPERTY_AZURE_HTTP_LOG_DETAIL_LEVEL = "AZURE_HTTP_LOG_DETAIL_LEVEL";

    /**
     * Disables tracing.
     */
    public static final String PROPERTY_AZURE_TRACING_DISABLED = "AZURE_TRACING_DISABLED";

    /*
     * Configurations that are loaded into the global configuration store when the application starts.
     */
    private static final String[] DEFAULT_CONFIGURATIONS = {
        PROPERTY_HTTP_PROXY,
        PROPERTY_HTTPS_PROXY,
        PROPERTY_IDENTITY_ENDPOINT,
        PROPERTY_IDENTITY_HEADER,
        PROPERTY_NO_PROXY,
        PROPERTY_MSI_ENDPOINT,
        PROPERTY_MSI_SECRET,
        PROPERTY_AZURE_SUBSCRIPTION_ID,
        PROPERTY_AZURE_USERNAME,
        PROPERTY_AZURE_PASSWORD,
        PROPERTY_AZURE_CLIENT_ID,
        PROPERTY_AZURE_CLIENT_SECRET,
        PROPERTY_AZURE_TENANT_ID,
        PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH,
        PROPERTY_AZURE_RESOURCE_GROUP,
        PROPERTY_AZURE_CLOUD,
        PROPERTY_AZURE_AUTHORITY_HOST,
        PROPERTY_AZURE_TELEMETRY_DISABLED,
        PROPERTY_AZURE_LOG_LEVEL,
        PROPERTY_AZURE_HTTP_LOG_DETAIL_LEVEL,
        PROPERTY_AZURE_TRACING_DISABLED,
        PROPERTY_AZURE_POD_IDENTITY_TOKEN_URL,
        PROPERTY_AZURE_REGIONAL_AUTHORITY_NAME
    };

    /*
     * Gets the global configuration shared by all client libraries.
     */
    private static final Configuration GLOBAL_CONFIGURATION = new Configuration();

    /**
     * No-op {@link Configuration} object used to opt out of using global configurations when constructing client
     * libraries.
     */
    public static final Configuration NONE = new NoopConfiguration();

    private final ConcurrentMap<String, String> configurations;

    /**
     * Constructs a configuration containing the known Azure properties constants.
     */
    public Configuration() {
        this.configurations = new ConcurrentHashMap<>();
        loadBaseConfiguration(this);
    }

    private Configuration(ConcurrentMap<String, String> configurations) {
        this.configurations = new ConcurrentHashMap<>(configurations);
    }

    /**
     * Gets the global configuration store shared by all client libraries.
     *
     * @return The global configuration store.
     */
    public static Configuration getGlobalConfiguration() {
        return GLOBAL_CONFIGURATION;
    }

    /**
     * Gets the value of the configuration.
     * <p>
     * This method first checks the values previously loaded from the environment, if the configuration is found there
     * it will be returned. Otherwise, this will attempt to load the value from the environment.
     *
     * @param name Name of the configuration.
     * @return Value of the configuration if found, otherwise null.
     */
    public String get(String name) {
        return getOrLoad(name);
    }

    /**
     * Gets the value of the configuration converted to {@code T}.
     * <p>
     * This method first checks the values previously loaded from the environment, if the configuration is found there
     * it will be returned. Otherwise, this will attempt to load the value from the environment.
     * <p>
     * If no configuration is found, the {@code defaultValue} is returned.
     *
     * @param name Name of the configuration.
     * @param defaultValue Value to return if the configuration isn't found.
     * @param <T> Type that the configuration is converted to if found.
     * @return The converted configuration if found, otherwise the default value is returned.
     */
    public <T> T get(String name, T defaultValue) {
        return convertOrDefault(getOrLoad(name), defaultValue);
    }

    /**
     * Gets the value of the configuration and converts it with the {@code converter}.
     * <p>
     * This method first checks the values previously loaded from the environment, if the configuration is found there
     * it will be returned. Otherwise, this will attempt to load the value from the environment.
     * <p>
     * If no configuration is found the {@code converter} won't be called and null will be returned.
     *
     * @param name Name of the configuration.
     * @param converter Converter used to map the configuration to {@code T}.
     * @param <T> Type that the configuration is converted to if found.
     * @return The converted configuration if found, otherwise null.
     */
    public <T> T get(String name, Function<String, T> converter) {
        String value = getOrLoad(name);
        if (CoreUtils.isNullOrEmpty(value)) {
            return null;
        }

        return converter.apply(value);
    }

    /*
     * Attempts to get the value of the configuration from the configuration store, if the value isn't found then it
     * attempts to load it from the runtime parameters then the environment variables.
     *
     * If no configuration is found null is returned.
     *
     * @param name Name of the configuration.
     * @return The configuration value from either the configuration store, runtime parameters, or environment
     * variable, in that order, if found, otherwise null.
     */
    private String getOrLoad(String name) {
        String value = configurations.get(name);
        if (value != null) {
            return value;
        }

        value = load(name);
        if (value != null) {
            configurations.put(name, value);
            return value;
        }

        return null;
    }

    /*
     * Attempts to load the configuration from the environment.
     *
     * The runtime parameters are checked first followed by the environment variables.
     *
     * @param name Name of the configuration.
     * @return If found the loaded configuration, otherwise null.
     */
    private String load(String name) {
        String value = loadFromProperties(name);

        if (value != null) {
            return value;
        }

        return loadFromEnvironment(name);
    }

    String loadFromEnvironment(String name) {
        return System.getenv(name);
    }

    String loadFromProperties(String name) {
        return System.getProperty(name);
    }

    /**
     * Adds a configuration with the given value.
     * <p>
     * This will overwrite the previous configuration value if it existed.
     *
     * @param name Name of the configuration.
     * @param value Value of the configuration.
     * @return The updated Configuration object.
     */
    public Configuration put(String name, String value) {
        configurations.put(name, value);
        return this;
    }

    /**
     * Removes the configuration.
     * <p>
     * This returns the value of the configuration if it previously existed.
     *
     * @param name Name of the configuration.
     * @return The configuration if it previously existed, otherwise null.
     */
    public String remove(String name) {
        return configurations.remove(name);
    }

    /**
     * Determines if the configuration exists.
     * <p>
     * This only checks against values previously loaded into the Configuration object, this won't inspect the
     * environment for containing the value.
     *
     * @param name Name of the configuration.
     * @return True if the configuration exists, otherwise false.
     */
    public boolean contains(String name) {
        return configurations.containsKey(name);
    }

    /**
     * Clones this Configuration object.
     *
     * @return A clone of the Configuration object.
     */
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public Configuration clone() {
        return new Configuration(configurations);
    }

    /*
     * Attempts to convert the configuration value to {@code T}.
     *
     * If the value is null or empty then the default value is returned.
     *
     * @param value Configuration value retrieved from the map.
     * @param defaultValue Default value to return if the configuration value is null or empty.
     * @param <T> Generic type that the value is converted to if not null or empty.
     * @return The converted configuration, if null or empty the default value.
     */
    @SuppressWarnings("unchecked")
    private static <T> T convertOrDefault(String value, T defaultValue) {
        // Value is null or empty, return the default.
        if (CoreUtils.isNullOrEmpty(value)) {
            return defaultValue;
        }

        // Check the default value's type to determine how it needs to be converted.
        Object convertedValue;
        if (defaultValue instanceof Byte) {
            convertedValue = Byte.parseByte(value);
        } else if (defaultValue instanceof Short) {
            convertedValue = Short.parseShort(value);
        } else if (defaultValue instanceof Integer) {
            convertedValue = Integer.parseInt(value);
        } else if (defaultValue instanceof Long) {
            convertedValue = Long.parseLong(value);
        } else if (defaultValue instanceof Float) {
            convertedValue = Float.parseFloat(value);
        } else if (defaultValue instanceof Double) {
            convertedValue = Double.parseDouble(value);
        } else if (defaultValue instanceof Boolean) {
            convertedValue = Boolean.parseBoolean(value);
        } else {
            convertedValue = value;
        }

        return (T) convertedValue;
    }

    private void loadBaseConfiguration(Configuration configuration) {
        for (String config : DEFAULT_CONFIGURATIONS) {
            String value = load(config);
            if (value != null) {
                configuration.put(config, value);
            }
        }
    }
}

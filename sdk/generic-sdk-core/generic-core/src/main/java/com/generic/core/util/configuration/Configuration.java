// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.util.configuration;

import com.generic.core.http.client.HttpClient;
import com.generic.core.implementation.util.EnvironmentConfiguration;
import com.generic.core.implementation.util.CoreUtils;
import com.generic.core.util.logging.ClientLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Contains configuration information that is used during construction of client libraries.
 *
 * <!-- src_embed com.azure.core.util.configuration.Configuration -->
 * <!-- end com.azure.core.util.configuration.Configuration -->
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
     * A list of hosts or CIDR to not use proxy HTTP/HTTPS connections through.
     */
    public static final String PROPERTY_NO_PROXY = "NO_PROXY";
    /**
     * Enables logging by setting a log level.
     */
    public static final String PROPERTY_LOG_LEVEL = "LOG_LEVEL";

    /**
     * Enables HTTP request/response logging by setting an HTTP log detail level.
     */
    public static final String PROPERTY_HTTP_LOG_DETAIL_LEVEL = "HTTP_LOG_DETAIL_LEVEL";

    /**
     * Sets the default number of times a request will be retried, if it passes the conditions for retrying, before it
     * fails.
     */
    public static final String PROPERTY_REQUEST_RETRY_COUNT = "REQUEST_RETRY_COUNT";

    /**
     * Sets the default timeout, in milliseconds, for a request to connect to the remote host.
     * <p>
     * If the configured value is equal to or less than 0 no timeout will be applied.
     */
    public static final String PROPERTY_REQUEST_CONNECT_TIMEOUT = "REQUEST_CONNECT_TIMEOUT";

    /**
     * Sets the default timeout interval, in milliseconds, allowed between each byte written by a request.
     * <p>
     * If the configured value is equal to or less than 0 no timeout will be applied.
     */
    public static final String PROPERTY_REQUEST_WRITE_TIMEOUT = "REQUEST_WRITE_TIMEOUT";

    /**
     * Sets the default timeout, in milliseconds, for a request to receive a response from the remote host.
     * <p>
     * If the configured value is equal to or less than 0 no timeout will be applied.
     */
    public static final String PROPERTY_REQUEST_RESPONSE_TIMEOUT = "REQUEST_RESPONSE_TIMEOUT";

    /**
     * Sets the default timeout interval, in milliseconds, allowed between each byte read in a response.
     * <p>
     * If the configured value is equal to or less than 0 no timeout will be applied.
     */
    public static final String PROPERTY_REQUEST_READ_TIMEOUT = "REQUEST_READ_TIMEOUT";

    /*
     * Gets the global configuration shared by all client libraries.
     */
    private static final Configuration GLOBAL_CONFIGURATION =
        new Configuration(Collections.emptyMap(), EnvironmentConfiguration.getGlobalConfiguration(), null, null);

    private static final ClientLogger LOGGER = new ClientLogger(Configuration.class);

    private final EnvironmentConfiguration environmentConfiguration;
    private final Map<String, String> configurations;
    private final String path;
    private final Configuration sharedConfiguration;
    private final boolean isEmpty;

    /**
     * Constructs a configuration containing the known properties constants. Use to create instance of
     * {@link Configuration}.
     *
     * @param configurations map of all properties.
     * @param environmentConfiguration instance of {@link EnvironmentConfiguration} to mock environment for testing.
     * @param path Absolute path of current configuration section for logging and diagnostics purposes.
     * @param sharedConfiguration Instance of shared {@link Configuration} section to retrieve shared properties.
     */
    Configuration(Map<String, String> configurations, EnvironmentConfiguration environmentConfiguration,
                  String path, Configuration sharedConfiguration) {
        this.configurations = configurations;
        this.isEmpty = configurations.isEmpty();
        this.environmentConfiguration = Objects.requireNonNull(environmentConfiguration,
            "'environmentConfiguration' cannot be null");
        this.path = path;
        this.sharedConfiguration = sharedConfiguration;
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
     * Gets the value of system property or environment variable. Use {@link Configuration#get(ConfigurationProperty)}
     * overload to get explicit configuration or environment configuration from specific source.
     *
     * <p>
     * This method first checks the values previously loaded from the environment, if the configuration is found there
     * it will be returned. Otherwise, this will attempt to load the value from the environment.
     *
     * @param name Name of the configuration.
     * @return Value of the configuration if found, otherwise null.
     */
    public String get(String name) {
        return environmentConfiguration.get(name);
    }

    /**
     * Gets the value of system property or environment variable converted to given primitive {@code T} using
     * corresponding {@code parse} method on this type.
     *
     * Use {@link Configuration)} overload to get explicit configuration or environment
     * configuration from specific source.
     *
     * <p>
     * This method first checks the values previously loaded from the environment, if the configuration is found there
     * it will be returned. Otherwise, this will attempt to load the value from the environment.
     * <p>
     * If no configuration is found, the {@code defaultValue} is returned.
     *
     * <p><b>Following types are supported:</b></p>
     * <ul>
     * <li>{@link Byte}</li>
     * <li>{@link Short}</li>
     * <li>{@link Integer}</li>
     * <li>{@link Long}</li>
     * <li>{@link Float}</li>
     * <li>{@link Double}</li>
     * <li>{@link Boolean}</li>
     * </ul>
     *
     * @param name Name of the configuration.
     * @param defaultValue Value to return if the configuration isn't found.
     * @param <T> Type that the configuration is converted to if found.
     * @return The converted configuration if found, otherwise the default value is returned.
     */
    public <T> T get(String name, T defaultValue) {
        return convertToPrimitiveOrDefault(get(name), defaultValue);
    }

    /**
     * Gets the value of system property or environment variable and converts it with the {@code converter}.
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
        Objects.requireNonNull(converter, "'converter' can't be null");
        return converter.apply(get(name));
    }

    /**
     * Determines if the system property or environment variable is defined.
     * <p>
     * Use {@link Configuration#contains(ConfigurationProperty)} overload to get explicit configuration or environment
     * configuration from specific source.
     *
     * <p>
     * This only checks against values previously loaded into the Configuration object, this won't inspect the
     * environment for containing the value.
     *
     * @param name Name of the configuration.
     * @return True if the configuration exists, otherwise false.
     */
    public boolean contains(String name) {
        return get(name) != null;
    }

    /**
     * Checks if configuration contains the property. If property can be shared between clients, checks this
     * {@code Configuration} and falls back to shared section. If property has aliases, system property or environment
     * variable defined, checks them as well.
     * <p>
     * Value is not validated.
     *
     * @param property instance.
     * @return true if property is available, false otherwise.
     */
    public boolean contains(ConfigurationProperty<?> property) {
        Objects.requireNonNull(property, "'property' can't be null");
        return getWithFallback(property) != null;
    }

    /**
     * Gets property value from all available sources in the following order:
     *
     * <ul>
     *     <li>Explicit configuration from given {@link ConfigurationSource} by property name</li>
     *     <li>Explicit configuration by property aliases in the order they were provided in {@link ConfigurationProperty}</li>
     *     <li>Explicit configuration by property name in the shared section (if {@link ConfigurationProperty} is shared)</li>
     *     <li>Explicit configuration by property aliases in the shared section (if {@link ConfigurationProperty} is shared)</li>
     *     <li>System property (if set)</li>
     *     <li>Environment variable (if set)</li>
     * </ul>
     *
     * <p>
     * Property value is converted to specified type. If property value is missing and not required, default value is returned.
     *
     * <!-- src_embed com.azure.core.util.configuration.Configuration.get#ConfigurationProperty -->
     * <!-- end com.azure.core.util.configuration.Configuration.get#ConfigurationProperty -->
     *
     * @param property instance.
     * @param <T> Type that the configuration is converted to if found.
     * @return The value of the property if it exists, otherwise the default value of the property.
     * @throws NullPointerException when property instance is null.
     * @throws IllegalArgumentException when required property is missing.
     * @throws RuntimeException when property value conversion (and validation) throws.
     */
    public <T> T get(ConfigurationProperty<T> property) {
        Objects.requireNonNull(property, "'property' cannot be null");
        String value = getWithFallback(property);

        if (value == null) {
            if (property.isRequired()) {
                throw LOGGER.atError()
                    .addKeyValue("name", property.getName())
                    .addKeyValue("path", path)
                    .log(new IllegalArgumentException("Missing required property."));
            }
            return property.getDefaultValue();
        }

        try {
            return property.getConverter().apply(value);
        } catch (RuntimeException ex) {
            throw LOGGER.atError()
                .addKeyValue("name", property.getName())
                .addKeyValue("path", path)
                .addKeyValue("value", property.getValueSanitizer().apply(value))
                .log(ex);
        }
    }

    private String getLocalProperty(String name, Iterable<String> aliases, Function<String, String> valueSanitizer) {
        if (this.isEmpty) {
            return null;
        }

        final String value = configurations.get(name);
        if (value != null) {
            LOGGER.atVerbose()
                .addKeyValue("name", name)
                .addKeyValue("path", path)
                .addKeyValue("value", () -> valueSanitizer.apply(value))
                .log("Got property value by name.");

            return value;
        }

        for (String alias : aliases) {
            final String valueByAlias = configurations.get(alias);
            if (valueByAlias != null) {
                LOGGER.atVerbose()
                    .addKeyValue("name", name)
                    .addKeyValue("path", path)
                    .addKeyValue("alias", alias)
                    .addKeyValue("value", () -> valueSanitizer.apply(valueByAlias))
                    .log("Got property value by alias.");
                return valueByAlias;
            }
        }

        return null;
    }

    private <T> String getWithFallback(ConfigurationProperty<T> property) {
        String name = property.getName();
        if (!CoreUtils.isNullOrEmpty(name)) {
            String value = getLocalProperty(name, property.getAliases(), property.getValueSanitizer());
            if (value != null) {
                return value;
            }

            if (property.isShared() && sharedConfiguration != null) {
                value = sharedConfiguration.getLocalProperty(name, property.getAliases(), property.getValueSanitizer());
                if (value != null) {
                    return value;
                }
            }
        }
        return getFromEnvironment(property);
    }

    private <T> String getFromEnvironment(ConfigurationProperty<T> property) {
        String systemProperty = property.getSystemPropertyName();
        if (systemProperty != null) {
            final String value = environmentConfiguration.getSystemProperty(systemProperty);
            if (value != null) {
                LOGGER.atVerbose()
                    .addKeyValue("name", property.getName())
                    .addKeyValue("systemProperty", systemProperty)
                    .addKeyValue("value", () -> property.getValueSanitizer().apply(value))
                    .log("Got property from system property.");
                return value;
            }
        }

        String envVar = property.getEnvironmentVariableName();
        if (envVar != null) {
            final String value = environmentConfiguration.getEnvironmentVariable(envVar);
            if (value != null) {
                LOGGER.atVerbose()
                    .addKeyValue("name", property.getName())
                    .addKeyValue("envVar", envVar)
                    .addKeyValue("value", () -> property.getValueSanitizer().apply(value))
                    .log("Got property from environment variable.");
                return value;
            }
        }

        return null;
    }

    private static Map<String, String> readConfigurations(ConfigurationSource source, String path) {
        Objects.requireNonNull(source, "'source' cannot be null");
        Map<String, String> configs = source.getProperties(path);

        if (configs == null || configs.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> props = new HashMap<>();

        for (Map.Entry<String, String> prop : configs.entrySet()) {
            String key = CoreUtils.isNullOrEmpty(path) ? prop.getKey() : prop.getKey().substring(path.length() + 1);
            String value = prop.getValue();

            LOGGER.atVerbose()
                .addKeyValue("name", prop.getKey())
                .log("Got property from configuration source.");

            if (key != null && value != null) {
                props.put(key, value);
            } else {
                LOGGER.atWarning()
                    .addKeyValue("name", prop.getKey())
                    .log("Key or value is null, property is ignored.");
            }
        }

        return props;
    }

    /**
     * Attempts to convert the configuration value to given primitive {@code T} using corresponding {@code parse} method
     * on this type.
     *
     * <p><b>Following types are supported:</b></p>
     * <ul>
     * <li>{@link Byte}</li>
     * <li>{@link Short}</li>
     * <li>{@link Integer}</li>
     * <li>{@link Long}</li>
     * <li>{@link Float}</li>
     * <li>{@link Double}</li>
     * <li>{@link Boolean}</li>
     * </ul>
     *
     * If the value is null or empty then the default value is returned.
     *
     * @param value Configuration value retrieved from the map.
     * @param defaultValue Default value to return if the configuration value is null or empty.
     * @param <T> Generic type that the value is converted to if not null or empty.
     * @return The converted configuration, if null or empty the default value.
     */
    @SuppressWarnings("unchecked")
    private static <T> T convertToPrimitiveOrDefault(String value, T defaultValue) {
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
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util;

import com.typespec.core.http.HttpClient;
import com.typespec.core.http.HttpClientProvider;
import com.typespec.core.implementation.util.EnvironmentConfiguration;
import com.typespec.core.util.logging.ClientLogger;
import com.typespec.core.util.metrics.Meter;
import com.typespec.core.util.metrics.MeterProvider;
import com.typespec.core.util.tracing.Tracer;
import com.typespec.core.util.tracing.TracerProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Contains configuration information that is used during construction of client libraries.
 *
 * <!-- src_embed com.azure.core.util.Configuration -->
 * <pre>
 * Configuration configuration = new ConfigurationBuilder&#40;new SampleSource&#40;properties&#41;&#41;
 *     .root&#40;&quot;azure.sdk&quot;&#41;
 *     .buildSection&#40;&quot;client-name&quot;&#41;;
 *
 * ConfigurationProperty&lt;String&gt; proxyHostnameProperty = ConfigurationPropertyBuilder.ofString&#40;&quot;http.proxy.hostname&quot;&#41;
 *     .shared&#40;true&#41;
 *     .build&#40;&#41;;
 * System.out.println&#40;configuration.get&#40;proxyHostnameProperty&#41;&#41;;
 * </pre>
 * <!-- end com.azure.core.util.Configuration -->
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
     * Path of a PFX/PEM certificate file to use when performing service principal authentication with Azure.
     */
    public static final String PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH = "AZURE_CLIENT_CERTIFICATE_PATH";

    /**
     * Password for a PFX/PEM certificate used when performing service principal authentication with Azure.
     */
    public static final String PROPERTY_AZURE_CLIENT_CERTIFICATE_PASSWORD = "AZURE_CLIENT_CERTIFICATE_PASSWORD";

    /**
     * Flag to disable the CP1 client capabilities in Azure Identity Token credentials.
     */
    public static final String PROPERTY_AZURE_IDENTITY_DISABLE_CP1 = "AZURE_IDENTITY_DISABLE_CP1";

    /**
     * URL used by Bridge To Kubernetes to redirect IMDS calls in the development environment.
     */
    public static final String PROPERTY_AZURE_POD_IDENTITY_TOKEN_URL = "AZURE_POD_IDENTITY_TOKEN_URL";

    /**
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

    /**
     * Sets the name of the {@link TracerProvider} implementation that should be used to construct instances of
     * {@link Tracer}.
     * <p>
     * The name must be the full class name, e.g. {@code com.azure.core.tracing.opentelemetry.OpenTelemetryTracerProvider} and not
     * {@code OpenTelemetryTracerProvider}.
     * <p>
     * If the value isn't set or is an empty string the first {@link TracerProvider} resolved by {@link java.util.ServiceLoader} will be
     * used to create an instance of {@link Tracer}. If the value is set and doesn't match any
     * {@link TracerProvider} resolved by {@link java.util.ServiceLoader} an {@link IllegalStateException} will be thrown when
     * attempting to create an instance of {@link TracerProvider}.
     */
    public static final String PROPERTY_AZURE_TRACING_IMPLEMENTATION = "AZURE_TRACING_IMPLEMENTATION";

    /**
     * Disables metrics.
     */
    public static final String PROPERTY_AZURE_METRICS_DISABLED = "AZURE_METRICS_DISABLED";

    /**
     * Sets the name of the {@link MeterProvider} implementation that should be used to construct instances of
     * {@link Meter}.
     * <p>
     * The name must be the full class name, e.g. {@code com.azure.core.tracing.opentelemetry.OpenTelemetryMeterProvider} and not
     * {@code OpenTelemetryMeterProvider}.
     * <p>
     * If the value isn't set or is an empty string the first {@link MeterProvider} resolved by {@link java.util.ServiceLoader} will be
     * used to create an instance of {@link Meter}. If the value is set and doesn't match any
     * {@link MeterProvider} resolved by {@link java.util.ServiceLoader} an {@link IllegalStateException} will be thrown when
     * attempting to create an instance of {@link MeterProvider}.
     */
    public static final String PROPERTY_AZURE_METRICS_IMPLEMENTATION = "AZURE_METRICS_IMPLEMENTATION";

    /**
     * Sets the default number of times a request will be retried, if it passes the conditions for retrying, before it
     * fails.
     */
    public static final String PROPERTY_AZURE_REQUEST_RETRY_COUNT = "AZURE_REQUEST_RETRY_COUNT";

    /**
     * Sets the default timeout, in milliseconds, for a request to connect to the remote host.
     * <p>
     * If the configured value is equal to or less than 0 no timeout will be applied.
     */
    public static final String PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT = "AZURE_REQUEST_CONNECT_TIMEOUT";

    /**
     * Sets the default timeout interval, in milliseconds, allowed between each byte written by a request.
     * <p>
     * If the configured value is equal to or less than 0 no timeout will be applied.
     */
    public static final String PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT = "AZURE_REQUEST_WRITE_TIMEOUT";

    /**
     * Sets the default timeout, in milliseconds, for a request to receive a response from the remote host.
     * <p>
     * If the configured value is equal to or less than 0 no timeout will be applied.
     */
    public static final String PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT = "AZURE_REQUEST_RESPONSE_TIMEOUT";

    /**
     * Sets the default timeout interval, in milliseconds, allowed between each byte read in a response.
     * <p>
     * If the configured value is equal to or less than 0 no timeout will be applied.
     */
    public static final String PROPERTY_AZURE_REQUEST_READ_TIMEOUT = "AZURE_REQUEST_READ_TIMEOUT";

    /**
     * Sets the name of the {@link HttpClientProvider} implementation that should be used to construct instances of
     * {@link HttpClient}.
     * <p>
     * The name must be the full class name, ex {@code com.azure.core.http.netty.NettyAsyncHttpClientProvider} and not
     * {@code NettyAsyncHttpClientProvider}, to disambiguate multiple providers with the same name but from different
     * packages.
     * <p>
     * If the value isn't set or is an empty string the first {@link HttpClientProvider} resolved by {@link java.util.ServiceLoader} will be
     * used to create an instance of {@link HttpClient}. If the value is set and doesn't match any
     * {@link HttpClientProvider} resolved by {@link java.util.ServiceLoader} an {@link IllegalStateException} will be thrown when
     * attempting to create an instance of {@link HttpClient}.
     */
    public static final String PROPERTY_AZURE_HTTP_CLIENT_IMPLEMENTATION = "AZURE_HTTP_CLIENT_IMPLEMENTATION";

    /*
     * Gets the global configuration shared by all client libraries.
     */
    private static final Configuration GLOBAL_CONFIGURATION = new Configuration();

    /**
     * No-op {@link Configuration} object used to opt out of using global configurations when constructing client
     * libraries.
     */
    @SuppressWarnings("StaticInitializerReferencesSubClass")
    public static final Configuration NONE = new NoopConfiguration();
    private static final ClientLogger LOGGER = new ClientLogger(Configuration.class);

    private final EnvironmentConfiguration environmentConfiguration;
    private final Map<String, String> configurations;
    private final String path;
    private final Configuration sharedConfiguration;
    private final boolean isEmpty;

    /**
     * Constructs a configuration containing the known Azure properties constants.
     *
     * @deprecated Use {@link ConfigurationBuilder} and {@link ConfigurationSource} that allow to provide all properties
     * before creating configuration and keep it immutable.
     */
    @Deprecated
    public Configuration() {
        this(Collections.emptyMap(), EnvironmentConfiguration.getGlobalConfiguration(), null, null);
    }

    /**
     * Constructs a configuration containing the known Azure properties constants. Use {@link ConfigurationBuilder} to
     * create instance of {@link Configuration}.
     *
     * @param configurationSource Configuration property source.
     * @param environmentConfiguration instance of {@link EnvironmentConfiguration} to mock environment for testing.
     * @param path Absolute path of current configuration section for logging and diagnostics purposes.
     * @param sharedConfiguration Instance of shared {@link Configuration} section to retrieve shared properties.
     */
    Configuration(ConfigurationSource configurationSource, EnvironmentConfiguration environmentConfiguration,
        String path, Configuration sharedConfiguration) {
        this(readConfigurations(configurationSource, path), environmentConfiguration, path, sharedConfiguration);
    }

    /**
     * Constructs a configuration containing the known Azure properties constants. Use {@link ConfigurationBuilder} to
     * create instance of {@link Configuration}.
     *
     * @param configurations map of all properties.
     * @param environmentConfiguration instance of {@link EnvironmentConfiguration} to mock environment for testing.
     * @param path Absolute path of current configuration section for logging and diagnostics purposes.
     * @param sharedConfiguration Instance of shared {@link Configuration} section to retrieve shared properties.
     */
    private Configuration(Map<String, String> configurations, EnvironmentConfiguration environmentConfiguration,
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
     * Use {@link Configuration#get(ConfigurationProperty)} overload to get explicit configuration or environment
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
     * Adds a configuration with the given value.
     * <p>
     * This will overwrite the previous configuration value if it existed.
     *
     * @param name Name of the configuration.
     * @param value Value of the configuration.
     * @return The updated Configuration object.
     * @deprecated Use {@link ConfigurationBuilder} and {@link ConfigurationSource} to provide all properties before
     * creating configuration.
     */
    @Deprecated
    public Configuration put(String name, String value) {
        environmentConfiguration.put(name, value);
        return this;
    }

    /**
     * Removes the configuration.
     * <p>
     * This returns the value of the configuration if it previously existed.
     *
     * @param name Name of the configuration.
     * @return The configuration if it previously existed, otherwise null.
     * @deprecated Use {@link ConfigurationBuilder} and {@link ConfigurationSource} to provide all properties before
     * creating configuration.
     */
    @Deprecated
    public String remove(String name) {
        return environmentConfiguration.remove(name);
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
     * Clones this Configuration object.
     *
     * @return A clone of the Configuration object.
     * @deprecated Use {@link ConfigurationBuilder} and {@link ConfigurationSource} to create configuration.
     */
    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Deprecated
    public Configuration clone() {
        return new Configuration(configurations, new EnvironmentConfiguration(environmentConfiguration), path,
            sharedConfiguration);
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
     * <!-- src_embed com.azure.core.util.Configuration.get#ConfigurationProperty -->
     * <pre>
     * ConfigurationProperty&lt;String&gt; property = ConfigurationPropertyBuilder.ofString&#40;&quot;http.proxy.hostname&quot;&#41;
     *     .shared&#40;true&#41;
     *     .logValue&#40;true&#41;
     *     .systemPropertyName&#40;&quot;http.proxyHost&quot;&#41;
     *     .build&#40;&#41;;
     *
     * &#47;&#47; attempts to get local `azure.sdk.&lt;client-name&gt;.http.proxy.host` property and falls back to
     * &#47;&#47; shared azure.sdk.http.proxy.port
     * System.out.println&#40;configuration.get&#40;property&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.Configuration.get#ConfigurationProperty -->
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

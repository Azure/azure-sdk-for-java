// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.cloud.appconfiguration.config.ConfigurationClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.implementation.http.policy.BaseAppConfigurationPolicy;
import com.azure.spring.cloud.appconfiguration.config.implementation.http.policy.TracingInfo;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;
import com.azure.spring.cloud.autoconfigure.implementation.appconfiguration.AzureAppConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.cloud.core.service.AzureServiceType.AppConfiguration;
import com.azure.spring.cloud.service.implementation.appconfiguration.ConfigurationClientBuilderFactory;

public class AppConfigurationReplicaClientsBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationReplicaClientsBuilder.class);

    /**
     * Invalid Connection String error message
     */
    private static final String NON_EMPTY_MSG = "%s property should not be null or empty in the connection string of Azure Config Service.";

    private static final String RETRY_MODE_PROPERTY_NAME = "retry.mode";

    private static final String MAX_RETRIES_PROPERTY_NAME = "retry.exponential.max-retries";

    private static final String BASE_DELAY_PROPERTY_NAME = "retry.exponential.base-delay";

    private static final String MAX_DELAY_PROPERTY_NAME = "retry.exponential.max-delay";

    private static final Duration DEFAULT_MIN_RETRY_POLICY = Duration.ofMillis(800);

    private static final Duration DEFAULT_MAX_RETRY_POLICY = Duration.ofSeconds(8);

    /**
     * Connection String Regex format
     */
    private static final String CONN_STRING_REGEXP = "Endpoint=([^;]+);Id=([^;]+);Secret=([^;]+)";

    /**
     * Invalid Formatted Connection String Error message
     */
    private static final String ENDPOINT_ERR_MSG = String.format("Connection string does not follow format %s.",
        CONN_STRING_REGEXP);

    private static final Pattern CONN_STRING_PATTERN = Pattern.compile(CONN_STRING_REGEXP);

    private final ConfigurationClientCustomizer clientCustomizer;

    private final ConfigurationClientBuilderFactory clientFactory;

    private final boolean isKeyVaultConfigured;

    private final boolean credentialConfigured;

    private final int defaultMaxRetries = 2;

    AppConfigurationReplicaClientsBuilder(ConfigurationClientBuilderFactory clientFactory,
        ConfigurationClientCustomizer clientCustomizer, boolean credentialConfigured, boolean isKeyVaultConfigured) {
        this.credentialConfigured = credentialConfigured;
        this.clientFactory = clientFactory;
        this.clientCustomizer = clientCustomizer;
        this.isKeyVaultConfigured = isKeyVaultConfigured;
    }

    /**
     * Given a connection string, returns the endpoint inside of it.
     * 
     * @param connectionString connection string to app configuration
     * @return endpoint
     * @throws IllegalStateException when connection string isn't valid.
     */
    public static String getEndpointFromConnectionString(String connectionString) {
        Assert.hasText(connectionString, "Connection string cannot be empty.");

        Matcher matcher = CONN_STRING_PATTERN.matcher(connectionString);
        if (!matcher.find()) {
            throw new IllegalStateException(ENDPOINT_ERR_MSG);
        }

        String endpoint = matcher.group(1);

        Assert.hasText(endpoint, String.format(NON_EMPTY_MSG, "Endpoint"));

        return endpoint;
    }

    /**
     * Builds all the clients for a connection.
     * 
     * @throws IllegalArgumentException when more than 1 connection method is given.
     */
    List<AppConfigurationReplicaClient> buildClients(ConfigStore configStore) {
        // Defensive copies — ConfigStore supports both singular (connectionString/endpoint) and
        // plural (connectionStrings/endpoints) properties. We normalize into these local lists.
        List<String> connectionStrings = new ArrayList<>(configStore.getConnectionStrings());
        List<String> endpoints = new ArrayList<>(configStore.getEndpoints());

        boolean hasSingleConnectionString = StringUtils.hasText(configStore.getConnectionString());
        boolean hasMultiEndpoints = !endpoints.isEmpty();
        boolean hasMultiConnectionStrings = !connectionStrings.isEmpty();

        boolean hasConnectionString = hasSingleConnectionString || hasMultiConnectionStrings;

        // Connection strings include their own auth (Id + Secret), so they cannot be combined with
        // endpoints or with credentialConfigured (which uses Entra ID via Spring Cloud Azure auto-config).
        // Endpoints + credentialConfigured is valid — that's the intended Entra ID auth flow.
        if ((hasConnectionString && hasMultiEndpoints)
            || (hasSingleConnectionString && hasMultiConnectionStrings)
            || (credentialConfigured && hasConnectionString)) {
            throw new IllegalArgumentException(
                "More than 1 connection method was set for connecting to App Configuration.");
        }

        if (hasSingleConnectionString) {
            // Normalize singular properties into the lists.
            connectionStrings.add(configStore.getConnectionString());
        } else if (endpoints.isEmpty() && StringUtils.hasText(configStore.getEndpoint())) {
            // Single endpoint is the recommended Entra ID connection method. When the plural
            // endpoints list is populated, validateAndInit already sets endpoint = endpoints[0],
            // so we must not add it again.
            endpoints.add(configStore.getEndpoint());
        }

        List<AppConfigurationReplicaClient> clients;

        if (!connectionStrings.isEmpty()) {
            clients = buildClientsFromConnectionStrings(connectionStrings, configStore.getEndpoint());
        } else {
            clients = buildClientsFromEndpoints(endpoints, configStore.getEndpoint());
        }

        if (configStore.isLoadBalancingEnabled()) {
            Collections.shuffle(clients);
        }

        return clients;
    }

    /**
     * Builds a single client for an auto-failover endpoint discovered via DNS SRV.
     * Reuses the auth method from the original ConfigStore but targets the failover endpoint.
     *
     * @param failoverEndpoint the failover endpoint to connect to
     * @param configStore the config store providing auth credentials
     * @return a replica client targeting the failover endpoint
     */
    AppConfigurationReplicaClient buildClient(String failoverEndpoint, ConfigStore configStore) {
        if (StringUtils.hasText(configStore.getConnectionString())) {
            return buildClientFromConnectionString(configStore.getConnectionString(), failoverEndpoint,
                configStore.getEndpoint());
        } else if (!configStore.getConnectionStrings().isEmpty()) {
            return buildClientFromConnectionString(configStore.getConnectionStrings().get(0), failoverEndpoint,
                configStore.getEndpoint());
        } else {
            return buildClientFromEndpoint(failoverEndpoint, configStore.getEndpoint(), null, 0);
        }
    }

    private List<AppConfigurationReplicaClient> buildClientsFromConnectionStrings(List<String> connectionStrings,
        String originEndpoint) {
        List<AppConfigurationReplicaClient> clients = new ArrayList<>();
        for (String connectionString : connectionStrings) {
            clientFactory.setConnectionStringProvider(new ConnectionStringConnector(connectionString));
            String endpoint = getEndpointFromConnectionString(connectionString);
            LOGGER.debug("Connecting to {} using Connection String.", endpoint);
            ConfigurationClientBuilder builder = createBuilderInstance().connectionString(connectionString);
            clients.add(
                modifyAndBuildClient(builder, endpoint, originEndpoint, connectionStrings.size() - 1));
        }
        return clients;
    }

    private List<AppConfigurationReplicaClient> buildClientsFromEndpoints(List<String> endpoints,
        String originEndpoint) {
        List<AppConfigurationReplicaClient> clients = new ArrayList<>();
        // When credentialConfigured is true, the credential is already set on the builder via
        // ConfigurationClientBuilderFactory (Spring Cloud Azure auto-config). Otherwise, create a
        // shared DefaultAzureCredential to avoid the cost of building one per endpoint.
        DefaultAzureCredential defaultAzureCredential = credentialConfigured ? null
            : new DefaultAzureCredentialBuilder().build();
        int replicaCount = endpoints.size() - 1;
        for (String endpoint : endpoints) {
            clients.add(buildClientFromEndpoint(endpoint, originEndpoint, defaultAzureCredential, replicaCount));
        }
        return clients;
    }

    private AppConfigurationReplicaClient buildClientFromEndpoint(String endpoint, String originEndpoint,
        DefaultAzureCredential sharedCredential, int replicaCount) {
        ConfigurationClientBuilder builder = createBuilderInstance();
        // When credentialConfigured is false, no credential was provided via Spring Cloud Azure
        // auto-config, so we fall back to DefaultAzureCredential. Prefer the shared instance
        // when available; create a new one only for single-endpoint failover calls (null passed in).
        if (!credentialConfigured) {
            builder.credential(sharedCredential != null ? sharedCredential
                : new DefaultAzureCredentialBuilder().build());
        }
        builder.endpoint(endpoint);
        return modifyAndBuildClient(builder, endpoint, originEndpoint, replicaCount);
    }

    /**
     * For failover: parses the original connection string, swaps the endpoint to the failover
     * target, and rebuilds. This preserves the original Id/Secret credentials.
     */
    private AppConfigurationReplicaClient buildClientFromConnectionString(String connectionString,
        String failoverEndpoint, String originEndpoint) {
        ConnectionString connStr = new ConnectionString(connectionString);
        connStr.setUri(failoverEndpoint);
        ConfigurationClientBuilder builder = createBuilderInstance().connectionString(connStr.toFullString());
        return modifyAndBuildClient(builder, failoverEndpoint, originEndpoint, 0);
    }

    /**
     * Final assembly: adds the tracing/telemetry HTTP policy, applies any user-provided customizer,
     * then builds the ConfigurationClient and wraps it in a replica-aware client.
     */
    private AppConfigurationReplicaClient modifyAndBuildClient(ConfigurationClientBuilder builder, String endpoint,
        String originEndpoint,
        Integer replicaCount) {
        TracingInfo tracingInfo = new TracingInfo(isKeyVaultConfigured, replicaCount,
            Configuration.getGlobalConfiguration());
        builder.addPolicy(new BaseAppConfigurationPolicy(tracingInfo));

        if (clientCustomizer != null) {
            clientCustomizer.customize(builder, endpoint);
        }
        return new AppConfigurationReplicaClient(endpoint, originEndpoint, builder.buildClient());
    }

    /**
     * Creates a ConfigurationClientBuilder with retry policy configured from system properties.
     * Properties are checked at two levels: service-specific (spring.cloud.azure.appconfiguration)
     * takes precedence over global (spring.cloud.azure). Defaults to exponential backoff.
     */
    private ConfigurationClientBuilder createBuilderInstance() {
        RetryStrategy retryStrategy = null;

        String mode = System.getProperty(AzureGlobalProperties.PREFIX + "." + RETRY_MODE_PROPERTY_NAME);
        String modeService = System
            .getProperty(AzureAppConfigurationProperties.PREFIX + "." + RETRY_MODE_PROPERTY_NAME);

        if ("exponential".equals(mode) || "exponential".equals(modeService) || (mode == null && modeService == null)) {
            Function<String, Integer> checkPropertyInt = Integer::parseInt;
            Function<String, Duration> checkPropertyDuration = parameter -> (DurationStyle.detectAndParse(parameter));

            int retries = checkProperty(MAX_RETRIES_PROPERTY_NAME, defaultMaxRetries,
                " isn't a valid integer, using default value.", checkPropertyInt);

            Duration baseDelay = checkProperty(BASE_DELAY_PROPERTY_NAME, DEFAULT_MIN_RETRY_POLICY,
                " isn't a valid Duration, using default value.", checkPropertyDuration);
            Duration maxDelay = checkProperty(MAX_DELAY_PROPERTY_NAME, DEFAULT_MAX_RETRY_POLICY,
                " isn't a valid Duration, using default value.", checkPropertyDuration);

            retryStrategy = new ExponentialBackoff(retries, baseDelay, maxDelay);
        }

        ConfigurationClientBuilder builder = clientFactory.build();

        if (retryStrategy != null) {
            builder.retryPolicy(new RetryPolicy(retryStrategy));
        }

        return builder;
    }

    /**
     * Reads a retry-related property from system properties. Service-specific properties
     * ({@code spring.cloud.azure.appconfiguration.{name}}) override global ones ({@code spring.cloud.azure.{name}}).
     * Falls back to defaultValue if the property is missing or fails to parse.
     */
    private <T> T checkProperty(String propertyName, T defaultValue, String errMsg, Function<String, T> fn) {
        String envValue = System.getProperty(AzureGlobalProperties.PREFIX + "." + propertyName);
        String envServiceValue = System.getProperty(AzureAppConfigurationProperties.PREFIX + "." + propertyName);
        T value = defaultValue;

        if (envServiceValue != null) {
            try {
                value = fn.apply(envServiceValue);
            } catch (Exception e) {
                LOGGER.warn("{}.{} {}", AzureAppConfigurationProperties.PREFIX, propertyName, errMsg);
            }
        } else if (envValue != null) {
            try {
                value = fn.apply(envValue);
            } catch (Exception e) {
                LOGGER.warn("{}.{} {}", AzureGlobalProperties.PREFIX, propertyName, errMsg);
            }
        }

        return value;
    }

    /**
     * Adapter to provide a connection string to Spring Cloud Azure's auto-configured
     * client builders, enabling other Spring Cloud Azure integrations (e.g., Key Vault
     * secret resolution) to authenticate to App Configuration.
     */
    private static class ConnectionStringConnector
        implements ServiceConnectionStringProvider<AzureServiceType.AppConfiguration> {

        private final String connectionString;

        ConnectionStringConnector(String connectionString) {
            this.connectionString = connectionString;
        }

        @Override
        public String getConnectionString() {
            return connectionString;
        }

        @Override
        public AppConfiguration getServiceType() {
            return null;
        }
    }

    /**
     * Parses an App Configuration connection string into its components (Endpoint, Id, Secret).
     * Used for auto-failover: {@link #setUri(String)} swaps the endpoint while preserving the original
     * credentials, and {@link #toFullString()} reassembles the connection string for the SDK.
     * {@link #toString()} always redacts the secret for safe logging.
     */
    static class ConnectionString {
        private static final String ENDPOINT = "Endpoint=";

        private static final String ID = "Id=";

        private static final String SECRET_PREFIX = "Secret=";

        private URL baseUri;

        private final String id;

        private final String secret;

        ConnectionString(String connectionString) {
            if (CoreUtils.isNullOrEmpty(connectionString)) {
                throw new IllegalArgumentException("'connectionString' cannot be null or empty.");
            }
            String[] args = connectionString.split(";");
            if (args.length < 3) {
                throw new IllegalArgumentException("invalid connection string segment count");
            }
            URL parsedUri = null;
            String parsedId = null;
            String parsedSecret = null;
            for (String arg : args) {
                String segment = arg.trim();
                if (ENDPOINT.regionMatches(true, 0, segment, 0, ENDPOINT.length())) {
                    try {
                        parsedUri = new URI(segment.substring(ENDPOINT.length())).toURL();
                    } catch (MalformedURLException | URISyntaxException ex) {
                        throw new IllegalArgumentException(ex);
                    }
                } else if (ID.regionMatches(true, 0, segment, 0, ID.length())) {
                    parsedId = segment.substring(ID.length());
                } else if (SECRET_PREFIX.regionMatches(true, 0, segment, 0, SECRET_PREFIX.length())) {
                    parsedSecret = segment.substring(SECRET_PREFIX.length());
                }
            }
            this.baseUri = parsedUri;
            this.id = parsedId;
            this.secret = parsedSecret;
            if (this.baseUri == null || CoreUtils.isNullOrEmpty(this.id)
                || this.secret == null || this.secret.length() == 0) {
                throw new IllegalArgumentException("Could not parse 'connectionString'."
                    + " Expected format: 'endpoint={endpoint};id={id};secret={secret}'.");
            }
        }

        protected ConnectionString setUri(String uri) {
            try {
                this.baseUri = new URI(uri).toURL();
            } catch (MalformedURLException | URISyntaxException ex) {
                throw new IllegalArgumentException(ex);
            }
            return this;
        }

        /**
         * Returns the full connection string including the secret. Use only when passing
         * to the SDK's {@link ConfigurationClientBuilder#connectionString(String)}.
         */
        String toFullString() {
            return String.format("%s%s;%s%s;%s%s", ENDPOINT, baseUri, ID, id, SECRET_PREFIX, secret);
        }

        /**
         * Returns the connection string with the secret redacted. Safe for logging and error messages.
         */
        @Override
        public String toString() {
            return String.format("%s%s;%s%s;%s<REDACTED>", ENDPOINT, baseUri, ID, id, SECRET_PREFIX);
        }
    }
}

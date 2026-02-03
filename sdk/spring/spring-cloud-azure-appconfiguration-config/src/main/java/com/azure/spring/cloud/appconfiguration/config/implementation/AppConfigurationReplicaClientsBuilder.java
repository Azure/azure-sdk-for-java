// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
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

    private ConfigurationClientCustomizer clientCustomizer;

    private final ConfigurationClientBuilderFactory clientFactory;

    private boolean isKeyVaultConfigured;

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
        List<AppConfigurationReplicaClient> clients = new ArrayList<>();
        // Single client or Multiple?
        // If single call buildClient
        int hasSingleConnectionString = StringUtils.hasText(configStore.getConnectionString()) ? 1 : 0;
        int hasMultiEndpoints = configStore.getEndpoints().size() > 0 ? 1 : 0;
        int hasMultiConnectionString = configStore.getConnectionStrings().size() > 0 ? 1 : 0;

        if (hasSingleConnectionString + hasMultiEndpoints + hasMultiConnectionString > 1) {
            throw new IllegalArgumentException(
                "More than 1 connection method was set for connecting to App Configuration.");
        }

        boolean connectionStringIsPresent = configStore.getConnectionString() != null
            || configStore.getConnectionStrings().size() > 0;

        if (credentialConfigured && connectionStringIsPresent) {
            throw new IllegalArgumentException(
                "More than 1 connection method was set for connecting to App Configuration.");
        }

        List<String> connectionStrings = configStore.getConnectionStrings();
        List<String> endpoints = configStore.getEndpoints();

        if (connectionStrings.size() == 0 && StringUtils.hasText(configStore.getConnectionString())) {
            connectionStrings.add(configStore.getConnectionString());
        }

        if (endpoints.size() == 0 && StringUtils.hasText(configStore.getEndpoint())) {
            endpoints.add(configStore.getEndpoint());
        }

        if (connectionStrings.size() > 0) {
            for (String connectionString : connectionStrings) {
                clientFactory.setConnectionStringProvider(new ConnectionStringConnector(connectionString));
                String endpoint = getEndpointFromConnectionString(connectionString);
                LOGGER.debug("Connecting to " + endpoint + " using Connecting String.");
                ConfigurationClientBuilder builder = createBuilderInstance().connectionString(connectionString);

                clients.add(modifyAndBuildClient(builder, endpoint, configStore.getEndpoint(), connectionStrings.size() - 1));
            }
        } else {
            DefaultAzureCredential defautAzureCredential = new DefaultAzureCredentialBuilder().build();
            for (String endpoint : endpoints) {
                ConfigurationClientBuilder builder = this.createBuilderInstance();
                if (!credentialConfigured) {
                    builder.credential(defautAzureCredential);
                }

                builder.endpoint(endpoint);

                clients.add(modifyAndBuildClient(builder, endpoint, configStore.getEndpoint(), endpoints.size() - 1));
            }
        }
        return clients;
    }

    AppConfigurationReplicaClient buildClient(String failoverEndpoint, ConfigStore configStore) {

        if (StringUtils.hasText(configStore.getConnectionString())) {
            ConnectionString connectionString = new ConnectionString(configStore.getConnectionString());
            connectionString.setUri(failoverEndpoint);
            ConfigurationClientBuilder builder = createBuilderInstance().connectionString(connectionString.toString());
            return modifyAndBuildClient(builder, failoverEndpoint, configStore.getEndpoint(), 0);
        } else if (configStore.getConnectionStrings().size() > 0) {
            ConnectionString connectionString = new ConnectionString(configStore.getConnectionStrings().get(0));
            connectionString.setUri(failoverEndpoint);
            ConfigurationClientBuilder builder = createBuilderInstance().connectionString(connectionString.toString());
            return modifyAndBuildClient(builder, failoverEndpoint, configStore.getEndpoint(), 0);
        } else {
            ConfigurationClientBuilder builder = createBuilderInstance();
            if (!credentialConfigured) {
                builder.credential(new DefaultAzureCredentialBuilder().build());
            }
            builder.endpoint(failoverEndpoint);
            return modifyAndBuildClient(builder, failoverEndpoint, configStore.getEndpoint(), 0);
        }
    }

    private AppConfigurationReplicaClient modifyAndBuildClient(ConfigurationClientBuilder builder, String endpoint, String originEndpoint,
        Integer replicaCount) {
        TracingInfo tracingInfo = new TracingInfo(isKeyVaultConfigured, replicaCount,
            Configuration.getGlobalConfiguration());
        builder.addPolicy(new BaseAppConfigurationPolicy(tracingInfo));

        if (clientCustomizer != null) {
            clientCustomizer.customize(builder, endpoint);
        }
        return new AppConfigurationReplicaClient(endpoint, originEndpoint, builder.buildClient());
    }

    private ConfigurationClientBuilder createBuilderInstance() {
        RetryStrategy retryStatagy = null;

        String mode = System.getProperty(AzureGlobalProperties.PREFIX + "." + RETRY_MODE_PROPERTY_NAME);
        String modeService = System
            .getProperty(AzureAppConfigurationProperties.PREFIX + "." + RETRY_MODE_PROPERTY_NAME);

        if ("exponential".equals(mode) || "exponential".equals(modeService) || (mode == null && modeService == null)) {
            Function<String, Integer> checkPropertyInt = parameter -> (Integer.parseInt(parameter));
            Function<String, Duration> checkPropertyDuration = parameter -> (DurationStyle.detectAndParse(parameter));

            int retries = checkProperty(MAX_RETRIES_PROPERTY_NAME, defaultMaxRetries,
                " isn't a valid integer, using default value.", checkPropertyInt);

            Duration baseDelay = checkProperty(BASE_DELAY_PROPERTY_NAME, DEFAULT_MIN_RETRY_POLICY,
                " isn't a valid Duration, using default value.", checkPropertyDuration);
            Duration maxDelay = checkProperty(MAX_DELAY_PROPERTY_NAME, DEFAULT_MAX_RETRY_POLICY,
                " isn't a valid Duration, using default value.", checkPropertyDuration);

            retryStatagy = new ExponentialBackoff(retries, baseDelay, maxDelay);
        }

        ConfigurationClientBuilder builder = clientFactory.build();

        if (retryStatagy != null) {
            builder.retryPolicy(new RetryPolicy(retryStatagy));
        }

        return builder;
    }

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

    private static class ConnectionString {
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
            URL baseUri = null;
            String id = null;
            String secret = null;
            for (String arg : args) {
                String segment = arg.trim();
                if (ENDPOINT.regionMatches(true, 0, segment, 0, ENDPOINT.length())) {
                    try {
                        baseUri = new URI(segment.substring(ENDPOINT.length())).toURL();
                    } catch (MalformedURLException ex) {
                        throw new IllegalArgumentException(ex);
                    } catch (URISyntaxException ex) {
                        throw new IllegalArgumentException(ex);
                    }
                } else if (ID.regionMatches(true, 0, segment, 0, ID.length())) {
                    id = segment.substring(ID.length());
                } else if (SECRET_PREFIX.regionMatches(true, 0, segment, 0, SECRET_PREFIX.length())) {
                    secret = segment.substring(SECRET_PREFIX.length());
                }
            }
            this.baseUri = baseUri;
            this.id = id;
            this.secret = secret;
            if (this.baseUri == null || CoreUtils.isNullOrEmpty(this.id)
                || this.secret == null || this.secret.length() == 0) {
                throw new IllegalArgumentException("Could not parse 'connectionString'."
                    + " Expected format: 'endpoint={endpoint};id={id};secret={secret}'. Actual:" + connectionString);
            }
        }

        protected ConnectionString setUri(String uri) {
            try {
                this.baseUri = new URI(uri).toURL();
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            } catch (URISyntaxException ex) {
                throw new IllegalArgumentException(ex);
            }
            return this;
        }

        public String toString() {
            return String.format("%s%s;%s%s;%s%s", ENDPOINT, baseUri, ID, id, SECRET_PREFIX, secret);
        }
    }
}

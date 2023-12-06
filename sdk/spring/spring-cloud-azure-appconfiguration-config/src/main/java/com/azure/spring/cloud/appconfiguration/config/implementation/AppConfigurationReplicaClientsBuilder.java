// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.config.Profiles;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.cloud.appconfiguration.config.ConfigurationClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.implementation.http.policy.BaseAppConfigurationPolicy;
import com.azure.spring.cloud.appconfiguration.config.implementation.http.policy.TracingInfo;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;

public class AppConfigurationReplicaClientsBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationReplicaClientsBuilder.class);

    /**
     * Invalid Connection String error message
     */
    public static final String NON_EMPTY_MSG = "%s property should not be null or empty in the connection string of Azure Config Service.";

    public static final String RETRY_MODE_PROPERTY_NAME = "retry.mode";

    public static final String MAX_RETRIES_PROPERTY_NAME = "retry.exponential.max-retries";

    public static final String BASE_DELAY_PROPERTY_NAME = "retry.exponential.base-delay";

    public static final String MAX_DELAY_PROPERTY_NAME = "retry.exponential.max-delay";

    private static final Duration DEFAULT_MIN_RETRY_POLICY = Duration.ofMillis(800);

    private static final Duration DEFAULT_MAX_RETRY_POLICY = Duration.ofSeconds(8);

    /**
     * Connection String Regex format
     */
    private static final String CONN_STRING_REGEXP = "Endpoint=([^;]+);Id=([^;]+);Secret=([^;]+)";

    /**
     * Invalid Formatted Connection String Error message
     */
    public static final String ENDPOINT_ERR_MSG = String.format("Connection string does not follow format %s.",
        CONN_STRING_REGEXP);

    private static final Pattern CONN_STRING_PATTERN = Pattern.compile(CONN_STRING_REGEXP);

    private ConfigurationClientCustomizer clientProvider;

    private boolean isDev = false;

    private boolean isKeyVaultConfigured = false;

    private final int defaultMaxRetries;

    public AppConfigurationReplicaClientsBuilder(int defaultMaxRetries) {
        this.defaultMaxRetries = defaultMaxRetries;
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
     * @param clientProvider the clientProvider to set
     */
    public void setClientProvider(ConfigurationClientCustomizer clientProvider) {
        this.clientProvider = clientProvider;
    }

    public void setIsKeyVaultConfigured(boolean isKeyVaultConfigured) {
        this.isKeyVaultConfigured = isKeyVaultConfigured;
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
                "More than 1 Connection method was set for connecting to App Configuration.");
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
                String endpoint = getEndpointFromConnectionString(connectionString);
                LOGGER.debug("Connecting to " + endpoint + " using Connecting String.");
                ConfigurationClientBuilder builder = createBuilderInstance().connectionString(connectionString);

                clients.add(modifyAndBuildClient(builder, endpoint, connectionStrings.size() - 1));
            }
            return clients;
        }
        for (String endpoint : endpoints) {
            ConfigurationClientBuilder builder = this.createBuilderInstance();

            // By default we just use a
            builder.credential(new DefaultAzureCredentialBuilder().build());

            builder.endpoint(endpoint);

            clients.add(modifyAndBuildClient(builder, endpoint, endpoints.size() - 1));
        }

        return clients;
    }

    private AppConfigurationReplicaClient modifyAndBuildClient(ConfigurationClientBuilder builder, String endpoint,
        Integer replicaCount) {
        TracingInfo tracingInfo = new TracingInfo(isDev, isKeyVaultConfigured, replicaCount,
            Configuration.getGlobalConfiguration());
        builder.addPolicy(new BaseAppConfigurationPolicy(tracingInfo));

        if (clientProvider != null) {
            clientProvider.customize(builder, endpoint);
        }
        return new AppConfigurationReplicaClient(endpoint, builder.buildClient(), tracingInfo);
    }

    public void setProfiles(Profiles profiles) {
        for (String profile : profiles.getActive()) {
            if ("dev".equalsIgnoreCase(profile)) {
                this.isDev = true;
                break;
            }
        }
    }

    protected ConfigurationClientBuilder createBuilderInstance() {
        RetryStrategy retryStatagy = new ExponentialBackoff(defaultMaxRetries, DEFAULT_MIN_RETRY_POLICY,
            DEFAULT_MAX_RETRY_POLICY);

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();

        if (retryStatagy != null) {
            builder.retryPolicy(new RetryPolicy(retryStatagy));
        }

        return builder;
    }

}

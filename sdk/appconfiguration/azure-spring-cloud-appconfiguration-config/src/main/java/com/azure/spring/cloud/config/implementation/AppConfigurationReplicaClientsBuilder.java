// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.spring.cloud.config.AppConfigurationCredentialProvider;
import com.azure.spring.cloud.config.ConfigurationClientBuilderSetup;
import com.azure.spring.cloud.config.pipline.policies.BaseAppConfigurationPolicy;
import com.azure.spring.cloud.config.properties.ConfigStore;

public class AppConfigurationReplicaClientsBuilder implements EnvironmentAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationReplicaClientsBuilder.class);

    /**
     * Invalid Connection String error message
     */
    public static final String NON_EMPTY_MSG = "%s property should not be null or empty in the connection string of Azure Config Service.";

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

    private AppConfigurationCredentialProvider tokenCredentialProvider;

    private ConfigurationClientBuilderSetup clientProvider;

    private boolean isDev = false;

    private boolean isKeyVaultConfigured = false;

    private String clientId = "";

    private final int maxRetries;

    public AppConfigurationReplicaClientsBuilder(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    /**
     * @param tokenCredentialProvider the tokenCredentialProvider to set
     */
    public void setTokenCredentialProvider(AppConfigurationCredentialProvider tokenCredentialProvider) {
        this.tokenCredentialProvider = tokenCredentialProvider;
    }

    /**
     * @param clientProvider the clientProvider to set
     */
    public void setClientProvider(ConfigurationClientBuilderSetup clientProvider) {
        this.clientProvider = clientProvider;
    }

    /**
     * @param isKeyVaultConfigured the isKeyVaultConfigured to set
     */
    public void setKeyVaultConfigured(boolean isKeyVaultConfigured) {
        this.isKeyVaultConfigured = isKeyVaultConfigured;
    }

    /**
     * @param clientId the clientId to set
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Given a connection string, returns the endpoint value inside of it.
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

        TokenCredential tokenCredential = null;

        if (tokenCredentialProvider != null) {
            tokenCredential = tokenCredentialProvider.getAppConfigCredential(configStore.getEndpoint());
        }

        boolean clientIdIsPresent = StringUtils.hasText(clientId);
        boolean tokenCredentialIsPresent = tokenCredential != null;
        boolean connectionStringIsPresent = configStore.getConnectionString() != null;

        if ((tokenCredentialIsPresent || clientIdIsPresent)
            && connectionStringIsPresent) {
            throw new IllegalArgumentException(
                "More than 1 Connection method was set for connecting to App Configuration.");
        } else if (tokenCredential != null && clientIdIsPresent) {
            throw new IllegalArgumentException(
                "More than 1 Connection method was set for connecting to App Configuration.");
        }

        ConfigurationClientBuilder builder = getBuilder();

        if (configStore.getConnectionString() != null) {
            clients.add(buildClientConnectionString(configStore.getConnectionString(), builder, 0));
        } else if (configStore.getConnectionStrings().size() > 0) {
            for (String connectionString : configStore.getConnectionStrings()) {
                clients.add(buildClientConnectionString(connectionString, builder,
                    configStore.getConnectionStrings().size() - 1));
            }
        } else if (configStore.getEndpoints().size() > 0) {
            for (String endpoint : configStore.getEndpoints()) {
                clients.add(buildClientEndpoint(tokenCredential, endpoint, builder, clientIdIsPresent,
                    configStore.getEndpoints().size() - 1));
            }
        } else if (configStore.getEndpoint() != null) {
            clients.add(buildClientEndpoint(tokenCredential, configStore.getEndpoint(), builder, clientIdIsPresent, 0));
        }
        return clients;
    }

    /**
     * @return creates an instance of ConfigurationClientBuilder
     */
    ConfigurationClientBuilder getBuilder() {
        return new ConfigurationClientBuilder();
    }

    private AppConfigurationReplicaClient buildClientEndpoint(TokenCredential tokenCredential,
        String endpoint, ConfigurationClientBuilder builder, boolean clientIdIsPresent, Integer replicaCount)
        throws IllegalArgumentException {
        if (tokenCredential != null) {
            // User Provided Token Credential
            LOGGER.debug("Connecting to " + endpoint + " using AppConfigurationCredentialProvider.");
            builder.credential(tokenCredential);
        } else if (clientIdIsPresent) {
            // User Assigned Identity - Client ID through configuration file.
            LOGGER.debug("Connecting to " + endpoint + " using Client ID from configuration file.");
            ManagedIdentityCredentialBuilder micBuilder = new ManagedIdentityCredentialBuilder()
                .clientId(clientId);
            builder.credential(micBuilder.build());
        } else {
            // System Assigned Identity. Needs to be checked last as all of the above should have an Endpoint.
            LOGGER.debug("Connecting to " + endpoint
                + " using Azure System Assigned Identity or Azure User Assigned Identity.");
            ManagedIdentityCredentialBuilder micBuilder = new ManagedIdentityCredentialBuilder();
            builder.credential(micBuilder.build());
        }

        builder.endpoint(endpoint);

        return modifyAndBuildClient(builder, endpoint, replicaCount);
    }

    private AppConfigurationReplicaClient buildClientConnectionString(String connectionString,
        ConfigurationClientBuilder builder, Integer replicaCount)
        throws IllegalArgumentException {
        String endpoint = getEndpointFromConnectionString(connectionString);
        LOGGER.debug("Connecting to " + endpoint + " using Connecting String.");

        builder.connectionString(connectionString);

        return modifyAndBuildClient(builder, endpoint, replicaCount);
    }

    private AppConfigurationReplicaClient modifyAndBuildClient(ConfigurationClientBuilder builder, String endpoint,
        Integer replicaCount) {
        ExponentialBackoff retryPolicy = new ExponentialBackoff(maxRetries, DEFAULT_MIN_RETRY_POLICY,
            DEFAULT_MAX_RETRY_POLICY);

        builder.addPolicy(new BaseAppConfigurationPolicy(isDev, isKeyVaultConfigured, replicaCount))
            .retryPolicy(new RetryPolicy(retryPolicy));

        if (clientProvider != null) {
            clientProvider.setup(builder, endpoint);
        }

        return new AppConfigurationReplicaClient(endpoint, builder.buildClient());
    }

    @Override
    public void setEnvironment(Environment environment) {
        for (String profile : environment.getActiveProfiles()) {
            if ("dev".equalsIgnoreCase(profile)) {
                this.isDev = true;
                break;
            }
        }
    }
}

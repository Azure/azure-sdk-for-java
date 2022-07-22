// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.spring.cloud.config.AppConfigurationCredentialProvider;
import com.azure.spring.cloud.config.ConfigurationClientBuilderSetup;
import com.azure.spring.cloud.config.health.AppConfigurationStoreHealth;
import com.azure.spring.cloud.config.pipline.policies.BaseAppConfigurationPolicy;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.ConfigStore;

/**
 * Holds a set of connections to an app configuration store with zero to many geo-replications.
 */
public class ConnectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

    /**
     * Invalid Connection String error message
     */
    public static final String NON_EMPTY_MSG = "%s property should not be null or empty in the connection string of Azure Config Service.";

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

    private final AppConfigurationProviderProperties appProperties;

    private final AppConfigurationCredentialProvider tokenCredentialProvider;

    private final ConfigurationClientBuilderSetup clientProvider;

    private final boolean isDev;

    private final boolean isKeyVaultConfigurated;

    private final String clientId;

    private final ConfigStore configStore;

    private final String originEndpoint;

    // Used if only one connection method is given.
    private ConfigurationClientWrapper client;

    // Used if multiple connection method is given.
    private List<ConfigurationClientWrapper> clients;

    private String currentReplica;

    private AppConfigurationStoreHealth health;

    /**
     * Creates a set of connections to a app configuration store.
     * @param configStore Connection info for the store
     * @param appProperties Properties for setting up the connection
     * @param tokenCredentialProvider optional provider for token credentials
     * @param clientProvider optional provider for modifying the client
     * @param isDev Is a dev machine
     * @param isKeyVaultConfigured is key vault configured
     * @param clientId Client Id for Managed Identity
     */
    ConnectionManager(ConfigStore configStore, AppConfigurationProviderProperties appProperties,
        AppConfigurationCredentialProvider tokenCredentialProvider,
        ConfigurationClientBuilderSetup clientProvider, Boolean isDev, Boolean isKeyVaultConfigured, String clientId) {
        this.configStore = configStore;
        this.appProperties = appProperties;
        this.tokenCredentialProvider = tokenCredentialProvider;
        this.clientProvider = clientProvider;
        this.isDev = isDev;
        this.isKeyVaultConfigurated = isKeyVaultConfigured;
        this.clientId = clientId;

        this.originEndpoint = configStore.getEndpoint();
        this.health = AppConfigurationStoreHealth.NOT_LOADED;
        this.currentReplica = configStore.getEndpoint();
    }

    /**
     * Given a connection string, returns the endpoint inside of it.
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
     * @return creates an instance of ConfigurationClientBuilder
     */
    ConfigurationClientBuilder getBuilder() {
        return new ConfigurationClientBuilder();
    }

    /**
     * Gets the current health information on the Connection to the Config Store
     * @return AppConfigurationConfigStoreHealth
     */
    AppConfigurationStoreHealth getHealth() {
        return this.health;
    }

    /**
     * Returns a client.
     * @return ConfiguraitonClient
     */
    String getCurrentClient() {
        String currentClientEndpoint = currentReplica;

        // This should always be reset after use. Any refresh event could trigger a check. Only ones that go through our
        // code shouldn't try all replicas.
        this.currentReplica = configStore.getEndpoint();

        return currentClientEndpoint;
    }

    void setCurrentClient(String replicaEndpoint) {
        this.currentReplica = replicaEndpoint;
    }

    /**
     * @return the originEndpoint
     */
    String getOriginEndpoint() {
        return originEndpoint;
    }

    /**
     * Returns a client.
     * @return ConfiguraitonClient
     */
    List<ConfigurationClientWrapper> getAvalibleClients() {
        if (client == null && clients == null) {
            buildClients();
            if (client == null && clients == null) {
                this.health = AppConfigurationStoreHealth.NOT_LOADED;
            }
        }

        List<ConfigurationClientWrapper> avalibleClients = new ArrayList<>();

        if (client != null) {
            avalibleClients.add(client);
        } else if (clients != null) {
            for (ConfigurationClientWrapper wrapper : clients) {
                if (wrapper.getBackoffEndTime().isBefore(Instant.now())) {
                    LOGGER.debug("Using Client: " + wrapper.getEndpoint());
                    avalibleClients.add(wrapper);
                }
            }
            if (avalibleClients.size() == 0) {
                this.health = AppConfigurationStoreHealth.DOWN;
            }
        }

        return avalibleClients;
    }

    List<String> getAllEndpoints() {
        if (client != null) {
            return Arrays.asList(client.getEndpoint());
        }
        return clients.stream().map(client -> client.getEndpoint()).collect(Collectors.toList());
    }

    /**
     * Call when the current client failed
     * @param endpoint replica endpoint
     */
    void backoffClient(String endpoint) {
        clients.stream().filter(client -> endpoint.equals(client.getEndpoint())).map(client -> {
            int failedAttempt = client.getFailedAttempts();
            long backoffTime = BackoffTimeCalculator.calculateBackoff(failedAttempt,
                appProperties.getDefaultMaxBackoff(),
                appProperties.getDefaultMinBackoff());
            client.updateBackoffEndTime(Instant.now().plusNanos(backoffTime));
            client.setHealth(AppConfigurationStoreHealth.DOWN);
            return client;
        });
    }

    /**
     * Builds all the clients for a connection.
     * @throws IllegalArgumentException when more than 1 connection method is given.
     */
    private void buildClients() {
        // Single client or Multiple?
        // If single call buildClient
        int hasSingleConnectionString = StringUtils.hasText(configStore.getConnectionString()) ? 1 : 0;
        int hasMultiEndpoints = configStore.getEndpoints().size() > 0 ? 1 : 0;
        int hasMultiConnectionString = configStore.getConnectionStrings().size() > 0 ? 1 : 0;

        if (hasSingleConnectionString + hasMultiEndpoints + hasMultiConnectionString > 1) {
            throw new IllegalArgumentException(
                "More than 1 Conncetion method was set for connecting to App Configuration.");
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
                "More than 1 Conncetion method was set for connecting to App Configuration.");
        } else if (tokenCredential != null && clientIdIsPresent) {
            throw new IllegalArgumentException(
                "More than 1 Conncetion method was set for connecting to App Configuration.");
        }

        ConfigurationClientBuilder builder = getBuilder();
        clients = new ArrayList<>();

        if (configStore.getConnectionString() != null) {
            client = buildClientConnectionString(configStore.getConnectionString(), builder);
        } else if (configStore.getConnectionStrings().size() > 0) {
            for (String connectionString : configStore.getConnectionStrings()) {
                clients.add(buildClientConnectionString(connectionString, builder));
            }
        } else if (configStore.getEndpoints().size() > 0) {
            for (String endpoint : configStore.getEndpoints()) {
                clients.add(buildClientEndpoint(tokenCredential, endpoint, builder, clientIdIsPresent));
            }
        } else if (configStore.getEndpoint() != null) {
            client = buildClientEndpoint(tokenCredential, configStore.getEndpoint(), builder, clientIdIsPresent);
        }
    }

    private ConfigurationClientWrapper buildClientEndpoint(TokenCredential tokenCredential,
        String endpoint, ConfigurationClientBuilder builder, boolean clientIdIsPresent)
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
            // System Assigned Identity. Needs to be checked last as all of the above should have a Endpoint.
            LOGGER.debug("Connecting to " + endpoint
                + " using Azure System Assigned Identity or Azure User Assigned Identity.");
            ManagedIdentityCredentialBuilder micBuilder = new ManagedIdentityCredentialBuilder();
            builder.credential(micBuilder.build());
        }

        builder.endpoint(endpoint);

        return modifyAndBuildClient(builder, endpoint);
    }

    private ConfigurationClientWrapper buildClientConnectionString(String connectionString,
        ConfigurationClientBuilder builder)
        throws IllegalArgumentException {
        String endpoint = getEndpointFromConnectionString(connectionString);
        LOGGER.debug("Connecting to " + endpoint + " using Connecting String.");

        builder.connectionString(connectionString);

        return modifyAndBuildClient(builder, endpoint);
    }

    private ConfigurationClientWrapper modifyAndBuildClient(ConfigurationClientBuilder builder, String endpoint) {
        ExponentialBackoff retryPolicy = new ExponentialBackoff(appProperties.getMaxRetries(),
            Duration.ofMillis(800), Duration.ofSeconds(8));

        builder.addPolicy(new BaseAppConfigurationPolicy(isDev, isKeyVaultConfigurated))
            .retryPolicy(new RetryPolicy(retryPolicy));

        if (clientProvider != null) {
            clientProvider.setup(builder, endpoint);
        }

        return new ConfigurationClientWrapper(endpoint, builder.buildClient());
    }

}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.spring.cloud.appconfiguration.config.ConfigurationClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.implementation.http.policy.BaseAppConfigurationPolicy;
import com.azure.spring.cloud.appconfiguration.config.implementation.http.policy.TracingInfo;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.cloud.core.service.AzureServiceType.AppConfiguration;
import com.azure.spring.cloud.service.implementation.appconfiguration.ConfigurationClientBuilderFactory;

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

    private final ConfigurationClientBuilderFactory clientFactory;

    private boolean isDev = false;

    private boolean isKeyVaultConfigured = false;

    private final boolean credentialConfigured;

    public AppConfigurationReplicaClientsBuilder(ConfigurationClientBuilderFactory clientFactory, boolean credentialConfigured) {
        this.clientFactory = clientFactory;
        this.credentialConfigured = credentialConfigured;
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

                clients.add(modifyAndBuildClient(builder, endpoint, connectionStrings.size() - 1));
            }
        } else {
            for (String endpoint : endpoints) {
                ConfigurationClientBuilder builder = this.createBuilderInstance();

                builder.credential(new DefaultAzureCredentialBuilder().build());

                builder.endpoint(endpoint);

                clients.add(modifyAndBuildClient(builder, endpoint, endpoints.size() - 1));
            }
        }
        return clients;
    }

    public AppConfigurationReplicaClient buildClient(String failoverEndpoint, ConfigStore configStore) {

        if (StringUtils.hasText(configStore.getConnectionString())) {
            ConnectionString connectionString = new ConnectionString(configStore.getConnectionString());
            connectionString.setUri(failoverEndpoint);
            ConfigurationClientBuilder builder = createBuilderInstance().connectionString(connectionString.toString());
            return modifyAndBuildClient(builder, failoverEndpoint, 0);
        } else if (configStore.getConnectionStrings().size() > 0) {
            ConnectionString connectionString = new ConnectionString(configStore.getConnectionStrings().get(0));
            connectionString.setUri(failoverEndpoint);
            ConfigurationClientBuilder builder = createBuilderInstance().connectionString(connectionString.toString());
            return modifyAndBuildClient(builder, failoverEndpoint, 0);
        } else {
            ConfigurationClientBuilder builder = createBuilderInstance();
            builder.credential(new DefaultAzureCredentialBuilder().build());

            builder.endpoint(failoverEndpoint);
            return modifyAndBuildClient(builder, failoverEndpoint, 0);
        }
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

    protected ConfigurationClientBuilder createBuilderInstance() {

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();

        return builder;
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
                        baseUri = new URL(segment.substring(ENDPOINT.length()));
                    } catch (MalformedURLException ex) {
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
                this.baseUri = new URL(uri);
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            }
            return this;
        }

        public String toString() {
            return String.format("%s%s;%s%s;%s%s", ENDPOINT, baseUri, ID, id, SECRET_PREFIX, secret);
        }
    }
}

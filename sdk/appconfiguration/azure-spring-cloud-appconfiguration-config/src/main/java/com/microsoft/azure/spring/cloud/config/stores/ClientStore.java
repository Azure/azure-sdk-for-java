// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config.stores;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.PagedResponse;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.microsoft.azure.spring.cloud.config.AppConfigurationCredentialProvider;
import com.microsoft.azure.spring.cloud.config.ConfigurationClientBuilderSetup;
import com.microsoft.azure.spring.cloud.config.pipline.policies.BaseAppConfigurationPolicy;
import com.microsoft.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.microsoft.azure.spring.cloud.config.resource.Connection;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionPool;

public class ClientStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientStore.class);

    private AppConfigurationProviderProperties appProperties;

    private ConnectionPool pool;

    private AppConfigurationCredentialProvider tokenCredentialProvider;

    private ConfigurationClientBuilderSetup clientProvider;

    private HashMap<String, ConfigurationAsyncClient> clients;

    public ClientStore(AppConfigurationProviderProperties appProperties, ConnectionPool pool,
        AppConfigurationCredentialProvider tokenCredentialProvider,
        ConfigurationClientBuilderSetup clientProvider) {
        this.appProperties = appProperties;
        this.pool = pool;
        this.tokenCredentialProvider = tokenCredentialProvider;
        this.clientProvider = clientProvider;
        this.clients = new HashMap<String, ConfigurationAsyncClient>();
    }

    private ConfigurationAsyncClient getClient(String store) throws IllegalArgumentException {
        if (clients.containsKey(store)) {
            return clients.get(store);
        }
        ExponentialBackoff retryPolicy = new ExponentialBackoff(appProperties.getMaxRetries(),
            Duration.ofMillis(800), Duration.ofSeconds(8));
        ConfigurationClientBuilder builder = getBuilder()
            .addPolicy(new BaseAppConfigurationPolicy())
            .retryPolicy(new RetryPolicy(retryPolicy));

        TokenCredential tokenCredential = null;
        Connection connection = pool.get(store);

        String endpoint = Optional.ofNullable(connection)
            .map(Connection::getEndpoint)
            .orElse(null);

        if (tokenCredentialProvider != null) {
            tokenCredential = tokenCredentialProvider.getAppConfigCredential(endpoint);
        }

        String clientId = Optional.ofNullable(connection)
            .map(Connection::getClientId)
            .orElse(null);
        boolean clientIdIsPresent = StringUtils.isNotEmpty(clientId);
        boolean tokenCredentialIsPresent = tokenCredential != null;
        boolean connectionStringIsPresent = Optional.ofNullable(connection)
            .map(Connection::getConnectionString)
            .filter(StringUtils::isNotEmpty)
            .isPresent();
        boolean endPointIsPresent = Optional.ofNullable(connection)
            .map(Connection::getEndpoint)
            .filter(StringUtils::isNotEmpty)
            .isPresent();
        if ((tokenCredentialIsPresent || clientIdIsPresent)
            && connectionStringIsPresent) {
            throw new IllegalArgumentException(
                "More than 1 Conncetion method was set for connecting to App Configuration.");
        } else if (tokenCredential != null && clientIdIsPresent) {
            throw new IllegalArgumentException(
                "More than 1 Conncetion method was set for connecting to App Configuration.");
        }

        if (tokenCredential != null) {
            // User Provided Token Credential
            LOGGER.debug("Connecting to " + endpoint + " using AppConfigurationCredentialProvider.");
            builder.credential(tokenCredential);
        } else if (clientIdIsPresent && endPointIsPresent) {
            // User Assigned Identity - Client ID through configuration file.
            LOGGER.debug("Connecting to " + endpoint + " using Client ID from configuration file.");
            ManagedIdentityCredentialBuilder micBuilder = new ManagedIdentityCredentialBuilder()
                .clientId(clientId);
            builder.credential(micBuilder.build());
        } else if (connectionStringIsPresent) {
            // Connection String
            LOGGER.debug("Connecting to " + endpoint + " using Connecting String.");
            builder.connectionString(connection.getConnectionString());
        } else if (endPointIsPresent) {
            // System Assigned Identity. Needs to be checked last as all of the above should have a Endpoint.
            LOGGER.debug("Connecting to " + endpoint
                + " using Azure System Assigned Identity or Azure User Assigned Identity.");
            ManagedIdentityCredentialBuilder micBuilder = new ManagedIdentityCredentialBuilder();
            builder.credential(micBuilder.build());
        } else {
            throw new IllegalArgumentException("No Configuration method was set for connecting to App Configuration");
        }

        builder.endpoint(endpoint);

        if (clientProvider != null) {
            clientProvider.setup(builder, endpoint);
        }

        clients.put(store, builder.buildAsyncClient());
        return clients.get(store);
    }

    /**
     * Gets the latest Configuration Setting from the revisions given config store that match the Setting Selector
     * criteria.
     *
     * @param settingSelector Information on which setting to pull. i.e. number of results, key value...
     * @param storeName Name of the App Configuration store to query against.
     * @return List of Configuration Settings.
     */
    public final ConfigurationSetting getRevison(SettingSelector settingSelector, String storeName) {
        PagedResponse<ConfigurationSetting> configurationRevision = null;
        int retryCount = 0;

        ConfigurationAsyncClient client = getClient(storeName);
        while (retryCount <= appProperties.getMaxRetries()) {
            configurationRevision = client.listRevisions(settingSelector).byPage().blockFirst();

            if (configurationRevision != null
                && configurationRevision.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                HttpHeader retryAfterHeader = configurationRevision.getHeaders().get("retry-after-ms");

                if (retryAfterHeader != null) {
                    try {
                        Integer retryAfter = Integer.valueOf(retryAfterHeader.getValue());

                        Thread.sleep(retryAfter);
                    } catch (NumberFormatException e) {
                        LOGGER.warn("Unable to parse Retry After value.", e);
                    } catch (InterruptedException e) {
                        LOGGER.warn("Failed to wait after getting 429.", e);
                    }
                }

                configurationRevision = null;
            } else if (configurationRevision != null && configurationRevision.getItems().size() > 0) {
                return configurationRevision.getItems().get(0);
            } else {
                return null;
            }
            retryCount++;
        }
        return null;
    }

    /**
     * Gets a list of Configuration Settings from the given config store that match the Setting Selector criteria.
     *
     * @param settingSelector Information on which setting to pull. i.e. number of results, key value...
     * @param storeName Name of the App Configuration store to query against.
     * @return List of Configuration Settings.
     */
    public final List<ConfigurationSetting> listSettings(SettingSelector settingSelector, String storeName) {
        ConfigurationAsyncClient client = getClient(storeName);

        return client.listConfigurationSettings(settingSelector).collectList().block();
    }

    ConfigurationClientBuilder getBuilder() {
        return new ConfigurationClientBuilder();
    }
}

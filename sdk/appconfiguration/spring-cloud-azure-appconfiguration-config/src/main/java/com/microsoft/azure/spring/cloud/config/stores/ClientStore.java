/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.stores;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

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
import com.microsoft.azure.spring.cloud.config.AppConfigurationProviderProperties;
import com.microsoft.azure.spring.cloud.config.ConfigurationClientBuilderSetup;
import com.microsoft.azure.spring.cloud.config.pipline.policies.BaseAppConfigurationPolicy;
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

    private ConfigurationAsyncClient buildClient(String store) throws IllegalArgumentException {
        if (clients.containsKey(store)) {
            return clients.get(store);
        }
        ConfigurationClientBuilder builder = getBuilder();
        ExponentialBackoff retryPolicy = new ExponentialBackoff(appProperties.getMaxRetries(),
            Duration.ofMillis(800), Duration.ofSeconds(8));
        builder = builder.addPolicy(new BaseAppConfigurationPolicy()).retryPolicy(new RetryPolicy(
            retryPolicy));

        TokenCredential tokenCredential = null;
        Connection connection = pool.get(store);

        String endpoint = Optional.ofNullable(connection)
            .map(Connection::getEndpoint)
            .orElse(null);

        if (tokenCredentialProvider != null) {
            tokenCredential = tokenCredentialProvider.getAppConfigCredential(endpoint);
        }
        if ((tokenCredential != null
            || (connection.getClientId() != null && StringUtils.isNotEmpty(connection.getClientId())))
            && (connection != null && StringUtils.isNotEmpty(connection.getConnectionString()))) {
            throw new IllegalArgumentException(
                "More than 1 Conncetion method was set for connecting to App Configuration.");
        } else if (tokenCredential != null && connection != null && connection.getClientId() != null
            && StringUtils.isNotEmpty(connection.getClientId())) {
            throw new IllegalArgumentException(
                "More than 1 Conncetion method was set for connecting to App Configuration.");
        }

        if (tokenCredential != null) {
            // User Provided Token Credential
            LOGGER.debug("Connecting to " + endpoint + " using AppConfigurationCredentialProvider.");
            builder.credential(tokenCredential);
        } else if ((connection.getClientId() != null && StringUtils.isNotEmpty(connection.getClientId()))
            && connection.getEndpoint() != null) {
            // User Assigned Identity - Client ID through configuration file.
            LOGGER.debug("Connecting to " + endpoint + " using Client ID from configuration file.");
            ManagedIdentityCredentialBuilder micBuilder = new ManagedIdentityCredentialBuilder()
                .clientId(connection.getClientId());
            builder.credential(micBuilder.build());
        } else if (StringUtils.isNotEmpty(connection.getConnectionString())) {
            // Connection String
            LOGGER.debug("Connecting to " + endpoint + " using Connecting String.");
            builder.connectionString(connection.getConnectionString());
        } else if (connection.getEndpoint() != null) {
            // System Assigned Identity. Needs to be checked last as all of the above
            // should have a Endpoint.
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

        ConfigurationAsyncClient client = buildClient(storeName);
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
     * @throws IOException thrown when failed to retrieve values.
     */
    public final List<ConfigurationSetting> listSettings(SettingSelector settingSelector, String storeName)
        throws IOException {
        ConfigurationAsyncClient client = buildClient(storeName);

        return client.listConfigurationSettings(settingSelector).collectList().block();
    }

    /**
     * Composite watched key names separated by comma, the key names is made up of: prefix, context and key name pattern
     * e.g., prefix: /config, context: /application, watched key: my.watch.key will return:
     * /config/application/my.watch.key
     *
     * The returned watched key will be one key pattern, one or multiple specific keys e.g., 1) * 2) /application/abc*
     * 3) /application/abc 4) /application/abc,xyz
     *
     * @param store the {@code store} for which to composite watched key names
     * @param storeContextsMap map storing store name and List of context key-value pair
     * @return the full name of the key mapping to the configuration store
     */
    public List<String> watchedKeyNames(ConfigStore store, Map<String, List<String>> storeContextsMap) {
        List<String> watchedKeys = new ArrayList<String>();
        String watchedKey = store.getWatchedKey().trim();
        List<String> contexts = storeContextsMap.get(store.getEndpoint());

        for (String context : contexts) {
            String key = genKey(context, watchedKey);
            if (key.contains(",") && key.contains("*")) {
                // Multi keys including one or more key patterns is not supported by API,
                // will
                // watch all keys(*) instead
                key = "*";
            }
            watchedKeys.add(key);
        }

        return watchedKeys;
    }

    public String watchedKeyNames(ConfigStore store, String context) {
        String watchedKey = store.getWatchedKey().trim();
        String watchedKeys = genKey(context, watchedKey);

        if (watchedKeys.contains(",") && watchedKeys.contains("*")) {
            // Multi keys including one or more key patterns is not supported by API, will
            // watch all keys(*) instead
            watchedKeys = "*";
        }

        return watchedKeys;
    }

    private String genKey(@NonNull String context, @Nullable String watchedKey) {
        String trimmedWatchedKey = StringUtils.isNoneEmpty(watchedKey) ? watchedKey.trim() : "*";

        return String.format("%s%s", context, trimmedWatchedKey);
    }

    ConfigurationClientBuilder getBuilder() {
        return new ConfigurationClientBuilder();
    }
}

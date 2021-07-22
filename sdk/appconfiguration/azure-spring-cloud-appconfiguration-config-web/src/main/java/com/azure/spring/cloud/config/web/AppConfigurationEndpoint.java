// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.web;

import com.azure.spring.cloud.config.properties.ConfigStore;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreMonitoring.AccessToken;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreMonitoring.PushNotification;
import com.fasterxml.jackson.databind.JsonNode;

import static com.azure.spring.cloud.config.web.Constants.VALIDATION_TOPIC;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Common class for authenticating refresh requests.
 */
public class AppConfigurationEndpoint {

    private static final String CONFIG_STORE_TOPIC = "configurationstores";

    private final String endpoint;

    private final String store;

    private final List<ConfigStore> configStores;

    private final Map<String, String> allRequestParams;

    /**
     * Base Authentication for refresh endpoints.
     * 
     * @param request Json body of the request
     * @param configStores List of all of the config stores that request could be for
     * @param allRequestParams paramaters to validate the request.
     * @throws IllegalArgumentException Request missing valid topic field.
     */
    public AppConfigurationEndpoint(JsonNode request, List<ConfigStore> configStores,
        Map<String, String> allRequestParams) {
        this.configStores = configStores;
        this.allRequestParams = allRequestParams;

        JsonNode requestTopic = request.findValue(VALIDATION_TOPIC);
        if (requestTopic != null) {
            String topic = requestTopic.asText();
            store = topic.substring(topic.toLowerCase(Locale.ROOT).indexOf(CONFIG_STORE_TOPIC) + CONFIG_STORE_TOPIC.length() + 1);
            endpoint = String.format("https://%s.azconfig.io", store);
        } else {
            throw new IllegalArgumentException("Refresh request missing topic field.");
        }

    }

    public boolean authenticate() {
        for (ConfigStore configStore : configStores) {
            if (configStore.getEndpoint().equals(endpoint)) {
                PushNotification pushNotification = configStore.getMonitoring().getPushNotification();

                // One of these need to be set
                if (!(pushNotification.getPrimaryToken().isValid()
                    || pushNotification.getSecondaryToken().isValid())) {
                    return false;
                }

                if (isTokenMatch(pushNotification.getPrimaryToken())) {
                    return true;
                }
                if (isTokenMatch(pushNotification.getSecondaryToken())) {
                    return true;
                }

            }
        }
        return false;
    }

    private boolean isTokenMatch(AccessToken token) {
        // if token's secret is allowed to be null this will cause NPE as well.
        return token != null && allRequestParams.containsKey(token.getName())
            && token.getSecret().equals(this.allRequestParams.get(token.getName()));

    }

    public boolean triggerRefresh() {
        for (ConfigStore configStore : configStores) {
            if (configStore.getEndpoint().equals(endpoint) && configStore.getMonitoring().isEnabled()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @return the store
     */
    public String getStore() {
        return store;
    }

}

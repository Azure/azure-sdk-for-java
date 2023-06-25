// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.web.implementation;

import static com.azure.spring.cloud.appconfiguration.config.web.implementation.AppConfigurationWebConstants.DATA;
import static com.azure.spring.cloud.appconfiguration.config.web.implementation.AppConfigurationWebConstants.SYNC_TOKEN;
import static com.azure.spring.cloud.appconfiguration.config.web.implementation.AppConfigurationWebConstants.VALIDATION_CODE_KEY;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring.AccessToken;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring.PushNotification;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Common class for authenticating refresh requests.
 */
public class AppConfigurationEndpoint {

    private static final String CONFIG_STORE_SUBJECT = "subject";

    private final URI endpoint;

    private final List<ConfigStore> configStores;

    private final Map<String, String> allRequestParams;

    private final String syncToken;

    private final JsonNode validationResponse;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Base Authentication for refresh endpoints.
     * 
     * @param request Json body of the request
     * @param configStores List of all of the config stores that request could be for
     * @param allRequestParams paramaters to validate the request.
     * @throws IOException Failed to read the Request body or parse it to json
     * @throws IllegalArgumentException Request missing valid topic field.
     */
    public AppConfigurationEndpoint(HttpServletRequest request, List<ConfigStore> configStores,
        Map<String, String> allRequestParams) throws IOException {
        this.configStores = configStores;
        this.allRequestParams = allRequestParams;

        String reference = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

        JsonNode requestBody = OBJECT_MAPPER.readTree(reference);

        JsonNode data = requestBody.findValue(DATA);

        String sToken = null;
        if (data != null) {
            JsonNode syncTokenNode = data.findValue(SYNC_TOKEN);
            if (syncTokenNode != null) {
                sToken = syncTokenNode.asText();
            }
        }

        syncToken = sToken;

        validationResponse = requestBody.findValue(VALIDATION_CODE_KEY);

        JsonNode requestSubject = requestBody.findValue(CONFIG_STORE_SUBJECT);
        if (requestSubject != null) {
            String subject = requestSubject.asText();
            endpoint = URI.create(subject);
        } else {
            throw new IllegalArgumentException("Refresh request missing topic field.");
        }

    }

    /**
     * Checks if the request is from a valid AppConfiguration Store and has a valid Token and Secret.
     * 
     * @return true if a valid connection.
     */
    public boolean authenticate() {
        for (ConfigStore configStore : configStores) {
            if (configStore.containsEndpoint(getEndpoint())) {
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

    /**
     * Checks if the endpoint's store has been configured for refresh.
     * 
     * @return true, if the configured endpoint has monitoring enabled.
     */
    public boolean triggerRefresh() {
        for (ConfigStore configStore : configStores) {
            if (configStore.containsEndpoint(getEndpoint()) && configStore.getMonitoring().isEnabled()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the syncToken from the event grid request
     * @return string value of sync token or null if it doesn't exist.
     */
    public String getSyncToken() {
        return syncToken;
    }

    /**
     * @return the validationResponse
     */
    public JsonNode getValidationResponse() {
        return validationResponse;
    }

    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint.getScheme() + "://" + endpoint.getHost();
    }

}

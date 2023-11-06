// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.MatchConditions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.data.appconfiguration.models.ConfigurationSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConditionalRequestForSettingsPagination {
    // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
    // and navigating to the "Access Keys" page under the "Settings" section.
    public static String connectionString = Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING");

    // Instantiate a client that will be used to call the service.
    public static final ConfigurationClient client = new ConfigurationClientBuilder()
        .connectionString(connectionString)
        .buildClient();

    public static void main(String[] args) {
        // List settings with conditional request:
        // If the Etag in the request header is *, the service will return all settings
        // If the Etag in the request header is matched with the ETag in the service, the service will return the status code "304"
        // If not matched, the service will return status code 200 with the settings in the response body
        // If the pre-condition fails, the service will return status code 412
        List<PagedResponse<ConfigurationSetting>> pagedResponses = client.listConfigurationSettings(null)
            .streamByPage()
            .collect(Collectors.toList());

        List<MatchConditions> matchConditionsCollected;

        // Monitoring if any setting has changed since last time.
        // In this sample, we check if the settings have changed since last time.
        // If the settings are changed, we will re-request the settings with the conditional request.
        // If the settings are not changed, we will stop the while loop and process the latest settings in the response.
        // It is still possible the setting is changed after the while loop. It is up to the user how to handle their monitoring logic.
        while (true) {
            // Reload settings with conditional request
            matchConditionsCollected = collectMatchConditions(pagedResponses);

            try {
                pagedResponses = client.listConfigurationSettings(null, matchConditionsCollected, Context.NONE)
                        .streamByPage()
                        .collect(Collectors.toList());
            } catch (HttpResponseException exception) {
                int statusCode = exception.getResponse().getStatusCode();
                if (statusCode == 304) {
                    System.out.println("The settings are not changed since last time.");
                    break;
                } else if (statusCode == 412) {
                    throw new RuntimeException("Pre-condition failed.");
                } else {
                    // There could be other status codes returned by the service. that can be handled here or just throw an exception.
                    throw new RuntimeException("Unexpected status code: " + statusCode);
                }
            }
        }

        // Process latest settings
        for (int i = 0; i < pagedResponses.size(); i++) {
            final PagedResponse<ConfigurationSetting> pagedResponse = pagedResponses.get(i);
            final List<ConfigurationSetting> value = pagedResponse.getValue();
            System.out.println("Page size: " + value.size());
        }
    }

    private static List<MatchConditions> collectMatchConditions(List<PagedResponse<ConfigurationSetting>> pagedResponses) {
        List<MatchConditions> matchConditionsCollected = new ArrayList<>();

        for (int i = 0; i < pagedResponses.size(); i++) {
            final PagedResponse<ConfigurationSetting> pagedResponse = pagedResponses.get(i);
            final String pagedETagInResponseHeader = pagedResponse.getHeaders().getValue("Etag");
            matchConditionsCollected.add(new MatchConditions().setIfNoneMatch(pagedETagInResponseHeader));
        }
        return matchConditionsCollected;
    }
}


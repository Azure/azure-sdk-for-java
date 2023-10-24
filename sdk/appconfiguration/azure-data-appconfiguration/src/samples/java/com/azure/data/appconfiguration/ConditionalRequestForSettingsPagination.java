// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.http.MatchConditions;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.data.appconfiguration.models.ConfigurationSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConditionalRequestForSettingsPagination {

    public static void main(String[] args) {
// The connection string value can be obtained by going to your App Configuration instance in the Azure portal
// and navigating to "Access Keys" page under the "Settings" section.
String connectionString = Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING");

// Instantiate a client that will be used to call the service.
final ConfigurationClient client = new ConfigurationClientBuilder()
    .connectionString(connectionString)
    .buildClient();

// List settings without conditional request, and collect all etags
List<MatchConditions> matchConditionsCollected = new ArrayList<>();

final List<PagedResponse<ConfigurationSetting>> responses =
    client.listConfigurationSettings(null).streamByPage().collect(Collectors.toList());
for (int i = 0; i < responses.size(); i++) {
    final PagedResponse<ConfigurationSetting> pagedResponse = responses.get(i);
    final String pagedETagInResponseHeader = pagedResponse.getHeaders().getValue("Etag");
    matchConditionsCollected.add(new MatchConditions().setIfNoneMatch(pagedETagInResponseHeader));
}

// List settings with conditional request:
// if the etag in the request header is *, the service will return all settings
// if the etag in the request header is matched with the etag in the service, the service will return status code "304"
// if not matched, the service will return status code 200 with the settings in the response body
// if pre-condition failed, the service will return status code 412

boolean isSettingsOutdated = true;
List<PagedResponse<ConfigurationSetting>> pagedResponses = null;
// Monitoring if anything setting has changed.
while (isSettingsOutdated) {
    // Reload settings with conditional request
    pagedResponses =
        client.listConfigurationSettings(null, matchConditionsCollected, Context.NONE).streamByPage().collect(Collectors.toList());

    for (int i = 0; i < pagedResponses.size(); i++) {
        final PagedResponse<ConfigurationSetting> pagedResponse = pagedResponses.get(i);
        final int statusCode = pagedResponse.getStatusCode();
        if (statusCode == 304) {
            System.out.println("The settings are not changed since last time.");
            isSettingsOutdated = false;
        } else if (statusCode == 200) {
            System.out.println("The settings are changed since last time.");
            isSettingsOutdated = true;
            break;
        } else if (statusCode == 412) {
            throw new RuntimeException("Pre-condition failed.");
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
}

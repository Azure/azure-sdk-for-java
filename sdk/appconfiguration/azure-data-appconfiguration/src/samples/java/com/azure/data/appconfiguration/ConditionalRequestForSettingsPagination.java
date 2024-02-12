// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.http.MatchConditions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;

import java.util.List;
import java.util.stream.Collectors;
/**
 * Sample demonstrates how to use pa.
 */
public class ConditionalRequestForSettingsPagination {
    public static void main(String[] args) {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to the "Access Keys" page under the "Settings" section.
        String connectionString = Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING");

        // Instantiate a client that will be used to call the service.
        ConfigurationClient client = new ConfigurationClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        // list all settings and get their etags
        List<MatchConditions> matchConditionsList = client.listConfigurationSettings(null)
                .streamByPage()
                .collect(Collectors.toList())
                .stream()
                .map(pagedResponse -> new MatchConditions().setIfNoneMatch(pagedResponse.getHeaders().getValue("Etag")))
                .collect(Collectors.toList());

        PagedIterable<ConfigurationSetting> settings = client.listConfigurationSettings(
                new SettingSelector().setMatchConditions(matchConditionsList));

        settings.iterableByPage().forEach(pagedResponse -> {
            int statusCode = pagedResponse.getStatusCode();
            System.out.println("Status code = " + statusCode);
            if (statusCode == 304) {
                System.out.println("Settings have not changed. ");
                String continuationToken = pagedResponse.getContinuationToken();
                String etag = pagedResponse.getHeaders().getValue("ETag");
                System.out.println("Continuation Token: " + continuationToken);
                System.out.println("ETag: " + etag);
            }

            System.out.println("Settings:");
            pagedResponse.getElements().forEach(setting -> {
                System.out.println("Key: " + setting.getKey() + ", Value: " + setting.getValue());
            });
        });
    }
}


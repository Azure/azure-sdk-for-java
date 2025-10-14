// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.data.appconfiguration;

import com.azure.v2.data.appconfiguration.models.ConfigurationSetting;
import com.azure.v2.data.appconfiguration.models.SettingSelector;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMatchConditions;
import io.clientcore.core.http.paging.PagedIterable;
import io.clientcore.core.utils.configuration.Configuration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Sample demonstrates how to list configuration settings by conditional request.
 * If the ETag of the given setting matches the one in the service, then 304 status code (not modified) with null value
 * returned in the response. Otherwise, a setting with new ETag returned, which is the latest setting retrieved from
 * the service.
 */
public class ConditionalRequestForSettingsPagination {
    /**
     * Runs the sample algorithm and demonstrates how to list configuration settings by conditional request.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to the "Access Keys" page under the "Settings" section.
        String connectionString = Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING");

        // Instantiate a client that will be used to call the service.
        ConfigurationClient client = new ConfigurationClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        // list all settings and get their page ETags
        List<HttpMatchConditions> matchConditionsList = client.listConfigurationSettings(null)
                .streamByPage()
                .collect(Collectors.toList())
                .stream()
                .map(pagedResponse -> new HttpMatchConditions().setIfNoneMatch(
                        pagedResponse.getHeaders().getValue(HttpHeaderName.ETAG)))
                .collect(Collectors.toList());

        PagedIterable<ConfigurationSetting> settings = client.listConfigurationSettings(
                new SettingSelector().setMatchConditions(matchConditionsList));

        settings.iterableByPage().forEach(pagedResponse -> {
            int statusCode = pagedResponse.getStatusCode();
            System.out.println("Status code = " + statusCode);
            if (statusCode == 304) {
                System.out.println("Settings have not changed. ");
                String continuationToken = pagedResponse.getContinuationToken();
                System.out.println("Continuation Token: " + continuationToken);
                return;
            }

            System.out.println("At least one setting in the page has changes. Listing all settings in the page:");
            System.out.println("new page ETag: " + pagedResponse.getHeaders().getValue(HttpHeaderName.ETAG));
            pagedResponse.getValue().forEach(setting -> {
                System.out.println("Key: " + setting.getKey() + ", Value: " + setting.getValue());
            });
        });
    }
}


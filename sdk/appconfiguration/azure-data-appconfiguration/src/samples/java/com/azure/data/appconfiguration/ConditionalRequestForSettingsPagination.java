// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.http.MatchConditions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConditionalRequestForSettingsPagination {
    // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
    // and navigating to the "Access Keys" page under the "Settings" section.
    public static String connectionString = Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING");

    // Instantiate a client that will be used to call the service.
    public static final ConfigurationClient client = new ConfigurationClientBuilder()
        .connectionString(connectionString)
        .buildClient();

    public static void main(String[] args) throws MalformedURLException {
        collectUrlMatchConditions();

        String url = client.getEndpoint() + "/kv?api-version=2023-10-01";
        String eTag = "your-page-etag";
        monitorPageSettings(url, eTag);
    }

    private static void monitorPageSettings(String url, String eTag) throws MalformedURLException {
        // Having the URL and eTag values, we can now make a conditional request to the service.
        PagedIterable<ConfigurationSetting> settings = client.listConfigurationSettings(
                new SettingSelector()
                        .setPageLink(new URL(url))
                        .setMatchConditions(new MatchConditions().setIfNoneMatch(eTag)));

        // Status code = 200 means that the requested settings have changes, so we can print them.
        settings.iterableByPage().forEach(pagedResponse -> {
            System.out.println("Status code = " + pagedResponse.getStatusCode());
            System.out.println("Settings:");
            pagedResponse.getElements().forEach(setting -> {
                System.out.println("Key: " + setting.getKey() + ", Value: " + setting.getValue());
            });
        });
    }

    // This method is used to collect the URL and eTag values from the paged responses.
    private static Map<URL, MatchConditions> collectUrlMatchConditions() {
        // list all settings
        List<PagedResponse<ConfigurationSetting>> pagedResponses = client.listConfigurationSettings(null)
            .streamByPage()
            .collect(Collectors.toList());

        // collect the URL and eTag values from the paged responses
        Map<URL, MatchConditions> urlMatchConditionsMap = new HashMap<>();
        for (int i = 0; i < pagedResponses.size(); i++) {
            final PagedResponse<ConfigurationSetting> currentPagedResponse = pagedResponses.get(i);
            URL currentPageLink = currentPagedResponse.getRequest().getUrl();
            MatchConditions currentETag = new MatchConditions().setIfNoneMatch(currentPagedResponse.getHeaders().getValue("Etag"));
            urlMatchConditionsMap.put(currentPageLink, currentETag);
        }

        // print the URL and eTag values
        urlMatchConditionsMap.forEach((url, matchConditions) -> {
            System.out.println("URL = " + url.toString());
            System.out.println("eTag = " + matchConditions.getIfNoneMatch());
        });

        return urlMatchConditionsMap;
    }
}


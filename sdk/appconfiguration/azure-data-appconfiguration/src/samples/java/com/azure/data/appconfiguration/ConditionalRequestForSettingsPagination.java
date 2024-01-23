// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.http.MatchConditions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.data.appconfiguration.models.ConfigurationSetting;

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

    public static void main(String[] args) {
        collectContinuesTokenAndPagedETags();
        String continuesToken = "/kv?api-version=2023-10-01";
        String eTag = "HFRHspwdgpY65Rnm1EuarWuL9mX-5T2q0s7k-Cmlf5g";

        monitorPageSettings(continuesToken, eTag);
    }

    private static void monitorPageSettings(String continuesToken, String etag) {

        PagedIterable<ConfigurationSetting> settings = client.listConfigurationSettings(null,
                new Context("continuesToken", continuesToken).addData("pageETag", etag));
        // Status code = 200 means that the requested settings have changes, so we can print them.
        settings.iterableByPage().forEach(pagedResponse -> {
            System.out.println("Status code = " + pagedResponse.getStatusCode());
            System.out.println("Settings:");
            pagedResponse.getElements().forEach(setting -> {
                System.out.println("Key: " + setting.getKey() + ", Value: " + setting.getValue());
            });
        });

    }

    private static Map<String, MatchConditions> collectContinuesTokenAndPagedETags() {
        // list all settings
        List<PagedResponse<ConfigurationSetting>> pagedResponses = client.listConfigurationSettings(null)
            .streamByPage()
            .collect(Collectors.toList());

        Map<String, MatchConditions> urlMatchConditionsMap = new HashMap<>();

        MatchConditions currentETag;
        String currentPageLink;
        String nextPageLink = "";

        for (int i = 0; i < pagedResponses.size(); i++) {
            final PagedResponse<ConfigurationSetting> currentPagedResponse = pagedResponses.get(i);
            currentETag = new MatchConditions().setIfNoneMatch(currentPagedResponse.getHeaders().getValue("Etag"));

            currentPageLink = nextPageLink;
            nextPageLink = currentPagedResponse.getContinuationToken();

            // First page will have empty continuation token with current page ETag
            // subsequent pages will have next page's link with current page ETag
            // Last page will have empty continuation token with current page ETag
            urlMatchConditionsMap.put(currentPageLink, currentETag);
        }
        return urlMatchConditionsMap;
    }
}


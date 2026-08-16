// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.MatchConditions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample demonstrates how to use HEAD requests to efficiently check for configuration changes.
 * This is useful for polling scenarios where you want to minimize bandwidth usage.
 */
public class CheckConfigurationSettingsForChanges {
    /**
     * Runs the sample algorithm demonstrating HEAD-based change detection.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        // The endpoint can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Overview" page. Looking for the "Endpoint" keyword.
        String endpoint = Configuration.getGlobalConfiguration().get("AZ_CONFIG_ENDPOINT");

        // Instantiate a client that will be used to call the service.
        final ConfigurationClient client = new ConfigurationClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildClient();

        // Create test settings
        final String key = "hello";
        final String value = "world";
        ConfigurationSetting setting = client.setConfigurationSetting(key, null, value);
        System.out.printf("[SetConfigurationSetting] Key: %s, Value: %s%n", setting.getKey(), setting.getValue());

        SettingSelector selector = new SettingSelector().setKeyFilter(key);

        // Perform an initial HEAD request to capture page ETags
        List<String> cachedPageETags = new ArrayList<>();
        PagedIterable<ConfigurationSetting> headResult = client.checkConfigurationSettings(selector);
        for (PagedResponse<ConfigurationSetting> page : headResult.iterableByPage()) {
            String pageETag = page.getHeaders().getValue(HttpHeaderName.ETAG);
            cachedPageETags.add(pageETag);
            System.out.printf("[CheckConfigurationSettings] Captured page ETag: %s%n", pageETag);
        }

        // Check for changes using cached ETags with If-None-Match
        // If no changes occurred, the service returns 304 Not Modified
        List<MatchConditions> matchConditions = new ArrayList<>();
        for (String cachedETag : cachedPageETags) {
            matchConditions.add(new MatchConditions().setIfNoneMatch(cachedETag));
        }

        SettingSelector conditionalSelector = new SettingSelector()
            .setKeyFilter(key)
            .setMatchConditions(matchConditions);

        boolean hasChanges = false;
        PagedIterable<ConfigurationSetting> conditionalResult = client.checkConfigurationSettings(conditionalSelector);
        for (PagedResponse<ConfigurationSetting> page : conditionalResult.iterableByPage()) {
            if (page.getStatusCode() == 304) {
                System.out.println("[CheckConfigurationSettings] No changes detected (304 Not Modified)");
            } else {
                System.out.printf("[CheckConfigurationSettings] Changes detected. New ETag: %s%n",
                    page.getHeaders().getValue(HttpHeaderName.ETAG));
                hasChanges = true;
            }
        }

        // Update the setting to simulate a change
        setting = client.setConfigurationSetting(key, null, "new-value");
        System.out.printf("[SetConfigurationSetting] Updated Key: %s, Value: %s%n", setting.getKey(), setting.getValue());

        // Check again with the same cached ETags - changes should be detected
        conditionalResult = client.checkConfigurationSettings(conditionalSelector);
        for (PagedResponse<ConfigurationSetting> page : conditionalResult.iterableByPage()) {
            if (page.getStatusCode() == 304) {
                System.out.println("[CheckConfigurationSettings] No changes detected (304 Not Modified)");
            } else {
                System.out.printf("[CheckConfigurationSettings] Changes detected. New ETag: %s%n",
                    page.getHeaders().getValue(HttpHeaderName.ETAG));
                hasChanges = true;
            }
        }

        // Fetch full data only if changes were detected
        if (hasChanges) {
            PagedIterable<ConfigurationSetting> fullResult = client.listConfigurationSettings(selector);
            for (ConfigurationSetting retrievedSetting : fullResult) {
                System.out.printf("[ListConfigurationSettings] Key: %s, Value: %s%n",
                    retrievedSetting.getKey(), retrievedSetting.getValue());
            }
        }

        // Clean up
        setting = client.deleteConfigurationSetting(key, null);
        System.out.printf("[DeleteConfigurationSetting] Key: %s, Value: %s%n", setting.getKey(), setting.getValue());
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.data.appconfiguration.models.SettingSelector;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Sample demonstrates how to add, get, list, and delete a feature flag configuration setting.
 */
public class FeatureFlagConfigurationSettingSample {
    /**
     * Runs the sample algorithm and demonstrates how to add, get, list, and delete a feature flag configuration
     * setting.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};secret={secret_value}";
        final ConfigurationClient client = new ConfigurationClientBuilder()
                                               .connectionString(connectionString)
                                               .buildClient();

        // Name of the key to add to the configuration service.
        final String key = "hello";

        System.out.println("Beginning of synchronous sample...");

        FeatureFlagFilter percentageFilter = new FeatureFlagFilter("Microsoft.Percentage")
                                                 .addParameter("Value", 30);
        FeatureFlagConfigurationSetting featureFlagConfigurationSetting =
            new FeatureFlagConfigurationSetting(key, true)
                .setClientFilters(Arrays.asList(percentageFilter));

        // setConfigurationSetting adds or updates a setting to Azure App Configuration store. Alternatively, you can
        // call addConfigurationSetting which only succeeds if the setting does not exist in the store. Or,
        // you can call setConfigurationSetting to update a setting that is already present in the store.
        System.out.println("[Set-FeatureFlagConfigurationSetting]");
        FeatureFlagConfigurationSetting setting =
            (FeatureFlagConfigurationSetting) client.setConfigurationSetting(featureFlagConfigurationSetting);
        printFeatureFlagSetting(setting);

        System.out.println("[Get-FeatureFlagConfigurationSetting]");
        setting = (FeatureFlagConfigurationSetting) client.getConfigurationSetting(setting);
        printFeatureFlagSetting(setting);

        System.out.println("[List-FeatureFlagConfigurationSetting]");
        PagedIterable<ConfigurationSetting> configurationSettings =
            client.listConfigurationSettings(new SettingSelector());
        for (ConfigurationSetting configurationSetting : configurationSettings) {
            if (configurationSetting instanceof FeatureFlagConfigurationSetting) {
                System.out.println("-Listing-FeatureFlagConfigurationSetting");
                printFeatureFlagSetting((FeatureFlagConfigurationSetting) configurationSetting);
            } else {
                System.out.println("-Listing-non-FeatureFlagConfigurationSetting");
                System.out.printf("Key: %s, Value: %s%n", configurationSetting.getKey(),
                    configurationSetting.getValue());
            }
        }

        System.out.println("[Delete-FeatureFlagConfigurationSetting");
        setting = (FeatureFlagConfigurationSetting) client.deleteConfigurationSetting(setting);
        printFeatureFlagSetting(setting);

        System.out.println("End of synchronous sample.");
    }

    private static void printFeatureFlagSetting(FeatureFlagConfigurationSetting setting) {
        System.out.printf("Key: %s, Value: %s%n", setting.getKey(), setting.getValue());
        System.out.printf("\tFeature ID: %s, Content Type: %s%n", setting.getFeatureId(), setting.getContentType());
        final List<FeatureFlagFilter> clientFilters = setting.getClientFilters();
        for (FeatureFlagFilter filter : clientFilters) {
            System.out.printf("\t\tFilter name: %s%n", filter.getName());
            final Map<String, Object> parameters = filter.getParameters();
            System.out.println("\t\t\tParameters:");
            parameters.forEach((k, v) -> System.out.printf("\t\t\t\tKey: %s, Value:%s%n", k, v));
        }
    }
}

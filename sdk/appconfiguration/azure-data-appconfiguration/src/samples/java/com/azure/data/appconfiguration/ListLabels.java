// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.util.Context;
import com.azure.data.appconfiguration.models.LabelSelector;

/**
 * A sample demonstrate how to list labels.
 */
public class ListLabels {
    /**
     * Runs the sample algorithm and demonstrates how to add, get, and delete a configuration setting.
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


        client.setConfigurationSetting("prod:prod1", "prod1", "prod1");
        client.setConfigurationSetting("prod:prod2", "prod2", "prod2");



        // If you want to list all labels in the sources, simply pass selector=null in the request;
        // If you want to list all labels by wildcard, pass wildcard where AppConfig supports, such as "prod*",
        // If you want to list labels by exact match, use the exact label name as the filter.
        LabelSelector selector = new LabelSelector().setLabelFilter("prod1");

        client.listLabels(selector, Context.NONE)
                .forEach(label -> System.out.println("Label name =" + label.getName()));
    }
}

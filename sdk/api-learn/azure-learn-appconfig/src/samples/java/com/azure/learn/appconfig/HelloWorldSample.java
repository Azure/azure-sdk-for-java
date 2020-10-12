// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.learn.appconfig;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.learn.appconfig.models.ConfigurationSetting;

/**
 * A sample for demonstrating App Configuration client library usage.
 */
public class HelloWorldSample {

    /**
     * Main method to run the sample.
     * @param args Input args to the program.
     */
    public static void main(String[] args) {
        // TODO: write sample to create configuration client and get configs
        ConfigurationClient configurationClient = new ConfigurationClientBuilder()
        .credential(new DefaultAzureCredentialBuilder().build())
        .endpoint(System.getenv("API_LEARN_ENDPOINT"))
        .buildClient();

        ConfigurationSetting fontColor = configurationClient.getConfigurationSetting("FontColor");
        ConfigurationSetting greetingText = configurationClient.getConfigurationSetting("GreetingText");
        System.out.println(fontColor.getValue() + greetingText.getValue());
    }
}

package com.test.annotation;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;

/**
 * Test @ServiceMethod annotation usage.
 */
public class AppConfigTestApp {
    public static void main(String[] args) {
        final ConfigurationClient configurationClient = new ConfigurationClientBuilder()
            .connectionString("foo")
            .buildClient();

        System.out.println("Setting configuration");
        ConfigurationSetting setting = configurationClient.setConfigurationSetting("key", "label", "value");
        System.out.println("Done: " + setting.getLastModified());

        setting = configurationClient.getConfigurationSetting("key", "label");
        System.out.println("Retrieved setting again, value is " + setting.getValue());
    }
}

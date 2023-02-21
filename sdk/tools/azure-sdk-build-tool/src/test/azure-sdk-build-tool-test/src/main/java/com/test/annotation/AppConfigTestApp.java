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
            .connectionString("Endpoint=https://srnagarappconfig.azconfig.io;Id=pFzC-l1-s0:ZN+D59S5KBG72bSsc2ly;Secret=A8kWyww+wZkiQH3RGTEd76oBBMicIQJpJ+aVqSC9rt0=")
            .buildClient();

        System.out.println("Setting configuration");
        try {
            ConfigurationSetting setting = configurationClient.setConfigurationSetting("key", "label", "value");
            System.out.println("Done: " + setting.getLastModified());
            setting = configurationClient.getConfigurationSetting("key", "label");
            System.out.println("Retrieved setting again, value is " + setting.getValue());
        } catch (Exception exception) {

        }

    }
}

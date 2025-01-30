// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.documentation;

import com.azure.core.http.HttpClient;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.ConfigurationClientTestBase;
import com.azure.data.appconfiguration.ConfigurationServiceVersion;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.data.appconfiguration.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Sample demonstrates how to use AAD token to build a configuration client.
 */
public class AadAuthenticationSample extends ConfigurationClientTestBase {
    /**
     * Sample for how to use AAD token Authentication.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        // The endpoint can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Overview" page. Looking for the "Endpoint" keyword.
        String endpoint = "{endpoint_value}";

        // Default token credential could be obtained from Identity service.
        // It tries to create a valid credential in the following order:
        //      EnvironmentCredential
        //      ManagedIdentityCredential
        //      SharedTokenCacheCredential
        //      Fails if none of the credentials above could be created.
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        final ConfigurationClient client = new ConfigurationClientBuilder()
            .credential(tokenCredential) // AAD authentication
            .endpoint(endpoint)
            .buildClient();

        // Name of the key to add to the configuration service.
        final String key = "hello";
        final String value = "world";

        System.out.println("Beginning of synchronous sample...");

        ConfigurationSetting setting = client.setConfigurationSetting(key, null, value);
        System.out.printf("[SetConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue());

        setting = client.getConfigurationSetting(key, null, null);
        System.out.printf("[GetConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue());

        setting = client.deleteConfigurationSetting(key, null);
        System.out.printf("[DeleteConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue());

        System.out.println("End of synchronous sample.");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void aadAuthenticationSample(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        // This will manage getting the testing "endpoint" and "tokenCredential" for the test.
        ConfigurationClient client = setupBuilder(new ConfigurationClientBuilder(), httpClient, serviceVersion, true)
            .buildClient();

        // Name of the key to add to the configuration service.
        final String key = testResourceNamer.randomName("key", 16);
        final String value = "hello world";

        ConfigurationSetting setting = client.setConfigurationSetting(key, null, value);
        assertEquals(key, setting.getKey());
        assertNull(setting.getLabel());
        assertEquals(value, setting.getValue());

        setting = client.getConfigurationSetting(key, null, null);
        assertEquals(key, setting.getKey());
        assertNull(setting.getLabel());
        assertEquals(value, setting.getValue());

        setting = client.deleteConfigurationSetting(key, null);
        assertEquals(key, setting.getKey());
        assertNull(setting.getLabel());
        assertEquals(value, setting.getValue());
    }
}

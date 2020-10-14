// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.learn.appconfig;

import com.azure.core.http.HttpClient;
import com.azure.learn.appconfig.models.ConfigurationSetting;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigurationClientTests extends ConfigurationClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private ConfigurationClient client;

    private void initializeClient(HttpClient httpClient) {
        client = new ConfigurationClientBuilder()
            .pipeline(getHttpPipeline(httpClient))
            .endpoint(getEndpoint())
            .buildClient();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getConfiguration(HttpClient httpClient) {
        // arrange
        initializeClient(httpClient);

        // act
        ConfigurationSetting actual = client.getConfigurationSetting("FontColor");

        // assert
        assertEquals("FontColor", actual.getKey());
        assertEquals("Green", actual.getValue());

        // act
        actual = client.getConfigurationSetting("GreetingText");

        // assert
        assertEquals("GreetingText", actual.getKey());
        assertEquals("Hello World!", actual.getValue());
    }
}

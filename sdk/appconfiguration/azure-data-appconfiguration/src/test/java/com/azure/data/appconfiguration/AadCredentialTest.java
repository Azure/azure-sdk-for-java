// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.implementation.ConfigurationClientCredentials;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.data.appconfiguration.ConfigurationClientTestBase.FAKE_CONNECTION_STRING;
import static com.azure.data.appconfiguration.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;

/**
 * Unit test for construct a configuration client by using AAD token credential.
 */
public class AadCredentialTest extends TestProxyTestBase {
    private static ConfigurationClient client;
    static String connectionString;
    static TokenCredential tokenCredential;

    private void setup(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        if (interceptorManager.isPlaybackMode()) {
            String endpoint = new ConfigurationClientCredentials(FAKE_CONNECTION_STRING).getBaseUri();
            // In playback mode use connection string because CI environment doesn't set up to support AAD
            client = new ConfigurationClientBuilder()
                .credential(TestHelper.getTokenCredential(interceptorManager))
                .endpoint(endpoint)
                .httpClient(interceptorManager.getPlaybackClient())
                .buildClient();
        } else {
            tokenCredential = TestHelper.getTokenCredential(interceptorManager);
            String endpoint = Configuration.getGlobalConfiguration().get("AZ_CONFIG_ENDPOINT");
            ConfigurationClientBuilder builder = new ConfigurationClientBuilder()
                .endpoint(endpoint)
                .credential(tokenCredential)
                .serviceVersion(serviceVersion);

            builder = setHttpClient(httpClient, builder);

            if (interceptorManager.isRecordMode()) {
                builder.addPolicy(interceptorManager.getRecordPolicy()); // Record
            }


            client = builder.buildClient();
        }

        // Disable `("$.key")` sanitizer
        if (!interceptorManager.isLiveMode()) {
            interceptorManager.removeSanitizers("AZSDK3447");
        }
    }

    ConfigurationClientBuilder setHttpClient(HttpClient httpClient, ConfigurationClientBuilder builder) {
        if (interceptorManager.isPlaybackMode()) {
            return builder.httpClient(buildSyncAssertingClient(interceptorManager.getPlaybackClient()));
        }
        return builder.httpClient(buildSyncAssertingClient(httpClient));
    }

    private HttpClient buildSyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .assertSync()
            .build();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void aadAuthenticationAzConfigClient(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        setup(httpClient, serviceVersion);
        final String key = "newKey";
        final String value = "newValue";

        ConfigurationSetting addedSetting = client.setConfigurationSetting(key, null, value);
        Assertions.assertEquals(addedSetting.getKey(), key);
        Assertions.assertEquals(addedSetting.getValue(), value);
    }
}

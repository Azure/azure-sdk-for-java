// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.implementation.ConfigurationClientCredentials;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.Arrays;
import java.util.List;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

/**
 * Unit test for construct a configuration client by using AAD token credential.
 */
@TestInstance(PER_CLASS)
public class AadCredentialTest extends TestBase {
    private static ConfigurationClient client;
    private static final String AZURE_APPCONFIG_CONNECTION_STRING = "AZURE_APPCONFIG_CONNECTION_STRING";
    static String connectionString;
    static TokenCredential tokenCredential;

    public void setup(HttpClient httpClient) throws InvalidKeyException, NoSuchAlgorithmException {
        if (interceptorManager.isPlaybackMode()) {
            connectionString = "Endpoint=http://localhost:8080;Id=0000000000000;Secret=MDAwMDAw";

            String endpoint = new ConfigurationClientCredentials(connectionString).getBaseUri();
            // In playback mode use connection string because CI environment doesn't set up to support AAD
            client = new ConfigurationClientBuilder()
                .connectionString(connectionString)
                .endpoint(endpoint)
                .httpClient(httpClient)
                .buildClient();
        } else {
            connectionString = Configuration.getGlobalConfiguration().get(AZURE_APPCONFIG_CONNECTION_STRING);
            tokenCredential = new DefaultAzureCredentialBuilder().build();

            String endpoint = new ConfigurationClientCredentials(connectionString).getBaseUri();
            client = new ConfigurationClientBuilder()
                .httpClient(httpClient)
                .credential(tokenCredential)
                .endpoint(endpoint)
                .addPolicy(interceptorManager.getRecordPolicy()) // Record
                .buildClient();
        }
    }

    @ParameterizedTest(name="{0}")
    @MethodSource("getHttpClients")
    public void aadAuthenticationAzConfigClient(HttpClient httpClient) throws Exception {
        setup(httpClient);
        final String key = "newKey";
        final String value = "newValue";

        ConfigurationSetting addedSetting = client.setConfigurationSetting(key, null, value);
        Assertions.assertEquals(addedSetting.getKey(), key);
        Assertions.assertEquals(addedSetting.getValue(), value);
    }

    private HttpClient[] getHttpClients(){
        if (getTestMode() == TestMode.PLAYBACK) {
            return Arrays.asList(interceptorManager.getPlaybackClient())
                .toArray(HttpClient[]::new);
        } else {
            return Arrays.asList(new NettyAsyncHttpClientBuilder().wiretap(true).build(),
                new OkHttpAsyncHttpClientBuilder().build()).toArray(HttpClient[]::new);
        }
    }
}

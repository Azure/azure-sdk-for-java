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
import com.azure.core.util.ServiceVersion;
import com.azure.data.appconfiguration.implementation.ConfigurationClientCredentials;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.data.appconfiguration.TestHelper.getCombinations;
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

    public void setup(Object httpClient, Object serviceVersion) throws InvalidKeyException, NoSuchAlgorithmException {
        if (interceptorManager.isPlaybackMode()) {
            connectionString = "Endpoint=http://localhost:8080;Id=0000000000000;Secret=MDAwMDAw";

            String endpoint = new ConfigurationClientCredentials(connectionString).getBaseUri();
            // In playback mode use connection string because CI environment doesn't set up to support AAD
            client = new ConfigurationClientBuilder()
                .connectionString(connectionString)
                .endpoint(endpoint)
                .httpClient(interceptorManager.getPlaybackClient())
                .buildClient();
        } else {
            connectionString = Configuration.getGlobalConfiguration().get(AZURE_APPCONFIG_CONNECTION_STRING);
            tokenCredential = new DefaultAzureCredentialBuilder().build();

            String endpoint = new ConfigurationClientCredentials(connectionString).getBaseUri();
            client = new ConfigurationClientBuilder()
                .httpClient((HttpClient) httpClient)
                .credential(tokenCredential)
                .endpoint(endpoint)
                .addPolicy(interceptorManager.getRecordPolicy()) // Record
                .serviceVersion((ConfigurationServiceVersion) serviceVersion)
                .buildClient();
        }
    }

    @ParameterizedTest()
    @MethodSource("getSources")
    public void aadAuthenticationAzConfigClient(HttpClient httpClient, ServiceVersion serviceVersion) throws Exception {
        setup(httpClient, serviceVersion);
        final String key = "newKey";
        final String value = "newValue";

        ConfigurationSetting addedSetting = client.setConfigurationSetting(key, null, value);
        Assertions.assertEquals(addedSetting.getKey(), key);
        Assertions.assertEquals(addedSetting.getValue(), value);
    }

    private Stream<Arguments> getSources(){
        HttpClient[] httpClients = new HttpClient[]{new NettyAsyncHttpClientBuilder().wiretap(true).build(),
            new OkHttpAsyncHttpClientBuilder().build()};
        return getCombinations(httpClients, ConfigurationServiceVersion.values());
    }


}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.credential.TokenCredential;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.implementation.ConfigurationClientCredentials;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class SyncTokenPolicyTest extends TestBase {
    private static ConfigurationClient client;
    private static final String AZURE_APPCONFIG_CONNECTION_STRING = "AZURE_APPCONFIG_CONNECTION_STRING";
    static String connectionString;
    static TokenCredential tokenCredential;

    @BeforeEach
    public void setup() throws InvalidKeyException, NoSuchAlgorithmException {
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
                .credential(tokenCredential)
                .endpoint(endpoint)
                .addPolicy(interceptorManager.getRecordPolicy()) // Record
                .buildClient();
        }
    }


    @Test
    public void parseSyncToken() {

    }

    @Test
    public void saveSyncToken() {

    }

    @Test
    public void setSyncToken() {

    }

    @Test
    public void saveMultipleSyncToken() {

    }

    @Test
    public void setMultipleSyncToken() {

    }

    @Test
    public void updateCachedSyncToken() {

    }

}

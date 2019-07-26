// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.test.models.RecordedData;
import com.azure.core.test.policy.RecordNetworkCallPolicy;
import com.azure.identity.credential.DefaultAzureCredential;
import com.azure.security.keyvault.keys.models.Key;
import com.azure.security.keyvault.keys.models.KeyBase;
import com.azure.security.keyvault.keys.models.webkey.KeyType;

/**
 * This class contains code samples for generating javadocs through doclets for {@link KeyClient}
 */
public final class KeyClientJavaDocCodeSnippets {

    /**
     * Generates code sample for creating a {@link KeyAsyncClient}
     * @return An instance of {@link KeyAsyncClient}
     */
    public KeyAsyncClient createAsyncClientWithHttpClient() {
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.withhttpclient.instantiation
        RecordedData networkData = new RecordedData();
        KeyAsyncClient keyClient = new KeyClientBuilder()
            .endpoint("https://myvault.azure.net/")
            .credential(new DefaultAzureCredential())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .addPolicy(new RecordNetworkCallPolicy(networkData))
            .httpClient(HttpClient.createDefault())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.keys.async.keyclient.withhttpclient.instantiation
        return keyClient;
    }

    /**
     * Generates code sample for creating a {@link KeyClient}
     * @return An instance of {@link KeyClient}
     */
    public KeyClient createClient() {
        // BEGIN: com.azure.security.keyvault.keys.keyclient.instantiation
        KeyClient keyClient = new KeyClientBuilder()
            .endpoint("https://myvault.azure.net/")
            .credential(new DefaultAzureCredential())
            .buildClient();
        // END: com.azure.security.keyvault.keys.keyclient.instantiation
        return keyClient;
    }

    /**
     * Generates code sample for creating a {@link KeyAsyncClient}
     * @return An instance of {@link KeyAsyncClient}
     */
    public KeyAsyncClient createAsyncClient() {
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.instantiation
        KeyAsyncClient keyClient = new KeyClientBuilder()
            .endpoint("https://myvault.azure.net/")
            .credential(new DefaultAzureCredential())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.keys.async.keyclient.instantiation
        return keyClient;
    }

    /**
     * Generates code sample for creating a {@link KeyAsyncClient}
     * @return An instance of {@link KeyAsyncClient}
     */
    public KeyAsyncClient createAsyncClientWithPipeline() {
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.pipeline.instantiation
        RecordedData networkData = new RecordedData();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RecordNetworkCallPolicy(networkData)).build();
        KeyAsyncClient keyClient = new KeyClientBuilder()
            .pipeline(pipeline)
            .endpoint("https://myvault.azure.net/")
            .credential(new DefaultAzureCredential())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.keys.async.keyclient.pipeline.instantiation
        return keyClient;
    }


    /**
     * Generates a code sample for using {@link KeyClient#createKey(String, KeyType)}
     */
    public void createKey() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.createKey#string-keyType
        Key retKey = keyClient.createKey("keyName", KeyType.EC).value();
        System.out.printf("Key is created with name %s and id %s %n", retKey.name(), retKey.id());
        // END: com.azure.keyvault.keys.keyclient.createKey#string-keyType
    }

    /**
     * Generates code sample for using {@link KeyClient#listKeyVersions(String)}
     */
    public void listKeyVersions() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.listKeyVersions
        for (KeyBase key : keyClient.listKeyVersions("keyName")) {
            Key keyWithMaterial  = keyClient.getKey(key).value();
            System.out.printf("Received key's version with name %s, type %s and version %s", keyWithMaterial.name(),
                    keyWithMaterial.keyMaterial().kty(), keyWithMaterial.version());
        }
        // END: com.azure.keyvault.keys.keyclient.listKeyVersions
    }

    /**
     * Implementation not provided for this method
     * @return {@code null}
     */
    private TokenCredential getKeyVaultCredential() {
        return null;
    }
}

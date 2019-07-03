// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.PortPolicy;
import com.azure.identity.credential.DefaultAzureCredential;
import com.azure.security.keyvault.secrets.models.Secret;
import com.azure.security.keyvault.secrets.models.SecretBase;

/**
 * This class contains code samples for generating javadocs through doclets for {@link SecretClient]
 */
public final class SecretClientJavaDocCodeSnippets {

    /**
     * Method to insert code snippets for {@link SecretClient#getSecret(SecretBase)}
     */
    public void getSecret() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.keyvault.secretclient.getSecret#secretBase
        for(SecretBase secret : secretClient.listSecrets()){
            Secret secretWithValue  = secretClient.getSecret(secret).value();
            System.out.printf("Secret is returned with name %s and value %s %n", secretWithValue.name(),
                    secretWithValue.value());
        }
        // END: com.azure.keyvault.secretclient.getSecret#secretBase
    }

    /**
     * Implementation for async SecretClient
     * @return sync SecretClient
     */
    private SecretAsyncClient getAsyncSecretClient() {

        // BEGIN: com.azure.keyvault.secretclient.async.construct
        SecretAsyncClient secretClient = new SecretClientBuilder()
            .credential(new DefaultAzureCredential())
            .endpoint("https://myvault.vault.azure.net/")
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .buildAsyncClient();
        // END: com.azure.keyvault.secretclient.async.construct
        return secretClient;
    }

    /**
     * Implementation for sync SecretClient
     * @return sync SecretClient
     */
    private SecretClient getSyncSecretClient() {

        // BEGIN: com.azure.keyvault.secretclient.sync.construct
        SecretClient secretClient = new SecretClientBuilder()
            .credential(new DefaultAzureCredential())
            .endpoint("https://myvault.vault.azure.net/")
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .buildClient();
        // END: com.azure.keyvault.secretclient.sync.construct
        return secretClient;
    }

    /**
     * Implementation not provided for this method
     * @return {@code null}
     */
    private SecretClient getSecretClient() {
        return null;
    }
}

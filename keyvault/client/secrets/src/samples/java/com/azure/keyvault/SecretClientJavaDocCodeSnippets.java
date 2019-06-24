// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault;

import com.azure.keyvault.models.Secret;
import com.azure.keyvault.models.SecretBase;

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
            System.out.printf("Secret is returned with name %s and value %s %n", secretWithValue.name(), secretWithValue.value());
        }
        // END: com.azure.keyvault.secretclient.getSecret#secretBase
    }

    /**
     * Implementation not provided for this method
     * @return {@code null}
     */
    private SecretClient getSecretClient() {
        return null;
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.secrets;

import com.azure.keyvault.secrets.models.SecretBase;
import com.azure.keyvault.secrets.SecretClient;

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
        secretClient.listSecrets()
            .stream()
            .map(secretClient::getSecret)
            .forEach(secretResponse ->
                System.out.printf("Secret is returned with name %s and value %s %n",
                    secretResponse.value().name(), secretResponse.value().value()));
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

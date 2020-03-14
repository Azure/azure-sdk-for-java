// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.core.util.polling.SyncPoller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class KeyVaultSecrets {
    private static SecretClient secretClient;
    private static final String SECRET_NAME = "MySecretName-" + UUID.randomUUID();
    private static final String SECRET_VALUE = "MySecretValue";

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyVaultSecrets.class);

    private static void setSecret() {
        LOGGER.info("Setting a secret...");
        KeyVaultSecret response = secretClient.setSecret(SECRET_NAME, SECRET_VALUE);
        LOGGER.info("\tDONE: ({},{}).", response.getName(), response.getValue());
    }

    private static void getSecret() {
        LOGGER.info("Getting the secret... ");
        KeyVaultSecret response = secretClient.getSecret(SECRET_NAME);
        LOGGER.info("\tDONE: secret ({},{}) retrieved.", response.getName(), response.getValue());
    }

    private static void deleteSecret() {
        LOGGER.info("Deleting the secret... ");
        SyncPoller<DeletedSecret, Void> poller = secretClient.beginDeleteSecret(SECRET_NAME);
        DeletedSecret response = poller.poll().getValue();

        LOGGER.info("\tDONE: deleted.");
    }

    public static void main(String[] args) {
        LOGGER.info("---------------------");
        LOGGER.info("KEY VAULT - SECRETS");
        LOGGER.info("IDENTITY - CREDENTIAL");
        LOGGER.info("---------------------");

        /* DefaultAzureCredentialBuilder() is expecting the following environment variables:
         * AZURE_CLIENT_ID
         * AZURE_CLIENT_SECRET
         * AZURE_TENANT_ID
         *
         * AZURE_AUTHORITY_HOST -- this is supplied to the credential builder
         * for use in other clouds
         */
        secretClient = new SecretClientBuilder()
            .vaultUrl(System.getenv("AZURE_PROJECT_URL"))
            .credential(
                new DefaultAzureCredentialBuilder()
                    .authorityHost(System.getenv("AZURE_AUTHORITY_HOST"))
                    .build()
            ).buildClient();
        try {
            setSecret();
            getSecret();
        } finally {
            deleteSecret();
        }
    }
}

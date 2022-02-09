// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.smoketest;

import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.SecretServiceVersion;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.core.util.polling.SyncPoller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.UUID;

public class KeyVaultSecrets {
    private static SecretClient secretClient;
    private static final String SECRET_NAME = "MySecretName-" + UUID.randomUUID();
    private static final String SECRET_VALUE = "MySecretValue";

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyVaultSecrets.class);

    private static HashMap<String, String> AUTHORITY_HOST_MAP = new HashMap<String, String>() {{
        put("AzureCloud", AzureAuthorityHosts.AZURE_PUBLIC_CLOUD);
        put("AzureChinaCloud", AzureAuthorityHosts.AZURE_CHINA);
        put("AzureGermanCloud", AzureAuthorityHosts.AZURE_GERMANY);
        put("AzureUSGovernment", AzureAuthorityHosts.AZURE_GOVERNMENT);
    }};

    private static HashMap<String, SecretServiceVersion> AUTHORITY_HOST_SERVICE_VERSION_MAP =
        new HashMap<String, SecretServiceVersion>() {{
        put("AzureCloud", SecretServiceVersion.V7_1);
        put("AzureChinaCloud", SecretServiceVersion.V7_0);
        put("AzureGermanCloud", SecretServiceVersion.V7_0);
        put("AzureUSGovernment", SecretServiceVersion.V7_0);
    }};

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

        // Configure authority host from AZURE_CLOUD
        String azureCloud = System.getenv("AZURE_CLOUD");
        String authorityHost = AUTHORITY_HOST_MAP.getOrDefault(
            azureCloud, AzureAuthorityHosts.AZURE_PUBLIC_CLOUD);

        SecretServiceVersion serviceVersion =
            AUTHORITY_HOST_SERVICE_VERSION_MAP.getOrDefault(azureCloud, SecretServiceVersion.getLatest());


        /* DefaultAzureCredentialBuilder() is expecting the following environment variables:
         * AZURE_CLIENT_ID
         * AZURE_CLIENT_SECRET
         * AZURE_TENANT_ID
         */
        secretClient = new SecretClientBuilder()
            .vaultUrl(System.getenv("AZURE_PROJECT_URL"))
            .credential(
                new DefaultAzureCredentialBuilder()
                    .authorityHost(authorityHost)
                    .build())
            .serviceVersion(serviceVersion)
            .buildClient();
        try {
            setSecret();
            getSecret();
        } finally {
            deleteSecret();
        }
    }
}

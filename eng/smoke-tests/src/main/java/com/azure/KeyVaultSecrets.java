package com.azure;

import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.Secret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class KeyVaultSecrets {
    private static SecretClient secretClient;
    private static final String SECRET_NAME = "MySecretName-" + UUID.randomUUID();
    private static final String SECRET_VALUE = "MySecretValue";

    private static final Logger logger = LoggerFactory.getLogger(KeyVaultSecrets.class);

    private static void setSecret() {
        logger.info("Setting a secret...");
        Secret response = secretClient.setSecret(SECRET_NAME, SECRET_VALUE);
        logger.info("\tDONE: ({},{}).", response.name(), response.value());
    }

    private static void getSecret() {
        logger.info("Getting the secret... ");
        Secret response = secretClient.getSecret(SECRET_NAME);
        logger.info("\tDONE: secret ({},{}) retrieved.", response.name(), response.value());
    }

    private static void deleteSecret() {
        logger.info("Deleting the secret... ");
        DeletedSecret response = secretClient.deleteSecret(SECRET_NAME);
        logger.info("\tDONE: '{}' deleted.", response.name());
    }

    public static void main(String[] args) {
        logger.info("---------------------");
        logger.info("KEY VAULT - SECRETS");
        logger.info("IDENTITY - CREDENTIAL");
        logger.info("---------------------");

        /* DefaultAzureCredentialBuilder() is expecting the following environment variables:
         * AZURE_CLIENT_ID
         * AZURE_CLIENT_SECRET
         * AZURE_TENANT_ID
         */
        secretClient = new SecretClientBuilder()
            .endpoint(System.getenv("AZURE_PROJECT_URL"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        try {
            setSecret();
            getSecret();
        } finally {
            deleteSecret();
        }
    }
}

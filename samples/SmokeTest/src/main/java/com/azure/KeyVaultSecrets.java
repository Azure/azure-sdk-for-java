package com.azure;

import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.Secret;

import java.util.UUID;

public class KeyVaultSecrets {
    private static SecretClient secretClient;
    private static final String SECRET_NAME = "MySecretName-" + UUID.randomUUID();
    private static final String SECRET_VALUE = "MySecretValue";

    private static void setSecret() {
        System.out.print("Setting a secret...");
        Secret response = secretClient.setSecret(SECRET_NAME, SECRET_VALUE);
        System.out.printf("\tDONE: (%s,%s).\n", response.name(), response.value());
    }

    private static void getSecret() {
        System.out.print("Getting the secret... ");
        Secret response = secretClient.getSecret(SECRET_NAME);
        System.out.printf("\tDONE: secret (%s,%s) retrieved.\n", response.name(), response.value());
    }

    private static void deleteSecret() {
        System.out.print("Deleting the secret... ");
        DeletedSecret response = secretClient.deleteSecret(SECRET_NAME);
        System.out.printf("\tDONE: '%s' deleted.\n", response.name());
    }

    public static void main(String[] args) {
        System.out.println("\n---------------------");
        System.out.println("KEY VAULT - SECRETS");
        System.out.println("IDENTITY - CREDENTIAL");
        System.out.println("---------------------\n");

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

package com.azure;

import com.azure.core.http.rest.Response;
import com.azure.identity.credential.DefaultAzureCredential;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.Secret;

public class KeyVaultSecrets {
    private static SecretClient secretClient;
    private static final String SECRET_NAME = "MySecretName";
    private static final String SECRET_VALUE = "MySecretValue";

    private static void setSecret() {
        System.out.print("Setting a secret...");
        Response<Secret> response = secretClient.setSecret(SECRET_NAME, SECRET_VALUE);
        System.out.printf("\tDONE: (%s,%s).\n", response.value().name(), response.value().value());
    }

    private static void getSecret() {
        System.out.print("Getting the secret... ");
        Response<Secret> response = secretClient.getSecret(SECRET_NAME);
        System.out.printf("\tDONE: secret (%s,%s) retrieved.\n", response.value().name(), response.value().value());
    }

    private static void deleteSecret() {
        System.out.print("Deleting the secret... ");
        Response<DeletedSecret> response = secretClient.deleteSecret(SECRET_NAME);
        System.out.printf("\tDONE: '%s' deleted.\n", response.value().name());
    }

    public static void main(String[] args) {
        System.out.println("\n---------------------");
        System.out.println("KEY VAULT - SECRETS");
        System.out.println("IDENTITY - CREDENTIAL");
        System.out.println("---------------------\n");

        /* DefaultAzureCredential() is expecting the following environment variables:
         * AZURE_CLIENT_ID
         * AZURE_CLIENT_SECRET
         * AZURE_TENANT_ID
         */
        secretClient = SecretClient.builder()
            .endpoint("https://azsdk-smoketest.vault.azure.net")
            .credential(new DefaultAzureCredential())
            .build();
        try {
            setSecret();
            getSecret();
        } finally {
            deleteSecret();
        }
    }
}

package com.azure.security.keyvault.secrets;

import com.azure.identity.credential.DefaultAzureCredential;
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.models.Secret;

public class PersistentTokenCacheDemo {

    public static void main(String[] args) {

        // Wrote to AZURE_USERNAME env variable
        DefaultAzureCredential defaultCredential = new DefaultAzureCredentialBuilder().build();

        SecretClient client = new SecretClientBuilder()
            .endpoint("https://persistentcachedemo.vault.azure.net")
            .credential(defaultCredential)
            .buildClient();

        // Try to get a secret! Only works if you are logged in
        try{
            System.out.println("\nWhat is the super secret secret?\n\n");
            Secret secret = client.getSecret("the-secret");
            System.out.println("Secret was found: " + secret.value());
        }

        catch(Exception e) {
            System.out.println("Sad life, we shall never know :( ");
        }
    }

}

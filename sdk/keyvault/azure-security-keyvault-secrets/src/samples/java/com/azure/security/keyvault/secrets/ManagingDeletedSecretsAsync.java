// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.models.SecretProperties;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to asynchronously list, recover and purge deleted secrets in a soft-delete enabled key vault.
 */
public class ManagingDeletedSecretsAsync {
    /**
     * Authenticates with the key vault and shows how to asynchronously list, recover and purge deleted secrets in a soft-delete enabled key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    public static void main(String[] args) throws InterruptedException {

        // NOTE: To manage deleted secrets, your key vault needs to have soft-delete enabled. Soft-delete allows deleted secrets
        // to be retained for a given retention period (90 days). During this period deleted secrets can be recovered and if
        // a secret needs to be permanently deleted then it needs to be purged.

        // Instantiate a client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        SecretAsyncClient secretAsyncClient = new SecretClientBuilder()
                .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildAsyncClient();

        // Let's create secrets holding storage and bank accounts credentials valid for 1 year. if the secret
        // already exists in the key vault, then a new version of the secret is created.
        secretAsyncClient.setSecret(new KeyVaultSecret("BankAccountPassword", "f4G34fMh8v")
                .setProperties(new SecretProperties()
                    .setExpiresOn(OffsetDateTime.now().plusYears(1)))).subscribe(secretResponse ->
                System.out.printf("Secret is created with name %s and value %s %n", secretResponse.getName(), secretResponse.getValue()));

        Thread.sleep(2000);

        secretAsyncClient.setSecret(new KeyVaultSecret("StorageAccountPassword", "f4G34fMh8v-fdsgjsk2323=-asdsdfsdf")
                .setProperties(new SecretProperties()
                    .setExpiresOn(OffsetDateTime.now().plusYears(1)))).subscribe(secretResponse ->
                System.out.printf("Secret is created with name %s and value %s %n", secretResponse.getName(), secretResponse.getValue()));

        Thread.sleep(2000);

        // The storage account was closed, need to delete its credentials from the key vault.
        secretAsyncClient.beginDeleteSecret("BankAccountPassword")
            .subscribe(pollResponse -> {
                System.out.println("Delete Status: " + pollResponse.getStatus().toString());
                System.out.println("Deleted Secret Name: " + pollResponse.getValue().getName());
                System.out.println("Deleted Secret Value: " + pollResponse.getValue().getValue());
            });

        //To ensure secret is deleted on server side.
        Thread.sleep(30000);

        // We accidentally deleted bank account secret. Let's recover it.
        // A deleted secret can only be recovered if the key vault is soft-delete enabled.
        secretAsyncClient.beginRecoverDeletedSecret("BankAccountPassword")
            .subscribe(pollResponse -> {
                System.out.println("Recovery Status: " + pollResponse.getStatus().toString());
                System.out.println("Recovered Secret Name: " + pollResponse.getValue().getName());
                System.out.println("Recovered Secret Value: " + pollResponse.getValue().getValue());
            });

        //To ensure secret is recovered on server side.
        Thread.sleep(10000);

        // The bank acoount and storage accounts got closed.
        // Let's delete bank and  storage accounts secrets.
        secretAsyncClient.beginDeleteSecret("BankAccountPassword")
            .subscribe(pollResponse -> {
                System.out.println("Delete Status: " + pollResponse.getStatus().toString());
                System.out.println("Deleted Secret Name: " + pollResponse.getValue().getName());
                System.out.println("Deleted Secret Value: " + pollResponse.getValue().getValue());
            });

        secretAsyncClient.beginDeleteSecret("StorageAccountPassword")
            .subscribe(pollResponse -> {
                System.out.println("Delete Status: " + pollResponse.getStatus().toString());
                System.out.println("Deleted Secret Name: " + pollResponse.getValue().getName());
                System.out.println("Deleted Secret Value: " + pollResponse.getValue().getValue());
            });

        // To ensure secret is deleted on server side.
        Thread.sleep(30000);

        // You can list all the deleted and non-purged secrets, assuming key vault is soft-delete enabled.
        secretAsyncClient.listDeletedSecrets().subscribe(deletedSecret ->
                System.out.printf("Deleted secret's recovery Id %s %n", deletedSecret.getRecoveryId()));

        Thread.sleep(15000);

        // If the key vault is soft-delete enabled, then for permanent deletion  deleted secrets need to be purged.
        secretAsyncClient.purgeDeletedSecretWithResponse("StorageAccountPassword").subscribe(purgeResponse ->
            System.out.printf("Storage account secret purge status response %d %n", purgeResponse.getStatusCode()));

        secretAsyncClient.purgeDeletedSecretWithResponse("BankAccountPassword").subscribe(purgeResponse ->
            System.out.printf("Bank account secret purge status response %d %n", purgeResponse.getStatusCode()));

        // To ensure secret is purged on server side.
        Thread.sleep(15000);
    }
}

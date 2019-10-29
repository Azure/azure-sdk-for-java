// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to list, recover and purge deleted secrets in a soft-delete enabled key vault.
 */
public class ManagingDeletedSecrets {
    /**
     * Authenticates with the key vault and shows how to list, recover and purge deleted secrets in a soft-delete enabled key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    public static void main(String[] args) throws IllegalArgumentException, InterruptedException {

        // NOTE: To manage deleted secrets, your key vault needs to have soft-delete enabled. Soft-delete allows deleted secrets
        // to be retained for a given retention period (90 days). During this period deleted secrets can be recovered and if
        // a secret needs to be permanently deleted then it needs to be purged.

        // Instantiate a client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // Let's create secrets holding storage and bank accounts credentials valid for 1 year. if the secret
        // already exists in the key vault, then a new version of the secret is created.
        client.setSecret(new KeyVaultSecret("StorageAccountPassword", "f4G34fMh8v-fdsgjsk2323=-asdsdfsdf")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.now().plusYears(1))));

        client.setSecret(new KeyVaultSecret("BankAccountPassword", "f4G34fMh8v")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.now().plusYears(1))));

        // The storage account was closed, need to delete its credentials from the key vault.
        SyncPoller<DeletedSecret, Void> deletedBankSecretPoller = client.beginDeleteSecret("BankAccountPassword");

        PollResponse<DeletedSecret> deletedBankSecretPollResponse = deletedBankSecretPoller.poll();

        System.out.println("Deleted Date %s" + deletedBankSecretPollResponse.getValue().getDeletedOn().toString());
        System.out.printf("Deleted Secret's Recovery Id %s", deletedBankSecretPollResponse.getValue().getRecoveryId());

        // Key is being deleted on server.
        deletedBankSecretPoller.waitForCompletion();

        // We accidentally deleted bank account secret. Let's recover it.
        // A deleted secret can only be recovered if the key vault is soft-delete enabled.
        SyncPoller<KeyVaultSecret, Void> recoverSecretPoller =
            client.beginRecoverDeletedSecret("BankAccountPassword");

        PollResponse<KeyVaultSecret> recoverSecretResponse = recoverSecretPoller.poll();

        System.out.println("Recovered Key Name %s" + recoverSecretResponse.getValue().getName());
        System.out.printf("Recovered Key's Id %s", recoverSecretResponse.getValue().getId());

        // Key is being recovered on server.
        recoverSecretPoller.waitForCompletion();

        // The bank acoount and storage accounts got closed.
        // Let's delete bank and  storage accounts secrets.
        SyncPoller<DeletedSecret, Void> deletedBankPwdSecretPoller
                = client.beginDeleteSecret("BankAccountPassword");

        PollResponse<DeletedSecret> deletedBankPwdSecretPollResponse = deletedBankPwdSecretPoller.poll();

        System.out.println("Deleted Date %s" + deletedBankPwdSecretPollResponse.getValue().getDeletedOn().toString());
        System.out.printf("Deleted Secret's Recovery Id %s", deletedBankPwdSecretPollResponse.getValue().getRecoveryId());

        // Key is being deleted on server.
        deletedBankPwdSecretPoller.waitForCompletion();

        SyncPoller<DeletedSecret, Void> deletedStorageSecretPoller
                = client.beginDeleteSecret("StorageAccountPassword");

        PollResponse<DeletedSecret> deletedStorageSecretPollResponse = deletedStorageSecretPoller.poll();

        System.out.println("Deleted Date  %s" + deletedStorageSecretPollResponse.getValue().getDeletedOn().toString());
        System.out.printf("Deleted Secret's Recovery Id %s", deletedStorageSecretPollResponse.getValue().getRecoveryId());

        // Key is being deleted on server.
        deletedStorageSecretPoller.waitForCompletion();


        // You can list all the deleted and non-purged secrets, assuming key vault is soft-delete enabled.
        for (DeletedSecret delSecret : client.listDeletedSecrets()) {
            System.out.printf("Deleted secret's recovery Id %s", delSecret.getRecoveryId());
        }

        // If the key vault is soft-delete enabled, then for permanent deletion deleted secrets need to be purged.
        client.purgeDeletedSecret("StorageAccountPassword");
        client.purgeDeletedSecret("BankAccountPassword");

        //To ensure secret is purged on server side.
        Thread.sleep(15000);
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.models.SecretProperties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to backup and restore secrets in the key vault.
 */
public class BackupAndRestoreOperations {
    /**
     * Authenticates with the key vault and shows how to backup and restore secrets in the key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     * @throws IOException when writing backup to file is unsuccessful.
     */
    public static void main(String[] args) throws IOException, InterruptedException, IllegalArgumentException {

        // Instantiate a client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // Let's create secrets holding storage account credentials valid for 1 year. if the secret
        // already exists in the key vault, then a new version of the secret is created.
        client.setSecret(new KeyVaultSecret("StorageAccountPassword", "f4G34fMh8v-fdsgjsk2323=-asdsdfsdf")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.now().plusYears(1))));

        // Backups are good to have, if in case secrets get accidentally deleted by you.
        // For long term storage, it is ideal to write the backup to a file.
        String backupFilePath = "YOUR_BACKUP_FILE_PATH";
        byte[] secretBackup = client.backupSecret("StorageAccountPassword");
        writeBackupToFile(secretBackup, backupFilePath);

        // The storage account secret is no longer in use, so you delete it.
        SyncPoller<DeletedSecret, Void> deletedStorageSecretPoller
                = client.beginDeleteSecret("StorageAccountPassword");

        PollResponse<DeletedSecret>  pollResponse = deletedStorageSecretPoller.poll();


        DeletedSecret deletedStorageSecret = pollResponse.getValue();
        System.out.println("Deleted Date %s" + deletedStorageSecret.getDeletedOn().toString());
        System.out.printf("Deleted Secret's Recovery Id %s", deletedStorageSecret.getRecoveryId());

        // Key is being deleted on server.
        deletedStorageSecretPoller.waitForCompletion();


        //To ensure secret is deleted on server side.
        Thread.sleep(30000);

        // If the vault is soft-delete enabled, then you need to purge the secret as well for permanent deletion.
        client.purgeDeletedSecret("StorageAccountPassword");

        //To ensure secret is purged on server side.
        Thread.sleep(15000);

        // After sometime, the secret is required again. We can use the backup value to restore it in the key vault.
        byte[] backupFromFile = Files.readAllBytes(new File(backupFilePath).toPath());
        KeyVaultSecret restoredSecret = client.restoreSecretBackup(backupFromFile);
    }

    private static void writeBackupToFile(byte[] bytes, String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            OutputStream os = new FileOutputStream(file);
            os.write(bytes);
            System.out.println("Successfully wrote backup to file.");
            // Close the file
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.models.Secret;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to asynchronously backup and restore secrets in the key vault.
 */
public class BackupAndRestoreOperationsAsync {
    /**
     * Authenticates with the key vault and shows how to asynchronously backup and restore secrets in the key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     * @throws IOException when writing backup to file is unsuccessful.
     */
    public static void main(String[] args) throws IOException, InterruptedException, IllegalArgumentException {

        // Instantiate async secret client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        SecretAsyncClient secretAsyncClient = new SecretClientBuilder()
            .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        // Let's create secrets holding storage account credentials valid for 1 year. if the secret
        // already exists in the key vault, then a new version of the secret is created.
        secretAsyncClient.setSecret(new Secret("StorageAccountPassword", "f4G34fMh8v-fdsgjsk2323=-asdsdfsdf")
            .expires(OffsetDateTime.now().plusYears(1)))
            .subscribe(secretResponse ->
                System.out.printf("Secret is created with name %s and value %s \n", secretResponse.name(), secretResponse.value()));

        Thread.sleep(2000);

        // Backups are good to have, if in case secrets get accidentally deleted by you.
        // For long term storage, it is ideal to write the backup to a file.
        String backupFilePath = "YOUR_BACKUP_FILE_PATH";
        secretAsyncClient.backupSecret("StorageAccountPassword").subscribe(backupResponse -> {
            byte[] backupBytes = backupResponse;
            writeBackupToFile(backupBytes, backupFilePath);
        });

        Thread.sleep(7000);

        // The storage account secret is no longer in use, so you delete it.
        secretAsyncClient.deleteSecret("StorageAccountPassword").subscribe(deletedSecretResponse ->
                System.out.printf("Deleted Secret's Recovery Id %s \n", deletedSecretResponse.recoveryId()));

        //To ensure file is deleted on server side.
        Thread.sleep(30000);

        // If the vault is soft-delete enabled, then you need to purge the secret as well for permanent deletion.
        secretAsyncClient.purgeDeletedSecret("StorageAccountPassword").subscribe(purgeResponse ->
            System.out.printf("Purge Status response %d \n", purgeResponse.statusCode()));

        //To ensure file is purged on server side.
        Thread.sleep(15000);

        // After sometime, the secret is required again. We can use the backup value to restore it in the key vault.
        byte[] backupFromFile = Files.readAllBytes(new File(backupFilePath).toPath());
        secretAsyncClient.restoreSecret(backupFromFile).subscribe(secretResponse ->
            System.out.printf("Restored Secret with name %s \n", secretResponse.name()));

        //To ensure secret is restored on server side.
        Thread.sleep(15000);
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

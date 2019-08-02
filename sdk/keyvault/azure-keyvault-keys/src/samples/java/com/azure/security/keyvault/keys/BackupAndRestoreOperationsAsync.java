// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.models.RsaKeyCreateOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to asynchronously backup and restore keys in the key vault.
 */
public class BackupAndRestoreOperationsAsync {
    /**
     * Authenticates with the key vault and shows how to asynchronously backup and restore keys in the key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     * @throws IOException when writing backup to file is unsuccessful.
     */
    public static void main(String[] args) throws IOException, InterruptedException, IllegalArgumentException {

        // Instantiate async key client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        KeyAsyncClient keyAsyncClient = new KeyClientBuilder()
            .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        // Let's create Cloud Rsa key valid for 1 year. if the key
        // already exists in the key vault, then a new version of the key is created.
        keyAsyncClient.createRsaKey(new RsaKeyCreateOptions("CloudRsaKey")
                .expires(OffsetDateTime.now().plusYears(1))
                .keySize(2048))
                .subscribe(keyResponse ->
                        System.out.printf("Key is created with name %s and type %s \n", keyResponse.name(), keyResponse.keyMaterial().kty()));

        Thread.sleep(2000);

        // Backups are good to have, if in case keys get accidentally deleted by you.
        // For long term storage, it is ideal to write the backup to a file.
        String backupFilePath = "YOUR_BACKUP_FILE_PATH";
        keyAsyncClient.backupKey("CloudRsaKey").subscribe(backupResponse -> {
            byte[] backupBytes = backupResponse;
            writeBackupToFile(backupBytes, backupFilePath);
        });

        Thread.sleep(7000);

        // The Cloud Rsa key is no longer in use, so you delete it.
        keyAsyncClient.deleteKey("CloudRsaKey").subscribe(deletedKeyResponse ->
                System.out.printf("Deleted Key's Recovery Id %s \n", deletedKeyResponse.recoveryId()));

        //To ensure file is deleted on server side.
        Thread.sleep(30000);

        // If the vault is soft-delete enabled, then you need to purge the key as well for permanent deletion.
        keyAsyncClient.purgeDeletedKey("CloudRsaKey").subscribe(purgeResponse ->
            System.out.printf("Purge Status response %d \n", purgeResponse.statusCode()));

        //To ensure file is purged on server side.
        Thread.sleep(15000);

        // After sometime, the key is required again. We can use the backup value to restore it in the key vault.
        byte[] backupFromFile = Files.readAllBytes(new File(backupFilePath).toPath());
        keyAsyncClient.restoreKey(backupFromFile).subscribe(keyResponse ->
            System.out.printf("Restored Key with name %s \n", keyResponse.name()));

        //To ensure key is restored on server side.
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

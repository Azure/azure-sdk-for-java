// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;

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
        /* Instantiate a KeyAsyncClient that will be used to call the service. Notice that the client is using default
        Azure credentials. For more information on this and other types of credentials, see this document:
        https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable.

        To get started, you'll need a URL to an Azure Key Vault. See the README (https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/README.md)
        for links and instructions. */
        KeyAsyncClient keyAsyncClient = new KeyClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        // Let's create RSA key valid for 1 year. If the key
        // already exists in the key vault, then a new version of the key is created.
        keyAsyncClient.createRsaKey(new CreateRsaKeyOptions("CloudRsaKey")
                .setExpiresOn(OffsetDateTime.now().plusYears(1))
                .setKeySize(2048))
                .subscribe(keyResponse ->
                        System.out.printf("Key is created with name %s and type %s %n", keyResponse.getName(), keyResponse.getKeyType()));

        Thread.sleep(2000);

        // Backups are good to have, if in case keys get accidentally deleted by you.
        // For long term storage, it is ideal to write the backup to a file.
        String backupFilePath = "YOUR_BACKUP_FILE_PATH";
        keyAsyncClient.backupKey("CloudRsaKey").subscribe(backupResponse -> {
            byte[] backupBytes = backupResponse;
            writeBackupToFile(backupBytes, backupFilePath);
        });

        Thread.sleep(7000);

        // The RSA key is no longer in use, so you delete it.
        keyAsyncClient.beginDeleteKey("CloudRsaKey")
            .subscribe(pollResponse -> {
                System.out.println("Delete Status: " + pollResponse.getStatus().toString());
                System.out.println("Delete Key Name: " + pollResponse.getValue().getName());
                System.out.println("Key Delete Date: " + pollResponse.getValue().getDeletedOn().toString());
            });

        // To ensure file is deleted server-side.
        Thread.sleep(30000);

        // If the vault is soft-delete enabled, then you need to purge the key as well for permanent deletion.
        keyAsyncClient.purgeDeletedKeyWithResponse("CloudRsaKey").subscribe(purgeResponse ->
            System.out.printf("Purge Status response %d %n", purgeResponse.getStatusCode()));

        // To ensure file is purged server-side.
        Thread.sleep(15000);

        // After sometime, the key is required again. We can use the backup value to restore it in the key vault.
        byte[] backupFromFile = Files.readAllBytes(new File(backupFilePath).toPath());
        keyAsyncClient.restoreKeyBackup(backupFromFile).subscribe(keyResponse ->
            System.out.printf("Restored Key with name %s %n", keyResponse.getName()));

        // To ensure the key is restored server-side.
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

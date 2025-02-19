// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to backup and restore keys in the key vault.
 */
public class BackupAndRestoreOperations {
    /**
     * Authenticates with the key vault and shows how to backup and restore keys in the key vault.
     *
     * @param args Unused. Arguments to the program.
     *
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     * @throws IOException when writing backup to file is unsuccessful.
     */
    public static void main(String[] args) throws IOException, InterruptedException, IllegalArgumentException {
        /* Instantiate a KeyClient that will be used to call the service. Notice that the client is using default Azure
        credentials. For more information on this and other types of credentials, see this document:
        https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable.

        To get started, you'll need a URL to an Azure Key Vault. See the README
        (https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/README.md)
        for links and instructions. */
        KeyClient keyClient = new KeyClientBuilder()
            .vaultUrl("<your-key-vault-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // Let's create an RSA key valid for 1 year. If the key already exists in the key vault, then a new version of
        // the key is created.
        keyClient.createRsaKey(new CreateRsaKeyOptions("CloudRsaKey")
            .setExpiresOn(OffsetDateTime.now().plusYears(1))
            .setKeySize(2048));

        // Backups are good to have, if in case keys get accidentally deleted by you.
        // For long term storage, it is ideal to write the backup to a file.
        String backupFilePath = "YOUR_BACKUP_FILE_PATH";
        byte[] keyBackup = keyClient.backupKey("CloudRsaKey");

        writeBackupToFile(keyBackup, backupFilePath);

        // The RSA key is no longer in use, so you delete it.
        SyncPoller<DeletedKey, Void> rsaDeletedKeyPoller = keyClient.beginDeleteKey("CloudRsaKey");
        PollResponse<DeletedKey> pollResponse = rsaDeletedKeyPoller.poll();
        DeletedKey rsaDeletedKey = pollResponse.getValue();

        System.out.println("Deleted Date  %s" + rsaDeletedKey.getDeletedOn().toString());
        System.out.printf("Deleted Key's Recovery Id %s", rsaDeletedKey.getRecoveryId());

        // The key is being deleted on the server.
        rsaDeletedKeyPoller.waitForCompletion();

        // To ensure the key is deleted server-side.
        Thread.sleep(30000);

        // If the vault is soft-delete enabled, then you need to purge the key as well for permanent deletion.
        keyClient.purgeDeletedKey("CloudRsaKey");

        // To ensure the key is purged server-side.
        Thread.sleep(15000);

        // After sometime, the key is required again. We can use the backup value to restore it in the key vault.
        byte[] backupFromFile = Files.readAllBytes(new File(backupFilePath).toPath());
        KeyVaultKey restoredKey = keyClient.restoreKeyBackup(backupFromFile);
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

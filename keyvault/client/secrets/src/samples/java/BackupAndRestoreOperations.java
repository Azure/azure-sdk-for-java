import com.azure.keyvault.SecretClient;
import com.azure.keyvault.models.Secret;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;

public class BackupAndRestoreOperations {
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException {

        // Instantiate a client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        SecretClient client = SecretClient.builder()
            .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
            //.credentials(AzureCredential.DEFAULT)  TODO: Enable this, once Azure Identity Library merges.
            .build();

        // Let's create secrets holding storage account credentials valid for 1 year. if the secret
        // already exists in the key vault, then a new version of the secret is created.
        client.setSecret(new Secret("STORAGE_ACCOUNT_PASSWORD", "f4G34fMh8v-fdsgjsk2323=-asdsdfsdf")
            .expires(OffsetDateTime.now().plusYears(1)));

        // Backups are good to have, if in case secrets get accidentally deleted by you.
        // For long term storage, it is ideal to write the backup to a file.
        byte[] secretBackup = client.backupSecret("STORAGE_ACCOUNT_PASSWORD").value();

        // The storage account secret is no longer in use so you delete it.
        client.deleteSecret("STORAGE_ACCOUNT_PASSWORD");

        // If the vault is soft-delete enabled, then you purge the secret as well for permanent deletion.
        // client.purgeDeletedSecret("STORAGE_ACCOUNT_PASSWORD");

        // After sometime, the secret is needed again. We can use the backup to restore it in the key vault.
        Secret restoredSecret = client.restoreSecret(secretBackup).value();

    }
}

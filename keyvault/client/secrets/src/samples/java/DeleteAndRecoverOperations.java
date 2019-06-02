import com.azure.keyvault.SecretClient;
import com.azure.keyvault.models.Secret;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;

public class DeleteAndRecoverOperations {
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException {

        // Instantiate a client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        SecretClient client = SecretClient.builder()
            .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
            //.credentials(AzureCredential.DEFAULT)  TODO: Enable this, once Azure Identity Library merges.
            .build();

        // Let's create secrets holding storage and bank accounts credentials valid for 1 year. if the secret
        // already exists in the key vault, then a new version of the secret is created.
        client.setSecret(new Secret("STORAGE_ACCOUNT_PASSWORD", "f4G34fMh8v-fdsgjsk2323=-asdsdfsdf")
            .expires(OffsetDateTime.now().plusYears(1)));

        client.setSecret(new Secret("BANK_ACCOUNT_PASSWORD", "f4G34fMh8v")
            .expires(OffsetDateTime.now().plusYears(1)));

        // The storage account was closed, need to delete its credentials from the key vault.
        client.deleteSecret("BANK_ACCOUNT_PASSWORD");

        // We accidentally deleted bank account secret. Let's recover it.
        // A deleted secret can only be recovered if the key vault is soft-delete enabled.
        // client.recoverDeletedSecret("BANK_ACCOUNT_PASSWORD");

        // Let's delete storage account carefully.
        client.deleteSecret("STORAGE_ACCOUNT_PASSWORD");

        // To ensure permanent deletion, we might need to purge the secret.
        // If the keyvault is soft-delete enabled, then for permanent deletion  deleted secret needs to be purged.
        // client.purgeDeletedSecret("STORAGE_ACCOUNT_PASSWORD");
    }
}

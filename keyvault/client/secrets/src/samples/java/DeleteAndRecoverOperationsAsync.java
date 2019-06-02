import com.azure.keyvault.SecretAsyncClient;
import com.azure.keyvault.SecretClient;
import com.azure.keyvault.models.Secret;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;

public class DeleteAndRecoverOperationsAsync {
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, InterruptedException {

        // Instantiate a client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        SecretAsyncClient secretAsyncClient = SecretAsyncClient.builder()
                .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
                //.credentials(AzureCredential.DEFAULT)  TODO: Enable this, once Azure Identity Library merges.
                .build();

        // Let's create secrets holding storage and bank accounts credentials valid for 1 year. if the secret
        // already exists in the key vault, then a new version of the secret is created.
        secretAsyncClient.setSecret(new Secret("BANK_ACCOUNT_PASSWORD", "f4G34fMh8v")
                .expires(OffsetDateTime.now().plusYears(1))).subscribe(secretResponse ->
                System.out.printf("Secret is created with name %s and value %s \n", secretResponse.value().name(), secretResponse.value().value()));

        secretAsyncClient.setSecret(new Secret("STORAGE_ACCOUNT_PASSWORD", "f4G34fMh8v-fdsgjsk2323=-asdsdfsdf")
                .expires(OffsetDateTime.now().plusYears(1))).subscribe(secretResponse ->
                System.out.printf("Secret is created with name %s and value %s \n", secretResponse.value().name(), secretResponse.value().value()));

        // The storage account was closed, need to delete its credentials from the key vault.
        secretAsyncClient.deleteSecret("BANK_ACCOUNT_PASSWORD").subscribe(deletedSecretResponse ->
                System.out.printf("Deleted Secret's Recovery Id %s \n", deletedSecretResponse.value().recoveryId()));

        Thread.sleep(5000);

        // We accidentally deleted bank account secret. Let's recover it.
        // A deleted secret can only be recovered if the key vault is soft-delete enabled.
        // secretAsyncClient.recoverDeletedSecret("BANK_ACCOUNT_PASSWORD").subscribe(recoveredSecretResponse -&gt;
        //   System.out.printf("Recovered Secret with name %s \n", recoveredSecretResponse.value().name()));

        // Let's delete storage account carefully.
        secretAsyncClient.deleteSecret("STORAGE_ACCOUNT_PASSWORD").subscribe(deletedSecretResponse ->
          System.out.printf("Deleted Secret's Recovery Id %s \n", deletedSecretResponse.value().recoveryId()));

        Thread.sleep(5000);

        // If the keyvault is soft-delete enabled, then for permanent deletion  deleted secret needs to be purged.
        // secretAsyncClient.purgeDeletedSecret("STORAGE_ACCOUNT_PASSWORD").subscribe(purgeResponse ->
        //   System.out.printf("Purge Status response %d \n", purgeResponse.statusCode()));
    }
}

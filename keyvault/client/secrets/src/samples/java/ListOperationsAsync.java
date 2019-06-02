import com.azure.keyvault.SecretAsyncClient;
import com.azure.keyvault.models.Secret;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;

public class ListOperationsAsync {
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException {

        // Instantiate an async secret client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        SecretAsyncClient secretAsyncClient = SecretAsyncClient.builder()
                .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
                //.credentials(AzureCredential.DEFAULT)  TODO: Enable this, once Azure Identity Library merges.
                .build();

        // Let's create secrets holding storage and bank accounts credentials valid for 1 year. if the secret
        // already exists in the key vault, then a new version of the secret is created.
        secretAsyncClient.setSecret(new Secret("BANK_ACCOUNT_PASSWORD", "f4G34fMh8v")
                .expires(OffsetDateTime.now().plusYears(1)))
                .subscribe(secretResponse ->
                        System.out.printf("Secret is created with name %s and value %s \n", secretResponse.value().name(), secretResponse.value().value()));

        secretAsyncClient.setSecret(new Secret("STORAGE_ACCOUNT_PASSWORD", "f4G34fMh8v-fdsgjsk2323=-asdsdfsdf")
                .expires(OffsetDateTime.now().plusYears(1)))
                .subscribe(secretResponse ->
                        System.out.printf("Secret is created with name %s and value %s \n", secretResponse.value().name(), secretResponse.value().value()));

        // You need to check if any of the secrets are sharing same values. Let's list the secrets and print their values.
        // List operations don't return the secrets with value information. So, for each returned secret we call getSecret to get the secret with its value information.
        secretAsyncClient.listSecrets()
          .subscribe(secretBase ->
            secretAsyncClient.getSecret(secretBase).subscribe(secretResponse ->
                  System.out.printf("Received secret with name %s and value %s", secretResponse.value().name(), secretResponse.value().value())));

        // The bank account password got updated, so you want to update the secret in key vault to ensure it reflects the new password.
        // Calling setSecret on an existing secret creates a new version of the secret in the key vault with the new value.
        secretAsyncClient.setSecret(new Secret("BANK_ACCOUNT_PASSWORD", "sskdjfsdasdjsd")
          .expires(OffsetDateTime.now().plusYears(1))).subscribe(secretResponse ->
                System.out.printf("Secret is created with name %s and value %s \n", secretResponse.value().name(), secretResponse.value().value()));

        // You need to check all the different values your bank account password secret had previously. Lets print all the versions of this secret.
        secretAsyncClient.listSecretVersions("BANK_ACCOUNT_PASSWORD").subscribe(secretBase ->
            secretAsyncClient.getSecret(secretBase).subscribe(secretResponse ->
                System.out.printf("Received secret's version with name %s and value %s", secretResponse.value().name(), secretResponse.value().value())));

        // The bank acoount and storage accounts got closed.
        // Let's delete bank and  storage accounts secrets.
        secretAsyncClient.deleteSecret("BANK_ACCOUNT_PASSWORD").subscribe(deletedSecretResponse ->
          System.out.printf("Deleted Secret's Recovery Id %s \n", deletedSecretResponse.value().recoveryId()));

        secretAsyncClient.deleteSecret("STORAGE_ACCOUNT_PASSWORD").subscribe(deletedSecretResponse ->
          System.out.printf("Deleted Secret's Recovery Id %s \n", deletedSecretResponse.value().recoveryId()));

        // You can list all the deleted and non-purged secrets, assuming key vault is soft-delete enabled.
        secretAsyncClient.listDeletedSecrets().subscribe(deletedSecret ->
          System.out.printf("Deleted secret's recovery Id %s \n", deletedSecret.recoveryId()));

        // If the keyvault is soft-delete enabled, then for permanent deletion  deleted secrets need to be purged.
        // secretAsyncClient.purgeDeletedSecret("STORAGE_ACCOUNT_PASSWORD").subscribe(purgeResponse ->
        //   System.out.printf("Storage account secret purge status response %d \n", purgeResponse.statusCode()));

        // secretAsyncClient.purgeDeletedSecret("BANK_ACCOUNT_PASSWORD").subscribe(purgeResponse ->
        //   System.out.printf("Bank account secret purge status response %d \n", purgeResponse.statusCode()));

    }
}

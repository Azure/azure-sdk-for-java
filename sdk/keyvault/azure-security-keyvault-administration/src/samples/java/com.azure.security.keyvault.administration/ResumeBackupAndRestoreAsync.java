package com.azure.security.keyvault.administration;

import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreOperation;
import reactor.core.publisher.Mono;

/**
 * This sample demonstrates how to selectively restore a key from key vault backup asynchronously.
 */
public class ResumeBackupAndRestoreAsync {
    /**
     * Authenticates with the key vault and shows how to re-hydrate long running asynchronous operations such as backup
     * and restore.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when an invalid key vault URL is passed.
     */
    public static void main(String[] args) {
        /* Instantiate a KeyVaultAccessControlClient that will be used to call the service. Notice that the client is
        using default Azure credentials. To make default credentials work, ensure that environment variables
        'AZURE_CLIENT_ID', 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.

        To get started, you'll need a URI to an Azure Key Vault. See the README (https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/keyvault/azure-security-keyvault-administration/README.md)
        for links and instructions. */
        KeyVaultBackupAsyncClient backupAsyncClient = new KeyVaultBackupClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        /* The ID from a started backup operation on one client can be saved to persistent storage instead of waiting
        for completion immediately. */
        String blobStorageUrl = "<blob-storage-url>";
        String sasToken = "<sas-token>";

        PollerFlux<KeyVaultBackupOperation, String> originalBackupPollerFlux =
            backupAsyncClient.beginBackup(blobStorageUrl, sasToken);

        String backupJobId = originalBackupPollerFlux.blockFirst().getValue().getJobId();

        /* At some later time, another client can use the persisted operation ID, check the status and retrieve the
        result of a previously started backup operation. */
        Mono<AsyncPollResponse<KeyVaultBackupOperation, String>> backupMono =
            backupAsyncClient.getBackupOperation(backupJobId)
                .doOnNext(pollResponse -> {
                    System.out.println("-----------------------------------------------------------------------------");
                    System.out.println(pollResponse.getStatus());
                    System.out.println(pollResponse.getValue().getStatus());
                    System.out.println(pollResponse.getValue().getStatusDetails());
                })
                .last();

        // Get the backup URI to begin a restore operation at some point.
        String backupFolderUrl = backupMono.block().getValue().getAzureStorageBlobContainerUri();

        PollerFlux<KeyVaultRestoreOperation, Void> originalRestorePollerFlux =
            backupAsyncClient.beginRestore(backupFolderUrl, sasToken);

        String restoreJobId = originalRestorePollerFlux.blockFirst().getValue().getJobId();

        /* Similarly to as with backup operations, you can check the status and retrieve the result of a previously
        started restore operation. */
        backupAsyncClient.getRestoreOperation(restoreJobId)
            .doOnNext(pollResponse -> {
                System.out.println("-----------------------------------------------------------------------------");
                System.out.println(pollResponse.getStatus());
                System.out.println(pollResponse.getValue().getStatus());
                System.out.println(pollResponse.getValue().getStatusDetails());
            })
            .blockLast();

        /* NOTE: block(), blockFirst() and blockLast() will block until all the above operations on are completed. This
        is strongly discouraged for use in production as it eliminates the benefits of asynchronous IO. It is used here
        for the sake of explaining where to get the jobId and backupFolderUrl parameters and to ensure the sample runs
        to completion. */
    }
}

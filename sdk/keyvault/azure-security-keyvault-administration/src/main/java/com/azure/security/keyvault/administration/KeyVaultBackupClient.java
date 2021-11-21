// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.administration.models.KeyVaultAdministrationException;
import com.azure.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreResult;
import com.azure.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreOperation;
import com.azure.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreResult;

/**
 * The {@link KeyVaultBackupClient} provides synchronous methods to perform backup and restore operations of an Azure
 * Key Vault.
 *
 * <p>Instances of this client are obtained by calling the {@link KeyVaultBackupClientBuilder#buildClient()}
 * method on a {@link KeyVaultBackupClientBuilder} object.</p>
 *
 * <p><strong>Samples to construct a sync client</strong></p>
 * <!-- src_embed com.azure.security.keyvault.administration.keyVaultBackupClient.instantiation -->
 * <pre>
 * KeyVaultBackupClient keyVaultBackupClient = new KeyVaultBackupClientBuilder&#40;&#41;
 *     .vaultUrl&#40;&quot;https:&#47;&#47;myaccount.managedhsm.azure.net&#47;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.keyVaultBackupClient.instantiation -->
 *
 * @see KeyVaultBackupClientBuilder
 */
@ServiceClient(builder = KeyVaultBackupClientBuilder.class)
public final class KeyVaultBackupClient {
    private final KeyVaultBackupAsyncClient asyncClient;

    /**
     * Creates an {@link KeyVaultBackupClient} that uses a {@code pipeline} to service requests
     *
     * @param asyncClient The {@link KeyVaultBackupAsyncClient} that the client routes its request through.
     */
    KeyVaultBackupClient(KeyVaultBackupAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Get the vault endpoint URL.
     *
     * @return The vault endpoint URL.
     */
    public String getVaultUrl() {
        return asyncClient.getVaultUrl();
    }

    /**
     * Initiates a full backup of the Key Vault.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Starts a {@link KeyVaultBackupOperation backup operation}, polls for its status and waits for it to complete.
     * Prints out the details of the operation's final result in case of success or prints out error details in case the
     * operation fails.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultBackupClient.beginBackup#String-String -->
     * <pre>
     * String blobStorageUrl = &quot;https:&#47;&#47;myaccount.blob.core.windows.net&#47;myContainer&quot;;
     * String sasToken = &quot;sv=2020-02-10&amp;ss=b&amp;srt=o&amp;sp=rwdlactfx&amp;se=2021-06-17T07:13:07Z&amp;st=2021-06-16T23:13:07Z&quot;
     *     + &quot;&amp;spr=https&amp;sig=n5V6fnlkViEF9b7ij%2FttTHNwO2BdFIHKHppRxGAyJdc%3D&quot;;
     *
     * SyncPoller&lt;KeyVaultBackupOperation, String&gt; backupPoller = client.beginBackup&#40;blobStorageUrl, sasToken&#41;;
     *
     * PollResponse&lt;KeyVaultBackupOperation&gt; pollResponse = backupPoller.poll&#40;&#41;;
     *
     * System.out.printf&#40;&quot;The current status of the operation is: %s.%n&quot;, pollResponse.getStatus&#40;&#41;&#41;;
     *
     * PollResponse&lt;KeyVaultBackupOperation&gt; finalPollResponse = backupPoller.waitForCompletion&#40;&#41;;
     *
     * if &#40;finalPollResponse.getStatus&#40;&#41; == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41; &#123;
     *     String folderUrl = backupPoller.getFinalResult&#40;&#41;;
     *
     *     System.out.printf&#40;&quot;Backup completed. The storage location of this backup is: %s.%n&quot;, folderUrl&#41;;
     * &#125; else &#123;
     *     KeyVaultBackupOperation operation = backupPoller.poll&#40;&#41;.getValue&#40;&#41;;
     *
     *     System.out.printf&#40;&quot;Backup failed with error: %s.%n&quot;, operation.getError&#40;&#41;.getMessage&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultBackupClient.beginBackup#String-String -->
     *
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup will be located.
     * @param sasToken A Shared Access Signature (SAS) token to authorize access to the blob.
     *
     * @return A {@link SyncPoller} polling on the {@link KeyVaultBackupOperation backup operation} status.
     *
     * @throws KeyVaultAdministrationException If the given {@code blobStorageUrl} or {@code sasToken} are invalid.
     * @throws NullPointerException If the {@code blobStorageUrl} or {@code sasToken} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<KeyVaultBackupOperation, String> beginBackup(String blobStorageUrl, String sasToken) {
        return asyncClient.beginBackup(blobStorageUrl, sasToken).getSyncPoller();
    }

    /**
     * Initiates a full restore of the Key Vault.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Starts a {@link KeyVaultRestoreOperation restore operation}, polls for its status and waits for it to
     * complete. Prints out error details in case the operation fails.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultBackupClient.beginBackup#String-String -->
     * <pre>
     * String blobStorageUrl = &quot;https:&#47;&#47;myaccount.blob.core.windows.net&#47;myContainer&quot;;
     * String sasToken = &quot;sv=2020-02-10&amp;ss=b&amp;srt=o&amp;sp=rwdlactfx&amp;se=2021-06-17T07:13:07Z&amp;st=2021-06-16T23:13:07Z&quot;
     *     + &quot;&amp;spr=https&amp;sig=n5V6fnlkViEF9b7ij%2FttTHNwO2BdFIHKHppRxGAyJdc%3D&quot;;
     *
     * SyncPoller&lt;KeyVaultBackupOperation, String&gt; backupPoller = client.beginBackup&#40;blobStorageUrl, sasToken&#41;;
     *
     * PollResponse&lt;KeyVaultBackupOperation&gt; pollResponse = backupPoller.poll&#40;&#41;;
     *
     * System.out.printf&#40;&quot;The current status of the operation is: %s.%n&quot;, pollResponse.getStatus&#40;&#41;&#41;;
     *
     * PollResponse&lt;KeyVaultBackupOperation&gt; finalPollResponse = backupPoller.waitForCompletion&#40;&#41;;
     *
     * if &#40;finalPollResponse.getStatus&#40;&#41; == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41; &#123;
     *     String folderUrl = backupPoller.getFinalResult&#40;&#41;;
     *
     *     System.out.printf&#40;&quot;Backup completed. The storage location of this backup is: %s.%n&quot;, folderUrl&#41;;
     * &#125; else &#123;
     *     KeyVaultBackupOperation operation = backupPoller.poll&#40;&#41;.getValue&#40;&#41;;
     *
     *     System.out.printf&#40;&quot;Backup failed with error: %s.%n&quot;, operation.getError&#40;&#41;.getMessage&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultBackupClient.beginBackup#String-String -->
     *
     * @param folderUrl The URL for the Blob Storage resource where the backup is located, including the path to
     * the blob container where the backup resides. This would be the exact value that is returned as the result of a
     * backup operation. An example of such a URL may look like the following:
     * https://contoso.blob.core.windows.net/backup/mhsm-contoso-2020090117323313.
     * @param sasToken A Shared Access Signature (SAS) token to authorize access to the blob.
     *
     * @return A {@link SyncPoller} to poll on the {@link KeyVaultRestoreOperation restore operation} status.
     *
     * @throws KeyVaultAdministrationException If the given {@code folderUrl} or {@code sasToken} are invalid.
     * @throws NullPointerException If the {@code folderUrl} or {@code sasToken} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<KeyVaultRestoreOperation, KeyVaultRestoreResult> beginRestore(String folderUrl, String sasToken) {
        return asyncClient.beginRestore(folderUrl, sasToken).getSyncPoller();
    }

    /**
     * Restores all versions of a given key using the supplied SAS token pointing to a previously stored Azure Blob
     * storage backup folder.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Starts a {@link KeyVaultSelectiveKeyRestoreOperation selective key restore operation}, polls for its status
     * and waits for it to complete. Prints out error details in case the operation fails.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultBackupClient.beginSelectiveKeyRestore#String-String-String -->
     * <pre>
     * String folderUrl = &quot;https:&#47;&#47;myaccount.blob.core.windows.net&#47;myContainer&#47;mhsm-myaccount-2020090117323313&quot;;
     * String sasToken = &quot;sv=2020-02-10&amp;ss=b&amp;srt=o&amp;sp=rwdlactfx&amp;se=2021-06-17T07:13:07Z&amp;st=2021-06-16T23:13:07Z&quot;
     *     + &quot;&amp;spr=https&amp;sig=n5V6fnlkViEF9b7ij%2FttTHNwO2BdFIHKHppRxGAyJdc%3D&quot;;
     * String keyName = &quot;myKey&quot;;
     *
     * SyncPoller&lt;KeyVaultSelectiveKeyRestoreOperation, KeyVaultSelectiveKeyRestoreResult&gt; backupPoller =
     *     client.beginSelectiveKeyRestore&#40;folderUrl, sasToken, keyName&#41;;
     *
     * PollResponse&lt;KeyVaultSelectiveKeyRestoreOperation&gt; pollResponse = backupPoller.poll&#40;&#41;;
     *
     * System.out.printf&#40;&quot;The current status of the operation is: %s.%n&quot;, pollResponse.getStatus&#40;&#41;&#41;;
     *
     * PollResponse&lt;KeyVaultSelectiveKeyRestoreOperation&gt; finalPollResponse = backupPoller.waitForCompletion&#40;&#41;;
     *
     * if &#40;finalPollResponse.getStatus&#40;&#41; == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41; &#123;
     *     System.out.printf&#40;&quot;Key restored successfully.%n&quot;&#41;;
     * &#125; else &#123;
     *     KeyVaultSelectiveKeyRestoreOperation operation = backupPoller.poll&#40;&#41;.getValue&#40;&#41;;
     *
     *     System.out.printf&#40;&quot;Key restore failed with error: %s.%n&quot;, operation.getError&#40;&#41;.getMessage&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultBackupClient.beginSelectiveKeyRestore#String-String-String -->
     *
     * @param keyName The name of the key to be restored.
     * @param folderUrl The URL for the Blob Storage resource where the backup is located, including the path to
     * the blob container where the backup resides. This would be the exact value that is returned as the result of a
     * backup operation. An example of such a URL may look like the following:
     * https://contoso.blob.core.windows.net/backup/mhsm-contoso-2020090117323313.
     * @param sasToken A Shared Access Signature (SAS) token to authorize access to the blob.
     *
     * @return A {@link SyncPoller} to poll on the {@link KeyVaultRestoreOperation restore operation} status.
     *
     * @throws KeyVaultAdministrationException If the given {@code folderUrl} or {@code sasToken} are invalid.
     * @throws NullPointerException If the {@code keyName}, {@code folderUrl} or {@code sasToken} are {@code
     * null}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<KeyVaultSelectiveKeyRestoreOperation, KeyVaultSelectiveKeyRestoreResult> beginSelectiveKeyRestore(String keyName, String folderUrl, String sasToken) {
        return asyncClient.beginSelectiveKeyRestore(keyName, folderUrl, sasToken).getSyncPoller();
    }
}

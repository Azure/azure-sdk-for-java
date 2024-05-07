// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.administration.implementation.KeyVaultBackupClientImpl;
import com.azure.security.keyvault.administration.implementation.KeyVaultErrorCodeStrings;
import com.azure.security.keyvault.administration.implementation.models.FullBackupHeaders;
import com.azure.security.keyvault.administration.implementation.models.FullBackupOperation;
import com.azure.security.keyvault.administration.implementation.models.FullRestoreOperationHeaders;
import com.azure.security.keyvault.administration.implementation.models.RestoreOperation;
import com.azure.security.keyvault.administration.implementation.models.RestoreOperationParameters;
import com.azure.security.keyvault.administration.implementation.models.SASTokenParameter;
import com.azure.security.keyvault.administration.implementation.models.SelectiveKeyRestoreOperation;
import com.azure.security.keyvault.administration.implementation.models.SelectiveKeyRestoreOperationHeaders;
import com.azure.security.keyvault.administration.implementation.models.SelectiveKeyRestoreOperationParameters;
import com.azure.security.keyvault.administration.models.KeyVaultAdministrationException;
import com.azure.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreResult;
import com.azure.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreOperation;
import com.azure.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreResult;

import java.net.URL;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.security.keyvault.administration.KeyVaultAdministrationUtil.enableSyncRestProxy;
import static com.azure.security.keyvault.administration.KeyVaultAdministrationUtil.toLongRunningOperationStatus;
import static com.azure.security.keyvault.administration.KeyVaultAdministrationUtil.transformToLongRunningOperation;
import static com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.restoreOperationToSelectiveKeyRestoreOperation;

/**
 * The {@link KeyVaultBackupClient} provides synchronous methods to perform full a backup and restore of a key vault,
 * as well as selectively restoring specific keys from a backup.
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Azure Key Vault service, you will need to create an instance of the
 * {@link KeyVaultBackupClient} class, a vault url and a credential object.</p>
 *
 * <p>The examples shown in this document use a credential object named DefaultAzureCredential for authentication,
 * which is appropriate for most scenarios, including local development and production environments. Additionally,
 * we recommend using a
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">
 * managed identity</a> for authentication in production environments.
 * You can find more information on different ways of authenticating and their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">
 * Azure Identity documentation"</a>.</p>
 *
 * <p><strong>Sample: Construct Asynchronous Backup Client</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link KeyVaultBackupClient}, using the
 * {@link KeyVaultBackupClientBuilder} to configure it.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultBackupClient.instantiation -->
 * <pre>
 * KeyVaultBackupClient keyVaultBackupClient = new KeyVaultBackupClientBuilder&#40;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-managed-hsm-url&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultBackupClient.instantiation -->
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Back Up a Collection of Keys</h2>
 * The {@link KeyVaultBackupClient} can be used to back up the entire collection of keys from a key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously back up an entire collection of keys using, using the
 * {@link KeyVaultBackupClient#beginBackup(String, String)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultBackupClient.beginBackup#String-String -->
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
 * <!-- end com.azure.security.keyvault.administration.KeyVaultBackupClient.beginBackup#String-String -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to {@link KeyVaultBackupAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Restore a Collection of Keys</h2>
 * The {@link KeyVaultBackupClient} can be used to restore an entire collection of keys from a backup.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously restore an entire collection of keys from a backup,
 * using the {@link KeyVaultBackupClient#beginRestore(String, String)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultBackupClient.beginRestore#String-String -->
 * <pre>
 * String folderUrl = &quot;https:&#47;&#47;myaccount.blob.core.windows.net&#47;myContainer&#47;mhsm-myaccount-2020090117323313&quot;;
 * String sasToken = &quot;sv=2020-02-10&amp;ss=b&amp;srt=o&amp;sp=rwdlactfx&amp;se=2021-06-17T07:13:07Z&amp;st=2021-06-16T23:13:07Z&quot;
 *     + &quot;&amp;spr=https&amp;sig=n5V6fnlkViEF9b7ij%2FttTHNwO2BdFIHKHppRxGAyJdc%3D&quot;;
 *
 * SyncPoller&lt;KeyVaultRestoreOperation, KeyVaultRestoreResult&gt; backupPoller =
 *     client.beginRestore&#40;folderUrl, sasToken&#41;;
 *
 * PollResponse&lt;KeyVaultRestoreOperation&gt; pollResponse = backupPoller.poll&#40;&#41;;
 *
 * System.out.printf&#40;&quot;The current status of the operation is: %s.%n&quot;, pollResponse.getStatus&#40;&#41;&#41;;
 *
 * PollResponse&lt;KeyVaultRestoreOperation&gt; finalPollResponse = backupPoller.waitForCompletion&#40;&#41;;
 *
 * if &#40;finalPollResponse.getStatus&#40;&#41; == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41; &#123;
 *     System.out.printf&#40;&quot;Backup restored successfully.%n&quot;&#41;;
 * &#125; else &#123;
 *     KeyVaultRestoreOperation operation = backupPoller.poll&#40;&#41;.getValue&#40;&#41;;
 *
 *     System.out.printf&#40;&quot;Restore failed with error: %s.%n&quot;, operation.getError&#40;&#41;.getMessage&#40;&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultBackupClient.beginRestore#String-String -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to {@link KeyVaultBackupAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Selectively Restore a Key</h2>
 * The {@link KeyVaultBackupClient} can be used to restore a specific key from a backup.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously restore a specific key from a backup, using
 * the {@link KeyVaultBackupClient#beginSelectiveKeyRestore(String, String, String)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultBackupClient.beginSelectiveKeyRestore#String-String-String -->
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
 * <!-- end com.azure.security.keyvault.administration.KeyVaultBackupClient.beginSelectiveKeyRestore#String-String-String -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to {@link KeyVaultBackupAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * @see com.azure.security.keyvault.administration
 * @see KeyVaultBackupClientBuilder
 */
@ServiceClient(builder = KeyVaultBackupClientBuilder.class)
public final class KeyVaultBackupClient {
    /**
     * The logger to be used.
     */
    private static final ClientLogger LOGGER = new ClientLogger(KeyVaultBackupClient.class);

    private static final Duration DEFAULT_POLLING_INTERVAL = Duration.ofSeconds(1);

    private final KeyVaultBackupAsyncClient asyncClient;

    /**
     * The underlying AutoRest client used to interact with the Key Vault service.
     */
    private final KeyVaultBackupClientImpl clientImpl;

    /**
     * The Key Vault URL this client is associated to.
     */
    private final String vaultUrl;

    /**
     * The Key Vault Administration Service version to use with this client.
     */
    private final String serviceVersion;

    /**
     * The {@link HttpPipeline} powering this client.
     */
    private final HttpPipeline pipeline;

    Duration getDefaultPollingInterval() {
        return DEFAULT_POLLING_INTERVAL;
    }

    /**
     * Package private constructor to be used by {@link KeyVaultBackupClientBuilder}.
     */
    KeyVaultBackupClient(URL vaultUrl, HttpPipeline httpPipeline,
                              KeyVaultAdministrationServiceVersion serviceVersion) {
        Objects.requireNonNull(vaultUrl, KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED);

        this.vaultUrl = vaultUrl.toString();
        this.serviceVersion = serviceVersion.getVersion();
        this.pipeline = httpPipeline;
        this.asyncClient = null;

        clientImpl = new KeyVaultBackupClientImpl(httpPipeline, this.serviceVersion);
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
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultBackupClient.beginBackup#String-String -->
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
     * <!-- end com.azure.security.keyvault.administration.KeyVaultBackupClient.beginBackup#String-String -->
     *
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup will be located.
     * @param sasToken Optional Shared Access Signature (SAS) token to authorize access to the blob. If {@code null},
     * Managed Identity will be used to authenticate instead.
     *
     * @return A {@link SyncPoller} polling on the {@link KeyVaultBackupOperation backup operation} status.
     *
     * @throws KeyVaultAdministrationException If the given {@code blobStorageUrl} or {@code sasToken} are invalid.
     * @throws NullPointerException If the {@code blobStorageUrl} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<KeyVaultBackupOperation, String> beginBackup(String blobStorageUrl, String sasToken) {
        if (blobStorageUrl == null) {
            throw LOGGER.logExceptionAsError(
                new NullPointerException(
                    String.format(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED, "'blobStorageUrl'")));
        }

        Context context = Context.NONE;
        return SyncPoller.createPoller(
            getDefaultPollingInterval(),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, backupActivationOperation(blobStorageUrl, sasToken, context).apply(cxt)),
            backupPollOperation(context),
            (pollingContext, firstResponse) -> {
                throw LOGGER.logExceptionAsError(new RuntimeException("Cancellation is not supported"));
            },
            backupFetchOperation());
    }

    /**
     * Initiates a full backup of the Key Vault.
     *
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup will be located.
     * @param sasToken Optional Shared Access Signature (SAS) token to authorize access to the blob. If {@code null},
     * Managed Identity will be used to authenticate instead.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link KeyVaultBackupOperation backup operation} status.
     *
     * @throws KeyVaultAdministrationException If the given {@code blobStorageUrl} or {@code sasToken} are invalid.
     */
    Response<KeyVaultBackupOperation> backupWithResponse(String blobStorageUrl, String sasToken, Context context) {
        SASTokenParameter sasTokenParameter = new SASTokenParameter(blobStorageUrl)
            .setToken(sasToken)
            .setUseManagedIdentity(sasToken == null);
        context = enableSyncRestProxy(context);

        try {
            ResponseBase<FullBackupHeaders, FullBackupOperation> backupOperationResponse =
                clientImpl.fullBackupWithResponse(vaultUrl, sasTokenParameter, context);
            return new SimpleResponse<>(backupOperationResponse.getRequest(), backupOperationResponse.getStatusCode(),
                backupOperationResponse.getHeaders(),
                (KeyVaultBackupOperation) transformToLongRunningOperation(backupOperationResponse.getValue()));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    private Function<PollingContext<KeyVaultBackupOperation>, KeyVaultBackupOperation> backupActivationOperation(
        String blobStorageUrl, String sasToken, Context context) {
        return (pollingContext) -> {
            try {
                return backupWithResponse(blobStorageUrl, sasToken, context).getValue();
            } catch (RuntimeException e) {
                throw LOGGER.logExceptionAsError(e);
            }
        };
    }

    private Function<PollingContext<KeyVaultBackupOperation>, PollResponse<KeyVaultBackupOperation>> backupPollOperation(Context context) {
        return (pollingContext) -> {
            try {
                PollResponse<KeyVaultBackupOperation> pollResponse = pollingContext.getLatestResponse();

                if (pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
                    || pollResponse.getStatus() == LongRunningOperationStatus.FAILED) {
                    return pollResponse;
                }

                final KeyVaultBackupOperation keyVaultBackupOperation = pollResponse.getValue();

                if (keyVaultBackupOperation == null) {
                    LOGGER.warning("Backup operation does not exist. Activation operation failed.");
                    return new PollResponse<KeyVaultBackupOperation>(
                        LongRunningOperationStatus.fromString("BACKUP_START_FAILED", true), null);
                }

                final String jobId = keyVaultBackupOperation.getOperationId();
                Context contextToUse = enableSyncRestProxy(context);

                Response<FullBackupOperation> backupOperationResponse = clientImpl.fullBackupStatusWithResponse(vaultUrl, jobId,
                    contextToUse);
                return processBackupOperationResponse(new SimpleResponse<>(backupOperationResponse,
                            (KeyVaultBackupOperation) transformToLongRunningOperation(backupOperationResponse.getValue())));
            } catch (HttpResponseException e) {
                //noinspection ThrowableNotThrown
                LOGGER.logExceptionAsError(e);
                return new PollResponse<>(LongRunningOperationStatus.FAILED, null);
            } catch (RuntimeException e) {
                throw LOGGER.logExceptionAsError(e);
            }
        };
    }

    private Function<PollingContext<KeyVaultBackupOperation>, String> backupFetchOperation() {
        return (pollingContext) -> {
            try {
                return pollingContext.getLatestResponse().getValue().getAzureStorageBlobContainerUrl();
            } catch (RuntimeException e) {
                throw LOGGER.logExceptionAsError(e);
            }
        };
    }

    private static PollResponse<KeyVaultBackupOperation> processBackupOperationResponse(
        Response<KeyVaultBackupOperation> response) {
        String operationStatus = response.getValue().getStatus().toLowerCase(Locale.US);
        return new PollResponse<>(
            toLongRunningOperationStatus(operationStatus.toLowerCase(Locale.US)), response.getValue());
    }

    /**
     * Initiates a full restore of the Key Vault.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Starts a {@link KeyVaultRestoreOperation restore operation}, polls for its status and waits for it to
     * complete. Prints out error details in case the operation fails.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultBackupClient.beginBackup#String-String -->
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
     * <!-- end com.azure.security.keyvault.administration.KeyVaultBackupClient.beginBackup#String-String -->
     *
     * @param folderUrl The URL for the Blob Storage resource where the backup is located, including the path to
     * the blob container where the backup resides. This would be the exact value that is returned as the result of a
     * backup operation. An example of such a URL may look like the following:
     * https://contoso.blob.core.windows.net/backup/mhsm-contoso-2020090117323313.
     * @param sasToken Optional Shared Access Signature (SAS) token to authorize access to the blob. If {@code null},
     * Managed Identity will be used to authenticate instead.
     *
     * @return A {@link SyncPoller} to poll on the {@link KeyVaultRestoreOperation restore operation} status.
     *
     * @throws KeyVaultAdministrationException If the given {@code folderUrl} or {@code sasToken} are invalid.
     * @throws NullPointerException If the {@code folderUrl} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<KeyVaultRestoreOperation, KeyVaultRestoreResult> beginRestore(String folderUrl, String sasToken) {
        if (folderUrl == null) {
            throw LOGGER.logExceptionAsError(
                new NullPointerException(String.format(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED, "'folderUrl'")));
        }

        Context context = Context.NONE;

        return SyncPoller.createPoller(getDefaultPollingInterval(),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, restoreActivationOperation(folderUrl, sasToken, context).apply(cxt)),
            restorePollOperation(context),
            (pollingContext, firstResponse) -> {
                throw LOGGER.logExceptionAsError(new RuntimeException("Cancellation is not supported"));
            },
            (pollingContext) -> new KeyVaultRestoreResult());
    }

    /**
     * Initiates a full restore of the Key Vault.
     *
     * @param folderUrl The URL for the Blob Storage resource where the backup is located, including the path to
     * the blob container where the backup resides. This would be the exact value that is returned as the result of a
     * backup operation. An example of such a URL may look like the following:
     * https://contoso.blob.core.windows.net/backup/mhsm-contoso-2020090117323313.
     * @param sasToken Optional Shared Access Signature (SAS) token to authorize access to the blob. If {@code null},
     * Managed Identity will be used to authenticate instead.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link KeyVaultRestoreOperation backup operation} status.
     *
     * @throws KeyVaultAdministrationException If the given {@code folderUrl} or {@code sasToken} are invalid.
     */
    Response<KeyVaultRestoreOperation> restoreWithResponse(String folderUrl, String sasToken, Context context) {
        String[] segments = folderUrl.split("/");
        String folderName = segments[segments.length - 1];
        String containerUrl = folderUrl.substring(0, folderUrl.length() - folderName.length());

        SASTokenParameter sasTokenParameter = new SASTokenParameter(containerUrl)
            .setToken(sasToken)
            .setUseManagedIdentity(sasToken == null);
        RestoreOperationParameters restoreOperationParameters =
            new RestoreOperationParameters(sasTokenParameter, folderName);
        context = enableSyncRestProxy(context);

        try {
            ResponseBase<FullRestoreOperationHeaders, RestoreOperation> restoreOperationResponse =
                clientImpl.fullRestoreOperationWithResponse(vaultUrl, restoreOperationParameters, context);
            return new SimpleResponse<>(restoreOperationResponse.getRequest(),
                restoreOperationResponse.getStatusCode(),
                restoreOperationResponse.getHeaders(),
                (KeyVaultRestoreOperation) transformToLongRunningOperation(restoreOperationResponse.getValue()));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    private Function<PollingContext<KeyVaultRestoreOperation>, KeyVaultRestoreOperation> restoreActivationOperation(String folderUrl, String sasToken, Context context) {
        return (pollingContext) -> {
            try {
                return restoreWithResponse(folderUrl, sasToken, context).getValue();
            } catch (RuntimeException e) {
                throw LOGGER.logExceptionAsError(e);
            }
        };
    }

    private Function<PollingContext<KeyVaultRestoreOperation>, PollResponse<KeyVaultRestoreOperation>> restorePollOperation(Context context) {
        return (pollingContext) -> {
            try {
                PollResponse<KeyVaultRestoreOperation> pollResponse = pollingContext.getLatestResponse();

                if (pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
                    || pollResponse.getStatus() == LongRunningOperationStatus.FAILED) {

                    return pollResponse;
                }

                final KeyVaultRestoreOperation keyVaultRestoreOperation = pollResponse.getValue();

                if (keyVaultRestoreOperation == null) {
                    LOGGER.warning("Restore operation does not exist. Activation operation failed.");

                    return new PollResponse<>(LongRunningOperationStatus.fromString("RESTORE_START_FAILED", true), null);
                }

                final String jobId = keyVaultRestoreOperation.getOperationId();
                Context contextToUse = enableSyncRestProxy(context);

                Response<RestoreOperation> response = clientImpl.restoreStatusWithResponse(vaultUrl, jobId,
                    contextToUse);
                return processRestoreOperationResponse(new SimpleResponse<>(response,
                            (KeyVaultRestoreOperation) transformToLongRunningOperation(response.getValue())));

            } catch (HttpResponseException e) {
                //noinspection ThrowableNotThrown
                LOGGER.logExceptionAsError(e);
                return new PollResponse<>(LongRunningOperationStatus.FAILED, null);
            } catch (RuntimeException e) {
                throw LOGGER.logExceptionAsError(e);
            }
        };
    }

    private static PollResponse<KeyVaultRestoreOperation> processRestoreOperationResponse(
        Response<KeyVaultRestoreOperation> response) {
        String operationStatus = response.getValue().getStatus().toLowerCase(Locale.US);
        return new PollResponse<>(
            toLongRunningOperationStatus(operationStatus.toLowerCase(Locale.US)), response.getValue());
    }

    /**
     * Restores all versions of a given key using the supplied SAS token pointing to a previously stored Azure Blob
     * storage backup folder.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Starts a {@link KeyVaultSelectiveKeyRestoreOperation selective key restore operation}, polls for its status
     * and waits for it to complete. Prints out error details in case the operation fails.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultBackupClient.beginSelectiveKeyRestore#String-String-String -->
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
     * <!-- end com.azure.security.keyvault.administration.KeyVaultBackupClient.beginSelectiveKeyRestore#String-String-String -->
     *
     * @param keyName The name of the key to be restored.
     * @param folderUrl The URL for the Blob Storage resource where the backup is located, including the path to
     * the blob container where the backup resides. This would be the exact value that is returned as the result of a
     * backup operation. An example of such a URL may look like the following:
     * https://contoso.blob.core.windows.net/backup/mhsm-contoso-2020090117323313.
     * @param sasToken Optional Shared Access Signature (SAS) token to authorize access to the blob. If {@code null},
     * Managed Identity will be used to authenticate instead.
     *
     * @return A {@link SyncPoller} to poll on the {@link KeyVaultRestoreOperation restore operation} status.
     *
     * @throws KeyVaultAdministrationException If the given {@code folderUrl} or {@code sasToken} are invalid.
     * @throws NullPointerException If the {@code keyName} or {@code folderUrl} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<KeyVaultSelectiveKeyRestoreOperation, KeyVaultSelectiveKeyRestoreResult> beginSelectiveKeyRestore(String keyName, String folderUrl, String sasToken) {
        if (keyName == null) {
            throw LOGGER.logExceptionAsError(
                new NullPointerException(String.format(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED, "'keyName'")));
        }

        if (folderUrl == null) {
            throw LOGGER.logExceptionAsError(
                new NullPointerException(String.format(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED, "'folderUrl'")));
        }

        Context context = Context.NONE;

        return SyncPoller.createPoller(getDefaultPollingInterval(),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, selectiveKeyRestoreActivationOperation(keyName, folderUrl, sasToken, context).apply(cxt)),
            selectiveKeyRestorePollOperation(context),
            (pollingContext, firstResponse) -> {
                throw LOGGER.logExceptionAsError(new RuntimeException("Cancellation is not supported"));
            },
            (pollingContext) -> new KeyVaultSelectiveKeyRestoreResult());
    }

    /**
     * Restores all versions of a given key using the supplied SAS token pointing to a previously stored Azure Blob
     * storage backup folder.
     *
     * @param keyName The name of the key to be restored.
     * @param folderUrl The URL for the Blob Storage resource where the backup is located, including the path to
     * the blob container where the backup resides. This would be the exact value that is returned as the result of a
     * backup operation. An example of such a URL may look like the following:
     * https://contoso.blob.core.windows.net/backup/mhsm-contoso-2020090117323313.
     * @param sasToken Optional Shared Access Signature (SAS) token to authorize access to the blob. If {@code null},
     * Managed Identity will be used to authenticate instead.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link KeyVaultSelectiveKeyRestoreOperation backup operation} status.
     */
    Response<KeyVaultSelectiveKeyRestoreOperation> selectiveKeyRestoreWithResponse(String keyName, String folderUrl,
                                                                                   String sasToken, Context context) {
        String[] segments = folderUrl.split("/");
        String folderName = segments[segments.length - 1];
        String containerUrl = folderUrl.substring(0, folderUrl.length() - folderName.length());

        SASTokenParameter sasTokenParameter = new SASTokenParameter(containerUrl)
            .setToken(sasToken)
            .setUseManagedIdentity(sasToken == null);
        SelectiveKeyRestoreOperationParameters selectiveKeyRestoreOperationParameters =
            new SelectiveKeyRestoreOperationParameters(sasTokenParameter, folderName);
        context = enableSyncRestProxy(context);

        try {
            ResponseBase<SelectiveKeyRestoreOperationHeaders, SelectiveKeyRestoreOperation> restoreOperationResponse =
                clientImpl.selectiveKeyRestoreOperationWithResponse(vaultUrl, keyName,
                    selectiveKeyRestoreOperationParameters, context);
            return new SimpleResponse<>(restoreOperationResponse.getRequest(),
                restoreOperationResponse.getStatusCode(),
                restoreOperationResponse.getHeaders(),
                (KeyVaultSelectiveKeyRestoreOperation) transformToLongRunningOperation(
                    restoreOperationResponse.getValue()));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    private Function<PollingContext<KeyVaultSelectiveKeyRestoreOperation>, KeyVaultSelectiveKeyRestoreOperation> selectiveKeyRestoreActivationOperation(String keyName, String folderUrl, String sasToken, Context context) {
        return (pollingContext) -> {
            try {
                return selectiveKeyRestoreWithResponse(keyName, folderUrl, sasToken, context).getValue();
            } catch (RuntimeException e) {
                throw LOGGER.logExceptionAsError(e);
            }
        };
    }

    private Function<PollingContext<KeyVaultSelectiveKeyRestoreOperation>, PollResponse<KeyVaultSelectiveKeyRestoreOperation>> selectiveKeyRestorePollOperation(Context context) {
        return (pollingContext) -> {
            try {
                PollResponse<KeyVaultSelectiveKeyRestoreOperation> pollResponse = pollingContext.getLatestResponse();

                if (pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
                    || pollResponse.getStatus() == LongRunningOperationStatus.FAILED) {
                    return pollResponse;
                }

                final KeyVaultSelectiveKeyRestoreOperation keyVaultSelectiveKeyRestoreOperation = pollResponse.getValue();

                if (keyVaultSelectiveKeyRestoreOperation == null) {
                    LOGGER.warning("Restore operation does not exist. Activation operation failed.");

                    return new PollResponse<>(LongRunningOperationStatus.fromString("SELECTIVE_RESTORE_START_FAILED", true), null);
                }
                final String jobId = keyVaultSelectiveKeyRestoreOperation.getOperationId();
                Context contextToUse = enableSyncRestProxy(context);
                Response<RestoreOperation> response = clientImpl.restoreStatusWithResponse(vaultUrl, jobId,
                    contextToUse);
                return processSelectiveKeyRestoreOperationResponse(new SimpleResponse<>(response,
                        (KeyVaultSelectiveKeyRestoreOperation) restoreOperationToSelectiveKeyRestoreOperation(response.getValue())));
            } catch (HttpResponseException e) {
                //noinspection ThrowableNotThrown
                LOGGER.logExceptionAsError(e);

                return new PollResponse<>(LongRunningOperationStatus.FAILED, null);
            } catch (RuntimeException e) {
                throw LOGGER.logExceptionAsError(e);
            }
        };
    }

    private static PollResponse<KeyVaultSelectiveKeyRestoreOperation> processSelectiveKeyRestoreOperationResponse(
        Response<KeyVaultSelectiveKeyRestoreOperation> response) {
        String operationStatus = response.getValue().getStatus().toLowerCase(Locale.US);
        return new PollResponse<>(
            toLongRunningOperationStatus(operationStatus.toLowerCase(Locale.US)), response.getValue());
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.administration;

import com.azure.v2.core.http.polling.LongRunningOperationStatus;
import com.azure.v2.core.http.polling.PollResponse;
import com.azure.v2.core.http.polling.Poller;
import com.azure.v2.core.http.polling.PollingContext;
import com.azure.v2.security.keyvault.administration.implementation.KeyVaultAdministrationClientImpl;
import com.azure.v2.security.keyvault.administration.implementation.models.FullBackupOperation;
import com.azure.v2.security.keyvault.administration.implementation.models.OperationStatus;
import com.azure.v2.security.keyvault.administration.implementation.models.RestoreOperation;
import com.azure.v2.security.keyvault.administration.implementation.models.RestoreOperationParameters;
import com.azure.v2.security.keyvault.administration.implementation.models.SASTokenParameter;
import com.azure.v2.security.keyvault.administration.implementation.models.SelectiveKeyRestoreOperation;
import com.azure.v2.security.keyvault.administration.implementation.models.SelectiveKeyRestoreOperationParameters;
import com.azure.v2.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRestoreOperation;
import com.azure.v2.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreOperation;
import io.clientcore.core.annotations.ServiceClient;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;

import static com.azure.v2.security.keyvault.administration.KeyVaultAdministrationUtil.toLongRunningOperationStatus;
import static com.azure.v2.security.keyvault.administration.KeyVaultAdministrationUtil.transformToLongRunningOperation;

/**
 * This class provides methods to perform full a backup and restore of a key vault,
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
 * <p><strong>Sample: Construct Backup Client</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link KeyVaultBackupClient}, using the
 * {@link KeyVaultBackupClientBuilder} to configure it.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.instantiation -->
 * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.instantiation -->
 *
 * <br/>
 * <hr/>
 *
 * <h2>Back Up a Collection of Keys</h2>
 * The {@link KeyVaultBackupClient} can be used to back up the entire collection of keys from a key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to back up an entire collection of keys using, using the
 * {/@link KeyVaultBackupClient#beginBackup(String, String)} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.beginBackup#String-String -->
 * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.beginBackup#String-String -->
 *
 * <br/>
 * <hr/>
 *
 * <h2>Restore a Collection of Keys</h2>
 * The {@link KeyVaultBackupClient} can be used to restore an entire collection of keys from a backup.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to restore an entire collection of keys from a backup, using the
 * {/@link KeyVaultBackupClient#beginRestore(String, String)} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.beginRestore#String-String -->
 * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.beginRestore#String-String -->
 *
 * <br/>
 * <hr/>
 *
 * <h2>Selectively Restore a Key</h2>
 * The {@link KeyVaultBackupClient} can be used to restore a specific key from a backup.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to restore a specific key from a backup, using the
 * {/@link KeyVaultBackupClient#beginSelectiveKeyRestore(String, String, String)} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.beginSelectiveKeyRestore#String-String-String -->
 * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.beginSelectiveKeyRestore#String-String-String -->
 *
 * @see com.azure.v2.security.keyvault.administration
 * @see KeyVaultBackupClientBuilder
 */
@ServiceClient(builder = KeyVaultBackupClientBuilder.class)
public final class KeyVaultBackupClient {
    private static final ClientLogger LOGGER = new ClientLogger(KeyVaultBackupClient.class);

    private final KeyVaultAdministrationClientImpl clientImpl;

    /**
     * Creates an instance of {@link KeyVaultBackupClient} that sends requests to the service using the provided
     * {@link KeyVaultAdministrationClientImpl}.
     *
     * @param clientImpl The implementation client.
     */
    KeyVaultBackupClient(KeyVaultAdministrationClientImpl clientImpl) {
        this.clientImpl = clientImpl;
    }

    /**
     * Initiates a full backup of the key vault.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Starts a backup operation, polls for its status and waits for it to complete. Prints out the details of the
     * operation's final result in case of success or prints out error details in case the operation fails.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.beginBackup#String-String -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.beginBackup#String-String -->
     *
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup will be located. It is required and
     * cannot be {@code null} or an empty string.
     * @param sasToken Optional Shared Access Signature (SAS) token to authorize access to the blob. If {@code null} is
     * provided, Managed Identity will be used to authenticate instead.
     *
     * @return A {@link Poller} to poll on and retrieve the backup operation status with.
     *
     * @throws HttpResponseException If the provided {@code blobStorageUrl} or {@code sasToken} are invalid.
     * @throws IllegalArgumentException If {@code blobStorageUrl} is {@code null} or an empty string.
     */
    // TODO (vcolin7): Uncomment when creating a Poller is supported in azure-core-v2.
    /*@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<KeyVaultBackupOperation, String> beginBackup(String blobStorageUrl, String sasToken) {
        try {
            if (isNullOrEmpty(blobStorageUrl)) {
                throw new IllegalArgumentException(
                    String.format(KeyVaultAdministrationUtil.CANNOT_BE_NULL_OR_EMPTY, "'blobStorageUrl'"));
            }

            return Poller.createPoller(Duration.ofSeconds(1),
                pollingContext -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                    backupWithResponse(blobStorageUrl, sasToken).getValue()),
                pollingContext -> backupPollOperation(pollingContext.getLatestResponse(), RequestContext.none()),
                (pollingContext, firstResponse) -> {
                    throw new RuntimeException("Cancellation is not supported");
                }, pollingContext -> pollingContext.getLatestResponse().getValue().getAzureStorageBlobContainerUrl());
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }*/

    private Response<KeyVaultBackupOperation> backupWithResponse(String blobStorageUrl, String sasToken) {
        SASTokenParameter sasTokenParameter = new SASTokenParameter(blobStorageUrl).setToken(sasToken)
            .setUseManagedIdentity(sasToken == null);

        try (Response<FullBackupOperation> backupOperationResponse =
            clientImpl.fullBackupWithResponse(sasTokenParameter, RequestContext.none())) {

            return new Response<>(backupOperationResponse.getRequest(), backupOperationResponse.getStatusCode(),
                backupOperationResponse.getHeaders(), (KeyVaultBackupOperation) transformToLongRunningOperation(
                    backupOperationResponse.getValue()));
        }
    }

    private PollResponse<KeyVaultBackupOperation> backupPollOperation(
        PollResponse<KeyVaultBackupOperation> pollResponse, RequestContext requestContext) {

        try {
            if (pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
                || pollResponse.getStatus() == LongRunningOperationStatus.FAILED) {

                return pollResponse;
            }

            final KeyVaultBackupOperation keyVaultBackupOperation = pollResponse.getValue();

            if (keyVaultBackupOperation == null) {
                LOGGER.atWarning().log("Backup operation does not exist. Activation operation failed.");

                return new PollResponse<>(LongRunningOperationStatus.fromString("BACKUP_START_FAILED", true), null);
            }

            final String jobId = keyVaultBackupOperation.getOperationId();

            try (Response<FullBackupOperation> response = clientImpl.fullBackupStatusWithResponse(jobId,
                requestContext)) {

                return processOperationResponse(
                    new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                        (KeyVaultBackupOperation) transformToLongRunningOperation(response.getValue())));
            }
        } catch (HttpResponseException e) {
            //noinspection ThrowableNotThrown
            LOGGER.logThrowableAsError(e);

            return new PollResponse<>(LongRunningOperationStatus.FAILED, null);
        }
    }

    /**
     * Initiates a full restore of the key vault.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Starts a restore operation, polls for its status and waits for it to complete. Prints out error details in
     * case the operation fails.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.beginBackup#String-String -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.beginBackup#String-String -->
     *
     * @param folderUrl The URL for the Blob Storage resource where the backup is located, including the path to
     * the blob container where the backup resides. This would be the exact value that is returned as the result of a
     * backup operation. An example of such a URL may look like the following:
     * {@code https://contoso.blob.core.windows.net/backup/mhsm-contoso-2020090117323313}. It is required and cannot be
     * {@code null} or an empty string.
     * @param sasToken Optional Shared Access Signature (SAS) token to authorize access to the blob. If {@code null} is
     * provided, Managed Identity will be used to authenticate instead.
     *
     * @return A {@link Poller} to poll on and retrieve the restore operation status with.
     *
     * @throws HttpResponseException If the provided {@code folderUrl} or {@code sasToken} are invalid.
     * @throws IllegalArgumentException If {@code folderUrl} is {@code null} or an empty string.
     */
    // TODO (vcolin7): Uncomment when creating a Poller is supported in azure-core-v2.
    /*@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<KeyVaultRestoreOperation, KeyVaultRestoreResult> beginRestore(String folderUrl, String sasToken) {
        try {
            if (isNullOrEmpty(folderUrl)) {
                throw new IllegalArgumentException(
                    String.format(KeyVaultAdministrationUtil.CANNOT_BE_NULL_OR_EMPTY, "'folderUrl'"));
            }

            return Poller.createPoller(Duration.ofSeconds(1),
                pollingContext -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                    restoreActivationOperation(folderUrl, sasToken)),
                pollingContext -> restorePollOperation(pollingContext),
                (pollingContext, firstResponse) -> { throw new RuntimeException("Cancellation is not supported"); },
                pollingContext -> new KeyVaultRestoreResult());
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }*/

    Response<KeyVaultRestoreOperation> restoreWithResponse(String folderUrl, String sasToken) {
        String[] segments = folderUrl.split("/");
        String folderName = segments[segments.length - 1];
        String containerUrl = folderUrl.substring(0, folderUrl.length() - folderName.length());

        SASTokenParameter sasTokenParameter = new SASTokenParameter(containerUrl).setToken(sasToken)
            .setUseManagedIdentity(sasToken == null);
        RestoreOperationParameters restoreOperationParameters = new RestoreOperationParameters(sasTokenParameter,
            folderName);

        try (Response<RestoreOperation> response = clientImpl.fullRestoreOperationWithResponse(
            restoreOperationParameters, RequestContext.none())) {

            return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                (KeyVaultRestoreOperation) transformToLongRunningOperation(response.getValue()));
        }
    }

    private KeyVaultRestoreOperation restoreActivationOperation(String folderUrl, String sasToken) {
        try (Response<KeyVaultRestoreOperation> response = restoreWithResponse(folderUrl, sasToken)) {
            return response.getValue();
        }
    }

    private PollResponse<KeyVaultRestoreOperation> restorePollOperation(
        PollingContext<KeyVaultRestoreOperation> pollingContext) {

        try {
            PollResponse<KeyVaultRestoreOperation> pollResponse = pollingContext.getLatestResponse();

            if (pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
                || pollResponse.getStatus() == LongRunningOperationStatus.FAILED) {

                return pollResponse;
            }

            final KeyVaultRestoreOperation keyVaultRestoreOperation = pollResponse.getValue();

            if (keyVaultRestoreOperation == null) {
                LOGGER.atWarning().log("Restore operation does not exist. Activation operation failed.");

                return new PollResponse<>(LongRunningOperationStatus.fromString("RESTORE_START_FAILED", true),
                    null);
            }

            final String jobId = keyVaultRestoreOperation.getOperationId();

            try (Response<RestoreOperation> response = clientImpl.restoreStatusWithResponse(jobId,
                RequestContext.none())) {

                return processOperationResponse(
                    new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                        (KeyVaultRestoreOperation) transformToLongRunningOperation(response.getValue())));
            }
        } catch (HttpResponseException e) {
            //noinspection ThrowableNotThrown
            LOGGER.logThrowableAsError(e);

            return new PollResponse<>(LongRunningOperationStatus.FAILED, null);
        }
    }

    /**
     * Restores all versions of a given key using the supplied SAS token pointing to a previously stored Azure Blob
     * Storage backup folder.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Starts a selective key restore operation, polls for its status and waits for it to complete. Prints out error
     * details in case the operation fails.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.beginSelectiveKeyRestore#String-String-String -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.beginSelectiveKeyRestore#String-String-String -->
     *
     * @param keyName The name of the key to be restored. It is required and cannot be {@code null} or an empty string.
     * @param folderUrl The URL for the Blob Storage resource where the backup is located, including the path to
     * the blob container where the backup resides. This would be the exact value that is returned as the result of a
     * backup operation. An example of such a URL may look like the following:
     * https://contoso.blob.core.windows.net/backup/mhsm-contoso-2020090117323313. It is required and cannot be
     * {@code null} or an empty string.
     * @param sasToken Optional Shared Access Signature (SAS) token to authorize access to the blob. If {@code null},
     * Managed Identity will be used to authenticate instead.
     *
     * @return A {@link Poller} to poll on the {@link KeyVaultRestoreOperation restore operation} status.
     *
     * @throws HttpResponseException If the given {@code folderUrl} or {@code sasToken} are invalid.
     * @throws NullPointerException If the {@code keyName} or {@code folderUrl} are {@code null}.
     */
    // TODO (vcolin7): Uncomment when creating a Poller is supported in azure-core-v2.
    /*@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<KeyVaultSelectiveKeyRestoreOperation, KeyVaultSelectiveKeyRestoreResult>
        beginSelectiveKeyRestore(String keyName, String folderUrl, String sasToken) {

        try {
            if (isNullOrEmpty(keyName)) {
                throw new IllegalArgumentException(
                    String.format(KeyVaultAdministrationUtil.CANNOT_BE_NULL_OR_EMPTY, "'keyName'"));
            }

            if (isNullOrEmpty(folderUrl)) {
                throw new IllegalArgumentException(
                    String.format(KeyVaultAdministrationUtil.CANNOT_BE_NULL_OR_EMPTY, "'folderUrl'"));
            }

            return Poller.createPoller(Duration.ofSeconds(1),
                pollingContext -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                    selectiveKeyRestoreActivationOperation(keyName, folderUrl, sasToken)),
                pollingContext -> selectiveKeyRestorePollOperation(pollingContext),
                (pollingContext, firstResponse) -> { throw new RuntimeException("Cancellation is not supported"); },
                pollingContext -> new KeyVaultSelectiveKeyRestoreResult());
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }*/

    Response<KeyVaultSelectiveKeyRestoreOperation> selectiveKeyRestoreWithResponse(String keyName, String folderUrl,
        String sasToken) {

        String[] segments = folderUrl.split("/");
        String folderName = segments[segments.length - 1];
        String containerUrl = folderUrl.substring(0, folderUrl.length() - folderName.length());

        SASTokenParameter sasTokenParameter = new SASTokenParameter(containerUrl).setToken(sasToken)
            .setUseManagedIdentity(sasToken == null);
        SelectiveKeyRestoreOperationParameters selectiveKeyRestoreOperationParameters =
            new SelectiveKeyRestoreOperationParameters(sasTokenParameter, folderName);

        try (Response<SelectiveKeyRestoreOperation> selectiveKeyRestoreOperationResponse =
            clientImpl.selectiveKeyRestoreOperationWithResponse(keyName, selectiveKeyRestoreOperationParameters,
                RequestContext.none())) {

            return new Response<>(selectiveKeyRestoreOperationResponse.getRequest(),
                selectiveKeyRestoreOperationResponse.getStatusCode(), selectiveKeyRestoreOperationResponse.getHeaders(),
                (KeyVaultSelectiveKeyRestoreOperation) transformToLongRunningOperation(
                    selectiveKeyRestoreOperationResponse.getValue()));
        }
    }

    private KeyVaultSelectiveKeyRestoreOperation selectiveKeyRestoreActivationOperation(String keyName,
        String folderUrl, String sasToken) {

        try (Response<KeyVaultSelectiveKeyRestoreOperation> response = selectiveKeyRestoreWithResponse(keyName,
            folderUrl, sasToken)) {

            return response.getValue();
        }
    }

    private PollResponse<KeyVaultSelectiveKeyRestoreOperation> selectiveKeyRestorePollOperation(
        PollingContext<KeyVaultSelectiveKeyRestoreOperation> pollingContext) {

        try {
            PollResponse<KeyVaultSelectiveKeyRestoreOperation> pollResponse = pollingContext.getLatestResponse();

            if (pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
                || pollResponse.getStatus() == LongRunningOperationStatus.FAILED) {

                return pollResponse;
            }

            final KeyVaultSelectiveKeyRestoreOperation keyVaultSelectiveKeyRestoreOperation
                = pollResponse.getValue();

            if (keyVaultSelectiveKeyRestoreOperation == null) {
                LOGGER.atWarning().log("Restore operation does not exist. Activation operation failed.");

                return new PollResponse<>(
                    LongRunningOperationStatus.fromString("SELECTIVE_RESTORE_START_FAILED", true), null);
            }

            final String jobId = keyVaultSelectiveKeyRestoreOperation.getOperationId();

            try (Response<SelectiveKeyRestoreOperation> response = clientImpl.selectiveKeyRestoreStatusWithResponse(
                jobId, RequestContext.none())) {

                return processOperationResponse(
                    new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                        (KeyVaultSelectiveKeyRestoreOperation) transformToLongRunningOperation(response.getValue())));
            }
        } catch (HttpResponseException e) {
            //noinspection ThrowableNotThrown
            LOGGER.logThrowableAsError(e);

            return new PollResponse<>(LongRunningOperationStatus.FAILED, null);
        }
    }

    private static <T> PollResponse<T> processOperationResponse(Response<T> response) {
        final OperationStatus status;
        final T value = response.getValue();

        if (value instanceof FullBackupOperation) {
            status = ((FullBackupOperation) value).getStatus();
        } else if (value instanceof RestoreOperation) {
            status = ((RestoreOperation) value).getStatus();
        } else if (value instanceof SelectiveKeyRestoreOperation) {
            status = ((SelectiveKeyRestoreOperation) value).getStatus();
        } else {
            throw new IllegalArgumentException("Unsupported operation type: " + value.getClass());
        }

        return new PollResponse<>(toLongRunningOperationStatus(status.toString().toLowerCase(Locale.US)),
            response.getValue());
    }
}

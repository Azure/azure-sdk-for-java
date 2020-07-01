// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import com.azure.security.keyvault.administration.implementation.KeyVaultBackupClientImpl;
import com.azure.security.keyvault.administration.implementation.KeyVaultBackupClientImplBuilder;
import com.azure.security.keyvault.administration.implementation.KeyVaultErrorCodeStrings;
import com.azure.security.keyvault.administration.implementation.models.*;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * The {@link KeyVaultBackupAsyncClient} provides asynchronous methods to perform full backup and restore of an Azure
 * Key Vault.
 */
@ServiceClient(builder = KeyVaultBackupClientBuilder.class, isAsync = true)
public class KeyVaultBackupAsyncClient {
    // Please see <a href=https://docs.microsoft.com/en-us/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    private static final String KEYVAULT_TRACING_NAMESPACE_VALUE = "Microsoft.KeyVault";

    /**
     * The logger to be used.
     */
    private final ClientLogger logger = new ClientLogger(KeyVaultBackupAsyncClient.class);

    /**
     * The underlying AutoRest client used to interact with the Key Vault service.
     */
    private final KeyVaultBackupClientImpl clientImpl;

    /**
     * The Kay Vault URL this client is associated to.
     */
    private final String vaultUrl;

    /**
     * Package private constructor to be used by {@link KeyVaultBackupClientBuilder}.
     */
    KeyVaultBackupAsyncClient(URL vaultUrl, HttpPipeline httpPipeline) {
        Objects.requireNonNull(vaultUrl,
            KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));

        this.vaultUrl = vaultUrl.toString();

        clientImpl = new KeyVaultBackupClientImplBuilder()
            .pipeline(httpPipeline)
            .buildClient();
    }

    /**
     * Gets the URL for the Key Vault this client is associated with.
     *
     * @return The Key Vault URL.
     */
    public String getVaultUrl() {
        return this.vaultUrl;
    }

    /**
     * Initiates a full backup of the Key Vault.
     *
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup will be located.
     * @param sasToken       A Shared Access Signature (SAS) token to authorize access to the blob.
     * @return A {@link PollerFlux} polling on the {@link FullBackupOperation backup operation} status.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code blobStorageUrl} or {@code sasToken} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PollerFlux<FullBackupOperation, Void> startBackup(String blobStorageUrl, String sasToken) {
        Objects.requireNonNull(blobStorageUrl, "'blobStorageUrl' cannot be null.");
        Objects.requireNonNull(sasToken, "'sasToken' cannot be null.");

        return new PollerFlux<>(Duration.ofSeconds(1),
            backupActivationOperation(blobStorageUrl, sasToken),
            backupPollOperation(),
            (pollingContext, firstResponse) -> Mono.empty(),
            (pollingContext) -> Mono.empty());
    }

    /**
     * Initiates a full backup of the Key Vault.
     *
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup will be located.
     * @param sasToken       A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param context        Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link PollerFlux} polling on the {@link FullBackupOperation backup operation} status.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code blobStorageUrl} or {@code sasToken} are {@code null}.
     */
    Mono<FullBackupResponse> backupWithResponse(String blobStorageUrl, String sasToken, Context context) {
        SASTokenParameter sasTokenParameter = new SASTokenParameter()
            .setStorageResourceUri(blobStorageUrl)
            .setToken(sasToken);

        return clientImpl.fullBackupWithResponseAsync(vaultUrl, sasTokenParameter,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
    }

    private Function<PollingContext<FullBackupOperation>, Mono<FullBackupOperation>> backupActivationOperation(String blobStorageUrl, String sasToken) {
        return (pollingContext) -> withContext(context -> backupWithResponse(blobStorageUrl, sasToken, context))
            .flatMap(backupResponse -> Mono.just(backupResponse.getValue()));
    }

    private Function<PollingContext<FullBackupOperation>, Mono<PollResponse<FullBackupOperation>>> backupPollOperation() {
        return (pollingContext) -> {
            try {
                PollResponse<FullBackupOperation> pollResponse = pollingContext.getLatestResponse();

                if (pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
                    || pollResponse.getStatus() == LongRunningOperationStatus.FAILED) {

                    return Mono.just(pollResponse);
                }

                final FullBackupOperation responseValue = pollResponse.getValue();

                if (responseValue == null) {
                    logger.warning("Operation does not exist. Activation operation failed.");

                    return Mono.just(new PollResponse<FullBackupOperation>(
                        LongRunningOperationStatus.fromString("BACKUP_START_FAILED", true), null));
                }

                final String jobId = responseValue.getJobId();

                return withContext(context -> clientImpl.fullBackupStatusWithResponseAsync(vaultUrl, jobId,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE)))
                    .flatMap(this::processBackupOperationResponse);
            } catch (HttpResponseException e) {
                //noinspection ThrowableNotThrown
                logger.logExceptionAsError(e);

                return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null));
            }
        };
    }

    private Mono<PollResponse<FullBackupOperation>> processBackupOperationResponse(Response<FullBackupOperation> response) {
        String operationStatus = response.getValue().getStatus().toLowerCase();

        return Mono.just(new PollResponse<>(toLongRunningOperationStatus(operationStatus), response.getValue()));
    }

    private LongRunningOperationStatus toLongRunningOperationStatus(String operationStatus) {
        switch (operationStatus) {
            case "inprogress":
                return LongRunningOperationStatus.IN_PROGRESS;
            case "success":
                return LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
            case "failed":
                return LongRunningOperationStatus.FAILED;
            default:
                // Should not reach here
                return LongRunningOperationStatus.fromString("POLLING_FAILED", true);
        }
    }

    /**
     * Initiates a full restore of the Key Vault.
     *
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup is located.
     * @param sasToken       A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param folderName     The name of the folder containing the backup data to restore.
     * @return A {@link PollerFlux} polling on the {@link RestoreOperation backup operation} status.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code blobStorageUrl} or {@code sasToken} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PollerFlux<RestoreOperation, Void> startRestore(String blobStorageUrl, String sasToken, String folderName) {
        Objects.requireNonNull(blobStorageUrl, "'blobStorageUrl' cannot be null.");
        Objects.requireNonNull(sasToken, "'sasToken' cannot be null.");
        Objects.requireNonNull(folderName, "'folderName' cannot be null.");


        return new PollerFlux<>(Duration.ofSeconds(1),
            restoreActivationOperation(blobStorageUrl, sasToken, folderName),
            restorePollOperation(),
            (pollingContext, firstResponse) -> Mono.empty(),
            (pollingContext) -> Mono.empty());
    }

    /**
     * Initiates a full restore of the Key Vault.
     *
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup is located.
     * @param sasToken       A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param folderName     The name of the folder containing the backup data to restore.
     * @param context        Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link PollerFlux} polling on the {@link RestoreOperation backup operation} status.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code blobStorageUrl} or {@code sasToken} are {@code null}.
     */
    Mono<FullRestoreOperationResponse> restoreWithResponse(String blobStorageUrl, String sasToken,
                                                           String folderName, Context context) {
        SASTokenParameter sasTokenParameter = new SASTokenParameter()
            .setStorageResourceUri(blobStorageUrl)
            .setToken(sasToken);

        RestoreOperationParameters restoreOperationParameters = new RestoreOperationParameters()
            .setSasTokenParameters(sasTokenParameter)
            .setFolderToRestore(folderName);

        return clientImpl.fullRestoreOperationWithResponseAsync(vaultUrl, restoreOperationParameters,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
    }

    private Function<PollingContext<RestoreOperation>, Mono<RestoreOperation>> restoreActivationOperation(String blobStorageUrl, String sasToken, String folderName) {
        return (pollingContext) ->
            withContext(context -> restoreWithResponse(blobStorageUrl, sasToken, folderName, context))
                .flatMap(restoreResponse -> Mono.just(restoreResponse.getValue()));
    }

    private Function<PollingContext<RestoreOperation>, Mono<PollResponse<RestoreOperation>>> restorePollOperation() {
        return (pollingContext) -> {
            try {
                PollResponse<RestoreOperation> pollResponse = pollingContext.getLatestResponse();

                if (pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
                    || pollResponse.getStatus() == LongRunningOperationStatus.FAILED) {

                    return Mono.just(pollResponse);
                }

                final RestoreOperation responseValue = pollResponse.getValue();

                if (responseValue == null) {
                    logger.warning("RestoreOperation does not exist. Activation operation failed.");

                    return Mono.just(new PollResponse<RestoreOperation>(
                        LongRunningOperationStatus.fromString("RESTORE_START_FAILED", true), null));
                }

                final String jobId = responseValue.getJobId();

                return withContext(context -> clientImpl.restoreStatusWithResponseAsync(vaultUrl, jobId,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE)))
                    .flatMap(this::processRestoreOperationResponse);
            } catch (HttpResponseException e) {
                //noinspection ThrowableNotThrown
                logger.logExceptionAsError(e);

                return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null));
            }
        };
    }

    private Mono<PollResponse<RestoreOperation>> processRestoreOperationResponse(Response<RestoreOperation> response) {
        String operationStatus = response.getValue().getStatus().toLowerCase();

        return Mono.just(new PollResponse<>(toLongRunningOperationStatus(operationStatus), response.getValue()));
    }

    /**
     * Restores all versions of a given key using the supplied SAS token pointing to a previously stored Azure Blob storage backup folder.
     *
     * @param keyName        The name of the key to be restored.
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup is located.
     * @param sasToken       A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param folderName     The name of the folder containing the backup data to restore.
     * @return A {@link PollerFlux} polling on the {@link RestoreOperation backup operation} status.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code blobStorageUrl} or {@code sasToken} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PollerFlux<SelectiveKeyRestoreOperation, Void> startSelectiveRestore(String keyName, String blobStorageUrl, String sasToken, String folderName) {
        Objects.requireNonNull(keyName, "'keyName' cannot be null.");
        Objects.requireNonNull(blobStorageUrl, "'blobStorageUrl' cannot be null.");
        Objects.requireNonNull(sasToken, "'sasToken' cannot be null.");
        Objects.requireNonNull(folderName, "'folderName' cannot be null.");


        return new PollerFlux<>(Duration.ofSeconds(1),
            selectiveRestoreActivationOperation(keyName, blobStorageUrl, sasToken, folderName),
            selectiveRestorePollOperation(),
            (pollingContext, firstResponse) -> Mono.empty(),
            (pollingContext) -> Mono.empty());
    }

    /**
     * Restores all versions of a given key using the supplied SAS token pointing to a previously stored Azure Blob storage backup folder.
     *
     * @param keyName        The name of the key to be restored.
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup is located.
     * @param sasToken       A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param folderName     The name of the folder containing the backup data to restore.
     * @param context        Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link PollerFlux} polling on the {@link RestoreOperation backup operation} status.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code blobStorageUrl} or {@code sasToken} are {@code null}.
     */
    Mono<SelectiveKeyRestoreOperationResponse> selectiveRestoreWithResponse(String keyName, String blobStorageUrl, String sasToken, String folderName, Context context) {
        SASTokenParameter sasTokenParameter = new SASTokenParameter()
            .setStorageResourceUri(blobStorageUrl)
            .setToken(sasToken);

        SelectiveKeyRestoreOperationParameters selectiveKeyRestoreOperationParameters =
            new SelectiveKeyRestoreOperationParameters()
                .setSasTokenParameters(sasTokenParameter)
                .setFolder(folderName);

        return clientImpl.selectiveKeyRestoreOperationWithResponseAsync(vaultUrl, keyName,
            selectiveKeyRestoreOperationParameters, context.addData(AZ_TRACING_NAMESPACE_KEY,
                KEYVAULT_TRACING_NAMESPACE_VALUE));
    }

    private Function<PollingContext<SelectiveKeyRestoreOperation>, Mono<SelectiveKeyRestoreOperation>> selectiveRestoreActivationOperation(String keyName, String blobStorageUrl, String sasToken, String folderName) {
        return (pollingContext) ->
            withContext(context -> selectiveRestoreWithResponse(keyName, blobStorageUrl, sasToken, folderName, context))
                .flatMap(selectiveKeyRestoreResponse -> Mono.just(selectiveKeyRestoreResponse.getValue()));
    }

    private Function<PollingContext<SelectiveKeyRestoreOperation>, Mono<PollResponse<SelectiveKeyRestoreOperation>>> selectiveRestorePollOperation() {
        return (pollingContext) -> {
            try {
                PollResponse<SelectiveKeyRestoreOperation> pollResponse = pollingContext.getLatestResponse();

                if (pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
                    || pollResponse.getStatus() == LongRunningOperationStatus.FAILED) {

                    return Mono.just(pollResponse);
                }

                final SelectiveKeyRestoreOperation responseValue = pollResponse.getValue();

                if (responseValue == null) {
                    logger.warning("SelectiveKeyRestoreOperation does not exist. Activation operation failed.");

                    return Mono.just(new PollResponse<SelectiveKeyRestoreOperation>(
                        LongRunningOperationStatus.fromString("RESTORE_START_FAILED", true), null));
                }

                final String jobId = responseValue.getJobId();

                return withContext(context -> clientImpl.restoreStatusWithResponseAsync(vaultUrl, jobId,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE)))
                    .flatMap(this::processSelectiveRestoreOperationResponse);
            } catch (HttpResponseException e) {
                //noinspection ThrowableNotThrown
                logger.logExceptionAsError(e);

                return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null));
            }
        };
    }

    private Mono<PollResponse<SelectiveKeyRestoreOperation>> processSelectiveRestoreOperationResponse(Response<RestoreOperation> response) {
        RestoreOperation restoreOperation = response.getValue();
        String operationStatus = restoreOperation.getStatus().toLowerCase();
        SelectiveKeyRestoreOperation selectiveKeyRestoreOperation = new SelectiveKeyRestoreOperation();

        selectiveKeyRestoreOperation.setEndTime(restoreOperation.getEndTime());
        selectiveKeyRestoreOperation.setError(restoreOperation.getError());
        selectiveKeyRestoreOperation.setJobId(restoreOperation.getJobId());
        selectiveKeyRestoreOperation.setStartTime(restoreOperation.getStartTime());
        selectiveKeyRestoreOperation.setStatus(restoreOperation.getStatus());
        selectiveKeyRestoreOperation.setStatusDetails(restoreOperation.getStatusDetails());

        return Mono.just(
            new PollResponse<>(toLongRunningOperationStatus(operationStatus), selectiveKeyRestoreOperation));
    }
}

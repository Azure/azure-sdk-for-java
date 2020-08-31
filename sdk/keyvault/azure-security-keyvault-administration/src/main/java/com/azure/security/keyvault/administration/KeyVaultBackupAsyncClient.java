// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
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
import com.azure.security.keyvault.administration.implementation.models.Error;
import com.azure.security.keyvault.administration.models.*;
import com.azure.security.keyvault.administration.models.KeyVaultError;
import com.azure.security.keyvault.administration.models.KeyVaultException;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * The {@link KeyVaultBackupAsyncClient} provides asynchronous methods to perform full backup and restore of an Azure
 * Key Vault.
 */
@ServiceClient(builder = KeyVaultBackupClientBuilder.class, isAsync = true)
public final class KeyVaultBackupAsyncClient {
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
     * @return A {@link PollerFlux} polling on the {@link KeyVaultBackupOperation backup operation} status.
     * @throws KeyVaultException    if the operation is unsuccessful.
     * @throws NullPointerException if the {@code blobStorageUrl} or {@code sasToken} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PollerFlux<KeyVaultBackupOperation, Void> beginBackup(String blobStorageUrl, String sasToken) {
        Objects.requireNonNull(blobStorageUrl,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'blobStorageUrl'"));
        Objects.requireNonNull(sasToken,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'sasToken'"));

        return new PollerFlux<>(Duration.ofSeconds(1),
            backupActivationOperation(blobStorageUrl, sasToken),
            backupPollOperation(),
            (pollingContext, firstResponse) -> Mono.error(new RuntimeException("Cancellation is not supported")),
            (pollingContext) -> Mono.empty());
    }

    /**
     * Initiates a full backup of the Key Vault.
     *
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup will be located.
     * @param sasToken       A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param context        Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link PollerFlux} polling on the {@link KeyVaultBackupOperation backup operation} status.
     * @throws KeyVaultException    if the operation is unsuccessful.
     * @throws NullPointerException if the {@code blobStorageUrl} or {@code sasToken} are {@code null}.
     */
    Mono<Response<KeyVaultBackupOperation>> backupWithResponse(String blobStorageUrl, String sasToken,
                                                               Context context) {
        SASTokenParameter sasTokenParameter = new SASTokenParameter()
            .setStorageResourceUri(blobStorageUrl)
            .setToken(sasToken);

        try {
            return clientImpl.fullBackupWithResponseAsync(vaultUrl, sasTokenParameter,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .map(backupOperationResponse ->
                    new SimpleResponse<>(backupOperationResponse.getRequest(), backupOperationResponse.getStatusCode(),
                        backupOperationResponse.getHeaders(),
                        (KeyVaultBackupOperation) transformToLongRunningOperation(backupOperationResponse.getValue())));
        } catch (KeyVaultErrorException e) {
            throw new KeyVaultException(e.getMessage(), e.getResponse(),
                createKeyVaultErrorFromError(e.getValue().getError()));
        }
    }

    private Function<PollingContext<KeyVaultBackupOperation>, Mono<KeyVaultBackupOperation>> backupActivationOperation(String blobStorageUrl, String sasToken) {
        return (pollingContext) -> {
            try {
                return withContext(context -> backupWithResponse(blobStorageUrl, sasToken, context))
                    .flatMap(backupResponse -> Mono.just(backupResponse.getValue()));
            } catch (RuntimeException e) {
                return monoError(logger, e);
            }
        };
    }

    private Function<PollingContext<KeyVaultBackupOperation>, Mono<PollResponse<KeyVaultBackupOperation>>> backupPollOperation() {
        return (pollingContext) -> {
            try {
                PollResponse<KeyVaultBackupOperation> pollResponse = pollingContext.getLatestResponse();

                if (pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
                    || pollResponse.getStatus() == LongRunningOperationStatus.FAILED) {
                    return Mono.just(pollResponse);
                }

                final KeyVaultBackupOperation keyVaultBackupOperation = pollResponse.getValue();

                if (keyVaultBackupOperation == null) {
                    logger.warning("Backup operation does not exist. Activation operation failed.");

                    return Mono.just(new PollResponse<KeyVaultBackupOperation>(
                        LongRunningOperationStatus.fromString("BACKUP_START_FAILED", true), null));
                }

                final String jobId = keyVaultBackupOperation.getJobId();

                return withContext(context -> clientImpl.fullBackupStatusWithResponseAsync(vaultUrl, jobId,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE)))
                    .map(response ->
                        new SimpleResponse<>(response,
                            (KeyVaultBackupOperation) transformToLongRunningOperation(response.getValue())))
                    .flatMap(this::processBackupOperationResponse);
            } catch (HttpResponseException e) {
                //noinspection ThrowableNotThrown
                logger.logExceptionAsError(e);

                return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null));
            }
        };
    }

    private Mono<PollResponse<KeyVaultBackupOperation>> processBackupOperationResponse(Response<KeyVaultBackupOperation> response) {
        String operationStatus = response.getValue().getStatus().toLowerCase();

        return Mono.just(new PollResponse<>(toLongRunningOperationStatus(operationStatus), response.getValue()));
    }

    private LongRunningOperationStatus toLongRunningOperationStatus(String operationStatus) {
        switch (operationStatus) {
            case "InProgress":
                return LongRunningOperationStatus.IN_PROGRESS;
            case "Success":
                return LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
            case "Failed":
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
     * @return A {@link PollerFlux} polling on the {@link KeyVaultRestoreOperation backup operation} status.
     * @throws KeyVaultException    if the operation is unsuccessful.
     * @throws NullPointerException if the {@code blobStorageUrl} or {@code sasToken} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PollerFlux<KeyVaultRestoreOperation, Void> beginRestore(String blobStorageUrl, String sasToken,
                                                                   String folderName) {
        Objects.requireNonNull(blobStorageUrl,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'blobStorageUrl'"));
        Objects.requireNonNull(sasToken,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'sasToken'"));
        Objects.requireNonNull(folderName,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'folderName'"));


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
     * @return A {@link PollerFlux} polling on the {@link KeyVaultRestoreOperation backup operation} status.
     * @throws KeyVaultException    if the operation is unsuccessful.
     * @throws NullPointerException if the {@code blobStorageUrl} or {@code sasToken} are {@code null}.
     */
    Mono<Response<KeyVaultRestoreOperation>> restoreWithResponse(String blobStorageUrl, String sasToken,
                                                                 String folderName, Context context) {
        SASTokenParameter sasTokenParameter = new SASTokenParameter()
            .setStorageResourceUri(blobStorageUrl)
            .setToken(sasToken);

        RestoreOperationParameters restoreOperationParameters = new RestoreOperationParameters()
            .setSasTokenParameters(sasTokenParameter)
            .setFolderToRestore(folderName);

        try {
            return clientImpl.fullRestoreOperationWithResponseAsync(vaultUrl, restoreOperationParameters,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .map(restoreOperationResponse ->
                    new SimpleResponse<>(restoreOperationResponse.getRequest(), restoreOperationResponse.getStatusCode(),
                        restoreOperationResponse.getHeaders(),
                        (KeyVaultRestoreOperation) transformToLongRunningOperation(restoreOperationResponse.getValue())));
        } catch (KeyVaultErrorException e) {
            throw new KeyVaultException(e.getMessage(), e.getResponse(),
                createKeyVaultErrorFromError(e.getValue().getError()));
        }
    }

    private Function<PollingContext<KeyVaultRestoreOperation>, Mono<KeyVaultRestoreOperation>> restoreActivationOperation(String blobStorageUrl, String sasToken, String folderName) {
        return (pollingContext) -> {
            try {
                return withContext(context -> restoreWithResponse(blobStorageUrl, sasToken, folderName, context))
                    .flatMap(restoreResponse -> Mono.just(restoreResponse.getValue()));
            } catch (RuntimeException e) {
                return monoError(logger, e);
            }
        };
    }

    private Function<PollingContext<KeyVaultRestoreOperation>, Mono<PollResponse<KeyVaultRestoreOperation>>> restorePollOperation() {
        return (pollingContext) -> {
            try {
                PollResponse<KeyVaultRestoreOperation> pollResponse = pollingContext.getLatestResponse();

                if (pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
                    || pollResponse.getStatus() == LongRunningOperationStatus.FAILED) {

                    return Mono.just(pollResponse);
                }

                final KeyVaultRestoreOperation keyVaultRestoreOperation = pollResponse.getValue();

                if (keyVaultRestoreOperation == null) {
                    logger.warning("Restore operation does not exist. Activation operation failed.");

                    return Mono.just(new PollResponse<KeyVaultRestoreOperation>(
                        LongRunningOperationStatus.fromString("RESTORE_START_FAILED", true), null));
                }

                final String jobId = keyVaultRestoreOperation.getJobId();

                return withContext(context -> clientImpl.restoreStatusWithResponseAsync(vaultUrl, jobId,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE)))
                    .map(response ->
                        new SimpleResponse<>(response,
                            (KeyVaultRestoreOperation) transformToLongRunningOperation(response.getValue())))
                    .flatMap(this::processRestoreOperationResponse);
            } catch (HttpResponseException e) {
                //noinspection ThrowableNotThrown
                logger.logExceptionAsError(e);

                return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null));
            }
        };
    }

    private Mono<PollResponse<KeyVaultRestoreOperation>> processRestoreOperationResponse(Response<KeyVaultRestoreOperation> response) {
        String operationStatus = response.getValue().getStatus().toLowerCase();

        return Mono.just(new PollResponse<>(toLongRunningOperationStatus(operationStatus), response.getValue()));
    }

    /**
     * Restores all versions of a given key using the supplied SAS token pointing to a previously stored Azure Blob
     * storage backup folder.
     *
     * @param keyName        The name of the key to be restored.
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup is located.
     * @param sasToken       A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param folderName     The name of the folder containing the backup data to restore.
     * @return A {@link PollerFlux} polling on the {@link KeyVaultRestoreOperation backup operation} status.
     * @throws KeyVaultException    if the operation is unsuccessful.
     * @throws NullPointerException if the {@code blobStorageUrl} or {@code sasToken} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PollerFlux<KeyVaultRestoreOperation, Void> beginSelectiveRestore(String keyName, String blobStorageUrl,
                                                                            String sasToken, String folderName) {
        Objects.requireNonNull(keyName,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'keyName'"));
        Objects.requireNonNull(blobStorageUrl,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'blobStorageUrl'"));
        Objects.requireNonNull(sasToken,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'sasToken'"));
        Objects.requireNonNull(folderName,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'folderName'"));

        return new PollerFlux<>(Duration.ofSeconds(1),
            selectiveRestoreActivationOperation(keyName, blobStorageUrl, sasToken, folderName),
            selectiveRestorePollOperation(),
            (pollingContext, firstResponse) -> Mono.empty(),
            (pollingContext) -> Mono.empty());
    }

    /**
     * Restores all versions of a given key using the supplied SAS token pointing to a previously stored Azure Blob
     * storage backup folder.
     *
     * @param keyName        The name of the key to be restored.
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup is located.
     * @param sasToken       A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param folderName     The name of the folder containing the backup data to restore.
     * @param context        Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link PollerFlux} polling on the {@link KeyVaultRestoreOperation backup operation} status.
     * @throws KeyVaultException    if the operation is unsuccessful.
     * @throws NullPointerException if the {@code blobStorageUrl} or {@code sasToken} are {@code null}.
     */
    Mono<Response<KeyVaultRestoreOperation>> selectiveRestoreWithResponse(String keyName, String blobStorageUrl,
                                                                          String sasToken, String folderName,
                                                                          Context context) {
        SASTokenParameter sasTokenParameter = new SASTokenParameter()
            .setStorageResourceUri(blobStorageUrl)
            .setToken(sasToken);

        SelectiveKeyRestoreOperationParameters selectiveKeyRestoreOperationParameters =
            new SelectiveKeyRestoreOperationParameters()
                .setSasTokenParameters(sasTokenParameter)
                .setFolder(folderName);

        try {
            return clientImpl.selectiveKeyRestoreOperationWithResponseAsync(vaultUrl, keyName,
                selectiveKeyRestoreOperationParameters, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE))
                .map(restoreOperationResponse ->
                    new SimpleResponse<>(restoreOperationResponse.getRequest(), restoreOperationResponse.getStatusCode(),
                        restoreOperationResponse.getHeaders(),
                        (KeyVaultRestoreOperation) transformToLongRunningOperation(restoreOperationResponse.getValue())));
        } catch (KeyVaultErrorException e) {
            throw new KeyVaultException(e.getMessage(), e.getResponse(),
                createKeyVaultErrorFromError(e.getValue().getError()));
        }
    }

    private Function<PollingContext<KeyVaultRestoreOperation>, Mono<KeyVaultRestoreOperation>> selectiveRestoreActivationOperation(String keyName, String blobStorageUrl, String sasToken, String folderName) {
        return (pollingContext) -> {
            try {
                return withContext(context -> selectiveRestoreWithResponse(keyName, blobStorageUrl, sasToken,
                    folderName, context))
                    .flatMap(selectiveKeyRestoreResponse -> Mono.just(selectiveKeyRestoreResponse.getValue()));
            } catch (RuntimeException e) {
                return monoError(logger, e);
            }
        };
    }

    private Function<PollingContext<KeyVaultRestoreOperation>, Mono<PollResponse<KeyVaultRestoreOperation>>> selectiveRestorePollOperation() {
        return (pollingContext) -> {
            try {
                PollResponse<KeyVaultRestoreOperation> pollResponse = pollingContext.getLatestResponse();

                if (pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
                    || pollResponse.getStatus() == LongRunningOperationStatus.FAILED) {

                    return Mono.just(pollResponse);
                }

                final KeyVaultRestoreOperation keyVaultRestoreOperation = pollResponse.getValue();

                if (keyVaultRestoreOperation == null) {
                    logger.warning("Restore operation does not exist. Activation operation failed.");

                    return Mono.just(new PollResponse<KeyVaultRestoreOperation>(
                        LongRunningOperationStatus.fromString("SELECTIVE_RESTORE_START_FAILED", true), null));
                }

                final String jobId = keyVaultRestoreOperation.getJobId();

                return withContext(context -> clientImpl.restoreStatusWithResponseAsync(vaultUrl, jobId,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE)))
                    .map(response ->
                        new SimpleResponse<>(response,
                            (KeyVaultRestoreOperation) transformToLongRunningOperation(response.getValue())))
                    .flatMap(this::processRestoreOperationResponse);
            } catch (HttpResponseException e) {
                //noinspection ThrowableNotThrown
                logger.logExceptionAsError(e);

                return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null));
            }
        };
    }

    private static <O> KeyVaultLongRunningOperation transformToLongRunningOperation(O operation) {
        if (operation instanceof RestoreOperation) {
            RestoreOperation restoreOperation = (RestoreOperation) operation;

            return new KeyVaultRestoreOperation(restoreOperation.getStatus(),
                restoreOperation.getStatusDetails(),
                createKeyVaultErrorFromError(restoreOperation.getError()), restoreOperation.getJobId(),
                restoreOperation.getStartTime(), restoreOperation.getEndTime());
        } else if (operation instanceof SelectiveKeyRestoreOperation) {
            SelectiveKeyRestoreOperation selectiveKeyRestoreOperation = (SelectiveKeyRestoreOperation) operation;

            return new KeyVaultRestoreOperation(selectiveKeyRestoreOperation.getStatus(),
                selectiveKeyRestoreOperation.getStatusDetails(),
                createKeyVaultErrorFromError(selectiveKeyRestoreOperation.getError()),
                selectiveKeyRestoreOperation.getJobId(), selectiveKeyRestoreOperation.getStartTime(),
                selectiveKeyRestoreOperation.getEndTime());
        } else if (operation instanceof FullBackupOperation) {
            FullBackupOperation fullBackupOperation = (FullBackupOperation) operation;

            return new KeyVaultBackupOperation(fullBackupOperation.getStatus(), fullBackupOperation.getStatusDetails(),
                createKeyVaultErrorFromError(fullBackupOperation.getError()), fullBackupOperation.getJobId(),
                fullBackupOperation.getStartTime(), fullBackupOperation.getEndTime(),
                fullBackupOperation.getAzureStorageBlobContainerUri());
        } else {
            throw new IllegalArgumentException("Operation type not supported");
        }
    }

    private static KeyVaultError createKeyVaultErrorFromError(Error error) {
        if (error.getInnerError() == null) {
            return new KeyVaultError(error.getCode(), error.getMessage(), null);
        } else {
            return new KeyVaultError(error.getCode(), error.getMessage(),
                createKeyVaultErrorFromError(error.getInnerError()));
        }
    }
}

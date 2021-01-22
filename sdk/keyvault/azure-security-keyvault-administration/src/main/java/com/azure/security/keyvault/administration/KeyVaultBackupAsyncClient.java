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
import com.azure.security.keyvault.administration.implementation.models.Error;
import com.azure.security.keyvault.administration.implementation.models.FullBackupOperation;
import com.azure.security.keyvault.administration.implementation.models.RestoreOperation;
import com.azure.security.keyvault.administration.implementation.models.RestoreOperationParameters;
import com.azure.security.keyvault.administration.implementation.models.SASTokenParameter;
import com.azure.security.keyvault.administration.implementation.models.SelectiveKeyRestoreOperation;
import com.azure.security.keyvault.administration.implementation.models.SelectiveKeyRestoreOperationParameters;
import com.azure.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.security.keyvault.administration.models.KeyVaultError;
import com.azure.security.keyvault.administration.models.KeyVaultLongRunningOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreOperation;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
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

    private static final Duration DEFAULT_POLLING_INTERVAL = Duration.ofSeconds(1);

    /**
     * The logger to be used.
     */
    private final ClientLogger logger = new ClientLogger(KeyVaultBackupAsyncClient.class);

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

    static Duration getDefaultPollingInterval() {
        return DEFAULT_POLLING_INTERVAL;
    }

    /**
     * Package private constructor to be used by {@link KeyVaultBackupClientBuilder}.
     */
    KeyVaultBackupAsyncClient(URL vaultUrl, HttpPipeline httpPipeline, KeyVaultAdministrationServiceVersion serviceVersion) {
        Objects.requireNonNull(vaultUrl,
            KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));

        this.vaultUrl = vaultUrl.toString();
        this.serviceVersion = serviceVersion.getVersion();

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
     * @param sasToken A Shared Access Signature (SAS) token to authorize access to the blob.
     * @return A {@link PollerFlux} polling on the {@link KeyVaultBackupOperation backup operation} status.
     * @throws NullPointerException if the {@code blobStorageUrl} or {@code sasToken} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<KeyVaultBackupOperation, String> beginBackup(String blobStorageUrl, String sasToken) {
        return beginBackup(blobStorageUrl, sasToken, getDefaultPollingInterval());
    }

    /**
     * Initiates a full backup of the Key Vault.
     *
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup will be located.
     * @param sasToken A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param pollingInterval The interval at which the operation status will be polled for.
     * @return A {@link PollerFlux} polling on the {@link KeyVaultBackupOperation backup operation} status.
     * @throws NullPointerException if the {@code blobStorageUrl} or {@code sasToken} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<KeyVaultBackupOperation, String> beginBackup(String blobStorageUrl, String sasToken, Duration pollingInterval) {
        Objects.requireNonNull(blobStorageUrl,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'blobStorageUrl'"));
        Objects.requireNonNull(sasToken,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'sasToken'"));

        return new PollerFlux<>(pollingInterval,
            backupActivationOperation(blobStorageUrl, sasToken),
            backupPollOperation(),
            (pollingContext, firstResponse) -> Mono.error(new RuntimeException("Cancellation is not supported")),
            backupFetchOperation());
    }

    /**
     * Initiates a full backup of the Key Vault.
     *
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup will be located.
     * @param sasToken A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link PollerFlux} polling on the {@link KeyVaultBackupOperation backup operation} status.
     */
    Mono<Response<KeyVaultBackupOperation>> backupWithResponse(String blobStorageUrl, String sasToken, Context context) {
        SASTokenParameter sasTokenParameter = new SASTokenParameter()
            .setStorageResourceUri(blobStorageUrl)
            .setToken(sasToken);

        try {
            return clientImpl.fullBackupWithResponseAsync(vaultUrl, sasTokenParameter,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.info("Backing up at URL - {}", blobStorageUrl))
                .doOnSuccess(response -> logger.info("Backed up at URL - {}",
                    response.getValue().getAzureStorageBlobContainerUri()))
                .doOnError(error -> logger.warning("Failed to backup at URL - {}", blobStorageUrl, error))
                .map(backupOperationResponse ->
                    new SimpleResponse<>(backupOperationResponse.getRequest(), backupOperationResponse.getStatusCode(),
                        backupOperationResponse.getHeaders(),
                        (KeyVaultBackupOperation) transformToLongRunningOperation(backupOperationResponse.getValue())));
        } catch (RuntimeException e) {
            return monoError(logger, e);
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
                    .flatMap(KeyVaultBackupAsyncClient::processBackupOperationResponse);
            } catch (HttpResponseException e) {
                //noinspection ThrowableNotThrown
                logger.logExceptionAsError(e);

                return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null));
            } catch (RuntimeException e) {
                return monoError(logger, e);
            }
        };
    }

    private Function<PollingContext<KeyVaultBackupOperation>, Mono<String>> backupFetchOperation() {
        return (pollingContext) -> {
            try {
                String blobContainerUri =
                    pollingContext.getLatestResponse().getValue().getAzureStorageBlobContainerUrl();

                if (blobContainerUri == null) {
                    return Mono.empty();
                } else {
                    return Mono.just(blobContainerUri);
                }
            } catch (RuntimeException e) {
                return monoError(logger, e);
            }
        };
    }

    private static Mono<PollResponse<KeyVaultBackupOperation>> processBackupOperationResponse(Response<KeyVaultBackupOperation> response) {
        String operationStatus = response.getValue().getStatus().toLowerCase(Locale.US);

        return Mono.just(new PollResponse<>(
            toLongRunningOperationStatus(operationStatus.toLowerCase(Locale.US)), response.getValue()));
    }

    private static LongRunningOperationStatus toLongRunningOperationStatus(String operationStatus) {
        switch (operationStatus) {
            case "inprogress":
                return LongRunningOperationStatus.IN_PROGRESS;
            case "succeeded":
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
     * @param backupFolderUrl The URL for the Blob Storage resource where the backup is located, including the path to
     * the blob container where the backup resides. This would be the exact value that is returned as the result of a
     * backup operation. An example of such a URL may look like the following: https://contoso.blob.core.windows.net/backup/mhsm-contoso-2020090117323313.
     * @param sasToken A Shared Access Signature (SAS) token to authorize access to the blob.
     * @return A {@link PollerFlux} polling on the {@link KeyVaultRestoreOperation restore operation} status.
     * @throws NullPointerException if the {@code backupFolderUrl} or {@code sasToken} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<KeyVaultRestoreOperation, Void> beginRestore(String backupFolderUrl, String sasToken) {
        return beginRestore(backupFolderUrl, sasToken, getDefaultPollingInterval());
    }

    /**
     * Initiates a full restore of the Key Vault.
     *
     * @param backupFolderUrl The URL for the Blob Storage resource where the backup is located, including the path to
     * the blob container where the backup resides. This would be the exact value that is returned as the result of a
     * backup operation. An example of such a URL may look like the following: https://contoso.blob.core.windows.net/backup/mhsm-contoso-2020090117323313.
     * @param sasToken A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param pollingInterval The interval at which the operation status will be polled for.
     * @return A {@link PollerFlux} polling on the {@link KeyVaultRestoreOperation restore operation} status.
     * @throws NullPointerException if the {@code backupFolderUrl} or {@code sasToken} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<KeyVaultRestoreOperation, Void> beginRestore(String backupFolderUrl, String sasToken, Duration pollingInterval) {
        Objects.requireNonNull(backupFolderUrl,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'backupFolderUrl'"));
        Objects.requireNonNull(sasToken,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'sasToken'"));

        return new PollerFlux<>(pollingInterval,
            restoreActivationOperation(backupFolderUrl, sasToken),
            restorePollOperation(),
            (pollingContext, firstResponse) -> Mono.empty(),
            (pollingContext) -> Mono.empty());
    }

    /**
     * Initiates a full restore of the Key Vault.
     *
     * @param backupFolderUrl The URL for the Blob Storage resource where the backup is located, including the path to
     * the blob container where the backup resides. This would be the exact value that is returned as the result of a
     * backup operation. An example of such a URL may look like the following: https://contoso.blob.core.windows.net/backup/mhsm-contoso-2020090117323313.
     * @param sasToken A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link PollerFlux} polling on the {@link KeyVaultRestoreOperation backup operation} status.
     */
    Mono<Response<KeyVaultRestoreOperation>> restoreWithResponse(String backupFolderUrl, String sasToken, Context context) {
        String[] segments = backupFolderUrl.split("/");
        String folderName = segments[segments.length - 1];
        String containerUrl = backupFolderUrl.substring(0, backupFolderUrl.length() - folderName.length());

        SASTokenParameter sasTokenParameter = new SASTokenParameter()
            .setStorageResourceUri(containerUrl)
            .setToken(sasToken);

        RestoreOperationParameters restoreOperationParameters = new RestoreOperationParameters()
            .setSasTokenParameters(sasTokenParameter)
            .setFolderToRestore(folderName);

        try {
            return clientImpl.fullRestoreOperationWithResponseAsync(vaultUrl, restoreOperationParameters,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.info("Restoring from location - {}", backupFolderUrl))
                .doOnSuccess(response -> logger.info("Restored from location - {}", backupFolderUrl))
                .doOnError(error ->
                    logger.warning("Failed to restore from location - {}", backupFolderUrl, error))
                .map(restoreOperationResponse ->
                    new SimpleResponse<>(restoreOperationResponse.getRequest(),
                        restoreOperationResponse.getStatusCode(),
                        restoreOperationResponse.getHeaders(),
                        (KeyVaultRestoreOperation) transformToLongRunningOperation(
                            restoreOperationResponse.getValue())));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    private Function<PollingContext<KeyVaultRestoreOperation>, Mono<KeyVaultRestoreOperation>> restoreActivationOperation(String backupFolderUrl, String sasToken) {
        return (pollingContext) -> {
            try {
                return withContext(context -> restoreWithResponse(backupFolderUrl, sasToken, context))
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
                    .flatMap(KeyVaultBackupAsyncClient::processRestoreOperationResponse);
            } catch (HttpResponseException e) {
                //noinspection ThrowableNotThrown
                logger.logExceptionAsError(e);

                return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null));
            } catch (RuntimeException e) {
                return monoError(logger, e);
            }
        };
    }

    private static Mono<PollResponse<KeyVaultRestoreOperation>> processRestoreOperationResponse(Response<KeyVaultRestoreOperation> response) {
        String operationStatus = response.getValue().getStatus().toLowerCase(Locale.US);

        return Mono.just(new PollResponse<>(
            toLongRunningOperationStatus(operationStatus.toLowerCase(Locale.US)), response.getValue()));
    }

    /**
     * Restores all versions of a given key using the supplied SAS token pointing to a previously stored Azure Blob
     * storage backup folder.
     *
     * @param keyName The name of the key to be restored.
     * @param backupFolderUrl The URL for the Blob Storage resource where the backup is located, including the path to
     * the blob container where the backup resides. This would be the exact value that is returned as the result of a
     * backup operation. An example of such a URL may look like the following: https://contoso.blob.core.windows.net/backup/mhsm-contoso-2020090117323313.
     * @param sasToken A Shared Access Signature (SAS) token to authorize access to the blob.
     * @return A {@link PollerFlux} polling on the {@link KeyVaultRestoreOperation restore operation} status.
     * @throws NullPointerException if the {@code keyName}, {@code backupFolderUrl} or {@code sasToken} are {@code
     * null}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<KeyVaultRestoreOperation, Void> beginSelectiveRestore(String keyName, String backupFolderUrl, String sasToken) {
        return beginSelectiveRestore(keyName, backupFolderUrl, sasToken, getDefaultPollingInterval());
    }

    /**
     * Restores all versions of a given key using the supplied SAS token pointing to a previously stored Azure Blob
     * storage backup folder.
     *
     * @param keyName The name of the key to be restored.
     * @param backupFolderUrl The URL for the Blob Storage resource where the backup is located, including the path to
     * the blob container where the backup resides. This would be the exact value that is returned as the result of a
     * backup operation. An example of such a URL may look like the following: https://contoso.blob.core.windows.net/backup/mhsm-contoso-2020090117323313.
     * @param sasToken A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param pollingInterval The interval at which the operation status will be polled for.
     * @return A {@link PollerFlux} polling on the {@link KeyVaultRestoreOperation restore operation} status.
     * @throws NullPointerException if the {@code keyName}, {@code backupFolderUrl} or {@code sasToken} are {@code
     * null}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<KeyVaultRestoreOperation, Void> beginSelectiveRestore(String keyName, String backupFolderUrl, String sasToken, Duration pollingInterval) {
        Objects.requireNonNull(keyName,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'keyName'"));
        Objects.requireNonNull(backupFolderUrl,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'backupFolderUrl'"));
        Objects.requireNonNull(sasToken,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'sasToken'"));

        return new PollerFlux<>(pollingInterval,
            selectiveRestoreActivationOperation(keyName, backupFolderUrl, sasToken),
            selectiveRestorePollOperation(),
            (pollingContext, firstResponse) -> Mono.empty(),
            (pollingContext) -> Mono.empty());
    }

    /**
     * Restores all versions of a given key using the supplied SAS token pointing to a previously stored Azure Blob
     * storage backup folder.
     *
     * @param keyName The name of the key to be restored.
     * @param backupFolderUrl The URL for the Blob Storage resource where the backup is located, including the path to
     * the blob container where the backup resides. This would be the exact value that is returned as the result of a
     * backup operation. An example of such a URL may look like the following: https://contoso.blob.core.windows.net/backup/mhsm-contoso-2020090117323313.
     * @param sasToken A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link PollerFlux} polling on the {@link KeyVaultRestoreOperation backup operation} status.
     */
    Mono<Response<KeyVaultRestoreOperation>> selectiveRestoreWithResponse(String keyName, String backupFolderUrl, String sasToken, Context context) {
        String[] segments = backupFolderUrl.split("/");
        String folderName = segments[segments.length - 1];
        String containerUrl = backupFolderUrl.substring(0, backupFolderUrl.length() - folderName.length());

        SASTokenParameter sasTokenParameter = new SASTokenParameter()
            .setStorageResourceUri(containerUrl)
            .setToken(sasToken);

        SelectiveKeyRestoreOperationParameters selectiveKeyRestoreOperationParameters =
            new SelectiveKeyRestoreOperationParameters()
                .setSasTokenParameters(sasTokenParameter)
                .setFolder(folderName);

        try {
            return clientImpl.selectiveKeyRestoreOperationWithResponseAsync(vaultUrl, keyName,
                selectiveKeyRestoreOperationParameters, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored ->
                    logger.info("Restoring key \"{}\" from location - {}", keyName, backupFolderUrl))
                .doOnSuccess(response ->
                    logger.info("Restored key \"{}\" from location - {}", keyName, backupFolderUrl))
                .doOnError(error ->
                    logger.warning("Failed to restore key \"{}\" from location - {}", keyName, backupFolderUrl, error))
                .map(restoreOperationResponse ->
                    new SimpleResponse<>(restoreOperationResponse.getRequest(),
                        restoreOperationResponse.getStatusCode(),
                        restoreOperationResponse.getHeaders(),
                        (KeyVaultRestoreOperation) transformToLongRunningOperation(
                            restoreOperationResponse.getValue())));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    private Function<PollingContext<KeyVaultRestoreOperation>, Mono<KeyVaultRestoreOperation>> selectiveRestoreActivationOperation(String keyName, String backupFolderUrl, String sasToken) {
        return (pollingContext) -> {
            try {
                return withContext(context -> selectiveRestoreWithResponse(keyName, backupFolderUrl, sasToken, context))
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
                    .flatMap(KeyVaultBackupAsyncClient::processRestoreOperationResponse);
            } catch (HttpResponseException e) {
                //noinspection ThrowableNotThrown
                logger.logExceptionAsError(e);

                return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null));
            } catch (RuntimeException e) {
                return monoError(logger, e);
            }
        };
    }

    private static <O> KeyVaultLongRunningOperation transformToLongRunningOperation(O operation) {
        if (operation instanceof RestoreOperation) {
            RestoreOperation restoreOperation = (RestoreOperation) operation;

            return new KeyVaultRestoreOperation(restoreOperation.getStatus(), restoreOperation.getStatusDetails(),
                createKeyVaultErrorFromError(restoreOperation.getError()), restoreOperation.getJobId(),
                longToOffsetDateTime(restoreOperation.getStartTime()),
                longToOffsetDateTime(restoreOperation.getEndTime()));
        } else if (operation instanceof SelectiveKeyRestoreOperation) {
            SelectiveKeyRestoreOperation selectiveKeyRestoreOperation = (SelectiveKeyRestoreOperation) operation;

            return new KeyVaultRestoreOperation(selectiveKeyRestoreOperation.getStatus(),
                selectiveKeyRestoreOperation.getStatusDetails(),
                createKeyVaultErrorFromError(selectiveKeyRestoreOperation.getError()),
                selectiveKeyRestoreOperation.getJobId(),
                longToOffsetDateTime(selectiveKeyRestoreOperation.getStartTime()),
                longToOffsetDateTime(selectiveKeyRestoreOperation.getEndTime()));
        } else if (operation instanceof FullBackupOperation) {
            FullBackupOperation fullBackupOperation = (FullBackupOperation) operation;

            return new KeyVaultBackupOperation(fullBackupOperation.getStatus(), fullBackupOperation.getStatusDetails(),
                createKeyVaultErrorFromError(fullBackupOperation.getError()), fullBackupOperation.getJobId(),
                longToOffsetDateTime(fullBackupOperation.getStartTime()),
                longToOffsetDateTime(fullBackupOperation.getEndTime()),
                fullBackupOperation.getAzureStorageBlobContainerUri());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private static KeyVaultError createKeyVaultErrorFromError(Error error) {
        if (error == null) {
            return null;
        }

        return
            new KeyVaultError(error.getCode(), error.getMessage(), createKeyVaultErrorFromError(error.getInnerError()));
    }

    private static OffsetDateTime longToOffsetDateTime(Long epochInSeconds) {
        return epochInSeconds == null ? null
            : OffsetDateTime.ofInstant(Instant.ofEpochSecond(epochInSeconds), ZoneOffset.UTC);
    }
}

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
import com.azure.security.keyvault.administration.implementation.KeyVaultErrorCodeStrings;
import com.azure.security.keyvault.administration.implementation.models.PreBackupOperationParameters;
import com.azure.security.keyvault.administration.implementation.models.PreRestoreOperationParameters;
import com.azure.security.keyvault.administration.implementation.models.RestoreOperation;
import com.azure.security.keyvault.administration.implementation.models.RestoreOperationParameters;
import com.azure.security.keyvault.administration.implementation.models.SASTokenParameter;
import com.azure.security.keyvault.administration.implementation.models.SelectiveKeyRestoreOperationParameters;
import com.azure.security.keyvault.administration.models.KeyVaultAdministrationException;
import com.azure.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.security.keyvault.administration.models.KeyVaultLongRunningOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreResult;
import com.azure.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreOperation;
import com.azure.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreResult;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.security.keyvault.administration.KeyVaultAdministrationUtil.toLongRunningOperationStatus;
import static com.azure.security.keyvault.administration.KeyVaultAdministrationUtil.transformToLongRunningOperation;
import static com.azure.security.keyvault.administration.implementation.KeyVaultAdministrationUtils.createKeyVaultErrorFromError;

/**
 * The {@link KeyVaultBackupAsyncClient} provides asynchronous methods to perform full a backup and restore of a key
 * vault, as well as selectively restoring specific keys from a backup.
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Azure Key Vault service, you will need to create an instance of the
 * {@link KeyVaultBackupAsyncClient} class, a vault url and a credential object.</p>
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
 * <p>The following code sample demonstrates the creation of a {@link KeyVaultBackupAsyncClient}, using the
 * {@link KeyVaultBackupClientBuilder} to configure it.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.instantiation -->
 * <pre>
 * KeyVaultBackupAsyncClient keyVaultBackupAsyncClient = new KeyVaultBackupClientBuilder&#40;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-managed-hsm-url&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.instantiation -->
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Back Up a Collection of Keys</h2>
 * The {@link KeyVaultBackupAsyncClient} can be used to back up the entire collection of keys from a key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously back up an entire collection of keys using, using the
 * {@link KeyVaultBackupAsyncClient#beginBackup(String, String)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginBackup#String-String -->
 * <pre>
 * String blobStorageUrl = &quot;https:&#47;&#47;myaccount.blob.core.windows.net&#47;myContainer&quot;;
 * String sasToken = &quot;&lt;sas-token&gt;&quot;;
 *
 * client.beginBackup&#40;blobStorageUrl, sasToken&#41;
 *     .setPollInterval&#40;Duration.ofSeconds&#40;1&#41;&#41; &#47;&#47; You can set a custom polling interval.
 *     .doOnError&#40;e -&gt; System.out.printf&#40;&quot;Backup failed with error: %s.%n&quot;, e.getMessage&#40;&#41;&#41;&#41;
 *     .doOnNext&#40;pollResponse -&gt;
 *         System.out.printf&#40;&quot;The current status of the operation is: %s.%n&quot;, pollResponse.getStatus&#40;&#41;&#41;&#41;
 *     .filter&#40;pollResponse -&gt; pollResponse.getStatus&#40;&#41; == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41;
 *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
 *     .subscribe&#40;folderUrl -&gt;
 *         System.out.printf&#40;&quot;Backup completed. The storage location of this backup is: %s.%n&quot;, folderUrl&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginBackup#String-String -->
 *
 * <p><strong>Note:</strong> For the synchronous sample, refer to {@link KeyVaultBackupClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Restore a Collection of Keys</h2>
 * The {@link KeyVaultBackupClient} can be used to restore an entire collection of keys from a backup.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously restore an entire collection of keys from a backup,
 * using the {@link KeyVaultBackupClient#beginRestore(String, String)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginRestore#String-String -->
 * <pre>
 * String folderUrl = &quot;https:&#47;&#47;myaccount.blob.core.windows.net&#47;myContainer&#47;mhsm-myaccount-2020090117323313&quot;;
 * String sasToken = &quot;&lt;sas-token&gt;&quot;;
 *
 * client.beginRestore&#40;folderUrl, sasToken&#41;
 *     .setPollInterval&#40;Duration.ofSeconds&#40;1&#41;&#41; &#47;&#47; You can set a custom polling interval.
 *     .doOnError&#40;e -&gt; System.out.printf&#40;&quot;Restore failed with error: %s.%n&quot;, e.getMessage&#40;&#41;&#41;&#41;
 *     .doOnNext&#40;pollResponse -&gt;
 *         System.out.printf&#40;&quot;The current status of the operation is: %s.%n&quot;, pollResponse.getStatus&#40;&#41;&#41;&#41;
 *     .filter&#40;pollResponse -&gt; pollResponse.getStatus&#40;&#41; == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41;
 *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
 *     .subscribe&#40;unused -&gt; System.out.printf&#40;&quot;Backup restored successfully.%n&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginRestore#String-String -->
 *
 * <p><strong>Note:</strong> For the synchronous sample, refer to {@link KeyVaultBackupClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Selectively Restore a Key</h2>
 * The {@link KeyVaultBackupAsyncClient} can be used to restore a specific key from a backup.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously restore a specific key from a backup, using
 * the {@link KeyVaultBackupAsyncClient#beginSelectiveKeyRestore(String, String, String)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginSelectiveKeyRestore#String-String-String -->
 * <pre>
 * String folderUrl = &quot;https:&#47;&#47;myaccount.blob.core.windows.net&#47;myContainer&#47;mhsm-myaccount-2020090117323313&quot;;
 * String sasToken = &quot;&lt;sas-token&gt;&quot;;
 * String keyName = &quot;myKey&quot;;
 *
 * client.beginSelectiveKeyRestore&#40;folderUrl, sasToken, keyName&#41;
 *     .setPollInterval&#40;Duration.ofSeconds&#40;1&#41;&#41; &#47;&#47; You can set a custom polling interval.
 *     .doOnError&#40;e -&gt; System.out.printf&#40;&quot;Key restoration failed with error: %s.%n&quot;, e.getMessage&#40;&#41;&#41;&#41;
 *     .doOnNext&#40;pollResponse -&gt;
 *         System.out.printf&#40;&quot;The current status of the operation is: %s.%n&quot;, pollResponse.getStatus&#40;&#41;&#41;&#41;
 *     .filter&#40;pollResponse -&gt; pollResponse.getStatus&#40;&#41; == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41;
 *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
 *     .subscribe&#40;unused -&gt; System.out.printf&#40;&quot;Key restored successfully.%n&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginSelectiveKeyRestore#String-String-String -->
 *
 * <p><strong>Note:</strong> For the synchronous sample, refer to {@link KeyVaultBackupClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * @see com.azure.security.keyvault.administration
 * @see KeyVaultBackupClientBuilder
 */
@ServiceClient(builder = KeyVaultBackupClientBuilder.class, isAsync = true)
public final class KeyVaultBackupAsyncClient {
    /**
     * The logger to be used.
     */
    private static final ClientLogger LOGGER = new ClientLogger(KeyVaultBackupAsyncClient.class);

    private static final Duration DEFAULT_POLLING_INTERVAL = Duration.ofSeconds(1);

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
    KeyVaultBackupAsyncClient(URL vaultUrl, HttpPipeline httpPipeline,
                              KeyVaultAdministrationServiceVersion serviceVersion) {
        Objects.requireNonNull(vaultUrl, KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED);

        this.vaultUrl = vaultUrl.toString();
        this.serviceVersion = serviceVersion.getVersion();
        this.pipeline = httpPipeline;

        clientImpl = new KeyVaultBackupClientImpl(httpPipeline, this.serviceVersion);
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
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    HttpPipeline getHttpPipeline() {
        return this.pipeline;
    }

    /**
     * Initiates a full backup of the Key Vault.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Starts a {@link KeyVaultBackupOperation backup operation}, polls for its status and waits for it to complete.
     * Prints out the details of the operation's final result in case of success or prints out details of an error in
     * case the operation fails.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginBackup#String-String -->
     * <pre>
     * String blobStorageUrl = &quot;https:&#47;&#47;myaccount.blob.core.windows.net&#47;myContainer&quot;;
     * String sasToken = &quot;&lt;sas-token&gt;&quot;;
     *
     * client.beginBackup&#40;blobStorageUrl, sasToken&#41;
     *     .setPollInterval&#40;Duration.ofSeconds&#40;1&#41;&#41; &#47;&#47; You can set a custom polling interval.
     *     .doOnError&#40;e -&gt; System.out.printf&#40;&quot;Backup failed with error: %s.%n&quot;, e.getMessage&#40;&#41;&#41;&#41;
     *     .doOnNext&#40;pollResponse -&gt;
     *         System.out.printf&#40;&quot;The current status of the operation is: %s.%n&quot;, pollResponse.getStatus&#40;&#41;&#41;&#41;
     *     .filter&#40;pollResponse -&gt; pollResponse.getStatus&#40;&#41; == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;folderUrl -&gt;
     *         System.out.printf&#40;&quot;Backup completed. The storage location of this backup is: %s.%n&quot;, folderUrl&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginBackup#String-String -->
     *
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup will be located.
     * @param sasToken Optional Shared Access Signature (SAS) token to authorize access to the blob. If {@code null},
     * Managed Identity will be used to authenticate instead.
     *
     * @return A {@link PollerFlux} polling on the {@link KeyVaultBackupOperation backup operation} status.
     *
     * @throws KeyVaultAdministrationException If the given {@code blobStorageUrl} or {@code sasToken} are invalid.
     * @throws NullPointerException If the {@code blobStorageUrl} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<KeyVaultBackupOperation, String> beginBackup(String blobStorageUrl, String sasToken) {
        if (blobStorageUrl == null) {
            throw LOGGER.logExceptionAsError(
                new NullPointerException(
                    String.format(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED, "'blobStorageUrl'")));
        }

        return new PollerFlux<>(getDefaultPollingInterval(),
            backupActivationOperation(blobStorageUrl, sasToken),
            backupPollOperation(),
            (pollingContext, firstResponse) ->
                Mono.error(LOGGER.logExceptionAsError(new RuntimeException("Cancellation is not supported"))),
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
     * @return A {@link PollerFlux} polling on the {@link KeyVaultBackupOperation backup operation} status.
     *
     * @throws KeyVaultAdministrationException If the given {@code blobStorageUrl} or {@code sasToken} are invalid.
     */
    Mono<Response<KeyVaultBackupOperation>> backupWithResponse(String blobStorageUrl, String sasToken,
                                                               Context context) {
        SASTokenParameter sasTokenParameter = new SASTokenParameter(blobStorageUrl)
            .setToken(sasToken)
            .setUseManagedIdentity(sasToken == null);

        try {
            return clientImpl.fullBackupWithResponseAsync(vaultUrl, sasTokenParameter,
                context)
                .doOnRequest(ignored -> LOGGER.verbose("Backing up at URL - {}", blobStorageUrl))
                .doOnSuccess(response -> LOGGER.verbose("Backed up at URL - {}",
                    response.getValue().getAzureStorageBlobContainerUri()))
                .doOnError(error -> LOGGER.warning("Failed to backup at URL - {}", blobStorageUrl, error))
                .map(backupOperationResponse ->
                    new SimpleResponse<>(backupOperationResponse.getRequest(), backupOperationResponse.getStatusCode(),
                        backupOperationResponse.getHeaders(),
                        (KeyVaultBackupOperation) transformToLongRunningOperation(backupOperationResponse.getValue())));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    private Function<PollingContext<KeyVaultBackupOperation>, Mono<KeyVaultBackupOperation>> backupActivationOperation(
        String blobStorageUrl, String sasToken) {

        return (pollingContext) -> {
            try {
                return withContext(context -> backupWithResponse(blobStorageUrl, sasToken, context))
                    .flatMap(backupResponse -> Mono.just(backupResponse.getValue()));
            } catch (RuntimeException e) {
                return monoError(LOGGER, e);
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
                    LOGGER.warning("Backup operation does not exist. Activation operation failed.");

                    return Mono.just(new PollResponse<KeyVaultBackupOperation>(
                        LongRunningOperationStatus.fromString("BACKUP_START_FAILED", true), null));
                }

                final String jobId = keyVaultBackupOperation.getOperationId();

                return withContext(context -> clientImpl.fullBackupStatusWithResponseAsync(vaultUrl, jobId,
                    context))
                    .map(response ->
                        new SimpleResponse<>(response,
                            (KeyVaultBackupOperation) transformToLongRunningOperation(response.getValue())))
                    .flatMap(KeyVaultBackupAsyncClient::processBackupOperationResponse);
            } catch (HttpResponseException e) {
                //noinspection ThrowableNotThrown
                LOGGER.logExceptionAsError(e);

                return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null));
            } catch (RuntimeException e) {
                return monoError(LOGGER, e);
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
                return monoError(LOGGER, e);
            }
        };
    }

    private static Mono<PollResponse<KeyVaultBackupOperation>> processBackupOperationResponse(
        Response<KeyVaultBackupOperation> response) {

        String operationStatus = response.getValue().getStatus().toLowerCase(Locale.US);

        return Mono.just(new PollResponse<>(
            toLongRunningOperationStatus(operationStatus.toLowerCase(Locale.US)), response.getValue()));
    }

    /**
     * Initiates a pre-backup check on the Key Vault. This operation checks if it is possible to back up the entire
     * collection of keys from a key vault.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Starts a {@link KeyVaultBackupOperation pre-backup operation}, polls for its status and waits for it to
     * complete. Prints out the details of the operation's final result in case of success or prints out details of an
     * error in case the operation fails.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginPreBackup#String-String -->
     * <pre>
     * String blobStorageUrl = &quot;https:&#47;&#47;myaccount.blob.core.windows.net&#47;myContainer&quot;;
     * String sasToken = &quot;&lt;sas-token&gt;&quot;;
     *
     * client.beginPreBackup&#40;blobStorageUrl, sasToken&#41;
     *     .setPollInterval&#40;Duration.ofSeconds&#40;1&#41;&#41; &#47;&#47; You can set a custom polling interval.
     *     .doOnError&#40;e -&gt; System.out.printf&#40;&quot;Pre-backup check failed with error: %s.%n&quot;, e.getMessage&#40;&#41;&#41;&#41;
     *     .doOnNext&#40;pollResponse -&gt;
     *         System.out.printf&#40;&quot;The current status of the operation is: %s.%n&quot;, pollResponse.getStatus&#40;&#41;&#41;&#41;
     *     .filter&#40;pollResponse -&gt; pollResponse.getStatus&#40;&#41; == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;unused -&gt; System.out.printf&#40;&quot;Pre-backup check completed successfully.%n&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginPreBackup#String-String -->
     *
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup will be located.
     * @param sasToken Optional Shared Access Signature (SAS) token to authorize access to the blob. If {@code null},
     * Managed Identity will be used to authenticate instead.
     *
     * @return A {@link PollerFlux} polling on the {@link KeyVaultBackupOperation pre-backup operation} status.
     *
     * @throws KeyVaultAdministrationException If the given {@code blobStorageUrl} or {@code sasToken} are invalid.
     * @throws NullPointerException If the {@code blobStorageUrl} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<KeyVaultBackupOperation, String> beginPreBackup(String blobStorageUrl, String sasToken) {
        if (blobStorageUrl == null) {
            throw LOGGER.logExceptionAsError(
                new NullPointerException(
                    String.format(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED, "'blobStorageUrl'")));
        }

        return new PollerFlux<>(getDefaultPollingInterval(),
            preBackupActivationOperation(blobStorageUrl, sasToken),
            backupPollOperation(),
            (pollingContext, firstResponse) ->
                Mono.error(LOGGER.logExceptionAsError(new RuntimeException("Cancellation is not supported"))),
            backupFetchOperation());
    }

    /**
     * Initiates a pre-backup check on the Key Vault. This operation checks if it is possible to back up the entire
     * collection of keys from a key vault.
     *
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup will be located.
     * @param sasToken Optional Shared Access Signature (SAS) token to authorize access to the blob. If {@code null},
     * Managed Identity will be used to authenticate instead.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link PollerFlux} polling on the {@link KeyVaultBackupOperation pre-backup operation} status.
     *
     * @throws KeyVaultAdministrationException If the given {@code blobStorageUrl} or {@code sasToken} are invalid.
     */
    Mono<Response<KeyVaultBackupOperation>> preBackupWithResponse(String blobStorageUrl, String sasToken,
                                                                  Context context) {
        PreBackupOperationParameters preBackupOperationParameters = new PreBackupOperationParameters()
            .setStorageResourceUri(blobStorageUrl)
            .setToken(sasToken)
            .setUseManagedIdentity(sasToken == null);

        try {
            return clientImpl.preFullBackupWithResponseAsync(vaultUrl, preBackupOperationParameters, context)
                .doOnRequest(ignored -> LOGGER.verbose("Backing up at URL - {}", blobStorageUrl))
                .doOnSuccess(response -> LOGGER.verbose("Backed up at URL - {}",
                    response.getValue().getAzureStorageBlobContainerUri()))
                .doOnError(error -> LOGGER.warning("Failed to backup at URL - {}", blobStorageUrl, error))
                .map(backupOperationResponse ->
                    new SimpleResponse<>(backupOperationResponse.getRequest(), backupOperationResponse.getStatusCode(),
                        backupOperationResponse.getHeaders(),
                        (KeyVaultBackupOperation) transformToLongRunningOperation(backupOperationResponse.getValue())));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    private Function<PollingContext<KeyVaultBackupOperation>, Mono<KeyVaultBackupOperation>> preBackupActivationOperation(String blobStorageUrl, String sasToken) {
        return (pollingContext) -> {
            try {
                return withContext(context -> preBackupWithResponse(blobStorageUrl, sasToken, context))
                    .flatMap(backupResponse -> Mono.just(backupResponse.getValue()));
            } catch (RuntimeException e) {
                return monoError(LOGGER, e);
            }
        };
    }

    /**
     * Initiates a full restore of the Key Vault.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Starts a {@link KeyVaultRestoreOperation restore operation}, polls for its status and waits for it to
     * complete. Prints out error details in case the operation fails.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginRestore#String-String -->
     * <pre>
     * String folderUrl = &quot;https:&#47;&#47;myaccount.blob.core.windows.net&#47;myContainer&#47;mhsm-myaccount-2020090117323313&quot;;
     * String sasToken = &quot;&lt;sas-token&gt;&quot;;
     *
     * client.beginRestore&#40;folderUrl, sasToken&#41;
     *     .setPollInterval&#40;Duration.ofSeconds&#40;1&#41;&#41; &#47;&#47; You can set a custom polling interval.
     *     .doOnError&#40;e -&gt; System.out.printf&#40;&quot;Restore failed with error: %s.%n&quot;, e.getMessage&#40;&#41;&#41;&#41;
     *     .doOnNext&#40;pollResponse -&gt;
     *         System.out.printf&#40;&quot;The current status of the operation is: %s.%n&quot;, pollResponse.getStatus&#40;&#41;&#41;&#41;
     *     .filter&#40;pollResponse -&gt; pollResponse.getStatus&#40;&#41; == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;unused -&gt; System.out.printf&#40;&quot;Backup restored successfully.%n&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginRestore#String-String -->
     *
     * @param folderUrl The URL for the Blob Storage resource where the backup is located, including the path to
     * the blob container where the backup resides. This would be the exact value that is returned as the result of a
     * backup operation. An example of such a URL may look like the following:
     * https://contoso.blob.core.windows.net/backup/mhsm-contoso-2020090117323313.
     * @param sasToken Optional Shared Access Signature (SAS) token to authorize access to the blob. If {@code null},
     * Managed Identity will be used to authenticate instead.
     *
     * @return A {@link PollerFlux} polling on the {@link KeyVaultRestoreOperation restore operation} status.
     *
     * @throws KeyVaultAdministrationException If the given {@code folderUrl} or {@code sasToken} are invalid.
     * @throws NullPointerException If the {@code folderUrl} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<KeyVaultRestoreOperation, KeyVaultRestoreResult> beginRestore(String folderUrl, String sasToken) {
        if (folderUrl == null) {
            throw LOGGER.logExceptionAsError(
                new NullPointerException(String.format(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED, "'folderUrl'")));
        }

        return new PollerFlux<>(getDefaultPollingInterval(),
            restoreActivationOperation(folderUrl, sasToken),
            restorePollOperation(),
            (pollingContext, firstResponse) ->
                Mono.error(LOGGER.logExceptionAsError(new RuntimeException("Cancellation is not supported"))),
            (pollingContext) -> Mono.just(new KeyVaultRestoreResult()));
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
     * @return A {@link PollerFlux} polling on the {@link KeyVaultRestoreOperation backup operation} status.
     *
     * @throws KeyVaultAdministrationException If the given {@code folderUrl} or {@code sasToken} are invalid.
     */
    Mono<Response<KeyVaultRestoreOperation>> restoreWithResponse(String folderUrl, String sasToken, Context context) {
        String[] segments = folderUrl.split("/");
        String folderName = segments[segments.length - 1];
        String containerUrl = folderUrl.substring(0, folderUrl.length() - folderName.length());

        SASTokenParameter sasTokenParameter = new SASTokenParameter(containerUrl)
            .setToken(sasToken)
            .setUseManagedIdentity(sasToken == null);

        RestoreOperationParameters restoreOperationParameters =
            new RestoreOperationParameters(sasTokenParameter, folderName);

        try {
            return clientImpl.fullRestoreOperationWithResponseAsync(vaultUrl, restoreOperationParameters,
                context)
                .doOnRequest(ignored -> LOGGER.verbose("Restoring from location - {}", folderUrl))
                .doOnSuccess(response -> LOGGER.verbose("Restored from location - {}", folderUrl))
                .doOnError(error ->
                    LOGGER.warning("Failed to restore from location - {}", folderUrl, error))
                .map(restoreOperationResponse ->
                    new SimpleResponse<>(restoreOperationResponse.getRequest(),
                        restoreOperationResponse.getStatusCode(),
                        restoreOperationResponse.getHeaders(),
                        (KeyVaultRestoreOperation) transformToLongRunningOperation(
                            restoreOperationResponse.getValue())));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    private Function<PollingContext<KeyVaultRestoreOperation>, Mono<KeyVaultRestoreOperation>> restoreActivationOperation(String folderUrl, String sasToken) {
        return (pollingContext) -> {
            try {
                return withContext(context -> restoreWithResponse(folderUrl, sasToken, context))
                    .flatMap(restoreResponse -> Mono.just(restoreResponse.getValue()));
            } catch (RuntimeException e) {
                return monoError(LOGGER, e);
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
                    LOGGER.warning("Restore operation does not exist. Activation operation failed.");

                    return Mono.just(new PollResponse<KeyVaultRestoreOperation>(
                        LongRunningOperationStatus.fromString("RESTORE_START_FAILED", true), null));
                }

                final String jobId = keyVaultRestoreOperation.getOperationId();

                return withContext(context -> clientImpl.restoreStatusWithResponseAsync(vaultUrl, jobId,
                    context))
                    .map(response ->
                        new SimpleResponse<>(response,
                            (KeyVaultRestoreOperation) transformToLongRunningOperation(response.getValue())))
                    .flatMap(KeyVaultBackupAsyncClient::processRestoreOperationResponse);
            } catch (HttpResponseException e) {
                //noinspection ThrowableNotThrown
                LOGGER.logExceptionAsError(e);

                return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null));
            } catch (RuntimeException e) {
                return monoError(LOGGER, e);
            }
        };
    }

    static Mono<PollResponse<KeyVaultRestoreOperation>> processRestoreOperationResponse(
        Response<KeyVaultRestoreOperation> response) {

        String operationStatus = response.getValue().getStatus().toLowerCase(Locale.US);

        return Mono.just(new PollResponse<>(
            toLongRunningOperationStatus(operationStatus.toLowerCase(Locale.US)), response.getValue()));
    }

    /**
     * Initiates a pre-restore check on the Key Vault. This operation checks if it is possible to restore an entire
     * collection of keys from a backup.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Starts a {@link KeyVaultRestoreOperation pre-restore operation}, polls for its status and waits for it to
     * complete. Prints out error details in case the operation fails.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginPreRestore#String-String -->
     * <pre>
     * String folderUrl = &quot;https:&#47;&#47;myaccount.blob.core.windows.net&#47;myContainer&#47;mhsm-myaccount-2020090117323313&quot;;
     * String sasToken = &quot;&lt;sas-token&gt;&quot;;
     *
     * client.beginPreRestore&#40;folderUrl, sasToken&#41;
     *     .setPollInterval&#40;Duration.ofSeconds&#40;1&#41;&#41; &#47;&#47; You can set a custom polling interval.
     *     .doOnError&#40;e -&gt; System.out.printf&#40;&quot;Pre-restore check failed with error: %s.%n&quot;, e.getMessage&#40;&#41;&#41;&#41;
     *     .doOnNext&#40;pollResponse -&gt;
     *         System.out.printf&#40;&quot;The current status of the operation is: %s.%n&quot;, pollResponse.getStatus&#40;&#41;&#41;&#41;
     *     .filter&#40;pollResponse -&gt; pollResponse.getStatus&#40;&#41; == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;unused -&gt; System.out.printf&#40;&quot;Pre-restore check completed successfully.%n&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginPreRestore#String-String -->
     *
     * @param folderUrl The URL for the Blob Storage resource where the backup is located, including the path to
     * the blob container where the backup resides. This would be the exact value that is returned as the result of a
     * backup operation. An example of such a URL may look like the following:
     * {@code https://contoso.blob.core.windows.net/backup/mhsm-contoso-2020090117323313}.
     * @param sasToken Optional Shared Access Signature (SAS) token to authorize access to the blob. If {@code null},
     * Managed Identity will be used to authenticate instead.
     *
     * @return A {@link PollerFlux} polling on the {@link KeyVaultRestoreOperation restore operation} status.
     *
     * @throws KeyVaultAdministrationException If the given {@code folderUrl} or {@code sasToken} are invalid.
     * @throws NullPointerException If the {@code folderUrl} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<KeyVaultRestoreOperation, KeyVaultRestoreResult> beginPreRestore(String folderUrl,
                                                                                       String sasToken) {
        if (folderUrl == null) {
            throw LOGGER.logExceptionAsError(
                new NullPointerException(String.format(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED, "'folderUrl'")));
        }

        return new PollerFlux<>(getDefaultPollingInterval(),
            preRestoreActivationOperation(folderUrl, sasToken),
            restorePollOperation(),
            (pollingContext, firstResponse) ->
                Mono.error(LOGGER.logExceptionAsError(new RuntimeException("Cancellation is not supported"))),
            (pollingContext) -> Mono.just(new KeyVaultRestoreResult()));
    }

    /**
     * Initiates a pre-restore check on the Key Vault. This operation checks if it is possible to restore an entire
     * collection of keys from a backup.
     *
     * @param folderUrl The URL for the Blob Storage resource where the backup is located, including the path to
     * the blob container where the backup resides. This would be the exact value that is returned as the result of a
     * backup operation. An example of such a URL may look like the following:
     * {@code https://contoso.blob.core.windows.net/backup/mhsm-contoso-2020090117323313}.
     * @param sasToken Optional Shared Access Signature (SAS) token to authorize access to the blob. If {@code null},
     * Managed Identity will be used to authenticate instead.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link PollerFlux} polling on the {@link KeyVaultRestoreOperation backup operation} status.
     *
     * @throws KeyVaultAdministrationException If the given {@code folderUrl} or {@code sasToken} are invalid.
     */
    Mono<Response<KeyVaultRestoreOperation>> preRestoreWithResponse(String folderUrl, String sasToken,
                                                                    Context context) {
        String[] segments = folderUrl.split("/");
        String folderName = segments[segments.length - 1];
        String containerUrl = folderUrl.substring(0, folderUrl.length() - folderName.length());

        SASTokenParameter sasTokenParameter = new SASTokenParameter(containerUrl)
            .setToken(sasToken)
            .setUseManagedIdentity(sasToken == null);

        PreRestoreOperationParameters preRestoreOperationParameters =
            new PreRestoreOperationParameters()
                .setFolderToRestore(folderName)
                .setSasTokenParameters(sasTokenParameter);

        try {
            return clientImpl.preFullRestoreOperationWithResponseAsync(vaultUrl, preRestoreOperationParameters, context)
                .doOnRequest(ignored -> LOGGER.verbose("Restoring from location - {}", folderUrl))
                .doOnSuccess(response -> LOGGER.verbose("Restored from location - {}", folderUrl))
                .doOnError(error ->
                    LOGGER.warning("Failed to restore from location - {}", folderUrl, error))
                .map(restoreOperationResponse ->
                    new SimpleResponse<>(restoreOperationResponse.getRequest(),
                        restoreOperationResponse.getStatusCode(),
                        restoreOperationResponse.getHeaders(),
                        (KeyVaultRestoreOperation) transformToLongRunningOperation(
                            restoreOperationResponse.getValue())));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    private Function<PollingContext<KeyVaultRestoreOperation>, Mono<KeyVaultRestoreOperation>> preRestoreActivationOperation(String folderUrl, String sasToken) {
        return (pollingContext) -> {
            try {
                return withContext(context -> preRestoreWithResponse(folderUrl, sasToken, context))
                    .flatMap(restoreResponse -> Mono.just(restoreResponse.getValue()));
            } catch (RuntimeException e) {
                return monoError(LOGGER, e);
            }
        };
    }

    /**
     * Restores all versions of a given key using the supplied SAS token pointing to a previously stored Azure Blob
     * storage backup folder.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Starts a {@link KeyVaultSelectiveKeyRestoreOperation selective key restore operation}, polls for its status
     * and waits for it to complete. Prints out error details in case the operation fails.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginSelectiveKeyRestore#String-String-String -->
     * <pre>
     * String folderUrl = &quot;https:&#47;&#47;myaccount.blob.core.windows.net&#47;myContainer&#47;mhsm-myaccount-2020090117323313&quot;;
     * String sasToken = &quot;&lt;sas-token&gt;&quot;;
     * String keyName = &quot;myKey&quot;;
     *
     * client.beginSelectiveKeyRestore&#40;folderUrl, sasToken, keyName&#41;
     *     .setPollInterval&#40;Duration.ofSeconds&#40;1&#41;&#41; &#47;&#47; You can set a custom polling interval.
     *     .doOnError&#40;e -&gt; System.out.printf&#40;&quot;Key restoration failed with error: %s.%n&quot;, e.getMessage&#40;&#41;&#41;&#41;
     *     .doOnNext&#40;pollResponse -&gt;
     *         System.out.printf&#40;&quot;The current status of the operation is: %s.%n&quot;, pollResponse.getStatus&#40;&#41;&#41;&#41;
     *     .filter&#40;pollResponse -&gt; pollResponse.getStatus&#40;&#41; == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;unused -&gt; System.out.printf&#40;&quot;Key restored successfully.%n&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginSelectiveKeyRestore#String-String-String -->
     *
     * @param keyName The name of the key to be restored.
     * @param folderUrl The URL for the Blob Storage resource where the backup is located, including the path to
     * the blob container where the backup resides. This would be the exact value that is returned as the result of a
     * backup operation. An example of such a URL may look like the following:
     * https://contoso.blob.core.windows.net/backup/mhsm-contoso-2020090117323313.
     * @param sasToken Optional Shared Access Signature (SAS) token to authorize access to the blob. If {@code null},
     * Managed Identity will be used to authenticate instead.
     *
     * @return A {@link PollerFlux} polling on the {@link KeyVaultRestoreOperation restore operation} status.
     *
     * @throws KeyVaultAdministrationException If the given {@code keyName}, {@code folderUrl} or {@code sasToken} are
     * invalid.
     * @throws NullPointerException If the {@code keyName} or {@code folderUrl} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<KeyVaultSelectiveKeyRestoreOperation, KeyVaultSelectiveKeyRestoreResult> beginSelectiveKeyRestore(String keyName, String folderUrl, String sasToken) {
        if (keyName == null) {
            throw LOGGER.logExceptionAsError(
                new NullPointerException(String.format(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED, "'keyName'")));
        }

        if (folderUrl == null) {
            throw LOGGER.logExceptionAsError(
                new NullPointerException(String.format(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED, "'folderUrl'")));
        }

        return new PollerFlux<>(getDefaultPollingInterval(),
            selectiveKeyRestoreActivationOperation(keyName, folderUrl, sasToken),
            selectiveKeyRestorePollOperation(),
            (pollingContext, firstResponse) -> Mono.error(LOGGER.logExceptionAsError(new RuntimeException("Cancellation is not supported"))),
            (pollingContext) -> Mono.just(new KeyVaultSelectiveKeyRestoreResult()));
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
     * @return A {@link PollerFlux} polling on the {@link KeyVaultRestoreOperation backup operation} status.
     */
    Mono<Response<KeyVaultSelectiveKeyRestoreOperation>> selectiveKeyRestoreWithResponse(String keyName,
                                                                                         String folderUrl,
                                                                                         String sasToken,
                                                                                         Context context) {
        String[] segments = folderUrl.split("/");
        String folderName = segments[segments.length - 1];
        String containerUrl = folderUrl.substring(0, folderUrl.length() - folderName.length());

        SASTokenParameter sasTokenParameter = new SASTokenParameter(containerUrl)
            .setToken(sasToken)
            .setUseManagedIdentity(sasToken == null);
        SelectiveKeyRestoreOperationParameters selectiveKeyRestoreOperationParameters =
            new SelectiveKeyRestoreOperationParameters(sasTokenParameter, folderName);

        try {
            return clientImpl.selectiveKeyRestoreOperationWithResponseAsync(vaultUrl, keyName,
                selectiveKeyRestoreOperationParameters, context)
                .doOnRequest(ignored ->
                    LOGGER.verbose("Restoring key \"{}\" from location - {}", keyName, folderUrl))
                .doOnSuccess(response ->
                    LOGGER.verbose("Restored key \"{}\" from location - {}", keyName, folderUrl))
                .doOnError(error ->
                    LOGGER.warning("Failed to restore key \"{}\" from location - {}", keyName, folderUrl, error))
                .map(restoreOperationResponse ->
                    new SimpleResponse<>(restoreOperationResponse.getRequest(),
                        restoreOperationResponse.getStatusCode(),
                        restoreOperationResponse.getHeaders(),
                        (KeyVaultSelectiveKeyRestoreOperation) transformToLongRunningOperation(
                            restoreOperationResponse.getValue())));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    private Function<PollingContext<KeyVaultSelectiveKeyRestoreOperation>, Mono<KeyVaultSelectiveKeyRestoreOperation>> selectiveKeyRestoreActivationOperation(String keyName, String folderUrl, String sasToken) {
        return (pollingContext) -> {
            try {
                return withContext(context -> selectiveKeyRestoreWithResponse(keyName, folderUrl, sasToken, context))
                    .flatMap(selectiveKeyRestoreResponse -> Mono.just(selectiveKeyRestoreResponse.getValue()));
            } catch (RuntimeException e) {
                return monoError(LOGGER, e);
            }
        };
    }

    private Function<PollingContext<KeyVaultSelectiveKeyRestoreOperation>, Mono<PollResponse<KeyVaultSelectiveKeyRestoreOperation>>> selectiveKeyRestorePollOperation() {
        return (pollingContext) -> {
            try {
                PollResponse<KeyVaultSelectiveKeyRestoreOperation> pollResponse = pollingContext.getLatestResponse();

                if (pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
                    || pollResponse.getStatus() == LongRunningOperationStatus.FAILED) {

                    return Mono.just(pollResponse);
                }

                final KeyVaultSelectiveKeyRestoreOperation keyVaultSelectiveKeyRestoreOperation = pollResponse.getValue();

                if (keyVaultSelectiveKeyRestoreOperation == null) {
                    LOGGER.warning("Restore operation does not exist. Activation operation failed.");

                    return Mono.just(new PollResponse<KeyVaultSelectiveKeyRestoreOperation>(
                        LongRunningOperationStatus.fromString("SELECTIVE_RESTORE_START_FAILED", true), null));
                }

                final String jobId = keyVaultSelectiveKeyRestoreOperation.getOperationId();

                return withContext(context -> clientImpl.restoreStatusWithResponseAsync(vaultUrl, jobId,
                    context))
                    .map(response -> new SimpleResponse<>(response,
                        (KeyVaultSelectiveKeyRestoreOperation) restoreOperationToSelectiveKeyRestoreOperation(response.getValue())))
                    .flatMap(KeyVaultBackupAsyncClient::processSelectiveKeyRestoreOperationResponse);
            } catch (HttpResponseException e) {
                //noinspection ThrowableNotThrown
                LOGGER.logExceptionAsError(e);

                return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null));
            } catch (RuntimeException e) {
                return monoError(LOGGER, e);
            }
        };
    }

    private static Mono<PollResponse<KeyVaultSelectiveKeyRestoreOperation>> processSelectiveKeyRestoreOperationResponse(
        Response<KeyVaultSelectiveKeyRestoreOperation> response) {

        String operationStatus = response.getValue().getStatus().toLowerCase(Locale.US);

        return Mono.just(new PollResponse<>(
            toLongRunningOperationStatus(operationStatus.toLowerCase(Locale.US)), response.getValue()));
    }

    static KeyVaultLongRunningOperation restoreOperationToSelectiveKeyRestoreOperation(RestoreOperation operation) {
        return new KeyVaultSelectiveKeyRestoreOperation(operation.getStatus(),
            operation.getStatusDetails(),
            createKeyVaultErrorFromError(operation.getError()),
            operation.getJobId(),
            operation.getStartTime(),
            operation.getEndTime());
    }
}

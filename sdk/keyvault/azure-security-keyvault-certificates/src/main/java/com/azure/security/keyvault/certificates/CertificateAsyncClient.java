// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import com.azure.security.keyvault.certificates.models.CertificateContact;
import com.azure.security.keyvault.certificates.models.CertificateContentType;
import com.azure.security.keyvault.certificates.models.CertificateIssuer;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.CertificatePolicyAction;
import com.azure.security.keyvault.certificates.models.CertificateProperties;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.security.keyvault.certificates.models.ImportCertificateOptions;
import com.azure.security.keyvault.certificates.models.IssuerProperties;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import com.azure.security.keyvault.certificates.models.LifetimeAction;
import com.azure.security.keyvault.certificates.models.MergeCertificateOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * The CertificateAsyncClient provides asynchronous methods to manage {@link KeyVaultCertificate certifcates} in the Azure Key Vault. The client
 * supports creating, retrieving, updating, merging, deleting, purging, backing up, restoring and listing the
 * {@link KeyVaultCertificate certificates}. The client also supports listing {@link DeletedCertificate deleted certificates} for
 * a soft-delete enabled Azure Key Vault.
 *
 * <p>The client further allows creating, retrieving, updating, deleting and listing the {@link CertificateIssuer certificate issuers}. The client also supports
 * creating, listing and deleting {@link CertificateContact certificate contacts}</p>
 *
 * <p><strong>Samples to construct the async client</strong></p>
 *
 * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.instantiation}
 *
 * @see CertificateClientBuilder
 * @see PagedFlux
 */
@ServiceClient(builder = CertificateClientBuilder.class, isAsync = true, serviceInterfaces = CertificateService.class)
public final class CertificateAsyncClient {
    private final String apiVersion;
    static final String ACCEPT_LANGUAGE = "en-US";
    static final int DEFAULT_MAX_PAGE_RESULTS = 25;
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    static final String KEY_VAULT_SCOPE = "https://vault.azure.net/.default";
    // Please see <a href=https://docs.microsoft.com/en-us/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    private static final String KEYVAULT_TRACING_NAMESPACE_VALUE = "Microsoft.KeyVault";

    private static final Duration DEFAULT_POLLING_INTERVAL = Duration.ofSeconds(1);

    private final String vaultUrl;
    private final CertificateService service;
    private final ClientLogger logger = new ClientLogger(CertificateAsyncClient.class);

    private final HttpPipeline pipeline;

    /**
     * Creates a CertificateAsyncClient that uses {@code pipeline} to service requests
     *
     * @param vaultUrl URL for the Azure KeyVault service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     * @param version {@link CertificateServiceVersion} of the service to be used when making requests.
     */
    CertificateAsyncClient(URL vaultUrl, HttpPipeline pipeline, CertificateServiceVersion version) {
        Objects.requireNonNull(vaultUrl, KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));
        this.vaultUrl = vaultUrl.toString();
        this.service = RestProxy.create(CertificateService.class, pipeline);
        this.pipeline = pipeline;
        apiVersion = version.getVersion();
    }

    /**
     * Get the vault endpoint url to which service requests are sent to.
     * @return the vault endpoint url
     */
    public String getVaultUrl() {
        return vaultUrl;
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    HttpPipeline getHttpPipeline() {
        return this.pipeline;
    }

    Duration getDefaultPollingInterval() {
        return DEFAULT_POLLING_INTERVAL;
    }

    /**
     * Creates a new certificate. If this is the first version, the certificate resource is created. This operation requires
     * the certificates/create permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Create certificate is a long running operation. The {@link PollerFlux poller} allows users to automatically poll on the create certificate
     * operation status. It is possible to monitor each intermediate poll response during the poll operation.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.beginCreateCertificate#String-CertificatePolicy-Boolean-Map}
     *
     * @param certificateName The name of the certificate to be created.
     * @param policy The policy of the certificate to be created.
     * @param isEnabled The enabled status for the certificate.
     * @param tags The application specific metadata to set.
     * @throws ResourceModifiedException when invalid certificate policy configuration is provided.
     * @return A {@link PollerFlux} polling on the create certificate operation status.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> beginCreateCertificate(String certificateName, CertificatePolicy policy, Boolean isEnabled, Map<String, String> tags) {
        return new PollerFlux<>(getDefaultPollingInterval(),
            activationOperation(certificateName, policy, isEnabled, tags),
            createPollOperation(certificateName),
            cancelOperation(certificateName),
            fetchResultOperation(certificateName));
    }

    private BiFunction<PollingContext<CertificateOperation>,
            PollResponse<CertificateOperation>,
            Mono<CertificateOperation>> cancelOperation(String certificateName) {
        return (pollingContext, firstResponse) -> withContext(context
            -> cancelCertificateOperationWithResponse(certificateName, context))
            .flatMap(FluxUtil::toMono);
    }

    private Function<PollingContext<CertificateOperation>, Mono<CertificateOperation>> activationOperation(String certificateName,
                                                                                      CertificatePolicy policy,
                                                                                      boolean enabled,
                                                                                      Map<String, String> tags) {
        return (pollingContext) -> withContext(context -> createCertificateWithResponse(certificateName,
                policy,
                enabled,
                tags,
                context))
            .flatMap(certificateOperationResponse -> Mono.just(certificateOperationResponse.getValue()));
    }

    private Function<PollingContext<CertificateOperation>,
            Mono<KeyVaultCertificateWithPolicy>> fetchResultOperation(String certificateName) {
        return (pollingContext) -> withContext(context
            -> getCertificateWithResponse(certificateName, "", context))
                        .flatMap(certificateResponse -> Mono.just(certificateResponse.getValue()));
    }

    /**
     * Creates a new certificate. If this is the first version, the certificate resource is created. This operation requires
     * the certificates/create permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Create certificate is a long running operation. The {@link PollerFlux poller} allows users to automatically poll on the create certificate
     * operation status. It is possible to monitor each intermediate poll response during the poll operation.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.beginCreateCertificate#String-CertificatePolicy}
     *
     * @param certificateName The name of the certificate to be created.
     * @param policy The policy of the certificate to be created.
     * @throws ResourceModifiedException when invalid certificate policy configuration is provided.
     * @return A {@link PollerFlux} polling on the create certificate operation status.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> beginCreateCertificate(String certificateName, CertificatePolicy policy) {
        return beginCreateCertificate(certificateName, policy, true, null);
    }

    /*
       Polling operation to poll on create certificate operation status.
     */
    private Function<PollingContext<CertificateOperation>, Mono<PollResponse<CertificateOperation>>> createPollOperation(String certificateName) {
        return (pollingContext) -> {

            try {
                return withContext(context -> service.getCertificateOperation(vaultUrl, certificateName, apiVersion,
                    ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE)))
                    .flatMap(this::processCertificateOperationResponse);
            } catch (HttpResponseException e) {
                logger.logExceptionAsError(e);
                return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null));
            }
        };
    }

    private Mono<PollResponse<CertificateOperation>> processCertificateOperationResponse(Response<CertificateOperation> certificateOperationResponse) {
        LongRunningOperationStatus status = null;
        switch (certificateOperationResponse.getValue().getStatus()) {
            case "inProgress":
                status = LongRunningOperationStatus.IN_PROGRESS;
                break;
            case "completed":
                status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                break;
            case "failed":
                status = LongRunningOperationStatus.FAILED;
                break;
            default:
                //should not reach here
                status = LongRunningOperationStatus.fromString(certificateOperationResponse.getValue().getStatus(), true);
                break;
        }
        return Mono.just(new PollResponse<>(status, certificateOperationResponse.getValue()));
    }

    Mono<Response<CertificateOperation>> createCertificateWithResponse(String certificateName, CertificatePolicy certificatePolicy, boolean enabled, Map<String, String> tags, Context context) {
        CertificateRequestParameters certificateRequestParameters = new CertificateRequestParameters()
            .certificatePolicy(new CertificatePolicyRequest(certificatePolicy))
            .certificateAttributes(new CertificateRequestAttributes().enabled(enabled))
            .tags(tags);
        return service.createCertificate(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE, certificateRequestParameters, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
    }


    /**
     * Gets a pending {@link CertificateOperation} from the key vault. This operation requires the certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Get a pending certificate operation. The {@link PollerFlux poller} allows users to automatically poll on the certificate
     * operation status. It is possible to monitor each intermediate poll response during the poll operation.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateOperation#String}
     *
     * @param certificateName The name of the certificate.
     * @throws ResourceNotFoundException when a certificate operation for a certificate with {@code certificateName} doesn't exist.
     * @return A {@link PollerFlux} polling on the certificate operation status.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> getCertificateOperation(String certificateName) {
        return new PollerFlux<>(getDefaultPollingInterval(),
            (pollingContext) -> Mono.empty(),
            createPollOperation(certificateName),
            cancelOperation(certificateName),
            fetchResultOperation(certificateName));
    }

    /**
     * Gets information about the latest version of the specified certificate. This operation requires the certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the certificate in the key vault. Prints out the
     * returned certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificate#String}
     *
     * @param certificateName The name of the certificate to retrieve, cannot be null
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException if {@code certificateName} is empty string.
     * @return A {@link Mono} containing the requested {@link KeyVaultCertificateWithPolicy certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultCertificateWithPolicy> getCertificate(String certificateName) {
        try {
            return withContext(context -> getCertificateWithResponse(certificateName, "",
                context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets information about the latest version of the specified certificate. This operation requires the certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the certificate in the key vault. Prints out the
     * returned certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateWithResponse#String}
     *
     * @param certificateName The name of the certificate to retrieve, cannot be null
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException if {@code certificateName} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the requested {@link KeyVaultCertificateWithPolicy certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultCertificateWithPolicy>> getCertificateWithResponse(String certificateName) {
        try {
            return withContext(context -> getCertificateWithResponse(certificateName, "", context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultCertificateWithPolicy>> getCertificateWithResponse(String certificateName, String version, Context context) {
        context = context == null ? Context.NONE : context;

        return service.getCertificateWithPolicy(vaultUrl, certificateName, version, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Retrieving certificate - {}",  certificateName))
            .doOnSuccess(response -> logger.verbose("Retrieved the certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> logger.warning("Failed to Retrieve the certificate - {}", certificateName, error));
    }

    Mono<Response<KeyVaultCertificate>> getCertificateVersionWithResponse(String certificateName, String version, Context context) {
        context = context == null ? Context.NONE : context;

        return service.getCertificate(vaultUrl, certificateName, version, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.verbose("Retrieving certificate - {}",  certificateName))
            .doOnSuccess(response -> logger.verbose("Retrieved the certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> logger.warning("Failed to Retrieve the certificate - {}", certificateName, error));
    }

    /**
     * Gets information about the latest version of the specified certificate. This operation requires the certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the certificate in the key vault. Prints out the
     * returned certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateVersionWithResponse#string-string}
     *
     * @param certificateName The name of the certificate to retrieve, cannot be null
     * @param version The version of the certificate to retrieve. If this is an empty String or null then latest version of the certificate is retrieved.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException if {@code certificateName} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the requested {@link KeyVaultCertificate certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultCertificate>> getCertificateVersionWithResponse(String certificateName, String version) {
        try {
            return withContext(context -> getCertificateVersionWithResponse(certificateName, version == null ? "" : version,
                context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets information about the specified version of the specified certificate. This operation requires the certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the certificate in the key vault. Prints out the
     * returned certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateVersion#String-String}
     *
     * @param certificateName The name of the certificate to retrieve, cannot be null
     * @param version The version of the certificate to retrieve. If this is an empty String or null then latest version of the certificate is retrieved.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException if {@code certificateName} is empty string.
     * @return A {@link Mono} containing the requested {@link KeyVaultCertificate certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultCertificate> getCertificateVersion(String certificateName, String version) {
        try {
            return withContext(context -> getCertificateVersionWithResponse(certificateName, version == null ? "" : version,
                context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Updates the specified attributes associated with the specified certificate. The update operation changes specified attributes of an existing
     * stored certificate and attributes that are not specified in the request are left unchanged. This operation requires the certificates/update permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets latest version of the certificate, changes its tags and enabled status and then updates it in the Azure Key Vault. Prints out the
     * returned certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificateProperties#CertificateProperties}
     *
     * @param properties The {@link CertificateProperties} object with updated properties.
     * @throws NullPointerException if {@code certificate} is {@code null}.
     * @throws ResourceNotFoundException when a certificate with {@link CertificateProperties#getName() name} and {@link CertificateProperties#getVersion() version} doesn't exist in the key vault.
     * @throws HttpResponseException if {@link CertificateProperties#getName() name} or {@link CertificateProperties#getVersion() version} is empty string.
     * @return A {@link Mono} containing the {@link CertificateProperties updated certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultCertificate> updateCertificateProperties(CertificateProperties properties) {
        try {
            return withContext(context -> updateCertificatePropertiesWithResponse(properties, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Updates the specified attributes associated with the specified certificate. The update operation changes specified attributes of an existing
     * stored certificate and attributes that are not specified in the request are left unchanged. This operation requires the certificates/update permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets latest version of the certificate, changes its enabled status and then updates it in the Azure Key Vault. Prints out the
     * returned certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificatePropertiesWithResponse#CertificateProperties}
     *
     * @param properties The {@link CertificateProperties} object with updated properties.
     * @throws NullPointerException if {@code properties} is {@code null}.
     * @throws ResourceNotFoundException when a certificate with {@link CertificateProperties#getName() name} and {@link CertificateProperties#getVersion() version} doesn't exist in the key vault.
     * @throws HttpResponseException if {@link CertificateProperties#getName() name} or {@link CertificateProperties#getVersion() version} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link CertificateProperties updated certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultCertificate>> updateCertificatePropertiesWithResponse(CertificateProperties properties) {
        try {
            return withContext(context -> updateCertificatePropertiesWithResponse(properties,
                context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultCertificate>> updateCertificatePropertiesWithResponse(CertificateProperties properties, Context context) {
        context = context == null ? Context.NONE : context;

        Objects.requireNonNull(properties, "properties' cannot be null.");
        CertificateUpdateParameters parameters = new CertificateUpdateParameters()
            .tags(properties.getTags())
            .certificateAttributes(new CertificateRequestAttributes(properties));
        return service.updateCertificate(vaultUrl, properties.getName(), properties.getVersion(), apiVersion, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Updating certificate - {}",  properties.getName()))
            .doOnSuccess(response -> logger.verbose("Updated the certificate - {}", properties.getName()))
            .doOnError(error -> logger.warning("Failed to update the certificate - {}", properties.getName(), error));
    }

    /**
     * Deletes a certificate from a specified key vault. All the versions of the certificate along with its associated policy
     * get deleted. If soft-delete is enabled on the key vault then the certificate is placed in the deleted state and requires to be
     * purged for permanent deletion else the certificate is permanently deleted. The delete operation applies to any certificate stored in
     * Azure Key Vault but it cannot be applied to an individual version of a certificate. This operation requires the certificates/delete permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the certificate in the Azure Key Vault. Prints out the deleted certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.beginDeleteCertificate#String}
     *
     * @param certificateName The name of the certificate to be deleted.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException when a certificate with {@code certificateName} is empty string.
     * @return A {@link PollerFlux} to poll on the {@link DeletedCertificate deleted certificate}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<DeletedCertificate, Void> beginDeleteCertificate(String certificateName) {
        return new PollerFlux<>(getDefaultPollingInterval(),
            activationOperation(certificateName),
            createDeletePollOperation(certificateName),
            (context, firstResponse) -> Mono.empty(),
            (context) -> Mono.empty());
    }

    private Function<PollingContext<DeletedCertificate>, Mono<DeletedCertificate>> activationOperation(String certificateName) {
        return (pollingContext) -> withContext(context -> deleteCertificateWithResponse(certificateName,
            context))
            .flatMap(deletedCertificateResponse -> Mono.just(deletedCertificateResponse.getValue()));
    }

    /*
    Polling operation to poll on create delete certificate operation status.
    */
    private Function<PollingContext<DeletedCertificate>, Mono<PollResponse<DeletedCertificate>>> createDeletePollOperation(String keyName) {
        return pollingContext ->
            withContext(context -> service.getDeletedCertificatePoller(vaultUrl, keyName, apiVersion,
                ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE)))
                .flatMap(deletedCertificateResponse -> {
                    if (deletedCertificateResponse.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                        return Mono.defer(() -> Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                            pollingContext.getLatestResponse().getValue())));
                    }
                    if (deletedCertificateResponse.getStatusCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                        return Mono.defer(() -> Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                            pollingContext.getLatestResponse().getValue())));
                    }
                    return Mono.defer(() -> Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, deletedCertificateResponse.getValue())));
                })
                // This means either vault has soft-delete disabled or permission is not granted for the get deleted certificate operation.
                // In both cases deletion operation was successful when activation operation succeeded before reaching here.
                .onErrorReturn(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollingContext.getLatestResponse().getValue()));
    }

    Mono<Response<DeletedCertificate>> deleteCertificateWithResponse(String certificateName, Context context) {
        return service.deleteCertificate(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Deleting certificate - {}",  certificateName))
            .doOnSuccess(response -> logger.verbose("Deleted the certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> logger.warning("Failed to delete the certificate - {}", certificateName, error));
    }

    /**
     * Retrieves information about the specified deleted certificate. The GetDeletedCertificate operation  is applicable for soft-delete
     * enabled vaults and additionally retrieves deleted certificate's attributes, such as retention interval, scheduled permanent deletion and the current deletion recovery level. This operation
     * requires the certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Gets the deleted certificate from the key vault enabled for soft-delete. Prints out the
     * deleted certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.getDeletedCertificate#string}
     *
     * @param certificateName The name of the deleted certificate.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException when a certificate with {@code certificateName} is empty string.
     * @return A {@link Mono} containing the {@link DeletedCertificate deleted certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DeletedCertificate> getDeletedCertificate(String certificateName) {
        try {
            return withContext(context -> getDeletedCertificateWithResponse(certificateName, context))
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves information about the specified deleted certificate. The GetDeletedCertificate operation  is applicable for soft-delete
     * enabled vaults and additionally retrieves deleted certificate's attributes, such as retention interval, scheduled permanent deletion and the current deletion recovery level. This operation
     * requires the certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Gets the deleted certificate from the key vault enabled for soft-delete. Prints out the
     * deleted certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.getDeletedCertificateWithResponse#string}
     *
     * @param certificateName The name of the deleted certificate.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException when a certificate with {@code certificateName} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link DeletedCertificate deleted certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DeletedCertificate>> getDeletedCertificateWithResponse(String certificateName) {
        try {
            return withContext(context -> getDeletedCertificateWithResponse(certificateName, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DeletedCertificate>> getDeletedCertificateWithResponse(String certificateName, Context context) {
        context = context == null ? Context.NONE : context;

        return service.getDeletedCertificate(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Retrieving deleted certificate - {}",  certificateName))
            .doOnSuccess(response -> logger.verbose("Retrieved the deleted certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> logger.warning("Failed to Retrieve the deleted certificate - {}", certificateName, error));
    }

    /**
     * Permanently deletes the specified deleted certificate without possibility for recovery. The Purge Deleted Certificate operation is applicable for
     * soft-delete enabled vaults and is not available if the recovery level does not specify 'Purgeable'. This operation requires the certificate/purge permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Purges the deleted certificate from the key vault enabled for soft-delete. Prints out the
     * status code from the server response when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.purgeDeletedCertificateWithResponse#string}
     *
     * @param certificateName The name of the deleted certificate.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException when a certificate with {@code certificateName} is empty string.
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> purgeDeletedCertificate(String certificateName) {
        try {
            return purgeDeletedCertificateWithResponse(certificateName).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Permanently deletes the specified deleted certificate without possibility for recovery. The Purge Deleted Certificate operation is applicable for
     * soft-delete enabled vaults and is not available if the recovery level does not specify 'Purgeable'. This operation requires the certificate/purge permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Purges the deleted certificate from the key vault enabled for soft-delete. Prints out the
     * status code from the server response when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.purgeDeletedCertificateWithResponse#string}
     *
     * @param certificateName The name of the deleted certificate.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException when a certificate with {@code certificateName} is empty string.
     * @return A {@link Mono} containing a Void Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> purgeDeletedCertificateWithResponse(String certificateName) {
        try {
            return withContext(context -> purgeDeletedCertificateWithResponse(certificateName, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> purgeDeletedCertificateWithResponse(String certificateName, Context context) {
        context = context == null ? Context.NONE : context;

        return service.purgeDeletedcertificate(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Purging certificate - {}",  certificateName))
            .doOnSuccess(response -> logger.verbose("Purged the certificate - {}", response.getStatusCode()))
            .doOnError(error -> logger.warning("Failed to purge the certificate - {}", certificateName, error));
    }

    /**
     * Recovers the deleted certificate back to its current version under /certificates and can only be performed on a soft-delete enabled vault.
     * The RecoverDeletedCertificate operation performs the reversal of the Delete operation and must be issued during the retention interval
     * (available in the deleted certificate's attributes). This operation requires the certificates/recover permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Recovers the deleted certificate from the key vault enabled for soft-delete. Prints out the
     * recovered certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.certificatevault.certificates.CertificateAsyncClient.beginRecoverDeletedCertificate#String}
     *
     * @param certificateName The name of the deleted certificate to be recovered.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the certificate vault.
     * @throws HttpResponseException when a certificate with {@code certificateName} is empty string.
     * @return A {@link PollerFlux} to poll on the {@link KeyVaultCertificate recovered certificate}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<KeyVaultCertificateWithPolicy, Void> beginRecoverDeletedCertificate(String certificateName) {
        return new PollerFlux<>(getDefaultPollingInterval(),
            recoverActivationOperation(certificateName),
            createRecoverPollOperation(certificateName),
            (context, firstResponse) -> Mono.empty(),
            context -> Mono.empty());
    }

    private Function<PollingContext<KeyVaultCertificateWithPolicy>, Mono<KeyVaultCertificateWithPolicy>> recoverActivationOperation(String certificateName) {
        return (pollingContext) -> withContext(context -> recoverDeletedCertificateWithResponse(certificateName,
            context))
            .flatMap(certificateResponse -> Mono.just(certificateResponse.getValue()));
    }

    /*
    Polling operation to poll on create recover certificate operation status.
    */
    private Function<PollingContext<KeyVaultCertificateWithPolicy>, Mono<PollResponse<KeyVaultCertificateWithPolicy>>> createRecoverPollOperation(String keyName) {
        return pollingContext ->
            withContext(context -> service.getCertificatePoller(vaultUrl, keyName, "", apiVersion,
                ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE)))
                .flatMap(certificateResponse -> {
                    if (certificateResponse.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                        return Mono.defer(() -> Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                            pollingContext.getLatestResponse().getValue())));
                    }
                    if (certificateResponse.getStatusCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                        return Mono.defer(() -> Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                            pollingContext.getLatestResponse().getValue())));
                    }
                    return Mono.defer(() -> Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                        certificateResponse.getValue())));
                })
                // This means permission is not granted for the get deleted key operation.
                // In both cases deletion operation was successful when activation operation succeeded before reaching here.
                .onErrorReturn(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    pollingContext.getLatestResponse().getValue()));
    }

    Mono<Response<KeyVaultCertificateWithPolicy>> recoverDeletedCertificateWithResponse(String certificateName, Context context) {
        return service.recoverDeletedCertificate(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Recovering deleted certificate - {}",  certificateName))
            .doOnSuccess(response -> logger.verbose("Recovered the deleted certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> logger.warning("Failed to recover the deleted certificate - {}", certificateName, error));
    }

    /**
     * Requests that a backup of the specified certificate be downloaded to the client. All versions of the certificate will
     * be downloaded. This operation requires the certificates/backup permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Backs up the certificate from the key vault. Prints out the
     * length of the certificate's backup byte array returned in the response.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.backupCertificate#string}
     *
     * @param certificateName The name of the certificate.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException when a certificate with {@code certificateName} is empty string.
     * @return A {@link Mono} containing the backed up certificate blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<byte[]> backupCertificate(String certificateName) {
        try {
            return withContext(context -> backupCertificateWithResponse(certificateName, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Requests that a backup of the specified certificate be downloaded to the client. All versions of the certificate will
     * be downloaded. This operation requires the certificates/backup permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Backs up the certificate from the key vault. Prints out the
     * length of the certificate's backup byte array returned in the response.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.backupCertificateWithResponse#string}
     *
     * @param certificateName The name of the certificate.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException when a certificate with {@code certificateName} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the backed up certificate blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<byte[]>> backupCertificateWithResponse(String certificateName) {
        try {
            return withContext(context -> backupCertificateWithResponse(certificateName, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<byte[]>> backupCertificateWithResponse(String certificateName, Context context) {
        context = context == null ? Context.NONE : context;

        return service.backupCertificate(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Backing up certificate - {}",  certificateName))
            .doOnSuccess(response -> logger.verbose("Backed up the certificate - {}", response.getStatusCode()))
            .doOnError(error -> logger.warning("Failed to back up the certificate - {}", certificateName, error))
            .flatMap(certificateBackupResponse -> Mono.just(new SimpleResponse<>(certificateBackupResponse.getRequest(),
                certificateBackupResponse.getStatusCode(), certificateBackupResponse.getHeaders(), certificateBackupResponse.getValue().getValue())));
    }

    /**
     * Restores a backed up certificate to the vault. All the versions of the certificate are restored to the vault. This operation
     * requires the certificates/restore permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Restores the certificate in the key vault from its backup. Prints out the restored certificate
     * details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.restoreCertificate#byte}
     *
     * @param backup The backup blob associated with the certificate.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     * @return A {@link Mono} containing the {@link KeyVaultCertificate restored certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultCertificateWithPolicy> restoreCertificateBackup(byte[] backup) {
        try {
            return withContext(context -> restoreCertificateBackupWithResponse(backup, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Restores a backed up certificate to the vault. All the versions of the certificate are restored to the vault. This operation
     * requires the certificates/restore permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Restores the certificate in the key vault from its backup. Prints out the restored certificate
     * details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.restoreCertificateWithResponse#byte}
     *
     * @param backup The backup blob associated with the certificate.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultCertificate restored certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultCertificateWithPolicy>> restoreCertificateBackupWithResponse(byte[] backup) {
        try {
            return withContext(context -> restoreCertificateBackupWithResponse(backup, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultCertificateWithPolicy>> restoreCertificateBackupWithResponse(byte[] backup, Context context) {
        context = context == null ? Context.NONE : context;

        CertificateRestoreParameters parameters = new CertificateRestoreParameters().certificateBundleBackup(backup);
        return service.restoreCertificate(vaultUrl, apiVersion, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Restoring the certificate"))
            .doOnSuccess(response -> logger.verbose("Restored the certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> logger.warning("Failed to restore the certificate - {}", error));
    }

    /**
     * List certificates in a the key vault. Retrieves the set of certificates resources in the key vault and the individual
     * certificate response in the flux is represented by {@link CertificateProperties} as only the certificate identifier, thumbprint,
     * attributes and tags are provided in the response. The policy and individual certificate versions are not listed in
     * the response. This operation requires the certificates/list permission.
     *
     * <p>It is possible to get certificates with all the properties excluding the policy from this information. Convert the {@link Flux} containing {@link CertificateProperties} to
     * {@link Flux} containing {@link KeyVaultCertificate certificate} using {@link CertificateAsyncClient#getCertificateVersion(String, String)} within {@link Flux#flatMap(Function)}.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificates}
     *
     * @param includePending indicate if pending certificates should be included in the results.
     * @return A {@link PagedFlux} containing {@link CertificateProperties certificate} for all the certificates in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CertificateProperties> listPropertiesOfCertificates(boolean includePending) {
        try {
            return new PagedFlux<>(() -> withContext(context -> listCertificatesFirstPage(includePending,
                context)),
                continuationToken -> withContext(context -> listCertificatesNextPage(continuationToken,
                    context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * List certificates in a the key vault. Retrieves the set of certificates resources in the key vault and the individual
     * certificate response in the flux is represented by {@link CertificateProperties} as only the certificate identifier, thumbprint,
     * attributes and tags are provided in the response. The policy and individual certificate versions are not listed in
     * the response. This operation requires the certificates/list permission.
     *
     * <p>It is possible to get certificates with all the properties excluding the policy from this information. Convert the {@link Flux} containing {@link CertificateProperties} to
     * {@link Flux} containing {@link KeyVaultCertificate certificate} using {@link CertificateAsyncClient#getCertificateVersion(String, String)} within {@link Flux#flatMap(Function)}.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificates}
     *
     * @return A {@link PagedFlux} containing {@link CertificateProperties certificate} for all the certificates in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CertificateProperties> listPropertiesOfCertificates() {
        try {
            return new PagedFlux<>(() -> withContext(context -> listCertificatesFirstPage(false,
                context)),
                continuationToken -> withContext(context -> listCertificatesNextPage(continuationToken,
                    context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<CertificateProperties> listPropertiesOfCertificates(boolean includePending, Context context) {
        return new PagedFlux<>(
            () -> listCertificatesFirstPage(includePending, context),
            continuationToken -> listCertificatesNextPage(continuationToken, context));
    }

    /*
     * Gets attributes of all the certificates given by the {@code nextPageLink} that was retrieved from a call to
     * {@link CertificateAsyncClient#listCertificates()}.
     *
     * @param continuationToken The {@link PagedResponse#nextLink()} from a previous, successful call to one of the listCertificates operations.
     * @return A {@link Mono} of {@link PagedResponse<KeyBase>} from the next page of results.
     */
    private Mono<PagedResponse<CertificateProperties>> listCertificatesNextPage(String continuationToken, Context context) {
        try {
            return service.getCertificates(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing next certificates page - Page {} ", continuationToken))
                .doOnSuccess(response -> logger.verbose("Listed next certificates page - Page {} ", continuationToken))
                .doOnError(error -> logger.warning("Failed to list next certificates page - Page {} ", continuationToken, error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /*
     * Calls the service and retrieve first page result. It makes one call and retrieve {@code DEFAULT_MAX_PAGE_RESULTS} values.
     */
    private Mono<PagedResponse<CertificateProperties>> listCertificatesFirstPage(boolean includePending, Context context) {
        try {
            return service
                .getCertificates(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, includePending, apiVersion, ACCEPT_LANGUAGE,
                    CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing certificates"))
                .doOnSuccess(response -> logger.verbose("Listed certificates"))
                .doOnError(error -> logger.warning("Failed to list certificates", error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }


    /**
     * Lists the {@link DeletedCertificate deleted certificates} in the key vault currently available for recovery. This
     * operation includes deletion-specific information and is applicable for vaults enabled for soft-delete. This
     * operation requires the {@code certificates/get/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the deleted certificates in the key vault. Prints out the
     * recovery id of each deleted certificate when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.listDeletedCertificates}
     *
     * @return A {@link PagedFlux} containing all of the {@link DeletedCertificate deleted certificates} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DeletedCertificate> listDeletedCertificates() {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listDeletedCertificatesFirstPage(false,
                    context)),
                continuationToken -> withContext(
                    context -> listDeletedCertificatesNextPage(continuationToken,
                        context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Lists the {@link DeletedCertificate deleted certificates} in the key vault currently available for recovery. This
     * operation includes deletion-specific information and is applicable for vaults enabled for soft-delete. This
     * operation requires the {@code certificates/get/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the deleted certificates in the key vault. Prints out the
     * recovery id of each deleted certificate when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.listDeletedCertificates}
     *
     * @param includePending indicate if pending deleted certificates should be included in the results.
     * @return A {@link PagedFlux} containing all of the {@link DeletedCertificate deleted certificates} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DeletedCertificate> listDeletedCertificates(boolean includePending) {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listDeletedCertificatesFirstPage(includePending, context)),
                continuationToken -> withContext(
                    context -> listDeletedCertificatesNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<DeletedCertificate> listDeletedCertificates(Boolean includePending, Context context) {
        return new PagedFlux<>(
            () -> listDeletedCertificatesFirstPage(includePending, context),
            continuationToken -> listDeletedCertificatesNextPage(continuationToken, context));
    }

    /*
     * Gets attributes of all the certificates given by the {@code nextPageLink}
     *
     * @param continuationToken The {@link PagedResponse#nextLink()} from a previous, successful call to one of the list operations.
     * @return A {@link Mono} of {@link PagedResponse<DeletedCertificate>} from the next page of results.
     */
    private Mono<PagedResponse<DeletedCertificate>> listDeletedCertificatesNextPage(String continuationToken, Context context) {
        try {
            return service
                .getDeletedCertificates(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(
                    ignored -> logger.verbose("Listing next deleted certificates page - Page {} ", continuationToken))
                .doOnSuccess(
                    response -> logger.verbose("Listed next deleted certificates page - Page {} ", continuationToken))
                .doOnError(error -> logger
                    .warning("Failed to list next deleted certificates page - Page {} ", continuationToken, error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /*
     * Calls the service and retrieve first page result. It makes one call and retrieve {@code DEFAULT_MAX_PAGE_RESULTS} values.
     */
    private Mono<PagedResponse<DeletedCertificate>> listDeletedCertificatesFirstPage(boolean includePending, Context context) {
        try {
            return service.getDeletedCertificates(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, includePending, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing deleted certificates"))
                .doOnSuccess(response -> logger.verbose("Listed deleted certificates"))
                .doOnError(error -> logger.warning("Failed to list deleted certificates", error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * List all versions of the specified certificate. The individual certificate response in the flux is represented by {@link CertificateProperties}
     * as only the certificate identifier, thumbprint, attributes and tags are provided in the response. The policy is not listed in
     * the response. This operation requires the certificates/list permission.
     *
     * <p>It is possible to get the certificates with properties excluding the policy for all the versions from this information. Convert the {@link PagedFlux}
     * containing {@link CertificateProperties} to {@link PagedFlux} containing {@link KeyVaultCertificate certificate} using
     * {@link CertificateAsyncClient#getCertificateVersion(String, String)} within {@link Flux#flatMap(Function)}.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificateVersions}
     *
     * @param certificateName The name of the certificate.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException when a certificate with {@code certificateName} is empty string.
     * @return A {@link PagedFlux} containing {@link CertificateProperties certificate} of all the versions of the specified certificate in the vault. Flux is empty if certificate with {@code certificateName} does not exist in key vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CertificateProperties> listPropertiesOfCertificateVersions(String certificateName) {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listCertificateVersionsFirstPage(certificateName, context)),
                continuationToken -> withContext(context -> listCertificateVersionsNextPage(continuationToken,
                    context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<CertificateProperties> listPropertiesOfCertificateVersions(String certificateName, Context context) {
        return new PagedFlux<>(
            () -> listCertificateVersionsFirstPage(certificateName, context),
            continuationToken -> listCertificateVersionsNextPage(continuationToken, context));
    }

    private Mono<PagedResponse<CertificateProperties>> listCertificateVersionsFirstPage(String certificateName, Context context) {
        try {
            return service.getCertificateVersions(vaultUrl, certificateName, DEFAULT_MAX_PAGE_RESULTS, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing certificate versions - {}", certificateName))
                .doOnSuccess(response -> logger.verbose("Listed certificate versions - {}", certificateName))
                .doOnError(error -> logger.warning("Failed to list certificate versions - {}", certificateName, error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /*
     * Gets attributes of all the certificates given by the {@code nextPageLink}
     */
    private Mono<PagedResponse<CertificateProperties>> listCertificateVersionsNextPage(String continuationToken, Context context) {
        try {
            return service.getCertificates(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing next certificate versions page - Page {} ", continuationToken))
                .doOnSuccess(response -> logger.verbose("Listed next certificate versions page - Page {} ", continuationToken))
                .doOnError(error -> logger.warning("Failed to list next certificate versions page - Page {} ", continuationToken, error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Merges a certificate or a certificate chain with a key pair currently available in the service. This operation requires
     * the {@code certificates/create} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Merges a certificate with a kay pair available in the service.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificate#config}
     *
     * @param mergeCertificateOptions the merge certificate options holding the x509 certificates.
     *
     * @throws NullPointerException when {@code mergeCertificateOptions} is null.
     * @throws HttpResponseException if {@code mergeCertificateOptions} is invalid/corrupt.
     * @return A {@link Mono} containing the merged certificate.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultCertificate> mergeCertificate(MergeCertificateOptions mergeCertificateOptions) {
        try {
            return withContext(context -> mergeCertificateWithResponse(mergeCertificateOptions, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Merges a certificate or a certificate chain with a key pair currently available in the service. This operation requires
     * the {@code certificates/create} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Merges a certificate with a kay pair available in the service.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificateWithResponse#config}
     *
     * @param mergeCertificateOptions the merge certificate options holding the x509 certificates.
     *
     * @throws NullPointerException when {@code mergeCertificateOptions} is null.
     * @throws HttpResponseException if {@code mergeCertificateOptions} is invalid/corrupt.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the merged certificate.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultCertificateWithPolicy>> mergeCertificateWithResponse(MergeCertificateOptions mergeCertificateOptions) {
        try {
            return withContext(context -> mergeCertificateWithResponse(mergeCertificateOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultCertificateWithPolicy>> mergeCertificateWithResponse(MergeCertificateOptions mergeCertificateOptions, Context context) {
        context = context == null ? Context.NONE : context;
        Objects.requireNonNull(mergeCertificateOptions, "'mergeCertificateOptions' cannot be null.");
        CertificateMergeParameters mergeParameters = new CertificateMergeParameters().x509Certificates(mergeCertificateOptions.getX509Certificates())
            .tags(mergeCertificateOptions.getTags())
            .certificateAttributes(new CertificateRequestAttributes().enabled(mergeCertificateOptions.isEnabled()));
        return service.mergeCertificate(vaultUrl, mergeCertificateOptions.getName(), apiVersion, ACCEPT_LANGUAGE, mergeParameters, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Merging certificate - {}",  mergeCertificateOptions.getName()))
            .doOnSuccess(response -> logger.verbose("Merged certificate  - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> logger.warning("Failed to merge certificate - {}", mergeCertificateOptions.getName(), error));
    }

    /**
     * Retrieves the policy of the specified certificate in the key vault. This operation requires the {@code certificates/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the policy of a certirifcate in the key vault. Prints out the
     * returned certificate policy details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificatePolicy#string}
     *
     * @param certificateName The name of the certificate whose policy is to be retrieved, cannot be null
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException if {@code certificateName} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the requested {@link CertificatePolicy certificate policy}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CertificatePolicy> getCertificatePolicy(String certificateName) {
        try {
            return withContext(context -> getCertificatePolicyWithResponse(certificateName, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves the policy of the specified certificate in the key vault. This operation requires the {@code certificates/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the policy of a certirifcate in the key vault. Prints out the
     * returned certificate policy details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificatePolicyWithResponse#string}
     *
     * @param certificateName The name of the certificate whose policy is to be retrieved, cannot be null
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException if {@code certificateName} is empty string.
     * @return A {@link Mono} containing the requested {@link CertificatePolicy certificate policy}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CertificatePolicy>> getCertificatePolicyWithResponse(String certificateName) {
        try {
            return withContext(context -> getCertificatePolicyWithResponse(certificateName, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<CertificatePolicy>> getCertificatePolicyWithResponse(String certificateName, Context context) {
        context = context == null ? Context.NONE : context;
        return service.getCertificatePolicy(vaultUrl, apiVersion, ACCEPT_LANGUAGE, certificateName, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Retrieving certificate policy - {}",  certificateName))
            .doOnSuccess(response -> logger.verbose("Retrieved certificate policy - {}", certificateName))
            .doOnError(error -> logger.warning("Failed to retrieve certificate policy - {}", certificateName, error));
    }

    /**
     * Updates the policy for a certificate. The update operation changes specified attributes of the certificate policy and attributes
     * that are not specified in the request are left unchanged. This operation requires the {@code certificates/update} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the certificate policy, changes its properties and then updates it in the Azure Key Vault. Prints out the
     * returned policy details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificatePolicy#string}
     *
     * @param certificateName The name of the certificate whose policy is to be updated.
     * @param policy The certificate policy to be updated.
     * @throws NullPointerException if {@code policy} is {@code null}.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException if {@code certificateName} is empty string or if {@code policy} is invalid.
     * @return A {@link Mono} containing the updated {@link CertificatePolicy certificate policy}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CertificatePolicy> updateCertificatePolicy(String certificateName, CertificatePolicy policy) {
        try {
            return withContext(context -> updateCertificatePolicyWithResponse(certificateName, policy, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Updates the policy for a certificate. The update operation changes specified attributes of the certificate policy and attributes
     * that are not specified in the request are left unchanged. This operation requires the {@code certificates/update} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the certificate policy, changes its properties and then updates it in the Azure Key Vault. Prints out the
     * returned policy details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificatePolicyWithResponse#string}
     *
     * @param certificateName The name of the certificate whose policy is to be updated.
     * @param policy The certificate policy is to be updated.
     * @throws NullPointerException if {@code policy} is {@code null}.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException if {@code name} is empty string or if {@code policy} is invalid.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the updated {@link CertificatePolicy certificate policy}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CertificatePolicy>> updateCertificatePolicyWithResponse(String certificateName, CertificatePolicy policy) {
        try {
            return withContext(context -> updateCertificatePolicyWithResponse(certificateName, policy, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<CertificatePolicy>> updateCertificatePolicyWithResponse(String certificateName, CertificatePolicy policy, Context context) {
        context = context == null ? Context.NONE : context;
        CertificatePolicyRequest policyRequest = new CertificatePolicyRequest(policy);
        return service.updateCertificatePolicy(vaultUrl, apiVersion, ACCEPT_LANGUAGE, certificateName, policyRequest, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Updating certificate policy - {}",  certificateName))
            .doOnSuccess(response -> logger.verbose("Updated the certificate policy - {}", response.getValue().getUpdatedOn()))
            .doOnError(error -> logger.warning("Failed to update the certificate policy - {}", certificateName, error));
    }

    /**
     * Creates the specified certificate issuer. The SetCertificateIssuer operation updates the specified certificate issuer if it
     * already exists or adds it if doesn't exist. This operation requires the certificates/setissuers permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new certificate issuer in the key vault. Prints out the created certificate
     * issuer details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.createIssuer#CertificateIssuer}
     *
     * @param issuer The configuration of the certificate issuer to be created.
     * @throws ResourceModifiedException when invalid certificate issuer {@code issuer} configuration is provided.
     * @throws HttpResponseException when a certificate issuer with {@code issuerName} is empty string.
     * @return A {@link Mono} containing the created {@link CertificateIssuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CertificateIssuer> createIssuer(CertificateIssuer issuer) {
        try {
            return withContext(context -> createIssuerWithResponse(issuer, context))
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates the specified certificate issuer. The SetCertificateIssuer operation updates the specified certificate issuer if it
     * already exists or adds it if doesn't exist. This operation requires the certificates/setissuers permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new certificate issuer in the key vault. Prints out the created certificate
     * issuer details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.createIssuerWithResponse#CertificateIssuer}
     *
     * @param issuer The configuration of the certificate issuer to be created. Use
     * {@link CertificateIssuer#CertificateIssuer(String, String)} to initialize the issuer object
     * @throws ResourceModifiedException when invalid certificate issuer {@code issuer} configuration is provided.
     * @throws HttpResponseException when a certificate issuer with {@link CertificateIssuer#getName() name} is empty string.
     * @return A {@link Mono} containing  a {@link Response} whose {@link Response#getValue() value} contains the created {@link CertificateIssuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CertificateIssuer>> createIssuerWithResponse(CertificateIssuer issuer) {
        try {
            return withContext(context -> createIssuerWithResponse(issuer, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<CertificateIssuer>> createIssuerWithResponse(CertificateIssuer issuer, Context context) {
        context = context == null ? Context.NONE : context;
        CertificateIssuerSetParameters parameters = new CertificateIssuerSetParameters()
            .provider(issuer.getProvider())
            .credentials(new IssuerCredentials().accountId(issuer.getAccountId()).password(issuer.getPassword()))
            .organizationDetails(new OrganizationDetails().adminDetails(issuer.getAdministratorContacts()))
            .credentials(new IssuerCredentials().password(issuer.getPassword()).accountId(issuer.getAccountId()));
        return service.setCertificateIssuer(vaultUrl, apiVersion, ACCEPT_LANGUAGE, issuer.getName(), parameters, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Creating certificate issuer - {}",  issuer.getName()))
            .doOnSuccess(response -> logger.verbose("Created the certificate issuer - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create the certificate issuer - {}", issuer.getName(), error));
    }

    /**
     * Retrieves the specified certificate issuer from the key vault. This operation requires the certificates/manageissuers/getissuers permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the specificed certifcate issuer in the key vault. Prints out the
     * returned certificate issuer details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.getIssuerWithResponse#string}
     *
     * @param issuerName The name of the certificate issuer to retrieve, cannot be null
     * @throws ResourceNotFoundException when a certificate issuer with {@code issuerName} doesn't exist in the key vault.
     * @throws HttpResponseException if {@code issuerName} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the requested {@link CertificateIssuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CertificateIssuer>> getIssuerWithResponse(String issuerName) {
        try {
            return withContext(context -> getIssuerWithResponse(issuerName, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves the specified certificate issuer from the key vault. This operation requires the certificates/manageissuers/getissuers permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the specified certificate issuer in the key vault. Prints out the
     * returned certificate issuer details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.getIssuer#string}
     *
     * @param issuerName The name of the certificate to retrieve, cannot be null
     * @throws ResourceNotFoundException when a certificate issuer with {@code issuerName} doesn't exist in the key vault.
     * @throws HttpResponseException if {@code issuerName} is empty string.
     * @return A {@link Mono} containing the requested {@link CertificateIssuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CertificateIssuer> getIssuer(String issuerName) {
        try {
            return withContext(context -> getIssuerWithResponse(issuerName, context))
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<CertificateIssuer>> getIssuerWithResponse(String issuerName, Context context) {
        context = context == null ? Context.NONE : context;
        return service.getCertificateIssuer(vaultUrl, apiVersion, ACCEPT_LANGUAGE, issuerName, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Retrieving certificate issuer - {}",  issuerName))
            .doOnSuccess(response -> logger.verbose("Retrieved the certificate issuer - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to retreive the certificate issuer - {}", issuerName, error));
    }

    /**
     * Deletes the specified certificate issuer. The DeleteCertificateIssuer operation permanently removes the specified certificate
     * issuer from the key vault. This operation requires the {@code certificates/manageissuers/deleteissuers permission}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the certificate issuer in the Azure Key Vault. Prints out the
     * deleted certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteIssuerWithResponse#string}
     *
     * @param issuerName The name of the certificate issuer to be deleted.
     * @throws ResourceNotFoundException when a certificate issuer with {@code issuerName} doesn't exist in the key vault.
     * @throws HttpResponseException when a certificate issuer with {@code issuerName} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link CertificateIssuer deleted issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CertificateIssuer>> deleteIssuerWithResponse(String issuerName) {
        try {
            return withContext(context -> deleteIssuerWithResponse(issuerName, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the specified certificate issuer. The DeleteCertificateIssuer operation permanently removes the specified certificate
     * issuer from the key vault. This operation requires the {@code certificates/manageissuers/deleteissuers permission}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the certificate issuer in the Azure Key Vault. Prints out the
     * deleted certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteIssuer#string}
     *
     * @param issuerName The name of the certificate issuer to be deleted.
     * @throws ResourceNotFoundException when a certificate issuer with {@code issuerName} doesn't exist in the key vault.
     * @throws HttpResponseException when a certificate issuer with {@code issuerName} is empty string.
     * @return A {@link Mono} containing the {@link CertificateIssuer deleted issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CertificateIssuer> deleteIssuer(String issuerName) {
        try {
            return withContext(context -> deleteIssuerWithResponse(issuerName, context))
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<CertificateIssuer>> deleteIssuerWithResponse(String issuerName, Context context) {
        context = context == null ? Context.NONE : context;
        return service.deleteCertificateIssuer(vaultUrl, issuerName, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Deleting certificate issuer - {}",  issuerName))
            .doOnSuccess(response -> logger.verbose("Deleted the certificate issuer - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to delete the certificate issuer - {}", issuerName, error));
    }


    /**
     * List all the certificate issuers resources in the key vault. The individual certificate issuer response in the flux is represented by {@link IssuerProperties}
     * as only the certificate issuer identifier and provider are provided in the response. This operation requires the
     * {@code certificates/manageissuers/getissuers} permission.
     *
     * <p>It is possible to get the certificate issuer with all of its properties from this information. Convert the {@link PagedFlux}
     * containing {@link IssuerProperties issuerProperties} to {@link PagedFlux} containing {@link CertificateIssuer issuer} using
     * {@link CertificateAsyncClient#getIssuer(String)}
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.listPropertiesOfIssuers}
     *
     * @return A {@link PagedFlux} containing all of the {@link IssuerProperties certificate issuers} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<IssuerProperties> listPropertiesOfIssuers() {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listPropertiesOfIssuersFirstPage(context)),
                continuationToken -> withContext(context -> listPropertiesOfIssuersNextPage(continuationToken,
                    context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<IssuerProperties> listPropertiesOfIssuers(Context context) {
        return new PagedFlux<>(
            () -> listPropertiesOfIssuersFirstPage(context),
            continuationToken -> listPropertiesOfIssuersNextPage(continuationToken, context));
    }

    private Mono<PagedResponse<IssuerProperties>> listPropertiesOfIssuersFirstPage(Context context) {
        try {
            return service.getCertificateIssuers(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing certificate issuers - {}"))
                .doOnSuccess(response -> logger.verbose("Listed certificate issuers - {}"))
                .doOnError(error -> logger.warning("Failed to list certificate issuers - {}", error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /*
     * Gets attributes of all the certificates given by the {@code nextPageLink} that was retrieved from a call to
     * {@link KeyAsyncClient#listKeyVersions()}.
     *
     * @param continuationToken The {@link PagedResponse#nextLink()} from a previous, successful call to one of the listKeys operations.
     * @return A {@link Mono} of {@link PagedResponse<KeyBase>} from the next page of results.
     */
    private Mono<PagedResponse<IssuerProperties>> listPropertiesOfIssuersNextPage(String continuationToken, Context context) {
        try {
            return service.getCertificateIssuers(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing next certificate issuers page - Page {} ", continuationToken))
                .doOnSuccess(response -> logger.verbose("Listed next certificate issuers page - Page {} ", continuationToken))
                .doOnError(error -> logger.warning("Failed to list next certificate issuers page - Page {} ", continuationToken, error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Updates the specified certificate issuer. The UpdateCertificateIssuer operation updates the specified attributes of
     * the certificate issuer entity. This operation requires the certificates/setissuers permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the certificate issuer, changes its attributes/properties then updates it in the Azure Key Vault. Prints out the
     * returned certificate issuer details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.updateIssuer#CertificateIssuer}
     *
     * @param issuer The {@link CertificateIssuer issuer} with updated properties. Use
     * {@link CertificateIssuer#CertificateIssuer(String)} to initialize the issuer object
     * @throws NullPointerException if {@code issuer} is {@code null}.
     * @throws ResourceNotFoundException when a certificate issuer with {@link CertificateIssuer#getName() name} doesn't exist in the key vault.
     * @throws HttpResponseException if {@link CertificateIssuer#getName() name} is empty string.
     * @return A {@link Mono} containing the {@link CertificateIssuer updated issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CertificateIssuer> updateIssuer(CertificateIssuer issuer) {
        try {
            return withContext(context -> updateIssuerWithResponse(issuer, context))
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Updates the specified certificate issuer. The UpdateCertificateIssuer operation updates the specified attributes of
     * the certificate issuer entity. This operation requires the certificates/setissuers permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the certificate issuer, changes its attributes/properties then updates it in the Azure Key Vault. Prints out the
     * returned certificate issuer details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.updateIssuer#CertificateIssuer}
     *
     * @param issuer The {@link CertificateIssuer issuer} with updated properties.
     * @throws NullPointerException if {@code issuer} is {@code null}.
     * @throws ResourceNotFoundException when a certificate issuer with {@link CertificateIssuer#getName() name} doesn't exist in the key vault.
     * @throws HttpResponseException if {@link CertificateIssuer#getName() name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link CertificateIssuer updated issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CertificateIssuer>> updateIssuerWithResponse(CertificateIssuer issuer) {
        try {
            return withContext(context -> updateIssuerWithResponse(issuer, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<CertificateIssuer>> updateIssuerWithResponse(CertificateIssuer issuer, Context context) {
        context = context == null ? Context.NONE : context;
        CertificateIssuerUpdateParameters updateParameters = new CertificateIssuerUpdateParameters()
            .provider(issuer.getProvider())
            .organizationDetails(new OrganizationDetails().adminDetails(issuer.getAdministratorContacts()))
            .credentials(new IssuerCredentials().password(issuer.getPassword()).accountId(issuer.getAccountId()));
        return service.updateCertificateIssuer(vaultUrl, issuer.getName(), apiVersion, ACCEPT_LANGUAGE, updateParameters, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Updating certificate issuer - {}",  issuer.getName()))
            .doOnSuccess(response -> logger.verbose("Updated up the certificate issuer - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to updated the certificate issuer - {}", issuer.getName(), error));
    }

    /**
     * Sets the certificate contacts on the key vault. This operation requires the {@code certificates/managecontacts} permission.
     *
     *<p>The {@link LifetimeAction} of type {@link CertificatePolicyAction#EMAIL_CONTACTS} set on a {@link CertificatePolicy} emails the contacts set on the vault when triggered.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Sets the certificate contacts in the Azure Key Vault. Prints out the
     * returned contacts details.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.setContacts#contacts}
     *
     * @param contacts The list of contacts to set on the vault.
     * @throws HttpResponseException when a contact information provided is invalid/incomplete.
     * @return A {@link PagedFlux} containing all of the {@link CertificateContact certificate contacts} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CertificateContact> setContacts(List<CertificateContact> contacts) {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> setCertificateContactsWithResponse(contacts, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<CertificateContact> setContacts(List<CertificateContact> contacts, Context context) {
        return new PagedFlux<>(
            () -> setCertificateContactsWithResponse(contacts, context));
    }

    private Mono<PagedResponse<CertificateContact>> setCertificateContactsWithResponse(List<CertificateContact> contacts, Context context) {
        Contacts contactsParams = new Contacts().contactList(contacts);
        return service.setCertificateContacts(vaultUrl, apiVersion, ACCEPT_LANGUAGE, contactsParams, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Listing certificate contacts - {}"))
            .doOnSuccess(response -> logger.verbose("Listed certificate contacts - {}"))
            .doOnError(error -> logger.warning("Failed to list certificate contacts - {}", error));
    }

    /**
     * Lists the certificate contacts in the key vault. This operation requires the certificates/managecontacts permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the certificate contacts in the Azure Key Vault. Prints out the
     * returned contacts details.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.listContacts}
     *
     * @return A {@link PagedFlux} containing all of the {@link CertificateContact certificate contacts} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CertificateContact> listContacts() {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listCertificateContactsFirstPage(context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<CertificateContact> listContacts(Context context) {
        return new PagedFlux<>(
            () -> listCertificateContactsFirstPage(context));
    }

    private Mono<PagedResponse<CertificateContact>> listCertificateContactsFirstPage(Context context) {
        try {
            return service.getCertificateContacts(vaultUrl, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing certificate contacts - {}"))
                .doOnSuccess(response -> logger.verbose("Listed certificate contacts - {}"))
                .doOnError(error -> logger.warning("Failed to list certificate contacts - {}", error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the certificate contacts in the key vault. This operation requires the {@code certificates/managecontacts} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the certificate contacts in the Azure Key Vault. Prints out the
     * deleted contacts details.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteContacts}
     *
     * @return A {@link PagedFlux} containing all of the {@link CertificateContact deleted certificate contacts} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CertificateContact> deleteContacts() {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> deleteCertificateContactsWithResponse(context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<CertificateContact> deleteContacts(Context context) {
        return new PagedFlux<>(
            () -> deleteCertificateContactsWithResponse(context));
    }

    private Mono<PagedResponse<CertificateContact>> deleteCertificateContactsWithResponse(Context context) {
        return service.deleteCertificateContacts(vaultUrl, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Deleting certificate contacts - {}"))
            .doOnSuccess(response -> logger.verbose("Deleted certificate contacts - {}"))
            .doOnError(error -> logger.warning("Failed to delete certificate contacts - {}", error));
    }

    /**
     * Deletes the creation operation for the specified certificate that is in the process of being created. The certificate is
     * no longer created. This operation requires the {@code certificates/update permission}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Triggers certificate creation and then deletes the certificate creation operation in the Azure Key Vault. Prints out the
     * deleted certificate operation details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateOperation#string}
     *
     * @param certificateName The name of the certificate which is in the process of being created.
     * @throws ResourceNotFoundException when a certificate operation for a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException when the {@code certificateName} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link CertificateOperation deleted certificate operation}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CertificateOperation> deleteCertificateOperation(String certificateName) {
        try {
            return withContext(context -> deleteCertificateOperationWithResponse(certificateName, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the creation operation for the specified certificate that is in the process of being created. The certificate is
     * no longer created. This operation requires the {@code certificates/update permission}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Triggers certificate creation and then deletes the certificate creation operation in the Azure Key Vault. Prints out the
     * deleted certificate operation details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateOperationWithResponse#string}
     *
     * @param certificateName The name of the certificate which is in the process of being created.
     * @throws ResourceNotFoundException when a certificate operation for a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException when the {@code certificateName} is empty string.
     * @return A {@link Mono} containing the {@link CertificateOperation deleted certificate operation}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CertificateOperation>> deleteCertificateOperationWithResponse(String certificateName) {
        try {
            return withContext(context -> deleteCertificateOperationWithResponse(certificateName, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<CertificateOperation>> deleteCertificateOperationWithResponse(String certificateName, Context context) {
        return service.deletetCertificateOperation(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Deleting certificate operation - {}",  certificateName))
            .doOnSuccess(response -> logger.verbose("Deleted the certificate operation - {}", response.getStatusCode()))
            .doOnError(error -> logger.warning("Failed to delete the certificate operation - {}", certificateName, error));
    }

    Mono<Response<CertificateOperation>> cancelCertificateOperationWithResponse(String certificateName, Context context) {
        context = context == null ? Context.NONE : context;
        CertificateOperationUpdateParameter parameter = new CertificateOperationUpdateParameter().cancellationRequested(true);

        return service.updateCertificateOperation(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE, parameter, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Cancelling certificate operation - {}",  certificateName))
            .doOnSuccess(response -> logger.verbose("Cancelled the certificate operation - {}", response.getValue().getStatus()))
            .doOnError(error -> logger.warning("Failed to cancel the certificate operation - {}", certificateName, error));
    }

    /**
     * Cancels a certificate creation operation that is already in progress. This operation requires the {@code certificates/update} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Triggers certificate creation and then cancels the certificate creation operation in the Azure Key Vault. Prints out the
     * updated certificate operation details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.cancelCertificateOperation#string}
     *
     * @param certificateName The name of the certificate which is in the process of being created.
     * @throws ResourceNotFoundException when a certificate operation for a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when the {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link CertificateOperation cancelled certificate operation}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CertificateOperation> cancelCertificateOperation(String certificateName) {
        try {
            return withContext(context -> cancelCertificateOperationWithResponse(certificateName, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Cancels a certificate creation operation that is already in progress. This operation requires the {@code certificates/update} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Triggers certificate creation and then cancels the certificate creation operation in the Azure Key Vault. Prints out the
     * updated certificate operation details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.cancelCertificateOperationWithResponse#string}
     *
     * @param certificateName The name of the certificate which is in the process of being created.
     * @throws ResourceNotFoundException when a certificate operation for a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when the {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link CertificateOperation cancelled certificate operation}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CertificateOperation>> cancelCertificateOperationWithResponse(String certificateName) {
        try {
            return withContext(context -> cancelCertificateOperationWithResponse(certificateName, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Imports an existing valid certificate, containing a private key, into Azure Key Vault. This operation requires
     * the {@code certificates/import} permission. The certificate to be imported can be in either PFX or PEM format. If
     * the certificate is in PEM format the PEM file must contain the key as well as x509 certificates. Key Vault
     * will only accept a key in PKCS#8 format.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Imports a certificate into the key vault.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.importCertificate#options}
     *
     * @param importCertificateOptions The details of the certificate to import to the key vault
     * @throws HttpResponseException when the {@code importCertificateOptions} are invalid.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultCertificateWithPolicy imported certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultCertificateWithPolicy> importCertificate(ImportCertificateOptions importCertificateOptions) {
        try {
            return withContext(context -> importCertificateWithResponse(importCertificateOptions, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Imports a pre-existing certificate to the key vault. The specified certificate must be in PFX or PEM format,
     * and must contain the private key as well as the x509 certificates. This operation requires the {@code certificates/import} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Imports a certificate into the key vault.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.importCertificateWithResponse#options}
     *
     * @param importCertificateOptions The details of the certificate to import to the key vault
     * @throws HttpResponseException when the {@code importCertificateOptions} are invalid.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultCertificateWithPolicy imported certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultCertificateWithPolicy>> importCertificateWithResponse(ImportCertificateOptions importCertificateOptions) {
        try {
            return withContext(context -> importCertificateWithResponse(importCertificateOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultCertificateWithPolicy>> importCertificateWithResponse(ImportCertificateOptions importCertificateOptions, Context context) {
        context = context == null ? Context.NONE : context;

        CertificateImportParameters parameters = new CertificateImportParameters()
            .base64EncodedCertificate(transformCertificateForImport(importCertificateOptions))
            .certificateAttributes(new CertificateRequestAttributes(importCertificateOptions))
            .password(importCertificateOptions.getPassword())
            .tags(importCertificateOptions.getTags());

        if (importCertificateOptions.getPolicy() != null) {
            parameters.certificatePolicy(new CertificatePolicyRequest(importCertificateOptions.getPolicy()));
        }

        return service.importCertificate(vaultUrl, importCertificateOptions.getName(), apiVersion, ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
    }

    private String transformCertificateForImport(ImportCertificateOptions options) {
        CertificatePolicy policy = options.getPolicy();

        if (policy != null) {
            CertificateContentType contentType = policy.getContentType();
            if (contentType != null && contentType.equals(CertificateContentType.PEM)) {
                return new String(options.getCertificate(), StandardCharsets.US_ASCII);
            }
        }
        return Base64.getEncoder().encodeToString(options.getCertificate());
    }
}

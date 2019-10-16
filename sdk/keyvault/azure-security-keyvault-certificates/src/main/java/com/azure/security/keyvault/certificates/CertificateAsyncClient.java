// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import static com.azure.core.implementation.util.FluxUtil.monoError;
import static com.azure.core.implementation.util.FluxUtil.withContext;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpRequestException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.RestProxy;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.implementation.Base64Url;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.Poller;
import com.azure.security.keyvault.certificates.models.Certificate;
import com.azure.security.keyvault.certificates.models.CertificateProperties;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.Contact;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.security.keyvault.certificates.models.Issuer;
import com.azure.security.keyvault.certificates.models.IssuerProperties;
import com.azure.security.keyvault.certificates.models.LifetimeAction;
import com.azure.security.keyvault.certificates.models.LifetimeActionType;
import com.azure.security.keyvault.certificates.models.MergeCertificateOptions;
import com.azure.security.keyvault.certificates.models.CertificateImportOptions;
import com.azure.security.keyvault.certificates.models.CertificateKeyUsage;
import com.azure.security.keyvault.certificates.models.CertificateContentType;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import com.azure.security.keyvault.certificates.models.webkey.CertificateKeyType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The CertificateAsyncClient provides asynchronous methods to manage {@link Certificate certifcates} in the Azure Key Vault. The client
 * supports creating, retrieving, updating, merging, deleting, purging, backing up, restoring and listing the
 * {@link Certificate certificates}. The client also supports listing {@link DeletedCertificate deleted certificates} for
 * a soft-delete enabled Azure Key Vault.
 *
 * <p>The client further allows creating, retrieving, updating, deleting and listing the {@link Issuer certificate issuers}. The client also supports
 * creating, listing and deleting {@link Contact certificate contacts}</p>
 *
 * <p><strong>Samples to construct the async client</strong></p>
 *
 * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.instantiation}
 *
 * @see CertificateClientBuilder
 * @see PagedFlux
 */
@ServiceClient(builder = CertificateClientBuilder.class, isAsync = true, serviceInterfaces = CertificateService.class)
public class CertificateAsyncClient {
    static final String API_VERSION = "7.0";
    static final String ACCEPT_LANGUAGE = "en-US";
    static final int DEFAULT_MAX_PAGE_RESULTS = 25;
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    private final String endpoint;
    private final CertificateService service;
    private final ClientLogger logger = new ClientLogger(CertificateAsyncClient.class);

    /**
     * Creates a CertificateAsyncClient that uses {@code pipeline} to service requests
     *
     * @param endpoint URL for the Azure KeyVault service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     */
    CertificateAsyncClient(URL endpoint, HttpPipeline pipeline) {
        Objects.requireNonNull(endpoint, KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));
        this.endpoint = endpoint.toString();
        this.service = RestProxy.create(CertificateService.class, pipeline);
    }

    /**
     * Creates a new certificate. If this is the first version, the certificate resource is created. This operation requires
     * the certificates/create permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Create certificate is a long running operation. The {@link Poller poller} allows users to automatically poll on the create certificate
     * operation status. It is possible to monitor each intermediate poll response during the poll operation.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.createCertificate#String-CertificatePolicy-Boolean-Map}
     *
     * @param name The name of the certificate to be created.
     * @param policy The policy of the certificate to be created.
     * @param enabled The enabled status for the certificate.
     * @param tags The application specific metadata to set.
     * @throws ResourceModifiedException when invalid certificate policy configuration is provided.
     * @return A {@link Poller} polling on the create certificate operation status.
     */
    public Poller<CertificateOperation, Certificate> beginCreateCertificate(String name, CertificatePolicy policy, boolean enabled, Map<String, String> tags) {
        return new Poller<>(Duration.ofSeconds(1), createPollOperation(name), fetchResultOperation(name), activationOperation(name, policy, enabled, tags), cancelOperation(name));
    }

    private Function<Poller<CertificateOperation, Certificate>, Mono<CertificateOperation>> cancelOperation(String name) {
        return poller -> withContext(context -> cancelCertificateOperationWithResponse(name, context)).flatMap(FluxUtil::toMono);
    }

    private Supplier<Mono<CertificateOperation>> activationOperation(String name, CertificatePolicy policy, boolean enabled, Map<String, String> tags) {
        return () -> withContext(context -> createCertificateWithResponse(name, policy, enabled, tags, context)
            .flatMap(certificateOperationResponse -> Mono.just(certificateOperationResponse.getValue())));
    }

    private Supplier<Mono<Certificate>> fetchResultOperation(String name) {
        return () -> withContext(context -> getCertificateWithResponse(name, "", context)
            .flatMap(certificateResponse -> Mono.just(certificateResponse.getValue())));
    }

    /**
     * Creates a new certificate. If this is the first version, the certificate resource is created. This operation requires
     * the certificates/create permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Create certificate is a long running operation. The {@link Poller poller} allows users to automatically poll on the create certificate
     * operation status. It is possible to monitor each intermediate poll response during the poll operation.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.createCertificate#String-CertificatePolicy}
     *
     * @param name The name of the certificate to be created.
     * @param policy The policy of the certificate to be created.
     * @throws ResourceModifiedException when invalid certificate policy configuration is provided.
     * @return A {@link Poller} polling on the create certificate operation status.
     */
    public Poller<CertificateOperation, Certificate> beginCreateCertificate(String name, CertificatePolicy policy) {
        return beginCreateCertificate(name, policy, true, null);
    }

    /**
     * Creates a new certificate with the default policy. If this is the first version, the certificate resource is created. This operation requires
     * the certificates/create permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Create certificate is a long running operation. The {@link Poller poller} allows users to automatically poll on the create certificate
     * operation status. It is possible to monitor each intermediate poll response during the poll operation.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.createCertificate#String-CertificatePolicy}
     *
     * @param name The name of the certificate to be created.
     * @throws ResourceModifiedException when certificate configuration is invalid.
     * @return A {@link Poller} polling on the create certificate operation status.
     */
    public Poller<CertificateOperation, Certificate> beginCreateCertificate(String name) {
        return beginCreateCertificate(name, getDefaultPolicy(), true, null);
    }

    private CertificatePolicy getDefaultPolicy() {
        return new CertificatePolicy("Self", "CN=default")
            .setKeyType(CertificateKeyType.RSA)
            .setExportable(true)
            .setReuseKey(false)
            .setCertificateTransparency(false)
            .setContentType(CertificateContentType.PKCS12)
            .setKeyUsage(CertificateKeyUsage.CRL_SIGN, CertificateKeyUsage.DATA_ENCIPHERMENT,
                CertificateKeyUsage.DIGITAL_SIGNATURE, CertificateKeyUsage.KEY_ENCIPHERMENT,
                CertificateKeyUsage.KEY_AGREEMENT, CertificateKeyUsage.KEY_CERT_SIGN);
    }

    /*
       Polling operation to poll on create certificate operation status.
     */
    private Function<PollResponse<CertificateOperation>, Mono<PollResponse<CertificateOperation>>> createPollOperation(String certificateName) {
        return prePollResponse -> {
            try {
                return withContext(context -> service.getCertificateOperation(endpoint, certificateName, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
                    .flatMap(this::processCertificateOperationResponse));
            } catch (HttpRequestException e) {
                logger.logExceptionAsError(e);
                return Mono.just(new PollResponse<>(PollResponse.OperationStatus.FAILED, null));
            }
        };
    }

    private Mono<PollResponse<CertificateOperation>> processCertificateOperationResponse(Response<CertificateOperation> certificateOperationResponse) {
        PollResponse.OperationStatus status = null;
        switch (certificateOperationResponse.getValue().getStatus()) {
            case "inProgress":
                status = PollResponse.OperationStatus.IN_PROGRESS;
                break;
            case "completed":
                status = PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED;
                break;
            case "failed":
                status = PollResponse.OperationStatus.FAILED;
                break;
            default:
                //should not reach here
                break;
        }
        return Mono.just(new PollResponse<>(status, certificateOperationResponse.getValue()));
    }

    Mono<Response<CertificateOperation>> createCertificateWithResponse(String name, CertificatePolicy certificatePolicy, boolean enabled, Map<String, String> tags, Context context) {
        CertificateRequestParameters certificateRequestParameters = new CertificateRequestParameters()
            .certificatePolicy(new CertificatePolicyRequest(certificatePolicy))
            .certificateAttributes(new CertificateRequestAttributes().enabled(enabled))
            .tags(tags);
        return service.createCertificate(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, certificateRequestParameters, CONTENT_TYPE_HEADER_VALUE, context);
    }

    /**
     * Gets information about the latest version of the specified certificate. This operation requires the certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the key in the key vault. Prints out the
     * returned certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateWithPolicy#String}
     *
     * @param name The name of the certificate to retrieve, cannot be null
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Mono} containing the requested {@link Certificate certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Certificate> getCertificateWithPolicy(String name) {
        try {
            return withContext(context -> getCertificateWithResponse(name, "", context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Certificate>> getCertificateWithResponse(String name, String version, Context context) {
        return service.getCertificate(endpoint, name, version, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Retrieving certificate - {}",  name))
            .doOnSuccess(response -> logger.info("Retrieved the certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> logger.warning("Failed to Retrieve the certificate - {}", name, error));
    }

    /**
     * Gets information about the latest version of the specified certificate. This operation requires the certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the key in the key vault. Prints out the
     * returned certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateWithResponse#string-string}
     *
     * @param name The name of the certificate to retrieve, cannot be null
     * @param version The version of the certificate to retrieve. If this is an empty String or null, this call is equivalent to calling {@link CertificateAsyncClient#getCertificateWithPolicy(String)}, with the latest version being retrieved.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the requested {@link Certificate certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Certificate>> getCertificateWithResponse(String name, String version) {
        try {
            return withContext(context -> getCertificateWithResponse(name, version == null ? "" : version, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets information about the specified version of the specified certificate. This operation requires the certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the key in the key vault. Prints out the
     * returned certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificate#String-String}
     *
     * @param name The name of the certificate to retrieve, cannot be null
     * @param version The version of the certificate to retrieve. If this is an empty String or null, this call is equivalent to calling {@link CertificateAsyncClient#getCertificateWithPolicy(String)}, with the latest version being retrieved.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Mono} containing the requested {@link Certificate certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Certificate> getCertificate(String name, String version) {
        try {
            return withContext(context -> getCertificateWithResponse(name, version == null ? "" : version, context)).flatMap(FluxUtil::toMono);
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
     * @param certificateProperties The {@link CertificateProperties} object with updated properties.
     * @throws NullPointerException if {@code certificate} is {@code null}.
     * @throws ResourceNotFoundException when a certificate with {@link CertificateProperties#getName() name} and {@link CertificateProperties#getVersion() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link CertificateProperties#getName() name} or {@link CertificateProperties#getVersion() version} is empty string.
     * @return A {@link Mono} containing the {@link CertificateProperties updated certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Certificate> updateCertificateProperties(CertificateProperties certificateProperties) {
        try {
            return withContext(context -> updateCertificatePropertiesWithResponse(certificateProperties, context)).flatMap(FluxUtil::toMono);
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
     * @param certificateProperties The {@link CertificateProperties} object with updated properties.
     * @throws NullPointerException if {@code certificate} is {@code null}.
     * @throws ResourceNotFoundException when a certificate with {@link CertificateProperties#getName() name} and {@link CertificateProperties#getVersion() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link CertificateProperties#getName() name} or {@link CertificateProperties#getVersion() version} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link CertificateProperties updated certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Certificate>> updateCertificatePropertiesWithResponse(CertificateProperties certificateProperties) {
        try {
            return withContext(context -> updateCertificatePropertiesWithResponse(certificateProperties, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Certificate>> updateCertificatePropertiesWithResponse(CertificateProperties certificateProperties, Context context) {
        Objects.requireNonNull(certificateProperties, "certificateProperties' cannot be null.");
        CertificateUpdateParameters parameters = new CertificateUpdateParameters()
            .tags(certificateProperties.getTags())
            .certificateAttributes(new CertificateRequestAttributes(certificateProperties));
        return service.updateCertificate(endpoint, certificateProperties.getName(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Updating certificate - {}",  certificateProperties.getName()))
            .doOnSuccess(response -> logger.info("Updated the certificate - {}", certificateProperties.getName()))
            .doOnError(error -> logger.warning("Failed to update the certificate - {}", certificateProperties.getName(), error));
    }

    /**
     * Gets information about the certificate which represents the {@link CertificateProperties} from the key vault. This
     * operation requires the certificates/get permission.
     *
     * <p>The list operations {@link CertificateAsyncClient#listCertificates()} and {@link CertificateAsyncClient#listCertificateVersions(String)} return
     * the {@link Flux} containing {@link CertificateProperties} as output excluding the properties like secretId and keyId of the certificate.
     * This operation can then be used to get the full certificate with its properties excluding the policy from {@code certificateProperties}.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificate#CertificateProperties}
     *
     * @param certificateProperties The {@link CertificateProperties} holding attributes of the certificate being requested.
     * @throws ResourceNotFoundException when a certificate with {@link CertificateProperties#getName() name} and {@link CertificateProperties#getVersion() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link CertificateProperties#getName()}  name} or {@link CertificateProperties#getVersion() version} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the requested {@link Certificate certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Certificate> getCertificate(CertificateProperties certificateProperties) {
        try {
            return withContext(context -> getCertificateWithResponse(certificateProperties.getName(), certificateProperties.getVersion(), context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificate#string}
     *
     * @param name The name of the certificate to be deleted.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     * @return A {@link Mono} containing the {@link DeletedCertificate deleted certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DeletedCertificate> deleteCertificate(String name) {
        try {
            return withContext(context -> deleteCertificateWithResponse(name, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateWithResponse#string}
     *
     * @param name The name of the certificate to be deleted.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link DeletedCertificate deleted certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DeletedCertificate>> deleteCertificateWithResponse(String name) {
        try {
            return withContext(context -> deleteCertificateWithResponse(name, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DeletedCertificate>> deleteCertificateWithResponse(String name, Context context) {
        return service.deleteCertificate(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Deleting certificate - {}",  name))
            .doOnSuccess(response -> logger.info("Deleted the certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> logger.warning("Failed to delete the certificate - {}", name, error));
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
     * @param name The name of the deleted certificate.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     * @return A {@link Mono} containing the {@link DeletedCertificate deleted certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DeletedCertificate> getDeletedCertificate(String name) {
        try {
            return withContext(context -> getDeletedCertificateWithResponse(name, context)).flatMap(FluxUtil::toMono);
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
     * @param name The name of the deleted certificate.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link DeletedCertificate deleted certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DeletedCertificate>> getDeletedCertificateWithResponse(String name) {
        try {
            return withContext(context -> getDeletedCertificateWithResponse(name, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DeletedCertificate>> getDeletedCertificateWithResponse(String name, Context context) {
        return service.getDeletedCertificate(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Retrieving deleted certificate - {}",  name))
            .doOnSuccess(response -> logger.info("Retrieved the deleted certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> logger.warning("Failed to Retrieve the deleted certificate - {}", name, error));
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
     * @param name The name of the deleted certificate.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> purgeDeletedCertificate(String name) {
        try {
            return purgeDeletedCertificateWithResponse(name).flatMap(FluxUtil::toMono);
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
     * @param name The name of the deleted certificate.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     * @return A {@link Mono} containing a Void Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> purgeDeletedCertificateWithResponse(String name) {
        try {
            return withContext(context -> purgeDeletedCertificateWithResponse(name, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> purgeDeletedCertificateWithResponse(String name, Context context) {
        return service.purgeDeletedcertificate(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Purging certificate - {}",  name))
            .doOnSuccess(response -> logger.info("Purged the certificate - {}", response.getStatusCode()))
            .doOnError(error -> logger.warning("Failed to purge the certificate - {}", name, error));
    }

    /**
     * Recovers the deleted certificate back to its current version under /certificates and can only be performed on a soft-delete enabled vault.
     * The RecoverDeletedCertificate operation performs the reversal of the Delete operation and must be issued during the retention interval
     * (available in the deleted certificate's attributes). This operation requires the certificates/recover permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Recovers the deleted certificate from the key vault enabled for soft-delete. Prints out the
     * recovered certificate details when a response has been received.</p>

     * {@codesnippet com.azure.security.certificatevault.certificates.CertificateAsyncClient.recoverDeletedCertificate#string}
     *
     * @param name The name of the deleted certificate to be recovered.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the certificate vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     * @return A {@link Mono} containing the {@link Certificate recovered certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Certificate> recoverDeletedCertificate(String name) {
        try {
            return withContext(context -> recoverDeletedCertificateWithResponse(name, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Recovers the deleted certificate back to its current version under /certificates and can only be performed on a soft-delete enabled vault.
     * The RecoverDeletedCertificate operation performs the reversal of the Delete operation and must be issued during the retention interval
     * (available in the deleted certificate's attributes). This operation requires the certificates/recover permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Recovers the deleted certificate from the key vault enabled for soft-delete. Prints out the
     * recovered certificate details when a response has been received.</p>

     * {@codesnippet com.azure.security.certificatevault.certificates.CertificateAsyncClient.recoverDeletedCertificateWithResponse#string}
     *
     * @param name The name of the deleted certificate to be recovered.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the certificate vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link Certificate recovered certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Certificate>> recoverDeletedCertificateWithResponse(String name) {
        try {
            return withContext(context -> recoverDeletedCertificateWithResponse(name, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Certificate>> recoverDeletedCertificateWithResponse(String name, Context context) {
        return service.recoverDeletedCertificate(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Recovering deleted certificate - {}",  name))
            .doOnSuccess(response -> logger.info("Recovered the deleted certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> logger.warning("Failed to recover the deleted certificate - {}", name, error));
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
     * @param name The name of the certificate.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     * @return A {@link Mono} containing the backed up certificate blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<byte[]> backupCertificate(String name) {
        try {
            return withContext(context -> backupCertificateWithResponse(name, context)).flatMap(FluxUtil::toMono);
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
     * @param name The name of the certificate.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the backed up certificate blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<byte[]>> backupCertificateWithResponse(String name) {
        try {
            return withContext(context -> backupCertificateWithResponse(name, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<byte[]>> backupCertificateWithResponse(String name, Context context) {
        return service.backupCertificate(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Backing up certificate - {}",  name))
            .doOnSuccess(response -> logger.info("Backed up the certificate - {}", response.getStatusCode()))
            .doOnError(error -> logger.warning("Failed to back up the certificate - {}", name, error))
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
     * @return A {@link Mono} containing the {@link Certificate restored certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Certificate> restoreCertificate(byte[] backup) {
        try {
            return withContext(context -> restoreCertificateWithResponse(backup, context)).flatMap(FluxUtil::toMono);
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
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link Certificate restored certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Certificate>> restoreCertificateWithResponse(byte[] backup) {
        try {
            return withContext(context -> restoreCertificateWithResponse(backup, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Certificate>> restoreCertificateWithResponse(byte[] backup, Context context) {
        CertificateRestoreParameters parameters = new CertificateRestoreParameters().certificateBundleBackup(backup);
        return service.restoreCertificate(endpoint, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Restoring the certificate"))
            .doOnSuccess(response -> logger.info("Restored the certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> logger.warning("Failed to restore the certificate - {}", error));
    }

    /**
     * List certificates in a the key vault. Retrieves the set of certificates resources in the key vault and the individual
     * certificate response in the flux is represented by {@link CertificateProperties} as only the certificate identifier, thumbprint,
     * attributes and tags are provided in the response. The policy and individual certificate versions are not listed in
     * the response. This operation requires the certificates/list permission.
     *
     * <p>It is possible to get certificates with all the properties excluding the policy from this information. Convert the {@link Flux} containing {@link CertificateProperties} to
     * {@link Flux} containing {@link Certificate certificate} using {@link CertificateAsyncClient#getCertificate(CertificateProperties certificateProperties)} within {@link Flux#flatMap(Function)}.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificates}
     *
     * @param includePending indicate if pending certificates should be included in the results.
     * @return A {@link PagedFlux} containing {@link CertificateProperties certificate} for all the certificates in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CertificateProperties> listCertificates(Boolean includePending) {
        try {
            return new PagedFlux<>(() -> withContext(context -> listCertificatesFirstPage(includePending, context)),
                continuationToken -> withContext(context -> listCertificatesNextPage(continuationToken, context)));
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
     * {@link Flux} containing {@link Certificate certificate} using {@link CertificateAsyncClient#getCertificate(CertificateProperties certificateProperties)} within {@link Flux#flatMap(Function)}.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificates}
     *
     * @return A {@link PagedFlux} containing {@link CertificateProperties certificate} for all the certificates in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CertificateProperties> listCertificates() {
        try {
            return new PagedFlux<>(() -> withContext(context -> listCertificatesFirstPage(false, context)),
                continuationToken -> withContext(context -> listCertificatesNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<CertificateProperties> listCertificates(Boolean includePending, Context context) {
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
            return service.getCertificates(endpoint, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Listing next certificates page - Page {} ", continuationToken))
                .doOnSuccess(response -> logger.info("Listed next certificates page - Page {} ", continuationToken))
                .doOnError(error -> logger.warning("Failed to list next certificates page - Page {} ", continuationToken, error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /*
     * Calls the service and retrieve first page result. It makes one call and retrieve {@code DEFAULT_MAX_PAGE_RESULTS} values.
     */
    private Mono<PagedResponse<CertificateProperties>> listCertificatesFirstPage(Boolean includePending, Context context) {
        try {
            return service
                .getCertificates(endpoint, DEFAULT_MAX_PAGE_RESULTS, includePending, API_VERSION, ACCEPT_LANGUAGE,
                    CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Listing certificates"))
                .doOnSuccess(response -> logger.info("Listed certificates"))
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
                () -> withContext(context -> listDeletedCertificatesFirstPage(context)),
                continuationToken -> withContext(
                    context -> listDeletedCertificatesNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<DeletedCertificate> listDeletedCertificates(Context context) {
        return new PagedFlux<>(
            () -> listDeletedCertificatesFirstPage(context),
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
                .getDeletedCertificates(endpoint, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                    context)
                .doOnRequest(
                    ignored -> logger.info("Listing next deleted certificates page - Page {} ", continuationToken))
                .doOnSuccess(
                    response -> logger.info("Listed next deleted certificates page - Page {} ", continuationToken))
                .doOnError(error -> logger
                    .warning("Failed to list next deleted certificates page - Page {} ", continuationToken, error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /*
     * Calls the service and retrieve first page result. It makes one call and retrieve {@code DEFAULT_MAX_PAGE_RESULTS} values.
     */
    private Mono<PagedResponse<DeletedCertificate>> listDeletedCertificatesFirstPage(Context context) {
        try {
            return service.getDeletedCertificates(endpoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Listing deleted certificates"))
                .doOnSuccess(response -> logger.info("Listed deleted certificates"))
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
     * containing {@link CertificateProperties} to {@link PagedFlux} containing {@link Certificate certificate} using
     * {@link CertificateAsyncClient#getCertificate(CertificateProperties certificateProperties)} within {@link Flux#flatMap(Function)}.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificateVersions}
     *
     * @param name The name of the certificate.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     * @return A {@link PagedFlux} containing {@link CertificateProperties certificate} of all the versions of the specified certificate in the vault. Flux is empty if certificate with {@code name} does not exist in key vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CertificateProperties> listCertificateVersions(String name) {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listCertificateVersionsFirstPage(name, context)),
                continuationToken -> withContext(context -> listCertificateVersionsNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<CertificateProperties> listCertificateVersions(String name, Context context) {
        return new PagedFlux<>(
            () -> listCertificateVersionsFirstPage(name, context),
            continuationToken -> listCertificateVersionsNextPage(continuationToken, context));
    }

    private Mono<PagedResponse<CertificateProperties>> listCertificateVersionsFirstPage(String name, Context context) {
        try {
            return service.getCertificateVersions(endpoint, name, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Listing certificate versions - {}", name))
                .doOnSuccess(response -> logger.info("Listed certificate versions - {}", name))
                .doOnError(error -> logger.warning(String.format("Failed to list certificate versions - {}", name), error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /*
     * Gets attributes of all the certificates given by the {@code nextPageLink}
     */
    private Mono<PagedResponse<CertificateProperties>> listCertificateVersionsNextPage(String continuationToken, Context context) {
        try {
            return service.getCertificates(endpoint, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Listing next certificate versions page - Page {} ", continuationToken))
                .doOnSuccess(response -> logger.info("Listed next certificate versions page - Page {} ", continuationToken))
                .doOnError(error -> logger.warning("Failed to list next certificate versions page - Page {} ", continuationToken, error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets the pending certificate signing request for the specified certificate under pending status.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the pending signing request of a certificate created with third party issuer. Prints out the
     * returned certificate signing request details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.getPendingCertificateSigningRequest#string}
     *
     * @param certificateName the certificate for whom certifcate signing request is needed
     * @return A {@link Mono} containing the cerficate signing request blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<byte[]> getPendingCertificateSigningRequest(String certificateName) {
        try {
            return withContext(context -> getPendingCertificateSigningRequestWithResponse(certificateName, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets the pending certificate signing request for the specified certificate under pending status.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the pending signing request of a certificate created with third party issuer. Prints out the
     * returned certificate signing request details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.getPendingCertificateSigningRequestWithResponse#string}
     *
     * @param certificateName the certificate for whom certifcate signing request is needed
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the certificate signing request blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<byte[]>> getPendingCertificateSigningRequestWithResponse(String certificateName) {
        try {
            return withContext(context -> getPendingCertificateSigningRequestWithResponse(certificateName, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }


    Mono<Response<byte[]>> getPendingCertificateSigningRequestWithResponse(String certificateName, Context context) {
        return service.getPendingCertificateSigningRequest(endpoint, API_VERSION, ACCEPT_LANGUAGE, certificateName, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Retrieving CSR for certificate - {} ", certificateName))
            .doOnSuccess(response -> logger.info("Failed to retrieve CSR for certificate - {} ", certificateName))
            .doOnError(error -> logger.warning("Retrieved CSR for certificate - {} - {} ", certificateName, error))
            .flatMap(certificateOperationResponse -> Mono.just(new SimpleResponse<>(certificateOperationResponse.getRequest(),
                certificateOperationResponse.getStatusCode(), certificateOperationResponse.getHeaders(), certificateOperationResponse.getValue().getCsr())));
    }

    /**
     * Merges a certificate or a certificate chain with a key pair currently available in the service. This operation requires
     * the {@code certificates/create} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Merges a certificate with a kay pair available in the service.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificate#String-List}
     *
     * @param name the name of the certificate.
     * @param x509Certificates the certificate or certificate chain to merge.
     *
     * @throws HttpRequestException if {@code x509Certificates} is invalid/corrupt or {@code name} is empty.
     * @return A {@link Mono} containing the merged certificate.
     */
    public Mono<Certificate> mergeCertificate(String name, List<byte[]> x509Certificates) {
        try {
            return withContext(context -> mergeCertificateWithResponse(name, x509Certificates, context)).flatMap(FluxUtil::toMono);
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
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificateWithResponse#String-List}
     *
     * @param name the name of the certificate.
     * @param x509Certificates the certificate or certificate chain to merge.
     *
     * @throws HttpRequestException if {@code x509Certificates} is invalid/corrupt or {@code name} is empty.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the merged certificate.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Certificate>> mergeCertificateWithResponse(String name, List<byte[]> x509Certificates) {
        try {
            return withContext(context -> mergeCertificateWithResponse(name, x509Certificates, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Certificate>> mergeCertificateWithResponse(String name, List<byte[]> x509Certificates, Context context) {
        CertificateMergeParameters mergeParameters = new CertificateMergeParameters().x509Certificates(x509Certificates);
        return service.mergeCertificate(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, mergeParameters, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Merging certificate - {}",  name))
            .doOnSuccess(response -> logger.info("Merged certificate  - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> logger.warning("Failed to merge certificate - {}", name, error));
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
     * @param mergeCertificateConfig the merge certificate configuration holding the x509 certificates.
     *
     * @throws NullPointerException when {@code mergeCertificateConfig} is null.
     * @throws HttpRequestException if {@code mergeCertificateConfig} is invalid/corrupt.
     * @return A {@link Mono} containing the merged certificate.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Certificate> mergeCertificate(MergeCertificateOptions mergeCertificateConfig) {
        try {
            return withContext(context -> mergeCertificateWithResponse(mergeCertificateConfig, context)).flatMap(FluxUtil::toMono);
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
     * @param mergeCertificateConfig the merge certificate configuration holding the x509 certificates.
     *
     * @throws NullPointerException when {@code mergeCertificateConfig} is null.
     * @throws HttpRequestException if {@code mergeCertificateConfig} is invalid/corrupt.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the merged certificate.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Certificate>> mergeCertificateWithResponse(MergeCertificateOptions mergeCertificateConfig) {
        try {
            Objects.requireNonNull(mergeCertificateConfig, "'mergeCertificateConfig' cannot be null.");
            return withContext(context -> mergeCertificateWithResponse(mergeCertificateConfig, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Certificate>> mergeCertificateWithResponse(MergeCertificateOptions mergeCertificateConfig, Context context) {
        CertificateMergeParameters mergeParameters = new CertificateMergeParameters().x509Certificates(mergeCertificateConfig.getX509Certificates())
            .tags(mergeCertificateConfig.getTags())
            .certificateAttributes(new CertificateRequestAttributes().enabled(mergeCertificateConfig.isEnabled()));
        return service.mergeCertificate(endpoint, mergeCertificateConfig.getName(), API_VERSION, ACCEPT_LANGUAGE, mergeParameters, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Merging certificate - {}",  mergeCertificateConfig.getName()))
            .doOnSuccess(response -> logger.info("Merged certificate  - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> logger.warning("Failed to merge certificate - {}", mergeCertificateConfig.getName(), error));
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
     * @param name The name of the certificate whose policy is to be retrieved, cannot be null
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the requested {@link CertificatePolicy certificate policy}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CertificatePolicy> getCertificatePolicy(String name) {
        try {
            return withContext(context -> getCertificatePolicyWithResponse(name, context)).flatMap(FluxUtil::toMono);
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
     * @param name The name of the certificate whose policy is to be retrieved, cannot be null
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Mono} containing the requested {@link CertificatePolicy certificate policy}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CertificatePolicy>> getCertificatePolicyWithResponse(String name) {
        try {
            return withContext(context -> getCertificatePolicyWithResponse(name, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<CertificatePolicy>> getCertificatePolicyWithResponse(String name, Context context) {
        return service.getCertificatePolicy(endpoint, API_VERSION, ACCEPT_LANGUAGE, name, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Retrieving certificate policy - {}",  name))
            .doOnSuccess(response -> logger.info("Retrieved certificate policy - {}", name))
            .doOnError(error -> logger.warning("Failed to retrieve certificate policy - {}", name, error));
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
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string or if {@code policy} is invalid.
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
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string or if {@code policy} is invalid.
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
        CertificatePolicyRequest policyRequest = new CertificatePolicyRequest(policy);
        return service.updateCertificatePolicy(endpoint, API_VERSION, ACCEPT_LANGUAGE, certificateName, policyRequest, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Updating certificate policy - {}",  certificateName))
            .doOnSuccess(response -> logger.info("Updated the certificate policy - {}", response.getValue().getUpdated()))
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
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.createIssuer#String-String}
     *
     * @param name The name of the certificate issuer to be created.
     * @param provider The provider of the certificate issuer to be created.
     * @throws ResourceModifiedException when invalid certificate issuer {@code name} or {@code provider} configuration is provided.
     * @throws HttpRequestException when a certificate issuer with {@code name} is empty string.
     * @return A {@link Mono} containing the created {@link Issuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Issuer> createIssuer(String name, String provider) {
        try {
            return withContext(context -> createIssuerWithResponse(name, provider, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Issuer>> createIssuerWithResponse(String name, String provider, Context context) {
        CertificateIssuerSetParameters parameters = new CertificateIssuerSetParameters()
                    .provider(provider);
        return service.setCertificateIssuer(endpoint, API_VERSION, ACCEPT_LANGUAGE, name, parameters, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Creating certificate issuer - {}", name))
            .doOnSuccess(response -> logger.info("Created the certificate issuer - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create the certificate issuer - {}", name, error));
    }

    /**
     * Creates the specified certificate issuer. The SetCertificateIssuer operation updates the specified certificate issuer if it
     * already exists or adds it if doesn't exist. This operation requires the certificates/setissuers permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new certificate issuer in the key vault. Prints out the created certificate
     * issuer details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.createIssuer#issuer}
     *
     * @param issuer The configuration of the certificate issuer to be created.
     * @throws ResourceModifiedException when invalid certificate issuer {@code issuer} configuration is provided.
     * @throws HttpRequestException when a certificate issuer with {@code name} is empty string.
     * @return A {@link Mono} containing the created {@link Issuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Issuer> createIssuer(Issuer issuer) {
        try {
            return withContext(context -> createIssuerWithResponse(issuer, context)).flatMap(FluxUtil::toMono);
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
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.createIssuerWithResponse#issuer}
     *
     * @param issuer The configuration of the certificate issuer to be created.
     * @throws ResourceModifiedException when invalid certificate issuer {@code issuer} configuration is provided.
     * @throws HttpRequestException when a certificate issuer with {@code name} is empty string.
     * @return A {@link Mono} containing  a {@link Response} whose {@link Response#getValue() value} contains the created {@link Issuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Issuer>> createIssuerWithResponse(Issuer issuer) {
        try {
            return withContext(context -> createIssuerWithResponse(issuer, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Issuer>> createIssuerWithResponse(Issuer issuer, Context context) {
        CertificateIssuerSetParameters parameters = new CertificateIssuerSetParameters()
            .provider(issuer.getProperties().getProvider())
            .credentials(new IssuerCredentials().accountId(issuer.getAccountId()).password(issuer.getPassword()))
            .organizationDetails(new OrganizationDetails().adminDetails(issuer.getAdministrators()))
            .credentials(new IssuerCredentials().password(issuer.getPassword()).accountId(issuer.getAccountId()));
        return service.setCertificateIssuer(endpoint, API_VERSION, ACCEPT_LANGUAGE, issuer.getName(), parameters, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Creating certificate issuer - {}",  issuer.getName()))
            .doOnSuccess(response -> logger.info("Created the certificate issuer - {}", response.getValue().getName()))
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
     * @param name The name of the certificate to retrieve, cannot be null
     * @throws ResourceNotFoundException when a certificate issuer with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the requested {@link Issuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Issuer>> getIssuerWithResponse(String name) {
        try {
            return withContext(context -> getIssuerWithResponse(name, context));
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
     * @param name The name of the certificate to retrieve, cannot be null
     * @throws ResourceNotFoundException when a certificate issuer with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Mono} containing the requested {@link Issuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Issuer> getIssuer(String name) {
        try {
            return withContext(context -> getIssuerWithResponse(name, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Issuer>> getIssuerWithResponse(String name, Context context) {
        return service.getCertificateIssuer(endpoint, API_VERSION, ACCEPT_LANGUAGE, name, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Retrieving certificate issuer - {}",  name))
            .doOnSuccess(response -> logger.info("Retrieved the certificate issuer - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to retreive the certificate issuer - {}", name, error));
    }

    /**
     * Gets information about the certificate issuer which represents the {@link IssuerProperties} from the key vault. This operation
     * requires the certificates/manageissuers/getissuers permission.
     *
     * <p>The list operations {@link CertificateAsyncClient#listIssuers()} return the {@link PagedFlux} containing
     * {@link IssuerProperties issuerProperties} as output excluding the properties like accountId and organization details of the certificate issuer.
     * This operation can then be used to get the full certificate issuer with its properties from {@code issuerProperties}.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.getIssuer#issuerProperties}
     *
     * @param issuerProperties The {@link IssuerProperties issuerProperties} holding attributes of the certificate issuer being requested.
     * @throws ResourceNotFoundException when a certificate with {@link IssuerProperties#getName() name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link IssuerProperties#getName() name} is empty string.
     * @return A {@link Mono} containing the requested {@link Issuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Issuer> getIssuer(IssuerProperties issuerProperties) {
        try {
            return withContext(context -> getIssuerWithResponse(issuerProperties.getName(), context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets information about the certificate issuer which represents the {@link IssuerProperties} from the key vault. This operation
     * requires the certificates/manageissuers/getissuers permission.
     *
     * <p>The list operations {@link CertificateAsyncClient#listIssuers()} return the {@link PagedFlux} containing
     * {@link IssuerProperties issuerProperties} as output excluding the properties like accountId and organization details of the certificate issuer.
     * This operation can then be used to get the full certificate issuer with its properties from {@code issuerProperties}.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.getIssuerWithResponse#issuerProperties}
     *
     * @param issuerProperties The {@link IssuerProperties issuerProperties} holding attributes of the certificate issuer being requested.
     * @throws ResourceNotFoundException when a certificate with {@link IssuerProperties#getName() name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link IssuerProperties#getName() name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the requested {@link Issuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Issuer>> getIssuerWithResponse(IssuerProperties issuerProperties) {
        try {
            return withContext(context -> getIssuerWithResponse(issuerProperties.getName(), context));
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
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteIssuerWithResponse#string}
     *
     * @param name The name of the certificate issuer to be deleted.
     * @throws ResourceNotFoundException when a certificate issuer with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate issuer with {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link Issuer deleted issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Issuer>> deleteIssuerWithResponse(String name) {
        try {
            return withContext(context -> deleteIssuerWithResponse(name, context));
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
     * @param name The name of the certificate issuer to be deleted.
     * @throws ResourceNotFoundException when a certificate issuer with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate issuer with {@code name} is empty string.
     * @return A {@link Mono} containing the {@link Issuer deleted issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Issuer> deleteIssuer(String name) {
        try {
            return withContext(context -> deleteIssuerWithResponse(name, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Issuer>> deleteIssuerWithResponse(String name, Context context) {
        return service.deleteCertificateIssuer(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Deleting certificate issuer - {}",  name))
            .doOnSuccess(response -> logger.info("Deleted the certificate issuer - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to delete the certificate issuer - {}", name, error));
    }


    /**
     * List all the certificate issuers resources in the key vault. The individual certificate issuer response in the flux is represented by {@link IssuerProperties}
     * as only the certificate issuer identifier and provider are provided in the response. This operation requires the
     * {@code certificates/manageissuers/getissuers} permission.
     *
     * <p>It is possible to get the certificate issuer with all of its properties from this information. Convert the {@link PagedFlux}
     * containing {@link IssuerProperties issuerProperties} to {@link PagedFlux} containing {@link Issuer issuer} using
     * {@link CertificateAsyncClient#getIssuer(IssuerProperties issuerProperties)} within {@link PagedFlux#flatMap(Function)}.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.listIssuers}
     *
     * @return A {@link PagedFlux} containing all of the {@link IssuerProperties certificate issuers} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<IssuerProperties> listIssuers() {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listCertificateIssuersFirstPage(context)),
                continuationToken -> withContext(context -> listCertificateIssuersNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<IssuerProperties> listIssuers(Context context) {
        return new PagedFlux<>(
            () -> listCertificateIssuersFirstPage(context),
            continuationToken -> listCertificateIssuersNextPage(continuationToken, context));
    }

    private Mono<PagedResponse<IssuerProperties>> listCertificateIssuersFirstPage(Context context) {
        try {
            return service.getCertificateIssuers(endpoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Listing certificate issuers - {}"))
                .doOnSuccess(response -> logger.info("Listed certificate issuers - {}"))
                .doOnError(error -> logger.warning(String.format("Failed to list certificate issuers - {}"), error));
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
    private Mono<PagedResponse<IssuerProperties>> listCertificateIssuersNextPage(String continuationToken, Context context) {
        try {
            return service.getCertificateIssuers(endpoint, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Listing next certificate issuers page - Page {} ", continuationToken))
                .doOnSuccess(response -> logger.info("Listed next certificate issuers page - Page {} ", continuationToken))
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
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.updateIssuer#IssuerProperties}
     *
     * @param issuer The {@link Issuer issuer} with updated properties.
     * @throws NullPointerException if {@code issuer} is {@code null}.
     * @throws ResourceNotFoundException when a certificate issuer with {@link Issuer#getName() name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link Issuer#getName() name} is empty string.
     * @return A {@link Mono} containing the {@link Issuer updated issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Issuer> updateIssuer(Issuer issuer) {
        try {
            return withContext(context -> updateIssuerWithResponse(issuer, context).flatMap(FluxUtil::toMono));
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
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.updateIssuer#IssuerProperties}
     *
     * @param issuer The {@link Issuer issuer} with updated properties.
     * @throws NullPointerException if {@code issuer} is {@code null}.
     * @throws ResourceNotFoundException when a certificate issuer with {@link Issuer#getName() name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link Issuer#getName() name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link Issuer updated issuer}.
     */
    public Mono<Response<Issuer>> updateIssuerWithResponse(Issuer issuer) {
        try {
            return withContext(context -> updateIssuerWithResponse(issuer, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Issuer>> updateIssuerWithResponse(Issuer issuer, Context context) {
        CertificateIssuerUpdateParameters updateParameters = new CertificateIssuerUpdateParameters()
            .provider(issuer.getProperties().getProvider())
            .organizationDetails(new OrganizationDetails().adminDetails(issuer.getAdministrators()))
            .credentials(new IssuerCredentials().password(issuer.getPassword()).accountId(issuer.getAccountId()));
        return service.updateCertificateIssuer(endpoint, issuer.getName(), API_VERSION, ACCEPT_LANGUAGE, updateParameters, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Updating certificate issuer - {}",  issuer.getName()))
            .doOnSuccess(response -> logger.info("Updated up the certificate issuer - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to updated the certificate issuer - {}", issuer.getName(), error));
    }

    /**
     * Sets the certificate contacts on the key vault. This operation requires the {@code certificates/managecontacts} permission.
     *
     *<p>The {@link LifetimeAction} of type {@link LifetimeActionType#EMAIL_CONTACTS} set on a {@link CertificatePolicy} emails the contacts set on the vault when triggered.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Sets the certificate contacts in the Azure Key Vault. Prints out the
     * returned contacts details.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateAsyncClient.setContacts#contacts}
     *
     * @param contacts The list of contacts to set on the vault.
     * @throws HttpRequestException when a contact information provided is invalid/incomplete.
     * @return A {@link PagedFlux} containing all of the {@link Contact certificate contacts} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<Contact> setContacts(List<Contact> contacts) {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> setCertificateContactsWithResponse(contacts, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<Contact> setContacts(List<Contact> contacts, Context context) {
        return new PagedFlux<>(
            () -> setCertificateContactsWithResponse(contacts, context));
    }

    private Mono<PagedResponse<Contact>> setCertificateContactsWithResponse(List<Contact> contacts, Context context) {
        Contacts contactsParams = new Contacts().contactList(contacts);
        return service.setCertificateContacts(endpoint, API_VERSION, ACCEPT_LANGUAGE, contactsParams, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Listing certificate contacts - {}"))
            .doOnSuccess(response -> logger.info("Listed certificate contacts - {}"))
            .doOnError(error -> logger.warning(String.format("Failed to list certificate contacts - {}"), error));
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
     * @return A {@link PagedFlux} containing all of the {@link Contact certificate contacts} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<Contact> listContacts() {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listCertificateContactsFirstPage(context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<Contact> listContacts(Context context) {
        return new PagedFlux<>(
            () -> listCertificateContactsFirstPage(context));
    }

    private Mono<PagedResponse<Contact>> listCertificateContactsFirstPage(Context context) {
        try {
            return service.getCertificateContacts(endpoint, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Listing certificate contacts - {}"))
                .doOnSuccess(response -> logger.info("Listed certificate contacts - {}"))
                .doOnError(error -> logger.warning(String.format("Failed to list certificate contacts - {}"), error));
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
     * @return A {@link PagedFlux} containing all of the {@link Contact deleted certificate contacts} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<Contact> deleteContacts() {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> deleteCertificateContactsWithResponse(context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<Contact> deleteContacts(Context context) {
        return new PagedFlux<>(
            () -> deleteCertificateContactsWithResponse(context));
    }

    private Mono<PagedResponse<Contact>> deleteCertificateContactsWithResponse(Context context) {
        return service.deleteCertificateContacts(endpoint, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Deleting certificate contacts - {}"))
            .doOnSuccess(response -> logger.info("Deleted certificate contacts - {}"))
            .doOnError(error -> logger.warning(String.format("Failed to delete certificate contacts - {}"), error));
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
     * @throws ResourceNotFoundException when a certificate operation for a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when the {@code name} is empty string.
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
     * @throws ResourceNotFoundException when a certificate operation for a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when the {@code name} is empty string.
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
        return service.deletetCertificateOperation(endpoint, certificateName, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Deleting certificate operation - {}",  certificateName))
            .doOnSuccess(response -> logger.info("Deleted the certificate operation - {}", response.getStatusCode()))
            .doOnError(error -> logger.warning("Failed to delete the certificate operation - {}", certificateName, error));
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
     * @throws HttpRequestException when the {@code name} is empty string.
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
     * @throws HttpRequestException when the {@code name} is empty string.
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

    Mono<Response<CertificateOperation>> cancelCertificateOperationWithResponse(String certificateName, Context context) {
        CertificateOperationUpdateParameter parameter = new CertificateOperationUpdateParameter().cancellationRequested(true);
        return service.updateCertificateOperation(endpoint, certificateName, API_VERSION, ACCEPT_LANGUAGE, parameter, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Cancelling certificate operation - {}",  certificateName))
            .doOnSuccess(response -> logger.info("Cancelled the certificate operation - {}", response.getValue().getStatus()))
            .doOnError(error -> logger.warning("Failed to cancel the certificate operation - {}", certificateName, error));
    }

    /**
     * Imports a pre-existing certificate to the key vault. The specified certificate must be in PFX or PEM format,
     * and must contain the private key as well as the x509 certificates. This operation requires the {@code certificates/import} permission.
     *
     * @param importOptions The details of the certificate to import to the key vault
     * @throws HttpRequestException when the {@code importOptions} are invalid.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link Certificate imported certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Certificate> importCertificate(CertificateImportOptions importOptions) {
        try {
            return withContext(context -> importCertificateWithResponse(importOptions, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Imports a pre-existing certificate to the key vault. The specified certificate must be in PFX or PEM format,
     * and must contain the private key as well as the x509 certificates. This operation requires the {@code certificates/import} permission.
     *
     * @param importOptions The details of the certificate to import to the key vault
     * @throws HttpRequestException when the {@code importOptions} are invalid.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link Certificate imported certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Certificate>> importCertificateWithResponse(CertificateImportOptions importOptions) {
        try {
            return withContext(context -> importCertificateWithResponse(importOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Certificate>> importCertificateWithResponse(CertificateImportOptions importOptions, Context context) {
        CertificateImportParameters parameters = new CertificateImportParameters()
            .base64EncodedCertificate(Base64Url.encode(importOptions.getValue()).toString())
            .certificateAttributes(new CertificateRequestAttributes(importOptions))
            .certificatePolicy(importOptions.getCertificatePolicy())
            .password(importOptions.getPassword())
            .tags(importOptions.getTags());

        return service.importCertificate(endpoint, importOptions.getName(), API_VERSION, ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context);
    }

}

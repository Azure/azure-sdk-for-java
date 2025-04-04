// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.certificates;

import com.azure.v2.core.http.polling.LongRunningOperationStatus;
import com.azure.v2.core.http.polling.PollResponse;
import com.azure.v2.core.http.polling.Poller;
import com.azure.v2.core.http.polling.PollingContext;
import com.azure.v2.security.keyvault.certificates.implementation.CertificateClientImpl;
import com.azure.v2.security.keyvault.certificates.implementation.CertificateIssuerHelper;
import com.azure.v2.security.keyvault.certificates.implementation.CertificateOperationHelper;
import com.azure.v2.security.keyvault.certificates.implementation.CertificatePolicyHelper;
import com.azure.v2.security.keyvault.certificates.implementation.CertificatePropertiesHelper;
import com.azure.v2.security.keyvault.certificates.implementation.DeletedCertificateHelper;
import com.azure.v2.security.keyvault.certificates.implementation.IssuerPropertiesHelper;
import com.azure.v2.security.keyvault.certificates.implementation.KeyVaultCertificateWithPolicyHelper;
import com.azure.v2.security.keyvault.certificates.implementation.models.BackupCertificateResult;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateAttributes;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateCreateParameters;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateImportParameters;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateIssuerSetParameters;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateIssuerUpdateParameters;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateMergeParameters;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateOperationUpdateParameter;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateRestoreParameters;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateUpdateParameters;
import com.azure.v2.security.keyvault.certificates.implementation.models.Contacts;
import com.azure.v2.security.keyvault.certificates.implementation.models.IssuerBundle;
import com.azure.v2.security.keyvault.certificates.models.CertificateContact;
import com.azure.v2.security.keyvault.certificates.models.CertificateContentType;
import com.azure.v2.security.keyvault.certificates.models.CertificateIssuer;
import com.azure.v2.security.keyvault.certificates.models.CertificateOperation;
import com.azure.v2.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.v2.security.keyvault.certificates.models.CertificatePolicyAction;
import com.azure.v2.security.keyvault.certificates.models.CertificateProperties;
import com.azure.v2.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.v2.security.keyvault.certificates.models.ImportCertificateOptions;
import com.azure.v2.security.keyvault.certificates.models.IssuerProperties;
import com.azure.v2.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.v2.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import com.azure.v2.security.keyvault.certificates.models.LifetimeAction;
import com.azure.v2.security.keyvault.certificates.models.MergeCertificateOptions;
import io.clientcore.core.annotations.ReturnType;
import io.clientcore.core.annotations.ServiceClient;
import io.clientcore.core.annotations.ServiceMethod;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.paging.PagedIterable;
import io.clientcore.core.http.paging.PagedResponse;
import io.clientcore.core.http.paging.PagingOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.Context;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.v2.security.keyvault.certificates.implementation.CertificateIssuerHelper.createCertificateIssuer;
import static com.azure.v2.security.keyvault.certificates.implementation.CertificateIssuerHelper.getIssuerBundle;
import static com.azure.v2.security.keyvault.certificates.implementation.CertificateOperationHelper.createCertificateOperation;
import static com.azure.v2.security.keyvault.certificates.implementation.CertificatePolicyHelper.createCertificatePolicy;
import static com.azure.v2.security.keyvault.certificates.implementation.CertificatePolicyHelper.getImplCertificatePolicy;
import static com.azure.v2.security.keyvault.certificates.implementation.DeletedCertificateHelper.createDeletedCertificate;
import static com.azure.v2.security.keyvault.certificates.implementation.KeyVaultCertificateWithPolicyHelper.createCertificateWithPolicy;
import static io.clientcore.core.utils.CoreUtils.isNullOrEmpty;

/**
 * This class provides methods to manage {@link KeyVaultCertificate certifcates} in Azure Key Vault. The client supports
 * creating, retrieving, updating, merging, deleting, purging, backing up, restoring and listing the
 * {@link KeyVaultCertificate certificates}. The client also supports listing
 * {@link DeletedCertificate deleted certificates} for a soft-delete enabled key vault.
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Azure Key Vault service, you will need to create an instance of the
 * {@link CertificateClient} class, an Azure Key Vault endpoint and a credential object.</p>
 *
 * <p>The examples shown in this document use a credential object named {@code DefaultAzureCredential} for
 * authentication, which is appropriate for most scenarios, including local development and production environments.
 * Additionally, we recommend using a
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments. You can find more information on different ways of authenticating and
 * their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">Azure Identity documentation"</a>.</p>
 *
 * <p><strong>Sample: Construct Certificate Client</strong></p>
 * <p>The following code sample demonstrates the creation of a {@link CertificateClient}, using the
 * {@link CertificateClientBuilder} to configure it.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.instantiation -->
 * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.instantiation  -->
 *
 * <br/>
 * <hr/>
 *
 * <h2>Create a Certificate</h2>
 * The {@link CertificateClient} can be used to create a certificate in the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to create a certificate in the key vault, using the
 * {/@link CertificateClient#beginCreateCertificate(String, CertificatePolicy)} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy -->
 * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy -->
 *
 * <br/>
 * <hr/>
 *
 * <h2>Get a Certificate</h2>
 * The {@link CertificateClient} can be used to retrieve a certificate from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to retrieve a certificate from the key vault, using the
 * {@link CertificateClient#getCertificate(String)} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificatePolicy#String -->
 * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificatePolicy#String -->
 *
 * <br/>
 * <hr/>
 *
 * <h2>Delete a Certificate</h2>
 * The {@link CertificateClient} can be used to delete a certificate from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to delete a certificate from the key vault, using the
 * {/@link CertificateClient#beginDeleteCertificate(String)} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.beginDeleteCertificate#String -->
 * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.beginDeleteCertificate#String -->
 *
 * @see com.azure.v2.security.keyvault.certificates
 * @see CertificateClientBuilder
 */
@ServiceClient(
    builder = CertificateClientBuilder.class, serviceInterfaces = CertificateClientImpl.CertificateClientService.class)
public final class CertificateClient {
    private static final ClientLogger LOGGER = new ClientLogger(CertificateClient.class);

    private final CertificateClientImpl clientImpl;

    /**
     * Creates an instance of {@link CertificateClient} that sends requests to the given endpoint.
     *
     * @param clientImpl The implementation client.
     */
    CertificateClient(CertificateClientImpl clientImpl) {
        this.clientImpl = clientImpl;
    }

    /**
     * Creates a new certificate in the key vault. If a certificate with the provided name already exists, a new version
     * of the certificate is created. It requires the {@code certificates/create} permission.
     *
     * <p>Create certificate is a long running operation. It indefinitely waits for the create certificate operation to
     * complete on service side.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new certificate in the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy -->
     *
     * @param name The name of the certificate to create. It is required and cannot be {@code null} or empty.
     * @param policy The policy of the certificate to be created.
     * @return A {@link Poller} to poll on and retrieve the created certificate with.
     *
     * @throws HttpResponseException If the provided {@code policy} is malformed.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
    // TODO (vcolin7): Uncomment when creating a Poller is supported in azure-core-v2.
    /*@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<CertificateOperation, KeyVaultCertificateWithPolicy> beginCreateCertificate(String name,
        CertificatePolicy policy) {

        return beginCreateCertificate(name, policy, true, null);
    }*/

    /**
     * Creates a new certificate in the key vault. If a certificate with the provided name already exists, a new version
     * of the certificate is created. It requires the {@code certificates/create} permission.
     *
     * <p>Create certificate is a long running operation. It indefinitely waits for the create certificate operation to
     * complete on service side.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new certificate in the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy-Boolean-Map -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy-Boolean-Map -->
     *
     * @param name The name of the certificate to create. It is required and cannot be {@code null} or empty.
     * @param policy The policy of the certificate to be created.
     * @param isEnabled A value indicating whether the certificate is to be enabled.
     * @param tags Application specific metadata in the form of key-value pairs.
     * @return A {@link Poller} to poll on and retrieve the created certificate with.
     *
     * @throws HttpResponseException If the provided {@code policy} is malformed.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    // TODO (vcolin7): Uncomment when creating a Poller is supported in azure-core-v2.
    /*@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<CertificateOperation, KeyVaultCertificateWithPolicy> beginCreateCertificate(String name,
        CertificatePolicy policy, Boolean isEnabled, Map<String, String> tags) {

        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return Poller.createPoller(Duration.ofSeconds(1),
                pollingContext -> createCertificateActivation(name, policy, isEnabled, tags),
                pollingContext -> certificatePollOperation(name),
                (pollingContext, pollResponse) -> certificateCancellationOperation(name),
                pollingContext -> fetchCertificateOperation(name));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }*/

    private PollResponse<CertificateOperation> createCertificateActivation(String certificateName,
        CertificatePolicy policy, Boolean isEnabled, Map<String, String> tags) {

        CertificateCreateParameters certificateCreateParameters =
            new CertificateCreateParameters()
                .setCertificatePolicy(getImplCertificatePolicy(policy))
                .setCertificateAttributes(new CertificateAttributes().setEnabled(isEnabled))
                .setTags(tags);

        return new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
            createCertificateOperation(clientImpl.createCertificate(certificateName, certificateCreateParameters)));
    }

    private PollResponse<CertificateOperation> certificatePollOperation(String certificateName) {
        CertificateOperation certificateOperation =
            CertificateOperationHelper.createCertificateOperation(clientImpl.getCertificateOperation(certificateName));

        return new PollResponse<>(mapStatus(certificateOperation.getStatus()), certificateOperation);
    }

    private CertificateOperation certificateCancellationOperation(String name) {
        return createCertificateOperation(
            clientImpl.updateCertificateOperation(name, new CertificateOperationUpdateParameter(true)));
    }

    private KeyVaultCertificateWithPolicy fetchCertificateOperation(String name) {
        return createCertificateWithPolicy(clientImpl.getCertificate(name, null));
    }

    private static LongRunningOperationStatus mapStatus(String status) {
        switch (status) {
            case "inProgress":
                return LongRunningOperationStatus.IN_PROGRESS;

            case "completed":
                return LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;

            case "failed":
                return LongRunningOperationStatus.FAILED;

            default:
                return LongRunningOperationStatus.fromString(status, true);
        }
    }

    /**
     * Gets public information about the latest version of a given certificate. This operation requires the
     * {@code certificates/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the latest version of the certificate in the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificate#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificate#String -->
     *
     * @param name The name of the certificate to retrieve. It is required and cannot be {@code null} or empty.
     * @return The requested certificate.
     *
     * @throws HttpResponseException If a certificate with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultCertificateWithPolicy getCertificate(String name) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return createCertificateWithPolicy(clientImpl.getCertificate(name, ""));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets information about a specific version of a given certificate. This operation requires the
     * {@code certificates/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a specific version of the certificate in the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateVersion#String-String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateVersion#String-String -->
     *
     * @param name The name of the certificate to retrieve. It is required and cannot be {@code null} or empty.
     * @param version The version of the certificate to retrieve. If this is an empty string or {@code null}, this call
     * is equivalent to calling {@link CertificateClient#getCertificate(String)}, with the latest version being
     * retrieved.
     * @return The requested certificate.
     *
     * @throws HttpResponseException If a certificate with the given {@code name} and {@code version} doesn't exist in
     * the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultCertificate getCertificate(String name, String version) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return createCertificateWithPolicy(clientImpl.getCertificate(name, version));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets information about the latest version of the specified certificate. This operation requires the
     * {@code certificates/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a specific version of a certificate in the key vault. Prints out details of the response returned by the
     * service and the certificate.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateWithResponse#String-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateWithResponse#String-RequestOptions -->
     *
     * @param name The name of the certificate to retrieve. It is required and cannot be {@code null} or empty.
     * @param version The version of the certificate to retrieve. If this is an empty string or {@code null}, the latest
     * version will be retrieved.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue()} contains the requested certificate.
     *
     * @throws HttpResponseException If a certificate with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultCertificateWithPolicy> getCertificateWithResponse(String name, String version,
        RequestOptions requestOptions) {

        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return mapResponse(clientImpl.getCertificateWithResponse(name, version, requestOptions),
                KeyVaultCertificateWithPolicyHelper::createCertificateWithPolicy);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Updates the attributes associated with the specified certificate, but not the associated key material in the key
     * vault. Certificate attributes that are not specified in the request are left unchanged. This operation requires
     * the {@code certificates/set} permission.
     *
     * <p>The {@code certificateProperties} parameter and its {@link CertificateProperties#getName() name} value are
     * required.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets latest version of a certificate and updates its tags and enabled status in the key vault, then prints out
     * the updated certificate's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificateProperties#CertificateProperties -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificateProperties#CertificateProperties -->
     *
     * @param certificateProperties The certificate properties to update.
     * @return The updated certificate.
     *
     * @throws HttpResponseException If a certificate with the given {@link CertificateProperties#getName() name} and
     * {@link CertificateProperties#getVersion() version} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@link CertificateProperties#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code certificateProperties} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultCertificate updateCertificateProperties(CertificateProperties certificateProperties) {
        try {
            Objects.requireNonNull(certificateProperties, "'certificateProperties' cannot be null.");

            if (isNullOrEmpty(certificateProperties.getName())) {
                throw new IllegalArgumentException("'certificateProperties.getName()' cannot be null or empty.");
            }

            CertificateAttributes certificateAttributes = new CertificateAttributes()
                .setEnabled(certificateProperties.isEnabled())
                .setExpires(certificateProperties.getExpiresOn())
                .setNotBefore(certificateProperties.getNotBefore());

            CertificateUpdateParameters certificateUpdateParameters = new CertificateUpdateParameters()
                .setCertificateAttributes(certificateAttributes)
                .setTags(certificateProperties.getTags());

            return createCertificateWithPolicy(
                clientImpl.updateCertificate(certificateProperties.getName(), certificateProperties.getVersion(),
                    certificateUpdateParameters));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Updates the attributes associated with the specified certificate, but not the associated key material in the key
     * vault. Certificate attributes that are not specified in the request are left unchanged. This operation requires
     * the {@code certificates/set} permission.
     *
     * <p>The {@code certificateProperties} parameter and its {@link CertificateProperties#getName() name} value are
     * required.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets latest version of a certificate and updates its tags and enabled status in the key vault, then prints out
     * the updated certificate's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificatePropertiesWithResponse#CertificateProperties-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificatePropertiesWithResponse#CertificateProperties-RequestOptions -->
     *
     * @param certificateProperties The certificate properties to update.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue()} contains the updated certificate.
     *
     * @throws HttpResponseException If a certificate with the given {@link CertificateProperties#getName() name} and
     * {@link CertificateProperties#getVersion() version} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@link CertificateProperties#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code certificateProperties} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultCertificate> updateCertificatePropertiesWithResponse(
        CertificateProperties certificateProperties, RequestOptions requestOptions) {

        try {
            Objects.requireNonNull(certificateProperties,"'certificateProperties' cannot be null.");

            if (isNullOrEmpty(certificateProperties.getName())) {
                throw new IllegalArgumentException("'certificateProperties.getName()' cannot be null or empty.");
            }

            CertificateAttributes certificateAttributes = new CertificateAttributes()
                .setEnabled(certificateProperties.isEnabled())
                .setExpires(certificateProperties.getExpiresOn())
                .setNotBefore(certificateProperties.getNotBefore());

            CertificateUpdateParameters certificateUpdateParameters = new CertificateUpdateParameters()
                .setCertificateAttributes(certificateAttributes)
                .setTags(certificateProperties.getTags());

            return mapResponse(clientImpl.updateCertificateWithResponse(certificateProperties.getName(), certificateProperties.getVersion(),
                    certificateUpdateParameters, requestOptions),
                KeyVaultCertificateWithPolicyHelper::createCertificateWithPolicy);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Deletes a certificate from the key vault. If soft-delete is enabled on the key vault then the certificate is
     * placed in the deleted state and requires to be purged for permanent deletion. Otherwise, the certificate is
     * permanently deleted. All versions of a certificate are deleted. This cannot be applied to individual versions of
     * a certificate. This operation requires the {@code certificates/delete} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes a certificate from the key vault and prints out its recovery id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.beginDeleteCertificate#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.beginDeleteCertificate#String -->
     *
     * @param name The name of the certificate to be deleted.
     * @throws HttpResponseException If a certificate with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     * @return A {@link Poller} to poll on and retrieve the deleted certificate with.
     */
    // TODO (vcolin7): Uncomment when creating a Poller is supported in azure-core-v2.
    /*@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<DeletedCertificate, Void> beginDeleteCertificate(String name) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return Poller.createPoller(Duration.ofSeconds(1),
                pollingContext -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                    createDeletedCertificate(clientImpl.deleteCertificate(name))),
                pollingContext -> deleteCertificatePollOperation(name, pollingContext),
                (pollingContext, pollResponse) -> null,
                pollingContext -> null);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }*/

    private PollResponse<DeletedCertificate> deleteCertificatePollOperation(String name,
        PollingContext<DeletedCertificate> pollingContext) {

        try {
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                createDeletedCertificate(clientImpl.getDeletedCertificate(name)));
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                    pollingContext.getLatestResponse().getValue());
            } else {
                // This means either vault has soft-delete disabled or permission is not granted for the get deleted
                // certificate operation. In both cases deletion operation was successful when activation operation
                // succeeded before reaching here.
                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    pollingContext.getLatestResponse().getValue());
            }
        } catch (RuntimeException e) {
            // This means either vault has soft-delete disabled or permission is not granted for the get deleted
            // certificate operation. In both cases deletion operation was successful when activation operation
            // succeeded before reaching here.
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                pollingContext.getLatestResponse().getValue());
        }
    }

    /**
     * Gets information about a deleted certificate. This operation is applicable for soft-delete enabled vaults and
     * requires the {@code certificates/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a deleted certificate from the key vault enabled for soft-delete and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getDeletedCertificate#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getDeletedCertificate#String -->
     *
     * @param name The name of the deleted certificate to retrieve. It is required and cannot be {@code null} or empty.
     * @return The requested deleted certificate.
     *
     * @throws HttpResponseException If a certificate with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DeletedCertificate getDeletedCertificate(String name) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return createDeletedCertificate(clientImpl.getDeletedCertificate(name));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets information about a deleted certificate. This operation is applicable for soft-delete enabled vaults and
     * requires the {@code certificates/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a deleted certificate from the key vault enabled for soft-delete. Prints details of the response returned
     * by the service and the deleted certificate.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getDeletedCertificateWithResponse#String-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getDeletedCertificateWithResponse#String-RequestOptions -->
     *
     * @param name The name of the deleted certificate to retrieve. It is required and cannot be {@code null} or empty.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue()} contains the deleted certificate.
     *
     * @throws HttpResponseException If a certificate with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DeletedCertificate> getDeletedCertificateWithResponse(String name, RequestOptions requestOptions) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return mapResponse(clientImpl.getDeletedCertificateWithResponse(name, requestOptions),
                DeletedCertificateHelper::createDeletedCertificate);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Permanently removes a deleted certificate without the possibility of recovery. This operation can only be
     * performed on a key vault <b>enabled for soft-delete</b> and requires the {@code certificates/purge} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Purges a deleted certificate from a key vault enabled for soft-delete.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.purgeDeletedCertificate#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.purgeDeletedCertificate#String -->
     *
     * @param name The name of the deleted certificate to purge. It is required and cannot be {@code null} or empty.
     *
     * @throws HttpResponseException If a certificate with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void purgeDeletedCertificate(String name) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            clientImpl.purgeDeletedCertificate(name);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Permanently removes a deleted certificate without the possibility of recovery. This operation can only be
     * performed on a key vault <b>enabled for soft-delete</b> and requires the {@code certificates/purge} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Purges a deleted certificate from a key vault enabled for soft-delete and prints out details of the response
     * returned by the service.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.purgeDeletedCertificateWithResponse#String-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.purgeDeletedCertificateWithResponse#String-RequestOptions -->
     *
     * @param name The name of the deleted certificate to purge. It is required and cannot be {@code null} or empty.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object containing the status code and HTTP headers related to the operation.
     *
     * @throws HttpResponseException If a certificate with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> purgeDeletedCertificateWithResponse(String name, RequestOptions requestOptions) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return clientImpl.purgeDeletedCertificateWithResponse(name, requestOptions);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Recovers a deleted certificate back to its latest version. This operation can only be performed on a key vault
     * <b>enabled for soft-delete</b> and requires the {@code certificates/recover} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recovers a deleted certificate from a key vault enabled for soft-delete and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.beginRecoverDeletedCertificate#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.beginRecoverDeletedCertificate#String -->
     *
     * @param name The name of the deleted certificate to recover. It is required and cannot be {@code null} or empty.
     * @throws HttpResponseException If a certificate with the given {@code name} doesn't exist in the
     * certificate vault.
     * @return A {@link Poller} to poll on and retrieve the recovered certificate with.
     */
    // TODO (vcolin7): Uncomment when creating a Poller is supported in azure-core-v2.
    /*@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<KeyVaultCertificateWithPolicy, Void> beginRecoverDeletedCertificate(String name) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return Poller.createPoller(Duration.ofSeconds(1),
                pollingContext -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                    createCertificateWithPolicy(clientImpl.recoverDeletedCertificate(name))),
                pollingContext -> recoverDeletedCertificatePollOperation(name, pollingContext),
                (pollingContext, firstResponse) -> null,
                pollingContext -> null);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }*/

    private PollResponse<KeyVaultCertificateWithPolicy> recoverDeletedCertificatePollOperation(String certificateName,
        PollingContext<KeyVaultCertificateWithPolicy> pollingContext) {

        try {
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                createCertificateWithPolicy(clientImpl.getCertificate(certificateName, "")));
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() == 404) {
                return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                    pollingContext.getLatestResponse().getValue());
            } else {
                // This means permission is not granted for the get deleted certificate operation.
                // In both cases deletion operation was successful when activation operation succeeded before
                // reaching here.
                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    pollingContext.getLatestResponse().getValue());
            }
        } catch (RuntimeException e) {
            // This means permission is not granted for the get deleted certificate operation.
            // In both cases deletion operation was successful when activation operation succeeded before reaching
            // here.
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                pollingContext.getLatestResponse().getValue());
        }
    }

    /**
     * Requests a backup of the certificate be downloaded. All versions of the certificate will be downloaded. This
     * operation requires the {@code certificates/backup} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Backs up a certificate from the key vault and prints out the length of the certificate's backup blob.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.backupCertificate#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.backupCertificate#String -->
     *
     * @param name The name of the certificate to back up.
     * @return A byte array containing the backed up certificate blob.
     *
     * @throws HttpResponseException If a certificate with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public byte[] backupCertificate(String name) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return clientImpl.backupCertificate(name).getValue();
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Requests a backup of the certificate be downloaded. All versions of the certificate will be downloaded. This
     * operation requires the {@code certificates/backup} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Backs up a certificate from the key vault. Prints out details of the response returned by the service and the
     * length of the certificate's backup blob.
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.backupCertificateWithResponse#String-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.backupCertificateWithResponse#String-RequestOptions -->
     *
     * @param name The name of the certificate to back up.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue()} contains the backed up certificate blob.
     *
     * @throws HttpResponseException If a certificate with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<byte[]> backupCertificateWithResponse(String name, RequestOptions requestOptions) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return mapResponse(clientImpl.backupCertificateWithResponse(name, requestOptions),
                BackupCertificateResult::getValue);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Restores a backed up certificate and all its versions to a vault. All versions of the certificate are restored to
     * the vault. This operation requires the {@code certificates/restore} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Restores a certificate in the key vault from a backup and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.restoreCertificate#byte -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.restoreCertificate#byte -->
     *
     * @param backup The backup blob associated with the certificate.
     * @return The restored certificate.
     *
     * @throws HttpResponseException If the {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultCertificateWithPolicy restoreCertificateBackup(byte[] backup) {
        try {
            return createCertificateWithPolicy(clientImpl.restoreCertificate(new CertificateRestoreParameters(backup)));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Restores a backed up certificate and all its versions to a vault. All versions of the certificate are restored to
     * the vault. This operation requires the {@code certificates/restore} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Restores a certificate in the key vault from a backup. Prints our details of the response returned by the
     * service and the restored certificate.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.restoreCertificateWithResponse#byte-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.restoreCertificateWithResponse#byte-RequestOptions -->
     *
     * @param backup The backup blob associated with the certificate.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue()} contains the restored certificate.
     *
     * @throws HttpResponseException If the {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultCertificateWithPolicy> restoreCertificateBackupWithResponse(byte[] backup,
        RequestOptions requestOptions) {

        try {
            return mapResponse(
                clientImpl.restoreCertificateWithResponse(new CertificateRestoreParameters(backup), requestOptions),
                KeyVaultCertificateWithPolicyHelper::createCertificateWithPolicy);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Lists all certificates in the key vault. Each certificate is represented by a properties object containing the
     * certificate identifier, thumbprint, and attributes. The policy and individual versions are not included in the
     * response. This operation requires the {@code certificates/list} permission.
     *
     * <p><strong>Iterate through certificates</strong></p>
     * <p>Lists the certificates in the key vault and gets each one's latest version and their policies by looping
     * though the properties objects and calling {@link CertificateClient#getCertificate(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificates -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificates -->
     *
     * <p><strong>Iterate through certificates by page</strong></p>
     * <p>Iterates through the certificates in the key vault by page and gets each one's latest version and their
     * policies by looping though the properties objects and calling {@link CertificateClient#getCertificate(String)}.
     * </p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificates.iterableByPage -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificates.iterableByPage -->
     *
     * @return A {@link PagedIterable} of properties objects of all the certificates in the vault. A properties object
     * contains all information about the certificate, except its key material.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateProperties> listPropertiesOfCertificates() {
        return listPropertiesOfCertificates(false, RequestOptions.none());
    }

    /**
     * Lists all certificates in the key vault. Each certificate is represented by a properties object containing the
     * certificate identifier, thumbprint, and attributes. The policy and individual versions are not included in the
     * response. This operation requires the {@code certificates/list} permission.
     *
     * <p><strong>Iterate through certificates</strong></p>
     * <p>Lists the certificates in the key vault and gets each one's latest version and their policies by looping
     * though the properties objects and calling {@link CertificateClient#getCertificate(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificates#boolean-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificates#RequestOptions -->
     *
     * <p><strong>Iterate through certificates by page</strong></p>
     * <p>Iterates through the certificates in the key vault by page and gets each one's latest version and their
     * policies by looping though the properties objects and calling {@link CertificateClient#getCertificate(String)}.
     * </p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificates.iterableByPage#boolean-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificates.iterableByPage#boolean-RequestOptions -->
     *
     * @param includePending Indicate if pending certificates should be included in the results.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} of properties objects of all the certificates in the vault. A properties object
     * contains all information about the certificate, except its key material.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateProperties> listPropertiesOfCertificates(boolean includePending,
        RequestOptions requestOptions) {

        try {
            RequestOptions requestOptionsForNextPage = new RequestOptions();

            requestOptionsForNextPage.setContext(requestOptions != null && requestOptions.getContext() != null
                ? requestOptions.getContext()
                : Context.none());

            return mapPages(pagingOptions -> clientImpl.getCertificatesSinglePage(null, includePending),
                (pagingOptions, nextLink) -> clientImpl.getCertificatesNextSinglePage(nextLink,
                    requestOptionsForNextPage), CertificatePropertiesHelper::createCertificateProperties);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Lists all deleted certificates in the key vault currently available for recovery. This operation is applicable
     * for key vaults <b>enabled for soft-delete</b> and requires the {@code certificates/list} permission.
     *
     * <p><strong>Iterate through deleted certificates</strong></p>
     * <p>Lists the deleted certificates in a key vault enabled for soft-delete and prints out each one's recovery id.
     * </p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates -->
     *
     * <p><strong>Iterate through deleted certificates by page</strong></p>
     * <p>Iterates through the deleted certificates by page in the key vault by page and prints out each one's recovery
     * id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates.iterableByPage -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates.iterableByPage -->
     *
     * @return A {@link PagedIterable} of the deleted certificates in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedCertificate> listDeletedCertificates() {
        return listDeletedCertificates(false, RequestOptions.none());
    }

    /**
     * Lists all deleted certificates in the key vault currently available for recovery. This operation is applicable
     * for key vaults <b>enabled for soft-delete</b> and requires the {@code certificates/list} permission.
     *
     * <p><strong>Iterate through deleted certificates</strong></p>
     * <p>Lists the deleted certificates in a key vault enabled for soft-delete and prints out each one's recovery id.
     * </p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates#RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates#RequestOptions -->
     *
     * <p><strong>Iterate through deleted certificates by page</strong></p>
     * <p>Iterates through the deleted certificates by page in the key vault by page and prints out each one's recovery
     * id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates.iterableByPage#RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates.iterableByPage#RequestOptions -->
     *
     * @param includePending Indicate if pending certificates should be included in the results.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} of the deleted certificates in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedCertificate> listDeletedCertificates(boolean includePending,
        RequestOptions requestOptions) {

        try {
            RequestOptions requestOptionsForNextPage = new RequestOptions();

            requestOptionsForNextPage.setContext(requestOptions != null && requestOptions.getContext() != null
                ? requestOptions.getContext()
                : Context.none());

            return mapPages(pagingOptions -> clientImpl.getDeletedCertificatesSinglePage(null, includePending),
                (pagingOptions, nextLink) -> clientImpl.getDeletedCertificatesNextSinglePage(nextLink,
                    requestOptionsForNextPage), DeletedCertificateHelper::createDeletedCertificate);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Lists all versions of the specified certificate in the key vault. Each certificate is represented by a properties
     * object containing the certificate identifier, thumbprint, and attributes. The policy and individual versions are
     * not included in the response. This operation requires the {@code certificates/list} permission.
     *
     * <p><strong>Iterate through certificates versions</strong></p>
     * <p>Lists the versions of a certificate in the key vault and gets each one's latest version and their policies by
     * looping though the properties objects and calling {@link CertificateClient#getCertificate(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificateVersions#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificateVersions#String -->
     *
     * <p><strong>Iterate through certificates versions by page</strong></p>
     * <p>Iterates through the versions of a certificate in the key vault by page and gets each one's latest version and
     * their policies by looping though the properties objects and calling
     * {@link CertificateClient#getCertificate(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificateVersions.iterableByPage#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificateVersions.iterableByPage#String -->
     *
     * @param name The name of the certificate. It is required and cannot be {@code null} or empty.
     * @return A {@link PagedIterable} of properties objects of all the versions of the specified certificate. A
     * properties object contains all information about the certificate, except its key material. The
     * {@link PagedIterable} will be empty if no certificate with the given {@code name} exists in key vault.
     *
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateProperties> listPropertiesOfCertificateVersions(String name) {
        return listPropertiesOfCertificateVersions(name, RequestOptions.none());
    }

    /**
     * Lists all versions of the specified certificate in the key vault. Each certificate is represented by a properties
     * object containing the certificate identifier, thumbprint, and attributes. The policy and individual versions are
     * not included in the response. This operation requires the {@code certificates/list} permission.
     *
     * <p><strong>Iterate through certificates versions</strong></p>
     * <p>Lists the versions of a certificate in the key vault and gets each one's latest version and their policies by
     * looping though the properties objects and calling {@link CertificateClient#getCertificate(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificateVersions#String-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificateVersions#String-RequestOptions -->
     *
     * <p><strong>Iterate through certificates versions by page</strong></p>
     * <p>Iterates through the versions of a certificate in the key vault by page and gets each one's latest version and
     * their policies by looping though the properties objects and calling
     * {@link CertificateClient#getCertificate(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificateVersions.iterableByPage#String-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificateVersions.iterableByPage#String-RequestOptions -->
     *
     * @param name The name of the certificate. It is required and cannot be {@code null} or empty.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} of properties objects of all the versions of the specified certificate. A
     * properties object contains all information about the certificate, except its key material. The
     * {@link PagedIterable} will be empty if no certificate with the given {@code name} exists in key vault.
     *
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateProperties> listPropertiesOfCertificateVersions(String name,
        RequestOptions requestOptions) {

        try {
            RequestOptions requestOptionsForNextPage = new RequestOptions();

            requestOptionsForNextPage.setContext(requestOptions != null && requestOptions.getContext() != null
                ? requestOptions.getContext()
                : Context.none());

            return mapPages(pagingOptions -> clientImpl.getCertificateVersionsSinglePage(name, null),
                (pagingOptions, nextLink) -> clientImpl.getCertificateVersionsNextSinglePage(nextLink,
                    requestOptionsForNextPage), CertificatePropertiesHelper::createCertificateProperties);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Retrieves the policy of a certificate in the key vault. This operation requires the {@code certificates/get}
     * permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the policy of a certificate in the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificatePolicy#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificatePolicy#String -->
     *
     * @param certificateName The name of the certificate whose policy is to be retrieved. It is required and cannot be
     * {@code null} or empty.
     * @return The requested certificate policy.
     *
     * @throws HttpResponseException If a certificate with the given {@code certificateName} doesn't exist in the key
     * vault.
     * @throws IllegalArgumentException If the provided {@code certificateName} is {@code null} or an empty
     * string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificatePolicy getCertificatePolicy(String certificateName) {
        try {
            if (isNullOrEmpty(certificateName)) {
                throw new IllegalArgumentException("'certificateName' cannot be null or empty.");
            }

            return createCertificatePolicy(clientImpl.getCertificatePolicy(certificateName));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Retrieves the policy of a certificate in the key vault. This operation requires the {@code certificates/get}
     * permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the policy of a certificate in the key vault. Prints out details of the response returned by the service
     * and the requested certificate policy.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificatePolicyWithResponse#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificatePolicyWithResponse#String -->
     *
     * @param certificateName The name of the certificate whose policy is to be retrieved. It is required and cannot be
     * {@code null} or empty.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue()} contains the requested certificate policy.
     *
     * @throws HttpResponseException If a certificate with the given {@code certificateName} doesn't exist in the key
     * vault.
     * @throws IllegalArgumentException If the provided {@code certificateName} is {@code null} or an empty
     * string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificatePolicy> getCertificatePolicyWithResponse(String certificateName,
        RequestOptions requestOptions) {

        try {
            if (isNullOrEmpty(certificateName)) {
                throw new IllegalArgumentException("'certificateName' cannot be null or empty.");
            }

            return mapResponse(clientImpl.getCertificatePolicyWithResponse(certificateName, requestOptions),
                CertificatePolicyHelper::createCertificatePolicy);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Updates the policy for a certificate. Policy attributes that are not specified in the request are left unchanged.
     * This operation requires the {@code certificates/update} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the certificate policy and updates its properties in the key vault, then prints out the updated policy's
     * details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificatePolicy#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificatePolicy#String -->
     *
     * @param certificateName The name of the certificate whose policy is to be updated. It is required and cannot be
     * {@code null} or empty.
     * @param policy The certificate policy to be updated. It is required and cannot be {@code null}.
     * @return The updated certificate policy.
     *
     * @throws HttpResponseException If a certificate with the given {@code certificateName} doesn't exist in the key
     * vault.
     * @throws IllegalArgumentException If the provided {@code certificateName} is {@code null} or an empty
     * string.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificatePolicy updateCertificatePolicy(String certificateName, CertificatePolicy policy) {
        try {
            if (isNullOrEmpty(certificateName)) {
                throw new IllegalArgumentException("'certificateName' cannot be null or empty.");
            }

            Objects.requireNonNull(policy, "'policy' cannot be null.");

            return createCertificatePolicy(
                clientImpl.updateCertificatePolicy(certificateName, getImplCertificatePolicy(policy)));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Updates the policy for a certificate. Policy attributes that are not specified in the request are left unchanged.
     * This operation requires the {@code certificates/update} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the certificate policy and updates its properties in the key vault. Prints out details of the response
     * returned by the service and the updated policy.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificatePolicyWithResponse#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificatePolicyWithResponse#String -->
     *
     * @param certificateName The name of the certificate whose policy is to be updated. It is required and cannot be
     * {@code null} or empty.
     * @param policy The certificate policy to be updated. It is required and cannot be {@code null}.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue()} contains the updated certificate policy.
     *
     * @throws HttpResponseException If a certificate with the given {@code certificateName} doesn't exist in the key
     * vault.
     * @throws IllegalArgumentException If the provided {@code certificateName} is {@code null} or an empty
     * string.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificatePolicy> updateCertificatePolicyWithResponse(String certificateName,
        CertificatePolicy policy, RequestOptions requestOptions) {

        try {
            if (isNullOrEmpty(certificateName)) {
                throw new IllegalArgumentException("'certificateName' cannot be null or empty.");
            }

            Objects.requireNonNull(policy, "'policy' cannot be null.");

            return mapResponse(
                clientImpl.updateCertificatePolicyWithResponse(certificateName, getImplCertificatePolicy(policy),
                    requestOptions), CertificatePolicyHelper::createCertificatePolicy);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Creates a certificate issuer. This operation updates the specified certificate issuer if it already exists and
     * requires the {@code certificates/setissuers} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new certificate issuer in the key vault and prints out the created issuer's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.createIssuer#CertificateIssuer -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.createIssuer#CertificateIssuer -->
     *
     * @param issuer The configuration of the certificate issuer to be created. It is required and cannot be
     * {@code null}.
     * @return The created certificate issuer.
     *
     * @throws HttpResponseException If a certificate with the given {@code certificateName} doesn't exist in the key
     * vault.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificateIssuer createIssuer(CertificateIssuer issuer) {
        try {
            Objects.requireNonNull(issuer, "'issuer' cannot be null.");

            IssuerBundle issuerBundle = getIssuerBundle(issuer);
            CertificateIssuerSetParameters certificateIssuerSetParameters =
                new CertificateIssuerSetParameters(issuerBundle.getProvider())
                    .setOrganizationDetails(issuerBundle.getOrganizationDetails())
                    .setCredentials(issuerBundle.getCredentials())
                    .setAttributes(issuerBundle.getAttributes());

            return createCertificateIssuer(
                clientImpl.setCertificateIssuer(issuer.getName(), certificateIssuerSetParameters));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Creates a certificate issuer. This operation updates the specified certificate issuer if it already exists and
     * requires the {@code certificates/setissuers} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new certificate issuer in the key vault. Prints out details of the response returned by the
     * service and the created issuer.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.createIssuerWithResponse#CertificateIssuer-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.createIssuerWithResponse#CertificateIssuer-RequestOptions -->
     *
     * @param issuer The configuration of the certificate issuer to be created. It is required and cannot be
     * {@code null}.
     * @return A response object whose {@link Response#getValue()} contains the created certificate issuer.
     *
     * @throws HttpResponseException If a certificate with the given {@code certificateName} doesn't exist in the key
     * vault.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificateIssuer> createIssuerWithResponse(CertificateIssuer issuer,
        RequestOptions requestOptions) {

        try {
            Objects.requireNonNull(issuer, "'issuer' cannot be null.");

            IssuerBundle issuerBundle = getIssuerBundle(issuer);
            CertificateIssuerSetParameters certificateIssuerSetParameters =
                new CertificateIssuerSetParameters(issuerBundle.getProvider())
                    .setOrganizationDetails(issuerBundle.getOrganizationDetails())
                    .setCredentials(issuerBundle.getCredentials())
                    .setAttributes(issuerBundle.getAttributes());

            return mapResponse(
                clientImpl.setCertificateIssuerWithResponse(issuer.getName(), certificateIssuerSetParameters,
                    requestOptions), CertificateIssuerHelper::createCertificateIssuer);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Retrieves a certificate issuer from the key vault. This operation requires the
     * {@code certificates/manageissuers/getissuers} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a specific certificate issuer in the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getIssuer#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getIssuer#String -->
     *
     * @param name The name of the certificate issuer to retrieve. It is required and cannot be {@code null} or empty.
     * @return The requested certificate issuer.
     *
     * @throws HttpResponseException If a certificate issuer with the given {@code name} doesn't exist in the key
     * vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificateIssuer getIssuer(String name) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return createCertificateIssuer(clientImpl.getCertificateIssuer(name));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Retrieves a certificate issuer from the key vault. This operation requires the
     * {@code certificates/manageissuers/getissuers} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a specific certificate issuer in the key vault. Prints out details of the response returned by the
     * service and the requested certificate issuer.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getIssuerWithResponse#String-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getIssuer#String-RequestOptions -->
     *
     * @param name The name of the certificate issuer to retrieve. It is required and cannot be {@code null} or empty.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue()} contains the requested certificate issuer.
     *
     * @throws HttpResponseException If a certificate issuer with the given {@code name} doesn't exist in the key
     * vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificateIssuer> getIssuerWithResponse(String name, RequestOptions requestOptions) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return mapResponse(clientImpl.getCertificateIssuerWithResponse(name, requestOptions),
                CertificateIssuerHelper::createCertificateIssuer);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Deletes a certificate issuer from the key vault. This operation requires the
     * {@code certificates/manageissuers/deleteissuers} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes the certificate issuer in the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.deleteIssuer#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.deleteIssuer#String -->
     *
     * @param name The name of the certificate issuer to be deleted. It is required and cannot be {@code null} or empty.
     * @return The deleted certificate issuer.
     *
     * @throws HttpResponseException If a certificate issuer with the given {@code name} doesn't exist in the key
     * vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificateIssuer deleteIssuer(String name) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return createCertificateIssuer(clientImpl.deleteCertificateIssuer(name));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Deletes a certificate issuer from the key vault. This operation requires the
     * {@code certificates/manageissuers/deleteissuers} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes the certificate issuer in the key vault. Prints out details of the response returned by the
     * service and the deleted certificate issuer.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.deleteIssuer#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.deleteIssuer#String -->
     *
     * @param name The name of the certificate issuer to be deleted. It is required and cannot be {@code null} or empty.
     * @return The deleted certificate issuer.
     *
     * @throws HttpResponseException If a certificate issuer with the given {@code name} doesn't exist in the key
     * vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificateIssuer> deleteIssuerWithResponse(String name, RequestOptions requestOptions) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return mapResponse(clientImpl.deleteCertificateIssuerWithResponse(name, requestOptions),
                CertificateIssuerHelper::createCertificateIssuer);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Lists all the certificate issuers in the key vault. Each issuer is represented by a properties object containing
     * the certificate issuer identifier and provider. This operation requires the
     * {@code certificates/manageissuers/getissuers} permission.
     *
     * <p>><strong>Iterate through certificate issuers</strong></p>
     * <p>Lists the certificate issuers in the key vault and prints out each issuer's name and provider.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers -->
     *
     * <p><strong>Iterate through certificate issuers by page</strong></p>
     * <p>Iterates through the certificate issuers in the key vault by page and prints out each issuer's name and
     * provider.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers.iterableByPage -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers.iterableByPage -->
     *
     * @return A {@link PagedIterable} of properties objects of all the certificate issuers in the vault. A properties
     * object contains the issuer identifier and provider.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<IssuerProperties> listPropertiesOfIssuers() {
        return listPropertiesOfIssuers(RequestOptions.none());
    }

    /**
     * Lists all the certificate issuers in the key vault. Each issuer is represented by a properties object containing
     * the certificate issuer identifier and provider. This operation requires the
     * {@code certificates/manageissuers/getissuers} permission.
     *
     * <p>><strong>Iterate through certificate issuers</strong></p>
     * <p>Lists the certificate issuers in the key vault and gets their details by looping though the properties objects
     * and calling {@link CertificateClient#getIssuer(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers#RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers#RequestOptions -->
     *
     * <p><strong>Iterate through certificate issuers by page</strong></p>
     * <p>Iterates through the certificate issuers in the key vault by page and gets their details by looping though the
     * properties objects and calling {@link CertificateClient#getIssuer(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers.iterableByPage#RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers.iterableByPage#RequestOptions -->
     *
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} of properties objects of all the certificate issuers in the vault. A properties
     * object contains the issuer identifier and provider.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<IssuerProperties> listPropertiesOfIssuers(RequestOptions requestOptions) {
        try {
            RequestOptions requestOptionsForNextPage = new RequestOptions();

            requestOptionsForNextPage.setContext(requestOptions != null && requestOptions.getContext() != null
                ? requestOptions.getContext()
                : Context.none());

            return mapPages(pagingOptions -> clientImpl.getCertificateIssuersSinglePage(null),
                (pagingOptions, nextLink) -> clientImpl.getCertificateIssuersNextSinglePage(nextLink,
                    requestOptionsForNextPage), IssuerPropertiesHelper::createIssuerProperties);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Updates a certificate issuer. Only attributes populated in {@code issuer} are changed. Attributes not specified
     * in the request are not changed. This operation requires the certificates/setissuers permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the latest version of a certificate issuer and updates it in the key vault, then prints out the updated
     * issuer's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.updateIssuer#CertificateIssuer -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.updateIssuer#CertificateIssuer -->
     *
     * @param issuer The issuer with updated properties.
     * @return The updated issuer.
     *
     * @throws HttpResponseException If a certificate issuer with the given {@link CertificateIssuer#getName()} doesn't
     * exist in the key vault.
     * @throws IllegalArgumentException If {@link CertificateIssuer#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code issuer} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificateIssuer updateIssuer(CertificateIssuer issuer) {
        try {
            Objects.requireNonNull(issuer, "'issuer' cannot be null.");

            if (isNullOrEmpty(issuer.getName())) {
                throw new IllegalArgumentException("'issuer.getName()' cannot be null or empty.");
            }

            IssuerBundle issuerBundle = getIssuerBundle(issuer);
            CertificateIssuerUpdateParameters certificateIssuerUpdateParameters =
                new CertificateIssuerUpdateParameters()
                    .setProvider(issuerBundle.getProvider())
                    .setOrganizationDetails(issuerBundle.getOrganizationDetails())
                    .setCredentials(issuerBundle.getCredentials())
                    .setAttributes(issuerBundle.getAttributes());

            return createCertificateIssuer(
                clientImpl.updateCertificateIssuer(issuer.getName(), certificateIssuerUpdateParameters));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Updates a certificate issuer. Only attributes populated in {@code issuer} are changed. Attributes not specified
     * in the request are not changed. This operation requires the certificates/setissuers permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the latest version of a certificate issuer and updates it in the key vault. Prints out details of the
     * response returned by the service and the updated issuer.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.updateIssuer#CertificateIssuer-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.updateIssuer#CertificateIssuer-RequestOptions -->
     *
     * @param issuer The issuer with updated properties.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return The updated issuer.
     *
     * @throws HttpResponseException If a certificate issuer with the given {@link CertificateIssuer#getName()} doesn't
     * exist in the key vault.
     * @throws IllegalArgumentException If {@link CertificateIssuer#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code issuer} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificateIssuer> updateIssuerWithResponse(CertificateIssuer issuer,
        RequestOptions requestOptions) {

        try {
            Objects.requireNonNull(issuer, "'issuer' cannot be null.");

            if (isNullOrEmpty(issuer.getName())) {
                throw new IllegalArgumentException("'issuer.getName()' cannot be null or empty.");
            }

            IssuerBundle issuerBundle = getIssuerBundle(issuer);
            CertificateIssuerUpdateParameters certificateIssuerUpdateParameters =
                new CertificateIssuerUpdateParameters()
                    .setProvider(issuerBundle.getProvider())
                    .setOrganizationDetails(issuerBundle.getOrganizationDetails())
                    .setCredentials(issuerBundle.getCredentials())
                    .setAttributes(issuerBundle.getAttributes());

            return mapResponse(
                clientImpl.updateCertificateIssuerWithResponse(issuer.getName(), certificateIssuerUpdateParameters,
                    requestOptions), CertificateIssuerHelper::createCertificateIssuer);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Sets a list of certificate contacts on the key vault. This operation requires the
     * {@code certificates/managecontacts} permission.
     *
     * <p>The {@link LifetimeAction} of type {@link CertificatePolicyAction#EMAIL_CONTACTS} set on a
     * {@link CertificatePolicy} emails the contacts set on the vault when triggered.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Sets certificate contacts in the key vault and prints out each one's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.setContacts#List -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.setContacts#List -->
     *
     * @param contacts The list of contacts to set on the vault.
     * @return A {@link PagedIterable} containing all the certificate contacts in the vault.
     *
     * @throws HttpResponseException If the provided contact information is malformed.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateContact> setContacts(List<CertificateContact> contacts) {
        return setContacts(contacts, RequestOptions.none());
    }

    /**
     * Sets a list of certificate contacts on the key vault. This operation requires the
     * {@code certificates/managecontacts} permission.
     *
     * <p>The {@link LifetimeAction} of type {@link CertificatePolicyAction#EMAIL_CONTACTS} set on a
     * {@link CertificatePolicy} emails the contacts set on the vault when triggered.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Sets certificate contacts in the key vault and prints out each one's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.setContacts#List-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.setContacts#List-RequestOptions -->
     *
     * @param contacts The list of contacts to set on the vault.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} containing all the certificate contacts in the vault.
     *
     * @throws HttpResponseException If the provided contact information is malformed.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateContact> setContacts(List<CertificateContact> contacts,
        RequestOptions requestOptions) {

        try {
            return new PagedIterable<>((pagingOptions) -> mapContactsToPagedResponse(
                clientImpl.setCertificateContactsWithResponse(new Contacts().setContactList(contacts),
                    requestOptions)));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Lists all the certificate contacts in the key vault. This operation requires the
     * {@code certificates/managecontacts} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Lists the certificate contacts in the key vault and prints out each one's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listContacts -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listContacts -->
     *
     * @return A {@link PagedIterable} containing all the certificate contacts in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateContact> listContacts() {
        return listContacts(RequestOptions.none());
    }

    /**
     * Lists all the certificate contacts in the key vault. This operation requires the
     * {@code certificates/managecontacts} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Lists the certificate contacts in the key vault and prints out each one's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listContacts#RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listContacts#RequestOptions -->
     *
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} containing all the certificate contacts in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateContact> listContacts(RequestOptions requestOptions) {
        try {
            return new PagedIterable<>((pagingOptions) -> mapContactsToPagedResponse(
                clientImpl.getCertificateContactsWithResponse(requestOptions)));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Deletes all the certificate contacts in the key vault. This operation requires the
     * {@code certificates/managecontacts} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes the certificate contacts in the key vault and prints out each one's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.deleteContacts -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.deleteContacts -->
     *
     * @return A {@link PagedIterable} containing the freshly deleted certificate contacts.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateContact> deleteContacts() {
        return deleteContacts(RequestOptions.none());
    }

    /**
     * Deletes all the certificate contacts in the key vault. This operation requires the
     * {@code certificates/managecontacts} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes the certificate contacts in the key vault and prints out each one's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.deleteContacts-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.deleteContacts-RequestOptions -->
     *
     * @return A {@link PagedIterable} containing the freshly deleted certificate contacts.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateContact> deleteContacts(RequestOptions requestOptions) {
        try {
            return new PagedIterable<>((pagingOptions) -> mapContactsToPagedResponse(
                clientImpl.deleteCertificateContactsWithResponse(requestOptions)));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets information on a pending operation from the key vault. This operation requires the {@code certificates/get}
     * permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a pending certificate operation and prints out its status.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateOperation#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateOperation#String -->
     *
     * @param certificateName The name of the certificate the operation pertains to. It is required and cannot be
     * {@code null} or empty.
     * @return A {@link Poller} object to poll on and retrieve the operation status with.
     *
     * @throws HttpResponseException If an operation for a certificate with the given {@code certificateName} doesn't
     * exist.
     * @throws IllegalArgumentException If the provided {@code certificateName} is {@code null} or an empty string.
     */
    // TODO (vcolin7): Uncomment when creating a Poller is supported in azure-core-v2.
    /*@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<CertificateOperation, KeyVaultCertificateWithPolicy> getCertificateOperation(String certificateName) {
        try {
            if (isNullOrEmpty(certificateName)) {
                throw new IllegalArgumentException("'certificateName' cannot be null or empty.");
            }

            return Poller.createPoller(Duration.ofSeconds(1),
                pollingContext -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null),
                pollingContext -> certificatePollOperation(certificateName),
                (pollingContext, pollResponse) -> certificateCancellationOperation(certificateName),
                pollingContext -> fetchCertificateOperation(certificateName));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }*/

    /**
     * Deletes the creation operation for the specified certificate that is in the process of being created. The
     * certificate will not be created. This operation requires the {@code certificates/update} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Triggers certificate creation and then deletes the certificate creation operation in the key vault. Prints out
     * the deleted certificate operation details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.deleteCertificateOperation#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.deleteCertificateOperation#String -->
     *
     * @param certificateName The name of the certificate. It is required and cannot be {@code null} or empty.
     * @return The deleted certificate operation.
     *
     * @throws HttpResponseException If a certificate operation for a certificate with {@code certificateName}
     * doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code certificateName} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificateOperation deleteCertificateOperation(String certificateName) {
        try {
            if (isNullOrEmpty(certificateName)) {
                throw new IllegalArgumentException("'certificateName' cannot be null or empty.");
            }

            return createCertificateOperation(clientImpl.deleteCertificateOperation(certificateName));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Deletes the creation operation for the specified certificate that is in the process of being created. The
     * certificate will not be created. This operation requires the {@code certificates/update} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Triggers certificate creation and then deletes the certificate creation operation in the key vault. Prints out
     * the deleted certificate operation details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.deleteCertificateOperation#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.deleteCertificateOperation#String -->
     *
     * @param certificateName The name of the certificate the operation pertains to. It is required and cannot be
     * {@code null} or empty.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue()} contains the deleted certificate operation.
     *
     * @throws HttpResponseException If a certificate operation for a certificate with {@code certificateName}
     * doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code certificateName} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificateOperation> deleteCertificateOperationWithResponse(String certificateName,
        RequestOptions requestOptions) {

        try {
            if (isNullOrEmpty(certificateName)) {
                throw new IllegalArgumentException("'certificateName' cannot be null or empty.");
            }

            return mapResponse(clientImpl.deleteCertificateOperationWithResponse(certificateName, requestOptions),
                CertificateOperationHelper::createCertificateOperation);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Cancels a certificate creation operation that is already in progress. This operation requires the
     * {@code certificates/update} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Triggers certificate creation and then cancels the creation operation in the key vault. Prints out the
     * cancelled certificate operation details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.cancelCertificateOperation#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.cancelCertificateOperation#String -->
     *
     * @param certificateName The name of the certificate the operation pertains to. It is required and cannot be
     * {@code null} or empty.
     * @return The cancelled certificate operation.
     *
     * @throws HttpResponseException If a certificate operation for a certificate with the given {@code name} doesn't
     * exist in the key vault.
     * @throws IllegalArgumentException If the {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificateOperation cancelCertificateOperation(String certificateName) {
        try {
            if (isNullOrEmpty(certificateName)) {
                throw new IllegalArgumentException("'certificateName' cannot be null or empty.");
            }

            return createCertificateOperation(
                clientImpl.updateCertificateOperation(certificateName, new CertificateOperationUpdateParameter(true)));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Cancels a certificate creation operation that is already in progress. This operation requires the
     * {@code certificates/update} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Triggers certificate creation and then cancels the creation operation in the key vault. Prints out the
     * cancelled certificate operation details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.cancelCertificateOperation#String-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.cancelCertificateOperation#String-RequestOptions -->
     *
     * @param certificateName The name of the certificate the operation pertains to. It is required and cannot be
     * {@code null} or empty.
     * @return A response object whose {@link Response#getValue()} contains the cancelled certificate operation.
     *
     * @throws HttpResponseException If a certificate operation for a certificate with the given {@code name} doesn't
     * exist in the key vault.
     * @throws IllegalArgumentException If the {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificateOperation> cancelCertificateOperationWithResponse(String certificateName,
        RequestOptions requestOptions) {

        try {
            return mapResponse(clientImpl.updateCertificateOperationWithResponse(certificateName,
                    new CertificateOperationUpdateParameter(true), requestOptions),
                CertificateOperationHelper::createCertificateOperation);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Merges a certificate or a certificate chain with a key pair currently available in the service. This operation
     * requires the {@code certificates/create} permission.
     *
     * <p>The {@code mergeCertificateOptions} parameter and its {@link MergeCertificateOptions#getName() name} value are
     * required.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Merges a certificate with a kay pair available in the service and prints out the merged certificate's details.
     * </p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.mergeCertificate#MergeCertificateOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.mergeCertificate#MergeCertificateOptions -->
     *
     * @param mergeCertificateOptions The merge certificate configuration holding the x509 certificates to merge.
     * @return The merged certificate.
     *
     * @throws HttpResponseException If the provided {@code mergeCertificateOptions} are malformed.
     * @throws IllegalArgumentException If {@link MergeCertificateOptions#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code mergeCertificateOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultCertificateWithPolicy mergeCertificate(MergeCertificateOptions mergeCertificateOptions) {
        try {
            Objects.requireNonNull(mergeCertificateOptions, "'mergeCertificateOptions' cannot be null.");

            CertificateMergeParameters certificateMergeParameters =
                new CertificateMergeParameters(mergeCertificateOptions.getX509Certificates())
                    .setTags(mergeCertificateOptions.getTags())
                    .setCertificateAttributes(
                        new CertificateAttributes().setEnabled(mergeCertificateOptions.isEnabled()));

            return createCertificateWithPolicy(
                clientImpl.mergeCertificate(mergeCertificateOptions.getName(), certificateMergeParameters));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Merges a certificate or a certificate chain with a key pair currently available in the service. This operation
     * requires the {@code certificates/create} permission.
     *
     * <p>The {@code mergeCertificateOptions} parameter and its {@link MergeCertificateOptions#getName() name} value are
     * required.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Merges a certificate with a kay pair available in the service. Prints out details of the response returned by
     * the service and the merged certificate.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.mergeCertificate#MergeCertificateOptions-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.mergeCertificate#MergeCertificateOptions-RequestOptions -->
     *
     * @param mergeCertificateOptions The merge certificate configuration holding the x509 certificates to merge.
     * @return A response object whose {@link Response#getValue()} contains the merged certificate.
     *
     * @throws HttpResponseException If the provided {@code mergeCertificateOptions} are malformed.
     * @throws IllegalArgumentException If {@link MergeCertificateOptions#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code mergeCertificateOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultCertificateWithPolicy> mergeCertificateWithResponse(
        MergeCertificateOptions mergeCertificateOptions, RequestOptions requestOptions) {

        try {
            Objects.requireNonNull(mergeCertificateOptions, "'mergeCertificateOptions' cannot be null.");

            if (isNullOrEmpty(mergeCertificateOptions.getName())) {
                throw new IllegalArgumentException("'mergeCertificateOptions.getName()' cannot be null or empty.");
            }

            CertificateMergeParameters certificateMergeParameters =
                new CertificateMergeParameters(mergeCertificateOptions.getX509Certificates())
                    .setTags(mergeCertificateOptions.getTags())
                    .setCertificateAttributes(
                        new CertificateAttributes().setEnabled(mergeCertificateOptions.isEnabled()));

            return mapResponse(
                clientImpl.mergeCertificateWithResponse(mergeCertificateOptions.getName(), certificateMergeParameters,
                    requestOptions), KeyVaultCertificateWithPolicyHelper::createCertificateWithPolicy);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Imports a pre-existing certificate to the key vault. The specified certificate must be in PFX or PEM format,
     * and must contain the private key as well as the x509 certificates. This operation requires the
     * {@code certificates/import} permission.
     *
     * <p>The {@code importCertificateOptions} parameter and its {@link ImportCertificateOptions#getName() name} value
     * are required.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Imports a certificate into the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.importCertificate#ImportCertificateOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.importCertificate#ImportCertificateOptions -->
     *
     * @param importCertificateOptions The details of the certificate to import to the key vault. It is required and
     * cannot be {@code null}.
     * @return The imported certificate.
     *
     * @throws HttpResponseException If the provided {@code importCertificateOptions} are malformed.
     * @throws IllegalArgumentException If {@link ImportCertificateOptions#getName()} is {@code null} or an empty
     * string.
     * @throws NullPointerException If {@code importCertificateOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultCertificateWithPolicy importCertificate(ImportCertificateOptions importCertificateOptions) {
        try {
            Objects.requireNonNull(importCertificateOptions, "'importCertificateOptions' cannot be null.");

            if (isNullOrEmpty(importCertificateOptions.getName())) {
                throw new IllegalArgumentException("'importCertificateOptions.getName()' cannot be null or empty.");
            }

            com.azure.v2.security.keyvault.certificates.implementation.models.CertificatePolicy implPolicy =
                getImplCertificatePolicy(importCertificateOptions.getPolicy());

            CertificateImportParameters certificateImportParameters =
                new CertificateImportParameters(transformCertificateForImport(importCertificateOptions))
                    .setPassword(importCertificateOptions.getPassword())
                    .setCertificatePolicy(implPolicy)
                    .setTags(importCertificateOptions.getTags())
                    .setCertificateAttributes(
                        new CertificateAttributes().setEnabled(importCertificateOptions.isEnabled()));

            return createCertificateWithPolicy(
                clientImpl.importCertificate(importCertificateOptions.getName(), certificateImportParameters));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Imports a pre-existing certificate to the key vault. The specified certificate must be in PFX or PEM format,
     * and must contain the private key as well as the x509 certificates. This operation requires the
     * {@code certificates/import} permission.
     *
     * <p>The {@code importCertificateOptions} parameter and its {@link ImportCertificateOptions#getName() name} value
     * are required.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Imports a certificate into the key vault. Prints out details of the response returned by the service and the
     * imported certificate.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.importCertificate#ImportCertificateOptions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.importCertificate#ImportCertificateOptions -->
     *
     * @param importCertificateOptions The details of the certificate to import to the key vault. It is required and
     * cannot be {@code null}.
     * @return A response object whose {@link Response#getValue()} contains the imported certificate.
     *
     * @throws HttpResponseException If the provided {@code importCertificateOptions} are malformed.
     * @throws IllegalArgumentException If {@link ImportCertificateOptions#getName()} is {@code null} or an empty
     * string.
     * @throws NullPointerException If {@code importCertificateOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultCertificateWithPolicy> importCertificateWithResponse(
        ImportCertificateOptions importCertificateOptions, RequestOptions requestOptions) {

        try {
            Objects.requireNonNull(importCertificateOptions, "'importCertificateOptions' cannot be null.");

            if (isNullOrEmpty(importCertificateOptions.getName())) {
                throw new IllegalArgumentException("'importCertificateOptions.getName()' cannot be null or empty.");
            }

            com.azure.v2.security.keyvault.certificates.implementation.models.CertificatePolicy implPolicy =
                getImplCertificatePolicy(importCertificateOptions.getPolicy());

            CertificateImportParameters certificateImportParameters =
                new CertificateImportParameters(transformCertificateForImport(importCertificateOptions))
                    .setPassword(importCertificateOptions.getPassword())
                    .setCertificatePolicy(implPolicy)
                    .setTags(importCertificateOptions.getTags())
                    .setCertificateAttributes(
                        new CertificateAttributes().setEnabled(importCertificateOptions.isEnabled()));

            return mapResponse(clientImpl.importCertificateWithResponse(importCertificateOptions.getName(),
                    certificateImportParameters, requestOptions),
                KeyVaultCertificateWithPolicyHelper::createCertificateWithPolicy);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    private static <T, S> Response<S> mapResponse(Response<T> response, Function<T, S> mapper) {
        if (response == null) {
            return null;
        }

        return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            mapper.apply(response.getValue()));
    }

    private static <T, S> PagedIterable<S> mapPages(Function<PagingOptions, PagedResponse<T>> firstPageRetriever,
        BiFunction<PagingOptions, String, PagedResponse<T>> nextPageRetriever, Function<T, S> mapper) {

        return new PagedIterable<>(pageSize -> mapPagedResponse(firstPageRetriever.apply(pageSize), mapper),
            (continuationToken, pageSize) -> mapPagedResponse(nextPageRetriever.apply(continuationToken, pageSize),
                mapper));
    }

    private static <T, S> PagedResponse<S> mapPagedResponse(PagedResponse<T> pagedResponse, Function<T, S> mapper) {
        if (pagedResponse == null) {
            return null;
        }

        return new PagedResponse<>(pagedResponse.getRequest(), pagedResponse.getStatusCode(),
            pagedResponse.getHeaders(),
            pagedResponse.getValue()
                .stream()
                .map(mapper)
                .collect(Collectors.toCollection(() -> new ArrayList<>(pagedResponse.getValue().size()))),
            pagedResponse.getContinuationToken(), null, null, null, null);
    }

    private static PagedResponse<CertificateContact> mapContactsToPagedResponse(Response<Contacts> response) {
        return new PagedResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            response.getValue().getContactList());
    }

    private static String transformCertificateForImport(ImportCertificateOptions options) {
        CertificatePolicy policy = options.getPolicy();

        return (policy != null && CertificateContentType.PEM.equals(policy.getContentType()))
            ? new String(options.getCertificate(), StandardCharsets.US_ASCII)
            : Base64.getEncoder().encodeToString(options.getCertificate());
    }
}

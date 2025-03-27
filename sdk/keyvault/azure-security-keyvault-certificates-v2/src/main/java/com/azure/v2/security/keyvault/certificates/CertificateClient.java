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
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateBundle;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateCreateParameters;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateImportParameters;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateIssuerSetParameters;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateIssuerUpdateParameters;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateItem;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateMergeParameters;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateOperationUpdateParameter;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateRestoreParameters;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateUpdateParameters;
import com.azure.v2.security.keyvault.certificates.implementation.models.Contacts;
import com.azure.v2.security.keyvault.certificates.implementation.models.DeletedCertificateItem;
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
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.Context;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
 * <p><strong>Sample: Construct CertificateClient</strong></p>
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
 * {@link CertificateClient#beginCreateCertificate(String, CertificatePolicy)} API.</p>
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
 * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificatePolicy#string -->
 * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificatePolicy#string -->
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
    private final String endpoint;

    /**
     * Get the vault endpoint to which service requests are sent to.
     *
     * @return The vault endpoint.
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Creates an instance of {@link CertificateClient} that sends requests to the given endpoint.
     *
     * @param clientImpl The implementation client.
     * @param endpoint The vault endpoint.
     */
    CertificateClient(CertificateClientImpl clientImpl, String endpoint) {
        this.clientImpl = clientImpl;
        this.endpoint = endpoint;
    }

    /**
     * Creates a new certificate. If a certificate with the provided name already exists, a new version of the
     * certificate is created. It requires the {@code certificates/create} permission.
     *
     * <p>Create certificate is a long running operation. It indefinitely waits for the create certificate operation to
     * complete on service side.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new certificate key and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy -->
     *
     * @param name The name of the certificate. It is required and cannot be {@code null} or empty.
     * @param policy The policy of the certificate to be created.
     * @return A {@link Poller} to poll on the create certificate operation status.
     *
     * @throws HttpResponseException If an invalid {@code policy} is provided.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     * @throws NullPointerException If {@code policy} is null.
     */
    // TODO (vcolin7): Uncomment when creating a Poller is supported in azure-core-v2.
    /*@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<CertificateOperation, KeyVaultCertificateWithPolicy> beginCreateCertificate(String name,
        CertificatePolicy policy) {

        return beginCreateCertificate(name, policy, true, null);
    }*/

    /**
     * Creates a new certificate. If a certificate with the provided name already exists, a new version of the
     * certificate is created. It requires the {@code certificates/create} permission.
     *
     * <p>Create certificate is a long running operation. It indefinitely waits for the create certificate operation to
     * complete on service side.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new certificate key and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy-Boolean-Map -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy-Boolean-Map -->
     *
     * @param name The name of the certificate. It is required and cannot be {@code null} or empty.
     * @param policy The policy of the certificate to be created.
     * @param isEnabled A value indicating whether the certificate is to be enabled.
     * @param tags Application specific metadata in the form of key-value pairs.
     * @return A {@link Poller} to poll on the create certificate operation status.
     *
     * @throws HttpResponseException If an invalid {@code policy} is provided.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     * @throws NullPointerException If {@code policy} is null.
     */
    // TODO (vcolin7): Uncomment when creating a Poller is supported in azure-core-v2.
    /*@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<CertificateOperation, KeyVaultCertificateWithPolicy> beginCreateCertificate(String name,
        CertificatePolicy policy, Boolean isEnabled, Map<String, String> tags) {

        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            if (policy == null) {
                throw new NullPointerException("'policy' cannot be null.");
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
     * Gets information on a pending from the key vault. This operation requires the {@code certificates/get}
     * permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a pending certificate operation and prints out its status.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateOperation#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateOperation#String -->
     *
     * @param certificateName The name of the certificate the operation pertains to.
     * @return A poller object to poll on the operation status.
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
     * Gets public information about the latest version of a given certificate. This operation requires the
     * {@code certificates/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the version of the certificate in the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificate#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificate#String -->
     *
     * @param name The name of the certificate to retrieve, cannot be null
     * @return The requested {@link KeyVaultCertificateWithPolicy certificate}.
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

            return createCertificateWithPolicy(clientImpl.getCertificate(name, null));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets information about the latest version of the specified certificate. This operation requires the
     * certificates/get permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a specific version of a certificate in the key vault. Prints out details of the response returned by the
     * service and the certificate.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateWithResponse#String-Context -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateWithResponse#String-Context -->
     *
     * @param name The name of the certificate to retrieve, cannot be null
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the requested
     * {@link KeyVaultCertificateWithPolicy certificate}.
     *
     * @throws HttpResponseException If a certificate with {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultCertificateWithPolicy> getCertificateWithResponse(String name,
        RequestOptions requestOptions) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return mapResponse(clientImpl.getCertificateWithResponse(name, "", requestOptions),
                KeyVaultCertificateWithPolicyHelper::createCertificateWithPolicy);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets information about the specified version of the specified certificate. This operation requires the
     * certificates/get permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a specific version of the certificate in the key vault. Prints out the returned certificate details when
     * a response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateVersion#String-String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateVersion#String-String -->
     *
     * @param name The name of the certificate to retrieve, cannot be null
     * @param version The version of the certificate to retrieve. If this is an empty String or null then latest version
     * of the certificate is retrieved.
     * @return The requested certificate.
     *
     * @throws HttpResponseException If a certificate with {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultCertificate getCertificateVersion(String name, String version) {
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
     * certificates/get permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a specific version of the certificate in the key vault. Prints out the returned certificate details when
     * a response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateVersionWithResponse#String-String-Context -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateVersionWithResponse#String-String-Context -->
     *
     * @param name The name of the certificate to retrieve, cannot be null
     * @param version The version of the certificate to retrieve. If this is an empty String or null then latest version
     * of the certificate is retrieved.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the requested
     * certificate.
     *
     * @throws HttpResponseException If a certificate with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultCertificate> getCertificateVersionWithResponse(String name, String version,
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
     * Updates the specified attributes associated with the specified certificate. The update operation changes
     * specified attributes of an existing stored certificate and attributes that are not specified in the request are
     * left unchanged. This operation requires the certificates/update permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets latest version of the certificate, changes its tags and enabled status and then updates it in the Azure
     * Key Vault. Prints out the returned certificate details when a response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificateProperties#CertificateProperties -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificateProperties#CertificateProperties -->
     *
     * @param properties The {@link CertificateProperties} object with updated properties.
     * @return The {@link CertificateProperties updated certificate}.
     *
     * @throws NullPointerException If {@code properties} is null.
     * @throws HttpResponseException If a certificate with {@link CertificateProperties#getName() certificateName}
     * and {@link CertificateProperties#getVersion() version} doesn't exist in the key vault.
     * @throws HttpRequestException If {@link CertificateProperties#getName() certificateName} or
     * {@link CertificateProperties#getVersion() version} is empty string.
     * @throws IllegalArgumentException If {@link CertificateProperties#getName()} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultCertificate updateCertificateProperties(CertificateProperties properties) {
        try {
            if (properties == null) {
                throw new NullPointerException("'properties' cannot be null.");
            }

            CertificateAttributes certificateAttributes = new CertificateAttributes()
                .setEnabled(properties.isEnabled())
                .setExpires(properties.getExpiresOn())
                .setNotBefore(properties.getNotBefore());

            CertificateUpdateParameters certificateUpdateParameters = new CertificateUpdateParameters()
                .setCertificateAttributes(certificateAttributes)
                .setTags(properties.getTags());

            return createCertificateWithPolicy(
                clientImpl.updateCertificate(properties.getName(), properties.getVersion(),
                    certificateUpdateParameters));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Updates the specified attributes associated with the specified certificate. The update operation changes
     * specified attributes of an existing stored certificate and attributes that are not specified in the request are
     * left unchanged. This operation requires the certificates/update permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets latest version of the certificate, changes its tags and enabled status and then updates it in the Azure
     * Key Vault. Prints out the returned certificate details when a response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificatePropertiesWithResponse#CertificateProperties-Context -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificatePropertiesWithResponse#CertificateProperties-Context -->
     *
     * @param properties The {@link CertificateProperties} object with updated properties.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link CertificateProperties updated certificate}.
     *
     * @throws NullPointerException If {@code properties} is null.
     * @throws HttpResponseException If a certificate with {@link CertificateProperties#getName() certificateName}
     * and {@link CertificateProperties#getVersion() version} doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultCertificate> updateCertificatePropertiesWithResponse(CertificateProperties properties,
        RequestOptions requestOptions) {

        try {
            if (properties == null) {
                throw new NullPointerException("'properties' cannot be null.");
            }

            CertificateAttributes certificateAttributes = new CertificateAttributes()
                .setEnabled(properties.isEnabled())
                .setExpires(properties.getExpiresOn())
                .setNotBefore(properties.getNotBefore());

            CertificateUpdateParameters certificateUpdateParameters = new CertificateUpdateParameters()
                .setCertificateAttributes(certificateAttributes)
                .setTags(properties.getTags());

            return mapResponse(clientImpl.updateCertificateWithResponse(properties.getName(), properties.getVersion(),
                    certificateUpdateParameters, requestOptions),
                KeyVaultCertificateWithPolicyHelper::createCertificateWithPolicy);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Deletes a certificate from a specified key vault. All the versions of the certificate along with its associated
     * policy get deleted. If soft-delete is enabled on the key vault then the certificate is placed in the deleted
     * state and requires to be purged for permanent deletion else the certificate is permanently deleted. The delete
     * operation applies to any certificate stored in Azure Key Vault, but it cannot be applied to an individual version
     * of a certificate. This operation requires the certificates/delete permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes the certificate in the Azure Key Vault. Prints out the deleted certificate details when a response has
     * been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.beginDeleteCertificate#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.beginDeleteCertificate#String -->
     *
     * @param name The name of the certificate to be deleted.
     * @throws HttpResponseException If a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException If a certificate with {@code name} is empty string.
     * @return A {@link Poller} to poll on and retrieve {@link DeletedCertificate deleted certificate}.
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
     * Retrieves information about the specified deleted certificate. The GetDeletedCertificate operation  is applicable
     * for soft-delete enabled vaults and additionally retrieves deleted certificate's attributes, such as retention
     * interval, scheduled permanent deletion and the current deletion recovery level. This operation requires the
     * certificates/get permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p> Gets the deleted certificate from the key vault enabled for soft-delete. Prints out the deleted certificate
     * details when a response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getDeletedCertificate#string -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getDeletedCertificate#string -->
     *
     * @param name The name of the deleted certificate.
     * @throws HttpResponseException If a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException If a certificate with {@code name} is empty string.
     * @return The {@link DeletedCertificate deleted certificate}.
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
     * Retrieves information about the specified deleted certificate. The GetDeletedCertificate operation  is applicable
     * for soft-delete enabled vaults and additionally retrieves deleted certificate's attributes, such as retention
     * interval, scheduled permanent deletion and the current deletion recovery level. This operation requires the
     * certificates/get permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p> Gets the deleted certificate from the key vault enabled for soft-delete. Prints out the deleted certificate
     * details when a response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getDeletedCertificateWithResponse#String-Context -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getDeletedCertificateWithResponse#String-Context -->
     *
     * @param name The name of the deleted certificate.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @throws HttpResponseException If a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException If a certificate with {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DeletedCertificate deleted certificate}.
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
     * Permanently deletes the specified deleted certificate without possibility for recovery. The Purge Deleted
     * Certificate operation is applicable for soft-delete enabled vaults and is not available if the recovery level
     * does not specify 'Purgeable'. This operation requires the certificate/purge permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Purges the deleted certificate from the key vault enabled for soft-delete. Prints out the status code from the
     * server response when a response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.purgeDeletedCertificate#string -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.purgeDeletedCertificate#string -->
     *
     * @param name The name of the deleted certificate.
     * @throws HttpResponseException If a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException If a certificate with {@code name} is empty string.
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
     * Permanently deletes the specified deleted certificate without possibility for recovery. The Purge Deleted
     * Certificate operation is applicable for soft-delete enabled vaults and is not available if the recovery level
     * does not specify 'Purgeable'. This operation requires the certificate/purge permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Purges the deleted certificate from the key vault enabled for soft-delete. Prints out the status code from the
     * server response when a response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.purgeDeletedCertificateWithResponse#string-Context -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.purgeDeletedCertificateWithResponse#string-Context -->
     *
     * @param name The name of the deleted certificate.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @throws HttpResponseException If a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException If a certificate with {@code name} is empty string.
     * @return A response containing status code and HTTP headers.
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
     * Recovers the deleted certificate back to its current version under /certificates and can only be performed on a
     * soft-delete enabled vault. The RecoverDeletedCertificate operation performs the reversal of the Delete operation
     * and must be issued during the retention interval (available in the deleted certificate's attributes). This
     * operation requires the certificates/recover permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recovers the deleted certificate from the key vault enabled for soft-delete. Prints out the recovered
     * certificate details when a response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.beginRecoverDeletedCertificate#String -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.beginRecoverDeletedCertificate#String -->
     *
     * @param name The name of the deleted certificate to be recovered.
     * @throws HttpResponseException If a certificate with {@code name} doesn't exist in the
     * certificate vault.
     * @throws HttpRequestException If a certificate with {@code name} is empty string.
     * @return A {@link Poller} to poll on and retrieve {@link KeyVaultCertificateWithPolicy recovered certificate}.
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
                // This means permission is not granted for the get deleted key operation.
                // In both cases deletion operation was successful when activation operation succeeded before
                // reaching here.
                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    pollingContext.getLatestResponse().getValue());
            }
        } catch (RuntimeException e) {
            // This means permission is not granted for the get deleted key operation.
            // In both cases deletion operation was successful when activation operation succeeded before reaching
            // here.
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                pollingContext.getLatestResponse().getValue());
        }
    }

    /**
     * Requests that a backup of the specified certificate be downloaded to the client. All versions of the certificate
     * will be downloaded. This operation requires the certificates/backup permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Backs up the certificate from the key vault. Prints out the length of the certificate's backup byte array
     * returned in the response.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.backupCertificate#string -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.backupCertificate#string -->
     *
     * @param name The name of the certificate.
     * @throws HttpResponseException If a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException If a certificate with {@code name} is empty string.
     * @return The backed up certificate blob.
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
     * Requests that a backup of the specified certificate be downloaded to the client. All versions of the certificate
     * will be downloaded. This operation requires the certificates/backup permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Backs up the certificate from the key vault. Prints out the length of the certificate's backup byte array
     * returned in the response.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.backupCertificateWithResponse#String-Context -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.backupCertificateWithResponse#String-Context -->
     *
     * @param name The name of the certificate.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @throws HttpResponseException If a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException If a certificate with {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the backed up certificate blob.
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
     * Restores a backed up certificate to the vault. All the versions of the certificate are restored to the vault.
     * This operation requires the certificates/restore permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Restores the certificate in the key vault from its backup. Prints out the restored certificate details when a
     * response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.restoreCertificate#byte -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.restoreCertificate#byte -->
     *
     * @param backup The backup blob associated with the certificate.
     * @throws HttpResponseException If {@code backup} blob is malformed.
     * @return The {@link KeyVaultCertificate restored certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultCertificateWithPolicy restoreCertificateBackup(byte[] backup) {
        try {
            Objects.requireNonNull(backup, "'backup' cannot be null.");

            return createCertificateWithPolicy(clientImpl.restoreCertificate(new CertificateRestoreParameters(backup)));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Restores a backed up certificate to the vault. All the versions of the certificate are restored to the vault.
     * This operation requires the certificates/restore permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Restores the certificate in the key vault from its backup. Prints out the restored certificate details when a
     * response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.restoreCertificateWithResponse#byte-Context -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.restoreCertificateWithResponse#byte-Context -->
     *
     * @param backup The backup blob associated with the certificate.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @throws HttpResponseException If {@code backup} blob is malformed.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultCertificate restored certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultCertificateWithPolicy> restoreCertificateBackupWithResponse(byte[] backup,
        RequestOptions requestOptions) {

        try {
            Objects.requireNonNull(backup, "'backup' cannot be null.");

            return mapResponse(
                clientImpl.restoreCertificateWithResponse(new CertificateRestoreParameters(backup), requestOptions),
                KeyVaultCertificateWithPolicyHelper::createCertificateWithPolicy);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * List certificates in the key vault. Retrieves the set of certificates resources in the key vault and the
     * individual certificate response in the iterable is represented by {@link CertificateProperties} as only the
     * certificate identifier, thumbprint, attributes and tags are provided in the response. The policy and individual
     * certificate versions are not listed in the response. This operation requires the certificates/list permission.
     *
     * <p>It is possible to get certificates with all the properties excluding the policy from this information. Loop
     * over the {@link CertificateProperties} and call {@link CertificateClient#getCertificateVersion(String, String)} .
     * This will return the certificate with all its properties excluding the policy.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listCertificates -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listCertificates -->
     *
     * @return A {@link PagedIterable} containing {@link CertificateProperties certificate} for all the certificates in
     * the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateProperties> listPropertiesOfCertificates() {
        return listPropertiesOfCertificates(false, RequestOptions.none());
    }

    /**
     * List certificates in the key vault. Retrieves the set of certificates resources in the key vault and the
     * individual certificate response in the iterable is represented by {@link CertificateProperties} as only the
     * certificate identifier, thumbprint, attributes and tags are provided in the response. The policy and individual
     * certificate versions are not listed in the response. This operation requires the certificates/list permission.
     *
     * <p>It is possible to get certificates with all the properties excluding the policy from this information. Loop
     * over the {@link CertificateProperties} and call {@link CertificateClient#getCertificateVersion(String, String)} .
     * This will return the certificate with all its properties excluding the policy.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listCertificates#context -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listCertificates#context -->
     *
     * @param includePending indicate if pending certificates should be included in the results.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} containing {@link CertificateProperties certificate} for all the certificates in
     * the vault.
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
     * Lists the {@link DeletedCertificate deleted certificates} in the key vault currently available for recovery. This
     * operation includes deletion-specific information and is applicable for vaults enabled for soft-delete. This
     * operation requires the certificates/get/list permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Lists the deleted certificates in the key vault. Prints out the recovery id of each deleted certificate when a
     * response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates -->
     *
     * @return A {@link PagedIterable} containing all of the {@link DeletedCertificate deleted certificates} in the
     * vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedCertificate> listDeletedCertificates() {
        return listDeletedCertificates(false, RequestOptions.none());
    }

    /**
     * Lists the {@link DeletedCertificate deleted certificates} in the key vault currently available for recovery. This
     * operation includes deletion-specific information and is applicable for vaults enabled for soft-delete. This
     * operation requires the certificates/get/list permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Lists the deleted certificates in the key vault. Prints out the recovery id of each deleted certificate when a
     * response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates#context -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates#context -->
     *
     * @param includePending indicate if pending deleted certificates should be included in the results.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} containing all of the {@link DeletedCertificate deleted certificates} in the
     * vault.
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
     * List all versions of the specified certificate. The individual certificate response in the iterable is
     * represented by {@link CertificateProperties} as only the certificate identifier, thumbprint, attributes and tags
     * are provided in the response. The policy is not listed in the response. This operation requires the
     * certificates/list permission.
     *
     * <p>It is possible to get the certificates with properties excluding the policy for all the versions from this
     * information. Loop over the {@link CertificateProperties} and call
     * {@link CertificateClient#getCertificateVersion(String, String)}. This will return the
     * certificate with all its properties excluding the policy.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listCertificateVersions -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listCertificateVersions -->
     *
     * @param certificateName The name of the certificate.
     * @throws HttpResponseException If a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpRequestException If a certificate with {@code certificateName} is empty string.
     * @return A {@link PagedIterable} containing {@link CertificateProperties certificate} of all the versions of the
     * specified certificate in the vault. Paged Iterable is empty if certificate with {@code certificateName} does not
     * exist in key vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateProperties> listPropertiesOfCertificateVersions(String certificateName) {
        return listPropertiesOfCertificateVersions(certificateName, RequestOptions.none());
    }

    /**
     * List all versions of the specified certificate. The individual certificate response in the iterable is
     * represented by {@link CertificateProperties} as only the certificate identifier, thumbprint, attributes and tags
     * are provided in the response. The policy is not listed in the response. This operation requires the
     * certificates/list permission.
     *
     * <p>It is possible to get the certificates with properties excluding the policy for all the versions from this
     * information. Loop over the {@link CertificateProperties} and call
     * {@link CertificateClient#getCertificateVersion(String, String)}. This will return the
     * certificate with all its properties excluding the policy.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listCertificateVersions#context -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listCertificateVersions#context -->
     *
     * @param name The name of the certificate.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @throws HttpResponseException If a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException If a certificate with {@code name} is empty string.
     * @return A {@link PagedIterable} containing {@link CertificateProperties certificate} of all the versions of the
     * specified certificate in the vault. Iterable is empty if certificate with {@code name} does not exist
     * in key vault.
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
     * Retrieves the policy of the specified certificate in the key vault. This operation requires the certificates/get
     * permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the policy of a certificate in the key vault. Prints out the returned certificate policy details when a
     * response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificatePolicy#string -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificatePolicy#string -->
     *
     * @param certificateName The name of the certificate whose policy is to be retrieved, cannot be null
     * @throws HttpResponseException If a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpRequestException If {@code certificateName} is empty string.
     * @return The requested {@link CertificatePolicy certificate policy}.
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
     * Retrieves the policy of the specified certificate in the key vault. This operation requires the certificates/get
     * permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the policy of a certificate in the key vault. Prints out the returned certificate policy details when a
     * response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificatePolicyWithResponse#string -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificatePolicyWithResponse#string -->
     *
     * @param certificateName The name of the certificate whose policy is to be retrieved, cannot be null
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the requested
     * {@link CertificatePolicy certificate policy}.
     * @throws HttpResponseException If a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpRequestException If {@code certificateName} is empty string.
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
     * Updates the policy for a certificate. The update operation changes specified attributes of the certificate policy
     * and attributes that are not specified in the request are left unchanged. This operation requires the
     * certificates/update permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the certificate policy, changes its properties and then updates it in the Azure Key Vault. Prints out the
     * returned policy details when a response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificatePolicy#string -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificatePolicy#string -->
     *
     * @param certificateName The name of the certificate whose policy is to be updated.
     * @param policy The certificate policy to be updated.
     * @throws NullPointerException If {@code policy} is null.
     * @throws HttpResponseException If a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpRequestException If {@code certificateName} is empty string or if {@code policy} is invalid.
     * @return The updated {@link CertificatePolicy certificate policy}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificatePolicy updateCertificatePolicy(String certificateName, CertificatePolicy policy) {
        try {
            if (isNullOrEmpty(certificateName)) {
                throw new IllegalArgumentException("'certificateName' cannot be null or empty.");
            }

            if (policy == null) {
                throw new NullPointerException("'policy' cannot be null.");
            }

            return createCertificatePolicy(
                clientImpl.updateCertificatePolicy(certificateName, getImplCertificatePolicy(policy)));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Updates the policy for a certificate. The update operation changes specified attributes of the certificate policy
     * and attributes that are not specified in the request are left unchanged. This operation requires the
     * certificates/update permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the certificate policy, changes its properties and then updates it in the Azure Key Vault. Prints out the
     * returned policy details when a response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificatePolicyWithResponse#string -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificatePolicyWithResponse#string -->
     *
     * @param certificateName The certificateName of the certificate whose policy is to be updated.
     * @param policy The certificate policy to be updated.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @throws NullPointerException If {@code policy} is null.
     * @throws HttpResponseException If a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpRequestException If {@code certificateName} is empty string or if {@code policy} is invalid.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the updated
     * {@link CertificatePolicy certificate policy}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificatePolicy> updateCertificatePolicyWithResponse(String certificateName,
        CertificatePolicy policy, RequestOptions requestOptions) {

        try {
            if (isNullOrEmpty(certificateName)) {
                throw new IllegalArgumentException("'certificateName' cannot be null or empty.");
            }

            if (policy == null) {
                throw new NullPointerException("'policy' cannot be null.");
            }

            return mapResponse(
                clientImpl.updateCertificatePolicyWithResponse(certificateName, getImplCertificatePolicy(policy),
                    requestOptions), CertificatePolicyHelper::createCertificatePolicy);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Creates the specified certificate issuer. The SetCertificateIssuer operation updates the specified certificate
     * issuer if it already exists or adds it if it doesn't exist. This operation requires the
     * certificates/setissuers permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new certificate issuer in the key vault. Prints out the created certificate issuer details when a
     * response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.createIssuer#CertificateIssuer -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.createIssuer#CertificateIssuer -->
     *
     * @param issuer The configuration of the certificate issuer to be created.
     * @throws NullPointerException If {@code issuer} is null.
     * @throws HttpResponseException If invalid certificate issuer {@code issuer} configuration is provided.
     * @throws HttpRequestException If a certificate issuer with {@link CertificateIssuer#getName() name} is empty
     * string.
     * @return The created {@link CertificateIssuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificateIssuer createIssuer(CertificateIssuer issuer) {
        try {
            if (issuer == null) {
                throw new NullPointerException("'issuer' cannot be null.");
            }

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
     * Creates the specified certificate issuer. The SetCertificateIssuer operation updates the specified certificate
     * issuer if it already exists or adds it if it doesn't exist. This operation requires the
     * certificates/setissuers permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new certificate issuer in the key vault. Prints out the created certificate issuer details when a
     * response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.createIssuerWithResponse#CertificateIssuer-Context -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.createIssuerWithResponse#CertificateIssuer-Context -->
     *
     * @param issuer The configuration of the certificate issuer to be created.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @throws NullPointerException If {@code issuer} is null.
     * @throws HttpResponseException If invalid certificate issuer {@code issuer} configuration is provided.
     * @throws HttpRequestException If a certificate issuer with {@link CertificateIssuer#getName() name} is empty
     * string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the created
     * {@link CertificateIssuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificateIssuer> createIssuerWithResponse(CertificateIssuer issuer,
        RequestOptions requestOptions) {

        try {
            if (issuer == null) {
                throw new NullPointerException("'issuer' cannot be null.");
            }

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
     * Retrieves the specified certificate issuer from the key vault. This operation requires the
     * certificates/manageissuers/getissuers permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the specified certificate issuer in the key vault. Prints out the returned certificate issuer details
     * when a response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getIssuer#string -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getIssuer#string -->
     *
     * @param name The name of the certificate issuer to retrieve, cannot be null
     * @throws HttpResponseException If a certificate issuer with {@code name} doesn't exist in the key
     * vault.
     * @throws HttpRequestException If {@code name} is empty string.
     * @return The requested {@link CertificateIssuer certificate issuer}.
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
     * Retrieves the specified certificate issuer from the key vault. This operation requires the
     * certificates/manageissuers/getissuers permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the specified certificate issuer in the key vault. Prints out the returned certificate issuer details
     * when a response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getIssuerWithResponse#string-context -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getIssuerWithResponse#string-context -->
     *
     * @param name The name of the certificate issuer to retrieve, cannot be null
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @throws HttpResponseException If a certificate issuer with {@code name} doesn't exist in the key
     * vault.
     * @throws HttpRequestException If {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the requested
     * {@link CertificateIssuer certificate issuer}.
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
     * Deletes the specified certificate issuer. The DeleteCertificateIssuer operation permanently removes the specified
     * certificate issuer from the key vault. This operation requires the certificates/manageissuers/deleteissuers
     * permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes the certificate issuer in the Azure Key Vault. Prints out the deleted certificate details when a
     * response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.deleteIssuer#string -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.deleteIssuer#string -->
     *
     * @param name The name of the certificate issuer to be deleted.
     * @throws HttpResponseException If a certificate issuer with {@code name} doesn't exist in the key
     * vault.
     * @throws HttpRequestException If a certificate issuer with {@code name} is empty string.
     * @return The {@link CertificateIssuer deleted issuer}.
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
     * Deletes the specified certificate issuer. The DeleteCertificateIssuer operation permanently removes the specified
     * certificate issuer from the key vault. This operation requires the certificates/manageissuers/deleteissuers
     * permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes the certificate issuer in the Azure Key Vault. Prints out the
     * deleted certificate details when a response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.deleteIssuerWithResponse#string-context -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.deleteIssuerWithResponse#string-context -->
     *
     * @param name The name of the certificate issuer to be deleted.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @throws HttpResponseException If a certificate issuer with {@code name} doesn't exist in the key
     * vault.
     * @throws HttpRequestException If a certificate issuer with {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link CertificateIssuer deleted issuer}.
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
     * List all the certificate issuers resources in the key vault. The individual certificate issuer response in the
     * iterable is represented by {@link IssuerProperties} as only the certificate issuer identifier and provider are
     * provided in the response. This operation requires the certificates/manageissuers/getissuers permission.
     *
     * <p>It is possible to get the certificate issuer with all of its properties from this information. Loop over the
     * {@link IssuerProperties issuerProperties} and call {@link CertificateClient#getIssuer(String)}. This will return
     * the {@link CertificateIssuer issuer} with all its properties.</p>.
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers -->
     *
     * @return A {@link PagedIterable} containing all of the {@link IssuerProperties certificate issuers} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<IssuerProperties> listPropertiesOfIssuers() {
        return listPropertiesOfIssuers(RequestOptions.none());
    }

    /**
     * List all the certificate issuers resources in the key vault. The individual certificate issuer response in the
     * iterable is represented by {@link IssuerProperties} as only the certificate issuer identifier and provider are
     * provided in the response. This operation requires the certificates/manageissuers/getissuers permission.
     *
     * <p>It is possible to get the certificate issuer with all of its properties from this information. Loop over the
     * {@link IssuerProperties issuerProperties} and call {@link CertificateClient#getIssuer(String)}. This will return
     * the {@link CertificateIssuer issuer} with all its properties.</p>.
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers#context -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers#context -->
     *
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} containing all of the {@link IssuerProperties certificate issuers} in the vault.
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
     * Updates the specified certificate issuer. The UpdateCertificateIssuer operation updates the specified attributes
     * of the certificate issuer entity. This operation requires the certificates/setissuers permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the certificate issuer, changes its attributes/properties then updates it in the Azure Key Vault. Prints
     * out the returned certificate issuer details when a response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.updateIssuer#CertificateIssuer -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.updateIssuer#CertificateIssuer -->
     *
     * @param issuer The {@link CertificateIssuer issuer} with updated properties.
     * @throws NullPointerException If {@code issuer} is null.
     * @throws HttpResponseException If a certificate issuer with {@link CertificateIssuer#getName() name} doesn't
     * exist in the key vault.
     * @throws HttpRequestException If {@link CertificateIssuer#getName() name} is empty string.
     * @return The {@link CertificateIssuer updated issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificateIssuer updateIssuer(CertificateIssuer issuer) {
        try {
            if (issuer == null) {
                throw new NullPointerException("'issuer' cannot be null.");
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
     * Updates the specified certificate issuer. The UpdateCertificateIssuer operation updates the specified attributes
     * of the certificate issuer entity. This operation requires the certificates/setissuers permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the certificate issuer, changes its attributes/properties then updates it in the Azure Key Vault. Prints
     * out the returned certificate issuer details when a response has been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.updateIssuerWithResponse#CertificateIssuer-Context -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.updateIssuerWithResponse#CertificateIssuer-Context -->
     *
     * @param issuer The {@link CertificateIssuer issuer} with updated properties.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @throws NullPointerException If {@code issuer} is null.
     * @throws HttpResponseException If a certificate issuer with {@link CertificateIssuer#getName() name} doesn't
     * exist in the key vault.
     * @throws HttpRequestException If {@link CertificateIssuer#getName() name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link CertificateIssuer updated issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificateIssuer> updateIssuerWithResponse(CertificateIssuer issuer,
        RequestOptions requestOptions) {

        try {
            if (issuer == null) {
                throw new NullPointerException("'issuer' cannot be null.");
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
     * Sets the certificate contacts on the key vault. This operation requires the certificates/managecontacts
     * permission.
     *
     *<p>The {@link LifetimeAction} of type {@link CertificatePolicyAction#EMAIL_CONTACTS} set on a
     * {@link CertificatePolicy} emails the contacts set on the vault when triggered.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Sets the certificate contacts in the Azure Key Vault. Prints out the returned contacts details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.setContacts#contacts -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.setContacts#contacts -->
     *
     * @param contacts The list of contacts to set on the vault.
     * @throws HttpRequestException If a contact information provided is invalid/incomplete.
     * @return A {@link PagedIterable} containing all of the {@link CertificateContact certificate contacts} in the
     * vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateContact> setContacts(List<CertificateContact> contacts) {
        return setContacts(contacts, RequestOptions.none());
    }

    /**
     * Sets the certificate contacts on the key vault. This operation requires the certificates/managecontacts
     * permission.
     *
     *<p>The {@link LifetimeAction} of type {@link CertificatePolicyAction#EMAIL_CONTACTS} set on a
     * {@link CertificatePolicy} emails the contacts set on the vault when triggered.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Sets the certificate contacts in the Azure Key Vault. Prints out the returned contacts details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.setContacts#contacts-context -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.setContacts#contacts-context -->
     *
     * @param contacts The list of contacts to set on the vault.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @throws HttpRequestException If a contact information provided is invalid/incomplete.
     * @return A {@link PagedIterable} containing all of the {@link CertificateContact certificate contacts} in the
     * vault.
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
     * Lists the certificate contacts in the key vault. This operation requires the certificates/managecontacts
     * permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Lists the certificate contacts in the Azure Key Vault. Prints out the returned contacts details in the
     * response.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listContacts -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listContacts -->
     *
     * @return A {@link PagedIterable} containing all of the {@link CertificateContact certificate contacts} in the
     * vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateContact> listContacts() {
        return listContacts(RequestOptions.none());
    }

    /**
     * Lists the certificate contacts in the key vault. This operation requires the certificates/managecontacts
     * permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Lists the certificate contacts in the Azure Key Vault. Prints out the returned contacts details in the
     * response.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listContacts#context -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listContacts#context -->
     *
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} containing all of the {@link CertificateContact certificate contacts} in the
     * vault.
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
     * Deletes the certificate contacts in the key vault. This operation requires the certificates/managecontacts
     * permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes the certificate contacts in the Azure Key Vault. Subscribes to the call and prints out the deleted
     * contacts details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.deleteContacts -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.deleteContacts -->
     *
     * @return A {@link PagedIterable} containing the deleted {@link CertificateContact certificate contacts} in the
     * vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateContact> deleteContacts() {
        return deleteContacts(RequestOptions.none());
    }

    /**
     * Deletes the certificate contacts in the key vault. This operation requires the certificates/managecontacts
     * permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes the certificate contacts in the Azure Key Vault. Prints out the deleted contacts details in the
     * response.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.deleteContacts#context -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.deleteContacts#context -->
     *
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} containing the deleted {@link CertificateContact certificate contacts} in the
     * vault.
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
     * Deletes the creation operation for the specified certificate that is in the process of being created. The
     * certificate is no longer created. This operation requires the certificates/update permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Triggers certificate creation and then deletes the certificate creation operation in the Azure Key Vault.
     * Subscribes to the call and prints out the deleted certificate operation details when a response has been
     * received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.deleteCertificateOperation#string -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.deleteCertificateOperation#string -->
     *
     * @param certificateName The name of the certificate.
     * @throws HttpResponseException If a certificate operation for a certificate with {@code certificateName}
     * doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code certificateName} is {@code null} or an empty string.
     * @return The deleted {@link CertificateOperation certificate operation}.
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
     * certificate is no longer created. This operation requires the certificates/update permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Triggers certificate creation and then deletes the certificate creation operation in the Azure Key Vault.
     * Subscribes to the call and prints out the deleted certificate operation details when a response has been
     * received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.deleteCertificateOperationWithResponse#string -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.deleteCertificateOperationWithResponse#string -->
     *
     * @param certificateName The name of the certificate.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @throws HttpResponseException If a certificate operation for a certificate with {@code certificateName}
     * doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code certificateName} is {@code null} or an empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link CertificateOperation deleted certificate operation}.
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
     * certificates/update permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Triggers certificate creation and then cancels the certificate creation operation in the Azure Key Vault.
     * Subscribes to the call and prints out the updated certificate operation details when a response has been
     * received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.cancelCertificateOperation#string -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.cancelCertificateOperation#string -->
     *
     * @param certificateName The name of the certificate which is in the process of being created.
     * @throws HttpResponseException If a certificate operation for a certificate with {@code name} doesn't exist
     * in the key vault.
     * @throws HttpRequestException If the {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link CertificateOperation cancelled certificate operation}.
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
     * certificates/update permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Triggers certificate creation and then cancels the certificate creation operation in the Azure Key Vault.
     * Subscribes to the call and prints out the updated certificate operation details when a response has been
     * received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.cancelCertificateOperationWithResponse#string -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.cancelCertificateOperationWithResponse#string -->
     *
     * @param certificateName The name of the certificate which is in the process of being created.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @throws HttpResponseException If a certificate operation for a certificate with {@code name} doesn't exist
     * in the key vault.
     * @throws HttpRequestException If the {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link CertificateOperation cancelled certificate operation}.
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
     * requires the certificates/create permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p> Merges a certificate with a kay pair available in the service.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.mergeCertificate#config -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.mergeCertificate#config -->
     *
     * @param mergeCertificateOptions the merge certificate configuration holding the x509 certificates.
     * @throws NullPointerException If {@code mergeCertificateOptions} is null.
     * @throws HttpRequestException If {@code mergeCertificateOptions} is invalid/corrupt.
     * @return The merged certificate.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultCertificateWithPolicy mergeCertificate(MergeCertificateOptions mergeCertificateOptions) {
        try {
            if (mergeCertificateOptions == null) {
                throw new NullPointerException("'mergeCertificateOptions' cannot be null.");
            }

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
     * requires the certificates/create permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p> Merges a certificate with a kay pair available in the service.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.mergeCertificateWithResponse#config -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.mergeCertificateWithResponse#config -->
     *
     * @param mergeCertificateOptions the merge certificate configuration holding the x509 certificates.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @throws NullPointerException If {@code mergeCertificateOptions} is null.
     * @throws HttpRequestException If {@code mergeCertificateOptions} is invalid/corrupt.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the merged certificate.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultCertificateWithPolicy> mergeCertificateWithResponse(
        MergeCertificateOptions mergeCertificateOptions, RequestOptions requestOptions) {

        try {
            if (mergeCertificateOptions == null) {
                throw new NullPointerException("'mergeCertificateOptions' cannot be null.");
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
     * certificates/import permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p> Imports a certificate into the key vault.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.importCertificate#options -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.importCertificate#options -->
     *
     * @param importCertificateOptions The details of the certificate to import to the key vault
     * @throws HttpRequestException If the {@code importCertificateOptions} are invalid.
     * @throws NullPointerException If {@code importCertificateOptions} is null.
     * @return the {@link KeyVaultCertificateWithPolicy imported certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultCertificateWithPolicy importCertificate(ImportCertificateOptions importCertificateOptions) {
        try {
            if (importCertificateOptions == null) {
                throw new NullPointerException("'importCertificateOptions' cannot be null.");
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
     * certificates/import permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p> Imports a certificate into the key vault.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.importCertificateWithResponse#options -->
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.importCertificateWithResponse#options -->
     *
     * @param importCertificateOptions The details of the certificate to import to the key vault
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @throws HttpRequestException If the {@code importCertificateOptions} are invalid.
     * @throws NullPointerException If {@code importCertificateOptions} is null.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultCertificateWithPolicy imported certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultCertificateWithPolicy> importCertificateWithResponse(
        ImportCertificateOptions importCertificateOptions, RequestOptions requestOptions) {

        try {
            if (importCertificateOptions == null) {
                throw new NullPointerException("'importCertificateOptions' cannot be null.");
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

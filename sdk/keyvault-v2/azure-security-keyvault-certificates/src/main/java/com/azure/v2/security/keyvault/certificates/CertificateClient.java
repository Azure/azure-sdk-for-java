// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.certificates;

import com.azure.v2.core.http.polling.LongRunningOperationStatus;
import com.azure.v2.core.http.polling.PollResponse;
import com.azure.v2.core.http.polling.Poller;
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
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateMergeParameters;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateOperationUpdateParameter;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateRestoreParameters;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateUpdateParameters;
import com.azure.v2.security.keyvault.certificates.implementation.models.Contacts;
import com.azure.v2.security.keyvault.certificates.implementation.models.DeletedCertificateBundle;
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
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.paging.PagedIterable;
import io.clientcore.core.http.paging.PagedResponse;
import io.clientcore.core.http.paging.PagingOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;

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
 * This class provides methods to manage {@link KeyVaultCertificate certificates} in Azure Key Vault. The client
 * supports creating, retrieving, updating, merging, deleting, purging, backing up, restoring and listing the
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
 * <pre>
 * CertificateClient certificateClient = new CertificateClientBuilder&#40;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .endpoint&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;
 *     .httpInstrumentationOptions&#40;new HttpInstrumentationOptions&#40;&#41;
 *         .setHttpLogLevel&#40;HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.instantiation  -->
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
 * <pre>
 * CertificatePolicy certPolicy = new CertificatePolicy&#40;&quot;Self&quot;, &quot;CN=SelfSignedJavaPkcs12&quot;&#41;;
 *
 * Poller&lt;CertificateOperation, KeyVaultCertificateWithPolicy&gt; certPoller = null;
 *     &#47;&#47;certificateClient.beginCreateCertificate&#40;&quot;certificateName&quot;, certPolicy&#41;;
 *
 * certPoller.waitUntil&#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41;;
 *
 * KeyVaultCertificate cert = certPoller.getFinalResult&#40;&#41;;
 *
 * System.out.printf&#40;&quot;Certificate created with name %s%n&quot;, cert.getName&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy -->
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
 * <pre>
 * CertificatePolicy policy = certificateClient.getCertificatePolicy&#40;&quot;certificateName&quot;&#41;;
 *
 * System.out.printf&#40;&quot;Received policy with subject name %s%n&quot;, policy.getSubject&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificatePolicy#String -->
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
 * <pre>
 * Poller&lt;DeletedCertificate, Void&gt; deleteCertPoller = null;
 *     &#47;&#47;certificateClient.beginDeleteCertificate&#40;&quot;certificateName&quot;&#41;;
 *
 * &#47;&#47; Deleted Certificate is accessible as soon as polling beings.
 * PollResponse&lt;DeletedCertificate&gt; deleteCertPollResponse = deleteCertPoller.poll&#40;&#41;;
 *
 * System.out.printf&#40;&quot;Deleted certificate with name %s and recovery id %s%n&quot;,
 *     deleteCertPollResponse.getValue&#40;&#41;.getName&#40;&#41;, deleteCertPollResponse.getValue&#40;&#41;.getRecoveryId&#40;&#41;&#41;;
 * deleteCertPoller.waitForCompletion&#40;&#41;;
 * </pre>
 * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.beginDeleteCertificate#String -->
 *
 * @see com.azure.v2.security.keyvault.certificates
 * @see CertificateClientBuilder
 */
@ServiceClient(
    builder = CertificateClientBuilder.class,
    serviceInterfaces = CertificateClientImpl.CertificateClientService.class)
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
     * <p>Create certificate is a long-running operation. It indefinitely waits for the create certificate operation to
     * complete on service side.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new certificate in the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy -->
     * <pre>
     * CertificatePolicy certPolicy = new CertificatePolicy&#40;&quot;Self&quot;, &quot;CN=SelfSignedJavaPkcs12&quot;&#41;;
     *
     * Poller&lt;CertificateOperation, KeyVaultCertificateWithPolicy&gt; certPoller = null;
     *     &#47;&#47;certificateClient.beginCreateCertificate&#40;&quot;certificateName&quot;, certPolicy&#41;;
     *
     * certPoller.waitUntil&#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41;;
     *
     * KeyVaultCertificate cert = certPoller.getFinalResult&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Certificate created with name %s%n&quot;, cert.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy -->
     *
     * @param name The name of the certificate to create. It is required and cannot be {@code null} or empty.
     * @param policy The policy of the certificate to be created.
     * @return A {@link Poller} to poll on and retrieve the created certificate with.
     *
     * @throws HttpResponseException If the provided {@code policy} is malformed.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<CertificateOperation, KeyVaultCertificateWithPolicy> beginCreateCertificate(String name,
        CertificatePolicy policy) {

        return beginCreateCertificate(name, policy, true, null);
    }

    /**
     * Creates a new certificate in the key vault. If a certificate with the provided name already exists, a new version
     * of the certificate is created. It requires the {@code certificates/create} permission.
     *
     * <p>Create certificate is a long-running operation. It indefinitely waits for the create certificate operation to
     * complete on service side.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new certificate in the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy-Boolean-Map -->
     * <pre>
     * CertificatePolicy policy = new CertificatePolicy&#40;&quot;Self&quot;,
     *     &quot;CN=SelfSignedJavaPkcs12&quot;&#41;;
     * Map&lt;String, String&gt; tags = new HashMap&lt;&gt;&#40;&#41;;
     *
     * Poller&lt;CertificateOperation, KeyVaultCertificateWithPolicy&gt; certificateSyncPoller = null;
     *     &#47;&#47;certificateClient.beginCreateCertificate&#40;&quot;certificateName&quot;, policy, true, tags&#41;;
     *
     * certificateSyncPoller.waitUntil&#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41;;
     *
     * KeyVaultCertificate createdCertificate = certificateSyncPoller.getFinalResult&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Certificate created with name %s%n&quot;, createdCertificate.getName&#40;&#41;&#41;;
     * </pre>
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
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<CertificateOperation, KeyVaultCertificateWithPolicy> beginCreateCertificate(String name,
        CertificatePolicy policy, Boolean isEnabled, Map<String, String> tags) {

        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return Poller.createPoller(Duration.ofSeconds(1),
            pollingContext -> createCertificateActivationOperation(name, policy, isEnabled, tags),
            pollingContext -> createCertificatePollOperation(name),
            (pollingContext, pollResponse) -> createCertificateCancellationOperation(name),
            pollingContext -> createCertificateFetchOperation(name));
    }

    private PollResponse<CertificateOperation> createCertificateActivationOperation(String certificateName,
        CertificatePolicy policy, Boolean isEnabled, Map<String, String> tags) {

        CertificateCreateParameters certificateCreateParameters
            = new CertificateCreateParameters().setCertificatePolicy(getImplCertificatePolicy(policy))
                .setCertificateAttributes(new CertificateAttributes().setEnabled(isEnabled))
                .setTags(tags);

        try (Response<com.azure.v2.security.keyvault.certificates.implementation.models.CertificateOperation> response
            = clientImpl.createCertificateWithResponse(certificateName, certificateCreateParameters,
                RequestContext.none())) {

            return new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                createCertificateOperation(response.getValue()));
        }
    }

    private PollResponse<CertificateOperation> createCertificatePollOperation(String certificateName) {
        CertificateOperation certificateOperation = CertificateOperationHelper.createCertificateOperation(
            clientImpl.getCertificateOperationWithResponse(certificateName, RequestContext.none()).getValue());

        return new PollResponse<>(mapStatus(certificateOperation.getStatus()), certificateOperation);
    }

    private CertificateOperation createCertificateCancellationOperation(String name) {
        try (Response<com.azure.v2.security.keyvault.certificates.implementation.models.CertificateOperation> response
            = clientImpl.updateCertificateOperationWithResponse(name, new CertificateOperationUpdateParameter(true),
                RequestContext.none())) {

            return createCertificateOperation(response.getValue());
        }
    }

    private KeyVaultCertificateWithPolicy createCertificateFetchOperation(String name) {
        return createCertificateWithPolicy(
            clientImpl.getCertificateWithResponse(name, null, RequestContext.none()).getValue());
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
     * <pre>
     * KeyVaultCertificateWithPolicy certificate = certificateClient.getCertificate&#40;&quot;certificateName&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Received certificate with name %s and version %s and secret id %s%n&quot;,
     *     certificate.getProperties&#40;&#41;.getName&#40;&#41;,
     *     certificate.getProperties&#40;&#41;.getVersion&#40;&#41;, certificate.getSecretId&#40;&#41;&#41;;
     * </pre>
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
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return createCertificateWithPolicy(
            clientImpl.getCertificateWithResponse(name, "", RequestContext.none()).getValue());
    }

    /**
     * Gets information about a specific version of a given certificate. This operation requires the
     * {@code certificates/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a specific version of the certificate in the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificate#String-String -->
     * <pre>
     * KeyVaultCertificateWithPolicy returnedCertificate =
     *     certificateClient.getCertificate&#40;&quot;certificateName&quot;, &quot;certificateVersion&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Received certificate with name %s and version %s and secret id %s%n&quot;,
     *     returnedCertificate.getProperties&#40;&#41;.getName&#40;&#41;, returnedCertificate.getProperties&#40;&#41;.getVersion&#40;&#41;,
     *     returnedCertificate.getSecretId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificate#String-String -->
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
    public KeyVaultCertificateWithPolicy getCertificate(String name, String version) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return createCertificateWithPolicy(
            clientImpl.getCertificateWithResponse(name, version, RequestContext.none()).getValue());
    }

    /**
     * Gets information about the latest version of the specified certificate. This operation requires the
     * {@code certificates/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a specific version of a certificate in the key vault. Prints out details of the response returned by the
     * service and the certificate.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateWithResponse#String-RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * &#47;&#47; Passing a null or empty version retrieves the latest certificate version.
     * Response&lt;KeyVaultCertificateWithPolicy&gt; certificateWithResponse =
     *     certificateClient.getCertificateWithResponse&#40;&quot;certificateName&quot;, null, requestContext&#41;;
     * KeyVaultCertificateWithPolicy certificate = certificateWithResponse.getValue&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Received certificate with name %s and version %s and secret id %s%n&quot;,
     *     certificate.getProperties&#40;&#41;.getName&#40;&#41;, certificate.getProperties&#40;&#41;.getVersion&#40;&#41;,
     *     certificate.getSecretId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateWithResponse#String-RequestContext -->
     *
     * @param name The name of the certificate to retrieve. It is required and cannot be {@code null} or empty.
     * @param version The version of the certificate to retrieve. If this is an empty string or {@code null}, the latest
     * version will be retrieved.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue()} contains the requested certificate.
     *
     * @throws HttpResponseException If a certificate with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultCertificateWithPolicy> getCertificateWithResponse(String name, String version,
        RequestContext requestContext) {

        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return mapResponse(clientImpl.getCertificateWithResponse(name, version, requestContext),
            KeyVaultCertificateWithPolicyHelper::createCertificateWithPolicy);
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
     * <pre>
     * KeyVaultCertificateWithPolicy certificate = certificateClient.getCertificate&#40;&quot;certificateName&quot;&#41;;
     *
     * &#47;&#47; Update certificate enabled status
     * certificate.getProperties&#40;&#41;.setEnabled&#40;false&#41;;
     *
     * KeyVaultCertificate updatedCertificate = certificateClient.updateCertificateProperties&#40;certificate.getProperties&#40;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Updated Certificate with name %s and enabled status %s%n&quot;,
     *     updatedCertificate.getProperties&#40;&#41;.getName&#40;&#41;, updatedCertificate.getProperties&#40;&#41;.isEnabled&#40;&#41;&#41;;
     * </pre>
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
        Objects.requireNonNull(certificateProperties, "'certificateProperties' cannot be null.");
        if (isNullOrEmpty(certificateProperties.getName())) {
            throw LOGGER.throwableAtError()
                .log("'certificateProperties.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        CertificateAttributes certificateAttributes
            = new CertificateAttributes().setEnabled(certificateProperties.isEnabled())
                .setExpires(certificateProperties.getExpiresOn())
                .setNotBefore(certificateProperties.getNotBefore());

        CertificateUpdateParameters certificateUpdateParameters
            = new CertificateUpdateParameters().setCertificateAttributes(certificateAttributes)
                .setTags(certificateProperties.getTags());

        try (Response<CertificateBundle> response
            = clientImpl.updateCertificateWithResponse(certificateProperties.getName(), certificateUpdateParameters,
                certificateProperties.getVersion(), RequestContext.none())) {

            return createCertificateWithPolicy(response.getValue());
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
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificatePropertiesWithResponse#CertificateProperties-RequestContext -->
     * <pre>
     * KeyVaultCertificateWithPolicy certificateToUpdate = certificateClient.getCertificate&#40;&quot;certificateName&quot;&#41;;
     *
     * &#47;&#47; Update certificate enabled status
     * certificateToUpdate.getProperties&#40;&#41;.setEnabled&#40;false&#41;;
     *
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;KeyVaultCertificate&gt; updatedCertificateResponse =
     *     certificateClient.updateCertificatePropertiesWithResponse&#40;certificateToUpdate.getProperties&#40;&#41;,
     *         requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Updated Certificate with name %s and enabled status %s%n&quot;,
     *     updatedCertificateResponse.getValue&#40;&#41;.getProperties&#40;&#41;.getName&#40;&#41;,
     *     updatedCertificateResponse.getValue&#40;&#41;.getProperties&#40;&#41;.isEnabled&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificatePropertiesWithResponse#CertificateProperties-RequestContext -->
     *
     * @param certificateProperties The certificate properties to update.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue()} contains the updated certificate.
     *
     * @throws HttpResponseException If a certificate with the given {@link CertificateProperties#getName() name} and
     * {@link CertificateProperties#getVersion() version} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@link CertificateProperties#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code certificateProperties} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultCertificate> updateCertificatePropertiesWithResponse(
        CertificateProperties certificateProperties, RequestContext requestContext) {

        Objects.requireNonNull(certificateProperties, "'certificateProperties' cannot be null.");

        if (isNullOrEmpty(certificateProperties.getName())) {
            throw LOGGER.throwableAtError()
                .log("'certificateProperties.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        CertificateAttributes certificateAttributes
            = new CertificateAttributes().setEnabled(certificateProperties.isEnabled())
                .setExpires(certificateProperties.getExpiresOn())
                .setNotBefore(certificateProperties.getNotBefore());

        CertificateUpdateParameters certificateUpdateParameters
            = new CertificateUpdateParameters().setCertificateAttributes(certificateAttributes)
                .setTags(certificateProperties.getTags());

        return mapResponse(
            clientImpl.updateCertificateWithResponse(certificateProperties.getName(), certificateUpdateParameters,
                certificateProperties.getVersion(), requestContext),
            KeyVaultCertificateWithPolicyHelper::createCertificateWithPolicy);
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
     * <pre>
     * Poller&lt;DeletedCertificate, Void&gt; deleteCertPoller = null;
     *     &#47;&#47;certificateClient.beginDeleteCertificate&#40;&quot;certificateName&quot;&#41;;
     *
     * &#47;&#47; Deleted Certificate is accessible as soon as polling beings.
     * PollResponse&lt;DeletedCertificate&gt; deleteCertPollResponse = deleteCertPoller.poll&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Deleted certificate with name %s and recovery id %s%n&quot;,
     *     deleteCertPollResponse.getValue&#40;&#41;.getName&#40;&#41;, deleteCertPollResponse.getValue&#40;&#41;.getRecoveryId&#40;&#41;&#41;;
     * deleteCertPoller.waitForCompletion&#40;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.beginDeleteCertificate#String -->
     *
     * @param name The name of the certificate to be deleted.
     * @throws HttpResponseException If a certificate with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     * @return A {@link Poller} to poll on and retrieve the deleted certificate with.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<DeletedCertificate, Void> beginDeleteCertificate(String name) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return Poller.createPoller(Duration.ofSeconds(1), pollingContext -> deleteCertificateActivationOperation(name),
            pollingContext -> deleteCertificatePollOperation(name, pollingContext.getLatestResponse()),
            (pollingContext, pollResponse) -> null, pollingContext -> null);
    }

    private PollResponse<DeletedCertificate> deleteCertificateActivationOperation(String name) {
        try (Response<DeletedCertificateBundle> response
            = clientImpl.deleteCertificateWithResponse(name, RequestContext.none())) {

            return new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                createDeletedCertificate(response.getValue()));
        }
    }

    private PollResponse<DeletedCertificate> deleteCertificatePollOperation(String name,
        PollResponse<DeletedCertificate> latestPollResponse) {

        try {
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, createDeletedCertificate(
                clientImpl.getDeletedCertificateWithResponse(name, RequestContext.none()).getValue()));
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, latestPollResponse.getValue());
            } else {
                // This means either vault has soft-delete disabled or permission is not granted for the get deleted
                // certificate operation. In both cases deletion operation was successful when activation operation
                // succeeded before reaching here.
                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    latestPollResponse.getValue());
            }
        } catch (RuntimeException e) {
            // This means either vault has soft-delete disabled or permission is not granted for the get deleted
            // certificate operation. In both cases deletion operation was successful when activation operation
            // succeeded before reaching here.
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, latestPollResponse.getValue());
        }
    }

    /**
     * Gets information about a deleted certificate. This operation is applicable for soft-delete enabled vaults and
     * requires the {@code certificates/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a deleted certificate from the key vault enabled for soft-delete and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getDeletedCertificate#String -->
     * <pre>
     * DeletedCertificate deletedCertificate = certificateClient.getDeletedCertificate&#40;&quot;certificateName&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Deleted certificate with name %s and recovery id %s%n&quot;, deletedCertificate.getName&#40;&#41;,
     *     deletedCertificate.getRecoveryId&#40;&#41;&#41;;
     * </pre>
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
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return createDeletedCertificate(
            clientImpl.getDeletedCertificateWithResponse(name, RequestContext.none()).getValue());
    }

    /**
     * Gets information about a deleted certificate. This operation is applicable for soft-delete enabled vaults and
     * requires the {@code certificates/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a deleted certificate from the key vault enabled for soft-delete. Prints details of the response returned
     * by the service and the deleted certificate.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getDeletedCertificateWithResponse#String-RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;DeletedCertificate&gt; deletedCertificateWithResponse =
     *     certificateClient.getDeletedCertificateWithResponse&#40;&quot;certificateName&quot;, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Deleted certificate with name %s and recovery id %s%n&quot;,
     *     deletedCertificateWithResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *     deletedCertificateWithResponse.getValue&#40;&#41;.getRecoveryId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getDeletedCertificateWithResponse#String-RequestContext -->
     *
     * @param name The name of the deleted certificate to retrieve. It is required and cannot be {@code null} or empty.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue()} contains the deleted certificate.
     *
     * @throws HttpResponseException If a certificate with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DeletedCertificate> getDeletedCertificateWithResponse(String name, RequestContext requestContext) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return mapResponse(clientImpl.getDeletedCertificateWithResponse(name, requestContext),
            DeletedCertificateHelper::createDeletedCertificate);
    }

    /**
     * Permanently removes a deleted certificate without the possibility of recovery. This operation can only be
     * performed on a key vault <b>enabled for soft-delete</b> and requires the {@code certificates/purge} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Purges a deleted certificate from a key vault enabled for soft-delete.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.purgeDeletedCertificate#String -->
     * <pre>
     * certificateClient.purgeDeletedCertificate&#40;&quot;certificateName&quot;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.purgeDeletedCertificate#String -->
     *
     * @param name The name of the deleted certificate to purge. It is required and cannot be {@code null} or empty.
     *
     * @throws HttpResponseException If a certificate with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void purgeDeletedCertificate(String name) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        clientImpl.purgeDeletedCertificateWithResponse(name, RequestContext.none());
    }

    /**
     * Permanently removes a deleted certificate without the possibility of recovery. This operation can only be
     * performed on a key vault <b>enabled for soft-delete</b> and requires the {@code certificates/purge} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Purges a deleted certificate from a key vault enabled for soft-delete and prints out details of the response
     * returned by the service.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.purgeDeletedCertificateWithResponse#String-RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;Void&gt; purgeResponse =
     *     certificateClient.purgeDeletedCertificateWithResponse&#40;&quot;certificateName&quot;, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Purged Deleted certificate with status %d%n&quot;, purgeResponse.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.purgeDeletedCertificateWithResponse#String-RequestContext -->
     *
     * @param name The name of the deleted certificate to purge. It is required and cannot be {@code null} or empty.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object containing the status code and HTTP headers related to the operation.
     *
     * @throws HttpResponseException If a certificate with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> purgeDeletedCertificateWithResponse(String name, RequestContext requestContext) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return clientImpl.purgeDeletedCertificateWithResponse(name, requestContext);
    }

    /**
     * Recovers a deleted certificate back to its latest version. This operation can only be performed on a key vault
     * <b>enabled for soft-delete</b> and requires the {@code certificates/recover} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recovers a deleted certificate from a key vault enabled for soft-delete and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.beginRecoverDeletedCertificate#String -->
     * <pre>
     * Poller&lt;KeyVaultCertificateWithPolicy, Void&gt; recoverDeletedCertPoller = null;
     *     &#47;&#47;certificateClient.beginRecoverDeletedCertificate&#40;&quot;deletedCertificateName&quot;&#41;;
     *
     * &#47;&#47; Recovered certificate is accessible as soon as polling beings
     * PollResponse&lt;KeyVaultCertificateWithPolicy&gt; recoverDeletedCertPollResponse = recoverDeletedCertPoller.poll&#40;&#41;;
     *
     * System.out.printf&#40;&quot; Recovered Deleted certificate with name %s and id %s%n&quot;,
     *     recoverDeletedCertPollResponse.getValue&#40;&#41;.getProperties&#40;&#41;.getName&#40;&#41;,
     *     recoverDeletedCertPollResponse.getValue&#40;&#41;.getProperties&#40;&#41;.getId&#40;&#41;&#41;;
     *
     * recoverDeletedCertPoller.waitForCompletion&#40;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.beginRecoverDeletedCertificate#String -->
     *
     * @param name The name of the deleted certificate to recover. It is required and cannot be {@code null} or empty.
     * @throws HttpResponseException If a certificate with the given {@code name} doesn't exist in the
     * certificate vault.
     * @return A {@link Poller} to poll on and retrieve the recovered certificate with.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<KeyVaultCertificateWithPolicy, Void> beginRecoverDeletedCertificate(String name) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return Poller.createPoller(Duration.ofSeconds(1), pollingContext -> recoverCertificateActivationOperation(name),
            pollingContext -> recoverDeletedCertificatePollOperation(name, pollingContext.getLatestResponse()),
            (pollingContext, firstResponse) -> null, pollingContext -> null);
    }

    private PollResponse<KeyVaultCertificateWithPolicy> recoverCertificateActivationOperation(String name) {
        try (Response<CertificateBundle> response
            = clientImpl.recoverDeletedCertificateWithResponse(name, RequestContext.none())) {

            return new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                createCertificateWithPolicy(response.getValue()));
        }
    }

    private PollResponse<KeyVaultCertificateWithPolicy> recoverDeletedCertificatePollOperation(String certificateName,
        PollResponse<KeyVaultCertificateWithPolicy> latestPollResponse) {

        try {
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, createCertificateWithPolicy(
                clientImpl.getCertificateWithResponse(certificateName, "", RequestContext.none()).getValue()));
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() == 404) {
                return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, latestPollResponse.getValue());
            } else {
                // This means permission is not granted for the get deleted certificate operation.
                // In both cases deletion operation was successful when activation operation succeeded before
                // reaching here.
                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    latestPollResponse.getValue());
            }
        } catch (RuntimeException e) {
            // This means permission is not granted for the get deleted certificate operation.
            // In both cases deletion operation was successful when activation operation succeeded before reaching
            // here.
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, latestPollResponse.getValue());
        }
    }

    /**
     * Requests a backup of the certificate be downloaded. All versions of the certificate will be downloaded. This
     * operation requires the {@code certificates/backup} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Backs up a certificate from the key vault and prints out the length of the certificate's backup blob.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.backupCertificate#String -->
     * <pre>
     * byte[] certificateBackup = certificateClient.backupCertificate&#40;&quot;certificateName&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Backed up certificate with back up blob length %d%n&quot;, certificateBackup.length&#41;;
     * </pre>
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
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        try (Response<BackupCertificateResult> response
            = clientImpl.backupCertificateWithResponse(name, RequestContext.none())) {

            return response.getValue().getValue();
        }
    }

    /**
     * Requests a backup of the certificate be downloaded. All versions of the certificate will be downloaded. This
     * operation requires the {@code certificates/backup} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Backs up a certificate from the key vault. Prints out details of the response returned by the service and the
     * length of the certificate's backup blob.
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.backupCertificateWithResponse#String-RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;byte[]&gt; certificateBackupWithResponse =
     *     certificateClient.backupCertificateWithResponse&#40;&quot;certificateName&quot;, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Backed up certificate with back up blob length %d%n&quot;,
     *     certificateBackupWithResponse.getValue&#40;&#41;.length&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.backupCertificateWithResponse#String-RequestContext -->
     *
     * @param name The name of the certificate to back up.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue()} contains the backed up certificate blob.
     *
     * @throws HttpResponseException If a certificate with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<byte[]> backupCertificateWithResponse(String name, RequestContext requestContext) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return mapResponse(clientImpl.backupCertificateWithResponse(name, requestContext),
            BackupCertificateResult::getValue);
    }

    /**
     * Restores a backed up certificate and all its versions to a vault. All versions of the certificate are restored to
     * the vault. This operation requires the {@code certificates/restore} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Restores a certificate in the key vault from a backup and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.restoreCertificateBackup#byte -->
     * <pre>
     * byte[] certificateBackupBlob = &#123;&#125;;
     *
     * KeyVaultCertificate certificate = certificateClient.restoreCertificateBackup&#40;certificateBackupBlob&#41;;
     *
     * System.out.printf&#40;&quot; Restored certificate with name %s and id %s%n&quot;,
     *     certificate.getProperties&#40;&#41;.getName&#40;&#41;, certificate.getProperties&#40;&#41;.getId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.restoreCertificateBackup#byte -->
     *
     * @param backup The backup blob associated with the certificate.
     * @return The restored certificate.
     *
     * @throws HttpResponseException If the {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultCertificateWithPolicy restoreCertificateBackup(byte[] backup) {
        try (Response<CertificateBundle> response = clientImpl
            .restoreCertificateWithResponse(new CertificateRestoreParameters(backup), RequestContext.none())) {

            return createCertificateWithPolicy(response.getValue());
        }
    }

    /**
     * Restores a backed up certificate and all its versions to a vault. All versions of the certificate are restored to
     * the vault. This operation requires the {@code certificates/restore} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Restores a certificate in the key vault from a backup. Prints our details of the response returned by the
     * service and the restored certificate.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.restoreCertificateBackupWithResponse#byte-RequestContext -->
     * <pre>
     * byte[] certificateBackupBlobArray = &#123;&#125;;
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;KeyVaultCertificateWithPolicy&gt; certificateResponse =
     *     certificateClient.restoreCertificateBackupWithResponse&#40;certificateBackupBlobArray, requestContext&#41;;
     *
     * System.out.printf&#40;&quot; Restored certificate with name %s and id %s%n&quot;,
     *     certificateResponse.getValue&#40;&#41;.getProperties&#40;&#41;.getName&#40;&#41;,
     *     certificateResponse.getValue&#40;&#41;.getProperties&#40;&#41;.getId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.restoreCertificateBackupWithResponse#byte-RequestContext -->
     *
     * @param backup The backup blob associated with the certificate.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue()} contains the restored certificate.
     *
     * @throws HttpResponseException If the {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultCertificateWithPolicy> restoreCertificateBackupWithResponse(byte[] backup,
        RequestContext requestContext) {

        return mapResponse(
            clientImpl.restoreCertificateWithResponse(new CertificateRestoreParameters(backup), requestContext),
            KeyVaultCertificateWithPolicyHelper::createCertificateWithPolicy);
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
     * <pre>
     * certificateClient.listPropertiesOfCertificates&#40;&#41;.forEach&#40;certificateProperties -&gt; &#123;
     *     KeyVaultCertificateWithPolicy certificateWithAllProperties =
     *         certificateClient.getCertificate&#40;certificateProperties.getName&#40;&#41;, certificateProperties.getVersion&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot;Received certificate with name '%s' and secret id '%s'%n&quot;,
     *         certificateWithAllProperties.getProperties&#40;&#41;.getName&#40;&#41;,
     *         certificateWithAllProperties.getSecretId&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificates -->
     *
     * <p><strong>Iterate through certificates by page</strong></p>
     * <p>Iterates through the certificates in the key vault by page and gets each one's latest version and their
     * policies by looping though the properties objects and calling {@link CertificateClient#getCertificate(String)}.
     * </p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificates.iterableByPage -->
     * <pre>
     * certificateClient.listPropertiesOfCertificates&#40;&#41;.iterableByPage&#40;&#41;.forEach&#40;pagedResponse -&gt; &#123;
     *     pagedResponse.getValue&#40;&#41;.forEach&#40;certificateProperties -&gt; &#123;
     *         KeyVaultCertificateWithPolicy certificateWithAllProperties =
     *             certificateClient.getCertificate&#40;certificateProperties.getName&#40;&#41;, certificateProperties.getVersion&#40;&#41;&#41;;
     *
     *         System.out.printf&#40;&quot;Received certificate with name '%s' and secret id '%s'%n&quot;,
     *             certificateWithAllProperties.getProperties&#40;&#41;.getName&#40;&#41;,
     *             certificateWithAllProperties.getSecretId&#40;&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificates.iterableByPage -->
     *
     * @return A {@link PagedIterable} of properties objects of all the certificates in the vault. A properties object
     * contains all information about the certificate, except its key material.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateProperties> listPropertiesOfCertificates() {
        return listPropertiesOfCertificates(false, RequestContext.none());
    }

    /**
     * Lists all certificates in the key vault. Each certificate is represented by a properties object containing the
     * certificate identifier, thumbprint, and attributes. The policy and individual versions are not included in the
     * response. This operation requires the {@code certificates/list} permission.
     *
     * <p><strong>Iterate through certificates</strong></p>
     * <p>Lists the certificates in the key vault and gets each one's latest version and their policies by looping
     * though the properties objects and calling {@link CertificateClient#getCertificate(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificates#boolean-RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * certificateClient.listPropertiesOfCertificates&#40;true, requestContext&#41;.forEach&#40;certificateProperties -&gt; &#123;
     *     KeyVaultCertificateWithPolicy certificateWithAllProperties =
     *         certificateClient.getCertificate&#40;certificateProperties.getName&#40;&#41;, certificateProperties.getVersion&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot;Received certificate with name '%s' and secret id '%s'%n&quot;,
     *         certificateWithAllProperties.getProperties&#40;&#41;.getName&#40;&#41;,
     *         certificateWithAllProperties.getSecretId&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificates#RequestContext -->
     *
     * <p><strong>Iterate through certificates by page</strong></p>
     * <p>Iterates through the certificates in the key vault by page and gets each one's latest version and their
     * policies by looping though the properties objects and calling {@link CertificateClient#getCertificate(String)}.
     * </p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificates.iterableByPage#boolean-RequestContext -->
     * <pre>
     * RequestContext reqContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * certificateClient.listPropertiesOfCertificates&#40;true, reqContext&#41;.iterableByPage&#40;&#41;.forEach&#40;pagedResponse -&gt; &#123;
     *     pagedResponse.getValue&#40;&#41;.forEach&#40;certificateProperties -&gt; &#123;
     *         KeyVaultCertificateWithPolicy certificateWithAllProperties =
     *             certificateClient.getCertificate&#40;certificateProperties.getName&#40;&#41;, certificateProperties.getVersion&#40;&#41;&#41;;
     *
     *         System.out.printf&#40;&quot;Received certificate with name '%s' and secret id '%s'%n&quot;,
     *             certificateWithAllProperties.getProperties&#40;&#41;.getName&#40;&#41;,
     *             certificateWithAllProperties.getSecretId&#40;&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificates.iterableByPage#boolean-RequestContext -->
     *
     * @param includePending Indicate if pending certificates should be included in the results.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} of properties objects of all the certificates in the vault. A properties object
     * contains all information about the certificate, except its key material.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateProperties> listPropertiesOfCertificates(boolean includePending,
        RequestContext requestContext) {

        return mapPages(pagingOptions -> clientImpl.getCertificatesSinglePage(null, includePending),
            (pagingOptions, nextLink) -> clientImpl.getCertificatesNextSinglePage(nextLink, requestContext),
            CertificatePropertiesHelper::createCertificateProperties);
    }

    /**
     * Lists all deleted certificates in the key vault currently available for recovery. This operation is applicable
     * for key vaults <b>enabled for soft-delete</b> and requires the {@code certificates/list} permission.
     *
     * <p><strong>Iterate through deleted certificates</strong></p>
     * <p>Lists the deleted certificates in a key vault enabled for soft-delete and prints out each one's recovery id.
     * </p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates -->
     * <pre>
     * certificateClient.listDeletedCertificates&#40;&#41;.forEach&#40;deletedCertificate -&gt; &#123;
     *     System.out.printf&#40;&quot;Deleted certificate's recovery Id %s%n&quot;, deletedCertificate.getRecoveryId&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates -->
     *
     * <p><strong>Iterate through deleted certificates by page</strong></p>
     * <p>Iterates through the deleted certificates by page in the key vault by page and prints out each one's recovery
     * id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates.iterableByPage -->
     * <pre>
     * certificateClient.listDeletedCertificates&#40;&#41;.iterableByPage&#40;&#41;.forEach&#40;pagedResponse -&gt; &#123;
     *     pagedResponse.getValue&#40;&#41;.forEach&#40;deletedCertificate -&gt; &#123;
     *         System.out.printf&#40;&quot;Deleted certificate's recovery Id %s%n&quot;, deletedCertificate.getRecoveryId&#40;&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates.iterableByPage -->
     *
     * @return A {@link PagedIterable} of the deleted certificates in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedCertificate> listDeletedCertificates() {
        return listDeletedCertificates(false, RequestContext.none());
    }

    /**
     * Lists all deleted certificates in the key vault currently available for recovery. This operation is applicable
     * for key vaults <b>enabled for soft-delete</b> and requires the {@code certificates/list} permission.
     *
     * <p><strong>Iterate through deleted certificates</strong></p>
     * <p>Lists the deleted certificates in a key vault enabled for soft-delete and prints out each one's recovery id.
     * </p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates#boolean-RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * certificateClient.listDeletedCertificates&#40;true, requestContext&#41;.forEach&#40;deletedCertificate -&gt; &#123;
     *     System.out.printf&#40;&quot;Deleted certificate's recovery Id %s%n&quot;, deletedCertificate.getRecoveryId&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates#boolean-RequestContext -->
     *
     * <p><strong>Iterate through deleted certificates by page</strong></p>
     * <p>Iterates through the deleted certificates by page in the key vault by page and prints out each one's recovery
     * id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates.iterableByPage#boolean-RequestContext -->
     * <pre>
     * RequestContext reqContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * certificateClient.listDeletedCertificates&#40;true, reqContext&#41;.iterableByPage&#40;&#41;.forEach&#40;pagedResponse -&gt; &#123;
     *     pagedResponse.getValue&#40;&#41;.forEach&#40;deletedCertificate -&gt; &#123;
     *         System.out.printf&#40;&quot;Deleted certificate's recovery Id %s%n&quot;, deletedCertificate.getRecoveryId&#40;&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates.iterableByPage#boolean-RequestContext -->
     *
     * @param includePending Indicate if pending certificates should be included in the results.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} of the deleted certificates in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedCertificate> listDeletedCertificates(boolean includePending,
        RequestContext requestContext) {

        return mapPages(pagingOptions -> clientImpl.getDeletedCertificatesSinglePage(null, includePending),
            (pagingOptions, nextLink) -> clientImpl.getDeletedCertificatesNextSinglePage(nextLink, requestContext),
            DeletedCertificateHelper::createDeletedCertificate);
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
     * <pre>
     * certificateClient.listPropertiesOfCertificateVersions&#40;&quot;certificateName&quot;&#41;.forEach&#40;certificateProperties -&gt; &#123;
     *     KeyVaultCertificateWithPolicy certificateWithAllProperties =
     *         certificateClient.getCertificate&#40;certificateProperties.getName&#40;&#41;, certificateProperties.getVersion&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot;Received certificate's version with name %s, version %s and secret id %s%n&quot;,
     *         certificateWithAllProperties.getProperties&#40;&#41;.getName&#40;&#41;,
     *         certificateWithAllProperties.getProperties&#40;&#41;.getVersion&#40;&#41;, certificateWithAllProperties.getSecretId&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificateVersions#String -->
     *
     * <p><strong>Iterate through certificates versions by page</strong></p>
     * <p>Iterates through the versions of a certificate in the key vault by page and gets each one's latest version and
     * their policies by looping though the properties objects and calling
     * {@link CertificateClient#getCertificate(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificateVersions.iterableByPage#String -->
     * <pre>
     * certificateClient.listPropertiesOfCertificateVersions&#40;&quot;certificateName&quot;&#41;.iterableByPage&#40;&#41;.forEach&#40;
     *     pagedResponse -&gt; &#123;
     *         pagedResponse.getValue&#40;&#41;.forEach&#40;certificateProperties -&gt; &#123;
     *             KeyVaultCertificateWithPolicy certificateWithAllProperties =
     *                 certificateClient.getCertificate&#40;certificateProperties.getName&#40;&#41;, certificateProperties.getVersion&#40;&#41;&#41;;
     *
     *             System.out.printf&#40;&quot;Received certificate's version with name %s, version %s and secret id %s%n&quot;,
     *                 certificateWithAllProperties.getProperties&#40;&#41;.getName&#40;&#41;,
     *                 certificateWithAllProperties.getProperties&#40;&#41;.getVersion&#40;&#41;, certificateWithAllProperties.getSecretId&#40;&#41;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
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
        return listPropertiesOfCertificateVersions(name, RequestContext.none());
    }

    /**
     * Lists all versions of the specified certificate in the key vault. Each certificate is represented by a properties
     * object containing the certificate identifier, thumbprint, and attributes. The policy and individual versions are
     * not included in the response. This operation requires the {@code certificates/list} permission.
     *
     * <p><strong>Iterate through certificates versions</strong></p>
     * <p>Lists the versions of a certificate in the key vault and gets each one's latest version and their policies by
     * looping though the properties objects and calling {@link CertificateClient#getCertificate(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificateVersions#String-RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * certificateClient.listPropertiesOfCertificateVersions&#40;&quot;certificateName&quot;, requestContext&#41;.forEach&#40;
     *     certificateProperties -&gt; &#123;
     *         KeyVaultCertificateWithPolicy certificateWithAllProperties =
     *             certificateClient.getCertificate&#40;certificateProperties.getName&#40;&#41;, certificateProperties.getVersion&#40;&#41;&#41;;
     *
     *         System.out.printf&#40;&quot;Received certificate's version with name %s, version %s and secret id %s%n&quot;,
     *             certificateWithAllProperties.getProperties&#40;&#41;.getName&#40;&#41;,
     *             certificateWithAllProperties.getProperties&#40;&#41;.getVersion&#40;&#41;, certificateWithAllProperties.getSecretId&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificateVersions#String-RequestContext -->
     *
     * <p><strong>Iterate through certificates versions by page</strong></p>
     * <p>Iterates through the versions of a certificate in the key vault by page and gets each one's latest version and
     * their policies by looping though the properties objects and calling
     * {@link CertificateClient#getCertificate(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificateVersions.iterableByPage#String-RequestContext -->
     * <pre>
     * RequestContext reqContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * certificateClient.listPropertiesOfCertificateVersions&#40;&quot;certificateName&quot;, reqContext&#41;.iterableByPage&#40;&#41;
     *     .forEach&#40;pagedResponse -&gt; &#123;
     *         pagedResponse.getValue&#40;&#41;.forEach&#40;certificateProperties -&gt; &#123;
     *             KeyVaultCertificateWithPolicy certificateWithAllProperties =
     *                 certificateClient.getCertificate&#40;certificateProperties.getName&#40;&#41;, certificateProperties.getVersion&#40;&#41;&#41;;
     *
     *             System.out.printf&#40;&quot;Received certificate's version with name %s, version %s and secret id %s%n&quot;,
     *                 certificateWithAllProperties.getProperties&#40;&#41;.getName&#40;&#41;,
     *                 certificateWithAllProperties.getProperties&#40;&#41;.getVersion&#40;&#41;, certificateWithAllProperties.getSecretId&#40;&#41;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfCertificateVersions.iterableByPage#String-RequestContext -->
     *
     * @param name The name of the certificate. It is required and cannot be {@code null} or empty.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} of properties objects of all the versions of the specified certificate. A
     * properties object contains all information about the certificate, except its key material. The
     * {@link PagedIterable} will be empty if no certificate with the given {@code name} exists in key vault.
     *
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateProperties> listPropertiesOfCertificateVersions(String name,
        RequestContext requestContext) {

        return mapPages(pagingOptions -> clientImpl.getCertificateVersionsSinglePage(name, null),
            (pagingOptions, nextLink) -> clientImpl.getCertificateVersionsNextSinglePage(nextLink, requestContext),
            CertificatePropertiesHelper::createCertificateProperties);
    }

    /**
     * Retrieves the policy of a certificate in the key vault. This operation requires the {@code certificates/get}
     * permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the policy of a certificate in the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificatePolicy#String -->
     * <pre>
     * CertificatePolicy policy = certificateClient.getCertificatePolicy&#40;&quot;certificateName&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Received policy with subject name %s%n&quot;, policy.getSubject&#40;&#41;&#41;;
     * </pre>
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
        if (isNullOrEmpty(certificateName)) {
            throw LOGGER.throwableAtError()
                .log("'certificateName' cannot be null or empty.", IllegalArgumentException::new);
        }

        return createCertificatePolicy(
            clientImpl.getCertificatePolicyWithResponse(certificateName, RequestContext.none()).getValue());
    }

    /**
     * Retrieves the policy of a certificate in the key vault. This operation requires the {@code certificates/get}
     * permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the policy of a certificate in the key vault. Prints out details of the response returned by the service
     * and the requested certificate policy.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificatePolicyWithResponse#String -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;CertificatePolicy&gt; returnedPolicyWithResponse =
     *     certificateClient.getCertificatePolicyWithResponse&#40;&quot;certificateName&quot;, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Received policy with subject name %s%n&quot;,
     *     returnedPolicyWithResponse.getValue&#40;&#41;.getSubject&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificatePolicyWithResponse#String -->
     *
     * @param certificateName The name of the certificate whose policy is to be retrieved. It is required and cannot be
     * {@code null} or empty.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue()} contains the requested certificate policy.
     *
     * @throws HttpResponseException If a certificate with the given {@code certificateName} doesn't exist in the key
     * vault.
     * @throws IllegalArgumentException If the provided {@code certificateName} is {@code null} or an empty
     * string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificatePolicy> getCertificatePolicyWithResponse(String certificateName,
        RequestContext requestContext) {

        if (isNullOrEmpty(certificateName)) {
            throw LOGGER.throwableAtError()
                .log("'certificateName' cannot be null or empty.", IllegalArgumentException::new);
        }

        return mapResponse(clientImpl.getCertificatePolicyWithResponse(certificateName, requestContext),
            CertificatePolicyHelper::createCertificatePolicy);
    }

    /**
     * Updates the policy for a certificate. Policy attributes that are not specified in the request are left unchanged.
     * This operation requires the {@code certificates/update} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the certificate policy and updates its properties in the key vault, then prints out the updated policy's
     * details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificatePolicy#String -->
     * <pre>
     * CertificatePolicy certificatePolicy = certificateClient.getCertificatePolicy&#40;&quot;certificateName&quot;&#41;;
     *
     * &#47;&#47;Update the certificate policy cert transparency property.
     * certificatePolicy.setCertificateTransparent&#40;true&#41;;
     *
     * CertificatePolicy updatedCertPolicy =
     *     certificateClient.updateCertificatePolicy&#40;&quot;certificateName&quot;, certificatePolicy&#41;;
     *
     * System.out.printf&#40;&quot;Updated Certificate Policy transparency status %s%n&quot;,
     *     updatedCertPolicy.isCertificateTransparent&#40;&#41;&#41;;
     * </pre>
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
        if (isNullOrEmpty(certificateName)) {
            throw LOGGER.throwableAtError()
                .log("'certificateName' cannot be null or empty.", IllegalArgumentException::new);
        }

        Objects.requireNonNull(policy, "'policy' cannot be null.");

        try (Response<com.azure.v2.security.keyvault.certificates.implementation.models.CertificatePolicy> response
            = clientImpl.updateCertificatePolicyWithResponse(certificateName, getImplCertificatePolicy(policy),
                RequestContext.none())) {

            return createCertificatePolicy(response.getValue());
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
     * <pre>
     * CertificatePolicy certificatePolicyToUpdate = certificateClient.getCertificatePolicy&#40;&quot;certificateName&quot;&#41;;
     *
     * &#47;&#47;Update the certificate policy cert transparency property.
     * certificatePolicyToUpdate.setCertificateTransparent&#40;true&#41;;
     *
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;CertificatePolicy&gt; updatedCertPolicyWithResponse =
     *     certificateClient.updateCertificatePolicyWithResponse&#40;&quot;certificateName&quot;,
     *         certificatePolicyToUpdate, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Updated Certificate Policy transparency status %s%n&quot;, updatedCertPolicyWithResponse
     *     .getValue&#40;&#41;.isCertificateTransparent&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificatePolicyWithResponse#String -->
     *
     * @param certificateName The name of the certificate whose policy is to be updated. It is required and cannot be
     * {@code null} or empty.
     * @param policy The certificate policy to be updated. It is required and cannot be {@code null}.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
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
        CertificatePolicy policy, RequestContext requestContext) {

        if (isNullOrEmpty(certificateName)) {
            throw LOGGER.throwableAtError()
                .log("'certificateName' cannot be null or empty.", IllegalArgumentException::new);
        }

        Objects.requireNonNull(policy, "'policy' cannot be null.");

        return mapResponse(clientImpl.updateCertificatePolicyWithResponse(certificateName,
            getImplCertificatePolicy(policy), requestContext), CertificatePolicyHelper::createCertificatePolicy);
    }

    /**
     * Creates a certificate issuer. This operation updates the specified certificate issuer if it already exists and
     * requires the {@code certificates/setissuers} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new certificate issuer in the key vault and prints out the created issuer's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.createIssuer#CertificateIssuer -->
     * <pre>
     * CertificateIssuer issuerToCreate = new CertificateIssuer&#40;&quot;myissuer&quot;, &quot;myProvider&quot;&#41;
     *     .setAccountId&#40;&quot;testAccount&quot;&#41;
     *     .setAdministratorContacts&#40;Collections.singletonList&#40;new AdministratorContact&#40;&#41;.setFirstName&#40;&quot;test&quot;&#41;
     *         .setLastName&#40;&quot;name&quot;&#41;.setEmail&#40;&quot;test&#64;example.com&quot;&#41;&#41;&#41;;
     *
     * CertificateIssuer returnedIssuer = certificateClient.createIssuer&#40;issuerToCreate&#41;;
     *
     * System.out.printf&#40;&quot;Created Issuer with name %s provider %s%n&quot;, returnedIssuer.getName&#40;&#41;,
     *     returnedIssuer.getProvider&#40;&#41;&#41;;
     * </pre>
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
        Objects.requireNonNull(issuer, "'issuer' cannot be null.");

        IssuerBundle issuerBundle = getIssuerBundle(issuer);
        CertificateIssuerSetParameters certificateIssuerSetParameters
            = new CertificateIssuerSetParameters(issuerBundle.getProvider())
                .setOrganizationDetails(issuerBundle.getOrganizationDetails())
                .setCredentials(issuerBundle.getCredentials())
                .setAttributes(issuerBundle.getAttributes());

        try (Response<IssuerBundle> response = clientImpl.setCertificateIssuerWithResponse(issuer.getName(),
            certificateIssuerSetParameters, RequestContext.none())) {

            return createCertificateIssuer(response.getValue());
        }
    }

    /**
     * Creates a certificate issuer. This operation updates the specified certificate issuer if it already exists and
     * requires the {@code certificates/setissuers} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new certificate issuer in the key vault. Prints out details of the response returned by the
     * service and the created issuer.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.createIssuerWithResponse#CertificateIssuer-RequestContext -->
     * <pre>
     * CertificateIssuer issuer = new CertificateIssuer&#40;&quot;issuerName&quot;, &quot;myProvider&quot;&#41;
     *     .setAccountId&#40;&quot;testAccount&quot;&#41;
     *     .setAdministratorContacts&#40;Collections.singletonList&#40;
     *         new AdministratorContact&#40;&#41;
     *             .setFirstName&#40;&quot;test&quot;&#41;
     *             .setLastName&#40;&quot;name&quot;&#41;
     *             .setEmail&#40;&quot;test&#64;example.com&quot;&#41;&#41;&#41;;
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;CertificateIssuer&gt; issuerResponse = certificateClient.createIssuerWithResponse&#40;issuer, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Created Issuer with name %s provider %s%n&quot;, issuerResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *     issuerResponse.getValue&#40;&#41;.getProvider&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.createIssuerWithResponse#CertificateIssuer-RequestContext -->
     *
     * @param issuer The configuration of the certificate issuer to be created. It is required and cannot be
     * {@code null}.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue()} contains the created certificate issuer.
     *
     * @throws HttpResponseException If a certificate with the given {@code certificateName} doesn't exist in the key
     * vault.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificateIssuer> createIssuerWithResponse(CertificateIssuer issuer,
        RequestContext requestContext) {

        Objects.requireNonNull(issuer, "'issuer' cannot be null.");

        IssuerBundle issuerBundle = getIssuerBundle(issuer);
        CertificateIssuerSetParameters certificateIssuerSetParameters
            = new CertificateIssuerSetParameters(issuerBundle.getProvider())
                .setOrganizationDetails(issuerBundle.getOrganizationDetails())
                .setCredentials(issuerBundle.getCredentials())
                .setAttributes(issuerBundle.getAttributes());

        return mapResponse(clientImpl.setCertificateIssuerWithResponse(issuer.getName(), certificateIssuerSetParameters,
            requestContext), CertificateIssuerHelper::createCertificateIssuer);
    }

    /**
     * Retrieves a certificate issuer from the key vault. This operation requires the
     * {@code certificates/manageissuers/getissuers} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a specific certificate issuer in the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getIssuer#String -->
     * <pre>
     * CertificateIssuer returnedIssuer = certificateClient.getIssuer&#40;&quot;issuerName&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Retrieved issuer with name %s and provider %s%n&quot;, returnedIssuer.getName&#40;&#41;,
     *     returnedIssuer.getProvider&#40;&#41;&#41;;
     * </pre>
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
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return createCertificateIssuer(
            clientImpl.getCertificateIssuerWithResponse(name, RequestContext.none()).getValue());
    }

    /**
     * Retrieves a certificate issuer from the key vault. This operation requires the
     * {@code certificates/manageissuers/getissuers} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a specific certificate issuer in the key vault. Prints out details of the response returned by the
     * service and the requested certificate issuer.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getIssuerWithResponse#String-RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;CertificateIssuer&gt; issuerResponse =
     *     certificateClient.getIssuerWithResponse&#40;&quot;issuerName&quot;, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Retrieved issuer with name %s and provider %s%n&quot;, issuerResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *     issuerResponse.getValue&#40;&#41;.getProvider&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.getIssuer#String-RequestContext -->
     *
     * @param name The name of the certificate issuer to retrieve. It is required and cannot be {@code null} or empty.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue()} contains the requested certificate issuer.
     *
     * @throws HttpResponseException If a certificate issuer with the given {@code name} doesn't exist in the key
     * vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificateIssuer> getIssuerWithResponse(String name, RequestContext requestContext) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return mapResponse(clientImpl.getCertificateIssuerWithResponse(name, requestContext),
            CertificateIssuerHelper::createCertificateIssuer);
    }

    /**
     * Deletes a certificate issuer from the key vault. This operation requires the
     * {@code certificates/manageissuers/deleteissuers} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes the certificate issuer in the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.deleteIssuer#String -->
     * <pre>
     * CertificateIssuer deletedIssuer = certificateClient.deleteIssuer&#40;&quot;issuerName&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Deleted certificate issuer with name %s and provider id %s%n&quot;, deletedIssuer.getName&#40;&#41;,
     *     deletedIssuer.getProvider&#40;&#41;&#41;;
     * </pre>
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
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        try (Response<IssuerBundle> response
            = clientImpl.deleteCertificateIssuerWithResponse(name, RequestContext.none())) {

            return createCertificateIssuer(response.getValue());
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
     * <pre>
     * CertificateIssuer deletedIssuer = certificateClient.deleteIssuer&#40;&quot;issuerName&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Deleted certificate issuer with name %s and provider id %s%n&quot;, deletedIssuer.getName&#40;&#41;,
     *     deletedIssuer.getProvider&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.deleteIssuer#String -->
     *
     * @param name The name of the certificate issuer to be deleted. It is required and cannot be {@code null} or empty.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return The deleted certificate issuer.
     *
     * @throws HttpResponseException If a certificate issuer with the given {@code name} doesn't exist in the key
     * vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificateIssuer> deleteIssuerWithResponse(String name, RequestContext requestContext) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return mapResponse(clientImpl.deleteCertificateIssuerWithResponse(name, requestContext),
            CertificateIssuerHelper::createCertificateIssuer);
    }

    /**
     * Lists all the certificate issuers in the key vault. Each issuer is represented by a properties object containing
     * the certificate issuer identifier and provider. This operation requires the
     * {@code certificates/manageissuers/getissuers} permission.
     *
     * <p>><strong>Iterate through certificate issuers</strong></p>
     * <p>Lists the certificate issuers in the key vault and prints out each issuer's name and provider.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers -->
     * <pre>
     * certificateClient.listPropertiesOfIssuers&#40;&#41;.forEach&#40;issuer -&gt; &#123;
     *     CertificateIssuer retrievedIssuer = certificateClient.getIssuer&#40;issuer.getName&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot;Received issuer with name %s and provider %s%n&quot;, retrievedIssuer.getName&#40;&#41;,
     *         retrievedIssuer.getProvider&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers -->
     *
     * <p><strong>Iterate through certificate issuers by page</strong></p>
     * <p>Iterates through the certificate issuers in the key vault by page and prints out each issuer's name and
     * provider.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers.iterableByPage -->
     * <pre>
     * certificateClient.listPropertiesOfIssuers&#40;&#41;.iterableByPage&#40;&#41;.forEach&#40;pagedResponse -&gt; &#123;
     *     pagedResponse.getValue&#40;&#41;.forEach&#40;issuer -&gt; &#123;
     *         CertificateIssuer retrievedIssuer = certificateClient.getIssuer&#40;issuer.getName&#40;&#41;&#41;;
     *
     *         System.out.printf&#40;&quot;Received issuer with name %s and provider %s%n&quot;, retrievedIssuer.getName&#40;&#41;,
     *             retrievedIssuer.getProvider&#40;&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers.iterableByPage -->
     *
     * @return A {@link PagedIterable} of properties objects of all the certificate issuers in the vault. A properties
     * object contains the issuer identifier and provider.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<IssuerProperties> listPropertiesOfIssuers() {
        return listPropertiesOfIssuers(RequestContext.none());
    }

    /**
     * Lists all the certificate issuers in the key vault. Each issuer is represented by a properties object containing
     * the certificate issuer identifier and provider. This operation requires the
     * {@code certificates/manageissuers/getissuers} permission.
     *
     * <p>><strong>Iterate through certificate issuers</strong></p>
     * <p>Lists the certificate issuers in the key vault and gets their details by looping though the properties objects
     * and calling {@link CertificateClient#getIssuer(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers#RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * certificateClient.listPropertiesOfIssuers&#40;requestContext&#41;.forEach&#40;issuer -&gt; &#123;
     *     CertificateIssuer retrievedIssuer = certificateClient.getIssuer&#40;issuer.getName&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot;Received issuer with name %s and provider %s%n&quot;, retrievedIssuer.getName&#40;&#41;,
     *         retrievedIssuer.getProvider&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers#RequestContext -->
     *
     * <p><strong>Iterate through certificate issuers by page</strong></p>
     * <p>Iterates through the certificate issuers in the key vault by page and gets their details by looping though the
     * properties objects and calling {@link CertificateClient#getIssuer(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers.iterableByPage#RequestContext -->
     * <pre>
     * RequestContext reqContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * certificateClient.listPropertiesOfIssuers&#40;reqContext&#41;.iterableByPage&#40;&#41;.forEach&#40;pagedResponse -&gt; &#123;
     *     pagedResponse.getValue&#40;&#41;.forEach&#40;issuer -&gt; &#123;
     *         CertificateIssuer retrievedIssuer = certificateClient.getIssuer&#40;issuer.getName&#40;&#41;&#41;;
     *
     *         System.out.printf&#40;&quot;Received issuer with name %s and provider %s%n&quot;, retrievedIssuer.getName&#40;&#41;,
     *             retrievedIssuer.getProvider&#40;&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers.iterableByPage#RequestContext -->
     *
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} of properties objects of all the certificate issuers in the vault. A properties
     * object contains the issuer identifier and provider.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<IssuerProperties> listPropertiesOfIssuers(RequestContext requestContext) {
        return mapPages(pagingOptions -> clientImpl.getCertificateIssuersSinglePage(null),
            (pagingOptions, nextLink) -> clientImpl.getCertificateIssuersNextSinglePage(nextLink, requestContext),
            IssuerPropertiesHelper::createIssuerProperties);
    }

    /**
     * Updates a certificate issuer. Only attributes populated in {@code issuer} are changed. Attributes not specified
     * in the request are not changed. This operation requires the certificates/setissuers permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the latest version of a certificate issuer and updates it in the key vault, then prints out the updated
     * issuer's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.updateIssuer#CertificateIssuer -->
     * <pre>
     * CertificateIssuer returnedIssuer = certificateClient.getIssuer&#40;&quot;issuerName&quot;&#41;;
     *
     * returnedIssuer.setAccountId&#40;&quot;newAccountId&quot;&#41;;
     *
     * CertificateIssuer updatedIssuer = certificateClient.updateIssuer&#40;returnedIssuer&#41;;
     *
     * System.out.printf&#40;&quot;Updated issuer with name %s, provider %s and account Id %s%n&quot;, updatedIssuer.getName&#40;&#41;,
     *     updatedIssuer.getProvider&#40;&#41;, updatedIssuer.getAccountId&#40;&#41;&#41;;
     * </pre>
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
        Objects.requireNonNull(issuer, "'issuer' cannot be null.");
        if (isNullOrEmpty(issuer.getName())) {
            throw LOGGER.throwableAtError()
                .log("'issuer.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        IssuerBundle issuerBundle = getIssuerBundle(issuer);
        CertificateIssuerUpdateParameters certificateIssuerUpdateParameters
            = new CertificateIssuerUpdateParameters().setProvider(issuerBundle.getProvider())
                .setOrganizationDetails(issuerBundle.getOrganizationDetails())
                .setCredentials(issuerBundle.getCredentials())
                .setAttributes(issuerBundle.getAttributes());

        try (Response<IssuerBundle> response = clientImpl.updateCertificateIssuerWithResponse(issuer.getName(),
            certificateIssuerUpdateParameters, RequestContext.none())) {

            return createCertificateIssuer(response.getValue());
        }
    }

    /**
     * Updates a certificate issuer. Only attributes populated in {@code issuer} are changed. Attributes not specified
     * in the request are not changed. This operation requires the certificates/setissuers permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the latest version of a certificate issuer and updates it in the key vault. Prints out details of the
     * response returned by the service and the updated issuer.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.updateIssuerWithResponse#CertificateIssuer-RequestContext -->
     * <pre>
     * CertificateIssuer issuer = certificateClient.getIssuer&#40;&quot;issuerName&quot;&#41;;
     *
     * issuer.setAccountId&#40;&quot;newAccountId&quot;&#41;;
     *
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;CertificateIssuer&gt; updatedIssuerWithResponse =
     *     certificateClient.updateIssuerWithResponse&#40;issuer, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Updated issuer with name %s, provider %s and account Id %s%n&quot;,
     *     updatedIssuerWithResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *     updatedIssuerWithResponse.getValue&#40;&#41;.getProvider&#40;&#41;,
     *     updatedIssuerWithResponse.getValue&#40;&#41;.getAccountId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.updateIssuerWithResponse#CertificateIssuer-RequestContext -->
     *
     * @param issuer The issuer with updated properties.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return The updated issuer.
     *
     * @throws HttpResponseException If a certificate issuer with the given {@link CertificateIssuer#getName()} doesn't
     * exist in the key vault.
     * @throws IllegalArgumentException If {@link CertificateIssuer#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code issuer} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificateIssuer> updateIssuerWithResponse(CertificateIssuer issuer,
        RequestContext requestContext) {
        Objects.requireNonNull(issuer, "'issuer' cannot be null.");

        if (isNullOrEmpty(issuer.getName())) {
            throw LOGGER.throwableAtError()
                .log("'issuer.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        IssuerBundle issuerBundle = getIssuerBundle(issuer);
        CertificateIssuerUpdateParameters certificateIssuerUpdateParameters
            = new CertificateIssuerUpdateParameters().setProvider(issuerBundle.getProvider())
                .setOrganizationDetails(issuerBundle.getOrganizationDetails())
                .setCredentials(issuerBundle.getCredentials())
                .setAttributes(issuerBundle.getAttributes());

        return mapResponse(clientImpl.updateCertificateIssuerWithResponse(issuer.getName(),
            certificateIssuerUpdateParameters, requestContext), CertificateIssuerHelper::createCertificateIssuer);
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
     * <pre>
     * CertificateContact contactToAdd = new CertificateContact&#40;&#41;.setName&#40;&quot;user&quot;&#41;.setEmail&#40;&quot;useremail&#64;example.com&quot;&#41;;
     *
     * certificateClient.setContacts&#40;Collections.singletonList&#40;contactToAdd&#41;&#41;.forEach&#40;contact -&gt; &#123;
     *     System.out.printf&#40;&quot;Added contact with name %s and email %s to key vault%n&quot;, contact.getName&#40;&#41;,
     *         contact.getEmail&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.setContacts#List -->
     *
     * @param contacts The list of contacts to set on the vault.
     * @return A {@link PagedIterable} containing all the certificate contacts in the vault.
     *
     * @throws HttpResponseException If the provided contact information is malformed.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateContact> setContacts(List<CertificateContact> contacts) {
        return setContacts(contacts, RequestContext.none());
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
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.setContacts#List-RequestContext -->
     * <pre>
     * CertificateContact sampleContact = new CertificateContact&#40;&#41;.setName&#40;&quot;user&quot;&#41;.setEmail&#40;&quot;useremail&#64;example.com&quot;&#41;;
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * certificateClient.setContacts&#40;Collections.singletonList&#40;sampleContact&#41;, requestContext&#41;.forEach&#40;contact -&gt; &#123;
     *     System.out.printf&#40;&quot;Added contact with name %s and email %s to key vault%n&quot;, contact.getName&#40;&#41;,
     *         contact.getEmail&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.setContacts#List-RequestContext -->
     *
     * @param contacts The list of contacts to set on the vault.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} containing all the certificate contacts in the vault.
     *
     * @throws HttpResponseException If the provided contact information is malformed.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateContact> setContacts(List<CertificateContact> contacts,
        RequestContext requestContext) {

        return new PagedIterable<>((pagingOptions) -> mapContactsToPagedResponse(
            clientImpl.setCertificateContactsWithResponse(new Contacts().setContactList(contacts), requestContext)));
    }

    /**
     * Lists all the certificate contacts in the key vault. This operation requires the
     * {@code certificates/managecontacts} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Lists the certificate contacts in the key vault and prints out each one's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listContacts -->
     * <pre>
     * for &#40;CertificateContact contact : certificateClient.listContacts&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Added contact with name %s and email %s to key vault%n&quot;, contact.getName&#40;&#41;,
     *         contact.getEmail&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listContacts -->
     *
     * @return A {@link PagedIterable} containing all the certificate contacts in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateContact> listContacts() {
        return listContacts(RequestContext.none());
    }

    /**
     * Lists all the certificate contacts in the key vault. This operation requires the
     * {@code certificates/managecontacts} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Lists the certificate contacts in the key vault and prints out each one's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.listContacts#RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * for &#40;CertificateContact contact : certificateClient.listContacts&#40;requestContext&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Added contact with name %s and email %s to key vault%n&quot;, contact.getName&#40;&#41;,
     *         contact.getEmail&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.listContacts#RequestContext -->
     *
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} containing all the certificate contacts in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateContact> listContacts(RequestContext requestContext) {
        return new PagedIterable<>((pagingOptions) -> mapContactsToPagedResponse(
            clientImpl.getCertificateContactsWithResponse(requestContext)));
    }

    /**
     * Deletes all the certificate contacts in the key vault. This operation requires the
     * {@code certificates/managecontacts} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes the certificate contacts in the key vault and prints out each one's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.deleteContacts -->
     * <pre>
     * for &#40;CertificateContact contact : certificateClient.deleteContacts&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Deleted contact with name %s and email %s from key vault%n&quot;, contact.getName&#40;&#41;,
     *         contact.getEmail&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.deleteContacts -->
     *
     * @return A {@link PagedIterable} containing the freshly deleted certificate contacts.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateContact> deleteContacts() {
        return deleteContacts(RequestContext.none());
    }

    /**
     * Deletes all the certificate contacts in the key vault. This operation requires the
     * {@code certificates/managecontacts} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes the certificate contacts in the key vault and prints out each one's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.deleteContacts#RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * for &#40;CertificateContact contact : certificateClient.deleteContacts&#40;requestContext&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Deleted contact with name %s and email %s from key vault%n&quot;, contact.getName&#40;&#41;,
     *         contact.getEmail&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.deleteContacts#RequestContext -->
     *
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} containing the freshly deleted certificate contacts.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateContact> deleteContacts(RequestContext requestContext) {
        return new PagedIterable<>((pagingOptions) -> mapContactsToPagedResponse(
            clientImpl.deleteCertificateContactsWithResponse(requestContext)));
    }

    /**
     * Gets information on a pending operation from the key vault. This operation requires the {@code certificates/get}
     * permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a pending certificate operation and prints out its status.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateOperation#String -->
     * <pre>
     * Poller&lt;CertificateOperation, KeyVaultCertificateWithPolicy&gt; getCertPoller = null;
     *     &#47;&#47;certificateClient.getCertificateOperation&#40;&quot;certificateName&quot;&#41;;
     *
     * getCertPoller.waitUntil&#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41;;
     *
     * KeyVaultCertificate cert = getCertPoller.getFinalResult&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Certificate created with name %s%n&quot;, cert.getName&#40;&#41;&#41;;
     * </pre>
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
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<CertificateOperation, KeyVaultCertificateWithPolicy> getCertificateOperation(String certificateName) {
        if (isNullOrEmpty(certificateName)) {
            throw LOGGER.throwableAtError()
                .log("'certificateName' cannot be null or empty.", IllegalArgumentException::new);
        }

        return Poller.createPoller(Duration.ofSeconds(1),
            pollingContext -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null),
            pollingContext -> createCertificatePollOperation(certificateName),
            (pollingContext, pollResponse) -> createCertificateCancellationOperation(certificateName),
            pollingContext -> createCertificateFetchOperation(certificateName));
    }

    /**
     * Deletes the creation operation for the specified certificate that is in the process of being created. The
     * certificate will not be created. This operation requires the {@code certificates/update} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Triggers certificate creation and then deletes the certificate creation operation in the key vault. Prints out
     * the deleted certificate operation details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.deleteCertificateOperation#String -->
     * <pre>
     * CertificateOperation deletedCertificateOperation =
     *     certificateClient.deleteCertificateOperation&#40;&quot;certificateName&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Deleted Certificate Operation's last status %s%n&quot;, deletedCertificateOperation.getStatus&#40;&#41;&#41;;
     * </pre>
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
        if (isNullOrEmpty(certificateName)) {
            throw LOGGER.throwableAtError()
                .log("'certificateName' cannot be null or empty.", IllegalArgumentException::new);
        }

        try (Response<com.azure.v2.security.keyvault.certificates.implementation.models.CertificateOperation> response
            = clientImpl.deleteCertificateOperationWithResponse(certificateName, RequestContext.none())) {

            return createCertificateOperation(response.getValue());
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
     * <pre>
     * CertificateOperation deletedCertificateOperation =
     *     certificateClient.deleteCertificateOperation&#40;&quot;certificateName&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Deleted Certificate Operation's last status %s%n&quot;, deletedCertificateOperation.getStatus&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.deleteCertificateOperation#String -->
     *
     * @param certificateName The name of the certificate the operation pertains to. It is required and cannot be
     * {@code null} or empty.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue()} contains the deleted certificate operation.
     *
     * @throws HttpResponseException If a certificate operation for a certificate with {@code certificateName}
     * doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code certificateName} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificateOperation> deleteCertificateOperationWithResponse(String certificateName,
        RequestContext requestContext) {

        if (isNullOrEmpty(certificateName)) {
            throw LOGGER.throwableAtError()
                .log("'certificateName' cannot be null or empty.", IllegalArgumentException::new);
        }

        return mapResponse(clientImpl.deleteCertificateOperationWithResponse(certificateName, requestContext),
            CertificateOperationHelper::createCertificateOperation);
    }

    /**
     * Cancels a certificate creation operation that is already in progress. This operation requires the
     * {@code certificates/update} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Triggers certificate creation and then cancels the creation operation in the key vault. Prints out the
     * cancelled certificate operation details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.cancelCertificateOperation#String -->
     * <pre>
     * CertificateOperation certificateOperation =
     *     certificateClient.cancelCertificateOperation&#40;&quot;certificateName&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Certificate Operation status %s%n&quot;, certificateOperation.getStatus&#40;&#41;&#41;;
     * </pre>
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
        if (isNullOrEmpty(certificateName)) {
            throw LOGGER.throwableAtError()
                .log("'certificateName' cannot be null or empty.", IllegalArgumentException::new);
        }

        try (Response<com.azure.v2.security.keyvault.certificates.implementation.models.CertificateOperation> response
            = clientImpl.updateCertificateOperationWithResponse(certificateName,
                new CertificateOperationUpdateParameter(true), RequestContext.none())) {

            return createCertificateOperation(response.getValue());
        }
    }

    /**
     * Cancels a certificate creation operation that is already in progress. This operation requires the
     * {@code certificates/update} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Triggers certificate creation and then cancels the creation operation in the key vault. Prints out the
     * cancelled certificate operation details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.cancelCertificateOperationWithResponse#String-RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;CertificateOperation&gt; certificateOperationWithResponse =
     *     certificateClient.cancelCertificateOperationWithResponse&#40;&quot;certificateName&quot;, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Certificate Operation status %s%n&quot;, certificateOperationWithResponse.getValue&#40;&#41;.getStatus&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.cancelCertificateOperationWithResponse#String-RequestContext -->
     *
     * @param certificateName The name of the certificate the operation pertains to. It is required and cannot be
     * {@code null} or empty.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue()} contains the cancelled certificate operation.
     *
     * @throws HttpResponseException If a certificate operation for a certificate with the given {@code name} doesn't
     * exist in the key vault.
     * @throws IllegalArgumentException If the {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificateOperation> cancelCertificateOperationWithResponse(String certificateName,
        RequestContext requestContext) {

        return mapResponse(
            clientImpl.updateCertificateOperationWithResponse(certificateName,
                new CertificateOperationUpdateParameter(true), requestContext),
            CertificateOperationHelper::createCertificateOperation);
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
     * <pre>
     * List&lt;byte[]&gt; x509CertificatesToMerge = new ArrayList&lt;&gt;&#40;&#41;;
     * MergeCertificateOptions config =
     *     new MergeCertificateOptions&#40;&quot;certificateName&quot;, x509CertificatesToMerge&#41;
     *         .setEnabled&#40;false&#41;;
     *
     * KeyVaultCertificate mergedCertificate = certificateClient.mergeCertificate&#40;config&#41;;
     *
     * System.out.printf&#40;&quot;Received Certificate with name %s and key id %s%n&quot;,
     *     mergedCertificate.getProperties&#40;&#41;.getName&#40;&#41;, mergedCertificate.getKeyId&#40;&#41;&#41;;
     * </pre>
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
        Objects.requireNonNull(mergeCertificateOptions, "'mergeCertificateOptions' cannot be null.");

        CertificateMergeParameters certificateMergeParameters
            = new CertificateMergeParameters(mergeCertificateOptions.getX509Certificates())
                .setTags(mergeCertificateOptions.getTags())
                .setCertificateAttributes(new CertificateAttributes().setEnabled(mergeCertificateOptions.isEnabled()));

        try (Response<CertificateBundle> response = clientImpl.mergeCertificateWithResponse(
            mergeCertificateOptions.getName(), certificateMergeParameters, RequestContext.none())) {

            return createCertificateWithPolicy(response.getValue());
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
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.mergeCertificateWithResponse#MergeCertificateOptions-RequestContext -->
     * <pre>
     * List&lt;byte[]&gt; x509CertsToMerge = new ArrayList&lt;&gt;&#40;&#41;;
     * MergeCertificateOptions mergeConfig =
     *     new MergeCertificateOptions&#40;&quot;certificateName&quot;, x509CertsToMerge&#41;
     *         .setEnabled&#40;false&#41;;
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;KeyVaultCertificateWithPolicy&gt; mergedCertificateWithResponse =
     *     certificateClient.mergeCertificateWithResponse&#40;mergeConfig, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Received Certificate with name %s and key id %s%n&quot;,
     *     mergedCertificateWithResponse.getValue&#40;&#41;.getProperties&#40;&#41;.getName&#40;&#41;,
     *     mergedCertificateWithResponse.getValue&#40;&#41;.getKeyId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.mergeCertificateWithResponse#MergeCertificateOptions-RequestContext -->
     *
     * @param mergeCertificateOptions The merge certificate configuration holding the x509 certificates to merge.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue()} contains the merged certificate.
     *
     * @throws HttpResponseException If the provided {@code mergeCertificateOptions} are malformed.
     * @throws IllegalArgumentException If {@link MergeCertificateOptions#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code mergeCertificateOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultCertificateWithPolicy>
        mergeCertificateWithResponse(MergeCertificateOptions mergeCertificateOptions, RequestContext requestContext) {

        Objects.requireNonNull(mergeCertificateOptions, "'mergeCertificateOptions' cannot be null.");

        if (isNullOrEmpty(mergeCertificateOptions.getName())) {
            throw LOGGER.throwableAtError()
                .log("'mergeCertificateOptions.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        CertificateMergeParameters certificateMergeParameters
            = new CertificateMergeParameters(mergeCertificateOptions.getX509Certificates())
                .setTags(mergeCertificateOptions.getTags())
                .setCertificateAttributes(new CertificateAttributes().setEnabled(mergeCertificateOptions.isEnabled()));

        return mapResponse(clientImpl.mergeCertificateWithResponse(mergeCertificateOptions.getName(),
            certificateMergeParameters, requestContext),
            KeyVaultCertificateWithPolicyHelper::createCertificateWithPolicy);
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
     * <pre>
     * byte[] certificateToImport = new byte[100];
     * ImportCertificateOptions config =
     *     new ImportCertificateOptions&#40;&quot;certificateName&quot;, certificateToImport&#41;.setEnabled&#40;false&#41;;
     *
     * KeyVaultCertificate importedCertificate = certificateClient.importCertificate&#40;config&#41;;
     *
     * System.out.printf&#40;&quot;Received Certificate with name %s and key id %s%n&quot;,
     *     importedCertificate.getProperties&#40;&#41;.getName&#40;&#41;, importedCertificate.getKeyId&#40;&#41;&#41;;
     * </pre>
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
        Objects.requireNonNull(importCertificateOptions, "'importCertificateOptions' cannot be null.");

        if (isNullOrEmpty(importCertificateOptions.getName())) {
            throw LOGGER.throwableAtError()
                .log("'importCertificateOptions.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        com.azure.v2.security.keyvault.certificates.implementation.models.CertificatePolicy implPolicy
            = getImplCertificatePolicy(importCertificateOptions.getPolicy());

        CertificateImportParameters certificateImportParameters
            = new CertificateImportParameters(transformCertificateForImport(importCertificateOptions))
                .setPassword(importCertificateOptions.getPassword())
                .setCertificatePolicy(implPolicy)
                .setTags(importCertificateOptions.getTags())
                .setCertificateAttributes(new CertificateAttributes().setEnabled(importCertificateOptions.isEnabled()));

        try (Response<CertificateBundle> response = clientImpl.importCertificateWithResponse(
            importCertificateOptions.getName(), certificateImportParameters, RequestContext.none())) {

            return createCertificateWithPolicy(response.getValue());
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
     * <!-- src_embed com.azure.v2.security.keyvault.certificates.CertificateClient.importCertificateWithResponse#ImportCertificateOptions-RequestContext -->
     * <pre>
     * byte[] certToImport = new byte[100];
     * ImportCertificateOptions importCertificateOptions =
     *     new ImportCertificateOptions&#40;&quot;certificateName&quot;, certToImport&#41;.setEnabled&#40;false&#41;;
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;KeyVaultCertificateWithPolicy&gt; importedCertificateWithResponse =
     *     certificateClient.importCertificateWithResponse&#40;importCertificateOptions, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Received Certificate with name %s and key id %s%n&quot;,
     *     importedCertificateWithResponse.getValue&#40;&#41;.getProperties&#40;&#41;.getName&#40;&#41;,
     *     importedCertificateWithResponse.getValue&#40;&#41;.getKeyId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.certificates.CertificateClient.importCertificateWithResponse#ImportCertificateOptions-RequestContext -->
     *
     * @param importCertificateOptions The details of the certificate to import to the key vault. It is required and
     * cannot be {@code null}.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue()} contains the imported certificate.
     *
     * @throws HttpResponseException If the provided {@code importCertificateOptions} are malformed.
     * @throws IllegalArgumentException If {@link ImportCertificateOptions#getName()} is {@code null} or an empty
     * string.
     * @throws NullPointerException If {@code importCertificateOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultCertificateWithPolicy> importCertificateWithResponse(
        ImportCertificateOptions importCertificateOptions, RequestContext requestContext) {
        Objects.requireNonNull(importCertificateOptions, "'importCertificateOptions' cannot be null.");

        if (isNullOrEmpty(importCertificateOptions.getName())) {
            throw LOGGER.throwableAtError()
                .log("'importCertificateOptions.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        com.azure.v2.security.keyvault.certificates.implementation.models.CertificatePolicy implPolicy
            = getImplCertificatePolicy(importCertificateOptions.getPolicy());

        CertificateImportParameters certificateImportParameters
            = new CertificateImportParameters(transformCertificateForImport(importCertificateOptions))
                .setPassword(importCertificateOptions.getPassword())
                .setCertificatePolicy(implPolicy)
                .setTags(importCertificateOptions.getTags())
                .setCertificateAttributes(new CertificateAttributes().setEnabled(importCertificateOptions.isEnabled()));

        return mapResponse(clientImpl.importCertificateWithResponse(importCertificateOptions.getName(),
            certificateImportParameters, requestContext),
            KeyVaultCertificateWithPolicyHelper::createCertificateWithPolicy);
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

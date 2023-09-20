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
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.PollerFlux;
import com.azure.security.keyvault.certificates.implementation.CertificateClientImpl;
import com.azure.security.keyvault.certificates.models.CertificateContact;
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

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * The CertificateAsyncClient provides asynchronous methods to manage {@link KeyVaultCertificate certifcates} in
 * a key vault. The client supports creating, retrieving, updating, merging, deleting, purging, backing up,
 * restoring and listing the {@link KeyVaultCertificate certificates}. The client also supports listing
 * {@link DeletedCertificate deleted certificates} for a soft-delete enabled key vault.
 *
 * <p>The client further allows creating, retrieving, updating, deleting and listing the
 * {@link CertificateIssuer certificate issuers}. The client also supports creating, listing and deleting
 * {@link CertificateContact certificate contacts}.</p>
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Azure Key Vault service, you will need to create an instance of the
 * {@link CertificateAsyncClient} class, a vault url and a credential object.</p>
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
 * <p><strong>Sample: Construct Asynchronous Certificate Client</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a
 * {@link com.azure.security.keyvault.certificates.CertificateAsyncClient}, using the
 * {@link com.azure.security.keyvault.certificates.CertificateClientBuilder} to configure it.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.instantiation -->
 * <pre>
 * CertificateAsyncClient certificateAsyncClient = new CertificateClientBuilder&#40;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;
 *     .httpLogOptions&#40;new HttpLogOptions&#40;&#41;.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.instantiation -->
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Create a Certificate</h2>
 * The {@link CertificateAsyncClient} can be used to create a certificate in the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously create a certificate in the key vault,
 * using the {@link CertificateAsyncClient#beginCreateCertificate(String, CertificatePolicy)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.beginCreateCertificate#String-CertificatePolicy -->
 * <pre>
 * CertificatePolicy certPolicy = new CertificatePolicy&#40;&quot;Self&quot;, &quot;CN=SelfSignedJavaPkcs12&quot;&#41;;
 * certificateAsyncClient.beginCreateCertificate&#40;&quot;certificateName&quot;, certPolicy&#41;
 *     .subscribe&#40;pollResponse -&gt; &#123;
 *         System.out.println&#40;&quot;---------------------------------------------------------------------------------&quot;&#41;;
 *         System.out.println&#40;pollResponse.getStatus&#40;&#41;&#41;;
 *         System.out.println&#40;pollResponse.getValue&#40;&#41;.getStatus&#40;&#41;&#41;;
 *         System.out.println&#40;pollResponse.getValue&#40;&#41;.getStatusDetails&#40;&#41;&#41;;
 *     &#125;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.beginCreateCertificate#String-CertificatePolicy -->
 *
 * <p><strong>Note:</strong> For the synchronous sample, refer to {@link CertificateClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Get a Certificate</h2>
 * The {@link CertificateAsyncClient} can be used to retrieve a certificate from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously retrieve a certificate from the key vault, using
 * the {@link CertificateAsyncClient#getCertificate(String)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificate#String -->
 * <pre>
 * certificateAsyncClient.getCertificate&#40;&quot;certificateName&quot;&#41;
 *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
 *     .subscribe&#40;certificateResponse -&gt;
 *         System.out.printf&#40;&quot;Certificate is returned with name %s and secretId %s %n&quot;,
 *             certificateResponse.getProperties&#40;&#41;.getName&#40;&#41;, certificateResponse.getSecretId&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificate#String -->
 *
 * <p><strong>Note:</strong> For the synchronous sample, refer to {@link CertificateClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Delete a Certificate</h2>
 * The {@link CertificateAsyncClient} can be used to delete a certificate from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously delete a certificate from the Azure
 * KeyVault, using the {@link CertificateAsyncClient#beginDeleteCertificate(String)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.beginDeleteCertificate#String -->
 * <pre>
 * certificateAsyncClient.beginDeleteCertificate&#40;&quot;certificateName&quot;&#41;
 *     .subscribe&#40;pollResponse -&gt; &#123;
 *         System.out.println&#40;&quot;Delete Status: &quot; + pollResponse.getStatus&#40;&#41;.toString&#40;&#41;&#41;;
 *         System.out.println&#40;&quot;Delete Certificate Name: &quot; + pollResponse.getValue&#40;&#41;.getName&#40;&#41;&#41;;
 *         System.out.println&#40;&quot;Certificate Delete Date: &quot; + pollResponse.getValue&#40;&#41;.getDeletedOn&#40;&#41;.toString&#40;&#41;&#41;;
 *     &#125;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.beginDeleteCertificate#String -->
 *
 * <p><strong>Note:</strong> For the synchronous sample, refer to {@link CertificateClient}.</p>
 *
 * @see com.azure.security.keyvault.certificates
 * @see CertificateClientBuilder
 */
@ServiceClient(builder = CertificateClientBuilder.class, isAsync = true, serviceInterfaces = CertificateClientImpl.CertificateService.class)
public final class CertificateAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(CertificateAsyncClient.class);

    private final CertificateClientImpl implClient;

    /**
     * Creates a CertificateAsyncClient to service requests
     *
     * @param implClient The implementation client to route requests through.
     */
    CertificateAsyncClient(CertificateClientImpl implClient) {
        this.implClient = implClient;
    }

    /**
     * Get the vault endpoint url to which service requests are sent to.
     * @return the vault endpoint url
     */
    public String getVaultUrl() {
        return implClient.getVaultUrl();
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    HttpPipeline getHttpPipeline() {
        return implClient.getHttpPipeline();
    }

    /**
     * Creates a new certificate. If this is the first version, the certificate resource is created. This operation requires
     * the certificates/create permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Create certificate is a long running operation. The {@link PollerFlux poller} allows users to automatically poll on the create certificate
     * operation status. It is possible to monitor each intermediate poll response during the poll operation.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.beginCreateCertificate#String-CertificatePolicy-Boolean-Map -->
     * <pre>
     * CertificatePolicy policy = new CertificatePolicy&#40;&quot;Self&quot;, &quot;CN=SelfSignedJavaPkcs12&quot;&#41;;
     * Map&lt;String, String&gt; tags = new HashMap&lt;&gt;&#40;&#41;;
     * tags.put&#40;&quot;foo&quot;, &quot;bar&quot;&#41;;
     * certificateAsyncClient.beginCreateCertificate&#40;&quot;certificateName&quot;, policy, true, tags&#41;
     *     .subscribe&#40;pollResponse -&gt; &#123;
     *         System.out.println&#40;&quot;---------------------------------------------------------------------------------&quot;&#41;;
     *         System.out.println&#40;pollResponse.getStatus&#40;&#41;&#41;;
     *         System.out.println&#40;pollResponse.getValue&#40;&#41;.getStatus&#40;&#41;&#41;;
     *         System.out.println&#40;pollResponse.getValue&#40;&#41;.getStatusDetails&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.beginCreateCertificate#String-CertificatePolicy-Boolean-Map -->
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
        return implClient.beginCreateCertificateAsync(certificateName, policy, isEnabled, tags);
    }

    /**
     * Creates a new certificate. If this is the first version, the certificate resource is created. This operation requires
     * the certificates/create permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Create certificate is a long running operation. The {@link PollerFlux poller} allows users to automatically poll on the create certificate
     * operation status. It is possible to monitor each intermediate poll response during the poll operation.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.beginCreateCertificate#String-CertificatePolicy -->
     * <pre>
     * CertificatePolicy certPolicy = new CertificatePolicy&#40;&quot;Self&quot;, &quot;CN=SelfSignedJavaPkcs12&quot;&#41;;
     * certificateAsyncClient.beginCreateCertificate&#40;&quot;certificateName&quot;, certPolicy&#41;
     *     .subscribe&#40;pollResponse -&gt; &#123;
     *         System.out.println&#40;&quot;---------------------------------------------------------------------------------&quot;&#41;;
     *         System.out.println&#40;pollResponse.getStatus&#40;&#41;&#41;;
     *         System.out.println&#40;pollResponse.getValue&#40;&#41;.getStatus&#40;&#41;&#41;;
     *         System.out.println&#40;pollResponse.getValue&#40;&#41;.getStatusDetails&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.beginCreateCertificate#String-CertificatePolicy -->
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


    /**
     * Gets a pending {@link CertificateOperation} from the key vault. This operation requires the certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Get a pending certificate operation. The {@link PollerFlux poller} allows users to automatically poll on the certificate
     * operation status. It is possible to monitor each intermediate poll response during the poll operation.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateOperation#String -->
     * <pre>
     * certificateAsyncClient.getCertificateOperation&#40;&quot;certificateName&quot;&#41;
     *     .subscribe&#40;pollResponse -&gt; &#123;
     *         System.out.println&#40;&quot;---------------------------------------------------------------------------------&quot;&#41;;
     *         System.out.println&#40;pollResponse.getStatus&#40;&#41;&#41;;
     *         System.out.println&#40;pollResponse.getValue&#40;&#41;.getStatus&#40;&#41;&#41;;
     *         System.out.println&#40;pollResponse.getValue&#40;&#41;.getStatusDetails&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateOperation#String -->
     *
     * @param certificateName The name of the certificate.
     * @throws ResourceNotFoundException when a certificate operation for a certificate with {@code certificateName} doesn't exist.
     * @return A {@link PollerFlux} polling on the certificate operation status.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> getCertificateOperation(String certificateName) {
        return implClient.getCertificateOperationAsync(certificateName);
    }

    /**
     * Gets information about the latest version of the specified certificate. This operation requires the certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the certificate in the key vault. Prints out the
     * returned certificate details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificate#String -->
     * <pre>
     * certificateAsyncClient.getCertificate&#40;&quot;certificateName&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;certificateResponse -&gt;
     *         System.out.printf&#40;&quot;Certificate is returned with name %s and secretId %s %n&quot;,
     *             certificateResponse.getProperties&#40;&#41;.getName&#40;&#41;, certificateResponse.getSecretId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificate#String -->
     *
     * @param certificateName The name of the certificate to retrieve, cannot be null
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException if {@code certificateName} is empty string.
     * @return A {@link Mono} containing the requested {@link KeyVaultCertificateWithPolicy certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultCertificateWithPolicy> getCertificate(String certificateName) {
        try {
            return withContext(context -> implClient.getCertificateWithResponseAsync(certificateName, "",
                context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Gets information about the latest version of the specified certificate. This operation requires the certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the certificate in the key vault. Prints out the
     * returned certificate details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateWithResponse#String -->
     * <pre>
     * certificateAsyncClient.getCertificateWithResponse&#40;&quot;certificateName&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;certificateResponse -&gt;
     *         System.out.printf&#40;&quot;Certificate is returned with name %s and secretId %s %n&quot;,
     *             certificateResponse.getValue&#40;&#41;.getProperties&#40;&#41;.getName&#40;&#41;,
     *             certificateResponse.getValue&#40;&#41;.getSecretId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateWithResponse#String -->
     *
     * @param certificateName The name of the certificate to retrieve, cannot be null
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException if {@code certificateName} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the requested {@link KeyVaultCertificateWithPolicy certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultCertificateWithPolicy>> getCertificateWithResponse(String certificateName) {
        try {
            return withContext(context -> implClient.getCertificateWithResponseAsync(certificateName, "", context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Gets information about the latest version of the specified certificate. This operation requires the certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the certificate in the key vault. Prints out the
     * returned certificate details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateVersionWithResponse#string-string -->
     * <pre>
     * String certificateVersion = &quot;6A385B124DEF4096AF1361A85B16C204&quot;;
     * certificateAsyncClient.getCertificateVersionWithResponse&#40;&quot;certificateName&quot;, certificateVersion&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;certificateWithVersion -&gt;
     *         System.out.printf&#40;&quot;Certificate is returned with name %s and secretId %s %n&quot;,
     *             certificateWithVersion.getValue&#40;&#41;.getProperties&#40;&#41;.getName&#40;&#41;,
     *             certificateWithVersion.getValue&#40;&#41;.getSecretId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateVersionWithResponse#string-string -->
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
            return withContext(context -> implClient.getCertificateVersionWithResponseAsync(certificateName, version == null ? "" : version,
                context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Gets information about the specified version of the specified certificate. This operation requires the certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the certificate in the key vault. Prints out the
     * returned certificate details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateVersion#String-String -->
     * <pre>
     * certificateAsyncClient.getCertificateVersion&#40;&quot;certificateName&quot;, certificateVersion&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;certificateWithVersion -&gt;
     *         System.out.printf&#40;&quot;Certificate is returned with name %s and secretId %s %n&quot;,
     *             certificateWithVersion.getProperties&#40;&#41;.getName&#40;&#41;, certificateWithVersion.getSecretId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateVersion#String-String -->
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
            return withContext(context -> implClient.getCertificateVersionWithResponseAsync(certificateName, version == null ? "" : version,
                context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificateProperties#CertificateProperties -->
     * <pre>
     * certificateAsyncClient.getCertificate&#40;&quot;certificateName&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;certificateResponseValue -&gt; &#123;
     *         KeyVaultCertificate certificate = certificateResponseValue;
     *         &#47;&#47;Update enabled status of the certificate
     *         certificate.getProperties&#40;&#41;.setEnabled&#40;false&#41;;
     *         certificateAsyncClient.updateCertificateProperties&#40;certificate.getProperties&#40;&#41;&#41;
     *             .subscribe&#40;certificateResponse -&gt;
     *                 System.out.printf&#40;&quot;Certificate's enabled status %s %n&quot;,
     *                     certificateResponse.getProperties&#40;&#41;.isEnabled&#40;&#41;.toString&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificateProperties#CertificateProperties -->
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
            return withContext(context -> implClient.updateCertificatePropertiesWithResponseAsync(properties, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificatePropertiesWithResponse#CertificateProperties -->
     * <pre>
     * certificateAsyncClient.getCertificate&#40;&quot;certificateName&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;certificateResponseValue -&gt; &#123;
     *         KeyVaultCertificate certificate = certificateResponseValue;
     *         &#47;&#47;Update the enabled status of the certificate.
     *         certificate.getProperties&#40;&#41;.setEnabled&#40;false&#41;;
     *         certificateAsyncClient.updateCertificatePropertiesWithResponse&#40;certificate.getProperties&#40;&#41;&#41;
     *             .subscribe&#40;certificateResponse -&gt;
     *                 System.out.printf&#40;&quot;Certificate's enabled status %s %n&quot;,
     *                     certificateResponse.getValue&#40;&#41;.getProperties&#40;&#41;.isEnabled&#40;&#41;.toString&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificatePropertiesWithResponse#CertificateProperties -->
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
            return withContext(context -> implClient.updateCertificatePropertiesWithResponseAsync(properties,
                context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.beginDeleteCertificate#String -->
     * <pre>
     * certificateAsyncClient.beginDeleteCertificate&#40;&quot;certificateName&quot;&#41;
     *     .subscribe&#40;pollResponse -&gt; &#123;
     *         System.out.println&#40;&quot;Delete Status: &quot; + pollResponse.getStatus&#40;&#41;.toString&#40;&#41;&#41;;
     *         System.out.println&#40;&quot;Delete Certificate Name: &quot; + pollResponse.getValue&#40;&#41;.getName&#40;&#41;&#41;;
     *         System.out.println&#40;&quot;Certificate Delete Date: &quot; + pollResponse.getValue&#40;&#41;.getDeletedOn&#40;&#41;.toString&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.beginDeleteCertificate#String -->
     *
     * @param certificateName The name of the certificate to be deleted.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException when a certificate with {@code certificateName} is empty string.
     * @return A {@link PollerFlux} to poll on the {@link DeletedCertificate deleted certificate}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<DeletedCertificate, Void> beginDeleteCertificate(String certificateName) {
        return implClient.beginDeleteCertificateAsync(certificateName);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.getDeletedCertificate#string -->
     * <pre>
     * certificateAsyncClient.getDeletedCertificate&#40;&quot;certificateName&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;deletedSecretResponse -&gt;
     *         System.out.printf&#40;&quot;Deleted Certificate's Recovery Id %s %n&quot;, deletedSecretResponse.getRecoveryId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.getDeletedCertificate#string -->
     *
     * @param certificateName The name of the deleted certificate.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException when a certificate with {@code certificateName} is empty string.
     * @return A {@link Mono} containing the {@link DeletedCertificate deleted certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DeletedCertificate> getDeletedCertificate(String certificateName) {
        try {
            return withContext(context -> implClient.getDeletedCertificateWithResponseAsync(certificateName, context))
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.getDeletedCertificateWithResponse#string -->
     * <pre>
     * certificateAsyncClient.getDeletedCertificateWithResponse&#40;&quot;certificateName&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;deletedSecretResponse -&gt;
     *         System.out.printf&#40;&quot;Deleted Certificate's Recovery Id %s %n&quot;,
     *             deletedSecretResponse.getValue&#40;&#41;.getRecoveryId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.getDeletedCertificateWithResponse#string -->
     *
     * @param certificateName The name of the deleted certificate.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException when a certificate with {@code certificateName} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link DeletedCertificate deleted certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DeletedCertificate>> getDeletedCertificateWithResponse(String certificateName) {
        try {
            return withContext(context -> implClient.getDeletedCertificateWithResponseAsync(certificateName, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.purgeDeletedCertificateWithResponse#string -->
     * <pre>
     * certificateAsyncClient.purgeDeletedCertificateWithResponse&#40;&quot;deletedCertificateName&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;purgeResponse -&gt;
     *         System.out.printf&#40;&quot;Purge Status response %d %n&quot;, purgeResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.purgeDeletedCertificateWithResponse#string -->
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
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.purgeDeletedCertificateWithResponse#string -->
     * <pre>
     * certificateAsyncClient.purgeDeletedCertificateWithResponse&#40;&quot;deletedCertificateName&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;purgeResponse -&gt;
     *         System.out.printf&#40;&quot;Purge Status response %d %n&quot;, purgeResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.purgeDeletedCertificateWithResponse#string -->
     *
     * @param certificateName The name of the deleted certificate.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException when a certificate with {@code certificateName} is empty string.
     * @return A {@link Mono} containing a Void Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> purgeDeletedCertificateWithResponse(String certificateName) {
        try {
            return withContext(context -> implClient.purgeDeletedCertificateWithResponseAsync(certificateName, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     *
     * <!-- src_embed com.azure.security.certificatevault.certificates.CertificateAsyncClient.beginRecoverDeletedCertificate#String -->
     * <pre>
     * certificateAsyncClient.beginRecoverDeletedCertificate&#40;&quot;deletedCertificateName&quot;&#41;
     *     .subscribe&#40;pollResponse -&gt; &#123;
     *         System.out.println&#40;&quot;Recovery Status: &quot; + pollResponse.getStatus&#40;&#41;.toString&#40;&#41;&#41;;
     *         System.out.println&#40;&quot;Recover Certificate Name: &quot; + pollResponse.getValue&#40;&#41;.getName&#40;&#41;&#41;;
     *         System.out.println&#40;&quot;Recover Certificate Id: &quot; + pollResponse.getValue&#40;&#41;.getId&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.security.certificatevault.certificates.CertificateAsyncClient.beginRecoverDeletedCertificate#String -->
     *
     * @param certificateName The name of the deleted certificate to be recovered.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the certificate vault.
     * @throws HttpResponseException when a certificate with {@code certificateName} is empty string.
     * @return A {@link PollerFlux} to poll on the {@link KeyVaultCertificate recovered certificate}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<KeyVaultCertificateWithPolicy, Void> beginRecoverDeletedCertificate(String certificateName) {
        return implClient.beginRecoverDeletedCertificateAsync(certificateName);
    }

    /**
     * Requests that a backup of the specified certificate be downloaded to the client. All versions of the certificate will
     * be downloaded. This operation requires the certificates/backup permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Backs up the certificate from the key vault. Prints out the
     * length of the certificate's backup byte array returned in the response.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.backupCertificate#string -->
     * <pre>
     * certificateAsyncClient.backupCertificate&#40;&quot;certificateName&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;certificateBackupResponse -&gt;
     *         System.out.printf&#40;&quot;Certificate's Backup Byte array's length %s %n&quot;, certificateBackupResponse.length&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.backupCertificate#string -->
     *
     * @param certificateName The name of the certificate.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException when a certificate with {@code certificateName} is empty string.
     * @return A {@link Mono} containing the backed up certificate blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<byte[]> backupCertificate(String certificateName) {
        try {
            return withContext(context -> implClient.backupCertificateWithResponseAsync(certificateName, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.backupCertificateWithResponse#string -->
     * <pre>
     * certificateAsyncClient.backupCertificateWithResponse&#40;&quot;certificateName&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;certificateBackupResponse -&gt;
     *         System.out.printf&#40;&quot;Certificate's Backup Byte array's length %s %n&quot;,
     *             certificateBackupResponse.getValue&#40;&#41;.length&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.backupCertificateWithResponse#string -->
     *
     * @param certificateName The name of the certificate.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException when a certificate with {@code certificateName} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the backed up certificate blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<byte[]>> backupCertificateWithResponse(String certificateName) {
        try {
            return withContext(context -> implClient.backupCertificateWithResponseAsync(certificateName, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.restoreCertificate#byte -->
     * <pre>
     * byte[] certificateBackupByteArray = &#123;&#125;;
     * certificateAsyncClient.restoreCertificateBackup&#40;certificateBackupByteArray&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;certificateResponse -&gt; System.out.printf&#40;&quot;Restored Certificate with name %s and key id %s %n&quot;,
     *         certificateResponse.getProperties&#40;&#41;.getName&#40;&#41;, certificateResponse.getKeyId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.restoreCertificate#byte -->
     *
     * @param backup The backup blob associated with the certificate.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     * @return A {@link Mono} containing the {@link KeyVaultCertificate restored certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultCertificateWithPolicy> restoreCertificateBackup(byte[] backup) {
        try {
            return withContext(context -> implClient.restoreCertificateBackupWithResponseAsync(backup, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.restoreCertificateWithResponse#byte -->
     * <pre>
     * byte[] certificateBackup = &#123;&#125;;
     * certificateAsyncClient.restoreCertificateBackup&#40;certificateBackup&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;certificateResponse -&gt; System.out.printf&#40;&quot;Restored Certificate with name %s and key id %s %n&quot;,
     *         certificateResponse.getProperties&#40;&#41;.getName&#40;&#41;, certificateResponse.getKeyId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.restoreCertificateWithResponse#byte -->
     *
     * @param backup The backup blob associated with the certificate.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultCertificate restored certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultCertificateWithPolicy>> restoreCertificateBackupWithResponse(byte[] backup) {
        try {
            return withContext(context -> implClient.restoreCertificateBackupWithResponseAsync(backup, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificates -->
     * <pre>
     * certificateAsyncClient.listPropertiesOfCertificates&#40;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;certificate -&gt; certificateAsyncClient.getCertificateVersion&#40;certificate.getName&#40;&#41;,
     *         certificate.getVersion&#40;&#41;&#41;
     *         .subscribe&#40;certificateResponse -&gt; System.out.printf&#40;&quot;Received certificate with name %s and key id %s&quot;,
     *             certificateResponse.getName&#40;&#41;, certificateResponse.getKeyId&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificates -->
     *
     * @param includePending indicate if pending certificates should be included in the results.
     * @return A {@link PagedFlux} containing {@link CertificateProperties certificate} for all the certificates in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CertificateProperties> listPropertiesOfCertificates(boolean includePending) {
        return implClient.listPropertiesOfCertificatesAsync(includePending);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificates -->
     * <pre>
     * certificateAsyncClient.listPropertiesOfCertificates&#40;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;certificate -&gt; certificateAsyncClient.getCertificateVersion&#40;certificate.getName&#40;&#41;,
     *         certificate.getVersion&#40;&#41;&#41;
     *         .subscribe&#40;certificateResponse -&gt; System.out.printf&#40;&quot;Received certificate with name %s and key id %s&quot;,
     *             certificateResponse.getName&#40;&#41;, certificateResponse.getKeyId&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificates -->
     *
     * @return A {@link PagedFlux} containing {@link CertificateProperties certificate} for all the certificates in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CertificateProperties> listPropertiesOfCertificates() {
        return implClient.listPropertiesOfCertificatesAsync();
    }

    PagedFlux<CertificateProperties> listPropertiesOfCertificates(boolean includePending, Context context) {
        return implClient.listPropertiesOfCertificatesAsync(includePending, context);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.listDeletedCertificates -->
     * <pre>
     * certificateAsyncClient.listDeletedCertificates&#40;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;deletedCertificateResponse -&gt;  System.out.printf&#40;&quot;Deleted Certificate's Recovery Id %s %n&quot;,
     *         deletedCertificateResponse.getRecoveryId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.listDeletedCertificates -->
     *
     * @return A {@link PagedFlux} containing all of the {@link DeletedCertificate deleted certificates} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DeletedCertificate> listDeletedCertificates() {
        return implClient.listDeletedCertificatesAsync();
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.listDeletedCertificates -->
     * <pre>
     * certificateAsyncClient.listDeletedCertificates&#40;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;deletedCertificateResponse -&gt;  System.out.printf&#40;&quot;Deleted Certificate's Recovery Id %s %n&quot;,
     *         deletedCertificateResponse.getRecoveryId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.listDeletedCertificates -->
     *
     * @param includePending indicate if pending deleted certificates should be included in the results.
     * @return A {@link PagedFlux} containing all of the {@link DeletedCertificate deleted certificates} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DeletedCertificate> listDeletedCertificates(boolean includePending) {
        return implClient.listDeletedCertificatesAsync(includePending);
    }

    PagedFlux<DeletedCertificate> listDeletedCertificates(Boolean includePending, Context context) {
        return implClient.listDeletedCertificatesAsync(includePending, context);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificateVersions -->
     * <pre>
     * certificateAsyncClient.listPropertiesOfCertificateVersions&#40;&quot;certificateName&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;certificate -&gt; certificateAsyncClient.getCertificateVersion&#40;certificate.getName&#40;&#41;,
     *         certificate.getVersion&#40;&#41;&#41;
     *         .subscribe&#40;certificateResponse -&gt; System.out.printf&#40;&quot;Received certificate with name %s and key id %s&quot;,
     *             certificateResponse.getProperties&#40;&#41;.getName&#40;&#41;, certificateResponse.getKeyId&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificateVersions -->
     *
     * @param certificateName The name of the certificate.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException when a certificate with {@code certificateName} is empty string.
     * @return A {@link PagedFlux} containing {@link CertificateProperties certificate} of all the versions of the specified certificate in the vault. Flux is empty if certificate with {@code certificateName} does not exist in key vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CertificateProperties> listPropertiesOfCertificateVersions(String certificateName) {
        return implClient.listPropertiesOfCertificateVersionsAsync(certificateName);
    }

    PagedFlux<CertificateProperties> listPropertiesOfCertificateVersions(String certificateName, Context context) {
        return implClient.listPropertiesOfCertificateVersionsAsync(certificateName, context);
    }

    /**
     * Merges a certificate or a certificate chain with a key pair currently available in the service. This operation requires
     * the {@code certificates/create} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Merges a certificate with a kay pair available in the service.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificate#config -->
     * <pre>
     * List&lt;byte[]&gt; x509CertificatesToMerge = new ArrayList&lt;&gt;&#40;&#41;;
     * MergeCertificateOptions config =
     *     new MergeCertificateOptions&#40;&quot;certificateName&quot;, x509CertificatesToMerge&#41;.setEnabled&#40;false&#41;;
     * certificateAsyncClient.mergeCertificate&#40;config&#41;
     *     .subscribe&#40;certificate -&gt; System.out.printf&#40;&quot;Received Certificate with name %s and key id %s&quot;,
     *         certificate.getProperties&#40;&#41;.getName&#40;&#41;, certificate.getKeyId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificate#config -->
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
            return withContext(context -> implClient.mergeCertificateWithResponseAsync(mergeCertificateOptions, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Merges a certificate or a certificate chain with a key pair currently available in the service. This operation requires
     * the {@code certificates/create} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Merges a certificate with a kay pair available in the service.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificateWithResponse#config -->
     * <pre>
     * List&lt;byte[]&gt; x509CertsToMerge = new ArrayList&lt;&gt;&#40;&#41;;
     * MergeCertificateOptions mergeConfig =
     *     new MergeCertificateOptions&#40;&quot;certificateName&quot;, x509CertsToMerge&#41;.setEnabled&#40;false&#41;;
     * certificateAsyncClient.mergeCertificateWithResponse&#40;mergeConfig&#41;
     *     .subscribe&#40;certificateResponse -&gt; System.out.printf&#40;&quot;Received Certificate with name %s and key id %s&quot;,
     *         certificateResponse.getValue&#40;&#41;.getProperties&#40;&#41;.getName&#40;&#41;, certificateResponse.getValue&#40;&#41;.getKeyId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificateWithResponse#config -->
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
            return withContext(context -> implClient.mergeCertificateWithResponseAsync(mergeCertificateOptions, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }


    /**
     * Retrieves the policy of the specified certificate in the key vault. This operation requires the {@code certificates/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the policy of a certirifcate in the key vault. Prints out the
     * returned certificate policy details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificatePolicy#string -->
     * <pre>
     * certificateAsyncClient.getCertificatePolicy&#40;&quot;certificateName&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;policy -&gt;
     *         System.out.printf&#40;&quot;Certificate policy is returned with issuer name %s and subject name %s %n&quot;,
     *             policy.getIssuerName&#40;&#41;, policy.getSubject&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificatePolicy#string -->
     *
     * @param certificateName The name of the certificate whose policy is to be retrieved, cannot be null
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException if {@code certificateName} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the requested {@link CertificatePolicy certificate policy}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CertificatePolicy> getCertificatePolicy(String certificateName) {
        try {
            return withContext(context -> implClient.getCertificatePolicyWithResponseAsync(certificateName, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves the policy of the specified certificate in the key vault. This operation requires the {@code certificates/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the policy of a certirifcate in the key vault. Prints out the
     * returned certificate policy details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificatePolicyWithResponse#string -->
     * <pre>
     * certificateAsyncClient.getCertificatePolicyWithResponse&#40;&quot;certificateName&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;policyResponse -&gt;
     *         System.out.printf&#40;&quot;Certificate policy is returned with issuer name %s and subject name %s %n&quot;,
     *             policyResponse.getValue&#40;&#41;.getIssuerName&#40;&#41;, policyResponse.getValue&#40;&#41;.getSubject&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificatePolicyWithResponse#string -->
     *
     * @param certificateName The name of the certificate whose policy is to be retrieved, cannot be null
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException if {@code certificateName} is empty string.
     * @return A {@link Mono} containing the requested {@link CertificatePolicy certificate policy}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CertificatePolicy>> getCertificatePolicyWithResponse(String certificateName) {
        try {
            return withContext(context -> implClient.getCertificatePolicyWithResponseAsync(certificateName, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificatePolicy#string -->
     * <pre>
     * certificateAsyncClient.getCertificatePolicy&#40;&quot;certificateName&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;certificatePolicyResponseValue -&gt; &#123;
     *         CertificatePolicy certificatePolicy = certificatePolicyResponseValue;
     *         &#47;&#47; Update transparency
     *         certificatePolicy.setCertificateTransparent&#40;true&#41;;
     *         certificateAsyncClient.updateCertificatePolicy&#40;&quot;certificateName&quot;, certificatePolicy&#41;
     *             .subscribe&#40;updatedPolicy -&gt;
     *                 System.out.printf&#40;&quot;Certificate policy's updated transparency status %s %n&quot;,
     *                     updatedPolicy.isCertificateTransparent&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificatePolicy#string -->
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
            return withContext(context -> implClient.updateCertificatePolicyWithResponseAsync(certificateName, policy, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificatePolicyWithResponse#string -->
     * <pre>
     * certificateAsyncClient.getCertificatePolicy&#40;&quot;certificateName&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;certificatePolicyResponseValue -&gt; &#123;
     *         CertificatePolicy certificatePolicy = certificatePolicyResponseValue;
     *         &#47;&#47; Update transparency
     *         certificatePolicy.setCertificateTransparent&#40;true&#41;;
     *         certificateAsyncClient.updateCertificatePolicyWithResponse&#40;&quot;certificateName&quot;,
     *             certificatePolicy&#41;
     *             .subscribe&#40;updatedPolicyResponse -&gt;
     *                 System.out.printf&#40;&quot;Certificate policy's updated transparency status %s %n&quot;,
     *                     updatedPolicyResponse.getValue&#40;&#41;.isCertificateTransparent&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificatePolicyWithResponse#string -->
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
            return withContext(context -> implClient.updateCertificatePolicyWithResponseAsync(certificateName, policy, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.createIssuer#CertificateIssuer -->
     * <pre>
     * CertificateIssuer issuer = new CertificateIssuer&#40;&quot;issuerName&quot;, &quot;providerName&quot;&#41;
     *     .setAccountId&#40;&quot;keyvaultuser&quot;&#41;
     *     .setPassword&#40;&quot;fakePasswordPlaceholder&quot;&#41;;
     * certificateAsyncClient.createIssuer&#40;issuer&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;issuerResponse -&gt; &#123;
     *         System.out.printf&#40;&quot;Issuer created with %s and %s&quot;, issuerResponse.getName&#40;&#41;,
     *             issuerResponse.getProvider&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.createIssuer#CertificateIssuer -->
     *
     * @param issuer The configuration of the certificate issuer to be created.
     * @throws ResourceModifiedException when invalid certificate issuer {@code issuer} configuration is provided.
     * @throws HttpResponseException when a certificate issuer with {@code issuerName} is empty string.
     * @return A {@link Mono} containing the created {@link CertificateIssuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CertificateIssuer> createIssuer(CertificateIssuer issuer) {
        try {
            return withContext(context -> implClient.createIssuerWithResponseAsync(issuer, context))
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.createIssuerWithResponse#CertificateIssuer -->
     * <pre>
     * CertificateIssuer newIssuer = new CertificateIssuer&#40;&quot;issuerName&quot;, &quot;providerName&quot;&#41;
     *     .setAccountId&#40;&quot;keyvaultuser&quot;&#41;
     *     .setPassword&#40;&quot;fakePasswordPlaceholder&quot;&#41;;
     * certificateAsyncClient.createIssuerWithResponse&#40;newIssuer&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;issuerResponse -&gt; &#123;
     *         System.out.printf&#40;&quot;Issuer created with %s and %s&quot;, issuerResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *             issuerResponse.getValue&#40;&#41;.getProvider&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.createIssuerWithResponse#CertificateIssuer -->
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
            return withContext(context -> implClient.createIssuerWithResponseAsync(issuer, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves the specified certificate issuer from the key vault. This operation requires the certificates/manageissuers/getissuers permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the specificed certifcate issuer in the key vault. Prints out the
     * returned certificate issuer details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.getIssuerWithResponse#string -->
     * <pre>
     * certificateAsyncClient.getIssuerWithResponse&#40;&quot;issuerName&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;issuerResponse -&gt; &#123;
     *         System.out.printf&#40;&quot;Issuer returned with %s and %s&quot;, issuerResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *             issuerResponse.getValue&#40;&#41;.getProvider&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.getIssuerWithResponse#string -->
     *
     * @param issuerName The name of the certificate issuer to retrieve, cannot be null
     * @throws ResourceNotFoundException when a certificate issuer with {@code issuerName} doesn't exist in the key vault.
     * @throws HttpResponseException if {@code issuerName} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the requested {@link CertificateIssuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CertificateIssuer>> getIssuerWithResponse(String issuerName) {
        try {
            return withContext(context -> implClient.getIssuerWithResponseAsync(issuerName, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves the specified certificate issuer from the key vault. This operation requires the certificates/manageissuers/getissuers permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the specified certificate issuer in the key vault. Prints out the
     * returned certificate issuer details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.getIssuer#string -->
     * <pre>
     * certificateAsyncClient.getIssuer&#40;&quot;issuerName&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;issuer -&gt; &#123;
     *         System.out.printf&#40;&quot;Issuer returned with %s and %s&quot;, issuer.getName&#40;&#41;,
     *             issuer.getProvider&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.getIssuer#string -->
     *
     * @param issuerName The name of the certificate to retrieve, cannot be null
     * @throws ResourceNotFoundException when a certificate issuer with {@code issuerName} doesn't exist in the key vault.
     * @throws HttpResponseException if {@code issuerName} is empty string.
     * @return A {@link Mono} containing the requested {@link CertificateIssuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CertificateIssuer> getIssuer(String issuerName) {
        try {
            return withContext(context -> implClient.getIssuerWithResponseAsync(issuerName, context))
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteIssuerWithResponse#string -->
     * <pre>
     * certificateAsyncClient.deleteIssuerWithResponse&#40;&quot;issuerName&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;deletedIssuerResponse -&gt;
     *         System.out.printf&#40;&quot;Deleted issuer with name %s %n&quot;, deletedIssuerResponse.getValue&#40;&#41;.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteIssuerWithResponse#string -->
     *
     * @param issuerName The name of the certificate issuer to be deleted.
     * @throws ResourceNotFoundException when a certificate issuer with {@code issuerName} doesn't exist in the key vault.
     * @throws HttpResponseException when a certificate issuer with {@code issuerName} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link CertificateIssuer deleted issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CertificateIssuer>> deleteIssuerWithResponse(String issuerName) {
        try {
            return withContext(context -> implClient.deleteIssuerWithResponseAsync(issuerName, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteIssuer#string -->
     * <pre>
     * certificateAsyncClient.deleteIssuer&#40;&quot;issuerName&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;deletedIssuerResponse -&gt;
     *         System.out.printf&#40;&quot;Deleted issuer with name %s %n&quot;, deletedIssuerResponse.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteIssuer#string -->
     *
     * @param issuerName The name of the certificate issuer to be deleted.
     * @throws ResourceNotFoundException when a certificate issuer with {@code issuerName} doesn't exist in the key vault.
     * @throws HttpResponseException when a certificate issuer with {@code issuerName} is empty string.
     * @return A {@link Mono} containing the {@link CertificateIssuer deleted issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CertificateIssuer> deleteIssuer(String issuerName) {
        try {
            return withContext(context -> implClient.deleteIssuerWithResponseAsync(issuerName, context))
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }


    /**
     * List all the certificate issuers resources in the key vault. The individual certificate issuer response in the flux is represented by {@link IssuerProperties}
     * as only the certificate issuer identifier and provider are provided in the response. This operation requires the
     * {@code certificates/manageissuers/getissuers} permission.
     *
     * <p>It is possible to get the certificate issuer with all of its properties from this information. Convert the {@link PagedFlux}
     * containing {@link IssuerProperties issuerProperties} to {@link PagedFlux} containing {@link CertificateIssuer issuer} using
     * {@link CertificateAsyncClient#getIssuer(String)}
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.listPropertiesOfIssuers -->
     * <pre>
     * certificateAsyncClient.listPropertiesOfIssuers&#40;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;issuerProperties -&gt; certificateAsyncClient.getIssuer&#40;issuerProperties.getName&#40;&#41;&#41;
     *         .subscribe&#40;issuerResponse -&gt; System.out.printf&#40;&quot;Received issuer with name %s and provider %s&quot;,
     *             issuerResponse.getName&#40;&#41;, issuerResponse.getProvider&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.listPropertiesOfIssuers -->
     *
     * @return A {@link PagedFlux} containing all of the {@link IssuerProperties certificate issuers} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<IssuerProperties> listPropertiesOfIssuers() {
        return implClient.listPropertiesOfIssuersAsync();
    }

    /**
     * Updates the specified certificate issuer. The UpdateCertificateIssuer operation updates the specified attributes of
     * the certificate issuer entity. This operation requires the certificates/setissuers permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the certificate issuer, changes its attributes/properties then updates it in the Azure Key Vault. Prints out the
     * returned certificate issuer details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.updateIssuer#CertificateIssuer -->
     * <pre>
     * certificateAsyncClient.getIssuer&#40;&quot;issuerName&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;issuerResponseValue -&gt; &#123;
     *         CertificateIssuer issuer = issuerResponseValue;
     *         &#47;&#47;Update the enabled status of the issuer.
     *         issuer.setEnabled&#40;false&#41;;
     *         certificateAsyncClient.updateIssuer&#40;issuer&#41;
     *             .subscribe&#40;issuerResponse -&gt;
     *                 System.out.printf&#40;&quot;Issuer's enabled status %s %n&quot;,
     *                     issuerResponse.isEnabled&#40;&#41;.toString&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.updateIssuer#CertificateIssuer -->
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
            return withContext(context -> implClient.updateIssuerWithResponseAsync(issuer, context))
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.updateIssuer#CertificateIssuer -->
     * <pre>
     * certificateAsyncClient.getIssuer&#40;&quot;issuerName&quot;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;issuerResponseValue -&gt; &#123;
     *         CertificateIssuer issuer = issuerResponseValue;
     *         &#47;&#47;Update the enabled status of the issuer.
     *         issuer.setEnabled&#40;false&#41;;
     *         certificateAsyncClient.updateIssuer&#40;issuer&#41;
     *             .subscribe&#40;issuerResponse -&gt;
     *                 System.out.printf&#40;&quot;Issuer's enabled status %s %n&quot;,
     *                     issuerResponse.isEnabled&#40;&#41;.toString&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.updateIssuer#CertificateIssuer -->
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
            return withContext(context -> implClient.updateIssuerWithResponseAsync(issuer, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.setContacts#contacts -->
     * <pre>
     * CertificateContact contactToAdd = new CertificateContact&#40;&#41;.setName&#40;&quot;user&quot;&#41;.setEmail&#40;&quot;useremail&#64;example.com&quot;&#41;;
     * certificateAsyncClient.setContacts&#40;Collections.singletonList&#40;contactToAdd&#41;&#41;.subscribe&#40;contact -&gt;
     *     System.out.printf&#40;&quot;Contact name %s and email %s&quot;, contact.getName&#40;&#41;, contact.getEmail&#40;&#41;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.setContacts#contacts -->
     *
     * @param contacts The list of contacts to set on the vault.
     * @throws HttpResponseException when a contact information provided is invalid/incomplete.
     * @return A {@link PagedFlux} containing all of the {@link CertificateContact certificate contacts} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CertificateContact> setContacts(List<CertificateContact> contacts) {
        return implClient.setContactsAsync(contacts);
    }


    /**
     * Lists the certificate contacts in the key vault. This operation requires the certificates/managecontacts permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the certificate contacts in the Azure Key Vault. Prints out the
     * returned contacts details.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.listContacts -->
     * <pre>
     * certificateAsyncClient.listContacts&#40;&#41;.subscribe&#40;contact -&gt;
     *     System.out.printf&#40;&quot;Contact name %s and email %s&quot;, contact.getName&#40;&#41;, contact.getEmail&#40;&#41;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.listContacts -->
     *
     * @return A {@link PagedFlux} containing all of the {@link CertificateContact certificate contacts} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CertificateContact> listContacts() {
        return implClient.listContactsAsync();
    }

    /**
     * Deletes the certificate contacts in the key vault. This operation requires the {@code certificates/managecontacts} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the certificate contacts in the Azure Key Vault. Prints out the
     * deleted contacts details.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteContacts -->
     * <pre>
     * certificateAsyncClient.deleteContacts&#40;&#41;.subscribe&#40;contact -&gt;
     *     System.out.printf&#40;&quot;Deleted Contact name %s and email %s&quot;, contact.getName&#40;&#41;, contact.getEmail&#40;&#41;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteContacts -->
     *
     * @return A {@link PagedFlux} containing all of the {@link CertificateContact deleted certificate contacts} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CertificateContact> deleteContacts() {
        return implClient.deleteContactsAsync();
    }

    /**
     * Deletes the creation operation for the specified certificate that is in the process of being created. The certificate is
     * no longer created. This operation requires the {@code certificates/update permission}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Triggers certificate creation and then deletes the certificate creation operation in the Azure Key Vault. Prints out the
     * deleted certificate operation details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateOperation#string -->
     * <pre>
     * certificateAsyncClient.deleteCertificateOperation&#40;&quot;certificateName&quot;&#41;
     *     .subscribe&#40;certificateOperation -&gt; System.out.printf&#40;&quot;Deleted Certificate operation last status %s&quot;,
     *         certificateOperation.getStatus&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateOperation#string -->
     *
     * @param certificateName The name of the certificate which is in the process of being created.
     * @throws ResourceNotFoundException when a certificate operation for a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException when the {@code certificateName} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link CertificateOperation deleted certificate operation}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CertificateOperation> deleteCertificateOperation(String certificateName) {
        try {
            return withContext(context -> implClient.deleteCertificateOperationWithResponseAsync(certificateName, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateOperationWithResponse#string -->
     * <pre>
     * certificateAsyncClient.deleteCertificateOperationWithResponse&#40;&quot;certificateName&quot;&#41;
     *     .subscribe&#40;certificateOperationResponse -&gt; System.out.printf&#40;&quot;Deleted Certificate operation's last&quot;
     *         + &quot; status %s&quot;, certificateOperationResponse.getValue&#40;&#41;.getStatus&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateOperationWithResponse#string -->
     *
     * @param certificateName The name of the certificate which is in the process of being created.
     * @throws ResourceNotFoundException when a certificate operation for a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpResponseException when the {@code certificateName} is empty string.
     * @return A {@link Mono} containing the {@link CertificateOperation deleted certificate operation}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CertificateOperation>> deleteCertificateOperationWithResponse(String certificateName) {
        try {
            return withContext(context -> implClient.deleteCertificateOperationWithResponseAsync(certificateName, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Cancels a certificate creation operation that is already in progress. This operation requires the {@code certificates/update} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Triggers certificate creation and then cancels the certificate creation operation in the Azure Key Vault. Prints out the
     * updated certificate operation details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.cancelCertificateOperation#string -->
     * <pre>
     * certificateAsyncClient.cancelCertificateOperation&#40;&quot;certificateName&quot;&#41;
     *     .subscribe&#40;certificateOperation -&gt; System.out.printf&#40;&quot;Certificate operation status %s&quot;,
     *         certificateOperation.getStatus&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.cancelCertificateOperation#string -->
     *
     * @param certificateName The name of the certificate which is in the process of being created.
     * @throws ResourceNotFoundException when a certificate operation for a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when the {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link CertificateOperation cancelled certificate operation}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CertificateOperation> cancelCertificateOperation(String certificateName) {
        try {
            return withContext(context -> implClient.cancelCertificateOperationWithResponseAsync(certificateName, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Cancels a certificate creation operation that is already in progress. This operation requires the {@code certificates/update} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Triggers certificate creation and then cancels the certificate creation operation in the Azure Key Vault. Prints out the
     * updated certificate operation details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.cancelCertificateOperationWithResponse#string -->
     * <pre>
     * certificateAsyncClient.cancelCertificateOperationWithResponse&#40;&quot;certificateName&quot;&#41;
     *     .subscribe&#40;certificateOperationResponse -&gt; System.out.printf&#40;&quot;Certificate operation status %s&quot;,
     *         certificateOperationResponse.getValue&#40;&#41;.getStatus&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.cancelCertificateOperationWithResponse#string -->
     *
     * @param certificateName The name of the certificate which is in the process of being created.
     * @throws ResourceNotFoundException when a certificate operation for a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when the {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link CertificateOperation cancelled certificate operation}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CertificateOperation>> cancelCertificateOperationWithResponse(String certificateName) {
        try {
            return withContext(context -> implClient.cancelCertificateOperationWithResponseAsync(certificateName, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.importCertificate#options -->
     * <pre>
     * byte[] certificateToImport = new byte[100];
     * ImportCertificateOptions config =
     *     new ImportCertificateOptions&#40;&quot;certificateName&quot;, certificateToImport&#41;.setEnabled&#40;false&#41;;
     * certificateAsyncClient.importCertificate&#40;config&#41;
     *     .subscribe&#40;certificate -&gt; System.out.printf&#40;&quot;Received Certificate with name %s and key id %s&quot;,
     *         certificate.getProperties&#40;&#41;.getName&#40;&#41;, certificate.getKeyId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.importCertificate#options -->
     *
     * @param importCertificateOptions The details of the certificate to import to the key vault
     * @throws HttpResponseException when the {@code importCertificateOptions} are invalid.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultCertificateWithPolicy imported certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultCertificateWithPolicy> importCertificate(ImportCertificateOptions importCertificateOptions) {
        try {
            return withContext(context -> implClient.importCertificateWithResponseAsync(importCertificateOptions, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Imports a pre-existing certificate to the key vault. The specified certificate must be in PFX or PEM format,
     * and must contain the private key as well as the x509 certificates. This operation requires the {@code certificates/import} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Imports a certificate into the key vault.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.importCertificateWithResponse#options -->
     * <pre>
     * byte[] certToImport = new byte[100];
     * ImportCertificateOptions importCertificateOptions  =
     *     new ImportCertificateOptions&#40;&quot;certificateName&quot;, certToImport&#41;.setEnabled&#40;false&#41;;
     * certificateAsyncClient.importCertificateWithResponse&#40;importCertificateOptions&#41;
     *     .subscribe&#40;certificateResponse -&gt; System.out.printf&#40;&quot;Received Certificate with name %s and key id %s&quot;,
     *         certificateResponse.getValue&#40;&#41;.getProperties&#40;&#41;.getName&#40;&#41;, certificateResponse.getValue&#40;&#41;.getKeyId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.importCertificateWithResponse#options -->
     *
     * @param importCertificateOptions The details of the certificate to import to the key vault
     * @throws HttpResponseException when the {@code importCertificateOptions} are invalid.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultCertificateWithPolicy imported certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultCertificateWithPolicy>> importCertificateWithResponse(ImportCertificateOptions importCertificateOptions) {
        try {
            return withContext(context -> implClient.importCertificateWithResponseAsync(importCertificateOptions, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }
}

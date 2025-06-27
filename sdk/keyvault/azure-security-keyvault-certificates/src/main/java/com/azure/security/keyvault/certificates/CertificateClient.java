// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpRequestException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.certificates.implementation.CertificateClientImpl;
import com.azure.security.keyvault.certificates.implementation.CertificateIssuerHelper;
import com.azure.security.keyvault.certificates.implementation.CertificatePropertiesHelper;
import com.azure.security.keyvault.certificates.implementation.DeletedCertificateHelper;
import com.azure.security.keyvault.certificates.implementation.models.BackupCertificateResult;
import com.azure.security.keyvault.certificates.implementation.models.CertificateAttributes;
import com.azure.security.keyvault.certificates.implementation.models.CertificateBundle;
import com.azure.security.keyvault.certificates.implementation.models.CertificateCreateParameters;
import com.azure.security.keyvault.certificates.implementation.models.CertificateImportParameters;
import com.azure.security.keyvault.certificates.implementation.models.CertificateIssuerSetParameters;
import com.azure.security.keyvault.certificates.implementation.models.CertificateIssuerUpdateParameters;
import com.azure.security.keyvault.certificates.implementation.models.CertificateItem;
import com.azure.security.keyvault.certificates.implementation.models.CertificateMergeParameters;
import com.azure.security.keyvault.certificates.implementation.models.CertificateOperationUpdateParameter;
import com.azure.security.keyvault.certificates.implementation.models.CertificateRestoreParameters;
import com.azure.security.keyvault.certificates.implementation.models.CertificateUpdateParameters;
import com.azure.security.keyvault.certificates.implementation.models.Contacts;
import com.azure.security.keyvault.certificates.implementation.models.DeletedCertificateBundle;
import com.azure.security.keyvault.certificates.implementation.models.DeletedCertificateItem;
import com.azure.security.keyvault.certificates.implementation.models.IssuerBundle;
import com.azure.security.keyvault.certificates.models.CertificateContact;
import com.azure.security.keyvault.certificates.models.CertificateIssuer;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.CertificatePolicyAction;
import com.azure.security.keyvault.certificates.models.CertificateProperties;
import com.azure.security.keyvault.certificates.models.CreateCertificateOptions;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.security.keyvault.certificates.models.ImportCertificateOptions;
import com.azure.security.keyvault.certificates.models.IssuerProperties;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import com.azure.security.keyvault.certificates.models.LifetimeAction;
import com.azure.security.keyvault.certificates.models.MergeCertificateOptions;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.security.keyvault.certificates.CertificateAsyncClient.EMPTY_OPTIONS;
import static com.azure.security.keyvault.certificates.CertificateAsyncClient.mapContactsToPagedResponse;
import static com.azure.security.keyvault.certificates.CertificateAsyncClient.processCertificateOperationResponse;
import static com.azure.security.keyvault.certificates.CertificateAsyncClient.transformCertificateForImport;
import static com.azure.security.keyvault.certificates.implementation.CertificateIssuerHelper.createCertificateIssuer;
import static com.azure.security.keyvault.certificates.implementation.CertificateIssuerHelper.getIssuerBundle;
import static com.azure.security.keyvault.certificates.implementation.CertificateOperationHelper.createCertificateOperation;
import static com.azure.security.keyvault.certificates.implementation.CertificatePolicyHelper.createCertificatePolicy;
import static com.azure.security.keyvault.certificates.implementation.CertificatePolicyHelper.getImplCertificatePolicy;
import static com.azure.security.keyvault.certificates.implementation.DeletedCertificateHelper.createDeletedCertificate;
import static com.azure.security.keyvault.certificates.implementation.KeyVaultCertificateWithPolicyHelper.createCertificateWithPolicy;

/**
 * The CertificateClient provides synchronous methods to manage {@link KeyVaultCertificate certifcates} in the key
 * vault. The client supports creating, retrieving, updating, merging, deleting, purging, backing up, restoring and
 * listing the {@link KeyVaultCertificate certificates}. The client also supports listing
 * {@link DeletedCertificate deleted certificates} for a soft-delete enabled key vault.
 *
 * <p>The client further allows creating, retrieving, updating, deleting and listing the
 * {@link CertificateIssuer certificate issuers}. The client also supports creating, listing and deleting
 * {@link CertificateContact certificate contacts}</p>
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Azure Key Vault service, you will need to create an instance of the
 * {@link CertificateClient} class, a vault url and a credential object.</p>
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
 * <p><strong>Sample: Construct Synchronous Certificate Client</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link CertificateClient},
 * using the {@link CertificateClientBuilder} to configure it.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.instantiation -->
 * <pre>
 * CertificateClient certificateClient = new CertificateClientBuilder&#40;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;
 *     .httpLogOptions&#40;new HttpLogOptions&#40;&#41;.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.certificates.CertificateClient.instantiation  -->
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Create a Certificate</h2>
 * The {@link CertificateClient} can be used to create a certificate in the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously create a certificate in the key vault,
 * using the {@link CertificateClient#beginCreateCertificate(String, CertificatePolicy)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy -->
 * <pre>
 * CertificatePolicy certPolicy = new CertificatePolicy&#40;&quot;Self&quot;, &quot;CN=SelfSignedJavaPkcs12&quot;&#41;;
 *
 * SyncPoller&lt;CertificateOperation, KeyVaultCertificateWithPolicy&gt; certPoller =
 *     certificateClient.beginCreateCertificate&#40;&quot;certificateName&quot;, certPolicy&#41;;
 *
 * certPoller.waitUntil&#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41;;
 *
 * KeyVaultCertificate cert = certPoller.getFinalResult&#40;&#41;;
 *
 * System.out.printf&#40;&quot;Certificate created with name %s%n&quot;, cert.getName&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to {@link CertificateAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Get a Certificate</h2>
 * The {@link CertificateClient} can be used to retrieve a certificate from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously retrieve a certificate from the key vault, using
 * the {@link CertificateClient#getCertificate(String)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicy#string -->
 * <pre>
 * CertificatePolicy policy = certificateClient.getCertificatePolicy&#40;&quot;certificateName&quot;&#41;;
 * System.out.printf&#40;&quot;Received policy with subject name %s%n&quot;, policy.getSubject&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicy#string -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to {@link CertificateAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Delete a Certificate</h2>
 * The {@link CertificateClient} can be used to delete a certificate from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously delete a certificate from the
 * key vault, using the {@link CertificateClient#beginDeleteCertificate(String)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.beginDeleteCertificate#String -->
 * <pre>
 * SyncPoller&lt;DeletedCertificate, Void&gt; deleteCertPoller =
 *     certificateClient.beginDeleteCertificate&#40;&quot;certificateName&quot;&#41;;
 * &#47;&#47; Deleted Certificate is accessible as soon as polling beings.
 * PollResponse&lt;DeletedCertificate&gt; deleteCertPollResponse = deleteCertPoller.poll&#40;&#41;;
 * System.out.printf&#40;&quot;Deleted certificate with name %s and recovery id %s%n&quot;,
 *     deleteCertPollResponse.getValue&#40;&#41;.getName&#40;&#41;, deleteCertPollResponse.getValue&#40;&#41;.getRecoveryId&#40;&#41;&#41;;
 * deleteCertPoller.waitForCompletion&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.certificates.CertificateClient.beginDeleteCertificate#String -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to {@link CertificateAsyncClient}.</p>
 *
 * @see com.azure.security.keyvault.certificates
 * @see CertificateClientBuilder
 */
@ServiceClient(
    builder = CertificateClientBuilder.class,
    serviceInterfaces = CertificateClientImpl.CertificateClientService.class)
public final class CertificateClient {
    private static final ClientLogger LOGGER = new ClientLogger(CertificateClient.class);

    private final CertificateClientImpl implClient;
    private final String vaultUrl;

    /**
     * Creates a CertificateClient that uses {@code pipeline} to service requests
     *
     * @param implClient The implementation client to route requests through.
     * @param vaultUrl The vault url.
     */
    CertificateClient(CertificateClientImpl implClient, String vaultUrl) {
        this.implClient = implClient;
        this.vaultUrl = vaultUrl;
    }

    /**
     * Get the vault endpoint url to which service requests are sent to.
     * @return the vault endpoint url
     */
    public String getVaultUrl() {
        return vaultUrl;
    }

    /**
     * Creates a new certificate. If this is the first version, the certificate resource is created. This operation
     * requires the certificates/create permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Create certificate is a long-running operation. The following code waits indefinitely for the operation to
     * complete and returns its last status. The details of the last certificate operation status are printed when a
     * response is received</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy -->
     * <pre>
     * CertificatePolicy certPolicy = new CertificatePolicy&#40;&quot;Self&quot;, &quot;CN=SelfSignedJavaPkcs12&quot;&#41;;
     *
     * SyncPoller&lt;CertificateOperation, KeyVaultCertificateWithPolicy&gt; certPoller =
     *     certificateClient.beginCreateCertificate&#40;&quot;certificateName&quot;, certPolicy&#41;;
     *
     * certPoller.waitUntil&#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41;;
     *
     * KeyVaultCertificate cert = certPoller.getFinalResult&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Certificate created with name %s%n&quot;, cert.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy -->
     *
     * @param certificateName The name of the certificate to be created.
     * @param certificatePolicy The policy of the certificate to be created.
     * @throws NullPointerException if {@code certificatePolicy} is null.
     * @throws ResourceModifiedException when invalid certificate policy configuration is provided.
     * @return A {@link SyncPoller} to poll on the create certificate operation status.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy>
        beginCreateCertificate(String certificateName, CertificatePolicy certificatePolicy) {

        return beginCreateCertificate(certificateName, certificatePolicy, true, null);
    }

    /**
     * Creates a new certificate. If this is the first version, the certificate resource is created. This operation
     * requires the certificates/create permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Create certificate is a long-running operation. The following code waits indefinitely for the operation to
     * complete and returns its last status. The details of the last certificate operation status are printed when a
     * response is received</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy-Boolean-Map -->
     * <pre>
     * CertificatePolicy policy = new CertificatePolicy&#40;&quot;Self&quot;, &quot;CN=SelfSignedJavaPkcs12&quot;&#41;;
     * Map&lt;String, String&gt; tags = new HashMap&lt;&gt;&#40;&#41;;
     *
     * tags.put&#40;&quot;foo&quot;, &quot;bar&quot;&#41;;
     *
     * SyncPoller&lt;CertificateOperation, KeyVaultCertificateWithPolicy&gt; certificateSyncPoller =
     *     certificateClient.beginCreateCertificate&#40;&quot;certificateName&quot;, policy, true, tags&#41;;
     *
     * certificateSyncPoller.waitUntil&#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41;;
     *
     * KeyVaultCertificate createdCertificate = certificateSyncPoller.getFinalResult&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Certificate created with name %s%n&quot;, createdCertificate.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy-Boolean-Map -->
     *
     * @param certificateName The name of the certificate to be created.
     * @param certificatePolicy The policy of the certificate to be created.
     * @param isEnabled The enabled status of the certificate.
     * @param tags The tags to be associated with the certificate.
     * @throws NullPointerException if {@code certificatePolicy} is {@code null}.
     * @throws ResourceModifiedException when an invalid certificate policy configuration is provided.
     * @return A {@link SyncPoller} to poll on the create certificate operation status.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> beginCreateCertificate(
        String certificateName, CertificatePolicy certificatePolicy, Boolean isEnabled, Map<String, String> tags) {

        return beginCreateCertificate(
            new CreateCertificateOptions(certificateName, certificatePolicy).setEnabled(isEnabled)
                .setTags(tags)
                .setCertificateOrderPreserved(false));
    }

    /**
     * Creates a new certificate. If this is the first version, the certificate resource is created. This operation
     * requires the certificates/create permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Create certificate is a long-running operation. The following code waits indefinitely for the operation to
     * complete and returns its last status. The details of the last certificate operation status are printed when a
     * response is received</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.beginCreateCertificate#CreateCertificateOptions -->
     * <pre>
     * Map&lt;String, String&gt; certTags = new HashMap&lt;&gt;&#40;&#41;;
     *
     * tags.put&#40;&quot;foo&quot;, &quot;bar&quot;&#41;;
     *
     * CreateCertificateOptions createCertificateOptions =
     *     new CreateCertificateOptions&#40;&quot;certificateName&quot;, new CertificatePolicy&#40;&quot;Self&quot;, &quot;CN=SelfSignedJavaPkcs12&quot;&#41;&#41;
     *         .setEnabled&#40;true&#41;
     *         .setTags&#40;certTags&#41;
     *         .setCertificateOrderPreserved&#40;true&#41;;
     *
     * SyncPoller&lt;CertificateOperation, KeyVaultCertificateWithPolicy&gt; poller =
     *     certificateClient.beginCreateCertificate&#40;createCertificateOptions&#41;;
     *
     * poller.waitUntil&#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41;;
     *
     * KeyVaultCertificate certificate = poller.getFinalResult&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Certificate created with name %s%n&quot;, certificate.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.beginCreateCertificate#CreateCertificateOptions -->
     *
     * @param createCertificateOptions The configuration options to create a certificate with.
     * @throws NullPointerException if {@code createCertificateOptions} is null.
     * @throws ResourceModifiedException when an invalid certificate policy configuration is provided.
     * @return A {@link SyncPoller} to poll on the create certificate operation status.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy>
        beginCreateCertificate(CreateCertificateOptions createCertificateOptions) {

        if (createCertificateOptions == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'createCertificateOptions' cannot be null."));
        }

        return SyncPoller.createPoller(Duration.ofSeconds(1),
            ignored -> createCertificateActivation(createCertificateOptions.getName(),
                createCertificateOptions.getPolicy(), createCertificateOptions.isEnabled(),
                createCertificateOptions.getTags(), createCertificateOptions.isCertificateOrderPreserved()),
            ignored -> certificatePollOperation(createCertificateOptions.getName()),
            (ignored1, ignored2) -> certificateCancellationOperation(createCertificateOptions.getName()),
            ignored -> fetchCertificateOperation(createCertificateOptions.getName()));
    }

    private PollResponse<CertificateOperation> createCertificateActivation(String certificateName,
        CertificatePolicy certificatePolicy, Boolean isEnabled, Map<String, String> tags,
        Boolean preserveCertificateOrder) {

        CertificateCreateParameters certificateCreateParameters
            = new CertificateCreateParameters().setCertificatePolicy(getImplCertificatePolicy(certificatePolicy))
                .setCertificateAttributes(new CertificateAttributes().setEnabled(isEnabled))
                .setTags(tags)
                .setPreserveCertOrder(preserveCertificateOrder);

        return new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
            createCertificateOperation(callWithMappedException(
                () -> implClient
                    .createCertificateWithResponse(certificateName, BinaryData.fromObject(certificateCreateParameters),
                        EMPTY_OPTIONS)
                    .getValue()
                    .toObject(
                        com.azure.security.keyvault.certificates.implementation.models.CertificateOperation.class),
                CertificateAsyncClient::mapCreateCertificateException)));
    }

    private PollResponse<CertificateOperation> certificatePollOperation(String certificateName) {
        return processCertificateOperationResponse(callWithMappedException(
            () -> implClient.getCertificateOperationWithResponse(certificateName, EMPTY_OPTIONS)
                .getValue()
                .toObject(com.azure.security.keyvault.certificates.implementation.models.CertificateOperation.class),
            CertificateAsyncClient::mapGetCertificateOperationException));
    }

    private CertificateOperation certificateCancellationOperation(String certificateName) {
        CertificateOperationUpdateParameter certificateOperationUpdateParameter
            = new CertificateOperationUpdateParameter(true);

        return createCertificateOperation(callWithMappedException(
            () -> implClient
                .updateCertificateOperationWithResponse(certificateName,
                    BinaryData.fromObject(certificateOperationUpdateParameter), EMPTY_OPTIONS)
                .getValue()
                .toObject(com.azure.security.keyvault.certificates.implementation.models.CertificateOperation.class),
            CertificateAsyncClient::mapUpdateCertificateOperationException));
    }

    private KeyVaultCertificateWithPolicy fetchCertificateOperation(String certificateName) {
        return createCertificateWithPolicy(
            callWithMappedException(() -> implClient.getCertificateWithResponse(certificateName, null, EMPTY_OPTIONS)
                .getValue()
                .toObject(CertificateBundle.class), CertificateAsyncClient::mapGetCertificateException));
    }

    /**
     * Gets a pending {@link CertificateOperation} from the key vault. This operation requires the certificates/get
     * permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Geta a pending certificate operation. The {@link SyncPoller poller} allows users to automatically poll on the
     * certificate operation status.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.getCertificateOperation#String -->
     * <pre>
     * SyncPoller&lt;CertificateOperation, KeyVaultCertificateWithPolicy&gt; getCertPoller = certificateClient
     *     .getCertificateOperation&#40;&quot;certificateName&quot;&#41;;
     * getCertPoller.waitUntil&#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41;;
     * KeyVaultCertificate cert = getCertPoller.getFinalResult&#40;&#41;;
     * System.out.printf&#40;&quot;Certificate created with name %s%n&quot;, cert.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.getCertificateOperation#String -->
     *
     * @param certificateName The name of the certificate.
     * @throws ResourceNotFoundException when a certificate operation for a certificate with {@code certificateName}
     * doesn't exist.
     * @return A {@link SyncPoller} to poll on the certificate operation status.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy>
        getCertificateOperation(String certificateName) {

        return SyncPoller.createPoller(Duration.ofSeconds(1),
            ignored -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null),
            ignored -> certificatePollOperation(certificateName),
            (ignored1, ignored2) -> certificateCancellationOperation(certificateName),
            ignored -> fetchCertificateOperation(certificateName));
    }

    /**
     * Gets information about the latest version of the specified certificate. This operation requires the
     * certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the certificate in the key vault. Prints out the returned certificate details when
     * a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.getCertificate#String -->
     * <pre>
     * KeyVaultCertificateWithPolicy certificate = certificateClient.getCertificate&#40;&quot;certificateName&quot;&#41;;
     * System.out.printf&#40;&quot;Received certificate with name %s and version %s and secret id %s%n&quot;,
     *     certificate.getProperties&#40;&#41;.getName&#40;&#41;,
     *     certificate.getProperties&#40;&#41;.getVersion&#40;&#41;, certificate.getSecretId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.getCertificate#String -->
     *
     * @param certificateName The name of the certificate to retrieve, cannot be null
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code certificateName} is empty string.
     * @return The requested {@link KeyVaultCertificateWithPolicy certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultCertificateWithPolicy getCertificate(String certificateName) {
        return getCertificateWithResponse(certificateName, Context.NONE).getValue();
    }

    /**
     * Gets information about the latest version of the specified certificate. This operation requires the
     * certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the certificate in the key vault. Prints out the returned certificate details when
     * a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.getCertificateWithResponse#String-Context -->
     * <pre>
     * Response&lt;KeyVaultCertificateWithPolicy&gt; certificateWithResponse = certificateClient
     *     .getCertificateWithResponse&#40;&quot;certificateName&quot;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Received certificate with name %s and version %s and secret id %s%n&quot;,
     *     certificateWithResponse.getValue&#40;&#41;.getProperties&#40;&#41;.getName&#40;&#41;,
     *     certificateWithResponse.getValue&#40;&#41;.getProperties&#40;&#41;.getVersion&#40;&#41;, certificate.getSecretId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.getCertificateWithResponse#String-Context -->
     *
     * @param certificateName The name of the certificate to retrieve, cannot be null
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code certificateName} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the requested
     * {@link KeyVaultCertificateWithPolicy certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultCertificateWithPolicy> getCertificateWithResponse(String certificateName, Context context) {
        return callWithMappedResponseAndException(
            () -> implClient.getCertificateWithResponse(certificateName, null,
                new RequestOptions().setContext(context)),
            binaryData -> createCertificateWithPolicy(binaryData.toObject(CertificateBundle.class)),
            CertificateAsyncClient::mapGetCertificateException);
    }

    /**
     * Gets information about the latest version of the specified certificate. This operation requires the
     * certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the certificate in the key vault. Prints out the returned certificate details when
     * a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.getCertificateVersionWithResponse#String-String-Context -->
     * <pre>
     * Response&lt;KeyVaultCertificate&gt; returnedCertificateWithResponse = certificateClient
     *     .getCertificateVersionWithResponse&#40;&quot;certificateName&quot;, &quot;certificateVersion&quot;,
     *         new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Received certificate with name %s and version %s and secret id %s%n&quot;,
     *     returnedCertificateWithResponse.getValue&#40;&#41;.getProperties&#40;&#41;.getName&#40;&#41;,
     *     returnedCertificateWithResponse.getValue&#40;&#41;.getProperties&#40;&#41;.getVersion&#40;&#41;,
     *     returnedCertificateWithResponse.getValue&#40;&#41;.getSecretId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.getCertificateVersionWithResponse#String-String-Context -->
     *
     * @param certificateName The name of the certificate to retrieve, cannot be null
     * @param version The version of the certificate to retrieve. If this is an empty String or null then latest version
     * of the certificate is retrieved.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code certificateName} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the requested
     * {@link KeyVaultCertificate certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultCertificate> getCertificateVersionWithResponse(String certificateName, String version,
        Context context) {
        return callWithMappedResponseAndException(
            () -> implClient.getCertificateWithResponse(certificateName, version,
                new RequestOptions().setContext(context)),
            binaryData -> createCertificateWithPolicy(binaryData.toObject(CertificateBundle.class)),
            CertificateAsyncClient::mapGetCertificateException);
    }

    /**
     * Gets information about the specified version of the specified certificate. This operation requires the
     * certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the certificate in the key vault. Prints out the returned certificate details when
     * a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.getCertificateVersion#String-String -->
     * <pre>
     * KeyVaultCertificate returnedCertificate = certificateClient.getCertificateVersion&#40;&quot;certificateName&quot;,
     *     &quot;certificateVersion&quot;&#41;;
     * System.out.printf&#40;&quot;Received certificate with name %s and version %s and secret id %s%n&quot;,
     *     returnedCertificate.getProperties&#40;&#41;.getName&#40;&#41;, returnedCertificate.getProperties&#40;&#41;.getVersion&#40;&#41;,
     *     returnedCertificate.getSecretId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.getCertificateVersion#String-String -->
     *
     * @param certificateName The name of the certificate to retrieve, cannot be null
     * @param version The version of the certificate to retrieve. If this is an empty String or null then latest version
     * of the certificate is retrieved.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code certificateName} is empty string.
     * @return The requested {@link KeyVaultCertificate certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultCertificate getCertificateVersion(String certificateName, String version) {
        return getCertificateVersionWithResponse(certificateName, version, Context.NONE).getValue();
    }

    /**
     * Updates the specified attributes associated with the specified certificate. The update operation changes
     * specified attributes of an existing stored certificate and attributes that are not specified in the request are
     * left unchanged. This operation requires the certificates/update permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets latest version of the certificate, changes its tags and enabled status and then updates it in the Azure
     * Key Vault. Prints out the returned certificate details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.updateCertificateProperties#CertificateProperties -->
     * <pre>
     * KeyVaultCertificate certificate = certificateClient.getCertificate&#40;&quot;certificateName&quot;&#41;;
     * &#47;&#47; Update certificate enabled status
     * certificate.getProperties&#40;&#41;.setEnabled&#40;false&#41;;
     * KeyVaultCertificate updatedCertificate = certificateClient.updateCertificateProperties&#40;certificate.getProperties&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Updated Certificate with name %s and enabled status %s%n&quot;,
     *     updatedCertificate.getProperties&#40;&#41;.getName&#40;&#41;, updatedCertificate.getProperties&#40;&#41;.isEnabled&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.updateCertificateProperties#CertificateProperties -->
     *
     * @param properties The {@link CertificateProperties} object with updated properties.
     * @throws NullPointerException if {@code properties} is null.
     * @throws ResourceNotFoundException when a certificate with {@link CertificateProperties#getName() certificateName}
     * and {@link CertificateProperties#getVersion() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link CertificateProperties#getName() certificateName} or
     * {@link CertificateProperties#getVersion() version} is empty string.
     * @return The {@link CertificateProperties updated certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultCertificate updateCertificateProperties(CertificateProperties properties) {
        return updateCertificatePropertiesWithResponse(properties, Context.NONE).getValue();
    }

    /**
     * Updates the specified attributes associated with the specified certificate. The update operation changes
     * specified attributes of an existing stored certificate and attributes that are not specified in the request are
     * left unchanged. This operation requires the certificates/update permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets latest version of the certificate, changes its tags and enabled status and then updates it in the Azure
     * Key Vault. Prints out the returned certificate details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePropertiesWithResponse#CertificateProperties-Context -->
     * <pre>
     * KeyVaultCertificate certificateToUpdate = certificateClient.getCertificate&#40;&quot;certificateName&quot;&#41;;
     * &#47;&#47; Update certificate enabled status
     * certificateToUpdate.getProperties&#40;&#41;.setEnabled&#40;false&#41;;
     * Response&lt;KeyVaultCertificate&gt; updatedCertificateResponse = certificateClient.
     *     updateCertificatePropertiesWithResponse&#40;certificateToUpdate.getProperties&#40;&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Updated Certificate with name %s and enabled status %s%n&quot;,
     *     updatedCertificateResponse.getValue&#40;&#41;.getProperties&#40;&#41;.getName&#40;&#41;,
     *     updatedCertificateResponse.getValue&#40;&#41;.getProperties&#40;&#41;.isEnabled&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePropertiesWithResponse#CertificateProperties-Context -->
     *
     * @param properties The {@link CertificateProperties} object with updated properties.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws NullPointerException if {@code properties} is null.
     * @throws ResourceNotFoundException when a certificate with {@link CertificateProperties#getName() certificateName}
     * and {@link CertificateProperties#getVersion() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link CertificateProperties#getName() certificateName} or
     * {@link CertificateProperties#getVersion() version} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link CertificateProperties updated certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultCertificate> updateCertificatePropertiesWithResponse(CertificateProperties properties,
        Context context) {

        if (properties == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'properties' cannot be null."));
        }

        CertificateAttributes certificateAttributes = new CertificateAttributes().setEnabled(properties.isEnabled())
            .setExpires(properties.getExpiresOn())
            .setNotBefore(properties.getNotBefore());

        CertificateUpdateParameters certificateUpdateParameters
            = new CertificateUpdateParameters().setCertificateAttributes(certificateAttributes)
                .setTags(properties.getTags());

        Response<BinaryData> response
            = implClient.updateCertificateWithResponse(properties.getName(), properties.getVersion(),
                BinaryData.fromObject(certificateUpdateParameters), new RequestOptions().setContext(context));

        return new SimpleResponse<>(response,
            createCertificateWithPolicy(response.getValue().toObject(CertificateBundle.class)));
    }

    /**
     * Deletes a certificate from a specified key vault. All the versions of the certificate along with its associated
     * policy get deleted. If soft-delete is enabled on the key vault then the certificate is placed in the deleted
     * state and requires to be purged for permanent deletion else the certificate is permanently deleted. The delete
     * operation applies to any certificate stored in Azure Key Vault, but it cannot be applied to an individual version
     * of a certificate. This operation requires the certificates/delete permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the certificate in the Azure Key Vault. Prints out the deleted certificate details when a response has
     * been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.beginDeleteCertificate#String -->
     * <pre>
     * SyncPoller&lt;DeletedCertificate, Void&gt; deleteCertPoller =
     *     certificateClient.beginDeleteCertificate&#40;&quot;certificateName&quot;&#41;;
     * &#47;&#47; Deleted Certificate is accessible as soon as polling beings.
     * PollResponse&lt;DeletedCertificate&gt; deleteCertPollResponse = deleteCertPoller.poll&#40;&#41;;
     * System.out.printf&#40;&quot;Deleted certificate with name %s and recovery id %s%n&quot;,
     *     deleteCertPollResponse.getValue&#40;&#41;.getName&#40;&#41;, deleteCertPollResponse.getValue&#40;&#41;.getRecoveryId&#40;&#41;&#41;;
     * deleteCertPoller.waitForCompletion&#40;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.beginDeleteCertificate#String -->
     *
     * @param certificateName The name of the certificate to be deleted.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code certificateName} is empty string.
     * @return A {@link SyncPoller} to poll on and retrieve {@link DeletedCertificate deleted certificate}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<DeletedCertificate, Void> beginDeleteCertificate(String certificateName) {
        return SyncPoller.createPoller(Duration.ofSeconds(1), ignored -> deleteCertificateActivation(certificateName),
            pollingContext -> deleteCertificatePollOperation(certificateName, pollingContext),
            (pollingContext, firstResponse) -> null, pollingContext -> null);
    }

    private PollResponse<DeletedCertificate> deleteCertificateActivation(String certificateName) {
        return new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
            createDeletedCertificate(
                callWithMappedException(
                    () -> implClient.deleteCertificateWithResponse(certificateName, EMPTY_OPTIONS)
                        .getValue()
                        .toObject(DeletedCertificateBundle.class),
                    CertificateAsyncClient::mapDeleteCertificateException)));
    }

    private PollResponse<DeletedCertificate> deleteCertificatePollOperation(String certificateName,
        PollingContext<DeletedCertificate> pollingContext) {
        try {
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                createDeletedCertificate(implClient.getDeletedCertificateWithResponse(certificateName, EMPTY_OPTIONS)
                    .getValue()
                    .toObject(DeletedCertificateBundle.class)));
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
        } catch (Exception e) {
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
     * <p><strong>Code Samples</strong></p>
     * <p> Gets the deleted certificate from the key vault enabled for soft-delete. Prints out the deleted certificate
     * details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.getDeletedCertificate#string -->
     * <pre>
     * DeletedCertificate deletedCertificate = certificateClient.getDeletedCertificate&#40;&quot;certificateName&quot;&#41;;
     * System.out.printf&#40;&quot;Deleted certificate with name %s and recovery id %s%n&quot;, deletedCertificate.getName&#40;&#41;,
     *     deletedCertificate.getRecoveryId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.getDeletedCertificate#string -->
     *
     * @param certificateName The name of the deleted certificate.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code certificateName} is empty string.
     * @return The {@link DeletedCertificate deleted certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DeletedCertificate getDeletedCertificate(String certificateName) {
        return getDeletedCertificateWithResponse(certificateName, Context.NONE).getValue();
    }

    /**
     * Retrieves information about the specified deleted certificate. The GetDeletedCertificate operation  is applicable
     * for soft-delete enabled vaults and additionally retrieves deleted certificate's attributes, such as retention
     * interval, scheduled permanent deletion and the current deletion recovery level. This operation requires the
     * certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Gets the deleted certificate from the key vault enabled for soft-delete. Prints out the deleted certificate
     * details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.getDeletedCertificateWithResponse#String-Context -->
     * <pre>
     * Response&lt;DeletedCertificate&gt; deletedCertificateWithResponse = certificateClient
     *     .getDeletedCertificateWithResponse&#40;&quot;certificateName&quot;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Deleted certificate with name %s and recovery id %s%n&quot;,
     *     deletedCertificateWithResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *     deletedCertificateWithResponse.getValue&#40;&#41;.getRecoveryId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.getDeletedCertificateWithResponse#String-Context -->
     *
     * @param certificateName The name of the deleted certificate.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code certificateName} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DeletedCertificate deleted certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DeletedCertificate> getDeletedCertificateWithResponse(String certificateName, Context context) {
        Response<BinaryData> response
            = implClient.getDeletedCertificateWithResponse(certificateName, new RequestOptions().setContext(context));

        return new SimpleResponse<>(response,
            createDeletedCertificate(response.getValue().toObject(DeletedCertificateBundle.class)));
    }

    /**
     * Permanently deletes the specified deleted certificate without possibility for recovery. The Purge Deleted
     * Certificate operation is applicable for soft-delete enabled vaults and is not available if the recovery level
     * does not specify 'Purgeable'. This operation requires the certificate/purge permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Purges the deleted certificate from the key vault enabled for soft-delete. Prints out the status code from the
     * server response when a response has been received.</p>
    
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.purgeDeletedCertificate#string -->
     * <pre>
     * certificateClient.purgeDeletedCertificate&#40;&quot;certificateName&quot;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.purgeDeletedCertificate#string -->
     *
     * @param certificateName The name of the deleted certificate.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code certificateName} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void purgeDeletedCertificate(String certificateName) {
        purgeDeletedCertificateWithResponse(certificateName, Context.NONE);
    }

    /**
     * Permanently deletes the specified deleted certificate without possibility for recovery. The Purge Deleted
     * Certificate operation is applicable for soft-delete enabled vaults and is not available if the recovery level
     * does not specify 'Purgeable'. This operation requires the certificate/purge permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Purges the deleted certificate from the key vault enabled for soft-delete. Prints out the status code from the
     * server response when a response has been received.</p>
    
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.purgeDeletedCertificateWithResponse#string-Context -->
     * <pre>
     * Response&lt;Void&gt; purgeResponse = certificateClient.purgeDeletedCertificateWithResponse&#40;&quot;certificateName&quot;,
     *     new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Purged Deleted certificate with status %d%n&quot;, purgeResponse.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.purgeDeletedCertificateWithResponse#string-Context -->
     *
     * @param certificateName The name of the deleted certificate.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code certificateName} is empty string.
     * @return A response containing status code and HTTP headers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> purgeDeletedCertificateWithResponse(String certificateName, Context context) {
        return implClient.purgeDeletedCertificateWithResponse(certificateName,
            new RequestOptions().setContext(context));
    }

    /**
     * Recovers the deleted certificate back to its current version under /certificates and can only be performed on a
     * soft-delete enabled vault. The RecoverDeletedCertificate operation performs the reversal of the Delete operation
     * and must be issued during the retention interval (available in the deleted certificate's attributes). This
     * operation requires the certificates/recover permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Recovers the deleted certificate from the key vault enabled for soft-delete. Prints out the recovered
     * certificate details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.beginRecoverDeletedCertificate#String -->
     * <pre>
     * SyncPoller&lt;KeyVaultCertificateWithPolicy, Void&gt; recoverDeletedCertPoller = certificateClient
     *     .beginRecoverDeletedCertificate&#40;&quot;deletedCertificateName&quot;&#41;;
     * &#47;&#47; Recovered certificate is accessible as soon as polling beings
     * PollResponse&lt;KeyVaultCertificateWithPolicy&gt; recoverDeletedCertPollResponse = recoverDeletedCertPoller.poll&#40;&#41;;
     * System.out.printf&#40;&quot; Recovered Deleted certificate with name %s and id %s%n&quot;,
     *     recoverDeletedCertPollResponse.getValue&#40;&#41;.getProperties&#40;&#41;.getName&#40;&#41;,
     *     recoverDeletedCertPollResponse.getValue&#40;&#41;.getProperties&#40;&#41;.getId&#40;&#41;&#41;;
     * recoverDeletedCertPoller.waitForCompletion&#40;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.beginRecoverDeletedCertificate#String -->
     *
     * @param certificateName The name of the deleted certificate to be recovered.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the
     * certificate vault.
     * @throws HttpRequestException when a certificate with {@code certificateName} is empty string.
     * @return A {@link SyncPoller} to poll on and retrieve {@link KeyVaultCertificateWithPolicy recovered certificate}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<KeyVaultCertificateWithPolicy, Void> beginRecoverDeletedCertificate(String certificateName) {
        return SyncPoller.createPoller(Duration.ofSeconds(1),
            ignored -> recoverDeletedCertificateActivation(certificateName),
            pollingContext -> recoverDeletedCertificatePollOperation(certificateName, pollingContext),
            (pollingContext, firstResponse) -> null, pollingContext -> null);
    }

    private PollResponse<KeyVaultCertificateWithPolicy> recoverDeletedCertificateActivation(String certificateName) {
        return new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
            createCertificateWithPolicy(implClient.recoverDeletedCertificateWithResponse(certificateName, EMPTY_OPTIONS)
                .getValue()
                .toObject(CertificateBundle.class)));
    }

    private PollResponse<KeyVaultCertificateWithPolicy> recoverDeletedCertificatePollOperation(String certificateName,
        PollingContext<KeyVaultCertificateWithPolicy> pollingContext) {
        try {
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                createCertificateWithPolicy(implClient.getCertificateWithResponse(certificateName, null, EMPTY_OPTIONS)
                    .getValue()
                    .toObject(CertificateBundle.class)));
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                    pollingContext.getLatestResponse().getValue());
            } else {
                // This means permission is not granted for the get deleted key operation.
                // In both cases deletion operation was successful when activation operation succeeded before
                // reaching here.
                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    pollingContext.getLatestResponse().getValue());
            }
        } catch (Exception e) {
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
     * <p><strong>Code Samples</strong></p>
     * <p>Backs up the certificate from the key vault. Prints out the length of the certificate's backup byte array
     * returned in the response.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.backupCertificate#string -->
     * <pre>
     * byte[] certificateBackup = certificateClient.backupCertificate&#40;&quot;certificateName&quot;&#41;;
     * System.out.printf&#40;&quot;Backed up certificate with back up blob length %d%n&quot;, certificateBackup.length&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.backupCertificate#string -->
     *
     * @param certificateName The name of the certificate.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code certificateName} is empty string.
     * @return The backed up certificate blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public byte[] backupCertificate(String certificateName) {
        return backupCertificateWithResponse(certificateName, Context.NONE).getValue();
    }

    /**
     * Requests that a backup of the specified certificate be downloaded to the client. All versions of the certificate
     * will be downloaded. This operation requires the certificates/backup permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Backs up the certificate from the key vault. Prints out the length of the certificate's backup byte array
     * returned in the response.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.backupCertificateWithResponse#String-Context -->
     * <pre>
     * Response&lt;byte[]&gt; certificateBackupWithResponse = certificateClient
     *     .backupCertificateWithResponse&#40;&quot;certificateName&quot;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Backed up certificate with back up blob length %d%n&quot;,
     *     certificateBackupWithResponse.getValue&#40;&#41;.length&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.backupCertificateWithResponse#String-Context -->
     *
     * @param certificateName The certificateName of the certificate.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code certificateName} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the backed up certificate blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<byte[]> backupCertificateWithResponse(String certificateName, Context context) {
        Response<BinaryData> response
            = implClient.backupCertificateWithResponse(certificateName, new RequestOptions().setContext(context));

        return new SimpleResponse<>(response, response.getValue().toObject(BackupCertificateResult.class).getValue());
    }

    /**
     * Restores a backed up certificate to the vault. All the versions of the certificate are restored to the vault.
     * This operation requires the certificates/restore permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Restores the certificate in the key vault from its backup. Prints out the restored certificate details when a
     * response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.restoreCertificate#byte -->
     * <pre>
     * byte[] certificateBackupBlob = &#123;&#125;;
     * KeyVaultCertificate certificate = certificateClient.restoreCertificateBackup&#40;certificateBackupBlob&#41;;
     * System.out.printf&#40;&quot; Restored certificate with name %s and id %s%n&quot;,
     *     certificate.getProperties&#40;&#41;.getName&#40;&#41;, certificate.getProperties&#40;&#41;.getId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.restoreCertificate#byte -->
     *
     * @param backup The backup blob associated with the certificate.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     * @return The {@link KeyVaultCertificate restored certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultCertificateWithPolicy restoreCertificateBackup(byte[] backup) {
        return restoreCertificateBackupWithResponse(backup, Context.NONE).getValue();
    }

    /**
     * Restores a backed up certificate to the vault. All the versions of the certificate are restored to the vault.
     * This operation requires the certificates/restore permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Restores the certificate in the key vault from its backup. Prints out the restored certificate details when a
     * response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.restoreCertificateWithResponse#byte-Context -->
     * <pre>
     * byte[] certificateBackupBlobArray = &#123;&#125;;
     * Response&lt;KeyVaultCertificateWithPolicy&gt; certificateResponse = certificateClient
     *     .restoreCertificateBackupWithResponse&#40;certificateBackupBlobArray, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot; Restored certificate with name %s and id %s%n&quot;,
     *     certificateResponse.getValue&#40;&#41;.getProperties&#40;&#41;.getName&#40;&#41;,
     *     certificateResponse.getValue&#40;&#41;.getProperties&#40;&#41;.getId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.restoreCertificateWithResponse#byte-Context -->
     *
     * @param backup The backup blob associated with the certificate.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultCertificate restored certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultCertificateWithPolicy> restoreCertificateBackupWithResponse(byte[] backup,
        Context context) {

        CertificateRestoreParameters certificateRestoreParameters = new CertificateRestoreParameters(backup);

        return callWithMappedResponseAndException(
            () -> implClient.restoreCertificateWithResponse(BinaryData.fromObject(certificateRestoreParameters),
                new RequestOptions().setContext(context)),
            binaryData -> createCertificateWithPolicy(binaryData.toObject(CertificateBundle.class)),
            CertificateAsyncClient::mapRestoreCertificateException);
    }

    /**
     * List certificates in the key vault. Retrieves the set of certificates resources in the key vault and the
     * individual certificate response in the iterable is represented by {@link CertificateProperties} as only the
     * certificate identifier, thumbprint, attributes and tags are provided in the response. The policy and individual
     * certificate versions are not listed in the response. This operation requires the certificates/list permission.
     *
     * <p>It is possible to get certificates with all the properties excluding the policy from this information. Loop
     * over the {@link CertificateProperties} and call {@link CertificateClient#getCertificateVersion(String, String)} .
     * This will return the {@link KeyVaultCertificate certificate} with all its properties excluding the policy.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.listCertificates -->
     * <pre>
     * for &#40;CertificateProperties certificateProperties : certificateClient.listPropertiesOfCertificates&#40;&#41;&#41; &#123;
     *     KeyVaultCertificate certificateWithAllProperties = certificateClient
     *         .getCertificateVersion&#40;certificateProperties.getName&#40;&#41;, certificateProperties.getVersion&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Received certificate with name %s and secret id %s%n&quot;,
     *         certificateWithAllProperties.getProperties&#40;&#41;.getName&#40;&#41;,
     *         certificateWithAllProperties.getSecretId&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.listCertificates -->
     *
     * @return A {@link PagedIterable} containing {@link CertificateProperties certificate} for all the certificates in
     * the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateProperties> listPropertiesOfCertificates() {
        return listPropertiesOfCertificates(false, Context.NONE);
    }

    /**
     * List certificates in the key vault. Retrieves the set of certificates resources in the key vault and the
     * individual certificate response in the iterable is represented by {@link CertificateProperties} as only the
     * certificate identifier, thumbprint, attributes and tags are provided in the response. The policy and individual
     * certificate versions are not listed in the response. This operation requires the certificates/list permission.
     *
     * <p>It is possible to get certificates with all the properties excluding the policy from this information. Loop
     * over the {@link CertificateProperties} and call {@link CertificateClient#getCertificateVersion(String, String)} .
     * This will return the {@link KeyVaultCertificate certificate} with all its properties excluding the policy.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.listCertificates#context -->
     * <pre>
     * for &#40;CertificateProperties certificateProperties : certificateClient
     *     .listPropertiesOfCertificates&#40;true, new Context&#40;key1, value1&#41;&#41;&#41; &#123;
     *     KeyVaultCertificate certificateWithAllProperties = certificateClient
     *         .getCertificateVersion&#40;certificateProperties.getName&#40;&#41;, certificateProperties.getVersion&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Received certificate with name %s and secret id %s%n&quot;,
     *         certificateWithAllProperties.getProperties&#40;&#41;.getName&#40;&#41;,
     *         certificateWithAllProperties.getSecretId&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.listCertificates#context -->
     *
     * @param includePending indicate if pending certificates should be included in the results.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} containing {@link CertificateProperties certificate} for all the certificates in
     * the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateProperties> listPropertiesOfCertificates(boolean includePending, Context context) {
        RequestOptions requestOptions = new RequestOptions().setContext(context)
            .addQueryParam("includePending", String.valueOf(includePending), false);

        return implClient.getCertificates(requestOptions)
            .mapPage(binaryData -> CertificatePropertiesHelper
                .createCertificateProperties(binaryData.toObject(CertificateItem.class)));
    }

    /**
     * Lists the {@link DeletedCertificate deleted certificates} in the key vault currently available for recovery. This
     * operation includes deletion-specific information and is applicable for vaults enabled for soft-delete. This
     * operation requires the certificates/get/list permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the deleted certificates in the key vault. Prints out the recovery id of each deleted certificate when a
     * response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.listDeletedCertificates -->
     * <pre>
     * for &#40;DeletedCertificate deletedCertificate : certificateClient.listDeletedCertificates&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Deleted certificate's recovery Id %s%n&quot;, deletedCertificate.getRecoveryId&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.listDeletedCertificates -->
     *
     * @return A {@link PagedIterable} containing all of the {@link DeletedCertificate deleted certificates} in the
     * vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedCertificate> listDeletedCertificates() {
        return listDeletedCertificates(false, Context.NONE);
    }

    /**
     * Lists the {@link DeletedCertificate deleted certificates} in the key vault currently available for recovery. This
     * operation includes deletion-specific information and is applicable for vaults enabled for soft-delete. This
     * operation requires the certificates/get/list permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the deleted certificates in the key vault. Prints out the recovery id of each deleted certificate when a
     * response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.listDeletedCertificates#context -->
     * <pre>
     * for &#40;DeletedCertificate deletedCertificate : certificateClient
     *     .listDeletedCertificates&#40;true, new Context&#40;key1, value1&#41;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Deleted certificate's recovery Id %s%n&quot;, deletedCertificate.getRecoveryId&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.listDeletedCertificates#context -->
     *
     * @param includePending indicate if pending deleted certificates should be included in the results.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} containing all of the {@link DeletedCertificate deleted certificates} in the
     * vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedCertificate> listDeletedCertificates(boolean includePending, Context context) {
        RequestOptions requestOptions = new RequestOptions().setContext(context)
            .addQueryParam("includePending", String.valueOf(includePending), false);

        return implClient.getDeletedCertificates(requestOptions)
            .mapPage(binaryData -> DeletedCertificateHelper
                .createDeletedCertificate(binaryData.toObject(DeletedCertificateItem.class)));
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
     * {@link KeyVaultCertificate certificate} with all its properties excluding the policy.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.listCertificateVersions -->
     * <pre>
     * for &#40;CertificateProperties certificateProperties : certificateClient
     *     .listPropertiesOfCertificateVersions&#40;&quot;certificateName&quot;&#41;&#41; &#123;
     *     KeyVaultCertificate certificateWithAllProperties = certificateClient
     *         .getCertificateVersion&#40;certificateProperties.getName&#40;&#41;, certificateProperties.getVersion&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Received certificate's version with name %s, version %s and secret id %s%n&quot;,
     *         certificateWithAllProperties.getProperties&#40;&#41;.getName&#40;&#41;,
     *         certificateWithAllProperties.getProperties&#40;&#41;.getVersion&#40;&#41;, certificateWithAllProperties.getSecretId&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.listCertificateVersions -->
     *
     * @param certificateName The name of the certificate.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code certificateName} is empty string.
     * @return A {@link PagedIterable} containing {@link CertificateProperties certificate} of all the versions of the
     * specified certificate in the vault. Paged Iterable is empty if certificate with {@code certificateName} does not
     * exist in key vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateProperties> listPropertiesOfCertificateVersions(String certificateName) {
        return listPropertiesOfCertificateVersions(certificateName, Context.NONE);
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
     * {@link KeyVaultCertificate certificate} with all its properties excluding the policy.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.listCertificateVersions#context -->
     * <pre>
     * for &#40;CertificateProperties certificateProperties : certificateClient
     *     .listPropertiesOfCertificateVersions&#40;&quot;certificateName&quot;&#41;&#41; &#123;
     *     KeyVaultCertificate certificateWithAllProperties = certificateClient
     *         .getCertificateVersion&#40;certificateProperties.getName&#40;&#41;, certificateProperties.getVersion&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Received certificate's version with name %s, version %s and secret id %s%n&quot;,
     *         certificateWithAllProperties.getProperties&#40;&#41;.getName&#40;&#41;,
     *         certificateWithAllProperties.getProperties&#40;&#41;.getVersion&#40;&#41;, certificateWithAllProperties.getSecretId&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.listCertificateVersions#context -->
     *
     * @param certificateName The name of the certificate.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code certificateName} is empty string.
     * @return A {@link PagedIterable} containing {@link CertificateProperties certificate} of all the versions of the
     * specified certificate in the vault. Iterable is empty if certificate with {@code certificateName} does not exist
     * in key vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateProperties> listPropertiesOfCertificateVersions(String certificateName,
        Context context) {

        return implClient.getCertificateVersions(certificateName, new RequestOptions().setContext(context))
            .mapPage(binaryData -> CertificatePropertiesHelper
                .createCertificateProperties(binaryData.toObject(CertificateItem.class)));
    }

    /**
     * Retrieves the policy of the specified certificate in the key vault. This operation requires the certificates/get
     * permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the policy of a certificate in the key vault. Prints out the returned certificate policy details when a
     * response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicy#string -->
     * <pre>
     * CertificatePolicy policy = certificateClient.getCertificatePolicy&#40;&quot;certificateName&quot;&#41;;
     * System.out.printf&#40;&quot;Received policy with subject name %s%n&quot;, policy.getSubject&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicy#string -->
     *
     * @param certificateName The name of the certificate whose policy is to be retrieved, cannot be null
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code certificateName} is empty string.
     * @return The requested {@link CertificatePolicy certificate policy}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificatePolicy getCertificatePolicy(String certificateName) {
        return getCertificatePolicyWithResponse(certificateName, Context.NONE).getValue();
    }

    /**
     * Retrieves the policy of the specified certificate in the key vault. This operation requires the certificates/get
     * permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the policy of a certificate in the key vault. Prints out the returned certificate policy details when a
     * response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicyWithResponse#string -->
     * <pre>
     * Response&lt;CertificatePolicy&gt; returnedPolicyWithResponse = certificateClient.getCertificatePolicyWithResponse&#40;
     *     &quot;certificateName&quot;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Received policy with subject name %s%n&quot;,
     *     returnedPolicyWithResponse.getValue&#40;&#41;.getSubject&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicyWithResponse#string -->
     *
     * @param certificateName The name of the certificate whose policy is to be retrieved, cannot be null
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the requested
     * {@link CertificatePolicy certificate policy}.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code certificateName} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificatePolicy> getCertificatePolicyWithResponse(String certificateName, Context context) {
        return callWithMappedResponseAndException(
            () -> implClient.getCertificatePolicyWithResponse(certificateName,
                new RequestOptions().setContext(context)),
            binaryData -> createCertificatePolicy(binaryData
                .toObject(com.azure.security.keyvault.certificates.implementation.models.CertificatePolicy.class)),
            CertificateAsyncClient::mapGetCertificatePolicyException);
    }

    /**
     * Updates the policy for a certificate. The update operation changes specified attributes of the certificate policy
     * and attributes that are not specified in the request are left unchanged. This operation requires the
     * certificates/update permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the certificate policy, changes its properties and then updates it in the Azure Key Vault. Prints out the
     * returned policy details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePolicy#string -->
     * <pre>
     * CertificatePolicy certificatePolicy = certificateClient.getCertificatePolicy&#40;&quot;certificateName&quot;&#41;;
     * &#47;&#47;Update the certificate policy cert transparency property.
     * certificatePolicy.setCertificateTransparent&#40;true&#41;;
     * CertificatePolicy updatedCertPolicy = certificateClient.updateCertificatePolicy&#40;&quot;certificateName&quot;,
     *     certificatePolicy&#41;;
     * System.out.printf&#40;&quot;Updated Certificate Policy transparency status %s%n&quot;,
     *     updatedCertPolicy.isCertificateTransparent&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePolicy#string -->
     *
     * @param certificateName The name of the certificate whose policy is to be updated.
     * @param certificatePolicy The certificate policy to be updated.
     * @throws NullPointerException if {@code certificatePolicy} is null.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code certificateName} is empty string or if {@code certificatePolicy} is invalid.
     * @return The updated {@link CertificatePolicy certificate policy}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificatePolicy updateCertificatePolicy(String certificateName, CertificatePolicy certificatePolicy) {
        return updateCertificatePolicyWithResponse(certificateName, certificatePolicy, Context.NONE).getValue();
    }

    /**
     * Updates the policy for a certificate. The update operation changes specified attributes of the certificate policy
     * and attributes that are not specified in the request are left unchanged. This operation requires the
     * certificates/update permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the certificate policy, changes its properties and then updates it in the Azure Key Vault. Prints out the
     * returned policy details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePolicyWithResponse#string -->
     * <pre>
     * CertificatePolicy certificatePolicyToUpdate = certificateClient.getCertificatePolicy&#40;&quot;certificateName&quot;&#41;;
     * &#47;&#47;Update the certificate policy cert transparency property.
     * certificatePolicyToUpdate.setCertificateTransparent&#40;true&#41;;
     * Response&lt;CertificatePolicy&gt; updatedCertPolicyWithResponse = certificateClient
     *     .updateCertificatePolicyWithResponse&#40;&quot;certificateName&quot;, certificatePolicyToUpdate,
     *         new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Updated Certificate Policy transparency status %s%n&quot;, updatedCertPolicyWithResponse
     *     .getValue&#40;&#41;.isCertificateTransparent&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePolicyWithResponse#string -->
     *
     * @param certificateName The certificateName of the certificate whose policy is to be updated.
     * @param certificatePolicy The certificate policy to be updated.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws NullPointerException if {@code certificatePolicy} is null.
     * @throws ResourceNotFoundException when a certificate with {@code certificateName} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code certificateName} is empty string or if {@code certificatePolicy} is invalid.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the updated
     * {@link CertificatePolicy certificate policy}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificatePolicy> updateCertificatePolicyWithResponse(String certificateName,
        CertificatePolicy certificatePolicy, Context context) {

        if (certificatePolicy == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'certificatePolicy' cannot be null."));
        }

        Response<BinaryData> response = implClient.updateCertificatePolicyWithResponse(certificateName,
            BinaryData.fromObject(getImplCertificatePolicy(certificatePolicy)),
            new RequestOptions().setContext(context));

        return new SimpleResponse<>(response, createCertificatePolicy(response.getValue()
            .toObject(com.azure.security.keyvault.certificates.implementation.models.CertificatePolicy.class)));
    }

    /**
     * Creates the specified certificate issuer. The SetCertificateIssuer operation updates the specified certificate
     * issuer if it already exists or adds it if it doesn't exist. This operation requires the
     * certificates/setissuers permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new certificate issuer in the key vault. Prints out the created certificate issuer details when a
     * response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.createIssuer#CertificateIssuer -->
     * <pre>
     * CertificateIssuer issuerToCreate = new CertificateIssuer&#40;&quot;myissuer&quot;, &quot;myProvider&quot;&#41;
     *     .setAccountId&#40;&quot;testAccount&quot;&#41;
     *     .setAdministratorContacts&#40;Collections.singletonList&#40;new AdministratorContact&#40;&#41;.setFirstName&#40;&quot;test&quot;&#41;
     *         .setLastName&#40;&quot;name&quot;&#41;.setEmail&#40;&quot;test&#64;example.com&quot;&#41;&#41;&#41;;
     * CertificateIssuer returnedIssuer = certificateClient.createIssuer&#40;issuerToCreate&#41;;
     * System.out.printf&#40;&quot;Created Issuer with name %s provider %s%n&quot;, returnedIssuer.getName&#40;&#41;,
     *     returnedIssuer.getProvider&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.createIssuer#CertificateIssuer -->
     *
     * @param issuer The configuration of the certificate issuer to be created.
     * @throws NullPointerException if {@code issuer} is null.
     * @throws ResourceModifiedException when invalid certificate issuer {@code issuer} configuration is provided.
     * @throws HttpRequestException when a certificate issuer with {@link CertificateIssuer#getName() name} is empty
     * string.
     * @return The created {@link CertificateIssuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificateIssuer createIssuer(CertificateIssuer issuer) {
        return createIssuerWithResponse(issuer, Context.NONE).getValue();
    }

    /**
     * Creates the specified certificate issuer. The SetCertificateIssuer operation updates the specified certificate
     * issuer if it already exists or adds it if it doesn't exist. This operation requires the
     * certificates/setissuers permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new certificate issuer in the key vault. Prints out the created certificate issuer details when a
     * response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.createIssuerWithResponse#CertificateIssuer-Context -->
     * <pre>
     * CertificateIssuer issuer = new CertificateIssuer&#40;&quot;issuerName&quot;, &quot;myProvider&quot;&#41;
     *     .setAccountId&#40;&quot;testAccount&quot;&#41;
     *     .setAdministratorContacts&#40;Collections.singletonList&#40;new AdministratorContact&#40;&#41;.setFirstName&#40;&quot;test&quot;&#41;
     *         .setLastName&#40;&quot;name&quot;&#41;.setEmail&#40;&quot;test&#64;example.com&quot;&#41;&#41;&#41;;
     * Response&lt;CertificateIssuer&gt; issuerResponse = certificateClient.createIssuerWithResponse&#40;issuer,
     *     new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Created Issuer with name %s provider %s%n&quot;, issuerResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *     issuerResponse.getValue&#40;&#41;.getProvider&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.createIssuerWithResponse#CertificateIssuer-Context -->
     *
     * @param issuer The configuration of the certificate issuer to be created.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws NullPointerException if {@code issuer} is null.
     * @throws ResourceModifiedException when invalid certificate issuer {@code issuer} configuration is provided.
     * @throws HttpRequestException when a certificate issuer with {@link CertificateIssuer#getName() name} is empty
     * string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the created
     * {@link CertificateIssuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificateIssuer> createIssuerWithResponse(CertificateIssuer issuer, Context context) {
        if (issuer == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'issuer' cannot be null."));
        }

        IssuerBundle issuerBundle = getIssuerBundle(issuer);
        CertificateIssuerSetParameters certificateIssuerSetParameters
            = new CertificateIssuerSetParameters(issuerBundle.getProvider())
                .setOrganizationDetails(issuerBundle.getOrganizationDetails())
                .setCredentials(issuerBundle.getCredentials())
                .setAttributes(issuerBundle.getAttributes());

        Response<BinaryData> response = implClient.setCertificateIssuerWithResponse(issuer.getName(),
            BinaryData.fromObject(certificateIssuerSetParameters), new RequestOptions().setContext(context));

        return new SimpleResponse<>(response, createCertificateIssuer(response.getValue()
            .toObject(com.azure.security.keyvault.certificates.implementation.models.IssuerBundle.class)));
    }

    /**
     * Retrieves the specified certificate issuer from the key vault. This operation requires the
     * certificates/manageissuers/getissuers permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the specified certificate issuer in the key vault. Prints out the returned certificate issuer details
     * when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.getIssuerWithResponse#string-context -->
     * <pre>
     * Response&lt;CertificateIssuer&gt; issuerResponse = certificateClient.getIssuerWithResponse&#40;&quot;issuerName&quot;,
     *     new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Retrieved issuer with name %s and provider %s%n&quot;, issuerResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *     issuerResponse.getValue&#40;&#41;.getProvider&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.getIssuerWithResponse#string-context -->
     *
     * @param issuerName The name of the certificate issuer to retrieve, cannot be null
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate issuer with {@code issuerName} doesn't exist in the key
     * vault.
     * @throws HttpRequestException if {@code issuerName} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the requested
     * {@link CertificateIssuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificateIssuer> getIssuerWithResponse(String issuerName, Context context) {
        Response<BinaryData> response
            = implClient.getCertificateIssuerWithResponse(issuerName, new RequestOptions().setContext(context));

        return new SimpleResponse<>(response,
            createCertificateIssuer(response.getValue().toObject(IssuerBundle.class)));
    }

    /**
     * Retrieves the specified certificate issuer from the key vault. This operation requires the
     * certificates/manageissuers/getissuers permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the specified certificate issuer in the key vault. Prints out the returned certificate issuer details
     * when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.getIssuer#string -->
     * <pre>
     * CertificateIssuer returnedIssuer = certificateClient.getIssuer&#40;&quot;issuerName&quot;&#41;;
     * System.out.printf&#40;&quot;Retrieved issuer with name %s and provider %s%n&quot;, returnedIssuer.getName&#40;&#41;,
     *     returnedIssuer.getProvider&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.getIssuer#string -->
     *
     * @param issuerName The name of the certificate issuer to retrieve, cannot be null
     * @throws ResourceNotFoundException when a certificate issuer with {@code issuerName} doesn't exist in the key
     * vault.
     * @throws HttpRequestException if {@code issuerName} is empty string.
     * @return The requested {@link CertificateIssuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificateIssuer getIssuer(String issuerName) {
        return getIssuerWithResponse(issuerName, Context.NONE).getValue();
    }

    /**
     * Deletes the specified certificate issuer. The DeleteCertificateIssuer operation permanently removes the specified
     * certificate issuer from the key vault. This operation requires the certificates/manageissuers/deleteissuers
     * permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the certificate issuer in the Azure Key Vault. Prints out the
     * deleted certificate details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.deleteIssuerWithResponse#string-context -->
     * <pre>
     * CertificateIssuer deletedIssuer = certificateClient.deleteIssuer&#40;&quot;issuerName&quot;&#41;;
     * System.out.printf&#40;&quot;Deleted certificate issuer with name %s and provider id %s%n&quot;, deletedIssuer.getName&#40;&#41;,
     *     deletedIssuer.getProvider&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.deleteIssuerWithResponse#string-context -->
     *
     * @param issuerName The name of the certificate issuer to be deleted.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate issuer with {@code issuerName} doesn't exist in the key
     * vault.
     * @throws HttpRequestException when a certificate issuer with {@code issuerName} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link CertificateIssuer deleted issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificateIssuer> deleteIssuerWithResponse(String issuerName, Context context) {
        Response<BinaryData> response
            = implClient.deleteCertificateIssuerWithResponse(issuerName, new RequestOptions().setContext(context));

        return new SimpleResponse<>(response,
            createCertificateIssuer(response.getValue().toObject(IssuerBundle.class)));
    }

    /**
     * Deletes the specified certificate issuer. The DeleteCertificateIssuer operation permanently removes the specified
     * certificate issuer from the key vault. This operation requires the certificates/manageissuers/deleteissuers
     * permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the certificate issuer in the Azure Key Vault. Prints out the deleted certificate details when a
     * response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.deleteIssuer#string -->
     * <pre>
     * Response&lt;CertificateIssuer&gt; deletedIssuerWithResponse = certificateClient.
     *     deleteIssuerWithResponse&#40;&quot;issuerName&quot;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Deleted certificate issuer with name %s and provider id %s%n&quot;,
     *     deletedIssuerWithResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *     deletedIssuerWithResponse.getValue&#40;&#41;.getProvider&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.deleteIssuer#string -->
     *
     * @param issuerName The name of the certificate issuer to be deleted.
     * @throws ResourceNotFoundException when a certificate issuer with {@code issuerName} doesn't exist in the key
     * vault.
     * @throws HttpRequestException when a certificate issuer with {@code issuerName} is empty string.
     * @return The {@link CertificateIssuer deleted issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificateIssuer deleteIssuer(String issuerName) {
        return deleteIssuerWithResponse(issuerName, Context.NONE).getValue();
    }

    /**
     * List all the certificate issuers resources in the key vault. The individual certificate issuer response in the
     * iterable is represented by {@link IssuerProperties} as only the certificate issuer identifier and provider are
     * provided in the response. This operation requires the certificates/manageissuers/getissuers permission.
     *
     * <p>It is possible to get the certificate issuer with all of its properties from this information. Loop over the
     * {@link IssuerProperties issuerProperties} and call {@link CertificateClient#getIssuer(String)}. This will return
     * the {@link CertificateIssuer issuer} with all its properties.</p>.
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers -->
     * <pre>
     * for &#40;IssuerProperties issuer : certificateClient.listPropertiesOfIssuers&#40;&#41;&#41; &#123;
     *     CertificateIssuer retrievedIssuer = certificateClient.getIssuer&#40;issuer.getName&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Received issuer with name %s and provider %s%n&quot;, retrievedIssuer.getName&#40;&#41;,
     *         retrievedIssuer.getProvider&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers -->
     *
     * @return A {@link PagedIterable} containing all of the {@link IssuerProperties certificate issuers} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<IssuerProperties> listPropertiesOfIssuers() {
        return listPropertiesOfIssuers(Context.NONE);
    }

    /**
     * List all the certificate issuers resources in the key vault. The individual certificate issuer response in the
     * iterable is represented by {@link IssuerProperties} as only the certificate issuer identifier and provider are
     * provided in the response. This operation requires the certificates/manageissuers/getissuers permission.
     *
     * <p>It is possible to get the certificate issuer with all of its properties from this information. Loop over the
     * {@link IssuerProperties issuerProperties} and call {@link CertificateClient#getIssuer(String)}. This will return
     * the {@link CertificateIssuer issuer} with all its properties.</p>.
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers#context -->
     * <pre>
     * for &#40;IssuerProperties issuer : certificateClient.listPropertiesOfIssuers&#40;new Context&#40;key1, value1&#41;&#41;&#41; &#123;
     *     CertificateIssuer retrievedIssuer = certificateClient.getIssuer&#40;issuer.getName&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Received issuer with name %s and provider %s%n&quot;, retrievedIssuer.getName&#40;&#41;,
     *         retrievedIssuer.getProvider&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers#context -->
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} containing all of the {@link IssuerProperties certificate issuers} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<IssuerProperties> listPropertiesOfIssuers(Context context) {
        return implClient.getCertificateIssuers(new RequestOptions().setContext(context))
            .mapPage(issuer -> issuer.toObject(IssuerProperties.class));
    }

    /**
     * Updates the specified certificate issuer. The UpdateCertificateIssuer operation updates the specified attributes
     * of the certificate issuer entity. This operation requires the certificates/setissuers permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the certificate issuer, changes its attributes/properties then updates it in the Azure Key Vault. Prints
     * out the returned certificate issuer details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.updateIssuer#CertificateIssuer -->
     * <pre>
     * CertificateIssuer returnedIssuer = certificateClient.getIssuer&#40;&quot;issuerName&quot;&#41;;
     * returnedIssuer.setAccountId&#40;&quot;newAccountId&quot;&#41;;
     * CertificateIssuer updatedIssuer = certificateClient.updateIssuer&#40;returnedIssuer&#41;;
     * System.out.printf&#40;&quot;Updated issuer with name %s, provider %s and account Id %s%n&quot;, updatedIssuer.getName&#40;&#41;,
     *     updatedIssuer.getProvider&#40;&#41;, updatedIssuer.getAccountId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.updateIssuer#CertificateIssuer -->
     *
     * @param issuer The {@link CertificateIssuer issuer} with updated properties.
     * @throws NullPointerException if {@code issuer} is null.
     * @throws ResourceNotFoundException when a certificate issuer with {@link CertificateIssuer#getName() name} doesn't
     * exist in the key vault.
     * @throws HttpRequestException if {@link CertificateIssuer#getName() name} is empty string.
     * @return The {@link CertificateIssuer updated issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificateIssuer updateIssuer(CertificateIssuer issuer) {
        return updateIssuerWithResponse(issuer, Context.NONE).getValue();
    }

    /**
     * Updates the specified certificate issuer. The UpdateCertificateIssuer operation updates the specified attributes
     * of the certificate issuer entity. This operation requires the certificates/setissuers permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the certificate issuer, changes its attributes/properties then updates it in the Azure Key Vault. Prints
     * out the returned certificate issuer details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.updateIssuerWithResponse#CertificateIssuer-Context -->
     * <pre>
     * CertificateIssuer issuer = certificateClient.getIssuer&#40;&quot;issuerName&quot;&#41;;
     * returnedIssuer.setAccountId&#40;&quot;newAccountId&quot;&#41;;
     * Response&lt;CertificateIssuer&gt; updatedIssuerWithResponse = certificateClient.updateIssuerWithResponse&#40;issuer,
     *     new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Updated issuer with name %s, provider %s and account Id %s%n&quot;,
     *     updatedIssuerWithResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *     updatedIssuerWithResponse.getValue&#40;&#41;.getProvider&#40;&#41;,
     *     updatedIssuerWithResponse.getValue&#40;&#41;.getAccountId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.updateIssuerWithResponse#CertificateIssuer-Context -->
     *
     * @param issuer The {@link CertificateIssuer issuer} with updated properties.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws NullPointerException if {@code issuer} is null.
     * @throws ResourceNotFoundException when a certificate issuer with {@link CertificateIssuer#getName() name} doesn't
     * exist in the key vault.
     * @throws HttpRequestException if {@link CertificateIssuer#getName() name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link CertificateIssuer updated issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificateIssuer> updateIssuerWithResponse(CertificateIssuer issuer, Context context) {
        if (issuer == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'issuer' cannot be null."));
        }

        IssuerBundle issuerBundle = CertificateIssuerHelper.getIssuerBundle(issuer);
        CertificateIssuerUpdateParameters certificateIssuerUpdateParameters
            = new CertificateIssuerUpdateParameters().setProvider(issuerBundle.getProvider())
                .setAttributes(issuerBundle.getAttributes())
                .setCredentials(issuerBundle.getCredentials())
                .setOrganizationDetails(issuerBundle.getOrganizationDetails());

        Response<BinaryData> response = implClient.updateCertificateIssuerWithResponse(issuer.getName(),
            BinaryData.fromObject(certificateIssuerUpdateParameters), new RequestOptions().setContext(context));

        return new SimpleResponse<>(response,
            createCertificateIssuer(response.getValue().toObject(IssuerBundle.class)));
    }

    /**
     * Sets the certificate contacts on the key vault. This operation requires the certificates/managecontacts
     * permission.
     *
     *<p>The {@link LifetimeAction} of type {@link CertificatePolicyAction#EMAIL_CONTACTS} set on a
     * {@link CertificatePolicy} emails the contacts set on the vault when triggered.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Sets the certificate contacts in the Azure Key Vault. Prints out the returned contacts details.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.setContacts#contacts -->
     * <pre>
     * CertificateContact contactToAdd = new CertificateContact&#40;&#41;.setName&#40;&quot;user&quot;&#41;.setEmail&#40;&quot;useremail&#64;example.com&quot;&#41;;
     * for &#40;CertificateContact contact : certificateClient.setContacts&#40;Collections.singletonList&#40;contactToAdd&#41;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Added contact with name %s and email %s to key vault%n&quot;, contact.getName&#40;&#41;,
     *         contact.getEmail&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.setContacts#contacts -->
     *
     * @param contacts The list of contacts to set on the vault.
     * @throws HttpRequestException when a contact information provided is invalid/incomplete.
     * @return A {@link PagedIterable} containing all of the {@link CertificateContact certificate contacts} in the
     * vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateContact> setContacts(List<CertificateContact> contacts) {
        return setContacts(contacts, Context.NONE);
    }

    /**
     * Sets the certificate contacts on the key vault. This operation requires the certificates/managecontacts
     * permission.
     *
     *<p>The {@link LifetimeAction} of type {@link CertificatePolicyAction#EMAIL_CONTACTS} set on a
     * {@link CertificatePolicy} emails the contacts set on the vault when triggered.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Sets the certificate contacts in the Azure Key Vault. Prints out the returned contacts details.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.setContacts#contacts-context -->
     * <pre>
     * CertificateContact sampleContact = new CertificateContact&#40;&#41;.setName&#40;&quot;user&quot;&#41;.setEmail&#40;&quot;useremail&#64;example.com&quot;&#41;;
     * for &#40;CertificateContact contact : certificateClient.setContacts&#40;Collections.singletonList&#40;sampleContact&#41;,
     *     new Context&#40;key1, value1&#41;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Added contact with name %s and email %s to key vault%n&quot;, contact.getName&#40;&#41;,
     *         contact.getEmail&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.setContacts#contacts-context -->
     *
     * @param contacts The list of contacts to set on the vault.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws HttpRequestException when a contact information provided is invalid/incomplete.
     * @return A {@link PagedIterable} containing all of the {@link CertificateContact certificate contacts} in the
     * vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateContact> setContacts(List<CertificateContact> contacts, Context context) {
        return new PagedIterable<>(() -> mapContactsToPagedResponse(implClient.setCertificateContactsWithResponse(
            BinaryData.fromObject(new Contacts().setContactList(contacts)), new RequestOptions().setContext(context))));
    }

    /**
     * Lists the certificate contacts in the key vault. This operation requires the certificates/managecontacts
     * permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the certificate contacts in the Azure Key Vault. Prints out the returned contacts details in the
     * response.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.listContacts -->
     * <pre>
     * for &#40;CertificateContact contact : certificateClient.listContacts&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Added contact with name %s and email %s to key vault%n&quot;, contact.getName&#40;&#41;,
     *         contact.getEmail&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.listContacts -->
     *
     * @return A {@link PagedIterable} containing all of the {@link CertificateContact certificate contacts} in the
     * vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateContact> listContacts() {
        return listContacts(Context.NONE);
    }

    /**
     * Lists the certificate contacts in the key vault. This operation requires the certificates/managecontacts
     * permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the certificate contacts in the Azure Key Vault. Prints out the returned contacts details in the
     * response.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.listContacts#context -->
     * <pre>
     * for &#40;CertificateContact contact : certificateClient.listContacts&#40;new Context&#40;key1, value1&#41;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Added contact with name %s and email %s to key vault%n&quot;, contact.getName&#40;&#41;,
     *         contact.getEmail&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.listContacts#context -->
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} containing all of the {@link CertificateContact certificate contacts} in the
     * vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateContact> listContacts(Context context) {
        return new PagedIterable<>(() -> mapContactsToPagedResponse(
            implClient.getCertificateContactsWithResponse(new RequestOptions().setContext(context))));
    }

    /**
     * Deletes the certificate contacts in the key vault. This operation requires the certificates/managecontacts
     * permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the certificate contacts in the Azure Key Vault. Subscribes to the call and prints out the deleted
     * contacts details.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.deleteContacts -->
     * <pre>
     * for &#40;CertificateContact contact : certificateClient.deleteContacts&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Deleted contact with name %s and email %s from key vault%n&quot;, contact.getName&#40;&#41;,
     *         contact.getEmail&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.deleteContacts -->
     *
     * @return A {@link PagedIterable} containing the deleted {@link CertificateContact certificate contacts} in the
     * vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateContact> deleteContacts() {
        return deleteContacts(Context.NONE);
    }

    /**
     * Deletes the certificate contacts in the key vault. This operation requires the certificates/managecontacts
     * permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the certificate contacts in the Azure Key Vault. Prints out the deleted contacts details in the
     * response.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.deleteContacts#context -->
     * <pre>
     * for &#40;CertificateContact contact : certificateClient.deleteContacts&#40;new Context&#40;key1, value1&#41;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Deleted contact with name %s and email %s from key vault%n&quot;, contact.getName&#40;&#41;,
     *         contact.getEmail&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.deleteContacts#context -->
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} containing the deleted {@link CertificateContact certificate contacts} in the
     * vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateContact> deleteContacts(Context context) {
        return new PagedIterable<>(() -> mapContactsToPagedResponse(
            implClient.deleteCertificateContactsWithResponse(new RequestOptions().setContext(context))));
    }

    /**
     * Deletes the creation operation for the specified certificate that is in the process of being created. The
     * certificate is no longer created. This operation requires the certificates/update permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Triggers certificate creation and then deletes the certificate creation operation in the Azure Key Vault.
     * Subscribes to the call and prints out the deleted certificate operation details when a response has been
     * received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateOperation#string -->
     * <pre>
     * Response&lt;CertificateOperation&gt; deletedCertificateOperationWithResponse = certificateClient
     *     .deleteCertificateOperationWithResponse&#40;&quot;certificateName&quot;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Deleted Certificate Operation's last status %s%n&quot;,
     *     deletedCertificateOperationWithResponse.getValue&#40;&#41;.getStatus&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateOperation#string -->
     *
     * @param certificateName The name of the certificate.
     * @throws ResourceNotFoundException when a certificate operation for a certificate with {@code certificateName}
     * doesn't exist in the key vault.
     * @throws HttpRequestException when the {@code certificateName} is empty string.
     * @return The deleted {@link CertificateOperation certificate operation}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificateOperation deleteCertificateOperation(String certificateName) {
        return deleteCertificateOperationWithResponse(certificateName, Context.NONE).getValue();
    }

    /**
     * Deletes the creation operation for the specified certificate that is in the process of being created. The
     * certificate is no longer created. This operation requires the certificates/update permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Triggers certificate creation and then deletes the certificate creation operation in the Azure Key Vault.
     * Subscribes to the call and prints out the deleted certificate operation details when a response has been
     * received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateOperationWithResponse#string -->
     * <pre>
     * CertificateOperation deletedCertificateOperation = certificateClient
     *     .deleteCertificateOperation&#40;&quot;certificateName&quot;&#41;;
     * System.out.printf&#40;&quot;Deleted Certificate Operation's last status %s%n&quot;, deletedCertificateOperation.getStatus&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateOperationWithResponse#string -->
     *
     * @param certificateName The name of the certificate.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate operation for a certificate with {@code certificateName}
     * doesn't exist in the key vault.
     * @throws HttpRequestException when the {@code certificateName} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link CertificateOperation deleted certificate operation}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificateOperation> deleteCertificateOperationWithResponse(String certificateName,
        Context context) {

        return callWithMappedResponseAndException(
            () -> implClient.deleteCertificateOperationWithResponse(certificateName,
                new RequestOptions().setContext(context)),
            binaryData -> createCertificateOperation(binaryData
                .toObject(com.azure.security.keyvault.certificates.implementation.models.CertificateOperation.class)),
            CertificateAsyncClient::mapDeleteCertificateOperationException);
    }

    /**
     * Cancels a certificate creation operation that is already in progress. This operation requires the
     * certificates/update permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Triggers certificate creation and then cancels the certificate creation operation in the Azure Key Vault.
     * Subscribes to the call and prints out the updated certificate operation details when a response has been
     * received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.cancelCertificateOperation#string -->
     * <pre>
     * CertificateOperation certificateOperation = certificateClient
     *     .cancelCertificateOperation&#40;&quot;certificateName&quot;&#41;;
     * System.out.printf&#40;&quot;Certificate Operation status %s%n&quot;, certificateOperation.getStatus&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.cancelCertificateOperation#string -->
     *
     * @param certificateName The name of the certificate which is in the process of being created.
     * @throws ResourceNotFoundException when a certificate operation for a certificate with {@code name} doesn't exist
     * in the key vault.
     * @throws HttpRequestException when the {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link CertificateOperation cancelled certificate operation}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificateOperation cancelCertificateOperation(String certificateName) {
        return cancelCertificateOperationWithResponse(certificateName, Context.NONE).getValue();
    }

    /**
     * Cancels a certificate creation operation that is already in progress. This operation requires the
     * certificates/update permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Triggers certificate creation and then cancels the certificate creation operation in the Azure Key Vault.
     * Subscribes to the call and prints out the updated certificate operation details when a response has been
     * received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.cancelCertificateOperationWithResponse#string -->
     * <pre>
     * Response&lt;CertificateOperation&gt; certificateOperationWithResponse = certificateClient
     *     .cancelCertificateOperationWithResponse&#40;&quot;certificateName&quot;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Certificate Operation status %s%n&quot;, certificateOperationWithResponse.getValue&#40;&#41;.getStatus&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.cancelCertificateOperationWithResponse#string -->
     *
     * @param certificateName The name of the certificate which is in the process of being created.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate operation for a certificate with {@code name} doesn't exist
     * in the key vault.
     * @throws HttpRequestException when the {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link CertificateOperation cancelled certificate operation}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificateOperation> cancelCertificateOperationWithResponse(String certificateName,
        Context context) {

        CertificateOperationUpdateParameter certificateOperationUpdateParameter
            = new CertificateOperationUpdateParameter(true);

        return callWithMappedResponseAndException(
            () -> implClient.updateCertificateOperationWithResponse(certificateName,
                BinaryData.fromObject(certificateOperationUpdateParameter), new RequestOptions().setContext(context)),
            binaryData -> createCertificateOperation(binaryData
                .toObject(com.azure.security.keyvault.certificates.implementation.models.CertificateOperation.class)),
            CertificateAsyncClient::mapUpdateCertificateOperationException);
    }

    /**
     * Merges a certificate or a certificate chain with a key pair currently available in the service. This operation
     * requires the certificates/create permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Merges a certificate with a kay pair available in the service.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.mergeCertificate#config -->
     * <pre>
     * List&lt;byte[]&gt; x509CertificatesToMerge = new ArrayList&lt;&gt;&#40;&#41;;
     * MergeCertificateOptions config =
     *     new MergeCertificateOptions&#40;&quot;certificateName&quot;, x509CertificatesToMerge&#41;
     *         .setEnabled&#40;false&#41;;
     * KeyVaultCertificate mergedCertificate = certificateClient.mergeCertificate&#40;config&#41;;
     * System.out.printf&#40;&quot;Received Certificate with name %s and key id %s%n&quot;,
     *     mergedCertificate.getProperties&#40;&#41;.getName&#40;&#41;, mergedCertificate.getKeyId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.mergeCertificate#config -->
     *
     * @param mergeCertificateOptions the merge certificate configuration holding the x509 certificates.
     * @throws NullPointerException when {@code mergeCertificateOptions} is null.
     * @throws HttpRequestException if {@code mergeCertificateOptions} is invalid/corrupt.
     * @return The merged certificate.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultCertificateWithPolicy mergeCertificate(MergeCertificateOptions mergeCertificateOptions) {
        return mergeCertificateWithResponse(mergeCertificateOptions, Context.NONE).getValue();
    }

    /**
     * Merges a certificate or a certificate chain with a key pair currently available in the service. This operation
     * requires the certificates/create permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Merges a certificate with a kay pair available in the service.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.mergeCertificateWithResponse#config -->
     * <pre>
     * List&lt;byte[]&gt; x509CertsToMerge = new ArrayList&lt;&gt;&#40;&#41;;
     * MergeCertificateOptions mergeConfig =
     *     new MergeCertificateOptions&#40;&quot;certificateName&quot;, x509CertsToMerge&#41;
     *         .setEnabled&#40;false&#41;;
     * Response&lt;KeyVaultCertificateWithPolicy&gt; mergedCertificateWithResponse =
     *     certificateClient.mergeCertificateWithResponse&#40;mergeConfig, new Context&#40;key2, value2&#41;&#41;;
     * System.out.printf&#40;&quot;Received Certificate with name %s and key id %s%n&quot;,
     *     mergedCertificateWithResponse.getValue&#40;&#41;.getProperties&#40;&#41;.getName&#40;&#41;,
     *     mergedCertificateWithResponse.getValue&#40;&#41;.getKeyId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.mergeCertificateWithResponse#config -->
     *
     * @param mergeCertificateOptions the merge certificate configuration holding the x509 certificates.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws NullPointerException when {@code mergeCertificateOptions} is null.
     * @throws HttpRequestException if {@code mergeCertificateOptions} is invalid/corrupt.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the merged certificate.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultCertificateWithPolicy>
        mergeCertificateWithResponse(MergeCertificateOptions mergeCertificateOptions, Context context) {

        if (mergeCertificateOptions == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'mergeCertificateOptions' cannot be null."));
        }

        CertificateMergeParameters certificateMergeParameters
            = new CertificateMergeParameters(mergeCertificateOptions.getX509Certificates())
                .setTags(mergeCertificateOptions.getTags())
                .setCertificateAttributes(new CertificateAttributes().setEnabled(mergeCertificateOptions.isEnabled()));

        Response<BinaryData> response = implClient.mergeCertificateWithResponse(mergeCertificateOptions.getName(),
            BinaryData.fromObject(certificateMergeParameters), new RequestOptions().setContext(context));

        return new SimpleResponse<>(response, createCertificateWithPolicy(response.getValue()
            .toObject(com.azure.security.keyvault.certificates.implementation.models.CertificateBundle.class)));
    }

    /**
     * Imports a pre-existing certificate to the key vault. The specified certificate must be in PFX or PEM format,
     * and must contain the private key as well as the x509 certificates. This operation requires the
     * certificates/import permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Imports a certificate into the key vault.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.importCertificate#options -->
     * <pre>
     * byte[] certificateToImport = new byte[100];
     * ImportCertificateOptions config =
     *     new ImportCertificateOptions&#40;&quot;certificateName&quot;, certificateToImport&#41;.setEnabled&#40;false&#41;;
     * KeyVaultCertificate importedCertificate = certificateClient.importCertificate&#40;config&#41;;
     * System.out.printf&#40;&quot;Received Certificate with name %s and key id %s%n&quot;,
     *     importedCertificate.getProperties&#40;&#41;.getName&#40;&#41;, importedCertificate.getKeyId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.importCertificate#options -->
     *
     * @param importCertificateOptions The details of the certificate to import to the key vault
     * @throws HttpRequestException when the {@code importCertificateOptions} are invalid.
     * @throws NullPointerException when {@code importCertificateOptions} is null.
     * @return the {@link KeyVaultCertificateWithPolicy imported certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultCertificateWithPolicy importCertificate(ImportCertificateOptions importCertificateOptions) {
        return importCertificateWithResponse(importCertificateOptions, Context.NONE).getValue();
    }

    /**
     * Imports a pre-existing certificate to the key vault. The specified certificate must be in PFX or PEM format,
     * and must contain the private key as well as the x509 certificates. This operation requires the
     * certificates/import permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Imports a certificate into the key vault.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.importCertificateWithResponse#options -->
     * <pre>
     * byte[] certToImport = new byte[100];
     * ImportCertificateOptions importCertificateOptions =
     *     new ImportCertificateOptions&#40;&quot;certificateName&quot;, certToImport&#41;.setEnabled&#40;false&#41;;
     * Response&lt;KeyVaultCertificateWithPolicy&gt; importedCertificateWithResponse =
     *     certificateClient.importCertificateWithResponse&#40;importCertificateOptions, new Context&#40;key2, value2&#41;&#41;;
     * System.out.printf&#40;&quot;Received Certificate with name %s and key id %s%n&quot;,
     *     importedCertificateWithResponse.getValue&#40;&#41;.getProperties&#40;&#41;.getName&#40;&#41;,
     *     importedCertificateWithResponse.getValue&#40;&#41;.getKeyId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.certificates.CertificateClient.importCertificateWithResponse#options -->
     *
     * @param importCertificateOptions The details of the certificate to import to the key vault
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws HttpRequestException when the {@code importCertificateOptions} are invalid.
     * @throws NullPointerException when {@code importCertificateOptions} is null.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultCertificateWithPolicy imported certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultCertificateWithPolicy>
        importCertificateWithResponse(ImportCertificateOptions importCertificateOptions, Context context) {
        if (importCertificateOptions == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'importCertificateOptions' cannot be null."));
        }

        com.azure.security.keyvault.certificates.implementation.models.CertificatePolicy implPolicy
            = getImplCertificatePolicy(importCertificateOptions.getPolicy());

        CertificateImportParameters certificateImportParameters
            = new CertificateImportParameters(transformCertificateForImport(importCertificateOptions))
                .setPassword(importCertificateOptions.getPassword())
                .setCertificatePolicy(implPolicy)
                .setTags(importCertificateOptions.getTags())
                .setCertificateAttributes(new CertificateAttributes().setEnabled(importCertificateOptions.isEnabled()))
                .setPreserveCertOrder(importCertificateOptions.isCertificateOrderPreserved());

        Response<BinaryData> response = implClient.importCertificateWithResponse(importCertificateOptions.getName(),
            BinaryData.fromObject(certificateImportParameters), new RequestOptions().setContext(context));

        return new SimpleResponse<>(response, createCertificateWithPolicy(response.getValue()
            .toObject(com.azure.security.keyvault.certificates.implementation.models.CertificateBundle.class)));
    }

    private static <T> T callWithMappedException(Supplier<T> apiCall,
        Function<HttpResponseException, HttpResponseException> exceptionMapper) {

        try {
            return apiCall.get();
        } catch (HttpResponseException e) {
            throw exceptionMapper.apply(e);
        }
    }

    private static <T, R> Response<R> callWithMappedResponseAndException(Supplier<Response<T>> apiCall,
        Function<T, R> responseValueMapper, Function<HttpResponseException, HttpResponseException> exceptionMapper) {

        try {
            Response<T> response = apiCall.get();

            return new SimpleResponse<>(response, responseValueMapper.apply(response.getValue()));
        } catch (HttpResponseException e) {
            throw exceptionMapper.apply(e);
        }
    }
}

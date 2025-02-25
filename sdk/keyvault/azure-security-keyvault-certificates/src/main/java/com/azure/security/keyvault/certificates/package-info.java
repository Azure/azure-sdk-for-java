// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p><a href="https://learn.microsoft.com/azure/certificate-vault/general/">Azure Key Vault</a> is a cloud-based service
 * provided by Microsoft Azure that allows users to securely store and manage cryptographic certificates used for encrypting
 * and decrypting data. It is a part of Azure Key Vault, which is a cloud-based service for managing cryptographic certificates,
 * keys, and secrets.</p>
 *
 * <p>Azure Key Vault Certificates provides a centralized and highly secure location for storing certificates, which
 * eliminates the need to store sensitive certificate material in application code or configuration files.
 * By leveraging Azure Key Vault, you can better protect your certificates and ensure their availability
 * when needed.</p>
 *
 * <p>Key features of the Azure Key Vault Certificates service include:</p>
 *
 * <ul>
 *  <li>Secure storage: Certificates are stored securely within Azure Key Vault, which provides robust encryption
 *  and access control mechanisms to protect against unauthorized access.</li>
 *  <li>Certificate lifecycle management: You can create, import, and manage certificates within Azure Key Vault.
 *  It supports common certificate formats such as X.509 and PFX.</li>
 *  <li>Certificate management operations: Azure Key Vault provides a comprehensive set of management operations,
 *  including certificate creation, deletion, retrieval, renewal, and revocation.</li>
 *  <li>Integration with Azure services: Key Vault Certificates can be easily integrated with other Azure services,
 *  such as Azure App Service, Azure Functions, and Azure Virtual Machines, to enable secure authentication
 *  and encryption.</li>
 * </ul>
 *
 * <p>The Azure Key Vault Certificates client library allows developers to securely store and manage certificates
 * within Azure Key Vault. The library provides a set of APIs that enable developers to securely create, import,
 * retrieve, update, and perform other certificate-related operations.</p>
 *
 * <p><strong>Key Concepts:</strong></p>
 *
 * <p>What is a Certificate Client?</p>
 *
 * <p>The certificate client performs the interactions with the Azure Key Vault service for getting, setting, updating,
 * deleting, and listing certificates and its versions. Asynchronous (CertificateAsyncClient) and synchronous (CertificateClient) clients
 * exist in the SDK allowing for the selection of a client based on an application's use case. Once you have
 * initialized a certificate, you can interact with the primary resource types in Azure Key Vault.</p>
 *
 * <p>What is an Azure Key Vault Certificate ?</p>
 *
 * <p>Azure Key Vault supports certificates with secret content types (PKCS12 and PEM). The certificate can be
 * backed by keys in Azure Key Vault of types (EC and RSA). In addition to the certificate policy, the following
 * attributes may be specified:.</p>
 *
 * <ul>
 * <li>enabled: Specifies whether the certificate is enabled and usable.</li>
 * <li>created: Indicates when this version of the certificate was created.</li>
 * <li>updated: Indicates when this version of the certificate was updated.</li>
 * </ul>
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Azure Key Vault service, you will need to create an instance of the
 * {@link com.azure.security.keyvault.certificates.CertificateClient} or {@link com.azure.security.keyvault.certificates.CertificateAsyncClient} class, a vault url and a credential object.</p>
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
 * <p>The following code sample demonstrates the creation of a {@link com.azure.security.keyvault.certificates.CertificateClient},
 * using the {@link com.azure.security.keyvault.certificates.CertificateClientBuilder} to configure it.</p>
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
 * The {@link com.azure.security.keyvault.certificates.CertificateClient} or
 * {@link com.azure.security.keyvault.certificates.CertificateAsyncClient} can be used to create a certificate in
 * the key vault.
 *
 * <p><strong>Synchronous Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously create a certificate in the key vault,
 * using the {@link com.azure.security.keyvault.certificates.CertificateClient#beginCreateCertificate(java.lang.String, com.azure.security.keyvault.certificates.models.CertificatePolicy)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy -->
 * <pre>
 * CertificatePolicy certPolicy = new CertificatePolicy&#40;&quot;Self&quot;,
 *     &quot;CN=SelfSignedJavaPkcs12&quot;&#41;;
 * SyncPoller&lt;CertificateOperation, KeyVaultCertificateWithPolicy&gt; certPoller = certificateClient
 *     .beginCreateCertificate&#40;&quot;certificateName&quot;, certPolicy&#41;;
 * certPoller.waitUntil&#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41;;
 * KeyVaultCertificate cert = certPoller.getFinalResult&#40;&#41;;
 * System.out.printf&#40;&quot;Certificate created with name %s%n&quot;, cert.getName&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to
 * {@link com.azure.security.keyvault.certificates.CertificateAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Get a Certificate</h2>
 * The {@link com.azure.security.keyvault.certificates.CertificateClient} or
 * {@link com.azure.security.keyvault.certificates.CertificateAsyncClient} can be used to retrieve a certificate from the
 * key vault.
 *
 * <p><strong>Synchronous Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously retrieve a certificate from the key vault, using
 * the {@link com.azure.security.keyvault.certificates.CertificateClient#getCertificate(java.lang.String)}.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicy#string -->
 * <pre>
 * CertificatePolicy policy = certificateClient.getCertificatePolicy&#40;&quot;certificateName&quot;&#41;;
 * System.out.printf&#40;&quot;Received policy with subject name %s%n&quot;, policy.getSubject&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicy#string -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to
 * {@link com.azure.security.keyvault.certificates.CertificateAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Delete a Certificate</h2>
 * The {@link com.azure.security.keyvault.certificates.CertificateClient} or
 * {@link com.azure.security.keyvault.certificates.CertificateAsyncClient} can be used to delete a certificate from
 * the key vault.
 *
 * <p><strong>Synchronous Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously delete a certificate from the
 * key vault, using the {@link com.azure.security.keyvault.certificates.CertificateClient#beginDeleteCertificate(java.lang.String)} API.</p>
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
 * <p><strong>Note:</strong> For the asynchronous sample, refer to
 * {@link com.azure.security.keyvault.certificates.CertificateAsyncClient}.</p>
 *
 * @see com.azure.security.keyvault.certificates.CertificateClient
 * @see com.azure.security.keyvault.certificates.CertificateAsyncClient
 * @see com.azure.security.keyvault.certificates.CertificateClientBuilder
 */
package com.azure.security.keyvault.certificates;

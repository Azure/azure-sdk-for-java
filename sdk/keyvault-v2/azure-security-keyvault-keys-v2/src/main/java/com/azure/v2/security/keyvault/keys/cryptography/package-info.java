// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p><a href="https://learn.microsoft.com/azure/key-vault/general/">Azure Key Vault</a> is a cloud-based service
 * provided by Microsoft Azure that allows users to securely store and manage cryptographic keys used for encrypting
 * and decrypting data. It is a part of Azure Key Vault, which is a cloud-based service for managing cryptographic keys,
 * secrets, and certificates.</p>
 *
 * <p>The service supports various cryptographic algorithms and operations, including symmetric and asymmetric
 * encryption, digital signatures, hashing, and random number generation. You can use the service to perform
 * operations like encrypting sensitive data before storing it, decrypting data when needed, signing data to ensure
 * its integrity, and verifying signatures to validate the authenticity of the data.</p>
 *
 * <p>By utilizing the Azure Key Vault cryptography services, you benefit from the strong security features provided
 * by Azure Key Vault, such as hardware security modules (HSMs) for key storage and cryptographic operations,
 * access control policies, and audit logging. It helps you protect your sensitive data and comply with industry
 * standards and regulatory requirements.</p>
 *
 * <p>The Azure Key Vault Keys client library allows developers to interact with the Azure Key Vault service from their
 * applications. The library provides a set of APIs that enable developers to securely encrypt, decrypt, sign, and
 * verify data using cryptographic keys securely stored in Key Vault.</p>
 *
 * <p><strong>Key Concepts:</strong></p>
 *
 * <p>What is a Cryptography Client?</p>
 * <p>The cryptography client performs the cryptographic operations locally or calls the Azure Key Vault service
 * depending on how much key information is available locally. It supports encrypting, decrypting, signing, verifying,
 * key wrapping, key unwrapping, and retrieving the configured key.</p>
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Azure Key Vault service, you will need to create an instance of the
 * {@link com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient} class, a vault url and a credential
 * object.</p>
 *
 * <p>The examples shown in this document use a credential object named DefaultAzureCredential for authentication, which
 * is appropriate for most scenarios, including local development and production environments. Additionally, we
 * recommend using a <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">
 * managed identity</a> for authentication in production environments. You can find more information on different ways
 * of authenticating and their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">Azure Identity documentation"</a>.
 * </p>
 *
 * <p><strong>Sample: Construct Cryptography Client</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a
 * {@link com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient}, using the
 * {@link com.azure.v2.security.keyvault.keys.cryptography.CryptographyClientBuilder} to configure it.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.instantiation -->
 * <pre>
 * CryptographyClient cryptographyClient = new CryptographyClientBuilder&#40;&#41;
 *     .keyIdentifier&#40;&quot;&lt;your-key-id-from-keyvault&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.instantiation -->
 *
 * <br>
 * <hr>
 *
 * <h2>Encrypt Data</h2>
 * The {@link com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient} can be used to encrypt data.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to encrypt data using the
 * {@link com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient#encrypt(com.azure.v2.security.keyvault.keys.cryptography.models.EncryptionAlgorithm, byte[])} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptionAlgorithm-byte -->
 * <pre>
 * byte[] plaintext = new byte[100];
 * new Random&#40;0x1234567L&#41;.nextBytes&#40;plaintext&#41;;
 *
 * EncryptResult encryptResult = cryptographyClient.encrypt&#40;EncryptionAlgorithm.RSA_OAEP, plaintext&#41;;
 *
 * System.out.printf&#40;&quot;Received encrypted content of length: %d, with algorithm: %s.%n&quot;,
 *     encryptResult.getCipherText&#40;&#41;.length, encryptResult.getAlgorithm&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptionAlgorithm-byte -->
 *
 * <br>
 * <hr>
 *
 * <h2>Decrypt Data</h2>
 * The {@link com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient} can be used to decrypt data.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to decrypt data using the
 * {@link com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient#decrypt(com.azure.v2.security.keyvault.keys.cryptography.models.EncryptionAlgorithm, byte[])}
 * API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.decrypt#EncryptionAlgorithm-byte -->
 * <pre>
 * byte[] ciphertext = new byte[100];
 * new Random&#40;0x1234567L&#41;.nextBytes&#40;ciphertext&#41;;
 *
 * DecryptResult decryptResult = cryptographyClient.decrypt&#40;EncryptionAlgorithm.RSA_OAEP, ciphertext&#41;;
 *
 * System.out.printf&#40;&quot;Received decrypted content of length: %d.%n&quot;, decryptResult.getPlainText&#40;&#41;.length&#41;;
 * </pre>
 * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.decrypt#EncryptionAlgorithm-byte -->
 *
 * @see com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient
 * @see com.azure.v2.security.keyvault.keys.cryptography.CryptographyClientBuilder
 */
package com.azure.v2.security.keyvault.keys.cryptography;

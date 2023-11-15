// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p><a href="https://learn.microsoft.com/azure/key-vault/general/">Azure Key Vault</a> is a cloud-based service
 * provided by Microsoft Azure that allows users to securely store and manage cryptographic keys used for encrypting
 * and decrypting data. It is a part of Azure Key Vault, which is a cloud-based service for managing cryptographic keys,
 * secrets, and certificates.</p>
 *
 * <p>Azure Key Vault Keys provides a centralized and highly secure key management solution, allowing you to protect
 * your keys and control access to them. It eliminates the need for storing keys in code or configuration files,
 * reducing the risk of exposure and unauthorized access.</p>
 *
 * <p>With Azure Key Vault Keys, you can perform various operations on cryptographic keys, such as creating keys,
 * importing existing keys, generating key pairs, encrypting data using keys, and decrypting data using keys.
 * The service supports various key types and algorithms, including symmetric keys, asymmetric keys, and
 * Elliptic Curve Cryptography (ECC) keys.</p>
 *
 * <p>The Azure Key Vault Keys client library allows developers to interact with the Azure Key Vault service
 * from their applications. The library provides a set of APIs that enable developers to securely create keys,
 * import existing keys, delete keys, retrieving key metadata, encrypting and decrypting data using keys,
 * and signing and verifying signatures using keys.</p>
 *
 * <p><strong>Key Concepts:</strong></p>
 *
 * <p>What is a Key Client?</p>
 * <p>The key client performs the interactions with the Azure Key Vault service for getting, setting, updating,
 * deleting, and listing keys and its versions. Asynchronous (`KeyAsyncClient`) and synchronous (`KeyClient`) clients
 * exist in the SDK allowing for the selection of a client based on an application's use case. Once you have
 * initialized a key, you can interact with the primary resource types in Key Vault.</p>
 *
 * <p>What is an Azure Key Vault Key ?</p>
 * <p>Azure Key Vault supports multiple key types (RSA and EC) and algorithms, and enables the use of
 * Hardware Security Modules (HSM) for high value keys. In addition to the key material, the following attributes may
 * be specified:</p>
 *
 * <ul>
 *     <li>enabled: Specifies whether the key is enabled and usable for cryptographic operations.</li>
 *     <li>notBefore: Identifies the time before which the key must not be used for cryptographic operations.</li>
 *     <li>expires: Identifies the expiration time on or after which the key MUST NOT be used for cryptographic operations.</li>
 *     <li>created: Indicates when this version of the key was created.</li>
 *     <li>updated: Indicates when this version of the key was updated.</li>
 * </ul>
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Azure Key Vault service, you will need to create an instance of the
 * {@link com.azure.security.keyvault.keys.KeyClient} class, a vault url and a credential object.</p>
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
 * <p><strong>Sample: Construct Synchronous Key Client</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.security.keyvault.keys.KeyClient},
 * using the {@link com.azure.security.keyvault.keys.KeyClientBuilder} to configure it.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.keys.KeyClient.instantiation -->
 * <pre>
 * KeyClient keyClient = new KeyClientBuilder&#40;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.keys.KeyClient.instantiation -->
 *
 * <p><strong>Sample: Construct Asynchronous Key Client</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a
 * {@link com.azure.security.keyvault.keys.KeyClient}, using the
 * {@link com.azure.security.keyvault.keys.KeyClientBuilder} to configure it.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.keys.KeyAsyncClient.instantiation -->
 * <pre>
 * KeyAsyncClient keyAsyncClient = new KeyClientBuilder&#40;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.keys.KeyAsyncClient.instantiation -->
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Create a Cryptographic Key</h2>
 * The {@link com.azure.security.keyvault.keys.KeyClient} or
 * {@link com.azure.security.keyvault.keys.KeyAsyncClient} can be used to create a key in the key vault.
 *
 * <p><strong>Synchronous Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously create a cryptographic key in the key vault,
 * using the {@link com.azure.security.keyvault.keys.KeyClient#createKey(java.lang.String, com.azure.security.keyvault.keys.models.KeyType)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.keys.KeyClient.createKey#String-KeyType -->
 * <pre>
 * KeyVaultKey key = keyClient.createKey&#40;&quot;keyName&quot;, KeyType.EC&#41;;
 * System.out.printf&#40;&quot;Created key with name: %s and id: %s%n&quot;, key.getName&#40;&#41;, key.getId&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.keys.KeyClient.createKey#String-KeyType -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to
 * {@link com.azure.security.keyvault.keys.KeyAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Get a Cryptographic Key</h2>
 * The {@link com.azure.security.keyvault.keys.KeyClient} or
 * {@link com.azure.security.keyvault.keys.KeyAsyncClient} can be used to retrieve a key from the
 * key vault.
 *
 * <p><strong>Synchronous Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously retrieve a key from the key vault, using
 * the {@link com.azure.security.keyvault.keys.KeyClient#getKey(java.lang.String)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.keys.KeyClient.getKey#String -->
 * <pre>
 * KeyVaultKey keyWithVersionValue = keyClient.getKey&#40;&quot;keyName&quot;&#41;;
 *
 * System.out.printf&#40;&quot;Retrieved key with name: %s and: id %s%n&quot;, keyWithVersionValue.getName&#40;&#41;,
 *     keyWithVersionValue.getId&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.keys.KeyClient.getKey#String -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to
 * {@link com.azure.security.keyvault.keys.KeyAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Delete a Cryptographic Key</h2>
 * The {@link com.azure.security.keyvault.keys.KeyClient} or
 * {@link com.azure.security.keyvault.keys.KeyAsyncClient} can be used to delete a key from the key vault.
 *
 * <p><strong>Synchronous Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously delete a key from the
 * key vault, using the {@link com.azure.security.keyvault.keys.KeyClient#beginDeleteKey(java.lang.String)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.keys.KeyClient.deleteKey#String -->
 * <pre>
 * SyncPoller&lt;DeletedKey, Void&gt; deleteKeyPoller = keyClient.beginDeleteKey&#40;&quot;keyName&quot;&#41;;
 * PollResponse&lt;DeletedKey&gt; deleteKeyPollResponse = deleteKeyPoller.poll&#40;&#41;;
 *
 * &#47;&#47; Deleted date only works for SoftDelete Enabled Key Vault.
 * DeletedKey deletedKey = deleteKeyPollResponse.getValue&#40;&#41;;
 *
 * System.out.printf&#40;&quot;Key delete date: %s%n&quot;, deletedKey.getDeletedOn&#40;&#41;&#41;;
 * System.out.printf&#40;&quot;Deleted key's recovery id: %s%n&quot;, deletedKey.getRecoveryId&#40;&#41;&#41;;
 *
 * &#47;&#47; Key is being deleted on the server.
 * deleteKeyPoller.waitForCompletion&#40;&#41;;
 * &#47;&#47; Key is deleted
 * </pre>
 * <!-- end com.azure.security.keyvault.keys.KeyClient.deleteKey#String -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to
 * {@link com.azure.security.keyvault.keys.KeyAsyncClient}.</p>
 *
 * @see com.azure.security.keyvault.keys.KeyClient
 * @see com.azure.security.keyvault.keys.KeyAsyncClient
 * @see com.azure.security.keyvault.keys.KeyClientBuilder
 */
package com.azure.security.keyvault.keys;

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p><a href="https://learn.microsoft.com/azure/key-vault/general/">Azure KeyVault</a> is a cloud-based service
 * provided by Microsoft Azure that allows users to store, manage, and access secrets, such as passwords, certificates,
 * and other sensitive information, securely in the cloud. The service provides a centralized and secure location for
 * storing secrets, which can be accessed by authorized applications and users with appropriate permissions.
 * Azure KeyVault Secrets offers several key features, including:</p>
 * <ol>
 *     <li>Secret management: It allows users to store, manage, and access secrets securely, and provides features such
 *     as versioning, backup, and restoration.</li>
 *     <li>Access control: It offers role-based access control (RBAC) and enables users to grant specific permissions
 *     to access secrets to other users, applications, or services.</li>
 *     <li>Integration with other Azure services: Azure KeyVault Secrets can be integrated with other Azure services,
 *     such as Azure App Service, Azure Functions, and Azure Virtual Machines, to simplify the process of securing
 *     sensitive information.</li>
 *     <li>High availability and scalability: The service is designed to provide high availability and scalability,
 *     with the ability to handle large volumes of secrets and requests.</li>
 * </ol>
 *
 * <p>The Azure KeyVault Secrets client library allows developers to interact with the Azure KeyVault Secrets service
 * from their applications. The library provides a set of APIs that enable developers to securely store, manage, and
 * retrieve secrets in the Azure KeyVault The library provides a simple and secure way to manage secrets in the
 * Azure KeyVault, and supports operations such as creating, updating, deleting, and retrieving secrets.</p>
 *
 * <p><strong>Key Concepts:</strong></p>
 *
 * <p>What is a Secret Client?</p>
 * <p>The secret client performs the interactions with the Azure Key Vault service for getting, setting, updating,
 * deleting, and listing secrets and its versions. Asynchronous (SecretAsyncClient) and synchronous (SecretClient)
 * clients exist in the SDK allowing for selection of a client based on an application's use case.
 * Once you've initialized a secret, you can interact with the primary resource types in Key Vault.</p>
 *
 * <p>What is an Azure KeyVault Secret ?</p>
 * <p>A secret is the fundamental resource within Azure Key Vault. From a developer's perspective, Key Vault APIs
 * accept and return secret values as strings. In addition to the secret data, the following attributes may be
 * specified:</p>
 *
 * <ol>
 *     <li>enabled: Specifies whether the secret data can be retrieved.</li>
 *     <li>notBefore: Identifies the time after which the secret will be active.</li>
 *     <li>expires: Identifies the expiration time on or after which the secret data should not be retrieved.</li>
 *     <li>created: Indicates when this version of the secret was created.</li>
 *     <li>updated: Indicates when this version of the secret was updated.</li>
 * </ol>
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Azure Key Vault service, you will need to create an instance of the
 * {@link com.azure.security.keyvault.secrets.SecretClient} class, a vault url and a credential object.</p>
 *
 * <p>The examples shown in this document use a credential object named DefaultAzureCredential, which is appropriate
 * for most scenarios, including local development and production environments. Additionally, we recommend using a
 * managed identity for authentication in production environments.
 * You can find more information on different ways of authenticating and their corresponding credential types in the
 * Azure Identity documentation.</p>
 *
 * <p><strong>Sample: Construct a Secret Client</strong></p>
 * <p>The following code sample demonstrates the creation of a {@link com.azure.security.keyvault.secrets.SecretClient},
 * using the {@link com.azure.security.keyvault.secrets.SecretClientBuilder} to configure it.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.SecretClient.instantiation -->
 * <pre>
 * SecretClient secretClient = new SecretClientBuilder&#40;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.SecretClient.instantiation -->
 *
 * <p>The following code sample demonstrates the creation of a
 * {@link com.azure.security.keyvault.secrets.SecretAsyncClient}, using the
 * {@link com.azure.security.keyvault.secrets.SecretClientBuilder} to configure it.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.secrets.SecretAsyncClient.instantiation -->
 * <pre>
 * SecretAsyncClient secretAsyncClient = new SecretClientBuilder&#40;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.secrets.SecretAsyncClient.instantiation -->
 *
 * <hr/>
 *
 * <h2>Create a Secret</h2>
 * The {@link com.azure.security.keyvault.secrets.SecretClient} or
 * {@link com.azure.security.keyvault.secrets.SecretAsyncClient} can be used to create a secret in the Azure KeyVault.
 *
 * <p><strong>Synchronous Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously create and store a secret in the key vault,
 * using the {@link com.azure.security.keyvault.secrets.SecretClient}.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.SecretClient.setSecret#string-string -->
 * <pre>
 * KeyVaultSecret secret = secretClient.setSecret&#40;&quot;secretName&quot;, &quot;secretValue&quot;&#41;;
 * System.out.printf&#40;&quot;Secret is created with name %s and value %s%n&quot;, secret.getName&#40;&#41;, secret.getValue&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.SecretClient.setSecret#string-string -->
 *
 * <p><strong>Asynchronous Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously create and store a secret in the key vault,
 * using the {@link com.azure.security.keyvault.secrets.SecretAsyncClient}.</p>
 *
 * <!-- src_embed com.azure.keyvault.secrets.SecretClient.setSecret#string-string -->
 * <pre>
 * secretAsyncClient.setSecret&#40;&quot;secretName&quot;, &quot;secretValue&quot;&#41;
 *     .subscribe&#40;secretResponse -&gt;
 *         System.out.printf&#40;&quot;Secret is created with name %s and value %s%n&quot;,
 *             secretResponse.getName&#40;&#41;, secretResponse.getValue&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.keyvault.secrets.SecretClient.setSecret#string-string -->
 *
 * <hr/>
 *
 * <h2>Get a Secret</h2>
 * The {@link com.azure.security.keyvault.secrets.SecretClient} or
 * {@link com.azure.security.keyvault.secrets.SecretAsyncClient} can be used to retrieve a secret from the
 * Azure KeyVault.
 *
 * <p><strong>Synchronous Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously retrieve a previously stored secret from the Azure
 * KeyVault, using the {@link com.azure.security.keyvault.secrets.SecretClient}.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.SecretClient.getSecret#string -->
 * <pre>
 * KeyVaultSecret secret = secretClient.getSecret&#40;&quot;secretName&quot;&#41;;
 * System.out.printf&#40;&quot;Secret is returned with name %s and value %s%n&quot;,
 *     secret.getName&#40;&#41;, secret.getValue&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.SecretClient.getSecret#string -->
 *
 * <p><strong>Asynchronous Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously create and store a secret in the key vault,
 * using the {@link com.azure.security.keyvault.secrets.SecretAsyncClient}.</p>
 *
 * <!-- src_embed com.azure.keyvault.secrets.SecretClient.getSecret#string -->
 * <pre>
 * secretAsyncClient.getSecret&#40;&quot;secretName&quot;&#41;
 *     .subscribe&#40;secretWithVersion -&gt;
 *         System.out.printf&#40;&quot;Secret is returned with name %s and value %s %n&quot;,
 *             secretWithVersion.getName&#40;&#41;, secretWithVersion.getValue&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.keyvault.secrets.SecretClient.getSecret#string -->
 *
 * <hr/>
 *
 * @see com.azure.security.keyvault.secrets.SecretClient
 * @see com.azure.security.keyvault.secrets.SecretAsyncClient
 * @see com.azure.security.keyvault.secrets.SecretClientBuilder
 * @see com.azure.security.keyvault.secrets.models.KeyVaultSecret
 */
package com.azure.security.keyvault.secrets;

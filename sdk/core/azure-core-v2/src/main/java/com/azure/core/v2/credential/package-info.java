// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p>
 * Azure Core Credential library is designed to simplify the process of authenticating and authorizing access
 * to Azure services from Java applications. The SDK provides a set of classes and methods that handle authentication
 * and credential management, allowing developers to securely connect to Azure services without dealing with the
 * low-level details of authentication protocols.
 * </p>
 *
 * <p>
 * The library provides a unified way to obtain credentials for various Azure authentication
 * mechanisms, such as Azure Active Directory (AAD), shared access signatures, and API keys. It abstracts the
 * complexities of authentication and provides a consistent programming model for accessing Azure services.
 * </p>
 *
 * <p>
 * By using the library, users can easily integrate Azure authentication into their applications, retrieve the
 * required credentials based on the desired authentication method, and use those credentials to authenticate
 * requests to Azure services like Azure Storage, Azure Key Vault, Azure Service Bus, and more.
 * </p>
 *
 * <p>
 * The library offers several authentication types for authenticating with Azure services. Here are some of the
 * authentication mechanisms supported by the library:
 * </p>
 * <ul>
 * <li>Azure Active Directory (AAD) Authentication</li>
 * <li>Shared Access Signature (SAS) Authentication</li>
 * <li>Key Based Authentication</li>
 * </ul>
 *
 * <h2>Azure Active Directory (AAD) Authentication</h2>
 *
 * <p>
 * This type of authentication allows you to authenticate using Azure Active Directory and obtain a token to access
 * Azure resources. You can authenticate with AAD using client secrets, client certificates, or user credentials.
 * The library offers {@link com.azure.core.v2.credential.TokenCredential} interface which is accepted as an argument
 * on the client builders in Azure SDKs where AAD authentication is supported.
 * You can refer to and include our
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">Azure
 * Identity</a>
 * library in your application as it offers pluggable implementation of
 * {@link com.azure.core.v2.credential.TokenCredential} for various AAD based authentication mechanism including
 * service principal, managed identity, and more.
 * </p>
 *
 * <br>
 *
 * <hr>
 *
 * <h2>Shared Access Signature (SAS) Authentication</h2>
 *
 * <p>
 * Shared Access Signatures enable you to grant time-limited access to Azure resources. The library offers
 * {@link com.azure.core.v2.credential.AzureSasCredential} which allows you to authenticate using a shared access
 * signature, which is a string-based token that grants access to specific resources for a specific period.
 * </p>
 *
 * <p>
 * <strong>Sample: Azure SAS Authentication</strong>
 * </p>
 *
 * <p>
 * The following code sample demonstrates the creation of a {@link com.azure.core.v2.credential.AzureSasCredential},
 * using the sas token to configure it.
 * </p>
 *
 * <!-- src_embed com.azure.core.credential.azureSasCredential -->
 * <!-- end com.azure.core.credential.azureSasCredential -->
 *
 * <br>
 *
 * <hr>
 *
 * <h2>Key Based Authentication</h2>
 *
 * <p>
 * A key is a unique identifier or token that is associated with a specific user or application. It serves as a
 * simple form of authentication to ensure that only authorized clients can access the protected resources or APIs.
 * This authentication is commonly used for accessing certain services, such as Azure Cognitive Services, Azure Search,
 * or Azure Management APIs. Each service may have its own specific way of using API keys, but the general concept
 * remains the same. The library offers {@link com.azure.core.v2.credential.AzureKeyCredential} and
 * {@link com.azure.core.v2.credential.AzureNamedKeyCredential} which can allows you to authenticate using a key.
 * </p>
 *
 *
 * <p>
 * <strong>Sample: Azure Key Authentication</strong>
 * </p>
 *
 * <p>
 * The following code sample demonstrates the creation of a {@link com.azure.core.v2.credential.AzureKeyCredential},
 * using the Azure service key to configure it.
 * </p>
 *
 * <!-- src_embed com.azure.core.credential.azureKeyCredential -->
 * <!-- end com.azure.core.credential.azureKeyCredential -->
 *
 * @see com.azure.core.v2.credential.AzureKeyCredential
 * @see com.azure.core.v2.credential.AzureNamedKeyCredential
 * @see com.azure.core.v2.credential.AzureSasCredential
 * @see com.azure.core.v2.credential.TokenCredential
 */
package com.azure.core.v2.credential;

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * The Azure Identity library provides Azure Active Directory (Azure AD) token authentication support across the Azure SDK.
 * The library focuses on OAuth authentication with Azure AD, and it offers various credential classes capable of
 * acquiring an Azure AD token to authenticate service requests. All the credential classes in this package are
 * implementations of the `TokenCredential` interface offered by azure-core, and any of them can be used by to
 * construct service clients capable of authenticating with a `TokenCredential`.
 *
 * <p> Getting Started:</p>
 * The `DefaultAzureCredential` is appropriate for most scenarios where the application is intended to ultimately be run in Azure.
 * This is because the `DefaultAzureCredential` combines credentials commonly used to authenticate when deployed, with credentials
 * used to authenticate in a development environment. Note, this credential is intended to simplify getting started with the SDK
 * by handling common scenarios with reasonable default behaviors. Developers who want more control or whose scenario isn't
 * served by the default settings should use other credential types.
 * For more information refer to <a href="https://aka.ms/azsdk/java/identity/defaultazurecredentialsauthentication/docs"> Conceptual knowledge and configuration details </a>.
 *
 * <p><strong>Sample: Construct a simple DefaultAzureCredential</strong></p>
 * <!-- src_embed com.azure.identity.credential.defaultazurecredential.construct -->
 * <pre>
 * DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.defaultazurecredential.construct -->
 *
 *
 * <p> Authenticating on Azure Hosted Platforms via Managed Identity</p>
 * The Managed Identity authenticates the managed identity (system or user assigned) of an Azure resource. So, if the
 * application is running inside an Azure resource that supports Managed Identity through IDENTITY/MSI, IMDS endpoints,
 * or both, then the ManagedIdentityCredential will get your application authenticated, and offers a great secretless authentication experience.
 * For more information refer to <a href="https://aka.ms/azsdk/java/identity/managedidentityauthentication/docs"> Conceptual knowledge and configuration details </a>.
 *
 * <p><strong>Sample: Construct a ClientSecretCredential</strong></p>
 * <!-- src_embed com.azure.identity.credential.managedidentitycredential.construct -->
 * <pre>
 * ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder&#40;&#41;
 *     .clientId&#40;clientId&#41; &#47;&#47; specify client id only if targeting a user-assigned managed identity.
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.managedidentitycredential.construct -->
 *
 *
 * <p> Authenticate with Service Principals</p>
 * The Azure Active Directory (Azure AD) allows users to register service principals which can be used as an identity for authentication.
 * The authenticating is supported via a client secret or client certificate. The {@link com.azure.identity.ClientCertificateCredential} or {@link com.azure.identity.ClientSecretCredential}
 * here will work well to get your application authenticated.
 * For more information refer to <a href="https://aka.ms/azsdk/java/identity/serviceprincipalauthentication/docs"> Conceptual knowledge and configuration details </a>.
 *
 * <p><strong>Sample: Construct a ClientSecretCredential</strong></p>
 * <!-- src_embed com.azure.identity.credential.clientsecretcredential.construct -->
 * <pre>
 * ClientSecretCredential credential1 = new ClientSecretCredentialBuilder&#40;&#41;
 *     .tenantId&#40;tenantId&#41;
 *     .clientId&#40;clientId&#41;
 *     .clientSecret&#40;clientSecret&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.clientsecretcredential.construct -->
 *
 *
 * <p> Authenticate with User Credentials</p>
 * The Azure Identity library supports user credentials based authentication via {@link com.azure.identity.InteractiveBrowserCredential},
 * {@link com.azure.identity.DeviceCodeCredential} and {@link com.azure.identity.UsernamePasswordCredential}.
 * For more information refer to <a href="https://aka.ms/azsdk/java/identity/usercredentialsauthentication/docs"> Conceptual knowledge and configuration details </a>.
 *
 * <p><strong>Sample: Construct InteractiveBrowserCredential</strong></p>
 * <!-- src_embed com.azure.identity.credential.interactivebrowsercredential.construct -->
 * <pre>
 * InteractiveBrowserCredential interactiveBrowserCredential = new InteractiveBrowserCredentialBuilder&#40;&#41;
 *     .clientId&#40;clientId&#41;
 *     .redirectUrl&#40;&quot;http:&#47;&#47;localhost:8765&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.interactivebrowsercredential.construct -->
 *
 * <p> Authenticate in Developer Environment</p>
 * The Azure Identity library supports authenticating in developer environment via {@link com.azure.identity.AzureCliCredential} and
 * {@link com.azure.identity.IntelliJCredential}. These credentials offer a seamless authentication experience by utilizing
 * the cached Azure Plugin login information from their respective IDE tool.
 * For more information refer to <a href="https://aka.ms/azsdk/java/identity/developerenvironmentauthentication/docs"> Conceptual knowledge and configuration details </a>.
 *
 * <p><strong>Sample: Construct AzureCliCredential</strong></p>
 * <!-- src_embed com.azure.identity.credential.azureclicredential.construct -->
 * <pre>
 * AzureCliCredential azureCliCredential = new AzureCliCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.azureclicredential.construct -->
 *
 *
 * @see com.azure.identity.DefaultAzureCredential
 * @see com.azure.identity.ManagedIdentityCredential
 * @see com.azure.identity.EnvironmentCredential
 * @see com.azure.identity.ClientSecretCredential
 * @see com.azure.identity.ClientCertificateCredential
 * @see com.azure.identity.InteractiveBrowserCredential
 * @see com.azure.identity.DeviceCodeCredential
 * @see com.azure.identity.UsernamePasswordCredential
 * @see com.azure.identity.AzureCliCredential
 * @see com.azure.identity.IntelliJCredential
 */
package com.azure.identity;

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * The Azure Identity library provides Azure Active Directory (Azure AD) token authentication support across the Azure SDK.
 * The library focuses on OAuth authentication with Azure AD, and it offers various credential classes capable of
 * acquiring an Azure AD token to authenticate service requests. All the credential classes in this package are
 * implementations of the `TokenCredential` interface offered by azure-core, and any of them can be used to
 * construct service clients capable of authenticating with a `TokenCredential`.
 *
 * <H2>Getting Started:</H2>
 * The `DefaultAzureCredential` is appropriate for most scenarios where the application is intended to ultimately be run in Azure.
 * This is because the `DefaultAzureCredential` combines credentials commonly used to authenticate when deployed, with credentials
 * used to authenticate in a development environment. Note, this credential is intended to simplify getting started with the SDK
 * by handling common scenarios with reasonable default behaviors. Developers who want more control or whose scenario isn't
 * served by the default settings should use other credential types.
 * For more information refer to <a href="https://aka.ms/azsdk/java/identity/defaultazurecredential/docs"> Conceptual knowledge and configuration details </a>.
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
 * <H2> Authenticating on Azure Hosted Platforms via Managed Identity</H2>
 * The Managed Identity authenticates the managed identity (system or user assigned) of an Azure resource. So, if the
 * application is running inside an Azure resource that supports Managed Identity through IDENTITY/MSI, IMDS endpoints,
 * or both, then the ManagedIdentityCredential will get your application authenticated, and offers a great secretless authentication experience.
 * For more information refer to <a href="https://aka.ms/azsdk/java/identity/managedidentity/docs"> Conceptual knowledge and configuration details </a>.
 *
 * <p><strong>Sample: Construct a Managed Identity Credential</strong></p>
 * <!-- src_embed com.azure.identity.credential.managedidentitycredential.construct -->
 * <pre>
 * ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder&#40;&#41;
 *     .clientId&#40;clientId&#41; &#47;&#47; specify client id only if targeting a user-assigned managed identity.
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.managedidentitycredential.construct -->
 *
 * For other credentials that work well in Azure Hosted platforms, refer to the table below.
 * <table style="border: 1px; width: 100%;">
 *   <caption>Authenticate Azure-hosted applications</caption>
 *   <thead>
 *     <tr>
 *       <th>Credential class</th>
 *       <th>Usage</th>
 *       <th>Example</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.defaultazurecredential?view=azure-java-stable">DefaultAzureCredential</a></code></td>
 *       <td>provides a simplified authentication experience to quickly start developing applications run in Azure</td>
 *       <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-with-defaultazurecredential">example</a></td>
 *     </tr>
 *     <tr>
 *       <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.chainedtokencredential?view=azure-java-stable">ChainedTokenCredential</a></code></td>
 *       <td>allows users to define custom authentication flows composing multiple credentials</td>
 *       <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#chaining-credentials">example</a></td>
 *     </tr>
 *     <tr>
 *       <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.environmentcredential?view=azure-java-stable">EnvironmentCredential</a></code></td>
 *       <td>authenticates a service principal or user via credential information specified in environment variables</td>
 *       <td></td>
 *     </tr>
 *     <tr>
 *       <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.managedidentitycredential?view=azure-java-stable">ManagedIdentityCredential</a></code></td>
 *       <td>authenticates the managed identity of an Azure resource</td>
 *       <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-in-azure-with-managed-identity">example</a></td>
 *     </tr>
 *   </tbody>
 * </table>
 *
 *
 *
 * <H2> Authenticate with Service Principals</H2>
 * The Azure Active Directory (Azure AD) allows users to register service principals which can be used as an identity for authentication.
 * The authenticating is supported via a client secret or client certificate. The {@link com.azure.identity.ClientCertificateCredential} or {@link com.azure.identity.ClientSecretCredential}
 * here will work well to get your application authenticated.
 * For more information refer to <a href="https://aka.ms/azsdk/java/identity/serviceprincipal/docs"> Conceptual knowledge and configuration details </a>.
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
 * For other credentials that are compatible with service principal authentication, refer to the table below.
 * <table style="border: 1px; width: 100%;">
 *   <caption>Authenticate service principals</caption>
 *   <thead>
 *     <tr>
 *       <th>Credential class</th>
 *       <th>Usage</th>
 *       <th>Example</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.clientassertioncredential?view=azure-java-stable">ClientAssertionCredential</a></code></td>
 *       <td>authenticates a service principal using a signed client assertion</td>
 *       <td></td>
 *     </tr>
 *     <tr>
 *       <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.clientcertificatecredential?view=azure-java-stable">ClientCertificateCredential</a></code></td>
 *       <td>authenticates a service principal using a certificate</td>
 *       <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-service-principal-with-a-client-certificate">example</a></td>
 *     </tr>
 *     <tr>
 *       <td><code><a href="https://docs.microsoft.com/java/api/com.azure.identity.clientsecretcredential?view=azure-java-stable">ClientSecretCredential</a></code></td>
 *       <td>authenticates a service principal using a secret</td>
 *       <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-service-principal-with-a-client-secret">example</a></td>
 *     </tr>
 *   </tbody>
 * </table>
 *
 *
 * <H2> Authenticate with User Credentials</H2>
 * The Azure Identity library supports user credentials based authentication via {@link com.azure.identity.InteractiveBrowserCredential},
 * {@link com.azure.identity.DeviceCodeCredential} and {@link com.azure.identity.UsernamePasswordCredential}.
 * For more information refer to <a href="https://aka.ms/azsdk/java/identity/usercredential/docs"> Conceptual knowledge and configuration details </a>.
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
 *
 * For other credentials that are compatible with user credentials based authentication, refer to the table below.
 * <table style="border: 1px; width: 100%;">
 *   <caption>Authenticate users</caption>
 *   <thead>
 *     <tr>
 *       <th>Credential class</th>
 *       <th>Usage</th>
 *       <th>Example</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.authorizationcodecredential?view=azure-java-stable">AuthorizationCodeCredential</a></code></td>
 *       <td>authenticate a user with a previously obtained authorization code as part of an Oauth 2 flow</td>
 *       <td></td>
 *     </tr>
 *     <tr>
 *       <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.devicecodecredential?view=azure-java-stable">DeviceCodeCredential</a></code></td>
 *       <td>interactively authenticates a user on devices with limited UI</td>
 *       <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-with-device-code-flow">example</a></td>
 *     </tr>
 *     <tr>
 *       <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.interactivebrowsercredential?view=azure-java-stable">InteractiveBrowserCredential</a></code></td>
 *       <td>interactively authenticates a user with the default system browser</td>
 *       <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-interactively-in-the-browser">example</a></td>
 *     </tr>
 *     <tr>
 *       <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.onbehalfofcredential?view=azure-java-stable">OnBehalfOfCredential</a></code></td>
 *       <td>propagates the delegated user identity and permissions through the request chain</td>
 *       <td></td>
 *     </tr>
 *     <tr>
 *       <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.usernamepasswordcredential?view=azure-java-stable">UsernamePasswordCredential</a></code></td>
 *       <td>authenticates a user with a username and password without multi-factored auth</td>
 *       <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-with-username-and-password">example</a></td>
 *     </tr>
 *   </tbody>
 * </table>
 *
 * <H2> Authenticate in Developer Environment</H2>
 * The Azure Identity library supports authenticating in developer environment via {@link com.azure.identity.AzureCliCredential} and
 * {@link com.azure.identity.IntelliJCredential}. These credentials offer a seamless authentication experience by utilizing
 * the cached Azure Plugin login information from their respective IDE tool.
 * For more information refer to <a href="https://aka.ms/azsdk/java/identity/developerenvironment/docs"> Conceptual knowledge and configuration details </a>.
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
 * For other credentials that are compatible with developer tools authentication, refer to the table below.
 * <table style="border: 1px; width: 100%;">
 *   <caption>Authenticate via development tools</caption>
 *   <thead>
 *     <tr>
 *       <th>Credential class</th>
 *       <th>Usage</th>
 *       <th>Example</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.azureclicredential?view=azure-java-stable">AzureCliCredential</a></code></td>
 *       <td>Authenticate in a development environment with the enabled user or service principal in Azure CLI</td>
 *       <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-with-azure-cli">example</a></td>
 *     </tr>
 *     <tr>
 *       <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.azurepowershellcredential?view=azure-java-stable">AzurePowerShellCredential </a></code></td>
 *       <td>Authenticate in a development environment with the enabled user or service principal in Azure PowerShell</td>
 *       <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-with-azure-powershell">example</a></td>
 *     </tr>
 *     <tr>
 *       <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.intellijcredential?view=azure-java-stable">IntelliJCredential</a></code></td>
 *       <td>Authenticate in a development environment with the account in Azure Toolkit for IntelliJ</td>
 *       <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-with-intellij-idea">example</a></td>
 *     </tr>
 *     <tr>
 *       <td><code><a href="https://learn.microsoft.com/java/api/com.azure.identity.visualstudiocodecredential?view=azure-java-stable">VisualStudioCodeCredential</a></code></td>
 *       <td>Authenticate in a development environment with the account in Visual Studio Code Azure Account extension. </td>
 *       <td><a href="https://github.com/Azure/azure-sdk-for-java/wiki/Azure-Identity-Examples#authenticating-a-user-account-with-visual-studio-code">example</a></td>
 *     </tr>
 *   </tbody>
 * </table>
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

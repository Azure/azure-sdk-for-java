// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * The Azure Identity library provides Azure Active Directory (Azure AD) token authentication support across the Azure SDK.
 * The library focuses on OAuth authentication with Azure AD, and it offers various credential classes capable of
 * acquiring an Azure AD token to authenticate service requests. All the credential classes in this package are
 * implementations of the `TokenCredential` interface offered by azure-core, and any of them can be used to
 * construct service clients capable of authenticating with a `TokenCredential`.
 *
 * <H2> Getting Started</H2>
 * The {@link com.azure.identity.DefaultAzureCredential} is appropriate for most scenarios where the application is intended to ultimately be run in Azure.
 * This is because the {@link com.azure.identity.DefaultAzureCredential} combines credentials commonly used to authenticate when deployed, with credentials
 * used to authenticate in a development environment. Note, this credential is intended to simplify getting started with the SDK
 * by handling common scenarios with reasonable default behaviors. Developers who want more control or whose scenario isn't
 * served by the default settings should use other credential types.
 * For more information refer to the<a href="https://aka.ms/azsdk/java/identity/defaultazurecredential/docs"> conceptual knowledge and configuration details </a>.
 *
 * <p><strong>Sample: Construct a simple DefaultAzureCredential</strong></p>
 * <!-- src_embed com.azure.identity.credential.defaultazurecredential.construct -->
 * <pre>
 * TokenCredential defaultAzureCredential = new DefaultAzureCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.defaultazurecredential.construct -->
 * The Azure SDK client builders consume TokenCredential for Azure Active Directory (AAD) based authentication. The TokenCredential instantiated
 * above can be passed into most of the Azure SDK client builders to for AAD authentication.
 *
 *  <br> <br/>
 *
 * The {@link com.azure.identity.DefaultAzureCredential} works well in most of the scenarios as it executes a chain of credentials underneath which covers
 * well known authentication scenarios for both Azure hosted platforms and development environment. But, in some scenarios where only a specific
 * authentication mechanism will work we would like to use a specific credential to authenticate and skip running the chain of credentials offered by the {@link com.azure.identity.DefaultAzureCredential}.
 * Let's take a look at the individual authentication scenarios and their respective credential use below.
 *
 * <H2> Authenticating on Azure Hosted Platforms via Managed Identity</H2>
 * The Managed Identity credential authenticates the configured managed identity (system or user assigned) of an Azure resource. So, if the
 * application is running inside an Azure resource that supports Managed Identity through IDENTITY/MSI, IMDS endpoints,
 * or both, then the ManagedIdentityCredential will get your application authenticated, and offers a great secretless authentication experience.
 * For more information refer to the <a href="https://aka.ms/azsdk/java/identity/managedidentity/docs"> conceptual knowledge and configuration details </a>.
 *
 * <p><strong>Sample: Construct a Managed Identity Credential</strong></p>
 * <!-- src_embed com.azure.identity.credential.managedidentitycredential.construct -->
 * <pre>
 * TokenCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder&#40;&#41;
 *     .clientId&#40;clientId&#41; &#47;&#47; specify client id only if targeting a user-assigned managed identity.
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.managedidentitycredential.construct -->
 *
 * For other credentials that work well in Azure Hosted platforms, refer to the table below.
 * <br> <br/>
 * <table style="border: 2px; width: 50%;">
 *   <caption><strong>Authenticate Azure-hosted applications</strong></caption>
 *   <thead>
 *     <tr>
 *       <th>Credential class</th>
 *       <th>Usage</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link com.azure.identity.DefaultAzureCredential}</td>
 *       <td>Provides a simplified authentication experience to quickly start developing applications run in Azure.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link com.azure.identity.ChainedTokenCredential}</td>
 *       <td>Allows users to define custom authentication flows composing multiple credentials.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link com.azure.identity.EnvironmentCredential}</td>
 *       <td>Authenticates a service principal or user via credential information specified in environment variables.</td>
 *       <td></td>
 *     </tr>
 *     <tr>
 *       <td>{@link com.azure.identity.ManagedIdentityCredential}</td>
 *       <td>Authenticates the managed identity of an Azure resource.</td>
 *     </tr>
 *   </tbody>
 * </table>
 *
 * Refer to the JavaDoc for each of these classes for more details about when these credential types should be used.
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
 * TokenCredential clientSecretCredential = new ClientSecretCredentialBuilder&#40;&#41;
 *     .tenantId&#40;tenantId&#41;
 *     .clientId&#40;clientId&#41;
 *     .clientSecret&#40;clientSecret&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.clientsecretcredential.construct -->
 *
 * For other credentials that are compatible with service principal authentication, refer to the table below.
 * <br> <br/>
 * <table style="border: 2px; width: 50%;">
 *   <caption><strong>Authenticate service principals</strong></caption>
 *   <thead>
 *     <tr>
 *       <th>Credential class</th>
 *       <th>Usage</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link com.azure.identity.ClientAssertionCredential}</td>
 *       <td>Authenticates a service principal using a signed client assertion.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link com.azure.identity.ClientCertificateCredential}</td>
 *       <td>Authenticates a service principal using a certificate.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link com.azure.identity.ClientSecretCredential}</td>
 *       <td>Authenticates a service principal using a secret.</td>
 *     </tr>
 *   </tbody>
 * </table>
 *
 * Refer to the JavaDoc for each of these classes for more details about when these credential types should be used.
 *
 * <H2> Authenticate with User Credentials</H2>
 * The Azure Identity library supports user credentials based authentication via {@link com.azure.identity.InteractiveBrowserCredential},
 * {@link com.azure.identity.DeviceCodeCredential} and {@link com.azure.identity.UsernamePasswordCredential}.
 * For more information refer to <a href="https://aka.ms/azsdk/java/identity/usercredential/docs"> Conceptual knowledge and configuration details </a>.
 *
 * <p><strong>Sample: Construct InteractiveBrowserCredential</strong></p>
 * <!-- src_embed com.azure.identity.credential.interactivebrowsercredential.construct -->
 * <pre>
 * TokenCredential interactiveBrowserCredential = new InteractiveBrowserCredentialBuilder&#40;&#41;
 *     .clientId&#40;clientId&#41;
 *     .redirectUrl&#40;&quot;http:&#47;&#47;localhost:8765&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.interactivebrowsercredential.construct -->
 *
 *
 * For other credentials that are compatible with user credentials based authentication, refer to the table below.
 * <br> <br/>
 * <table style="border: 2px; width: 50%;">
 *   <caption><strong>Authenticate users</strong></caption>
 *   <thead>
 *     <tr>
 *       <th>Credential class</th>
 *       <th>Usage</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link com.azure.identity.AuthorizationCodeCredential}</td>
 *       <td>Authenticate a user with a previously obtained authorization code as part of an Oauth 2 flow.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link com.azure.identity.DeviceCodeCredential}</td>
 *       <td>Interactively authenticates a user on devices with limited UI.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link com.azure.identity.InteractiveBrowserCredential}</td>
 *       <td>Interactively authenticates a user with the default system browser.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link com.azure.identity.OnBehalfOfCredential}</td>
 *       <td>Propagates the delegated user identity and permissions through the request chain.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link com.azure.identity.UsernamePasswordCredential}</td>
 *       <td>Authenticates a user with a username and password without multi-factored auth.</td>
 *     </tr>
 *   </tbody>
 * </table>
 *
 * Refer to the JavaDoc for each of these classes for more details about when these credential types should be used.
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
 * TokenCredential azureCliCredential = new AzureCliCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.azureclicredential.construct -->
 *
 *
 * For other credentials that are compatible with developer tools authentication, refer to the table below.
 * <br> <br/>
 * <table style="border: 2px; width: 50%;">
 *   <caption><strong>Authenticate via development tools</strong></caption>
 *   <thead>
 *     <tr>
 *       <th>Credential class</th>
 *       <th>Usage</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link com.azure.identity.AzureCliCredential}</td>
 *       <td>Authenticate in a development environment with the enabled user or service principal in Azure CLI.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link com.azure.identity.AzurePowerShellCredential}</td>
 *       <td>Authenticate in a development environment with the enabled user or service principal in Azure PowerShell.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link com.azure.identity.IntelliJCredential}</td>
 *       <td>Authenticate in a development environment with the account in Azure Toolkit for IntelliJ.</td>
 *     </tr>
 *   </tbody>
 * </table>
 *
 * Refer to the JavaDoc for each of these classes for more details about when these credential types should be used.
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

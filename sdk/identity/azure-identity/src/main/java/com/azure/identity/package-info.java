// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p>The Azure Identity library provides
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a> token
 * authentication support across the
 * <a href="https://learn.microsoft.com/azure/developer/java/sdk/">Azure SDK</a>. The library focuses on
 * OAuth authentication with Microsoft Entra ID, and it offers various credential classes capable of acquiring a Microsoft Entra token
 * to authenticate service requests. All the credential classes in this package are implementations of the
 * TokenCredential interface offered by azure-core, and any of them can be used to construct service clients capable
 * of authenticating with a TokenCredential.</p>
 *
 * <h2>Getting Started</h2>
 *
 * <p>The {@link com.azure.identity.DefaultAzureCredential} is appropriate for most scenarios where the application is
 * intended to ultimately be run in Azure. This is because the {@link com.azure.identity.DefaultAzureCredential}
 * combines credentials commonly used to authenticate when deployed, with credentials used to authenticate in a
 * development environment.</p>
 *
 * <p><strong>Note:</strong> This credential is intended to simplify getting started with the SDK by handling
 * common scenarios with reasonable default behaviors. Developers who want more control or whose scenario isn't
 * served by the default settings should use other credential types (detailed below). For more information refer to the
 * <a href="https://aka.ms/azsdk/java/identity/defaultazurecredential/docs">default azure credential conceptual
 * documentation</a>.</p>
 *
 * <p><strong>Sample: Construct a simple DefaultAzureCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.DefaultAzureCredential}, using
 * the {@link com.azure.identity.DefaultAzureCredentialBuilder} to configure it. Once this credential is created, it
 * may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.defaultazurecredential.construct -->
 * <pre>
 * TokenCredential defaultAzureCredential = new DefaultAzureCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.defaultazurecredential.construct -->
 *
 * <p>Further, it is recommended to read
 * {@link com.azure.identity.DefaultAzureCredential DefaultAzureCredential JavaDocs} for more detailed information about
 * the credential usage and the chain of credentials it runs underneath.</p>
 *
 * <p>The {@link com.azure.identity.DefaultAzureCredential} works well in most of the scenarios as it executes a chain
 * of credentials underneath which covers well known authentication scenarios for both Azure hosted platforms and
 * development environment. But, in some scenarios where only a specific authentication mechanism will work, it is
 * recommended to use that specific credential to authenticate. Let's take a look at the individual
 * authentication scenarios and their respective credential use below.</p>
 *
 * <hr/>
 *
 * <h2>Authenticate in Developer Environment</h2>
 *
 * <p>Azure supports developer environment authentication via Azure CLI, Azure Powershell and Azure Tools for IntelliJ
 * plugin in IntelliJ IDE. It involves interactively authenticating using user credentials locally on the developer
 * machine. Once authenticated, the login information is persisted.</p>
 *
 * <p>The Azure Identity library supports authenticating in developer environment via
 * {@link com.azure.identity.AzureCliCredential}, {@link com.azure.identity.AzurePowerShellCredential} and
 * {@link com.azure.identity.IntelliJCredential}. These credentials offer a seamless authentication experience by
 * utilizing the cached Azure Plugin login information from their respective IDE tool. For more information refer to the
 * <a href="https://aka.ms/azsdk/java/identity/developerenvironment/docs">developer environment authentication
 * documentation</a>.</p>
 *
 * <p><strong>Sample: Construct AzureCliCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.AzureCliCredential},
 * using the {@link com.azure.identity.AzureCliCredentialBuilder} to configure it .Once this credential
 * is created, it may be passed into the builder of many of the Azure SDK for Java client builders as the
 * 'credential' parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.azureclicredential.construct -->
 * <pre>
 * TokenCredential azureCliCredential = new AzureCliCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.azureclicredential.construct -->
 *
 * <p>Further, it is recommended to read
 * {@link com.azure.identity.AzureCliCredential AzureCliCredential JavaDocs} for more detailed
 * information about the credential usage.</p>
 *
 * <p>For other credentials that are compatible with developer tools authentication, refer to the table below.</p>
 *
 * <br/>
 *
 * <table border="1">
 *   <caption><strong>Authenticate via development tools</strong></caption>
 *   <thead>
 *     <tr>
 *       <th>Credential class</th>
 *       <th>Usage</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link com.azure.identity.AzurePowerShellCredential}</td>
 *       <td>This credential authenticates in a development environment with the logged in user or service principal
 *       in Azure PowerShell. It utilizes the account of the already logged in user on Azure Powershell
 *       to get an access token. If there's no user logged in locally on Azure Powershell, then it will not work.
 *       Further, it is recommended to read
 *       {@link com.azure.identity.AzurePowerShellCredential AzurePowerShellCredential JavaDocs} for more
 *       information about the credential usage.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link com.azure.identity.IntelliJCredential}</td>
 *       <td>This credential authenticates in a development environment with the logged in user or service principal
 *       in Azure Toolkit for IntelliJ plugin on IntelliJ IDE. It utilizes the cached login information of the Azure
 *       Toolkit for IntelliJ plugin to seamlessly authenticate the application. If there's no user logged in locally
 *       on Azure Toolkit for IntelliJ in IntelliJ IDE, then it will not work. Further, it is recommended to read
 *       {@link com.azure.identity.IntelliJCredential IntelliJCredential JavaDocs} for more
 *       information about the credential usage.</td>
 *     </tr>
 *   </tbody>
 * </table>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2> Authenticating on Azure Hosted Platforms via Managed Identity</h2>
 *
 * <p><a href="https://learn.microsoft.com/entra/identity/managed-identities-azure-resources/">Azure
 * Managed Identity</a> is a feature in
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a>
 * that provides a way for applications running on Azure to authenticate themselves with Azure resources without
 * needing to manage or store any secrets like passwords or keys.</p>
 *
 * <p>The {@link com.azure.identity.ManagedIdentityCredential} authenticates the configured managed identity
 * (system or user assigned) of an Azure resource. So, if the application is running inside an Azure resource that
 * supports Managed Identity through IDENTITY/MSI, IMDS endpoints, or both, then the
 * {@link com.azure.identity.ManagedIdentityCredential} will get your application authenticated, and offers a great
 * secretless authentication experience. For more information refer to the
 * <a href="https://aka.ms/azsdk/java/identity/managedidentity/docs">managed identity authentication
 * documentation</a>.</p>
 *
 * <p><strong>Sample: Construct a Managed Identity Credential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.ManagedIdentityCredential},
 * using the {@link com.azure.identity.ManagedIdentityCredentialBuilder} to configure it. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the
 * 'credential' parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.managedidentitycredential.construct -->
 * <pre>
 * TokenCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.managedidentitycredential.construct -->
 *
 * <p>Further, it is recommended to read
 * {@link com.azure.identity.ManagedIdentityCredential ManagedIdentityCredential JavaDocs} for more detailed information
 * about the credential usage and the Azure platforms it supports.</p>
 *
 * <p>For other credentials that work well in Azure Hosted platforms, refer to the table below.</p>
 *
 * <br/>
 *
 * <table border="1">
 *   <caption><strong>Authenticate Azure-hosted applications</strong></caption>
 *   <thead>
 *     <tr>
 *       <th>Credential class</th>
 *       <th>Usage</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link com.azure.identity.EnvironmentCredential}</td>
 *       <td>This credential authenticates a service principal or user via credential information specified in
 *       environment variables. The service principal authentication works well in Azure hosted platforms when Managed
 *       Identity is not available. Further, it is recommended to read
 *       {@link com.azure.identity.EnvironmentCredential EnvironmentCredential JavaDocs} for more information about
 *       the credential usage.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link com.azure.identity.ChainedTokenCredential}</td>
 *       <td>This credential allows users to define custom authentication flows by chaining multiple credentials
 *       together. For example, the {@link com.azure.identity.ManagedIdentityCredential} and
 *       {@link com.azure.identity.EnvironmentCredential} can be chained together to sequentially execute on Azure
 *       hosted platforms. The credential that first returns the token is used for authentication. Further, it is
 *       recommended to read {@link com.azure.identity.ChainedTokenCredential ChainedTokenCredential JavaDocs} for more
 *       information about the credential usage.</td>
 *     </tr>
 *   </tbody>
 * </table>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Authenticate with Service Principals</h2>
 *
 * <p>Service Principal authentication is a type of authentication in Azure that enables a non-interactive login to
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a>, allowing an
 * application or service to authenticate itself with Azure resources.
 * A Service Principal is essentially an identity created for an application in Microsoft Entra ID that can be used to
 * authenticate with Azure resources. It's like a "user identity" for the application or service, and it provides
 * a way for the application to authenticate itself with Azure resources without needing to use a user's credentials.
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a> allows users to
 * register service principals which can be used as an identity for authentication.
 * A client secret and/or a client certificate associated with the registered service principal is used as the password
 * when authenticating the service principal.</p>
 *
 * <p>The Azure Identity library supports both client secret and client
 * certificate based service principal authentication via {@link com.azure.identity.ClientSecretCredential} and
 * {@link com.azure.identity.ClientCertificateCredential} respectively. For more information refer to the
 * <a href="https://aka.ms/azsdk/java/identity/serviceprincipal/docs">service principal authentication
 * documentation</a>.</p>
 *
 * <p><strong>Sample: Construct a ClientSecretCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.ClientSecretCredential},
 * using the {@link com.azure.identity.ClientSecretCredentialBuilder} to configure it. The {@code tenantId},
 * {@code clientId} and {@code clientSecret} parameters are required to create
 * {@link com.azure.identity.ClientSecretCredential} .Once this credential is created, it may be passed into the
 * builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
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
 * <p>Further, it is recommended to read
 * {@link com.azure.identity.ClientSecretCredential ClientSecretCredential JavaDocs} for more detailed information
 * about the credential usage.</p>
 *
 * <p>For other credentials that are compatible with service principal authentication, refer to the table below.</p>
 *
 * <br/>
 *
 * <table border="1">
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
 *       <td>This credential authenticates a service principal using a signed client assertion.
 *       It allows clients to prove their identity to Microsoft Entra ID without requiring them to disclose their
 *       credentials (such as a username and password). Further, it is recommended to read
 *       {@link com.azure.identity.ClientAssertionCredential ClientAssertionCredential JavaDocs} for more
 *       information about the credential usage.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link com.azure.identity.ClientCertificateCredential}</td>
 *       <td>This credential authenticates a service principal using a certificate. It doesn't require transmission of
 *       a client secret and mitigates the security related password storage and network transmission issues.
 *       Further, it is recommended to read {@link com.azure.identity.ClientCertificateCredential
 *       ClientCertificateCredential JavaDocs} for more information about the credential usage.</td>
 *     </tr>
 *   </tbody>
 * </table>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Authenticate with User Credentials</h2>
 *
 * <p>User credential authentication is a type of authentication in Azure that involves a user providing their
 * username and password to authenticate with Azure resources. In Azure, user credential authentication can be used to
 * authenticate with <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a>.</p>
 *
 * <p>The Azure Identity library supports user credentials based authentication via
 * {@link com.azure.identity.InteractiveBrowserCredential}, {@link com.azure.identity.DeviceCodeCredential} and
 * {@link com.azure.identity.UsernamePasswordCredential}. For more information refer to the
 * <a href="https://aka.ms/azsdk/java/identity/usercredential/docs">user credential authentication documentation</a>.
 * </p>
 *
 * <p><strong>Sample: Construct InteractiveBrowserCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.InteractiveBrowserCredential},
 * using the {@link com.azure.identity.InteractiveBrowserCredentialBuilder} to configure it .Once this credential
 * is created, it may be passed into the builder of many of the Azure SDK for Java client builders as the
 * 'credential' parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.interactivebrowsercredential.construct -->
 * <pre>
 * TokenCredential interactiveBrowserCredential = new InteractiveBrowserCredentialBuilder&#40;&#41;
 *     .redirectUrl&#40;&quot;http:&#47;&#47;localhost:8765&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.interactivebrowsercredential.construct -->
 *
 * <p>Further, it is recommended to read
 * {@link com.azure.identity.InteractiveBrowserCredential InteractiveBrowserCredential JavaDocs} for more information
 * about the credential usage.</p>
 *
 * <p>For other credentials that are compatible with user credentials based authentication, refer to the table below.
 * </p>
 *
 * <br/>
 *
 * <table border="1">
 *   <caption><strong>Authenticate users</strong></caption>
 *   <thead>
 *     <tr>
 *       <th>Credential class</th>
 *       <th>Usage</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link com.azure.identity.DeviceCodeCredential}</td>
 *       <td>This credential interactively authenticates a user on devices with limited UI. It prompts users
 *       to open an authentication URL with a device code on a UI enabled device and requires them to interactively
 *       authenticate there. Once authenticated, the original device requesting authentication gets authenticated
 *       and receives the access token. Further, it is recommended to read
 *       {@link com.azure.identity.DeviceCodeCredential DeviceCodeCredential JavaDocs} for more
 *       information about the credential usage.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link com.azure.identity.AuthorizationCodeCredential}</td>
 *       <td>This credential authenticates a user with a previously obtained authorization code as part of an
 *       Oauth 2 flow. This is applicable for applications which control the logic of interactive user authentication
 *       to fetch an authorization code first. Once the application has received the authorization code, it can
 *       then configure it on this credential and use it to get an access token. Further, it is recommended to read
 *       {@link com.azure.identity.AuthorizationCodeCredential AuthorizationCodeCredential JavaDocs} for more
 *       information about the credential usage.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link com.azure.identity.UsernamePasswordCredential}</td>
 *       <td>This credential authenticates a user with a username and password without multi-factored auth.
 *       This credential can be used on developer environment for user principals which do not require
 *       2FA/MFA (multi-facotred) authentication. Further, it is recommended to read
 *       {@link com.azure.identity.UsernamePasswordCredential UsernamePasswordCredential JavaDocs} for more
 *       information about the credential usage.</td>
 *     </tr>
 *   </tbody>
 * </table>
 *
 * <br/>
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

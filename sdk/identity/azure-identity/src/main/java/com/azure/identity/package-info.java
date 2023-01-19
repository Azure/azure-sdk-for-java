// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * The Azure Identity library provides Azure Active Directory (Azure AD) token authentication support across the Azure SDK.
 * The library focuses on OAuth authentication with Azure AD, and it offers various credential classes capable of
 * acquiring an Azure AD token to authenticate service requests. All of the credential classes in this package are
 * implementations of the `TokenCredential` interface offered by azure-core, and any of them can be used by to
 * construct service clients capable of authenticating with a `TokenCredential`.
 *
 *
 * <p> Getting Started:</p>
 * The `DefaultAzureCredential` is appropriate for most scenarios where the application is intended to ultimately be run in Azure.
 * This is because the `DefaultAzureCredential` combines credentials commonly used to authenticate when deployed, with credentials
 * used to authenticate in a development environment. Note, this credential is intended to simplify getting started with the SDK
 * by handling common scenarios with reasonable default behaviors. Developers who want more control or whose scenario isn't
 * served by the default settings should use other credential types.
 *
 * TODO: Add Code sample for DefaultAzureCredential
 *
 *
 * <p> Authenticating on Azure Hosted Platforms via Managed Identity</p>
 * The Managed Identity authenticates the managed identity (system or user assigned) of an Azure resource. So, if the
 * application is running inside an Azure resource that supports Managed Identity through IDENTITY/MSI, IMDS endpoints,
 * or both, then the ManagedIdentityCredential will get your application authenticated, and offers a great secretless authentication experience.
 *
 * TODO: Add Code Sample for Managed Identity
 *
 *
 * <p> Authenticate with Service Principals</p>
 * The Azure Active Directory (Azure AD) allows users to register service principals which can be used as an identity for authentication.
 * The authenticating is supported via a client secret or client certificate. The ClientCertificateCredential or ClientSecretCredential
 * here will work well to get your applicaton authenticated.
 *
 * TODO: Add Code Sample for Service Principals
 *
 *
 * <p> Authenticate with User Credentials</p>
 *
 *
 * TODO: Add Code Sample for User Credentials
 *
 *
 *
 *
 *
 *
 * @see com.azure.identity.DefaultAzureCredential
 * @see com.azure.identity.ClientSecretCredential
 * @see com.azure.identity.ClientCertificateCredential
 */
package com.azure.identity;

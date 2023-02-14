// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.TokenCredential;

import java.util.List;

/**
 *
 * The DefaultAzureCredential is appropriate for most scenarios where the application ultimately runs in the Azure Cloud.
 * DefaultAzureCredential combines credentials that are commonly used to authenticate when deployed,
 * with credentials that are used to authenticate in a development environment. The DefaultAzureCredential will
 * attempt to authenticate via the following mechanisms in order.
 *
 * <ol>
 * <li>{@link EnvironmentCredential} - The DefaultAzureCredential will read account information specified via environment variables and use it to authenticate.</li>
 * <li>{@link ManagedIdentityCredential} - If the application deploys to an Azure host with Managed Identity enabled, the DefaultAzureCredential will authenticate with that account.</li>
 * <li>{@link IntelliJCredential} - If you've authenticated via Azure Toolkit for IntelliJ, the DefaultAzureCredential will authenticate with that account.</li>
 * <li>{@link AzureCliCredential} - If you've authenticated an account via the Azure CLI az login command, the DefaultAzureCredential will authenticate with that account.</li>
 * <li>{@link AzurePowerShellCredential} - If you've authenticated an account via the Azure Power Shell Az Login command, the DefaultAzureCredential will authenticate with that account.</li>
 * <li>Fails if none of the credentials above could be created.</li>
 * </ol>
 *
 * For more information refer to <a href="https://aka.ms/azsdk/java/identity/defaultazurecredential/docs"> Conceptual knowledge and configuration details </a>.
 *
 * <H2>Configure DefaultAzureCredential</H2>
 * DefaultAzureCredential supports a set of configurations through setters on the DefaultAzureCredentialBuilder or environment variables.
 * <ol>
 *     <li>Setting the environment variables AZURE_CLIENT_ID, AZURE_CLIENT_SECRET/AZURE_CLIENT_CERTIFICATE_PATH, and AZURE_TENANT_ID configures the DefaultAzureCredential to authenticate as the service principal specified by the values.</li>
 *     <li>Setting {@link DefaultAzureCredentialBuilder#managedIdentityClientId(String)} on the builder or the environment variable AZURE_CLIENT_ID configures the DefaultAzureCredential to authenticate as a user-defined managed identity, while leaving them empty configures it to authenticate as a system-assigned managed identity.</li>
 *     <li>Setting .tenantId(String) on the builder or the environment variable AZURE_TENANT_ID configures the DefaultAzureCredential to authenticate to a specific tenant for Visual Studio Code, and IntelliJ IDEA.</li>
 * </ol>
 *
 * <p><strong>Sample: Construct DefaultAzureCredential</strong></p>
 * <!-- src_embed com.azure.identity.credential.defaultazurecredential.construct -->
 * <pre>
 * DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.defaultazurecredential.construct -->
 *
 * <p><strong>Sample: Construct DefaultAzureCredential with User Assigned Managed Identity </strong></p>
 * <!-- src_embed com.azure.identity.credential.defaultazurecredential.constructwithuserassignedmanagedidentity -->
 * <pre>
 * DefaultAzureCredential dacWithUserAssignedManagedIdentity = new DefaultAzureCredentialBuilder&#40;&#41;
 *     .managedIdentityClientId&#40;&quot;&lt;Managed-Identity-Client-Id&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.defaultazurecredential.constructwithuserassignedmanagedidentity -->
 *
 *
 * @see com.azure.identity.ManagedIdentityCredential
 * @see com.azure.identity.EnvironmentCredential
 * @see com.azure.identity.ClientSecretCredential
 * @see com.azure.identity.ClientCertificateCredential
 * @see com.azure.identity.UsernamePasswordCredential
 * @see com.azure.identity.AzureCliCredential
 * @see com.azure.identity.IntelliJCredential
 */
@Immutable
public final class DefaultAzureCredential extends ChainedTokenCredential {
    /**
     * Creates default DefaultAzureCredential instance to use. This will use AZURE_CLIENT_ID,
     * AZURE_CLIENT_SECRET, and AZURE_TENANT_ID environment variables to create a
     * ClientSecretCredential.
     *
     * If these environment variables are not available, then this will use the Shared MSAL
     * token cache.
     *
     * @param tokenCredentials the list of credentials to execute for authentication.
     */
    DefaultAzureCredential(List<TokenCredential> tokenCredentials) {
        super(tokenCredentials);
    }
}

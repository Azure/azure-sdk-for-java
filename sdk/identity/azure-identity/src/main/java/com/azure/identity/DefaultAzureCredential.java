// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.TokenCredential;

import java.util.List;

/**
 * <p>The DefaultAzureCredential is appropriate for most scenarios where the application is intended to ultimately be
 * run in Azure. DefaultAzureCredential combines credentials that are commonly used to authenticate when deployed,
 * with credentials that are used to authenticate in a development environment. The DefaultAzureCredential will
 * attempt to authenticate via the following mechanisms in order.</p>
 *
 * <img src="doc-files/DefaultAzureCredentialAuthFlow.png" alt="">
 *
 * <ol>
 * <li>{@link EnvironmentCredential} - The DefaultAzureCredential will read account information specified via
 * environment variables and use it to authenticate.</li>
 * <li>{@link WorkloadIdentityCredential} - If the app is deployed on Kubernetes with environment variables
 * set by the workload identity webhook, DefaultAzureCredential will authenticate the configured identity.</li>
 * <li>{@link ManagedIdentityCredential} - If the application deploys to an Azure host with Managed Identity enabled,
 * the DefaultAzureCredential will authenticate with that account.</li>
 * <li>{@link IntelliJCredential} - If you've authenticated via
 * <a href="https://learn.microsoft.com/azure/developer/java/toolkit-for-intellij/">Azure Toolkit for
 * IntelliJ</a>, the DefaultAzureCredential will authenticate with that account.</li>
 * <li>{@link AzureCliCredential} - If you've authenticated an account via the Azure CLI {@code az login} command, the
 * DefaultAzureCredential will authenticate with that account.</li>
 * <li>{@link AzurePowerShellCredential} - If you've authenticated an account via the
 * <a href="https://learn.microsoft.com/powershell/azure/?view=azps-9.4.0">Azure Power Shell</a> {@code Az Login}
 * command, the DefaultAzureCredential will authenticate with that account.</li>
 * <li>Fails if none of the credentials above could be created.</li>
 * </ol>
 *
 * <p>For more information refer to the
 * <a href="https://aka.ms/azsdk/java/identity/defaultazurecredential/docs">default azure credential authentication
 * docs</a>.</p>
 *
 * <h2>Configure DefaultAzureCredential</h2>
 *
 * <p>DefaultAzureCredential supports a set of configurations through setters on the
 * {@link DefaultAzureCredentialBuilder} or environment variables.</p>
 *
 * <ol>
 *     <li>Setting the environment variables {@code AZURE_CLIENT_ID},
 *     {@code AZURE_CLIENT_SECRET/AZURE_CLIENT_CERTIFICATE_PATH}, and {@code AZURE_TENANT_ID} configures the
 *     DefaultAzureCredential to authenticate as the service principal specified by the values.</li>
 *     <li>Setting {@link DefaultAzureCredentialBuilder#managedIdentityClientId(String)} on the builder or the
 *     environment variable AZURE_CLIENT_ID configures the DefaultAzureCredential to authenticate as a user-defined
 *     managed identity, while leaving them empty configures it to authenticate as a system-assigned managed identity.
 *     </li>
 *     <li>Setting {@link DefaultAzureCredentialBuilder#tenantId(String)} on the builder or the environment
 *     variable {@code AZURE_TENANT_ID} configures the DefaultAzureCredential to authenticate to a specific tenant for
 *     Visual Studio Code, and IntelliJ IDEA.</li>
 * </ol>
 *
 * <p><strong>Sample: Construct DefaultAzureCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a DefaultAzureCredential, using
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
 * <p><strong>Sample: Construct DefaultAzureCredential with User Assigned Managed Identity </strong></p>
 *
 * <p>User-Assigned Managed Identity (UAMI) in Azure is a feature that allows you to create an identity in
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a> that is
 * associated with one or more Azure resources. This identity can then be used to authenticate and
 * authorize access to various Azure services and resources. The following code sample demonstrates the creation of
 * a DefaultAzureCredential to target a user assigned managed identity, using the
 * {@link com.azure.identity.DefaultAzureCredentialBuilder} to configure it. Once this credential is created, it
 * may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.defaultazurecredential.constructwithuserassignedmanagedidentity -->
 * <pre>
 * TokenCredential dacWithUserAssignedManagedIdentity = new DefaultAzureCredentialBuilder&#40;&#41;
 *     .managedIdentityClientId&#40;&quot;&lt;Managed-Identity-Client-Id&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.defaultazurecredential.constructwithuserassignedmanagedidentity -->
 *
 * @see com.azure.identity
 * @see DefaultAzureCredentialBuilder
 * @see ManagedIdentityCredential
 * @see EnvironmentCredential
 * @see ClientSecretCredential
 * @see ClientCertificateCredential
 * @see UsernamePasswordCredential
 * @see AzureCliCredential
 * @see IntelliJCredential
 */
@Immutable
public final class DefaultAzureCredential extends ChainedTokenCredential {
    /**
     * Creates default DefaultAzureCredential instance to use.
     *
     * @param tokenCredentials the list of credentials to execute for authentication.
     */
    DefaultAzureCredential(List<TokenCredential> tokenCredentials) {
        super(tokenCredentials);
        this.enableUseCachedWorkingCredential();
    }
}

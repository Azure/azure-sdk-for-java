// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.core.credentials.TokenCredential;

import java.util.List;

/**
 * <p>DefaultAzureCredential simplifies authentication while developing apps that deploy to Azure by combining credentials
 * used in Azure hosting environments with credentials used in local development. In production, it's better to use something
 * else. For more information, see <a href="https://aka.ms/azsdk/java/identity/credential-chains#usage-guidance-for-defaultazurecredential">Usage guidance for DefaultAzureCredential</a>.
 *
 * <p>Attempts to authenticate with each of these credentials, in the following order, stopping when one provides a token:</p>
 *
 * <ol>
 * <li>{@link EnvironmentCredential}</li>
 * <li>{@link WorkloadIdentityCredential}</li>
 * <li>{@link ManagedIdentityCredential}</li>
 * <li>{@link AzureToolkitCredential}</li>
 * <li>{@link AzureCliCredential}</li>
 * <li>{@link AzurePowerShellCredential}</li>
 * <li>{@link AzureDeveloperCliCredential}</li>
 * </ol>
 *
 * <p>Consult the documentation of these credentials for more information on how they attempt authentication.</p>
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
 * the {@link DefaultAzureCredentialBuilder} to configure it. Once this credential is created, it
 * may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <pre>
 * TokenCredential defaultAzureCredential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 *
 * <p><strong>Sample: Construct DefaultAzureCredential with User Assigned Managed Identity </strong></p>
 *
 * <p>User-Assigned Managed Identity (UAMI) in Azure is a feature that allows you to create an identity in
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a> that is
 * associated with one or more Azure resources. This identity can then be used to authenticate and
 * authorize access to various Azure services and resources. The following code sample demonstrates the creation of
 * a DefaultAzureCredential to target a user assigned managed identity, using the
 * {@link DefaultAzureCredentialBuilder} to configure it. Once this credential is created, it
 * may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <pre>
 * TokenCredential dacWithUserAssignedManagedIdentity
 *     = new DefaultAzureCredentialBuilder&#40;&#41;.managedIdentityClientId&#40;&quot;&lt;Managed-Identity-Client-Id&quot;&#41;.build&#40;&#41;;
 * </pre>
 *
 * @see com.azure.v2.identity
 * @see DefaultAzureCredentialBuilder
 * @see ManagedIdentityCredential
 * @see EnvironmentCredential
 * @see ClientSecretCredential
 * @see ClientCertificateCredential
 * @see AzureCliCredential
 * @see AzureToolkitCredential
 */
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

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.customtokenproxy.CustomTokenProxyConfiguration;
import com.azure.identity.implementation.customtokenproxy.CustomTokenProxyHttpClient;
import com.azure.identity.implementation.customtokenproxy.ProxyConfig;
import com.azure.identity.implementation.util.ValidationUtil;

import static com.azure.identity.ManagedIdentityCredential.AZURE_FEDERATED_TOKEN_FILE;

/**
 * Fluent credential builder for instantiating a {@link WorkloadIdentityCredential}.
 *
 * <p>Workload identity authentication exchanges a projected Kubernetes service account token for a Microsoft Entra
 * access token, without requiring an application secret or certificate. The credential reads its tenant ID, client ID,
 * and token file path from the workload identity environment variables unless they are configured on this builder.
 * Refer to <a href="https://learn.microsoft.com/azure/aks/workload-identity-overview">Microsoft Entra Workload ID</a>
 * for more information. To use Azure Kubernetes Service identity bindings, opt in with {@link #enableAzureProxy()}.</p>
 *
 * <p><strong>Sample: Construct WorkloadIdentityCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link WorkloadIdentityCredential},
 * using the {@link WorkloadIdentityCredentialBuilder} to configure it. The {@code clientId},
 * is required to create {@link WorkloadIdentityCredential}. Once this credential is created, it may be passed into the
 * builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.workloadidentitycredential.construct -->
 * <pre>
 * TokenCredential workloadIdentityCredential = new WorkloadIdentityCredentialBuilder&#40;&#41;.clientId&#40;&quot;&lt;clientID&gt;&quot;&#41;
 *     .tenantId&#40;&quot;&lt;tenantID&gt;&quot;&#41;
 *     .tokenFilePath&#40;&quot;&lt;token-file-path&gt;&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.workloadidentitycredential.construct -->
 *
 * @see WorkloadIdentityCredential
 */
public class WorkloadIdentityCredentialBuilder extends AadCredentialBuilderBase<WorkloadIdentityCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(WorkloadIdentityCredentialBuilder.class);
    private String tokenFilePath;
    private boolean enableAzureProxy;

    /**
     * Creates an instance of a WorkloadIdentityCredentialBuilder.
     */
    public WorkloadIdentityCredentialBuilder() {
    }

    /**
     * Configure the path to a file containing a Kubernetes service account token that authenticates the identity.
     * The file path is required to authenticate.
     *
     * @param tokenFilePath the path to the file containing the token to use for authentication.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    public WorkloadIdentityCredentialBuilder tokenFilePath(String tokenFilePath) {
        this.tokenFilePath = tokenFilePath;
        return this;
    }

    /**
     * Enables Azure Kubernetes Service (AKS) identity bindings. When proxy configuration is present, authentication
     * requests are sent through the AKS token proxy instead of directly to Microsoft Entra ID.
     *
     * <p>This feature is disabled by default and requires using {@link WorkloadIdentityCredential} directly.
     * It is not supported by {@link DefaultAzureCredential} or {@link ManagedIdentityCredential}. The AKS identity
     * binding and Kubernetes permissions must be configured separately. See
     * <a href="https://learn.microsoft.com/azure/aks/identity-bindings-concepts">AKS identity bindings</a>.</p>
     *
     * <p>The AKS webhook supplies {@code AZURE_KUBERNETES_TOKEN_PROXY}, {@code AZURE_KUBERNETES_SNI_NAME},
     * and either {@code AZURE_KUBERNETES_CA_FILE} or {@code AZURE_KUBERNETES_CA_DATA}. Inline CA data must be PEM encoded,
     * not Base64 encoded. If neither CA setting is provided, the platform trust store is used. Changes to a CA file
     * are reloaded on subsequent network requests. Missing or empty files during rotation retain the previously loaded
     * CA; invalid nonempty certificate bundles fail authentication.</p>
     *
     * <p>If no proxy settings are present, the credential uses direct authentication. Incomplete or invalid proxy
     * configuration fails rather than falling back to direct authentication. When a proxy is configured, its dedicated
     * transport is set on a copy of the credential options, replacing any client set with
     * {@link #httpClient(com.azure.core.http.HttpClient)}. A custom HTTP pipeline retains its usual precedence over
     * HTTP client settings.
     * Keep the authority host set to the Microsoft Entra authority, not the proxy URL.</p>
     *
     * <p><strong>Sample: Enable AKS identity bindings</strong></p>
     *
     * <!-- src_embed readme-sample-workloadidentitybinding -->
     * <pre>
     * WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder&#40;&#41;
     *     .enableAzureProxy&#40;&#41;
     *     .build&#40;&#41;;
     * </pre>
     * <!-- end readme-sample-workloadidentitybinding -->
     *
     * @return An updated instance of this builder with Azure proxy enabled.
     */
    public WorkloadIdentityCredentialBuilder enableAzureProxy() {
        this.enableAzureProxy = true;
        return this;
    }

    /**
     * Creates new {@link WorkloadIdentityCredential} with the configured options set.
     *
     * @return a {@link WorkloadIdentityCredential} with the current configurations.
     * @throws IllegalArgumentException if required workload identity settings are missing or proxy configuration is invalid.
     */
    public WorkloadIdentityCredential build() {
        Configuration configuration = identityClientOptions.getConfiguration() == null
            ? Configuration.getGlobalConfiguration().clone()
            : identityClientOptions.getConfiguration();

        String tenantIdInput
            = CoreUtils.isNullOrEmpty(tenantId) ? configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID) : tenantId;

        String federatedTokenFilePathInput
            = CoreUtils.isNullOrEmpty(tokenFilePath) ? configuration.get(AZURE_FEDERATED_TOKEN_FILE) : tokenFilePath;

        String clientIdInput
            = CoreUtils.isNullOrEmpty(clientId) ? configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID) : clientId;

        ValidationUtil.validate(this.getClass().getSimpleName(), LOGGER, "Client ID", clientIdInput, "Tenant ID",
            tenantIdInput, "Service Token File Path", federatedTokenFilePathInput);

        IdentityClientOptions options = identityClientOptions.clone();
        if (enableAzureProxy) {
            ProxyConfig proxyConfig = CustomTokenProxyConfiguration.parseAndValidate(configuration);
            if (proxyConfig != null) {
                options.setHttpClient(new CustomTokenProxyHttpClient(proxyConfig));
            }
        }

        return new WorkloadIdentityCredential(tenantIdInput, clientIdInput, federatedTokenFilePathInput, options);
    }
}

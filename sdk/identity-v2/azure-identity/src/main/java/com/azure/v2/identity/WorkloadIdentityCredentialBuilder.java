// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.implementation.models.ClientOptions;
import com.azure.v2.identity.implementation.models.ConfidentialClientOptions;
import com.azure.v2.identity.implementation.util.ValidationUtil;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.configuration.Configuration;

import static com.azure.v2.identity.implementation.util.IdentityUtil.PROPERTY_AZURE_TENANT_ID;
import static com.azure.v2.identity.implementation.util.IdentityUtil.PROPERTY_AZURE_CLIENT_ID;
import static com.azure.v2.identity.implementation.util.IdentityUtil.AZURE_FEDERATED_TOKEN_FILE;

/**
 * Fluent credential builder for instantiating a {@link WorkloadIdentityCredential}.
 *
 * <p>Workload Identity authentication is a feature in Azure that allows applications running on virtual machines (VMs)
 * to access other Azure resources without the need for a service principal or managed identity. With Workload Identity
 * authentication, applications authenticate themselves using their own identity, rather than using a shared service
 * principal or managed identity. Under the hood, Workload Identity authentication uses the concept of Service Account
 * Credentials (SACs), which are automatically created by Azure and stored securely in the VM. By using Workload
 * Identity authentication, you can avoid the need to manage and rotate service principals or managed identities for
 * each application on each VM. Additionally, because SACs are created automatically and managed by Azure, you don't
 * need to worry about storing and securing sensitive credentials themselves.
 * The WorkloadIdentityCredential supports Azure workload identity authentication on Azure Kubernetes and acquires
 * a token using the service account credentials available in the Azure Kubernetes environment.
 * Refer to <a href="https://learn.microsoft.com/azure/aks/workload-identity-overview">Microsoft Entra Workload ID</a>
 * for more information.</p>
 *
 * <p><strong>Sample: Construct WorkloadIdentityCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link WorkloadIdentityCredential},
 * using the {@link WorkloadIdentityCredentialBuilder} to configure it. The {@code clientId},
 * is required to create {@link WorkloadIdentityCredential}. Once this credential is created, it may be passed into the
 * builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <pre>
 * TokenCredential workloadIdentityCredential = new WorkloadIdentityCredentialBuilder&#40;&#41;.clientId&#40;&quot;&lt;clientID&gt;&quot;&#41;
 *     .tenantId&#40;&quot;&lt;tenantID&gt;&quot;&#41;
 *     .tokenFilePath&#40;&quot;&lt;token-file-path&gt;&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 *
 * @see WorkloadIdentityCredential
 */
public class WorkloadIdentityCredentialBuilder extends EntraIdCredentialBuilderBase<WorkloadIdentityCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(WorkloadIdentityCredentialBuilder.class);
    private String tokenFilePath;

    private final ConfidentialClientOptions confidentialClientOptions;

    /**
     * Creates an instance of a WorkloadIdentityCredentialBuilder.
     */
    public WorkloadIdentityCredentialBuilder() {
        confidentialClientOptions = new ConfidentialClientOptions();
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
     * Creates new {@link WorkloadIdentityCredential} with the configured options set.
     *
     * @return a {@link WorkloadIdentityCredential} with the current configurations.
     */
    public WorkloadIdentityCredential build() {
        Configuration configuration = confidentialClientOptions.getConfiguration() == null
            ? Configuration.getGlobalConfiguration()
            : confidentialClientOptions.getConfiguration();

        String tenantIdInput = CoreUtils.isNullOrEmpty(confidentialClientOptions.getTenantId())
            ? configuration.get(PROPERTY_AZURE_TENANT_ID)
            : confidentialClientOptions.getTenantId();

        String federatedTokenFilePathInput
            = CoreUtils.isNullOrEmpty(tokenFilePath) ? configuration.get(AZURE_FEDERATED_TOKEN_FILE) : tokenFilePath;

        String clientIdInput = CoreUtils.isNullOrEmpty(confidentialClientOptions.getClientId())
            ? configuration.get(PROPERTY_AZURE_CLIENT_ID)
            : confidentialClientOptions.getClientId();

        ValidationUtil.validate(this.getClass().getSimpleName(), LOGGER, "Client ID", clientIdInput, "Tenant ID",
            tenantIdInput, "Service Token File Path", federatedTokenFilePathInput);

        return new WorkloadIdentityCredential(new ConfidentialClientOptions(confidentialClientOptions), tokenFilePath);
    }

    @Override
    ClientOptions getClientOptions() {
        return confidentialClientOptions;
    }
}

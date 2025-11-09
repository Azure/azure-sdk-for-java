// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.util.LoggingUtil;
import com.azure.identity.implementation.util.ValidationUtil;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.azure.identity.ManagedIdentityCredential.AZURE_FEDERATED_TOKEN_FILE;

/**
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
 * <!-- src_embed com.azure.identity.credential.workloadidentitycredential.construct -->
 * <pre>
 * TokenCredential workloadIdentityCredential = new WorkloadIdentityCredentialBuilder&#40;&#41;.clientId&#40;&quot;&lt;clientID&gt;&quot;&#41;
 *     .tenantId&#40;&quot;&lt;tenantID&gt;&quot;&#41;
 *     .tokenFilePath&#40;&quot;&lt;token-file-path&gt;&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.workloadidentitycredential.construct -->
 *
 * @see com.azure.identity
 * @see WorkloadIdentityCredentialBuilder
 */
public class WorkloadIdentityCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(WorkloadIdentityCredential.class);
    private final ClientAssertionCredential clientAssertionCredential;
    private final IdentityClientOptions identityClientOptions;
    private final String clientId;

    /**
     * WorkloadIdentityCredential supports Azure workload identity on Kubernetes.
     *
     * @param tenantId ID of the application's Microsoft Entra tenant. Also called its directory ID.
     * @param clientId The client ID of a Microsoft Entra app registration.
     * @param federatedTokenFilePath The path to a file containing a Kubernetes service account token that authenticates the identity.
     * @param identityClientOptions The identity client options to use for authentication.
     */
    WorkloadIdentityCredential(String tenantId, String clientId, String federatedTokenFilePath,
        IdentityClientOptions identityClientOptions) {
        ValidationUtil.validateTenantIdCharacterRange(tenantId, LOGGER);

        Configuration configuration = identityClientOptions.getConfiguration() == null
            ? Configuration.getGlobalConfiguration().clone()
            : identityClientOptions.getConfiguration();

        String tenantIdInput
            = CoreUtils.isNullOrEmpty(tenantId) ? configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID) : tenantId;

        String federatedTokenFilePathInput = CoreUtils.isNullOrEmpty(federatedTokenFilePath)
            ? configuration.get(AZURE_FEDERATED_TOKEN_FILE)
            : federatedTokenFilePath;

        String clientIdInput
            = CoreUtils.isNullOrEmpty(clientId) ? configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID) : clientId;

        ClientAssertionCredential tempClientAssertionCredential = null;
        String tempClientId = null;

        if (!(CoreUtils.isNullOrEmpty(tenantIdInput)
            || CoreUtils.isNullOrEmpty(federatedTokenFilePathInput)
            || CoreUtils.isNullOrEmpty(clientIdInput)
            || CoreUtils.isNullOrEmpty(identityClientOptions.getAuthorityHost()))) {
            try {
                tempClientAssertionCredential = buildClientAssertionCredential(tenantIdInput, clientIdInput,
                    federatedTokenFilePathInput, identityClientOptions);
                tempClientId = clientIdInput;
            } catch (Exception e) {
                LOGGER.atVerbose().log("Failed to build ClientAssertionCredential during construction", e);
            }
        }

        clientAssertionCredential = tempClientAssertionCredential;
        this.clientId = tempClientId;
        this.identityClientOptions = identityClientOptions;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        if (clientAssertionCredential == null) {
            return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, identityClientOptions,
                new CredentialUnavailableException("WorkloadIdentityCredential"
                    + " authentication unavailable. The workload options are not fully configured. See the troubleshooting"
                    + " guide for more information."
                    + " https://aka.ms/azsdk/java/identity/workloadidentitycredential/troubleshoot")));
        }
        return clientAssertionCredential.getToken(request);
    }

    @Override
    public AccessToken getTokenSync(TokenRequestContext request) {
        if (clientAssertionCredential == null) {
            throw LoggingUtil.logCredentialUnavailableException(LOGGER, identityClientOptions,
                new CredentialUnavailableException("WorkloadIdentityCredential"
                    + " authentication unavailable. The workload options are not fully configured. See the troubleshooting"
                    + " guide for more information."
                    + " https://aka.ms/azsdk/java/identity/workloadidentitycredential/troubleshoot"));
        }
        return clientAssertionCredential.getTokenSync(request);
    }

    String getClientId() {
        return this.clientId;
    }

    /**
     * Builds a ClientAssertionCredential with all applicable configuration options from IdentityClientOptions.
     *
     * @param tenantId The tenant ID for the credential
     * @param clientId The client ID for the credential
     * @param federatedTokenFilePath The path to the federated token file
     * @param identityClientOptions The identity client options containing configuration
     * @return A configured ClientAssertionCredential instance
     */
    private ClientAssertionCredential buildClientAssertionCredential(String tenantId, String clientId,
        String federatedTokenFilePath, IdentityClientOptions identityClientOptions) {

        ClientAssertionCredential credential = new ClientAssertionCredential(clientId, tenantId,
            () -> readFederatedTokenFromFile(federatedTokenFilePath), identityClientOptions);

        return credential;
    }

    /**
     * Reads the federated token from the specified file path.
     * This token will be used as a client assertion for authentication.
     */
    private String readFederatedTokenFromFile(String filePath) {
        if (filePath == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Federated token file path cannot be null"));
        }
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            return new String(bytes, StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Failed to read federated token from file. ", e));
        }
    }
}

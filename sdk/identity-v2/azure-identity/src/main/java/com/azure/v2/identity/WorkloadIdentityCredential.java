// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.exceptions.CredentialAuthenticationException;
import com.azure.v2.identity.exceptions.CredentialUnavailableException;
import com.azure.v2.identity.implementation.client.SynchronousAccessor;
import com.azure.v2.identity.implementation.models.ConfidentialClientOptions;
import com.azure.v2.identity.implementation.util.ValidationUtil;
import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.CoreUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

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
 * <pre>
 * TokenCredential workloadIdentityCredential = new WorkloadIdentityCredentialBuilder&#40;&#41;.clientId&#40;&quot;&lt;clientID&gt;&quot;&#41;
 *     .tenantId&#40;&quot;&lt;tenantID&gt;&quot;&#41;
 *     .tokenFilePath&#40;&quot;&lt;token-file-path&gt;&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 *
 * @see com.azure.v2.identity
 * @see WorkloadIdentityCredentialBuilder
 */
public class WorkloadIdentityCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(WorkloadIdentityCredential.class);
    private final ConfidentialClientOptions confidentialClientOptions;
    private final ClientAssertionCredential clientAssertionCredential;

    /**
     * WorkloadIdentityCredential supports Azure workload identity on Kubernetes.
     *
     * @param federatedTokenFilePath The path to a file containing a Kubernetes service account token that authenticates the identity.
     * @param confidentialClientOptions The confidential client options to use for authentication.
     */
    WorkloadIdentityCredential(ConfidentialClientOptions confidentialClientOptions, String federatedTokenFilePath) {
        this.confidentialClientOptions = confidentialClientOptions;
        ValidationUtil.validateTenantIdCharacterRange(confidentialClientOptions.getTenantId(), LOGGER);
        String tenantIdInput = confidentialClientOptions.getTenantId();

        String federatedTokenFilePathInput = federatedTokenFilePath;

        String clientIdInput = confidentialClientOptions.getClientId();

        if (!(CoreUtils.isNullOrEmpty(tenantIdInput)
            || CoreUtils.isNullOrEmpty(federatedTokenFilePathInput)
            || CoreUtils.isNullOrEmpty(clientIdInput)
            || CoreUtils.isNullOrEmpty(confidentialClientOptions.getAuthorityHost()))) {
            SynchronousAccessor<String> clientAssertionAccessor
                = new SynchronousAccessor<>(() -> parseClientAssertion(federatedTokenFilePath), Duration.ofMinutes(5));
            confidentialClientOptions.setClientAssertionSupplier(() -> clientAssertionAccessor.getValue());
            clientAssertionCredential = new ClientAssertionCredential(confidentialClientOptions);
        } else {
            clientAssertionCredential = null;
        }
    }

    @Override
    public AccessToken getToken(TokenRequestContext request) {
        if (clientAssertionCredential == null) {
            throw LOGGER.throwableAtError()
                .log("WorkloadIdentityCredential"
                    + " authentication unavailable. The workload options are not fully configured. See the troubleshooting"
                    + " guide for more information."
                    + " https://aka.ms/azsdk/java/identity/workloadidentitycredential/troubleshoot",
                    CredentialUnavailableException::new);
        }
        return clientAssertionCredential.getToken(request);
    }

    private String parseClientAssertion(String clientAssertionFilePath) {
        if (clientAssertionFilePath != null) {
            try {
                byte[] encoded = Files.readAllBytes(Paths.get(clientAssertionFilePath));
                return new String(encoded, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw LOGGER.throwableAtError().log(e, CredentialAuthenticationException::new);
            }
        } else {
            throw LOGGER.throwableAtError()
                .log("Client Assertion File Path is not provided."
                    + " It should be provided to authenticate with client assertion.", IllegalStateException::new);
        }
    }
}

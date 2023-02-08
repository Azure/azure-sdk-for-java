// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.util.ValidationUtil;
import reactor.core.publisher.Mono;

/**
 * WorkloadIdentityCredential supports Azure workload identity authentication on Kubernetes.
 * Refer to <a href="https://learn.microsoft.com/azure/aks/workload-identity-overview">Azure Active Directory Workload Identity</a>
 * for more information.
 */
public class WorkloadIdentityCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(WorkloadIdentityCredential.class);
    private final IdentityClient identityClient;

    /**
     * WorkloadIdentityCredential supports Azure workload identity on Kubernetes.
     *
     * @param tenantId ID of the application's Azure Active Directory tenant. Also called its directory ID.
     * @param clientId The client ID of an Azure AD app registration.
     * @param federatedTokenFilePath The path to a file containing a Kubernetes service account token that authenticates the identity.
     * @param identityClientOptions The identity client options to use for authentication.
     */
    WorkloadIdentityCredential(String tenantId, String clientId, String federatedTokenFilePath, IdentityClientOptions identityClientOptions) {
        identityClient = new IdentityClientBuilder()
            .clientAssertionPath(federatedTokenFilePath)
            .clientId(clientId)
            .tenantId(tenantId)
            .identityClientOptions(identityClientOptions)
            .build();
        ValidationUtil.validateTenantIdCharacterRange(tenantId, LOGGER);
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return identityClient.authenticateWithExchangeToken(request);
    }
}


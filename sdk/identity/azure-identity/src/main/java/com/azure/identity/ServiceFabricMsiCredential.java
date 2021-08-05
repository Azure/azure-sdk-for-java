// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClient;
import reactor.core.publisher.Mono;

/**
 * The Managed Service Identity credential for Azure Service Fabric.
 */
@Immutable
class ServiceFabricMsiCredential extends ManagedIdentityServiceCredential {
    private final String identityEndpoint;
    private final String identityHeader;
    private final String identityServerThumbprint;
    private final IdentityClient identityClient;
    private final ClientLogger logger = new ClientLogger(ServiceFabricMsiCredential.class);

    /**
     * Creates an instance of {@link ServiceFabricMsiCredential}.
     *
     * @param clientId The client ID of user assigned or system assigned identity.
     * @param identityClient The identity client to acquire a token with.
     */
    ServiceFabricMsiCredential(String clientId, IdentityClient identityClient) {
        super(clientId, identityClient, "AZURE SERVICE FABRIC IMDS ENDPOINT");
        Configuration configuration = Configuration.getGlobalConfiguration().clone();
        this.identityEndpoint = configuration.get(Configuration.PROPERTY_IDENTITY_ENDPOINT);
        this.identityHeader = configuration.get(Configuration.PROPERTY_IDENTITY_HEADER);
        this.identityServerThumbprint = configuration
                                            .get(ManagedIdentityCredential.PROPERTY_IDENTITY_SERVER_THUMBPRINT);
        this.identityClient = identityClient;
        if (identityEndpoint != null) {
            validateEndpointProtocol(this.identityEndpoint, "Identity", logger);
        }
    }

    /**
     * Gets an access token for a token request.
     *
     * @param request The details of the token request.
     * @return A publisher that emits an {@link AccessToken}.
     */
    public Mono<AccessToken> authenticate(TokenRequestContext request) {
        return identityClient.authenticateToServiceFabricManagedIdentityEndpoint(identityEndpoint, identityHeader,
            identityServerThumbprint, request);
    }
}

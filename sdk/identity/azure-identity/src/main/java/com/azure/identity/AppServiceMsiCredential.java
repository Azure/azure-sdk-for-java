// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.annotation.Immutable;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import com.azure.identity.implementation.IdentityClient;
import reactor.core.publisher.Mono;

/**
 * The Managed Service Identity credential for Azure App Service.
 */
@Immutable
class AppServiceMsiCredential {
    private final String msiEndpoint;
    private final String msiSecret;
    private final IdentityClient identityClient;
    private final String clientId;

    /**
     * Creates an instance of {@link AppServiceMsiCredential}.
     *
     * @param clientId The client ID of user assigned or system assigned identity.
     * @param identityClient The identity client to acquire a token with.
     */
    AppServiceMsiCredential(String clientId, IdentityClient identityClient) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        this.msiEndpoint = configuration.get(Configuration.PROPERTY_MSI_ENDPOINT);
        this.msiSecret = configuration.get(Configuration.PROPERTY_MSI_SECRET);
        this.identityClient = identityClient;
        this.clientId = clientId;
    }

    /**
     * Gets the client ID of the user assigned or system assigned identity.
     *
     * @return The client ID of user assigned or system assigned identity.
     */
    public String getClientId() {
        return this.clientId;
    }

    /**
     * Gets an access token for a token request.
     *
     * @param request The details of the token request.
     * @return A publisher that emits an {@link AccessToken}.
     */
    public Mono<AccessToken> authenticate(TokenRequestContext request) {
        return identityClient.authenticateToManagedIdentityEndpoint(msiEndpoint, msiSecret, request);
    }
}

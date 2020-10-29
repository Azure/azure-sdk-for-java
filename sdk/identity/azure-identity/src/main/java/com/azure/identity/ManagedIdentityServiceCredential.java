package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.implementation.IdentityClient;
import reactor.core.publisher.Mono;

abstract class ManagedIdentityServiceCredential {
    final IdentityClient identityClient;
    final String clientId;
    final String environment;

    ManagedIdentityServiceCredential(String clientId, IdentityClient identityClient, String environment) {
        this.identityClient = identityClient;
        this.clientId = clientId;
        this.environment = environment;
    }

    /**
     * Gets an access token for a token request.
     *
     * @param request The details of the token request.
     * @return A publisher that emits an {@link AccessToken}.
     */
    public abstract Mono<AccessToken> authenticate(TokenRequestContext request);

    /**
     * @return the client ID of user assigned or system assigned identity.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * @return the environment of the Maanged Identity.
     */
    public String getEnvironment() {
        return environment;
    }
}

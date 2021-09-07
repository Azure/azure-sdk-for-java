package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.implementation.IdentityClient;
import reactor.core.publisher.Mono;

public class ClientAssertionCredential extends ManagedIdentityServiceCredential {

    /**
     * Creates an instance of ManagedIdentityServiceCredential.
     *
     * @param clientId the client id of user assigned or system assigned identity.
     * @param identityClient the identity client to acquire a token with.
     */
    ClientAssertionCredential(String clientId, IdentityClient identityClient) {
        super(clientId, identityClient, "AZURE AKS TOKEN EXCHANGE");
    }

    @Override
    public Mono<AccessToken> authenticate(TokenRequestContext request) {
        System.out.printf("Attemtping Auth");
        return identityClient.authenticatewithExchangeToken(request);
    }
}

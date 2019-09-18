// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.core.implementation.annotation.Immutable;
import com.azure.core.util.Configuration;
import com.azure.identity.implementation.IdentityClient;
import reactor.core.publisher.Mono;

/**
 * The Managed Service Identity credential for App Service.
 */
@Immutable
class AppServiceMSICredential {
    private final String msiEndpoint;
    private final String msiSecret;
    private final IdentityClient identityClient;
    private final String clientId;

    /**
     * Creates an instance of AppServiceMSICredential.
     * @param clientId the client id of user assigned or system assigned identity
     * @param identityClient the identity client to acquire a token with.
     */
    AppServiceMSICredential(String clientId, IdentityClient identityClient) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        this.msiEndpoint = configuration.get(Configuration.PROPERTY_MSI_ENDPOINT);
        this.msiSecret = configuration.get(Configuration.PROPERTY_MSI_SECRET);
        this.identityClient = identityClient;
        this.clientId = clientId;
    }

    /**
     * @return the endpoint from which token needs to be retrieved.
     */
    public String getMsiEndpoint() {
        return this.msiEndpoint;
    }
    /**
     * @return the secret to use to retrieve the token.
     */
    public String getMsiSecret() {
        return this.msiSecret;
    }

    /**
     * @return the client id of user assigned or system assigned identity.
     */
    public String getClientId() {
        return this.clientId;
    }

    /**
     * Gets the token for a list of scopes.
     * @param scopes the scopes to get token for
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticate(String[] scopes) {
        return identityClient.authenticateToManagedIdentityEndpoint(msiEndpoint, msiSecret, scopes);
    }
}

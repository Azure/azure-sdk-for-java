// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.annotation.Immutable;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClient;
import reactor.core.publisher.Mono;

/**
 * The Managed Service Identity credential for Azure App Service.
 */
@Immutable
class AppServiceMsiCredential {
    private final String endpoint;
    private final String secret;
    private final String msiVersion;
    private final IdentityClient identityClient;
    private final String clientId;
    private final ClientLogger logger = new ClientLogger(AppServiceMsiCredential.class);

    /**
     * Creates an instance of {@link AppServiceMsiCredential}.
     *
     * @param clientId The client ID of user assigned or system assigned identity.
     * @param identityClient The identity client to acquire a token with.
     */
    AppServiceMsiCredential(String clientId, IdentityClient identityClient) {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();
        if (configuration.contains(ManagedIdentityCredential.PROPERTY_IDENTITY_ENDPOINT)) {
            this.endpoint = configuration.get(ManagedIdentityCredential.PROPERTY_IDENTITY_ENDPOINT);
            this.secret = configuration.get(ManagedIdentityCredential.PROPERTY_IDENTITY_HEADER);
            msiVersion = "2019-08-01";
            if (!(endpoint.startsWith("https") || endpoint.startsWith("http"))) {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("Identity Endpoint should start with 'https' or 'http' scheme."));
            }
        } else {
            this.endpoint = configuration.get(Configuration.PROPERTY_MSI_ENDPOINT);
            this.secret = configuration.get(Configuration.PROPERTY_MSI_SECRET);
            msiVersion = "2017-09-01";
            if (!(endpoint.startsWith("https") || endpoint.startsWith("http"))) {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("MSI Endpoint should start with 'https' or 'http' scheme."));
            }
        }
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
        return identityClient.authenticateToManagedIdentityEndpoint(endpoint, secret, msiVersion, request);
    }
}

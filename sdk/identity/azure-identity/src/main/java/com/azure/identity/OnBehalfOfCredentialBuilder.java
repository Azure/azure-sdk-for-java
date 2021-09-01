// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

public class OnBehalfOfCredentialBuilder extends AadCredentialBuilderBase<OnBehalfOfCredentialBuilder> {
    private String clientSecret;

    /**
     * Sets the client secret for the authentication.
     * @param clientSecret the secret value of the AAD application.
     * @return An updated instance of this builder.
     */
    public OnBehalfOfCredentialBuilder clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    /**
     * Configures the persistent shared token cache options and enables the persistent token cache which is disabled
     * by default. If configured, the credential will store tokens in a cache persisted to the machine, protected to
     * the current user, which can be shared by other credentials and processes.
     *
     * @param tokenCachePersistenceOptions the token cache configuration options
     * @return An updated instance of this builder with the token cache options configured.
     */
    public OnBehalfOfCredentialBuilder tokenCachePersistenceOptions(TokenCachePersistenceOptions
                                                                          tokenCachePersistenceOptions) {
        this.identityClientOptions.setTokenCacheOptions(tokenCachePersistenceOptions);
        return this;
    }

    /**
     * Configure the User Assertion Scope to be used for OnBehalfOf Authentication request.
     *
     * @param userAssertion the user assertion access token to be used for On behalf Of authentication flow
     * @return An updated instance of this builder with the user assertion scope configured.
     */
    public OnBehalfOfCredentialBuilder userAssertion(String userAssertion) {
        this.identityClientOptions.userAssertion(userAssertion);
        return this;
    }

    public OnBehalfOfCredential build() {
        return new OnBehalfOfCredential(clientId, tenantId, clientSecret, identityClientOptions);
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyOptions;
import io.reactivex.Single;

import java.util.concurrent.atomic.AtomicReference;

/**
 * TokenCredentials are a means of authenticating requests to Azure Storage via OAuth user tokens. This is the preferred
 * way of authenticating with Azure Storage.
 */
public final class TokenCredentials implements ICredentials {

    /*
    This is an atomic reference because it must be thread safe as all parts of the pipeline must be. It however cannot
    be final as most factory fields are because in order to actually be useful, the token has to be renewed every few
    hours, which requires updating the value here.
     */
    private AtomicReference<String> token;

    /**
     * Creates a token credential for use with role-based access control (RBAC) access to Azure Storage resources.
     *
     * @param token
     *         A {@code String} of the token to use for authentication.
     */
    public TokenCredentials(String token) {
        this.token = new AtomicReference<>(token);
    }

    /**
     * Retrieve the value of the token used by this factory.
     *
     * @return A {@code String} with the token's value.
     */
    public String getToken() {
        return this.token.get();
    }

    /**
     * Update the token to a new value.
     *
     * @param token
     *         A {@code String} containing the new token's value.
     */
    public void setToken(String token) {
        this.token.set(token);
    }

    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new TokenCredentialsPolicy(this, next);
    }

    private static final class TokenCredentialsPolicy implements RequestPolicy {

        private final TokenCredentials factory;

        private final RequestPolicy nextPolicy;

        private TokenCredentialsPolicy(TokenCredentials factory, RequestPolicy nextPolicy) {
            this.factory = factory;
            this.nextPolicy = nextPolicy;
        }

        public Single<HttpResponse> sendAsync(HttpRequest request) {
            if (!request.url().getProtocol().equals(Constants.HTTPS)) {
                throw new Error("Token credentials require a URL using the https protocol scheme");
            }
            request.withHeader(Constants.HeaderConstants.AUTHORIZATION,
                    "Bearer " + this.factory.getToken());
            return this.nextPolicy.sendAsync(request);
        }
    }
}

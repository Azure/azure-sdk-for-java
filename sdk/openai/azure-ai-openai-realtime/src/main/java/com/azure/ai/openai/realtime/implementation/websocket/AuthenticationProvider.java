// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime.implementation.websocket;

import com.azure.core.credential.KeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpHeaderName;
import reactor.core.publisher.Mono;

public final class AuthenticationProvider {

    private final boolean isAzure;
    private final KeyCredential keyCredential;
    private final TokenCredential tokenCredential;
    private final String[] scopes;

    public AuthenticationProvider(KeyCredential keyCredential, boolean isAzure) {
        this.keyCredential = keyCredential;
        this.isAzure = isAzure;
        this.tokenCredential = null;
        this.scopes = null;
    }

    public AuthenticationProvider(TokenCredential tokenCredential, String[] scopes) {
        this.keyCredential = null;
        this.isAzure = true;
        this.tokenCredential = tokenCredential;
        this.scopes = scopes;
    }

    public Mono<AuthenticationHeader> authenticationToken() {
        if (tokenCredential != null) {
            if (scopes == null) {
                return Mono.error(new IllegalArgumentException("Scopes are required for token based authentication."));
            }
            return tokenCredential.getToken(new TokenRequestContext().addScopes(scopes))
                .map(accessToken -> new AuthenticationHeader(HttpHeaderName.AUTHORIZATION.getCaseInsensitiveName(),
                    "Bearer " + accessToken.getToken()));
        } else if (keyCredential != null && isAzure) {
            return Mono.just(new AuthenticationHeader("api-key", keyCredential.getKey()));
        } else if (keyCredential != null) {
            return Mono.just(new AuthenticationHeader(HttpHeaderName.AUTHORIZATION.getCaseInsensitiveName(),
                "Bearer " + keyCredential.getKey()));
        } else {
            return Mono.error(new IllegalArgumentException("No valid credentials found."));
        }
    }

    public static final class AuthenticationHeader {
        private final String headerName;
        private final String headerValue;

        public AuthenticationHeader(String headerName, String headerValue) {
            this.headerName = headerName;
            this.headerValue = headerValue;
        }

        public String getHeaderName() {
            return headerName;
        }

        public String getHeaderValue() {
            return headerValue;
        }
    }

}

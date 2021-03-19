// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.credential;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import reactor.core.publisher.Mono;
import com.azure.core.credential.AccessToken;


public final class TokenSupplier<T extends TokenCredential, R extends TokenRequestContext> {

    T tokenCredential;
    R tokenRequestContext;

    public TokenSupplier(T tokenCredential)
    {
        this.tokenCredential = tokenCredential;
    }

    public Mono<AccessToken> getToken(R tokenRequestContext)
    {
        return tokenCredential.getToken(tokenRequestContext);
    }
}

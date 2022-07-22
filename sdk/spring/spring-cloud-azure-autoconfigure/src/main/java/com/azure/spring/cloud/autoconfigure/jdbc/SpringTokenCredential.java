// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;

public class SpringTokenCredential implements TokenCredential {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringTokenCredential.class);

    // to one JVM, one system assigned managed identity, many client assigned managed identity
    private TokenCredential tokenCredential;
    private static ApplicationContext context;

    @NotNull
    public SpringTokenCredential(TokenCredential tokenCredential, ApplicationContext context) {
        this.tokenCredential = tokenCredential;
        context = context;
    }

    public TokenCredential getTokenCredential() {
        return tokenCredential;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
        // @todo
        String key = tokenCredential.getClass().getSimpleName() + ":"
            + tokenRequestContext.getScopes() + ":"
            + tokenRequestContext.getTenantId() + ":"
            + tokenRequestContext.getClaims();
        LOGGER.info("key==" + key);
        //
        return null;
    }
}

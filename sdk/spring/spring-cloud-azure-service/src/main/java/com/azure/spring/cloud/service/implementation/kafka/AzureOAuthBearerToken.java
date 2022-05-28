// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.kafka;

import org.apache.kafka.common.security.oauthbearer.OAuthBearerToken;

import java.time.Instant;
import java.util.Set;

/**
 * Implementation of {@link OAuthBearerToken} for Azure Event Hubs.
 */
public class AzureOAuthBearerToken implements OAuthBearerToken {
    private final String token;
    private final Long startTimeMs;
    private final long lifetimeMs;
    private final Set<String> scope;
    private final String principalName;

    public AzureOAuthBearerToken(String token, Long startTimeMs, long lifetimeMs, Set<String> scope,
                                 String principalName) {
        this.token = token;
        this.startTimeMs = startTimeMs;
        this.lifetimeMs = lifetimeMs;
        this.scope = scope;
        this.principalName = principalName;
    }

    @Override
    public String value() {
        return this.token;
    }

    @Override
    public Long startTimeMs() {
        return startTimeMs;
    }

    @Override
    public long lifetimeMs() {
        return this.lifetimeMs;
    }

    @Override
    public Set<String> scope() {
        return scope;
    }

    @Override
    public String principalName() {
        return principalName;
    }

    public boolean isExpired() {
        return Instant.now().toEpochMilli() > lifetimeMs;
    }
}

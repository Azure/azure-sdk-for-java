// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.kafka;

import com.azure.core.credential.AccessToken;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerToken;

import java.text.ParseException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link OAuthBearerToken} for Azure Event Hubs.
 */
public class AzureOAuthBearerToken implements OAuthBearerToken {
    private final String token;
    private final Long startTimeMs;
    private final long lifetimeMs;
    private final Set<String> scope;
    private final String principalName;

    public AzureOAuthBearerToken(AccessToken accessToken) {
        this.token = accessToken.getToken();
        JWTClaimsSet claims;
        try {
            claims = JWTParser.parse(token).getJWTClaimsSet();
        } catch (ParseException exception) {
            throw new RuntimeException("Unable to parse access token", exception);
        }
        startTimeMs = claims.getIssueTime().getTime();
        lifetimeMs = claims.getExpirationTime().getTime();
        // Referring to https://docs.microsoft.com/azure/active-directory/develop/access-tokens#payload-claims, the scp
        // claim is a String which is presented as a space separated list.
        scope = Optional.ofNullable(claims.getClaim("scp"))
                .map(s -> Arrays.stream(((String) s).split(" ")).collect(Collectors.toSet()))
                .orElse(null);
        principalName = (String) claims.getClaim("upn");
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

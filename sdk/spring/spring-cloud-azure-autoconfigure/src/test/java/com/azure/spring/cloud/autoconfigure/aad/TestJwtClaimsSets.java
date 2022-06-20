// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad;

import org.springframework.security.oauth2.jwt.JwtClaimNames;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TestJwtClaimsSets {

    private TestJwtClaimsSets() {

    }

    public static Map<String, Object> jwtClaimsSet() {
        Map<String, Object> jwtClaimsSet = new HashMap<>();
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(Duration.ofSeconds(60L));
        jwtClaimsSet.put(JwtClaimNames.ISS, "https://localhost");
        jwtClaimsSet.put(JwtClaimNames.SUB, "test");
        jwtClaimsSet.put(JwtClaimNames.AUD, Collections.singletonList("client-1"));
        jwtClaimsSet.put(JwtClaimNames.JTI, UUID.randomUUID().toString());
        jwtClaimsSet.put(JwtClaimNames.IAT, issuedAt);
        jwtClaimsSet.put(JwtClaimNames.EXP, expiresAt);
        return jwtClaimsSet;
    }
}

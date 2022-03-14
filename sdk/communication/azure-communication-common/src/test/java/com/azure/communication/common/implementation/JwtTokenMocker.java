// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common.implementation;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;

import java.time.Instant;

public class JwtTokenMocker {

    public String generateRawToken(String resourceId, String userIdentity, Instant expiresAt) {
        String skypeId = generateMockId(resourceId, userIdentity);
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
        builder.claim("skypeid", skypeId);

        long expSeconds = expiresAt.getEpochSecond();
        builder.claim("exp", expSeconds);

        JWTClaimsSet claims =  builder.build();
        JWT idToken = new PlainJWT(claims);
        return idToken.serialize();

    }
    public String generateRawToken(String resourceId, String userIdentity, int validForSeconds) {
        String skypeId = generateMockId(resourceId, userIdentity);
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
        builder.claim("skypeid", skypeId);
        Instant expiresAt = Instant.now().plusSeconds(validForSeconds);
        return generateRawToken(resourceId, userIdentity, expiresAt);
    }

    public String generateMockId(String resourceId, String userIdentity) {
        return "communication:" + resourceId + "." + userIdentity;
    }

}

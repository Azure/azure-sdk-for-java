// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common.implementation;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JwtTokenMockerTest {

    @Test
    public void testGenerateTokenFromInstant() throws ParseException {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        JwtTokenMocker mocker = new JwtTokenMocker();
        String rawToken = mocker.generateRawToken("resId", "userId", now);

        JWT jwt = JWTParser.parse(rawToken);
        Date expirationTime = jwt.getJWTClaimsSet().getExpirationTime();
        assertEquals(now, expirationTime.toInstant());
    }

    @Test
    public void testGenerateTokenValidForSeconds() throws ParseException {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        JwtTokenMocker mocker = new JwtTokenMocker();
        String rawToken = mocker.generateRawToken("resId", "userId", 5);

        JWT jwt = JWTParser.parse(rawToken);
        Date expirationTime = jwt.getJWTClaimsSet().getExpirationTime();

        assertTrue(now.isBefore(expirationTime.toInstant()));
        assertTrue(now.plusSeconds(10).isAfter(expirationTime.toInstant()));
    }

}

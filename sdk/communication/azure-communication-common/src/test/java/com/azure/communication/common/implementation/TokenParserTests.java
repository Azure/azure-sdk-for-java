// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common.implementation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.azure.core.credential.AccessToken;

public class TokenParserTests {
    @Test
    public void testParsingWithoutExpiry() {
        String rawToken =  "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJza3lwZWlkIjoic3Bvb2w6PG15LXJlc291cmNlLWlkPl9hYmNkZWYtMDEyMzQ1Njc4OSJ9.";
        TokenParser parser = new TokenParser();
        assertThrows(IllegalStateException.class, () -> {
            parser.parseJWTToken(rawToken);
        });
    }

    @Test
    public void testParsingRawToken() {
        String rawToken = generateRawToken("AzureResourceId", "contosoUserId", 3 * 60);
        TokenParser parser = new TokenParser();
        AccessToken token = parser.parseJWTToken(rawToken);
        assertFalse(token.isExpired(), "Should not expire if expiry is set to 3 minutes later");

        rawToken = generateRawToken("AzureResourceId", "contosoUserId", -3 * 60);
        token = parser.parseJWTToken(rawToken);
        assertTrue(token.isExpired(), "Should expire if expiry is set to 3 minutes before");
    }

    private String generateRawToken(String resourceId, String userId, int expireInSeconds) {
        JwtTokenMocker mocker = new JwtTokenMocker();
        return mocker.generateRawToken(resourceId, userId, expireInSeconds);
    }
}

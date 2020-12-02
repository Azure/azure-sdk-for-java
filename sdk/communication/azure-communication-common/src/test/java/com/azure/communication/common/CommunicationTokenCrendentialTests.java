// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import com.azure.communication.common.implementation.JwtTokenMocker;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CommunicationTokenCrendentialTests {
    private final JwtTokenMocker tokenMocker = new JwtTokenMocker();

    public TokenRequestContext mockTokenRequestContext() {
        String scope = "the scope value";
        List<String> scopes = new ArrayList<>();
        scopes.add(scope);
        TokenRequestContext tokenRequestContext = new TokenRequestContext().setScopes(scopes);
        return tokenRequestContext;
    }
    @Test
    public void constructWithValidTokenWithoutFresher() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", 3 * 60);
        CommunicationUserCredential userCredential = new CommunicationUserCredential(tokenStr);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(userCredential);

        AccessToken token = tokenCredential.getToken(mockTokenRequestContext()).block();
        assertFalse(token.isExpired(),
                "Statically cached AccessToken should not expire when expiry is set to 3 minutes later");
        assertEquals(tokenStr, token.getToken());
        userCredential.close();
    }

    @Test
    public void constructWithExpiredTokenWithoutRefresher() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", -3 * 60);
        CommunicationUserCredential userCredential = new CommunicationUserCredential(tokenStr);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(userCredential);
        AccessToken token = tokenCredential.getToken(mockTokenRequestContext()).block();
        assertTrue(token.isExpired(),
                "Statically cached AccessToken should expire when expiry is set to 3 minutes before");
        userCredential.close();
    }
}

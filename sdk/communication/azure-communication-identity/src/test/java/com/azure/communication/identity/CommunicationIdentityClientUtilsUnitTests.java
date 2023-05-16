// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import com.azure.communication.identity.implementation.models.CommunicationIdentityAccessTokenRequest;
import com.azure.communication.identity.implementation.models.CommunicationIdentityCreateRequest;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static com.azure.communication.identity.CommunicationIdentityClientUtils.TOKEN_EXPIRATION_OVERFLOW_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;

public class CommunicationIdentityClientUtilsUnitTests {

    private final ClientLogger logger = new ClientLogger(CommunicationIdentityClientUtils.class);

    @Test
    public void createCommunicationIdentityCreateRequestWithCustomTokenValidity() {
        // Arrange
        int tokenExpirationInMinutes = 60;
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        Duration tokenExpiresIn = Duration.ofMinutes(tokenExpirationInMinutes);

        // Action
        CommunicationIdentityCreateRequest request = CommunicationIdentityClientUtils.createCommunicationIdentityCreateRequest(scopes, tokenExpiresIn, logger);

        // Assert
        assertNotNull(request.getExpiresInMinutes());
        assertEquals(tokenExpirationInMinutes, request.getExpiresInMinutes());
        assertEquals(scopes, request.getCreateTokenWithScopes());
    }

    @Test
    public void createCommunicationIdentityCreateRequestWithoutCustomTokenValidity() {
        // Arrange
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action
        CommunicationIdentityCreateRequest request = CommunicationIdentityClientUtils.createCommunicationIdentityCreateRequest(scopes, null, logger);

        // Assert
        assertNull(request.getExpiresInMinutes());
        assertEquals(scopes, request.getCreateTokenWithScopes());
    }

    @Test
    public void createCommunicationIdentityAccessTokenRequestWithCustomTokenValidity() {
        // Arrange
        int tokenExpirationInMinutes = 60;
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        Duration tokenExpiresIn = Duration.ofMinutes(tokenExpirationInMinutes);

        // Action
        CommunicationIdentityAccessTokenRequest request = CommunicationIdentityClientUtils.createCommunicationIdentityAccessTokenRequest(scopes, tokenExpiresIn, logger);

        // Assert
        assertNotNull(request.getExpiresInMinutes());
        assertEquals(tokenExpirationInMinutes, request.getExpiresInMinutes());
        assertEquals(scopes, request.getScopes());
    }

    @Test
    public void createCommunicationIdentityAccessTokenRequestWithoutCustomTokenValidity() {
        // Arrange
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action
        CommunicationIdentityAccessTokenRequest request = CommunicationIdentityClientUtils.createCommunicationIdentityAccessTokenRequest(scopes, null, logger);

        // Assert
        assertNull(request.getExpiresInMinutes());
        assertEquals(scopes, request.getScopes());
    }

    @Test()
    public void createCommunicationIdentityCreateRequestWithCustomTokenValidityException() {
        // Arrange
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        Duration tokenExpiresIn = Duration.ofHours(Integer.MAX_VALUE);

        // Action
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            CommunicationIdentityClientUtils.createCommunicationIdentityCreateRequest(scopes, tokenExpiresIn, logger));

        // Assert
        assertEquals(TOKEN_EXPIRATION_OVERFLOW_MESSAGE, exception.getMessage());
    }
}

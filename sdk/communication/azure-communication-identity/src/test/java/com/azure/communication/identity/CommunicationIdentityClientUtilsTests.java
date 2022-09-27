// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import com.azure.communication.identity.implementation.models.CommunicationIdentityAccessTokenRequest;
import com.azure.communication.identity.implementation.models.CommunicationIdentityCreateRequest;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CommunicationIdentityClientUtilsTests {

    private final ClientLogger logger = new ClientLogger(CommunicationIdentityClient.class);

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
    }

    @Test
    public void createCommunicationIdentityCreateRequestWithoutCustomTokenValidity() {
        // Arrange
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action
        CommunicationIdentityCreateRequest request = CommunicationIdentityClientUtils.createCommunicationIdentityCreateRequest(scopes, null, logger);

        // Assert
        assertNull(request.getExpiresInMinutes());
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
    }

    @Test
    public void createCommunicationIdentityAccessTokenRequestWithoutCustomTokenValidity() {
        // Arrange
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action
        CommunicationIdentityAccessTokenRequest request = CommunicationIdentityClientUtils.createCommunicationIdentityAccessTokenRequest(scopes, null, logger);

        // Assert
        assertNull(request.getExpiresInMinutes());
    }

    @Test
    public void defaultTokenWithinAllowedDeviation() {
        // Arrange
        OffsetDateTime actualTokenExpiration = OffsetDateTime.now(Clock.systemUTC()).plusHours(24);

        // Action
        boolean withinAllowedDeviation = CommunicationIdentityClientUtils.tokenExpirationWithinAllowedDeviation(null, actualTokenExpiration).getIsWithinAllowedDeviation();

        // Assert
        assertTrue(withinAllowedDeviation);
    }

    @Test
    public void customTokenWithinAllowedDeviation() {
        // Arrange
        Duration expectedTokenExpiration = Duration.ofHours(1);
        OffsetDateTime actualTokenExpiration = OffsetDateTime.now(Clock.systemUTC()).plusHours(1);

        // Action
        boolean withinAllowedDeviation = CommunicationIdentityClientUtils.tokenExpirationWithinAllowedDeviation(expectedTokenExpiration, actualTokenExpiration).getIsWithinAllowedDeviation();

        // Assert
        assertTrue(withinAllowedDeviation);
    }

    @Test
    public void customTokenOutsideOfAllowedDeviation() {
        // Arrange
        Duration expectedTokenExpiration = Duration.ofHours(1);
        OffsetDateTime actualTokenExpiration = OffsetDateTime.now(Clock.systemUTC()).plusMinutes(55);

        // Action
        boolean withinAllowedDeviation = CommunicationIdentityClientUtils.tokenExpirationWithinAllowedDeviation(expectedTokenExpiration, actualTokenExpiration).getIsWithinAllowedDeviation();

        // Assert
        assertFalse(withinAllowedDeviation);
    }
}

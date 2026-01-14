// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.endpoint.JwtBearerGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithms;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.MultiValueMap;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AadJwtBearerGrantRequestParametersConverterTests {

    @Test
    void requestedTokenUseParameter() {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("test")
                                                                  .clientId("test")
                                                                  .clientSecret("test-secret")
                                                                  .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
                                                                  .tokenUri("http://localhost/token")
                                                                  .build();
        Jwt jwt = Jwt.withTokenValue("jwt-token-value")
                     .header("alg", JwsAlgorithms.RS256)
                     .claim("sub", "test")
                     .issuedAt(Instant.ofEpochMilli(Instant.now().toEpochMilli()))
                     .expiresAt(Instant.ofEpochMilli(Instant.now().plusSeconds(60).toEpochMilli()))
                     .build();
        JwtBearerGrantRequest request = new JwtBearerGrantRequest(clientRegistration, jwt);
        AadJwtBearerGrantRequestParametersConverter converter =
            new AadJwtBearerGrantRequestParametersConverter();
        MultiValueMap<String, String> parameters = converter.convert(request);
        Assertions.assertNotNull(parameters);
        assertTrue(parameters.containsKey("requested_token_use"));
        assertEquals("on_behalf_of", parameters.getFirst("requested_token_use"));
        // Verify that the converter does not add grant_type or other standard OAuth2 parameters
        // to avoid duplication when composed with DefaultOAuth2TokenRequestParametersConverter
        Assertions.assertFalse(parameters.containsKey("grant_type"), 
            "Converter should not add grant_type to avoid duplication");
        assertEquals(1, parameters.size(), 
            "Converter should only add the Azure-specific parameter");
    }
}

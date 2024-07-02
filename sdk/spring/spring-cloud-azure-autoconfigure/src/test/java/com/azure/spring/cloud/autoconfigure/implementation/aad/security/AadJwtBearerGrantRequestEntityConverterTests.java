// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.JwtBearerGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithms;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.MultiValueMap;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AadJwtBearerGrantRequestEntityConverterTests {

    @SuppressWarnings("unchecked")
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
        AadJwtBearerGrantRequestEntityConverter converter =
            new AadJwtBearerGrantRequestEntityConverter();
        RequestEntity<MultiValueMap<String, String>> entity =
            (RequestEntity<MultiValueMap<String, String>>) converter.convert(request);
        MultiValueMap<String, String> parameters = entity.getBody();
        assertTrue(parameters.containsKey("requested_token_use"));
        assertEquals("on_behalf_of", parameters.getFirst("requested_token_use"));
    }
}

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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @SuppressWarnings("unchecked")
    @Test
    void noDuplicateGrantTypeParameterWithAdditionalConverter() {
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
        
        // Add a parameters converter that might set the grant_type again
        // This simulates the scenario where multiple converters provide the same parameter
        converter.addParametersConverter(grantRequest -> {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
            params.add("custom_param", "custom_value");
            return params;
        });
        
        RequestEntity<MultiValueMap<String, String>> entity =
            (RequestEntity<MultiValueMap<String, String>>) converter.convert(request);
        MultiValueMap<String, String> parameters = entity.getBody();
        
        // Verify that grant_type is present
        assertTrue(parameters.containsKey("grant_type"), "grant_type parameter should be present");
        
        // Verify that grant_type has only one value (not duplicated)
        List<String> grantTypeValues = parameters.get("grant_type");
        assertNotNull(grantTypeValues, "grant_type values should not be null");
        assertEquals(1, grantTypeValues.size(), 
            "grant_type should have exactly one value, not a list with duplicates");
        assertEquals("urn:ietf:params:oauth:grant-type:jwt-bearer", grantTypeValues.get(0),
            "grant_type should have the correct value");
        
        // Verify custom parameter is present
        assertTrue(parameters.containsKey("custom_param"), "custom_param should be present");
        assertEquals("custom_value", parameters.getFirst("custom_param"));
        
        // Verify requested_token_use is present
        assertTrue(parameters.containsKey("requested_token_use"), 
            "requested_token_use parameter should be present");
        assertEquals("on_behalf_of", parameters.getFirst("requested_token_use"));
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.JwtBearerGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithms;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void grantTypeIsNotDuplicatedWhenParametersConverterIsAdded() {
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
        
        // Create converter and add a parameters converter that returns additional parameters
        AadJwtBearerGrantRequestEntityConverter converter = new AadJwtBearerGrantRequestEntityConverter();
        converter.addParametersConverter((grantRequest) -> {
            MultiValueMap<String, String> additionalParams = new LinkedMultiValueMap<>();
            additionalParams.set("custom_param", "custom_value");
            return additionalParams;
        });
        
        RequestEntity<MultiValueMap<String, String>> entity =
            (RequestEntity<MultiValueMap<String, String>>) converter.convert(request);
        MultiValueMap<String, String> parameters = entity.getBody();
        
        // Verify that grant_type exists
        assertTrue(parameters.containsKey(OAuth2ParameterNames.GRANT_TYPE));
        
        // Verify that grant_type is a single value, not a list
        List<String> grantTypeValues = parameters.get(OAuth2ParameterNames.GRANT_TYPE);
        assertNotNull(grantTypeValues);
        assertEquals(1, grantTypeValues.size(), 
            "Grant type should be a single value, not duplicated: " + grantTypeValues);
        assertEquals("urn:ietf:params:oauth:grant-type:jwt-bearer", grantTypeValues.get(0));
        
        // Verify the custom parameter was added
        assertTrue(parameters.containsKey("custom_param"));
        assertEquals("custom_value", parameters.getFirst("custom_param"));
        
        // Verify requested_token_use is present
        assertTrue(parameters.containsKey("requested_token_use"));
        assertEquals("on_behalf_of", parameters.getFirst("requested_token_use"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void allRequiredParametersArePresent() {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("test")
                                                                  .clientId("test")
                                                                  .clientSecret("test-secret")
                                                                  .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
                                                                  .tokenUri("http://localhost/token")
                                                                  .scope("openid", "profile")
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
        
        // Verify all required parameters
        assertTrue(parameters.containsKey(OAuth2ParameterNames.GRANT_TYPE));
        assertEquals("urn:ietf:params:oauth:grant-type:jwt-bearer", 
                     parameters.getFirst(OAuth2ParameterNames.GRANT_TYPE));
        
        assertTrue(parameters.containsKey(OAuth2ParameterNames.ASSERTION));
        assertEquals("jwt-token-value", parameters.getFirst(OAuth2ParameterNames.ASSERTION));
        
        assertTrue(parameters.containsKey(OAuth2ParameterNames.SCOPE));
        assertEquals("openid profile", parameters.getFirst(OAuth2ParameterNames.SCOPE));
        
        assertTrue(parameters.containsKey(OAuth2ParameterNames.CLIENT_ID));
        assertEquals("test", parameters.getFirst(OAuth2ParameterNames.CLIENT_ID));
        
        assertTrue(parameters.containsKey(OAuth2ParameterNames.CLIENT_SECRET));
        assertEquals("test-secret", parameters.getFirst(OAuth2ParameterNames.CLIENT_SECRET));
        
        assertTrue(parameters.containsKey("requested_token_use"));
        assertEquals("on_behalf_of", parameters.getFirst("requested_token_use"));
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.security.oauth2.client.endpoint;

import org.junit.jupiter.api.Test;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.azure.spring.security.oauth2.client.utils.ClientRegistrations.CLIENT_1_SCOPES;
import static com.azure.spring.security.oauth2.client.utils.ClientRegistrations.CLIENT_1_SCOPES_STRING;
import static com.azure.spring.security.oauth2.client.utils.ClientRegistrations.CLIENT_2_SCOPES_STRING;
import static com.azure.spring.security.oauth2.client.utils.ClientRegistrations.CLIENT_REGISTRATION_1;
import static com.azure.spring.security.oauth2.client.utils.ClientRegistrations.CLIENT_REGISTRATION_2;
import static com.azure.spring.security.oauth2.client.utils.ClientRegistrations.CLIENT_REGISTRATION_ID_1;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class AddScopeOAuth2AuthorizationCodeGrantRequestEntityConverterTest {

    AddScopeOAuth2AuthorizationCodeGrantRequestEntityConverter CONVERTER =
        new AddScopeOAuth2AuthorizationCodeGrantRequestEntityConverter(CLIENT_REGISTRATION_ID_1,
            CLIENT_1_SCOPES);

    Object[] EXPECTED_HEADERS = CONVERTER.createHttpHeaders()
                                         .entrySet()
                                         .stream()
                                         .filter(entry -> !entry.getKey().equals("client-request-id"))
                                         .toArray();

    @Test
    public void addScopeForTargetClientTest() {
        OAuth2AuthorizationCodeGrantRequest request = createCodeGrantRequest(CLIENT_REGISTRATION_1);
        RequestEntity<?> requestEntity = CONVERTER.convert(request);
        assertNotNull(requestEntity);
        String scopes = toMultiValueMap(requestEntity).getFirst("scope");
        assertEquals(CLIENT_1_SCOPES_STRING, scopes);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addHeaderForTargetClientTest() {
        OAuth2AuthorizationCodeGrantRequest request = createCodeGrantRequest(CLIENT_REGISTRATION_1);
        RequestEntity<?> requestEntity = CONVERTER.convert(request);
        assertNotNull(requestEntity);
        Set<Map.Entry<String, List<String>>> entrySet = requestEntity.getHeaders().entrySet();
        for (Object expected_header : EXPECTED_HEADERS) {
            assertTrue(entrySet.contains((Map.Entry<String, List<String>>)expected_header));
        }
    }

    @Test
    public void addScopeForNonTargetClientTest() {
        OAuth2AuthorizationCodeGrantRequest request = createCodeGrantRequest(CLIENT_REGISTRATION_2);
        RequestEntity<?> requestEntity = CONVERTER.convert(request);
        assertNotNull(requestEntity);
        String scopes = toMultiValueMap(requestEntity).getFirst("scope");
        assertEquals(CLIENT_2_SCOPES_STRING, scopes);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addHeaderForNonTargetClientTest() {
        OAuth2AuthorizationCodeGrantRequest request = createCodeGrantRequest(CLIENT_REGISTRATION_2);
        RequestEntity<?> requestEntity = CONVERTER.convert(request);
        assertNotNull(requestEntity);
        Set<Map.Entry<String, List<String>>> entrySet = requestEntity.getHeaders().entrySet();
        for (Object expected_header : EXPECTED_HEADERS) {
            assertTrue(entrySet.contains((Map.Entry<String, List<String>>)expected_header));
        }
    }

    private OAuth2AuthorizationCodeGrantRequest createCodeGrantRequest(ClientRegistration client) {
        return new OAuth2AuthorizationCodeGrantRequest(client, createExchange(client));
    }

    private OAuth2AuthorizationExchange createExchange(ClientRegistration client) {
        return new OAuth2AuthorizationExchange(
            createAuthorizationRequest(client),
            createAuthorizationResponse());
    }

    private OAuth2AuthorizationRequest createAuthorizationRequest(ClientRegistration client) {
        OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest.authorizationCode();
        builder.authorizationUri(client.getProviderDetails().getAuthorizationUri());
        builder.clientId(client.getClientId());
        builder.scopes(client.getScopes());
        builder.state("fake-state");
        builder.redirectUri("http://localhost");
        return builder.build();
    }

    private OAuth2AuthorizationResponse createAuthorizationResponse() {
        OAuth2AuthorizationResponse.Builder builder = OAuth2AuthorizationResponse.success("fake-code");
        builder.redirectUri("http://localhost");
        builder.state("fake-state");
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private MultiValueMap<String, String> toMultiValueMap(RequestEntity<?> entity) {
        return (MultiValueMap<String, String>) entity.getBody();
    }

}

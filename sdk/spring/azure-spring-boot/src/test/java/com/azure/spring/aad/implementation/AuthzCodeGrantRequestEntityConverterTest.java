// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.implementation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AuthzCodeGrantRequestEntityConverterTest {

    private AzureClientRegistrationRepository clientRepo;
    private ClientRegistration azure;
    private ClientRegistration graph;

    @BeforeEach
    public void setupApp() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
            context,
            "azure.activedirectory.authorization-server-uri = fake-uri",
            "azure.activedirectory.authorization.graph.scopes = Calendars.Read",
            "azure.activedirectory.client-id = fake-client-id",
            "azure.activedirectory.client-secret = fake-client-secret",
            "azure.activedirectory.tenant-id = fake-tenant-id",
            "azure.activedirectory.user-group.allowed-groups = group1, group2"
        );
        context.register(AzureActiveDirectoryConfiguration.class);
        context.refresh();

        clientRepo = context.getBean(AzureClientRegistrationRepository.class);
        azure = clientRepo.findByRegistrationId("azure");
        graph = clientRepo.findByRegistrationId("graph");
    }

    @Test
    public void addScopeForDefaultClient() {
        MultiValueMap<String, String> body = convertedBodyOf(createCodeGrantRequest(azure));
        assertEquals(
            "openid profile offline_access"
                + " https://graph.microsoft.com/User.Read https://graph.microsoft.com/Directory.AccessAsUser.All",
            body.getFirst("scope")
        );
    }

    @Test
    public void noScopeParamForOtherClient() {
        MultiValueMap<String, String> body = convertedBodyOf(createCodeGrantRequest(graph));
        assertNull(body.get("scope"));
    }

    @SuppressWarnings("unchecked")
    private MultiValueMap<String, String> convertedBodyOf(OAuth2AuthorizationCodeGrantRequest request) {
        AuthzCodeGrantRequestEntityConverter converter =
            new AuthzCodeGrantRequestEntityConverter(clientRepo.getAzureClient());
        RequestEntity<?> entity = converter.convert(request);
        return (MultiValueMap<String, String>) Optional.ofNullable(entity)
                                                       .map(HttpEntity::getBody)
                                                       .orElse(null);
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
}

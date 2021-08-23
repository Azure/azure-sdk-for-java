// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.azure.spring.aad.AADClientRegistrationRepository;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

import static com.azure.spring.aad.AADClientRegistrationRepository.AZURE_CLIENT_REGISTRATION_ID;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AADOAuth2AuthorizationCodeGrantRequestEntityConverterTest {

    private WebApplicationContextRunner getContextRunner() {
        return WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.base-uri = fake-uri",
                "azure.activedirectory.authorization-clients.graph.scopes = Graph.Scope",
                "azure.activedirectory.authorization-clients.arm.scopes = Arm.Scope",
                "azure.activedirectory.authorization-clients.arm.on-demand = true");
    }

    @Test
    public void addScopeForAzureClient() {
        getContextRunner().run(context -> {
            AADClientRegistrationRepository repository =
                (AADClientRegistrationRepository) context.getBean(ClientRegistrationRepository.class);
            ClientRegistration azure = repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID);
            MultiValueMap<String, String> body = convertedBodyOf(repository, createCodeGrantRequest(azure));
            assertEquals("openid profile offline_access", body.getFirst("scope"));
        });
    }

    @Test
    public void addScopeForOnDemandClient() {
        getContextRunner().run(context -> {
            AADClientRegistrationRepository repository =
                (AADClientRegistrationRepository) context.getBean(ClientRegistrationRepository.class);
            ClientRegistration arm = repository.findByRegistrationId("arm");
            MultiValueMap<String, String> body = convertedBodyOf(repository, createCodeGrantRequest(arm));
            assertEquals("Arm.Scope openid profile offline_access", body.getFirst("scope"));
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addHeadersForAzureClient() {
        getContextRunner().run(context -> {
            AADClientRegistrationRepository repository =
                (AADClientRegistrationRepository) context.getBean(ClientRegistrationRepository.class);
            ClientRegistration azure = repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID);
            HttpHeaders httpHeaders = convertedHeaderOf(repository, createCodeGrantRequest(azure));
            assertThat(httpHeaders.entrySet(), (Matcher) hasItems(expectedHeaders(repository)));
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addHeadersForOnDemandClient() {
        getContextRunner().run(context -> {
            AADClientRegistrationRepository repository =
                (AADClientRegistrationRepository) context.getBean(ClientRegistrationRepository.class);
            ClientRegistration arm = repository.findByRegistrationId("arm");
            HttpHeaders httpHeaders = convertedHeaderOf(repository, createCodeGrantRequest(arm));
            assertThat(httpHeaders.entrySet(), (Matcher) hasItems(expectedHeaders(repository)));
        });
    }

    private HttpHeaders convertedHeaderOf(AADClientRegistrationRepository repository,
                                          OAuth2AuthorizationCodeGrantRequest request) {
        AADOAuth2AuthorizationCodeGrantRequestEntityConverter converter =
            new AADOAuth2AuthorizationCodeGrantRequestEntityConverter(repository.getAzureClientAccessTokenScopes());
        RequestEntity<?> entity = converter.convert(request);
        return Optional.ofNullable(entity)
                       .map(HttpEntity::getHeaders)
                       .orElse(null);
    }

    private Object[] expectedHeaders(AADClientRegistrationRepository repository) {
        return new AADOAuth2AuthorizationCodeGrantRequestEntityConverter(repository.getAzureClientAccessTokenScopes())
            .getHttpHeaders()
            .entrySet()
            .stream()
            .filter(entry -> !entry.getKey().equals("client-request-id"))
            .toArray();
    }

    private MultiValueMap<String, String> convertedBodyOf(AADClientRegistrationRepository repository,
                                                          OAuth2AuthorizationCodeGrantRequest request) {
        AADOAuth2AuthorizationCodeGrantRequestEntityConverter converter =
            new AADOAuth2AuthorizationCodeGrantRequestEntityConverter(repository.getAzureClientAccessTokenScopes());
        RequestEntity<?> entity = converter.convert(request);
        return WebApplicationContextRunnerUtils.toMultiValueMap(entity);
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

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.assertj.AssertableWebApplicationContext;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.util.MultiValueMap;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AzureOauth2RefreshTokenGrantRequestEntityConverterTest {

    private AADWebAppClientRegistrationRepository clientRepo;
    private ClientRegistration azure;
    private ClientRegistration graph;

    private final WebApplicationContextRunner contextRunner = PropertiesUtils
        .CONTEXT_RUNNER.withPropertyValues(
            "azure.activedirectory.authorization-server-uri = fake-uri",
            "azure.activedirectory.authorization.graph.scopes = Calendars.Read");

    private void getBeans(AssertableWebApplicationContext context) {
        clientRepo = context.getBean(AADWebAppClientRegistrationRepository.class);
        azure = clientRepo.findByRegistrationId("azure");
        graph = clientRepo.findByRegistrationId("graph");
    }

    @Test
    public void addScopeForOtherClient() {
        contextRunner
            .run(context -> {
                getBeans(context);
                MultiValueMap<String, String> body = convertedBodyOf(createOAuth2RefreshTokenGrantRequest(graph));
                assertEquals(
                    "Calendars.Read",
                    body.getFirst("scope")
                );
            });
    }

    @Test
    public void noScopeParamForDefaultClient() {
        contextRunner
            .run(context -> {
                getBeans(context);
                MultiValueMap<String, String> body = convertedBodyOf(createOAuth2RefreshTokenGrantRequest(azure));
                assertNull(body.get("scope"));
            });
    }

    @SuppressWarnings("unchecked")
    private MultiValueMap<String, String> convertedBodyOf(OAuth2RefreshTokenGrantRequest request) {
        AzureOauth2RefreshTokenGrantRequestEntityConverter converter =
            new AzureOauth2RefreshTokenGrantRequestEntityConverter();
        RequestEntity<?> entity = converter.convert(request);
        return PropertiesUtils.requestEntityConverter(entity);
    }

    private OAuth2RefreshTokenGrantRequest createOAuth2RefreshTokenGrantRequest(ClientRegistration client) {
        return new OAuth2RefreshTokenGrantRequest(client, new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "non-access-token",
            Instant.MIN,
            Instant.now().minus(100, ChronoUnit.DAYS)),
            new OAuth2RefreshToken("fake-value", Instant.MIN));
    }

}

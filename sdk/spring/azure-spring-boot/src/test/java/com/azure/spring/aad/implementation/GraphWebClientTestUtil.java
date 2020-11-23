// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.implementation;

import com.azure.spring.autoconfigure.aad.TestConstants;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class GraphWebClientTestUtil {

    public static WebClient createWebClientForTest() {
        ClientRegistration clientRegistration =
            ClientRegistration.withRegistrationId("graph")
                              .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                              .redirectUriTemplate("{baseUrl}/login/oauth2/code/{registrationId}")
                              .clientId("test")
                              .clientSecret("test")
                              .authorizationUri("test")
                              .tokenUri("test")
                              .jwkSetUri("test")
                              .build();
        OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager = authorizeRequest -> new OAuth2AuthorizedClient(
            clientRegistration,
            "principalName",
            new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                TestConstants.ACCESS_TOKEN,
                Instant.now().minus(10, ChronoUnit.MINUTES),
                Instant.now().plus(10, ChronoUnit.MINUTES)
            )
        );
        ServletOAuth2AuthorizedClientExchangeFilterFunction servletOAuth2AuthorizedClientExchangeFilterFunction =
            new ServletOAuth2AuthorizedClientExchangeFilterFunction(oAuth2AuthorizedClientManager);
        return WebClient.builder()
                        .apply(servletOAuth2AuthorizedClientExchangeFilterFunction.oauth2Configuration())
                        .build();
    }
}

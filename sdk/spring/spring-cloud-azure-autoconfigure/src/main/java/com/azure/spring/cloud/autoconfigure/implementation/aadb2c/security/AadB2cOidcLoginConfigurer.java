// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aadb2c.security;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import static com.azure.spring.cloud.autoconfigure.implementation.aad.utils.AadRestTemplateCreator.createOAuth2AccessTokenResponseClientRestTemplate;

/**
 * Configure B2C OAUTH2 login properties.
 *
 * @see AbstractHttpConfigurer
 */
public class AadB2cOidcLoginConfigurer extends AbstractHttpConfigurer<AadB2cOidcLoginConfigurer, HttpSecurity> {

    private final LogoutSuccessHandler handler;

    private final OAuth2AuthorizationRequestResolver resolver;

    private final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient;

    private final RestTemplateBuilder restTemplateBuilder;

    /**
     * Creates a new instance of {@link AadB2cOidcLoginConfigurer}.
     *
     * @param handler the AAD B2C logout success handler
     * @param resolver the AAD B2C authorization request resolver
     */
    public AadB2cOidcLoginConfigurer(AadB2cLogoutSuccessHandler handler, AadB2cAuthorizationRequestResolver resolver) {
        this(handler, resolver, null, null);
    }

    /**
     * Creates a new instance of {@link AadB2cOidcLoginConfigurer}.
     *
     * @param handler the AAD B2C logout success handler
     * @param resolver the AAD B2C authorization request resolver
     * @param accessTokenResponseClient the AAD B2C access token response client
     */
    public AadB2cOidcLoginConfigurer(LogoutSuccessHandler handler,
                                     OAuth2AuthorizationRequestResolver resolver,
                                     OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient) {
        this(handler, resolver, accessTokenResponseClient, null);
    }

    /**
     * Creates a new instance of {@link AadB2cOidcLoginConfigurer}.
     *
     * @param handler the AAD B2C logout success handler
     * @param resolver the AAD B2C authorization request resolver
     * @param accessTokenResponseClient the AAD B2C access token response client
     * @param restTemplateBuilder the RestTemplateBuilder used to build OAuth2AccessTokenResponseClient.
     *                           It will be used only when accessTokenResponseClient is null.
     */
    public AadB2cOidcLoginConfigurer(LogoutSuccessHandler handler,
                                     OAuth2AuthorizationRequestResolver resolver,
                                     OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient,
                                     RestTemplateBuilder restTemplateBuilder) {
        this.handler = handler;
        this.resolver = resolver;
        this.accessTokenResponseClient = accessTokenResponseClient;
        this.restTemplateBuilder = restTemplateBuilder;
    }

    /**
     * Initialize the SecurityBuilder.
     *
     * @param http the http
     * @throws Exception failed to initialize SecurityBuilder
     */
    @Override
    public void init(HttpSecurity http) throws Exception {
        // @formatter:off
        http.logout(logout -> logout.logoutSuccessHandler(handler))
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .authorizationRequestResolver(resolver)).tokenEndpoint(token -> token
                    .accessTokenResponseClient(accessTokenResponseClient())));
        // @formatter:on
    }

    /**
     * Gets the access token response client.
     *
     * @return the access token response client
     */
    @SuppressWarnings({"deprecation", "removal"})
    protected OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        if (accessTokenResponseClient != null) {
            return accessTokenResponseClient;
        }
        DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();
        client.setRequestEntityConverter(new AadB2cOAuth2AuthorizationCodeGrantRequestEntityConverter());
        client.setRestOperations(createOAuth2AccessTokenResponseClientRestTemplate(restTemplateBuilder));
        return client;
    }
}

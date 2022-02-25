// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c;

import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AadB2cOAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;

/**
 * Configure B2C OAUTH2 login properties.
 *
 * @see AbstractHttpConfigurer
 */
public class AadB2cOidcLoginConfigurer extends AbstractHttpConfigurer<AadB2cOidcLoginConfigurer, HttpSecurity> {

    private final AadB2cLogoutSuccessHandler handler;

    private final AadB2cAuthorizationRequestResolver resolver;

    /**
     * Creates a new instance of {@link AadB2cOidcLoginConfigurer}.
     *
     * @param handler the AAD B2C logout success handler
     * @param resolver the AAD B2C authorization request resolver
     */
    public AadB2cOidcLoginConfigurer(AadB2cLogoutSuccessHandler handler, AadB2cAuthorizationRequestResolver resolver) {
        this.handler = handler;
        this.resolver = resolver;
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
        http.logout()
                .logoutSuccessHandler(handler)
                .and()
            .oauth2Login()
                .authorizationEndpoint()
                    .authorizationRequestResolver(resolver)
                    .and()
                .tokenEndpoint()
                    .accessTokenResponseClient(accessTokenResponseClient());
        // @formatter:on
    }

    /**
     * Gets the access token response client.
     *
     * @return the access token response client
     */
    protected OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient result = new DefaultAuthorizationCodeTokenResponseClient();
        result.setRequestEntityConverter(new AadB2cOAuth2AuthorizationCodeGrantRequestEntityConverter());
        return result;
    }
}

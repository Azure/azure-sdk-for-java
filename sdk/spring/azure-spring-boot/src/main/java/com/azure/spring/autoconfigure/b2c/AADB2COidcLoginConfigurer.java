// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;

/**
 * Configure B2C OAUTH2 login properties.
 */
@Deprecated
public class AADB2COidcLoginConfigurer extends AbstractHttpConfigurer<AADB2COidcLoginConfigurer, HttpSecurity> {

    private final AADB2CLogoutSuccessHandler handler;

    private final AADB2CAuthorizationRequestResolver resolver;

    public AADB2COidcLoginConfigurer(AADB2CLogoutSuccessHandler handler, AADB2CAuthorizationRequestResolver resolver) {
        this.handler = handler;
        this.resolver = resolver;
    }

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

    protected OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient result = new DefaultAuthorizationCodeTokenResponseClient();
        result.setRequestEntityConverter(new AADB2COAuth2AuthorizationCodeGrantRequestEntityConverter());
        return result;
    }
}

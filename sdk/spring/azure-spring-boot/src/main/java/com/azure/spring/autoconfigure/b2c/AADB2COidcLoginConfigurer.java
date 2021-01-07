// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

/**
 * Configure B2C OAUTH2 login properties.
 */
public class AADB2COidcLoginConfigurer extends AbstractHttpConfigurer<AADB2COidcLoginConfigurer, HttpSecurity> {

    private final AADB2CProperties properties;

    private final AADB2CLogoutSuccessHandler handler;

    private final AADB2CAuthorizationRequestResolver resolver;

    public AADB2COidcLoginConfigurer(AADB2CProperties properties,
                                     AADB2CLogoutSuccessHandler handler, AADB2CAuthorizationRequestResolver resolver) {
        this.properties = properties;
        this.handler = handler;
        this.resolver = resolver;
    }

    @Override
    public void init(HttpSecurity http) throws Exception {
        http.logout()
                .logoutSuccessHandler(handler)
                .and()
            .oauth2Login()
                .loginProcessingUrl(properties.getLoginProcessingUrl())
                .authorizationEndpoint()
                    .authorizationRequestResolver(resolver);
    }
}

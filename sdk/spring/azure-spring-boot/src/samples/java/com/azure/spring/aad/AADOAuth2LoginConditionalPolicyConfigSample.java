// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import com.azure.spring.autoconfigure.aad.AADAuthenticationFailureHandler;
import com.azure.spring.autoconfigure.aad.AADOAuth2AuthorizationRequestResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 * <p>
 * Code samples for the AAD OAuth2.0 login in README.md, including handling for AAD conditional policy
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AADOAuth2LoginConditionalPolicyConfigSample extends WebSecurityConfigurerAdapter {

    @Autowired
    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService;

    @Autowired
    ApplicationContext applicationContext;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        final ClientRegistrationRepository clientRegistrationRepository =
            applicationContext.getBean(ClientRegistrationRepository.class);

        http.authorizeRequests()
            .anyRequest().authenticated()
            .and()
            .oauth2Login()
            .userInfoEndpoint()
            .oidcUserService(oidcUserService)
            .and()
            .authorizationEndpoint()
            .authorizationRequestResolver(new AADOAuth2AuthorizationRequestResolver(clientRegistrationRepository))
            .and()
            .failureHandler(new AADAuthenticationFailureHandler());
    }
}

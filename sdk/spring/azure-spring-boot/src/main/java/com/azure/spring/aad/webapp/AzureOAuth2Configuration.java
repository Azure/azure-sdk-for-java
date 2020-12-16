// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * Abstract configuration class, used to make AzureClientRegistrationRepository
 * and AuthzCodeGrantRequestEntityConverter take effect.
 */
public abstract class AzureOAuth2Configuration extends WebSecurityConfigurerAdapter {

    @Autowired
    private AzureClientRegistrationRepository repo;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.oauth2Login(Customizer.withDefaults())
            .logout(logout -> logout.logoutSuccessHandler(new OidcClientInitiatedLogoutSuccessHandler(this.repo)))
            .oauth2Login()
            .tokenEndpoint()
            .accessTokenResponseClient(accessTokenResponseClient())
            .and().authorizationEndpoint().authorizationRequestResolver(requestResolver())
            .and().failureHandler(failureHandler());
    }

    protected OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient result = new DefaultAuthorizationCodeTokenResponseClient();
        RestTemplate restTemplate = new RestTemplate(Arrays.asList(
            new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter()));
        restTemplate.setErrorHandler(new AADConditionalAccessResponseErrorHandler());
        result.setRestOperations(restTemplate);
        result.setRequestEntityConverter(new AuthzCodeGrantRequestEntityConverter(repo.getAzureClient()));
        return result;
    }

    protected OAuth2AuthorizationRequestResolver requestResolver() {
        return new AzureOAuth2AuthorizationRequestResolver(this.repo);
    }

    protected AuthenticationFailureHandler failureHandler() {
        return new AzureOAuthenticationFailureHandler();
    }
}

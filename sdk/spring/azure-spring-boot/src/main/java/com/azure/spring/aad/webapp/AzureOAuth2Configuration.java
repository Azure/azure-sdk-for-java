// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;

/**
 * Abstract configuration class, used to make AzureClientRegistrationRepository
 * and AuthzCodeGrantRequestEntityConverter take effect.
 */
public abstract class AzureOAuth2Configuration extends WebSecurityConfigurerAdapter {

    @Autowired
    private AzureClientRegistrationRepository repo;

    protected OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient result = new DefaultAuthorizationCodeTokenResponseClient();
        result.setRequestEntityConverter(new AuthzCodeGrantRequestEntityConverter(repo.getAzureClient()));
        return result;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.oauth2Login(Customizer.withDefaults())
            .logout(logout -> logout.logoutSuccessHandler(new OidcClientInitiatedLogoutSuccessHandler(this.repo)))
            .oauth2Login()
            .tokenEndpoint()
            .accessTokenResponseClient(accessTokenResponseClient());
    }
}

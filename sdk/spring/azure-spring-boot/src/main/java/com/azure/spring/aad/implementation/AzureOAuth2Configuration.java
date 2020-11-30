// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;

public abstract class AzureOAuth2Configuration extends WebSecurityConfigurerAdapter {

    @Autowired
    private AzureClientRegistrationRepository repo;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.oauth2Login().tokenEndpoint().accessTokenResponseClient(accessTokenResponseClient());
    }

    protected OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient result = new DefaultAuthorizationCodeTokenResponseClient();
        result.setRequestEntityConverter(new AuthzCodeGrantRequestEntityConverter(repo.getAzureClient()));
        return result;
    }
}

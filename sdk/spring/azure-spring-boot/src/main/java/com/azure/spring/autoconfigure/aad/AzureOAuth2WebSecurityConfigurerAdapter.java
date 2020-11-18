// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;

import java.util.Optional;

/**
 * The main purpose of this class is to make AzureOAuth2AuthorizationCodeGrantRequestEntityConverter take effect.
 */
public abstract class AzureOAuth2WebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    @Autowired
    private AzureClientRegistrationRepository azureClientRegistrationRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.oauth2Login().tokenEndpoint().accessTokenResponseClient(accessTokenResponseClient());
    }

    protected OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient result = new DefaultAuthorizationCodeTokenResponseClient();
        Optional.ofNullable(azureClientRegistrationRepository)
                .map(AzureClientRegistrationRepository::defaultClient)
                .map(AzureOAuth2AuthorizationCodeGrantRequestEntityConverter::new)
                .ifPresent(result::setRequestEntityConverter);
        return result;
    }
}

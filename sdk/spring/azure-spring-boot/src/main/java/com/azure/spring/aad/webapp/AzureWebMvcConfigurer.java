// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.endpoint.DefaultRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.client.web.method.annotation.OAuth2AuthorizedClientArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Abstract configuration class, used to make RefreshTokenGrantRequestEntityConverter take effect.
 */
public abstract class AzureWebMvcConfigurer implements WebMvcConfigurer {

    @Autowired
    OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;

    public OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> oAuth2AccessTokenResponseClient() {
        DefaultRefreshTokenTokenResponseClient result = new DefaultRefreshTokenTokenResponseClient();
        result.setRequestEntityConverter(
            new RefreshTokenGrantRequestEntityConverter());
        return result;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new OAuth2AuthorizedClientArgumentResolver(oAuth2AuthorizedClientManager));
    }
}

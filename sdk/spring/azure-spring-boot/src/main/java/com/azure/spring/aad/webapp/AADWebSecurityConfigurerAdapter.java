// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;

/**
 * Abstract configuration class, used to make AzureClientRegistrationRepository
 * and AuthzCodeGrantRequestEntityConverter take effect.
 */
public abstract class AADWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    @Autowired
    private AADWebAppClientRegistrationRepository repo;
    @Autowired
    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService;
    @Autowired
    protected AADAuthenticationProperties properties;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.oauth2Login()
                .tokenEndpoint()
                    .accessTokenResponseClient(accessTokenResponseClient())
                    .and()
                .userInfoEndpoint()
                    .oidcUserService(oidcUserService)
                    .and()
                .and()
            .logout()
                .logoutSuccessHandler(oidcLogoutSuccessHandler())
                .and();
        // @formatter:off
    }

    protected LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
            new OidcClientInitiatedLogoutSuccessHandler(this.repo);
        String uri = this.properties.getPostLogoutRedirectUri();
        if (StringUtils.hasText(uri)) {
            // TODO (jack) Remove deprecated method after we do not need to support spring-boot-2.2.x
            oidcLogoutSuccessHandler.setPostLogoutRedirectUri(URI.create(uri));
        }
        return oidcLogoutSuccessHandler;
    }

    protected OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient result = new DefaultAuthorizationCodeTokenResponseClient();
        RestTemplate restTemplate = new RestTemplate(Arrays.asList(
            new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter()));
        restTemplate.setErrorHandler(new ConditionalAccessResponseErrorHandler());
        result.setRestOperations(restTemplate);
        result.setRequestEntityConverter(
            new AADOAuth2AuthorizationCodeGrantRequestEntityConverter(repo.getAzureClient()));
        return result;
    }

    protected OAuth2AuthorizationRequestResolver requestResolver() {
        return new AADOAuth2AuthorizationRequestResolver(this.repo);
    }

    protected AuthenticationFailureHandler failureHandler() {
        return new AADAuthenticationFailureHandler();
    }
}

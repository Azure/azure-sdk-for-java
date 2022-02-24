// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad;

import com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2.AadClientRegistrationRepository;
import com.azure.spring.cloud.autoconfigure.aad.implementation.webapp.AadHandleConditionalAccessFilter;
import com.azure.spring.cloud.autoconfigure.aad.implementation.webapp.AadOAuth2AuthorizationCodeGrantRequestEntityConverter;
import com.azure.spring.cloud.autoconfigure.aad.implementation.webapp.AadOAuth2AuthorizationRequestResolver;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.util.StringUtils;

/**
 * Abstract configuration class, used to make AzureClientRegistrationRepository and AuthzCodeGrantRequestEntityConverter
 * take effect.
 *
 * @see WebSecurityConfigurerAdapter
 */
public abstract class AadWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    @Autowired
    private ClientRegistrationRepository repo;
    @Autowired
    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService;

    /**
     * AAD authentication properties
     */
    @Autowired
    protected AadAuthenticationProperties properties;

    /**
     * configure
     *
     * @param http the {@link HttpSecurity} to use
     * @throws Exception Configuration failed
     *
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.oauth2Login()
                .authorizationEndpoint()
                    .authorizationRequestResolver(requestResolver())
                    .and()
                .tokenEndpoint()
                    .accessTokenResponseClient(accessTokenResponseClient())
                    .and()
                .userInfoEndpoint()
                    .oidcUserService(oidcUserService)
                    .and()
                .and()
            .logout()
                .logoutSuccessHandler(oidcLogoutSuccessHandler())
                .and()
            .addFilterAfter(new AadHandleConditionalAccessFilter(), OAuth2AuthorizationRequestRedirectFilter.class);
        // @formatter:off
    }

    /**
     * Gets the OIDC logout success handler.
     *
     * @return the OIDC logout success handler
     */
    protected LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
            new OidcClientInitiatedLogoutSuccessHandler(this.repo);
        String uri = this.properties.getPostLogoutRedirectUri();
        if (StringUtils.hasText(uri)) {
            oidcLogoutSuccessHandler.setPostLogoutRedirectUri(uri);
        }
        return oidcLogoutSuccessHandler;
    }

    /**
     * Gets the access token response client.
     *
     * @return the access token response client
     */
    protected OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient result = new DefaultAuthorizationCodeTokenResponseClient();
        if (repo instanceof AadClientRegistrationRepository) {
            result.setRequestEntityConverter(
                new AadOAuth2AuthorizationCodeGrantRequestEntityConverter(
                    ((AadClientRegistrationRepository) repo).getAzureClientAccessTokenScopes()));
        }
        return result;
    }

    /**
     * Gets the request resolver.
     *
     * @return the request resolver
     */
    protected OAuth2AuthorizationRequestResolver requestResolver() {
        return new AadOAuth2AuthorizationRequestResolver(this.repo, properties);
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad;

import com.azure.spring.cloud.autoconfigure.aad.implementation.jwt.AadJwtClientAuthenticationParametersConverter;
import com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2.OAuth2ClientAuthenticationJwkResolver;
import com.azure.spring.cloud.autoconfigure.aad.implementation.webapp.AadOAuth2AuthorizationCodeGrantRequestEntityConverter;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import org.springframework.beans.factory.ObjectProvider;
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

import javax.servlet.Filter;

/**
 * Abstract configuration class, used to make AzureClientRegistrationRepository and AuthzCodeGrantRequestEntityConverter
 * take effect.
 *
 * @see WebSecurityConfigurerAdapter
 */
public abstract class AadWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    /**
     * A repository for OAuth 2.0 / OpenID Connect 1.0 ClientRegistration(s).
     */
    @Autowired
    protected ClientRegistrationRepository repo;

    /**
     * OIDC user service.
     */
    @Autowired
    protected OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService;

    /**
     * AAD authentication properties
     */
    @Autowired
    protected AadAuthenticationProperties properties;

    /**
     * JWK resolver implementation for client authentication.
     */
    @Autowired
    protected ObjectProvider<OAuth2ClientAuthenticationJwkResolver> jwkResolvers;

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
                .logoutSuccessHandler(oidcLogoutSuccessHandler());
        // @formatter:off

        Filter conditionalAccessFilter = conditionalAccessFilter();
        if (conditionalAccessFilter != null) {
            http.addFilterAfter(conditionalAccessFilter, OAuth2AuthorizationRequestRedirectFilter.class);
        }
    }

    /**
     * Return the filter to handle conditional access exception.
     * No conditional access filter is provided by default.
     * @see <a href="https://github.com/Azure-Samples/azure-spring-boot-samples/tree/spring-cloud-azure_4.0.0/aad/spring-cloud-azure-starter-active-directory/web-client-access-resource-server/aad-web-application/src/main/java/com/azure/spring/sample/aad/security/AadConditionalAccessFilter.java">Sample for AAD conditional access filter</a>
     * @see <a href="https://microsoft.github.io/spring-cloud-azure/4.0.0/4.0.0/reference/html/index.html#support-conditional-access-in-web-application">reference doc</a>
     * @return a filter that handles conditional access exception.
     */
    protected Filter conditionalAccessFilter() {
        return null;
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
            AadOAuth2AuthorizationCodeGrantRequestEntityConverter converter =
                new AadOAuth2AuthorizationCodeGrantRequestEntityConverter(
                    ((AadClientRegistrationRepository) repo).getAzureClientAccessTokenScopes());
            OAuth2ClientAuthenticationJwkResolver jwkResolver = jwkResolvers.getIfUnique();
            if (jwkResolver != null) {
                converter.addParametersConverter(new AadJwtClientAuthenticationParametersConverter<>(jwkResolver::resolve));
            }
            result.setRequestEntityConverter(converter);
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

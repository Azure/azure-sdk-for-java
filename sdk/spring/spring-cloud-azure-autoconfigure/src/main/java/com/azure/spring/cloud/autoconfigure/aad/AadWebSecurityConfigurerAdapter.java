// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad;

import com.azure.spring.cloud.autoconfigure.aad.implementation.jwt.AadJwtClientAuthenticationParametersConverter;
import com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2.OAuth2ClientAuthenticationJwkResolver;
import com.azure.spring.cloud.autoconfigure.aad.implementation.webapp.AadOAuth2AuthorizationCodeGrantRequestEntityConverter;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import com.nimbusds.jose.jwk.JWK;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.util.StringUtils;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.AadRestTemplateCreator.createOAuth2AccessTokenResponseClientRestTemplate;

/**
 * Abstract configuration class, used to make Azure client registration repository and OAuth2 request entity converter take effect.
 */
public abstract class AadWebSecurityConfigurerAdapter {

    /**
     * A security builder to configure the SecurityFilterChain instance.
     */
    protected final HttpSecurity httpSecurity;

    /**
     * A repository for OAuth 2.0 / OpenID Connect 1.0 ClientRegistration(s).
     */
    protected final ClientRegistrationRepository repo;

    /**
     * restTemplateBuilder bean used to create RestTemplate for Azure AD related http request.
     */
    protected final RestTemplateBuilder restTemplateBuilder;

    /**
     * OIDC user service.
     */
    protected final OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService;

    /**
     * AAD authentication properties
     */
    protected final AadAuthenticationProperties properties;

    /**
     * JWK resolver implementation for client authentication.
     */
    protected final ObjectProvider<OAuth2ClientAuthenticationJwkResolver> jwkResolvers;

    /**
     * Creates a new instance
     * @param properties the {@link AadAuthenticationProperties} to configure the OAuth2 clients
     * @param httpSecurity the security builder to configure the SecurityFilterChain instance.
     * @param repo the OAuth2 client repository
     * @param restTemplateBuilder the rest template builder
     * @param oidcUserService the user service to load OAuth2 user info
     * @param jwkResolvers the resolvers to resolve a {@link JWK}.
     */
    public AadWebSecurityConfigurerAdapter(AadAuthenticationProperties properties,
                                           HttpSecurity httpSecurity,
                                           ClientRegistrationRepository repo,
                                           RestTemplateBuilder restTemplateBuilder,
                                           OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService,
                                           ObjectProvider<OAuth2ClientAuthenticationJwkResolver> jwkResolvers) {
        this.properties = properties;
        this.httpSecurity = httpSecurity;
        this.repo = repo;
        this.restTemplateBuilder = restTemplateBuilder;
        this.oidcUserService = oidcUserService;
        this.jwkResolvers = jwkResolvers;
    }

    /**
     * The subclass can extend the HttpSecurity configuration if necessary, it will be invoked by {@link AadWebSecurityConfigurerAdapter#build()}.
     * @param http the {@link HttpSecurity} to use
     * @throws Exception Configuration failed
     */
    protected void configure(HttpSecurity http) throws Exception {

    }

    /**
     * Build a SecurityFilterChain instance.
     *
     * @return a default SecurityFilterChain instance
     * @throws Exception Configuration failed
     */
    public SecurityFilterChain build() throws Exception {
        // @formatter:off
        httpSecurity.oauth2Login()
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
            httpSecurity.addFilterAfter(conditionalAccessFilter, OAuth2AuthorizationRequestRedirectFilter.class);
        }
        this.configure(httpSecurity);
        return httpSecurity.build();
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
        result.setRestOperations(createOAuth2AccessTokenResponseClientRestTemplate(restTemplateBuilder));
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

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security;

import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.security.config.annotation.SecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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

import static com.azure.spring.cloud.autoconfigure.implementation.aad.utils.AadRestTemplateCreator.createOAuth2AccessTokenResponseClientRestTemplate;

/**
 * HTTP security configurer class for Azure Active Directory Web application scenario, used to
 * make Azure client registration repository and OAuth2 request entity converter take effect.
 */
public class AadWebApplicationHttpSecurityConfigurer extends AbstractHttpConfigurer<AadWebApplicationHttpSecurityConfigurer, HttpSecurity> {

    /**
     * A repository for OAuth 2.0 / OpenID Connect 1.0 ClientRegistration(s).
     */
    protected ClientRegistrationRepository repo;

    /**
     * restTemplateBuilder bean used to create RestTemplate for Azure AD related http request.
     */
    protected RestTemplateBuilder restTemplateBuilder;

    /**
     * OIDC user service.
     */
    protected OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService;

    /**
     * AAD authentication properties
     */
    protected AadAuthenticationProperties properties;

    /**
     * JWK resolver implementation for client authentication.
     */
    protected ObjectProvider<OAuth2ClientAuthenticationJwkResolver> jwkResolvers;

    /**
     * Conditional access filter, it's optional configuration.
     */
    private Filter conditionalAccessFilter;

    @SuppressWarnings({"deprecation", "removal"})
    @Override
    public void init(HttpSecurity builder)throws Exception {
        super.init(builder);
        ApplicationContext context = builder.getSharedObject(ApplicationContext.class);

        this.repo = context.getBean(ClientRegistrationRepository.class);
        this.properties = context.getBean(AadAuthenticationProperties.class);
        this.restTemplateBuilder = context.getBean(RestTemplateBuilder.class);

        ObjectProvider<OAuth2UserService<OidcUserRequest, OidcUser>> oidcUserServiceProvider = context.getBeanProvider(
            ResolvableType.forClassWithGenerics(OAuth2UserService.class, OidcUserRequest.class, OidcUser.class));
        this.oidcUserService = oidcUserServiceProvider.getIfUnique();
        this.jwkResolvers = context.getBeanProvider(OAuth2ClientAuthenticationJwkResolver.class);

        // @formatter:off
        builder.oauth2Login()
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
    }

    @Override
    public void configure(HttpSecurity builder) throws Exception {
        if (conditionalAccessFilter != null) {
            builder.addFilterAfter(conditionalAccessFilter, OAuth2AuthorizationRequestRedirectFilter.class);
        }
    }

    /**
     * Default configuer for Web Application with Azure AD.
     * @return the configuer instance to customize the {@link SecurityConfigurer}
     */
    public static AadWebApplicationHttpSecurityConfigurer aadWebApplication() {
        return new AadWebApplicationHttpSecurityConfigurer();
    }

    /**
     * Return the filter to handle conditional access exception. No conditional access filter is provided by default.
     * @param conditionalAccessFilter the conditional access filter
     * @see <a href="https://github.com/Azure-Samples/azure-spring-boot-samples/tree/spring-cloud-azure_4.0.0/aad/spring-cloud-azure-starter-active-directory/web-client-access-resource-server/aad-web-application/src/main/java/com/azure/spring/sample/aad/security/AadConditionalAccessFilter.java">Sample for AAD conditional access filter</a>
     * @see <a href="https://microsoft.github.io/spring-cloud-azure/4.0.0/4.0.0/reference/html/index.html#support-conditional-access-in-web-application">reference doc</a>
     * @return a filter that handles conditional access exception.
     */
    public AadWebApplicationHttpSecurityConfigurer conditionalAccessFilter(Filter conditionalAccessFilter) {
        this.conditionalAccessFilter = conditionalAccessFilter;
        return this;
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

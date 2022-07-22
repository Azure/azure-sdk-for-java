// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad;

import com.azure.spring.cloud.autoconfigure.aad.implementation.conditions.ConditionalOnReactiveTokenResponseClientConverterSettable;
import com.azure.spring.cloud.autoconfigure.aad.implementation.jwt.AadJwtClientAuthenticationParametersConverter;
import com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2.OAuth2ClientAuthenticationJwkResolver;
import com.azure.spring.cloud.autoconfigure.aad.implementation.webapp.AadOAuth2AuthorizationCodeGrantRequestEntityConverter;
import com.azure.spring.cloud.autoconfigure.aad.implementation.webapp.AadReactiveOAuth2AuthorizationCodeGrantRequestEntityConverter;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeReactiveAuthenticationManager;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.Filter;

/**
 * Abstract configuration class, used to make AzureClientRegistrationRepository and AuthzCodeGrantRequestEntityConverter
 * take effect.
 */
@ConditionalOnReactiveTokenResponseClientConverterSettable()
public abstract class AadWebFluxSecurityConfiguration {

    /**
     * A repository for OAuth 2.0 / OpenID Connect 1.0 ClientRegistration(s).
     */
    @Autowired
    protected ReactiveClientRegistrationRepository repo;

    /**
     * OIDC user service.
     */
    @Autowired
    protected ReactiveOAuth2UserService<OidcUserRequest, OidcUser> oidcUserService;

    /**
     * AAD authentication properties
     */
    @Autowired
    protected AadAuthenticationProperties properties;

    /**
     * AAD authentication manager.
     */
    @Autowired
    protected ReactiveAuthenticationManager authenticationManager;


    /**
     * AAD security context repository.
     */
    @Autowired
    protected ServerSecurityContextRepository securityContextRepository;

    /**
     * JWK resolver implementation for client authentication.
     */
    @Autowired
    protected ObjectProvider<OAuth2ClientAuthenticationJwkResolver> jwkResolvers;

    /**
     * The security configuration.
     *
     * @param http the security configuration chain.
     * @return the security configuration chain.
     */
    protected ServerHttpSecurity serverHttpSecurity(ServerHttpSecurity http) {
        return http
            .securityContextRepository(securityContextRepository)
            .authenticationManager(authenticationManager)
            .oauth2Login(oauth -> oauth
                .authorizationRequestResolver(requestResolver())
            ).oauth2Client(client -> client
                .authenticationManager(authorizationCodeAuthenticationManager())
            )
            .logout()
                .logoutSuccessHandler(oidcLogoutSuccessHandler())
            .and();
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
    protected ServerLogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedServerLogoutSuccessHandler oidcLogoutSuccessHandler =
            new OidcClientInitiatedServerLogoutSuccessHandler(this.repo);
        String uri = this.properties.getPostLogoutRedirectUri();
        if (StringUtils.hasText(uri)) {
            oidcLogoutSuccessHandler.setPostLogoutRedirectUri(uri);
        }
        return oidcLogoutSuccessHandler;
    }

    /**
     * Creates the authorization code authentication manager.
     *
     * @return the autorization code authentication manager.
     */
    protected ReactiveAuthenticationManager authorizationCodeAuthenticationManager() {
        WebClientReactiveAuthorizationCodeTokenResponseClient accessTokenResponseClient =
            new WebClientReactiveAuthorizationCodeTokenResponseClient();
        if (repo instanceof AadReactiveClientRegistrationRepository) {
            AadOAuth2AuthorizationCodeGrantRequestEntityConverter converter =
                new AadOAuth2AuthorizationCodeGrantRequestEntityConverter(
                    ((AadReactiveClientRegistrationRepository) repo).getAzureClientAccessTokenScopes());
            OAuth2ClientAuthenticationJwkResolver jwkResolver = jwkResolvers.getIfUnique();
            if (jwkResolver != null) {
                converter.addParametersConverter(new AadJwtClientAuthenticationParametersConverter<>(jwkResolver::resolve));
            }
            AadReactiveOAuth2AuthorizationCodeGrantRequestEntityConverter reactiveConverter = new AadReactiveOAuth2AuthorizationCodeGrantRequestEntityConverter(converter);
            this.setHeadersConverter(accessTokenResponseClient, reactiveConverter.getHeadersConverter());
            this.setParametersConverter(accessTokenResponseClient, reactiveConverter.getParametersConverter());
        }

        return new OAuth2AuthorizationCodeReactiveAuthenticationManager(accessTokenResponseClient);
    }

    /**
     * Sets the headers converter by reflection for Spring boot 2.5 handling.
     *
     * @param client the token response client.
     * @param converter the converter.
     */
    private void setHeadersConverter(WebClientReactiveAuthorizationCodeTokenResponseClient client, Converter<OAuth2AuthorizationCodeGrantRequest, HttpHeaders> converter) {
        Method method;
        try {
            method = WebClientReactiveAuthorizationCodeTokenResponseClient.class.getMethod("setHeadersConverter");
            method.invoke(client, converter);
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
    }

    /**
     * Sets the headers converter by reflection for Spring boot 2.5 handling.
     *
     * @param client the token response client.
     * @param converter the converter.
     */
    private void setParametersConverter(WebClientReactiveAuthorizationCodeTokenResponseClient client, Converter<OAuth2AuthorizationCodeGrantRequest, MultiValueMap<String, String>> converter) {
        Method method;
        try {
            method = WebClientReactiveAuthorizationCodeTokenResponseClient.class.getMethod("setParametersConverter");
            method.invoke(client, converter);
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
    }

    /**
     * Gets the access token response client.
     *
     * @return the access token response client
     */
    protected OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient result = new DefaultAuthorizationCodeTokenResponseClient();
        if (repo instanceof AadReactiveClientRegistrationRepository) {
            AadOAuth2AuthorizationCodeGrantRequestEntityConverter converter =
                new AadOAuth2AuthorizationCodeGrantRequestEntityConverter(
                    ((AadReactiveClientRegistrationRepository) repo).getAzureClientAccessTokenScopes());
            OAuth2ClientAuthenticationJwkResolver jwkResolver = jwkResolvers.getIfUnique();
            if (jwkResolver != null) {
                converter.addParametersConverter(new AadJwtClientAuthenticationParametersConverter<>(jwkResolver::resolve));
            }
            result.setRequestEntityConverter(converter);
        }
        return result;
    }

    /**
     * Gets the reactive request resolver.
     *
     * @return the request resolver
     */
    protected ServerOAuth2AuthorizationRequestResolver requestResolver() {
        return new AadServerOAuth2AuthorizationRequestResolver(this.repo, properties);
    }
}

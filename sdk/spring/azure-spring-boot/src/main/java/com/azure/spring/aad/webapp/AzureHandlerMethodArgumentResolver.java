// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Azure handler method argument resolver to add custom OAuth2AuthorizedClientManager
 */
public final class AzureHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
    private static final Authentication ANONYMOUS_AUTHENTICATION = new AnonymousAuthenticationToken(
        "anonymous", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

    private final OAuth2AuthorizedClientManager authorizedClientManager;

    private final boolean defaultAuthorizedClientManager;

    public AzureHandlerMethodArgumentResolver(OAuth2AuthorizedClientManager authorizedClientManager) {
        Assert.notNull(authorizedClientManager, "authorizedClientManager cannot be null");
        this.authorizedClientManager = authorizedClientManager;
        this.defaultAuthorizedClientManager = false;
    }

    public AzureHandlerMethodArgumentResolver(ClientRegistrationRepository clientRegistrationRepository,
                                              OAuth2AuthorizedClientRepository authorizedClientRepository) {
        Assert.notNull(clientRegistrationRepository, "clientRegistrationRepository cannot be null");
        Assert.notNull(authorizedClientRepository, "authorizedClientRepository cannot be null");
        this.authorizedClientManager = new DefaultOAuth2AuthorizedClientManager(
            clientRegistrationRepository, authorizedClientRepository);
        this.defaultAuthorizedClientManager = true;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> parameterType = parameter.getParameterType();
        return (OAuth2AuthorizedClient.class.isAssignableFrom(parameterType)
            && (AnnotatedElementUtils.findMergedAnnotation(
                parameter.getParameter(), RegisteredOAuth2AuthorizedClient.class) != null));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  @Nullable ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  @Nullable WebDataBinderFactory binderFactory) {
        String clientRegistrationId = this.resolveClientRegistrationId(parameter);
        if (StringUtils.isEmpty(clientRegistrationId)) {
            throw new IllegalArgumentException("Unable to resolve the Client Registration Identifier. "
                + "It must be provided via @RegisteredOAuth2AuthorizedClient(\"client1\") or "
                + "@RegisteredOAuth2AuthorizedClient(registrationId = \"client1\").");
        }

        Authentication principal = SecurityContextHolder.getContext().getAuthentication();
        if (principal == null) {
            principal = ANONYMOUS_AUTHENTICATION;
        }
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse servletResponse = webRequest.getNativeResponse(HttpServletResponse.class);

        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(clientRegistrationId)
            .principal(principal)
            .attribute(HttpServletRequest.class.getName(), servletRequest)
            .attribute(HttpServletResponse.class.getName(), servletResponse)
            .build();

        return this.authorizedClientManager.authorize(authorizeRequest);
    }

    private String resolveClientRegistrationId(MethodParameter parameter) {
        RegisteredOAuth2AuthorizedClient authorizedClientAnnotation = AnnotatedElementUtils.findMergedAnnotation(
            parameter.getParameter(), RegisteredOAuth2AuthorizedClient.class);

        Authentication principal = SecurityContextHolder.getContext().getAuthentication();

        Assert.notNull(authorizedClientAnnotation, "authorizedClientAnnotation cannot be null");
        String clientRegistrationId = null;
        if (!StringUtils.isEmpty(authorizedClientAnnotation.registrationId())) {
            clientRegistrationId = authorizedClientAnnotation.registrationId();
        } else if (!StringUtils.isEmpty(authorizedClientAnnotation.value())) {
            clientRegistrationId = authorizedClientAnnotation.value();
        } else if (principal != null && OAuth2AuthenticationToken.class.isAssignableFrom(principal.getClass())) {
            clientRegistrationId = ((OAuth2AuthenticationToken) principal).getAuthorizedClientRegistrationId();
        }

        return clientRegistrationId;
    }

    private void updateDefaultAuthorizedClientManager(
        OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> clientCredentialsTokenResponseClient) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
            OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode()
                .refreshToken()
                .clientCredentials(configurer ->
                    configurer.accessTokenResponseClient(clientCredentialsTokenResponseClient))
                .password()
                .build();
        ((DefaultOAuth2AuthorizedClientManager) this.authorizedClientManager)
            .setAuthorizedClientProvider(authorizedClientProvider);
    }

    @Deprecated
    public void setClientCredentialsTokenResponseClient(
        OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> clientCredentialsTokenResponseClient) {
        Assert.notNull(clientCredentialsTokenResponseClient, "clientCredentialsTokenResponseClient cannot be null");
        Assert.state(this.defaultAuthorizedClientManager, "The client cannot be set when the constructor used is "
            + "\"OAuth2AuthorizedClientArgumentResolver(OAuth2AuthorizedClientManager)\". "
            + "Instead, use the constructor "
            + "\"OAuth2AuthorizedClientArgumentResolver("
            + "ClientRegistrationRepository, OAuth2AuthorizedClientRepository)\".");
        updateDefaultAuthorizedClientManager(clientCredentialsTokenResponseClient);
    }
}

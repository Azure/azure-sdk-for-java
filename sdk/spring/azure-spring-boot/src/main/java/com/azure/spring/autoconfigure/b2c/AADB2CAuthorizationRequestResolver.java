// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class handles the OAuth2 request procession for AAD B2C authorization.
 * <p>
 * Userflow name is added in the request link and forgotten password redirection to password-reset page is added on the
 * base of default OAuth2 authorization resolve.
 */
@Deprecated
public class AADB2CAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private static final String REQUEST_BASE_URI =
            OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;

    private static final String REGISTRATION_ID_NAME = "registrationId";

    private static final String PARAMETER_X_CLIENT_SKU = "x-client-SKU";

    private static final String AAD_B2C_USER_AGENT = "spring-boot-starter";

    private static final String MATCHER_PATTERN = String.format("%s/{%s}", REQUEST_BASE_URI, REGISTRATION_ID_NAME);

    private static final AntPathRequestMatcher REQUEST_MATCHER = new AntPathRequestMatcher(MATCHER_PATTERN);

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    private final String passwordResetUserFlow;

    private final AADB2CProperties properties;

    public AADB2CAuthorizationRequestResolver(@NonNull ClientRegistrationRepository repository,
                                              @NonNull AADB2CProperties properties) {
        this.properties = properties;
        this.passwordResetUserFlow = this.properties.getPasswordReset();
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repository, REQUEST_BASE_URI);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(@NonNull HttpServletRequest request) {
        return resolve(request, getRegistrationId(request));
    }

    @Override
    public OAuth2AuthorizationRequest resolve(@NonNull HttpServletRequest request, String registrationId) {
        if (StringUtils.hasText(passwordResetUserFlow) && isForgotPasswordAuthorizationRequest(request)) {
            final OAuth2AuthorizationRequest authRequest = defaultResolver.resolve(request, passwordResetUserFlow);
            return getB2CAuthorizationRequest(authRequest, passwordResetUserFlow);
        }

        if (StringUtils.hasText(registrationId) && REQUEST_MATCHER.matches(request)) {
            return getB2CAuthorizationRequest(defaultResolver.resolve(request), registrationId);
        }

        // Return null may not be the good practice, but we need to align with oauth2.client.web
        // DefaultOAuth2AuthorizationRequestResolver.
        return null;
    }

    private OAuth2AuthorizationRequest getB2CAuthorizationRequest(@Nullable OAuth2AuthorizationRequest request,
                                                                  String userFlow) {
        Assert.hasText(userFlow, "User flow should contain text.");

        if (request == null) {
            return null;
        }

        final Map<String, Object> additionalParameters = new HashMap<>();
        Optional.ofNullable(this.properties)
                .map(AADB2CProperties::getAuthenticateAdditionalParameters)
                .ifPresent(additionalParameters::putAll);
        additionalParameters.put("p", userFlow);
        additionalParameters.put(PARAMETER_X_CLIENT_SKU, AAD_B2C_USER_AGENT);

        // OAuth2AuthorizationRequest.Builder.additionalParameters() in spring-security-oauth2-core 5.2.7.RELEASE
        // and 5.3.5.RELEASE implementation way is different, so we to compatible with them.
        additionalParameters.putAll(request.getAdditionalParameters());

        return OAuth2AuthorizationRequest.from(request).additionalParameters(additionalParameters).build();
    }

    private String getRegistrationId(HttpServletRequest request) {
        if (REQUEST_MATCHER.matches(request)) {
            return REQUEST_MATCHER.matcher(request).getVariables().get(REGISTRATION_ID_NAME);
        }

        return null;
    }

    // Handle the forgot password of sign-up-or-in page cannot redirect user to password-reset page.
    // The B2C service will enhance that, and then related code will be removed.
    private boolean isForgotPasswordAuthorizationRequest(@NonNull HttpServletRequest request) {
        final String error = request.getParameter("error");
        final String description = request.getParameter("error_description");

        if ("access_denied".equals(error)) {
            Assert.hasText(description, "description should contain text.");
            return description.startsWith("AADB2C90118:");
        }

        return false;
    }
}

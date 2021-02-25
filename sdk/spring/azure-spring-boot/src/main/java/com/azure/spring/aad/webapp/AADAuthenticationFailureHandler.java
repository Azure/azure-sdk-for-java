// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.azure.spring.autoconfigure.aad.Constants;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Redirect URL for handling OAuthentication failure
 */
public class AADAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private static final String DEFAULT_FAILURE_URL = "/login?error";
    private final AuthenticationFailureHandler defaultHandler;

    public AADAuthenticationFailureHandler() {
        this.defaultHandler = new SimpleUrlAuthenticationFailureHandler(DEFAULT_FAILURE_URL);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String claims = Optional.of(exception)
                                .filter(e -> e instanceof OAuth2AuthenticationException)
                                .map(e -> (OAuth2AuthenticationException) e)
                                .map(OAuth2AuthenticationException::getError)
                                .filter(e -> e instanceof AADOAuth2Error)
                                .map(e -> (AADOAuth2Error) e)
                                .map(AADOAuth2Error::getClaims)
                                .orElse(null);

        if (claims == null) {
            // Default handle logic
            defaultHandler.onAuthenticationFailure(request, response, exception);
        } else {
            // Handle conditional access policy, step 2.
            response.setStatus(302);
            request.getSession().setAttribute(Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS, claims);
            String redirectUrl = Optional.of(request)
                                         .map(HttpServletRequest::getSession)
                                         .map(s -> s.getAttribute(Constants.SAVED_REQUEST))
                                         .map(r -> (DefaultSavedRequest) r)
                                         .map(DefaultSavedRequest::getRedirectUrl)
                                         .orElse(null);
            response.sendRedirect(redirectUrl);
        }
    }
}

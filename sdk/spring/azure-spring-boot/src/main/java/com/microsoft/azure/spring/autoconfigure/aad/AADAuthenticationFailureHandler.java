// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.aad;

import com.microsoft.aad.msal4j.MsalServiceException;
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
 * Strategy used to handle a failed authentication attempt.
 * <p>
 * To redirect the user to the authentication page to allow them to try again when conditional access policy is
 * configured on Azure Active Directory.
 */
public class AADAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private AuthenticationFailureHandler defaultHandler;

    public AADAuthenticationFailureHandler() {
        this.defaultHandler = new SimpleUrlAuthenticationFailureHandler(Constants.FAILURE_DEFAULT_URL);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        final OAuth2AuthenticationException targetException = (OAuth2AuthenticationException) exception;
        // Handle conditional access policy
        if (Constants.CONDITIONAL_ACCESS_POLICY.equals((targetException.getError().getErrorCode()))) {
            // Get infos
            final Throwable cause = targetException.getCause();
            if (cause instanceof MsalServiceException) {
                // Put claims into session
                Optional.of(cause)
                        .map(c -> (MsalServiceException) c)
                        .map(MsalServiceException::claims)
                        .ifPresent(claims -> request.getSession().setAttribute(Constants.CAP_CLAIMS, claims));
                // Redirect
                String redirectUrl = Optional.of(request)
                        .map(HttpServletRequest::getSession)
                        .map(s -> s.getAttribute(Constants.SAVED_REQUEST))
                        .map(r -> (DefaultSavedRequest) r)
                        .map(DefaultSavedRequest::getRedirectUrl)
                        .orElse(null);
                response.sendRedirect(redirectUrl);
                response.setStatus(302);
                return;
            }
        }
        // Default handle logic
        defaultHandler.onAuthenticationFailure(request, response, exception);
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

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
    private static final String DEFAULT_FAILURE_URL = "/login?error";
    private final AuthenticationFailureHandler defaultHandler;

    public AADAuthenticationFailureHandler() {
        this.defaultHandler = new SimpleUrlAuthenticationFailureHandler(DEFAULT_FAILURE_URL);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        // Handle conditional access policy
        MsalServiceException msalServiceException = (MsalServiceException)
            Optional.of(exception)
                    .filter(e -> e instanceof OAuth2AuthenticationException)
                    .map(e -> (OAuth2AuthenticationException) e)
                    .filter(e -> AADOAuth2ErrorCode.CONDITIONAL_ACCESS_POLICY.equals((e.getError().getErrorCode())))
                    .map(Throwable::getCause)
                    .filter(cause -> cause instanceof MsalServiceException)
                    .orElse(null);
        if (msalServiceException == null) {
            // Default handle logic
            defaultHandler.onAuthenticationFailure(request, response, exception);
        } else {
            // Put claims into session
            Optional.of(msalServiceException)
                    .map(MsalServiceException::claims)
                    .ifPresent(claims -> request.getSession().setAttribute(Constants.CAP_CLAIMS, claims));
            // Redirect
            response.setStatus(302);
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

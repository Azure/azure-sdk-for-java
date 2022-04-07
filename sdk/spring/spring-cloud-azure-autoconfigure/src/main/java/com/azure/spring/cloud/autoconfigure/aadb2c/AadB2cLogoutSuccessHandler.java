// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c;

import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AadB2cUrl;
import com.azure.spring.cloud.autoconfigure.aadb2c.properties.AadB2cProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Get the url of successful logout and handle the navigation on logout.
 */
public class AadB2cLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    private final AadB2cProperties properties;

    /**
     * Creates a new instance of {@link AadB2cLogoutSuccessHandler}.
     *
     * @param properties the AAD B2C properties
     */
    public AadB2cLogoutSuccessHandler(AadB2cProperties properties) {
        this.properties = properties;

        super.setDefaultTargetUrl(getAadB2cEndSessionUrl());
    }

    private String getAadB2cEndSessionUrl() {
        final String userFlow = properties.getUserFlows().get(properties.getLoginFlow());
        final String logoutSuccessUrl = properties.getLogoutSuccessUrl();

        return AadB2cUrl.getEndSessionUrl(properties.getBaseUri(), logoutSuccessUrl, userFlow);
    }

    /**
     * A strategy that is called after a successful logout,
     * to handle redirection or forwarding to the appropriate destination.
     *
     * @param request the http servlet reqoest
     * @param response the http servlet response
     * @param authentication the authentication
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        super.onLogoutSuccess(request, response, authentication);
    }
}

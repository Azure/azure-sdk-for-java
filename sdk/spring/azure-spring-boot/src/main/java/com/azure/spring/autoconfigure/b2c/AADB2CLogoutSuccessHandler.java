// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Get the url of successful logout and handle the navigation on logout.
 */
public class AADB2CLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    private final AADB2CProperties properties;

    public AADB2CLogoutSuccessHandler(@NonNull AADB2CProperties properties) {
        this.properties = properties;

        super.setDefaultTargetUrl(getAADB2CEndSessionUrl());
    }

    private String getAADB2CEndSessionUrl() {
        final String userFlow = properties.getUserFlows().get(properties.getLoginFlow());
        final String logoutSuccessUrl = properties.getLogoutSuccessUrl();

        return AADB2CURL.getEndSessionUrl(properties.getBaseUri(), logoutSuccessUrl, userFlow);
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        super.onLogoutSuccess(request, response, authentication);
    }
}

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.b2c;

import lombok.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AADB2CLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    private final AADB2CProperties properties;

    public AADB2CLogoutSuccessHandler(@NonNull AADB2CProperties properties) {
        this.properties = properties;

        super.setDefaultTargetUrl(getAADB2CEndSessionUrl());
    }

    private String getAADB2CEndSessionUrl() {
        final String userFlow = properties.getUserFlows().getSignUpOrSignIn();
        final String logoutSuccessUrl = properties.getLogoutSuccessUrl();

        return AADB2CURL.getEndSessionUrl(properties.getTenant(), logoutSuccessUrl, userFlow);
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        super.onLogoutSuccess(request, response, authentication);
    }
}

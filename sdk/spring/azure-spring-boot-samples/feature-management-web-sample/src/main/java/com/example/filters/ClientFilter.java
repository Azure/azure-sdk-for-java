// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.example.filters;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.microsoft.azure.spring.cloud.feature.manager.FeatureFilter;
import com.microsoft.azure.spring.cloud.feature.manager.entities.FeatureFilterEvaluationContext;

@Component("ClientFilter")
public class ClientFilter implements FeatureFilter {

    private HttpServletRequest request;

    private static final String CLIENT_IP_KEY = "clientIp";

    @Autowired
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public boolean isValidIp(String clientIp) {
        return !(getRequestClientIp().equals(clientIp));

    }

    private String getRequestClientIp() {

        String remoteAddr = "";

        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (!StringUtils.hasText(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }

    @Override
    public boolean evaluate(FeatureFilterEvaluationContext context) {
        return !isValidIp((String) context.getParameters().get(CLIENT_IP_KEY));
    }

}

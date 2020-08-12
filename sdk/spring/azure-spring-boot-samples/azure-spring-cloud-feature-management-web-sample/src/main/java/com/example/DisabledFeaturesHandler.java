/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.example;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import com.microsoft.azure.spring.cloud.feature.manager.IDisabledFeaturesHandler;

@Component
public class DisabledFeaturesHandler implements IDisabledFeaturesHandler{

    @Override
    public HttpServletResponse handleDisabledFeatures(HttpServletRequest request, HttpServletResponse response) {
        response.addHeader("FeatureFlag", "false");
        return response;
    }

}

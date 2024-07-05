// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interface for Disabled Features Handler. The Feature Handler checks to see if this Component is implemented before
 * blocking an endpoint. If not implemented a 404 is returned.
 */
public interface DisabledFeaturesHandler {

    /**
     * Called when an endpoint interceptor returns and no redirect is set.
     *
     * @param request current HTTP
     * @param response current HTTP
     * @return response to current HTTP request
     */
    HttpServletResponse handleDisabledFeatures(HttpServletRequest request, HttpServletResponse response);

}

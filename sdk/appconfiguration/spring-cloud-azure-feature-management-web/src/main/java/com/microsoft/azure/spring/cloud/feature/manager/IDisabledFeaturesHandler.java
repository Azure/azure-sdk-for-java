// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.feature.manager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

/**
 * Interface for Disabled Features Handler. The Feature Handler checks to see if this
 * Component is implemented before blocking an endpoint. If not implemented a 404 is
 * returned.
 */
@Component
public interface IDisabledFeaturesHandler {

    /**
     * Called when an endpoint intercepter returns and no redirect is set.
     *
     * @param request current HTTP
     * @param response current HTTP
     * @return response to current HTTP request
     */
    HttpServletResponse handleDisabledFeatures(HttpServletRequest request, HttpServletResponse response);

}

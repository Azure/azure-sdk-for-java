/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.List;

/**
 * Cross-Origin Resource Sharing (CORS) settings for the web app.
 */
public class CorsSettings {
    /**
     * Gets or sets the list of origins that should be allowed to make
     * cross-origin
     * calls (for example: http://example.com:12345). Use "*" to
     * allow all.
     */
    private List<String> allowedOrigins;

    /**
     * Get the allowedOrigins value.
     *
     * @return the allowedOrigins value
     */
    public List<String> allowedOrigins() {
        return this.allowedOrigins;
    }

    /**
     * Set the allowedOrigins value.
     *
     * @param allowedOrigins the allowedOrigins value to set
     * @return the CorsSettings object itself.
     */
    public CorsSettings withAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
        return this;
    }

}

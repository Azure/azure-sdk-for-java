/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Information about the formal API definition for the web app.
 */
public class ApiDefinitionInfo {
    /**
     * The URL of the API definition.
     */
    private String url;

    /**
     * Get the url value.
     *
     * @return the url value
     */
    public String url() {
        return this.url;
    }

    /**
     * Set the url value.
     *
     * @param url the url value to set
     * @return the ApiDefinitionInfo object itself.
     */
    public ApiDefinitionInfo withUrl(String url) {
        this.url = url;
        return this;
    }

}

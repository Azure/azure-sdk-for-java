/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Entity representing the reference to the deployment paramaters.
 */
public class ParametersLink {
    /**
     * URI referencing the template.
     */
    @JsonProperty(required = true)
    private String uri;

    /**
     * If included it must match the ContentVersion in the template.
     */
    private String contentVersion;

    /**
     * Get the uri value.
     *
     * @return the uri value
     */
    public String uri() {
        return this.uri;
    }

    /**
     * Set the uri value.
     *
     * @param uri the uri value to set
     * @return the ParametersLink object itself.
     */
    public ParametersLink withUri(String uri) {
        this.uri = uri;
        return this;
    }

    /**
     * Get the contentVersion value.
     *
     * @return the contentVersion value
     */
    public String contentVersion() {
        return this.contentVersion;
    }

    /**
     * Set the contentVersion value.
     *
     * @param contentVersion the contentVersion value to set
     * @return the ParametersLink object itself.
     */
    public ParametersLink withContentVersion(String contentVersion) {
        this.contentVersion = contentVersion;
        return this;
    }

}

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;


/**
 * The ResourceGroupExportResultInner model.
 */
public class ResourceGroupExportResultInner {
    /**
     * Gets or sets the template content.
     */
    private Object template;

    /**
     * Gets or sets the error.
     */
    private ResourceManagementErrorWithDetails error;

    /**
     * Get the template value.
     *
     * @return the template value
     */
    public Object template() {
        return this.template;
    }

    /**
     * Set the template value.
     *
     * @param template the template value to set
     * @return the ResourceGroupExportResultInner object itself.
     */
    public ResourceGroupExportResultInner withTemplate(Object template) {
        this.template = template;
        return this;
    }

    /**
     * Get the error value.
     *
     * @return the error value
     */
    public ResourceManagementErrorWithDetails error() {
        return this.error;
    }

    /**
     * Set the error value.
     *
     * @param error the error value to set
     * @return the ResourceGroupExportResultInner object itself.
     */
    public ResourceGroupExportResultInner withError(ResourceManagementErrorWithDetails error) {
        this.error = error;
        return this;
    }

}

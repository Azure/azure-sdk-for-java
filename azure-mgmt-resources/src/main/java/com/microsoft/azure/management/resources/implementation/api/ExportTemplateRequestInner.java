/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;

import java.util.List;

/**
 * Export resource group template request parameters.
 */
public class ExportTemplateRequestInner {
    /**
     * Gets or sets the ids of the resources. The only supported string
     * currently is '*' (all resources). Future api updates will support
     * exporting specific resources.
     */
    private List<String> resources;

    /**
     * The export template options. Supported values include
     * 'IncludeParameterDefaultValue', 'IncludeComments' or
     * 'IncludeParameterDefaultValue, IncludeComments.
     */
    private String options;

    /**
     * Get the resources value.
     *
     * @return the resources value
     */
    public List<String> resources() {
        return this.resources;
    }

    /**
     * Set the resources value.
     *
     * @param resources the resources value to set
     * @return the ExportTemplateRequestInner object itself.
     */
    public ExportTemplateRequestInner withResources(List<String> resources) {
        this.resources = resources;
        return this;
    }

    /**
     * Get the options value.
     *
     * @return the options value
     */
    public String options() {
        return this.options;
    }

    /**
     * Set the options value.
     *
     * @param options the options value to set
     * @return the ExportTemplateRequestInner object itself.
     */
    public ExportTemplateRequestInner withOptions(String options) {
        this.options = options;
        return this;
    }

}

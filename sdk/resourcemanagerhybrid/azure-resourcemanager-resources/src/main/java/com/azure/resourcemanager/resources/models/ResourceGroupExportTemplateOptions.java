// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

/**
 * The export template options.
 */
public enum ResourceGroupExportTemplateOptions {
    /**
     * Includes default parameter values.
     */
    INCLUDE_PARAMETER_DEFAULT_VALUE("IncludeParameterDefaultValue"),

    /**
     * Includes comments.
     */
    INCLUDE_COMMENTS("IncludeComments"),

    /**
     * Includes default parameter values and comments.
     */
    INCLUDE_BOTH("IncludeParameterDefaultValue, IncludeComments");

    private String value;

    ResourceGroupExportTemplateOptions(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}

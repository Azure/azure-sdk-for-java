/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

/**
 * The export template options.
 */
public enum ResourceGroupExportTemplateOptions {
    INCLUDE_PARAMETER_DEFAULT_VALUE("IncludeParameterDefaultValue"),

    INCLUDE_COMMENTS("IncludeComments"),

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

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contains information for manual implementation for an Azure SQL Database, Server or Elastic Pool Recommended Action.
 */
@Immutable
public final class RecommendedActionImplementationInfo {
    /*
     * Gets the method in which this recommended action can be manually implemented. e.g., TSql, AzurePowerShell.
     */
    @JsonProperty(value = "method", access = JsonProperty.Access.WRITE_ONLY)
    private ImplementationMethod method;

    /*
     * Gets the manual implementation script. e.g., T-SQL script that could be executed on the database.
     */
    @JsonProperty(value = "script", access = JsonProperty.Access.WRITE_ONLY)
    private String script;

    /** Creates an instance of RecommendedActionImplementationInfo class. */
    public RecommendedActionImplementationInfo() {
    }

    /**
     * Get the method property: Gets the method in which this recommended action can be manually implemented. e.g.,
     * TSql, AzurePowerShell.
     *
     * @return the method value.
     */
    public ImplementationMethod method() {
        return this.method;
    }

    /**
     * Get the script property: Gets the manual implementation script. e.g., T-SQL script that could be executed on the
     * database.
     *
     * @return the script value.
     */
    public String script() {
        return this.script;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }
}

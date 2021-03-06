/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.synapse.v2020_12_01;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.microsoft.rest.serializer.JsonFlatten;

/**
 * The custom setup of setting environment variable.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", defaultImpl = EnvironmentVariableSetup.class)
@JsonTypeName("EnvironmentVariableSetup")
@JsonFlatten
public class EnvironmentVariableSetup extends CustomSetupBase {
    /**
     * The name of the environment variable.
     */
    @JsonProperty(value = "typeProperties.variableName", required = true)
    private String variableName;

    /**
     * The value of the environment variable.
     */
    @JsonProperty(value = "typeProperties.variableValue", required = true)
    private String variableValue;

    /**
     * Get the name of the environment variable.
     *
     * @return the variableName value
     */
    public String variableName() {
        return this.variableName;
    }

    /**
     * Set the name of the environment variable.
     *
     * @param variableName the variableName value to set
     * @return the EnvironmentVariableSetup object itself.
     */
    public EnvironmentVariableSetup withVariableName(String variableName) {
        this.variableName = variableName;
        return this;
    }

    /**
     * Get the value of the environment variable.
     *
     * @return the variableValue value
     */
    public String variableValue() {
        return this.variableValue;
    }

    /**
     * Set the value of the environment variable.
     *
     * @param variableValue the variableValue value to set
     * @return the EnvironmentVariableSetup object itself.
     */
    public EnvironmentVariableSetup withVariableValue(String variableValue) {
        this.variableValue = variableValue;
        return this;
    }

}

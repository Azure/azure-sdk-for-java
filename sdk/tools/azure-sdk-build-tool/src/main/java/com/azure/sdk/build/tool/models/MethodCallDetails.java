// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sdk.build.tool.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A model class that represents the details of a method call.
 */
public class MethodCallDetails {
    @JsonProperty
    private String methodName;

    @JsonProperty
    private int callFrequency;

    /**
     * Returns the name of the method.
     * @return The name of the method.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets the name of the method.
     * @param methodName The name of the method.
     * @return The updated {@link MethodCallDetails} object.
     */
    public MethodCallDetails setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    /**
     * Returns the number of times the method was called.
     * @return The number of times the method was called.
     */
    public int getCallFrequency() {
        return callFrequency;
    }

    /**
     * Sets the number of times the method was called.
     * @param callFrequency The number of times the method was called.
     * @return The updated {@link MethodCallDetails} object.
     */
    public MethodCallDetails setCallFrequency(int callFrequency) {
        this.callFrequency = callFrequency;
        return this;
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.implementation.serialization;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.*;

/**
 * An optional, helper class for deserializing a digital twin.
 */
@Fluent
@JsonInclude(Include.NON_NULL)
public class WritableProperty {

    @JsonProperty(value = "desiredValue")
    private Object desiredValue;

    @JsonProperty(value = "desiredVersion")
    private int desiredVersion;

    @JsonProperty(value = "ackVersion", required = true)
    private int ackVersion;

    @JsonProperty(value = "ackCode", required = true)
    private int ackCode;

    @JsonProperty(value = "ackDescription")
    private String ackDescription;

    /**
     * Gets the desired value.
     * @return The desired value.
     */
    public Object getDesiredValue() {
        return desiredValue;
    }

    /**
     * Sets the desired value.
     * @param desiredValue The desired value.
     * @return The WritableProperty object itself.
     */
    public WritableProperty setDesiredValue(Object desiredValue) {
        this.desiredValue = desiredValue;
        return this;
    }

    /**
     * Gets the version of the property with the specified desired value.
     * @return The version of the property with the specified desired value.
     */
    public int getDesiredVersion() {
        return desiredVersion;
    }

    /**
     * Sets the version of the property with the specified desired value.
     * @param desiredVersion The version of the property with the specified desired value.
     * @return The WritableProperty object itself.
     */
    public WritableProperty setDesiredVersion(int desiredVersion) {
        this.desiredVersion = desiredVersion;
        return this;
    }

    /**
     * Gets the version of the reported property value.
     * @return The version of the reported property value.
     */
    public int getAckVersion() {
        return ackVersion;
    }

    /**
     * Sets the version of the reported property value.
     * @param ackVersion The version of the reported property value.
     * @return The WritableProperty object itself.
     */
    public WritableProperty setAckVersion(int ackVersion) {
        this.ackVersion = ackVersion;
        return this;
    }

    /**
     * Gets the response code of the property update request, usually an HTTP Status Code (e.g. 200).
     * @return The response code of the property update request, usually an HTTP Status Code (e.g. 200).
     */
    public int getAckCode() {
        return ackCode;
    }

    /**
     * Sets the response code of the property update request, usually an HTTP Status Code (e.g. 200).
     * @param ackCode The response code of the property update request, usually an HTTP Status Code (e.g. 200).
     * @return The WritableProperty object itself.
     */
    public WritableProperty setAckCode(int ackCode) {
        this.ackCode = ackCode;
        return this;
    }

    /**
     * Gets the message response of the property update request.
     * @return The message response of the property update request.
     */
    public String getAckDescription() {
        return ackDescription;
    }

    /**
     * Sets the message response of the property update request.
     * @param ackDescription The message response of the property update request.
     * @return The WritableProperty object itself.
     */
    public WritableProperty setAckDescription(String ackDescription) {
        this.ackDescription = ackDescription;
        return this;
    }
}

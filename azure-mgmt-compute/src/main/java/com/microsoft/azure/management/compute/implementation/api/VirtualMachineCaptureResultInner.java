/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.SubResource;

/**
 * Resource Id.
 */
@JsonFlatten
public class VirtualMachineCaptureResultInner extends SubResource {
    /**
     * Operation output data (raw JSON).
     */
    @JsonProperty(value = "properties.output")
    private Object output;

    /**
     * Get the output value.
     *
     * @return the output value
     */
    public Object output() {
        return this.output;
    }

    /**
     * Set the output value.
     *
     * @param output the output value to set
     * @return the VirtualMachineCaptureResultInner object itself.
     */
    public VirtualMachineCaptureResultInner withOutput(Object output) {
        this.output = output;
        return this;
    }

}

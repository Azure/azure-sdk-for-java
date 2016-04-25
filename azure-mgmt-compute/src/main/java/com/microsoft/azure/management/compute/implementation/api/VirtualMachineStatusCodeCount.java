/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The status code and count of the virtual machine scale set instance view
 * status summary.
 */
public class VirtualMachineStatusCodeCount {
    /**
     * Gets the instance view status code.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String code;

    /**
     * Gets the number of instances having a particular status code.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer count;

    /**
     * Get the code value.
     *
     * @return the code value
     */
    public String code() {
        return this.code;
    }

    /**
     * Get the count value.
     *
     * @return the count value
     */
    public Integer count() {
        return this.count;
    }

}

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;


/**
 * Response for GetConnectionSharedKey Api servive call.
 */
public class ConnectionSharedKeyInner {
    /**
     * The virtual network connection shared key value.
     */
    private String value;

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public String value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the ConnectionSharedKeyInner object itself.
     */
    public ConnectionSharedKeyInner withValue(String value) {
        this.value = value;
        return this;
    }

}

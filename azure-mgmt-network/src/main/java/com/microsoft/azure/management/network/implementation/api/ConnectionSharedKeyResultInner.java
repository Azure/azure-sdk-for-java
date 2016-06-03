/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;


/**
 * Response for CheckConnectionSharedKey Api servive call.
 */
public class ConnectionSharedKeyResultInner {
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
     * @return the ConnectionSharedKeyResultInner object itself.
     */
    public ConnectionSharedKeyResultInner withValue(String value) {
        this.value = value;
        return this;
    }

}

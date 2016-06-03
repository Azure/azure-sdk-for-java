/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;


/**
 * The ConnectionResetSharedKeyInner model.
 */
public class ConnectionResetSharedKeyInner {
    /**
     * The virtual network connection reset shared key length.
     */
    private Long keyLength;

    /**
     * Get the keyLength value.
     *
     * @return the keyLength value
     */
    public Long keyLength() {
        return this.keyLength;
    }

    /**
     * Set the keyLength value.
     *
     * @param keyLength the keyLength value to set
     * @return the ConnectionResetSharedKeyInner object itself.
     */
    public ConnectionResetSharedKeyInner withKeyLength(Long keyLength) {
        this.keyLength = keyLength;
        return this;
    }

}

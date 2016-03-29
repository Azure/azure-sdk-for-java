/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.models;


/**
 * The ConnectionResetSharedKey model.
 */
public class ConnectionResetSharedKey {
    /**
     * The virtual network connection reset shared key length.
     */
    private Long keyLength;

    /**
     * Get the keyLength value.
     *
     * @return the keyLength value
     */
    public Long getKeyLength() {
        return this.keyLength;
    }

    /**
     * Set the keyLength value.
     *
     * @param keyLength the keyLength value to set
     */
    public void setKeyLength(Long keyLength) {
        this.keyLength = keyLength;
    }

}

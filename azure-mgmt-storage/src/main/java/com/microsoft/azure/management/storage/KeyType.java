/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage;

/**
 * Indicates whether the access key is primary or secondary.
 */
public enum KeyType {
    /** The access key is a primary key. */
    PRIMARY("Primary"),

    /** The access key is a secondary key. */
    SECONDARY("Secondary");

    private String value;

    KeyType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}

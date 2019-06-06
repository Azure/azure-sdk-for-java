// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.keys.models.webkey;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for KeyOperation.
 */
public enum KeyOperation {

    ENCRYPT("encrypt"),

    DECRYPT("decrypt"),

    SIGN("sign"),

    VERIFY("verify"),

    WRAP_KEY("wrapKey"),

    UNWRAP_KEY("unwrapKey"),

    OTHER("other");

    private String value;

    /**
     * Creates a custom value for KeyOperation.
     * @param value The custom value
     */
    KeyOperation(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    public int hashValue() {
        return value.hashCode();
    }

    /**
     * Return the KeyOperation which maps to {@code value}
     * @param value The value whose equivalent KeyOperation is needed.
     * @return the KeyOperation
     */
    public static KeyOperation fromString(String value) {
        for(KeyOperation keyOp : values())
            if(keyOp.value.equalsIgnoreCase(value)){
                return keyOp;
            }
        return null;
    }
}

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.webkey;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for JsonWebKeyOperation.
 */
public final class JsonWebKeyOperation {
    /** Static value encrypt for JsonWebKeyOperation. */
    public static final JsonWebKeyOperation ENCRYPT = new JsonWebKeyOperation("encrypt");

    /** Static value decrypt for JsonWebKeyOperation. */
    public static final JsonWebKeyOperation DECRYPT = new JsonWebKeyOperation("decrypt");

    /** Static value sign for JsonWebKeyOperation. */
    public static final JsonWebKeyOperation SIGN = new JsonWebKeyOperation("sign");

    /** Static value verify for JsonWebKeyOperation. */
    public static final JsonWebKeyOperation VERIFY = new JsonWebKeyOperation("verify");

    /** Static value wrapKey for JsonWebKeyOperation. */
    public static final JsonWebKeyOperation WRAP_KEY = new JsonWebKeyOperation("wrapKey");

    /** Static value unwrapKey for JsonWebKeyOperation. */
    public static final JsonWebKeyOperation UNWRAP_KEY = new JsonWebKeyOperation("unwrapKey");

    private String value;

    /**
     * Creates a custom value for JsonWebKeyOperation.
     * @param value the custom value
     */
    public JsonWebKeyOperation(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JsonWebKeyOperation)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        JsonWebKeyOperation rhs = (JsonWebKeyOperation) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }
    
    /**
     * All the JWK operations.
     */
    public static final List<JsonWebKeyOperation> ALL_OPERATIONS =
            Collections.unmodifiableList(Arrays.asList(ENCRYPT, DECRYPT, SIGN, VERIFY, WRAP_KEY, UNWRAP_KEY));
}

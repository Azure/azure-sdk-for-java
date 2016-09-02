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
 * Defines values for JsonWebKeyType.
 */
public final class JsonWebKeyType {
    /** Static value EC for JsonWebKeyType. */
    public static final JsonWebKeyType EC = new JsonWebKeyType("EC");

    /** Static value RSA for JsonWebKeyType. */
    public static final JsonWebKeyType RSA = new JsonWebKeyType("RSA");

    /** Static value RSA-HSM for JsonWebKeyType. */
    public static final JsonWebKeyType RSA_HSM = new JsonWebKeyType("RSA-HSM");

    /** Static value oct for JsonWebKeyType. */
    public static final JsonWebKeyType OCT = new JsonWebKeyType("oct");

    private String value;

    /**
     * Creates a custom value for JsonWebKeyType.
     * @param value the custom value
     */
    public JsonWebKeyType(String value) {
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
        if (!(obj instanceof JsonWebKeyType)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        JsonWebKeyType rhs = (JsonWebKeyType) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }
    
    /**
     * All the JWK key types.
     */
    public static final List<JsonWebKeyType> ALL_TYPES =
            Collections.unmodifiableList(Arrays.asList(EC, RSA, RSA_HSM, OCT));
}

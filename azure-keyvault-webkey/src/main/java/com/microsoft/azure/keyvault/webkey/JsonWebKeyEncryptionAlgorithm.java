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
 * Defines values for JsonWebKeyEncryptionAlgorithm.
 */
public final class JsonWebKeyEncryptionAlgorithm {
    /** Static value RSA-OAEP for JsonWebKeyEncryptionAlgorithm. */
    public static final JsonWebKeyEncryptionAlgorithm RSA_OAEP = new JsonWebKeyEncryptionAlgorithm("RSA-OAEP");

    /** Static value RSA1_5 for JsonWebKeyEncryptionAlgorithm. */
    public static final JsonWebKeyEncryptionAlgorithm RSA1_5 = new JsonWebKeyEncryptionAlgorithm("RSA1_5");

    private String value;

    /**
     * Creates a custom value for JsonWebKeyEncryptionAlgorithm.
     * @param value the custom value
     */
    public JsonWebKeyEncryptionAlgorithm(String value) {
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
        if (!(obj instanceof JsonWebKeyEncryptionAlgorithm)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        JsonWebKeyEncryptionAlgorithm rhs = (JsonWebKeyEncryptionAlgorithm) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }
    
    /**
     * All the JWK encryption algorithms.
     */
    public static final List<JsonWebKeyEncryptionAlgorithm> ALL_ALGORITHMS =
            Collections.unmodifiableList(Arrays.asList(RSA_OAEP, RSA1_5));
}

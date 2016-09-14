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
 * Defines values for JsonWebKeySignatureAlgorithm.
 */
public final class JsonWebKeySignatureAlgorithm {
    /** Static value RS256 for JsonWebKeySignatureAlgorithm. */
    public static final JsonWebKeySignatureAlgorithm RS256 = new JsonWebKeySignatureAlgorithm("RS256");

    /** Static value RS384 for JsonWebKeySignatureAlgorithm. */
    public static final JsonWebKeySignatureAlgorithm RS384 = new JsonWebKeySignatureAlgorithm("RS384");

    /** Static value RS512 for JsonWebKeySignatureAlgorithm. */
    public static final JsonWebKeySignatureAlgorithm RS512 = new JsonWebKeySignatureAlgorithm("RS512");

    /** Static value RSNULL for JsonWebKeySignatureAlgorithm. */
    public static final JsonWebKeySignatureAlgorithm RSNULL = new JsonWebKeySignatureAlgorithm("RSNULL");

    private String value;

    /**
     * Creates a custom value for JsonWebKeySignatureAlgorithm.
     * @param value the custom value
     */
    public JsonWebKeySignatureAlgorithm(String value) {
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
        if (!(obj instanceof JsonWebKeySignatureAlgorithm)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        JsonWebKeySignatureAlgorithm rhs = (JsonWebKeySignatureAlgorithm) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }
    
    /**
     * All the JWK signature algorithms.
     */
    public static final List<JsonWebKeySignatureAlgorithm> ALL_ALGORITHMS =
            Collections.unmodifiableList(Arrays.asList(RS256, RS384, RS512, RSNULL));
}

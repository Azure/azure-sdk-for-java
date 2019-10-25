// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;


/**
 * Defines values for KeyWrapAlgorithm.
 */
public final class KeyWrapAlgorithm extends ExpandableStringEnum<KeyWrapAlgorithm> {

    /**
     * Static value Encrypt for KeyWrapAlgorithm.
     */
    public static final KeyWrapAlgorithm RSA_OAEP = fromString("RSA-OAEP");

    /**
     * Static value Decrypt for KeyWrapAlgorithm.
     */
    public static final KeyWrapAlgorithm RSA_OAEP_256 = fromString("RSA-OAEP-256");

    /**
     * Static value Sign for KeyWrapAlgorithm.
     */
    public static final KeyWrapAlgorithm RSA1_5 = fromString("RSA1_5");

    /**
     * Static value Verify for KeyWrapAlgorithm.
     */
    public static final KeyWrapAlgorithm A192KW = fromString("A192KW");

    /**
     * Static value Wrap Key for KeyWrapAlgorithm.
     */
    public static final KeyWrapAlgorithm A128KW = fromString("A128KW");

    /**
     * Static value Unwrap Key for KeyWrapAlgorithm.
     */
    public static final KeyWrapAlgorithm A256KW = fromString("A256KW");

    /**
     * Creates or finds a KeyWrapAlgorithm from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding KeyWrapAlgorithm.
     */
    @JsonCreator
    public static KeyWrapAlgorithm fromString(String name) {
        return fromString(name, KeyWrapAlgorithm.class);
    }

    /**
     * @return known KeyWrapAlgorithm values.
     */
    public static Collection<KeyWrapAlgorithm> values() {
        return values(KeyWrapAlgorithm.class);
    }
}

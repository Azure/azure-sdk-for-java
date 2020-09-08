// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;


/**
 * Defines values for KeyOperation.
 */
public final class KeyOperation extends ExpandableStringEnum<KeyOperation> {

    /**
     * Static value Encrypt for KeyOperation.
     */
    public static final KeyOperation ENCRYPT = fromString("encrypt");

    /**
     * Static value Decrypt for KeyOperation.
     */
    public static final KeyOperation DECRYPT = fromString("decrypt");

    /**
     * Static value Sign for KeyOperation.
     */
    public static final KeyOperation SIGN = fromString("sign");

    /**
     * Static value Verify for KeyOperation.
     */
    public static final KeyOperation VERIFY = fromString("verify");

    /**
     * Static value Wrap Key for KeyOperation.
     */
    public static final KeyOperation WRAP_KEY = fromString("wrapKey");

    /**
     * Static value Unwrap Key for KeyOperation.
     */
    public static final KeyOperation UNWRAP_KEY = fromString("unwrapKey");

    /**
     * Static value Import for KeyOperation.
     */
    public static final KeyOperation IMPORT = fromString("import");

    /**
     * Creates or finds a KeyOperation from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding KeyOperation.
     */
    @JsonCreator
    public static KeyOperation fromString(String name) {
        return fromString(name, KeyOperation.class);
    }

    /**
     * @return known KeyOperation values.
     */
    public static Collection<KeyOperation> values() {
        return values(KeyOperation.class);
    }
}


// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;


/**
 * Defines values for KeyType.
 */
public final class KeyType extends ExpandableStringEnum<KeyType> {

    /**
     * Static value EC for KeyType.
     */
    public static final KeyType EC = fromString("EC");

    /**
     * Static value EC-HSM for KeyType.
     */
    public static final KeyType EC_HSM = fromString("EC-HSM");

    /**
     * Static value RSA for KeyType.
     */
    public static final KeyType RSA = fromString("RSA");

    /**
     * Static value RSA-HSM for KeyType.
     */
    public static final KeyType RSA_HSM = fromString("RSA-HSM");

    /**
     * Static value oct for KeyType.
     */
    public static final KeyType OCT = fromString("oct");

    /**
     * Static value oct-HSM for KeyType.
     */
    public static final KeyType OCT_HSM = fromString("oct-HSM");

    /**
     * Creates or finds a KeyType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding KeyType.
     */
    @JsonCreator
    public static KeyType fromString(String name) {
        return fromString(name, KeyType.class);
    }

    /**
     * @return known KeyType values.
     */
    public static Collection<KeyType> values() {
        return values(KeyType.class);
    }
}

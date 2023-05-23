// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Defines values for {@link KeyVaultSettingType}.
 */
public final class KeyVaultSettingType extends ExpandableStringEnum<KeyVaultSettingType> {
    /**
     * Static value boolean for {@link KeyVaultSettingType}.
     */
    public static final KeyVaultSettingType BOOLEAN = fromString("boolean");

    /**
     * Creates or finds a {@link KeyVaultSettingType} from its string representation.
     *
     * @param name A name to look for.
     *
     * @return The corresponding {@link KeyVaultSettingType}.
     */
    @JsonCreator
    public static KeyVaultSettingType fromString(String name) {
        return fromString(name, KeyVaultSettingType.class);
    }
}

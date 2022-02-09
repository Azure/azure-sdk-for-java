// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Defines values for {@link KeyVaultRoleType}.
 */
public final class KeyVaultRoleType extends ExpandableStringEnum<KeyVaultRoleType> {
    /**
     * Static value AKVBuiltInRole for {@link KeyVaultRoleType}.
     */
    public static final KeyVaultRoleType BUILT_IN_ROLE = fromString("AKVBuiltInRole");

    /**
     * Static value CustomRole for {@link KeyVaultRoleType}.
     */
    public static final KeyVaultRoleType CUSTOM_ROLE = fromString("CustomRole");

    /**
     * Creates or finds a {@link KeyVaultRoleType} from its string representation.
     *
     * @param name A name to look for.
     *
     * @return The corresponding {@link KeyVaultRoleType}.
     */
    @JsonCreator
    public static KeyVaultRoleType fromString(String name) {
        return fromString(name, KeyVaultRoleType.class);
    }
}

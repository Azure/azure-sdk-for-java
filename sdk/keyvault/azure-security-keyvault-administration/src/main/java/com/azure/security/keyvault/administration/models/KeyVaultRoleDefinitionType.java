// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Defines values for {@link KeyVaultRoleDefinitionType}.
 */
public final class KeyVaultRoleDefinitionType extends ExpandableStringEnum<KeyVaultRoleDefinitionType> {
    /**
     * Static value Microsoft.Authorization/roleDefinitions for {@link KeyVaultRoleDefinitionType}.
     */
    public static final KeyVaultRoleDefinitionType MICROSOFT_AUTHORIZATION_ROLE_DEFINITIONS =
        fromString("Microsoft.Authorization/roleDefinitions");

    /**
     * Creates or finds a {@link KeyVaultRoleDefinitionType} from its string representation.
     *
     * @param name A name to look for.
     *
     * @return The corresponding {@link KeyVaultRoleDefinitionType}.
     */
    @JsonCreator
    public static KeyVaultRoleDefinitionType fromString(String name) {
        return fromString(name, KeyVaultRoleDefinitionType.class);
    }
}

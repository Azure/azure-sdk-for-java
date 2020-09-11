// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.net.URI;

/**
 * A class that defines the scope of a role.
 */
public final class KeyVaultRoleAssignmentScope extends ExpandableStringEnum<KeyVaultRoleAssignmentScope> {
    public static final KeyVaultRoleAssignmentScope GLOBAL = fromString("/");
    public static final KeyVaultRoleAssignmentScope KEYS = fromString("/keys");

    /**
     * Creates or finds a {@link KeyVaultRoleAssignmentScope} from its string representation.
     *
     * @param name A name to look for.
     * @return The corresponding {@link KeyVaultRoleAssignmentScope}
     */
    public static KeyVaultRoleAssignmentScope fromString(String name) {
        return fromString(name, KeyVaultRoleAssignmentScope.class);
    }

    /**
     * Creates or finds a {@link KeyVaultRoleAssignmentScope} from its string representation.
     *
     * @param uri A URI to look for.
     * @return The corresponding {@link KeyVaultRoleAssignmentScope}
     */
    public static KeyVaultRoleAssignmentScope fromUri(URI uri) {
        return fromString(uri.getRawPath(), KeyVaultRoleAssignmentScope.class);
    }
}

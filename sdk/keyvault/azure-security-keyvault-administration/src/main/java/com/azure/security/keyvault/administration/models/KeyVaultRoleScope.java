// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.net.URI;

/**
 * A class that defines the scope of a role.
 */
public final class KeyVaultRoleScope extends ExpandableStringEnum<KeyVaultRoleScope> {
    public static final KeyVaultRoleScope GLOBAL = fromString("/");
    public static final KeyVaultRoleScope KEYS = fromString("/keys");

    /**
     * Creates or finds a {@link KeyVaultRoleScope} from its string representation.
     *
     * @param name A name to look for.
     * @return The corresponding {@link KeyVaultRoleScope}
     */
    public static KeyVaultRoleScope fromString(String name) {
        return fromString(name, KeyVaultRoleScope.class);
    }

    /**
     * Creates or finds a {@link KeyVaultRoleScope} from its string representation.
     *
     * @param uri A URI to look for.
     * @return The corresponding {@link KeyVaultRoleScope}
     */
    public static KeyVaultRoleScope fromUri(URI uri) {
        return fromString(uri.getRawPath(), KeyVaultRoleScope.class);
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.net.MalformedURLException;
import java.net.URL;

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
     * @return The corresponding {@link KeyVaultRoleScope}.
     */
    public static KeyVaultRoleScope fromString(String name) {
        return fromString(name, KeyVaultRoleScope.class);
    }

    /**
     * Creates or finds a {@link KeyVaultRoleScope} from its string representation.
     *
     * @param url A string representing a URL containing the name of the scope to look for.
     * @return The corresponding {@link KeyVaultRoleScope}.
     * @throws IllegalArgumentException If the given {@link String URL String} is malformed.
     */
    public static KeyVaultRoleScope fromUrl(String url) {
        try {
            return fromString(new URL(url).getPath(), KeyVaultRoleScope.class);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Creates or finds a {@link KeyVaultRoleScope} from its string representation.
     *
     * @param url A URL containing the name of the scope to look for.
     * @return The corresponding {@link KeyVaultRoleScope}.
     */
    public static KeyVaultRoleScope fromUrl(URL url) {
        return fromString(url.getPath(), KeyVaultRoleScope.class);
    }
}

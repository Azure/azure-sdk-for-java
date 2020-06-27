package com.azure.security.keyvault.administration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.net.URI;

public final class RoleScope extends ExpandableStringEnum<RoleScope> {
    public static final RoleScope GLOBAL = fromString("/");
    public static final RoleScope KEYS = fromString("/keys");

    /**
     * Creates or finds a {@link RoleScope} from its string representation.
     *
     * @param name A name to look for.
     * @return The corresponding {@link RoleScope}
     */
    public static RoleScope fromString(String name) {
        return fromString(name, RoleScope.class);
    }

    /**
     * Creates or finds a {@link RoleScope} from its string representation.
     *
     * @param uri A URI to look for.
     * @return The corresponding {@link RoleScope}
     */
    public static RoleScope fromUri(URI uri) {
        return fromString(uri.getRawPath(), RoleScope.class);
    }
}

package com.microsoft.windowsazure.services.media.models;

import java.util.EnumSet;

public class CreateAccessPolicyOptions {
    private final EnumSet<AccessPolicyPermission> permissions = EnumSet.noneOf(AccessPolicyPermission.class);

    public CreateAccessPolicyOptions() {
    }

    /**
     * Returns a live pointer to the underlying permissions set.
     * 
     * @return the permissions
     */
    public EnumSet<AccessPolicyPermission> getPermissions() {
        return permissions;
    }

    /**
     * Add the given permissions to this creation request
     * 
     * @param permissionsToAdd
     * @return the CreateAccessPolicyOptions object
     */
    public CreateAccessPolicyOptions addPermissions(EnumSet<AccessPolicyPermission> permissionsToAdd) {
        permissions.addAll(permissionsToAdd);
        return this;
    }

    /**
     * Remove the given permissions from this creation request
     * 
     * @param permissionsToRemove
     * @return the CreateAccessPolicyOptions object
     */
    public CreateAccessPolicyOptions removePermissions(EnumSet<AccessPolicyPermission> permissionsToRemove) {
        permissions.removeAll(permissionsToRemove);
        return this;
    }
}

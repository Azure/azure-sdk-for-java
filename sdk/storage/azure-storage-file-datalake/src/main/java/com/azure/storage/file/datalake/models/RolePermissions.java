// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * Represents the POSIX-style permissions on given resource for an arbitrary role. Each role can have any combination
 * of read, write, and execute permissions. Manipulating resource permissions is only supported when ADLS interop and
 * Hierarchical Namespace are enabled.
 */
public class RolePermissions {

    private final ClientLogger logger = new ClientLogger(RolePermissions.class);

    private static final String ROLE_PERMISSIONS_FORMAT_ERROR = "Invalid format for role permissions";

    /**
     * Grants read permissions for the role.
     */
    private boolean readPermission = false;

    /**
     * Grants write permissions for the role.
     */
    private boolean writePermission = false;

    /**
     * Grants execute permissions for the role.
     */
    private boolean executePermission = false;

    /**
     * Initializes an instance of {@code RolePermissions} with all values set to false.
     */
    public RolePermissions() {
    }

    /**
     * Package-private constructor for use by PathAccessControlEntry
     *
     * @param other {@link RolePermissions}
     */
    RolePermissions(RolePermissions other) {
        this.readPermission = other.readPermission;
        this.writePermission = other.writePermission;
        this.executePermission = other.executePermission;
    }

    /**
     * Convert an octal representation of permissions for a given role into an {@code RolePermissions} instance.
     *
     * @param octal The octal digit representing the permissions for the given role.
     * @return An {@link RolePermissions} instance with appropriate fields set.
     */
    public static RolePermissions parseOctal(int octal) {
        RolePermissions res = new RolePermissions();
        StorageImplUtils.assertInBounds("octal", octal, 0, 7);
        if (octal / 4 > 0) {
            res.readPermission = true;
        }
        octal = octal % 4;
        if (octal / 2 > 0) {
            res.writePermission = true;
        }
        octal = octal % 2;
        if (octal > 0) {
            res.executePermission = true;
        }
        return res;
    }

    /**
     * Convert a symbolic representation of permissions for a given role into an {@code RolePermissions} instance.
     *
     * @param str The string representing the permissions for the given role.
     * @param allowStickyBit Indicates whether or not the parsing should tolerate the sticky bit. The sticky bit is only
     * valid as the last character of permissions for "other" in a {@code String} representing full permissions for a
     * resource.
     * @return An {@link RolePermissions} instance with appropriate fields set.
     * @throws IllegalArgumentException if the String does not match the format.
     */
    public static RolePermissions parseSymbolic(String str, boolean allowStickyBit) {
        StorageImplUtils.assertNotNull("str", str);
        StorageImplUtils.assertInBounds("str.length", str.length(), 3, 3);

        RolePermissions res = new RolePermissions();
        IllegalArgumentException ex = new IllegalArgumentException(ROLE_PERMISSIONS_FORMAT_ERROR);
        if (str.charAt(0) == 'r') {
            res.readPermission = true;
        } else if (str.charAt(0) != '-') {
            throw ex;
        }

        if (str.charAt(1) == 'w') {
            res.writePermission = true;
        } else if (str.charAt(1) != '-') {
            throw ex;
        }

        if (str.charAt(2) == 'x') {
            res.executePermission = true;
        } else if (allowStickyBit) {
            if (str.charAt(2) == 't') {
                res.executePermission = true;
            } else if (str.charAt(2) != 'T' && str.charAt(2) != '-') {
                throw ex;
            }
        } else if (str.charAt(2) != '-') {
            throw ex;
        }

        return res;
    }

    /**
     * Converts the {@code RolePermissions} instance into its octal representation.
     *
     * @return The {@code String} representation of the permissions.
     */
    public String toOctal() {
        int res = 0;
        if (this.readPermission) {
            res = res | (1 << 2);
        }
        if (this.writePermission) {
            res = res | (1 << 1);
        }
        if (this.executePermission) {
            res = res | 1;
        }
        return "" + res;
    }

    /**
     * Converts the {@code RolePermissions} instance into its symbolic representation.
     *
     * @return The {@code String} representation of the permission.
     */
    public String toSymbolic() {
        StringBuilder sb = new StringBuilder();
        if (this.readPermission) {
            sb.append('r');
        } else {
            sb.append('-');
        }

        if (this.writePermission) {
            sb.append('w');
        } else {
            sb.append('-');
        }

        if (this.executePermission) {
            sb.append('x');
        } else {
            sb.append('-');
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RolePermissions that = (RolePermissions) o;

        if (readPermission != that.readPermission) {
            return false;
        }
        if (writePermission != that.writePermission) {
            return false;
        }
        return executePermission == that.executePermission;
    }

    @Override
    public int hashCode() {
        int result = (readPermission ? 1 : 0);
        result = 31 * result + (writePermission ? 1 : 0);
        result = 31 * result + (executePermission ? 1 : 0);
        return result;
    }

    /**
     * @return the read permission status
     */
    public boolean hasReadPermission() {
        return readPermission;
    }

    /**
     * @return the write permission status
     */
    public boolean hasWritePermission() {
        return writePermission;
    }

    /**
     * @return the execute permission status
     */
    public boolean hasExecutePermission() {
        return executePermission;
    }

    /**
     * Sets the read permission status.
     *
     * @param hasReadPermission Permission status to set
     * @return the updated RolePermissions object
     */
    public RolePermissions setReadPermission(boolean hasReadPermission) {
        this.readPermission = hasReadPermission;
        return this;
    }

    /**
     * Sets the write permission status.
     *
     * @param hasWritePermission Permission status to set
     * @return the updated RolePermissions object
     */
    public RolePermissions setWritePermission(boolean hasWritePermission) {
        this.writePermission = hasWritePermission;

        return this;
    }

    /**
     * Sets the execute permission status.
     *
     * @param hasExecutePermission Permission status to set
     * @return the updated RolePermissions object
     */
    public RolePermissions setExecutePermission(boolean hasExecutePermission) {
        this.executePermission = hasExecutePermission;
        return this;
    }
}

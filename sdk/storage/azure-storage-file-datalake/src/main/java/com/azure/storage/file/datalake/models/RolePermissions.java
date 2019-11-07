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
    private boolean read = false;

    /**
     * Grants write permissions for the role.
     */
    private boolean write = false;

    /**
     * Grants execute permissions for the role.
     */
    private boolean execute = false;

    /**
     * Initializes an instance of {@code RolePermissions} with all values set to false.
     */
    public RolePermissions() {

    }

    /**
     * Initializes an instance of {@code RolePermissions} with the given values.
     *
     * @param read Grants read permissions to this role.
     * @param write Grants write permissions to this role.
     * @param execute Grants execute permissions to this role.
     */
    public RolePermissions(boolean read, boolean write, boolean execute) {
        this.read = read;
        this.write = write;
        this.execute = execute;
    }

    /**
     * Initializs an instance of {@code RolePermissions} with values copied from the passed instance.
     *
     * @param other The instance with values to copy.
     */
    public RolePermissions(RolePermissions other) {
        this.read = other.read;
        this.write = other.write;
        this.execute = other.execute;
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
            res.read = true;
        }
        octal = octal % 4;
        if (octal / 2 > 0) {
            res.write = true;
        }
        octal = octal % 2;
        if (octal > 0) {
            res.execute = true;
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
            res.read = true;
        } else if (str.charAt(0) != '-') {
            throw ex;
        }

        if (str.charAt(1) == 'w') {
            res.write = true;
        } else if (str.charAt(1) != '-') {
            throw ex;
        }

        if (str.charAt(2) == 'x') {
            res.execute = true;
        } else if (allowStickyBit) {
            if (str.charAt(2) == 't') {
                res.execute = true;
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
        if (this.read) {
            res = res | (1 << 2);
        }
        if (this.write) {
            res = res | (1 << 1);
        }
        if (this.execute) {
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
        if (this.read) {
            sb.append('r');
        } else {
            sb.append('-');
        }

        if (this.write) {
            sb.append('w');
        } else {
            sb.append('-');
        }

        if (this.execute) {
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

        if (read != that.read) {
            return false;
        }
        if (write != that.write) {
            return false;
        }
        return execute == that.execute;
    }

    @Override
    public int hashCode() {
        int result = (read ? 1 : 0);
        result = 31 * result + (write ? 1 : 0);
        result = 31 * result + (execute ? 1 : 0);
        return result;
    }

    /**
     * Grants the role read permissions on the resource.
     *
     * @return Whether or not the role has read permissions on the resource.
     */
    public boolean read() {
        return read;
    }

    /**
     * Grants the role write permissions on the resource.
     *
     * @return Whether or not the role has write permissions on the resource.
     */
    public boolean write() {
        return write;
    }

    /**
     * Grants the role execute permissions on the resource.
     *
     * @return Whether or not the role has execute permissions on the resource.
     */
    public boolean execute() {
        return execute;
    }

    /**
     * Grants the role read permissions on the resource.
     *
     * @param read {@code true} if read permissions should be granted.
     * @return The updated RolePermissions object.
     */
    public RolePermissions read(boolean read) {
        this.read = read;
        return this;
    }

    /**
     * Grants the role execute permissions on the resource.
     *
     * @param write {@code true} if write permissions should be granted.
     * @return The updated RolePermissions object.
     */
    public RolePermissions write(boolean write) {
        this.write = write;

        return this;
    }

    /**
     * Grants the role execute permissions on the resource.
     *
     * @param execute {@code true} if execute permissions should be granted.
     * @return The updated RolePermissions object.
     */
    public RolePermissions execute(boolean execute) {
        this.execute = execute;
        return this;
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.StorageImplUtils;

import java.util.Locale;
import java.util.Objects;

/**
 * Represents POSIX-style permissions on a given resource. Each resource specifies permissions for the owner, the owning
 * group, and everyone else. Permissions for users or groups not included here can be set using an Access Control List.
 * Manipulating resource permissions is only supported when ADLS interop and Hierarchical Namespace are enabled.
 */
public class PathPermissions {

    private final ClientLogger logger = new ClientLogger(PathPermissions.class);

    private static final String PATH_PERMISSIONS_OCTAL_FORMAT_ERROR = "String cannot be null and must be four "
        + "characters (first bit--sticky bit--must be set to 0 for umask).";
    private static final String PATH_PERMISSIONS_SYMBOLIC_FORMAT_ERROR = "Invalid format. The only character that may "
        + "validly follow the permissions string is '+'.";

    /**
     * Permissions for the owner.
     */
    private RolePermissions owner;

    /**
     * Permissions for the owning group.
     */
    private RolePermissions group;

    /**
     * Permissions for everyone outside the owning group.
     */
    private RolePermissions other;

    /**
     * The sticky bit, when set, ensures that only the file owner or root user can delete or move a file, even if the
     * given user has write permissions.
     */
    private boolean stickyBit;

    /**
     * A flag to indicate whether there is more detailed permissions information contained in an ACL on the resource.
     */
    private boolean extendedInfoInAcl;

    /**
     * Initializes a new instance of {@code PathPermissions} by setting each member to a new instance of
     * {@link RolePermissions}.
     */
    public PathPermissions() {
        this.owner = new RolePermissions();
        this.group = new RolePermissions();
        this.other = new RolePermissions();
    }

    /**
     * Initializes a new instance of {@code PathPermissions} with the given values.
     *
     * @param owner The permissions given to the owner of the resource.
     * @param group The permissions given to the owning group of the resource.
     * @param other The permissions given to other users and groups.
     * @param stickyBit Sets the sticky bit. Please refer to the POSIX sticky bit for more information.
     */
    public PathPermissions(RolePermissions owner, RolePermissions group, RolePermissions other, boolean stickyBit) {
        this.owner = owner;
        this.group = group;
        this.other = other;
        this.stickyBit = stickyBit;
    }

    /**
     * Initializes a new instance of {@code PathPermissions} with copies of the fields from another instance.
     *
     * @param other The instance with values to copy.
     */
    public PathPermissions(PathPermissions other) {
        this.owner = new RolePermissions(other.owner);
        this.group = new RolePermissions(other.group);
        this.other = new RolePermissions(other.other);
        this.stickyBit = other.stickyBit;
        this.extendedInfoInAcl = other.extendedInfoInAcl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PathPermissions that = (PathPermissions) o;

        if (stickyBit != that.stickyBit) {
            return false;
        }
        if (extendedInfoInAcl != that.extendedInfoInAcl) {
            return false;
        }
        if (!Objects.equals(owner, that.owner)) {
            return false;
        }
        if (!Objects.equals(group, that.group)) {
            return false;
        }
        return Objects.equals(other, that.other);
    }

    @Override
    public int hashCode() {
        int result = owner != null ? owner.hashCode() : 0;
        result = 31 * result + (group != null ? group.hashCode() : 0);
        result = 31 * result + (other != null ? other.hashCode() : 0);
        result = 31 * result + (stickyBit ? 1 : 0);
        result = 31 * result + (extendedInfoInAcl ? 1 : 0);
        return result;
    }

    /**
     * Converts an octal string into a {@code PathPermissions} object.
     *
     * e.g. 1752
     * 0/1 in the first digit indicates sticky bit. Each subsequent octal character can be expanded into three bits.
     * In order of MSB to LSB, the bits represent read, write, execute.
     *
     * @param octal The octal representation of the permissions.
     * @return The permissions parsed out into a {@code PathPermissions} instance.
     * @throws IllegalArgumentException if the String does not match the format.
     */
    public static PathPermissions parseOctal(String octal) {
        StorageImplUtils.assertNotNull("octal", octal);
        if (octal.length() != 4) {
            throw new IllegalArgumentException(PATH_PERMISSIONS_OCTAL_FORMAT_ERROR);
        }
        PathPermissions res = new PathPermissions();
        res.stickyBit = octal.charAt(0) != '0';
        res.owner = RolePermissions.parseOctal(Integer.parseInt(octal.charAt(1) + ""));
        res.group = RolePermissions.parseOctal(Integer.parseInt(octal.charAt(2) + ""));
        res.other = RolePermissions.parseOctal(Integer.parseInt(octal.charAt(3) + ""));

        return res;
    }

    /**
     * Converts a symbolic representation of the permissions into a {@code PathPermissions} object.
     *
     * e.g. rwxr-x-wT
     * Each set of three characters correspondes to owner, owning group, and other respectively. 'r', 'w', and 'x'
     * respectively refer to read, write, and execute. A '-' indicates the permission is not given. The sticky bit, if
     * set, takes the place of the last execute bit; a 't' takes the place of 'x' and a 'T' takes the place of '-'.
     *
     * @param str The symbolic representation of the permissions.
     * @return The permissions parsed out into a {@code PathPermissions} instance.
     * @throws IllegalArgumentException if the String does not match the format.
     */
    public static PathPermissions parseSymbolic(String str) {
        StorageImplUtils.assertNotNull("str", str);
        StorageImplUtils.assertInBounds("str.length", str.length(), 9, 10);

        PathPermissions res = new PathPermissions();
        int stickyBitPos = 8; // sticky bit always replaces the last execute bit in symbolic notation.
        res.stickyBit = str.toLowerCase(Locale.ROOT).charAt(stickyBitPos) == 't';
        res.owner = RolePermissions.parseSymbolic(str.substring(0, 3), false);
        res.group = RolePermissions.parseSymbolic(str.substring(3, 6), false);
        res.other = RolePermissions.parseSymbolic(str.substring(6, 9), true);

        if (str.length() == 10) {
            if (str.charAt(9) == '+') {
                res.extendedInfoInAcl = true;
            } else {
                throw new IllegalArgumentException(PATH_PERMISSIONS_SYMBOLIC_FORMAT_ERROR);
            }
        } else {
            res.extendedInfoInAcl = false;
        }

        return res;
    }

    /**
     * Converts this object into its octal representation.
     *
     * @return A {@code String} that represents the permissions in octal.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.stickyBit) {
            sb.append(1);
        } else {
            sb.append(0);
        }
        sb.append(this.owner.toOctal());
        sb.append(this.group.toOctal());
        sb.append(this.other.toOctal());
        return sb.toString();
    }

    /**
     * Validates that there is no sticky bit set on the umask, as it is not supported.
     */
    void assertUmask() {
        if (this.stickyBit) {
            throw logger.logExceptionAsError(new IllegalArgumentException("umask cannot have a sticky bit."));
        }
    }

    /**
     * Returns the {@link RolePermissions} for the owner of the resource.
     *
     * @return the {@link RolePermissions} for the owner of the resource.
     */
    public RolePermissions getOwner() {
        return owner;
    }

    /**
     * Returns the {@link RolePermissions} for the owning group of the resource.
     *
     * @return the {@link RolePermissions} for the owning group of the resource.
     */
    public RolePermissions getGroup() {
        return group;
    }

    /**
     * Returns the {@link RolePermissions} for the other users.
     *
     * @return the {@link RolePermissions} for the other users.
     */
    public RolePermissions getOther() {
        return other;
    }

    /**
     * Sets the permissions for the owner of the resource.
     *
     * @param owner The {@link RolePermissions} that specify what permissions the owner should have.
     * @return The updated PathPermissions object.
     */
    public PathPermissions setOwner(RolePermissions owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Sets the permissions for the owning group of the resource.
     *
     * @param group The {@link RolePermissions} that specify what permissions the owning group should have.
     * @return The updated PathPermissions object.
     */
    public PathPermissions setGroup(RolePermissions group) {
        this.group = group;
        return this;
    }

    /**
     * Sets the permissions for the other users of the resource.
     *
     * @param other The {@link RolePermissions} that specify what permissions other users should have.
     * @return The updated PathPermissions object.
     */
    public PathPermissions setOther(RolePermissions other) {
        this.other = other;
        return this;
    }

    /**
     * Returns whether or not the sticky bit has been set. The sticky bit may be set on directories, the files in that
     * directory may only be renamed or deleted by the file's owner, the directory's owner, or the root user.
     *
     * @return {@code true} if the sticky bit is set and {@code false} otherwise.
     */
    public boolean hasStickyBit() {
        return stickyBit;
    }

    /**
     * Sets the value of the sticky bit. The sticky bit may be set on directories, the files in that
     * directory may only be renamed or deleted by the file's owner, the directory's owner, or the root user.
     *
     * @param hasStickyBit {@code True} to set the sticky bit and {@code false} to clear it.
     * @return The updated PathPermissions object.
     */
    public PathPermissions setStickyBit(boolean hasStickyBit) {
        this.stickyBit = hasStickyBit;
        return this;
    }

    /**
     * Returns whether or not there is more permissions information in the ACLs. The permissions string only returns
     * information on the owner, owning group, and other, but the ACLs may contain more permissions for specific users
     * or groups.
     *
     * @return {@code true} if there is more information in the ACL. {@code false} otherwise.
     */
    public boolean hasExtendedInfoInAcl() {
        return this.extendedInfoInAcl;
    }

    PathPermissions setExtendedInfoInAcl(boolean hasExtendedInfoInAcl) {
        this.extendedInfoInAcl = hasExtendedInfoInAcl;
        return this;
    }

}

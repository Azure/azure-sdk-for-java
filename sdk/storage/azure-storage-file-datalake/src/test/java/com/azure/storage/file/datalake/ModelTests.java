// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.storage.file.datalake.models.AccessControlType;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathPermissions;
import com.azure.storage.file.datalake.models.RolePermissions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.azure.storage.file.datalake.DataLakeTestBase.compareACL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelTests {
    @ParameterizedTest
    @CsvSource({ "1,--x", "2,-w-", "4,r--" })
    public void rolePermissionsParseOctal(int octal, String permission) {
        assertEquals(permission, RolePermissions.parseOctal(octal).toSymbolic());
    }

    @ParameterizedTest
    @CsvSource({"--x,1", "-w-,2", "r--,4"})
    public void rolePermissionsParseSymbolic(String symbol, int permission) {
        assertEquals(String.valueOf(permission), RolePermissions.parseSymbolic(symbol, false).toOctal());
    }

    @Test
    public void pathPermissionsParseSymbolic() {
        PathPermissions permissions = PathPermissions.parseSymbolic("r---w---t+");

        assertEquals(RolePermissions.parseOctal(4), permissions.getOwner());
        assertEquals(RolePermissions.parseOctal(2), permissions.getGroup());
        assertEquals(RolePermissions.parseOctal(1), permissions.getOther());
        assertTrue(permissions.isStickyBitSet());
        assertTrue(permissions.isExtendedInfoInAcl());
    }

    @Test
    public void pathPermissionsCreate() {
        PathPermissions permissions = PathPermissions.parseSymbolic("r---w---t+");

        assertEquals(RolePermissions.parseOctal(4), permissions.getOwner());
        assertEquals(RolePermissions.parseOctal(2), permissions.getGroup());
        assertEquals(RolePermissions.parseOctal(1), permissions.getOther());
        assertTrue(permissions.isStickyBitSet());
        assertTrue(permissions.isExtendedInfoInAcl());
    }

    @ParameterizedTest
    @CsvSource({"rwxrwxrwT,false,true,true,true,false", "rwxrwxrwx,true,true,true,false,false",
        "rwxrwxrw-,false,true,true,false,false"})
    public void pathPermissionsParse(String symbol, boolean execute, boolean read, boolean write, boolean stickyBit,
        boolean extendedInfoInAcl) {
        PathPermissions permissions = PathPermissions.parseSymbolic(symbol);

        assertEquals(execute, permissions.getOther().hasExecutePermission());
        assertEquals(read, permissions.getOther().hasReadPermission());
        assertEquals(write, permissions.getOther().hasWritePermission());
        assertEquals(stickyBit, permissions.isStickyBitSet());
        assertEquals(extendedInfoInAcl, permissions.isExtendedInfoInAcl());
    }

    @ParameterizedTest
    @MethodSource("pathPermissionsParseOctalSupplier")
    public void pathPermissionsParseOctal(String octal, RolePermissions owner, RolePermissions group,
        RolePermissions other, boolean stickyBit) {
        PathPermissions permissions = PathPermissions.parseOctal(octal);

        if (owner != null) {
            assertEquals(owner, permissions.getOwner());
        }

        if (group != null) {
            assertEquals(group, permissions.getGroup());
        }

        if (other != null) {
            assertEquals(other, permissions.getOther());
        }

        assertEquals(stickyBit, permissions.isStickyBitSet());
    }

    private static Stream<Arguments> pathPermissionsParseOctalSupplier() {
        return Stream.of(
            // octal, owner, group, other, stickyBit
            Arguments.of("1421", RolePermissions.parseOctal(4), RolePermissions.parseOctal(2),
                RolePermissions.parseOctal(1), true),
            Arguments.of("0123", null, null, null, false)
        );
    }

    @Test
    public void pathAccessControlEntry() {
        PathAccessControlEntry entry = new PathAccessControlEntry()
            .setAccessControlType(AccessControlType.GROUP)
            .setPermissions(RolePermissions.parseOctal(0))
            .setDefaultScope(true)
            .setEntityId("a");
        PathAccessControlEntry fromStr = PathAccessControlEntry.parse("default:group:a:---");

        assertTrue(entry.isInDefaultScope());
        assertEquals(AccessControlType.GROUP, entry.getAccessControlType());
        assertEquals("a", entry.getEntityId());
        assertEquals(RolePermissions.parseOctal(0), entry.getPermissions());
        assertEquals("default:group:a:---", entry.toString());
        assertEquals(fromStr, entry);

        entry = new PathAccessControlEntry()
            .setAccessControlType(AccessControlType.MASK)
            .setPermissions(RolePermissions.parseOctal(4))
            .setDefaultScope(false)
            .setEntityId(null);
        fromStr = PathAccessControlEntry.parse("mask::r--");

        assertEquals("mask::r--", entry.toString());
        assertEquals(fromStr, entry);

        entry = new PathAccessControlEntry()
            .setAccessControlType(AccessControlType.USER)
            .setPermissions(RolePermissions.parseOctal(2))
            .setDefaultScope(false)
            .setEntityId("b");

        assertEquals("user:b:-w-", entry.toString());
    }

    @Test
    public void pathAccessControlEntryList() {
        List<PathAccessControlEntry> acl = new ArrayList<>();
        acl.add(new PathAccessControlEntry()
            .setAccessControlType(AccessControlType.USER)
            .setPermissions(RolePermissions.parseOctal(1))
            .setDefaultScope(true)
            .setEntityId("c"));
        acl.add(new PathAccessControlEntry()
            .setAccessControlType(AccessControlType.OTHER)
            .setPermissions(RolePermissions.parseOctal(7))
            .setDefaultScope(false)
            .setEntityId(null));
        List<PathAccessControlEntry> listFromStr = PathAccessControlEntry.parseList("default:user:c:--x,other::rwx");

        assertEquals("default:user:c:--x,other::rwx", PathAccessControlEntry.serializeList(acl));
        compareACL(acl, listFromStr);
    }
}

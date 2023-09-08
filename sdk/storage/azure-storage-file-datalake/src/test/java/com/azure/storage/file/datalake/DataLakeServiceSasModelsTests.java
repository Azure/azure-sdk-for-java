// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.util.Context;
import com.azure.storage.file.datalake.implementation.util.DataLakeSasImplUtil;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;
import com.azure.storage.file.datalake.sas.FileSystemSasPermission;
import com.azure.storage.file.datalake.sas.PathSasPermission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataLakeServiceSasModelsTests {
    @ParameterizedTest
    @MethodSource("sasPermissionsToStringSupplier")
    public void pathSasPermissionsToString(boolean read, boolean write, boolean delete, boolean create, boolean add,
        boolean list, boolean move, boolean execute, boolean owner, boolean permission, String expectedString) {
        PathSasPermission perms = new PathSasPermission()
            .setReadPermission(read)
            .setWritePermission(write)
            .setDeletePermission(delete)
            .setCreatePermission(create)
            .setAddPermission(add)
            .setListPermission(list)
            .setMovePermission(move)
            .setExecutePermission(execute)
            .setManageOwnershipPermission(owner)
            .setManageAccessControlPermission(permission);

        assertEquals(expectedString, perms.toString());
    }

    private static Stream<Arguments> sasPermissionsToStringSupplier() {
        return Stream.of(
            // read | write | delete | create | add | list | move | execute | owner | permission || expectedString
            Arguments.of(true, false, false, false, false, false, false, false, false, false, "r"),
            Arguments.of(false, true, false, false, false, false, false, false, false, false, "w"),
            Arguments.of(false, false, true, false, false, false, false, false, false, false, "d"),
            Arguments.of(false, false, false, true, false, false, false, false, false, false, "c"),
            Arguments.of(false, false, false, false, true, false, false, false, false, false, "a"),
            Arguments.of(false, false, false, false, false, true, false, false, false, false, "l"),
            Arguments.of(false, false, false, false, false, false, true, false, false, false, "m"),
            Arguments.of(false, false, false, false, false, false, false, true, false, false, "e"),
            Arguments.of(false, false, false, false, false, false, false, false, true, false, "o"),
            Arguments.of(false, false, false, false, false, false, false, false, false, true, "p"),
            Arguments.of(true, true, true, true, true, true, true, true, true, true, "racwdlmeop")
        );
    }

    @ParameterizedTest
    @MethodSource("sasPermissionsParseSupplier")
    public void pathSasPermissionsParse(String permString, boolean read, boolean write, boolean delete, boolean create,
        boolean add, boolean list, boolean move, boolean execute, boolean owner, boolean permission) {
        PathSasPermission perms = PathSasPermission.parse(permString);

        assertEquals(read, perms.hasReadPermission());
        assertEquals(write, perms.hasWritePermission());
        assertEquals(delete, perms.hasDeletePermission());
        assertEquals(create, perms.hasCreatePermission());
        assertEquals(add, perms.hasAddPermission());
        assertEquals(list, perms.hasListPermission());
        assertEquals(move, perms.hasMovePermission());
        assertEquals(execute, perms.hasExecutePermission());
        assertEquals(owner, perms.hasManageOwnershipPermission());
        assertEquals(permission, perms.hasManageAccessControlPermission());
    }

    private static Stream<Arguments> sasPermissionsParseSupplier() {
        return Stream.of(
            // permString || read  | write | delete | create | add   | list  | move  | execute | owner | permission
            Arguments.of("r", true, false, false, false, false, false, false, false, false, false),
            Arguments.of("w", false, true, false, false, false, false, false, false, false, false),
            Arguments.of("d", false, false, true, false, false, false, false, false, false, false),
            Arguments.of("c", false, false, false, true, false, false, false, false, false, false),
            Arguments.of("a", false, false, false, false, true, false, false, false, false, false),
            Arguments.of("l", false, false, false, false, false, true, false, false, false, false),
            Arguments.of("m", false, false, false, false, false, false, true, false, false, false),
            Arguments.of("e", false, false, false, false, false, false, false, true, false, false),
            Arguments.of("o", false, false, false, false, false, false, false, false, true, false),
            Arguments.of("p", false, false, false, false, false, false, false, false, false, true),
            Arguments.of("racwdlmeop", true, true, true, true, true, true, true, true, true, true),
            Arguments.of("malwdcrepo", true, true, true, true, true, true, true, true, true, true)
        );
    }

    @Test
    public void pathSasPermissionsParseIA() {
        assertThrows(IllegalArgumentException.class, () -> PathSasPermission.parse("rwaq"));
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void pathSasPermissionNull() {
        assertThrows(NullPointerException.class, () -> PathSasPermission.parse(null));
    }

    @ParameterizedTest
    @MethodSource("sasPermissionsToStringSupplier")
    public void fileSystemSasPermissionsToString(boolean read, boolean write, boolean delete, boolean create,
        boolean add, boolean list, boolean move, boolean execute, boolean owner, boolean permission,
        String expectedString) {
        FileSystemSasPermission perms = new FileSystemSasPermission()
            .setReadPermission(read)
            .setWritePermission(write)
            .setDeletePermission(delete)
            .setCreatePermission(create)
            .setAddPermission(add)
            .setListPermission(list)
            .setMovePermission(move)
            .setExecutePermission(execute)
            .setManageOwnershipPermission(owner)
            .setManageAccessControlPermission(permission);

        assertEquals(expectedString, perms.toString());
    }

    @ParameterizedTest
    @MethodSource("sasPermissionsParseSupplier")
    public void fileSystemSasPermissionsParse(String permString, boolean read, boolean write, boolean delete,
        boolean create, boolean add, boolean list, boolean move, boolean execute, boolean owner, boolean permission) {
        FileSystemSasPermission perms = FileSystemSasPermission.parse(permString);

        assertEquals(read, perms.hasReadPermission());
        assertEquals(write, perms.hasWritePermission());
        assertEquals(delete, perms.hasDeletePermission());
        assertEquals(create, perms.hasCreatePermission());
        assertEquals(add, perms.hasAddPermission());
        assertEquals(list, perms.hasListPermission());
        assertEquals(move, perms.hasMovePermission());
        assertEquals(execute, perms.hasExecutePermission());
        assertEquals(owner, perms.hasManageOwnershipPermission());
        assertEquals(permission, perms.hasManageAccessControlPermission());
    }

    @Test
    public void fileSystemSasPermissionsParseIA() {
        assertThrows(IllegalArgumentException.class, () -> FileSystemSasPermission.parse("rwaq"));
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void fileSystemSasPermissionNull() {
        assertThrows(NullPointerException.class, () -> FileSystemSasPermission.parse(null));
    }

    @Test
    public void pathSasImplUtilNull() {
        DataLakeServiceSasSignatureValues v = new DataLakeServiceSasSignatureValues(
            OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), new PathSasPermission().setReadPermission(true));
        DataLakeSasImplUtil implUtil = new DataLakeSasImplUtil(v, "containerName", "blobName", false);

        NullPointerException ex = assertThrows(NullPointerException.class, () -> implUtil.generateSas(null, Context.NONE));
        assertTrue(ex.getMessage().contains("storageSharedKeyCredential"));

        ex = assertThrows(NullPointerException.class, () ->  implUtil.generateUserDelegationSas(null, "accountName", Context.NONE));
        assertTrue(ex.getMessage().contains("delegationKey"));

        ex = assertThrows(NullPointerException.class, () ->  implUtil.generateUserDelegationSas(new UserDelegationKey(), null, Context.NONE));
        assertTrue(ex.getMessage().contains("accountName"));
    }

    @ParameterizedTest
    @MethodSource("ensureStateResourceAndPermissionSupplier")
    public void ensureStateResourceAndPermission(String container, String blob, boolean isDirectory,
        Object permission, String resource, String permissionString, Integer directoryDepth) {
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        DataLakeSasImplUtil implUtil = (permission instanceof PathSasPermission)
            ? new DataLakeSasImplUtil(new DataLakeServiceSasSignatureValues(expiryTime, (PathSasPermission) permission), container, blob, isDirectory)
            : new DataLakeSasImplUtil(new DataLakeServiceSasSignatureValues(expiryTime, (FileSystemSasPermission) permission), container, blob, isDirectory);

        implUtil.ensureState();

        assertEquals(resource, implUtil.getResource());
        assertEquals(permissionString, implUtil.getPermissions());
        assertEquals(directoryDepth, implUtil.getDirectoryDepth());

    }

    private static Stream<Arguments> ensureStateResourceAndPermissionSupplier() {
        return Stream.of(
            // container, blob, isDirectory, permission, resource, permissionString, directoryDepth
            Arguments.of("container", null, false,
                new FileSystemSasPermission().setReadPermission(true).setListPermission(true), "c", "rl", null),
            Arguments.of("container", "blob", false, new PathSasPermission().setReadPermission(true), "b", "r", null),
            Arguments.of("container", "/", true, new PathSasPermission().setReadPermission(true), "d", "r", 0),
            Arguments.of("container", "blob/", true, new PathSasPermission().setReadPermission(true), "d", "r", 1),
            Arguments.of("container", "blob/dir1", true, new PathSasPermission().setReadPermission(true), "d", "r", 2),
            Arguments.of("container", "blob/dir1/dir2", true, new PathSasPermission().setReadPermission(true), "d", "r",
                3)
        );

    }

    @Test
    public void ensureStateAadIdIllegalState() {
        OffsetDateTime e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        FileSystemSasPermission p = new FileSystemSasPermission().setReadPermission(true).setListPermission(true);

        DataLakeServiceSasSignatureValues v = new DataLakeServiceSasSignatureValues(e, p)
            .setPreauthorizedAgentObjectId("authorizedId")
            .setAgentObjectId("unauthorizedId");
        DataLakeSasImplUtil implUtil = new DataLakeSasImplUtil(v, "containerName", "blobName", true);

        assertThrows(IllegalStateException.class, implUtil::ensureState);
    }
}

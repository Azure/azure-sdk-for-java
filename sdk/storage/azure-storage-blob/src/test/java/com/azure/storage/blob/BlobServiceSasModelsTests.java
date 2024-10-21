// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.Context;
import com.azure.storage.blob.implementation.util.BlobSasImplUtil;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
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

public class BlobServiceSasModelsTests extends BlobTestBase {

    @ParameterizedTest
    @MethodSource("blobSASPermissionsToStringSupplier")
    public void blobSASPermissionsToString(boolean read, boolean write, boolean delete, boolean create, boolean add,
        boolean deleteVersion, boolean tags, boolean list, boolean move, boolean execute, boolean setImmutabilityPolicy,
        String expectedString) {
        BlobSasPermission perms = new BlobSasPermission()
            .setReadPermission(read)
            .setWritePermission(write)
            .setDeletePermission(delete)
            .setCreatePermission(create)
            .setAddPermission(add)
            .setDeleteVersionPermission(deleteVersion)
            .setTagsPermission(tags)
            .setListPermission(list)
            .setMovePermission(move)
            .setExecutePermission(execute)
            .setImmutabilityPolicyPermission(setImmutabilityPolicy);

        assertEquals(perms.toString(), expectedString);
    }

    private static Stream<Arguments> blobSASPermissionsToStringSupplier() {
        return Stream.of(
            Arguments.of(true, false, false, false, false, false, false, false, false, false, false, "r"),
            Arguments.of(false, true, false, false, false, false, false, false, false, false, false, "w"),
            Arguments.of(false, false, true, false, false, false, false, false, false, false, false, "d"),
            Arguments.of(false, false, false, true, false, false, false, false, false, false, false, "c"),
            Arguments.of(false, false, false, false, true, false, false, false, false, false, false, "a"),
            Arguments.of(false, false, false, false, false, true, false, false, false, false, false, "x"),
            Arguments.of(false, false, false, false, false, false, true, false, false, false, false, "t"),
            Arguments.of(false, false, false, false, false, false, false, true, false, false, false, "l"),
            Arguments.of(false, false, false, false, false, false, false, false, true, false, false, "m"),
            Arguments.of(false, false, false, false, false, false, false, false, false, true, false, "e"),
            Arguments.of(false, false, false, false, false, false, false, false, false, false, true, "i"),
            Arguments.of(true, true, true, true, true, true, true, true, true, true, true, "racwdxltmei")
            );
    }

    @ParameterizedTest
    @MethodSource("blobSASPermissionsParseSupplier")
    public void blobSASPermissionsParse(boolean read, boolean write, boolean delete, boolean create, boolean add,
        boolean deleteVersion, boolean tags, boolean list, boolean move, boolean execute, boolean setImmutabilityPolicy,
        String expectedString) {
        BlobSasPermission perms = BlobSasPermission.parse(expectedString);

        assertEquals(read, perms.hasReadPermission());
        assertEquals(write, perms.hasWritePermission());
        assertEquals(delete, perms.hasDeletePermission());
        assertEquals(create, perms.hasCreatePermission());
        assertEquals(add, perms.hasAddPermission());
        assertEquals(deleteVersion, perms.hasDeleteVersionPermission());
        assertEquals(tags, perms.hasTagsPermission());
        assertEquals(list, perms.hasListPermission());
        assertEquals(move, perms.hasMovePermission());
        assertEquals(execute, perms.hasExecutePermission());
        assertEquals(setImmutabilityPolicy, perms.hasImmutabilityPolicyPermission());
    }

    private static Stream<Arguments> blobSASPermissionsParseSupplier() {
        return Stream.of(
            Arguments.of(true, false, false, false, false, false, false, false, false, false, false, "r"),
            Arguments.of(false, true, false, false, false, false, false, false, false, false, false, "w"),
            Arguments.of(false, false, true, false, false, false, false, false, false, false, false, "d"),
            Arguments.of(false, false, false, true, false, false, false, false, false, false, false, "c"),
            Arguments.of(false, false, false, false, true, false, false, false, false, false, false, "a"),
            Arguments.of(false, false, false, false, false, true, false, false, false, false, false, "x"),
            Arguments.of(false, false, false, false, false, false, true, false, false, false, false, "t"),
            Arguments.of(false, false, false, false, false, false, false, true, false, false, false, "l"),
            Arguments.of(false, false, false, false, false, false, false, false, true, false, false, "m"),
            Arguments.of(false, false, false, false, false, false, false, false, false, true, false, "e"),
            Arguments.of(false, false, false, false, false, false, false, false, false, false, true, "i"),
            Arguments.of(true, true, true, true, true, true, true, true, true, true, true, "racwdxltmei"),
            Arguments.of(true, true, true, true, true, true, true, true, true, true, true, "dtcxiewlrma")
        );
    }

    @Test
    public void blobSASPermissionsParseIA() {
        assertThrows(IllegalArgumentException.class, () -> BlobSasPermission.parse("rwaq"));
    }

    @Test
    public void blobSasPermissionNull() {
        assertThrows(NullPointerException.class, () -> BlobSasPermission.parse(null));
    }

    @ParameterizedTest
    @MethodSource("blobSASPermissionsToStringSupplier")
    public void containerSASPermissionsToString(boolean read, boolean write, boolean delete, boolean create, boolean add,
        boolean deleteVersion, boolean tags, boolean list, boolean move, boolean execute, boolean setImmutabilityPolicy,
        String expectedString) {
        BlobContainerSasPermission perms = new BlobContainerSasPermission()
            .setReadPermission(read)
            .setWritePermission(write)
            .setDeletePermission(delete)
            .setCreatePermission(create)
            .setAddPermission(add)
            .setListPermission(list)
            .setDeleteVersionPermission(deleteVersion)
            .setTagsPermission(tags)
            .setMovePermission(move)
            .setExecutePermission(execute)
            .setImmutabilityPolicyPermission(setImmutabilityPolicy);

        assertEquals(perms.toString(), expectedString);
    }

    @ParameterizedTest
    @MethodSource("blobSASPermissionsParseSupplier")
    public void containerSASPermissionsParse(boolean read, boolean write, boolean delete, boolean create, boolean add,
        boolean deleteVersion, boolean tags, boolean list, boolean move, boolean execute, boolean setImmutabilityPolicy,
        String expectedString) {
        BlobContainerSasPermission perms = BlobContainerSasPermission.parse(expectedString);

        assertEquals(read, perms.hasReadPermission());
        assertEquals(write, perms.hasWritePermission());
        assertEquals(delete, perms.hasDeletePermission());
        assertEquals(create, perms.hasCreatePermission());
        assertEquals(add, perms.hasAddPermission());
        assertEquals(deleteVersion, perms.hasDeleteVersionPermission());
        assertEquals(tags, perms.hasTagsPermission());
        assertEquals(list, perms.hasListPermission());
        assertEquals(move, perms.hasMovePermission());
        assertEquals(execute, perms.hasExecutePermission());
        assertEquals(setImmutabilityPolicy, perms.hasImmutabilityPolicyPermission());
    }

    @Test
    public void containerSASPermissionsParseIA() {
        assertThrows(IllegalArgumentException.class, () -> BlobContainerSasPermission.parse("rwaq"));
    }

    @Test
    public void containerSASPermissionsNull() {
        assertThrows(NullPointerException.class, () -> BlobContainerSasPermission.parse(null));
    }

    @Test
    public void blobSasImplUtilNull() {
        OffsetDateTime e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0,
            ZoneOffset.UTC);
        BlobSasPermission p = new BlobSasPermission().setReadPermission(true);
        BlobServiceSasSignatureValues v = new BlobServiceSasSignatureValues(e, p);
        BlobSasImplUtil implUtil = new BlobSasImplUtil(v, "containerName", "blobName",
            null, null, null);

        NullPointerException ex = assertThrows(NullPointerException.class, () ->
            implUtil.generateSas(null, Context.NONE));
        assertTrue(ex.getMessage().contains("storageSharedKeyCredential"));

        ex = assertThrows(NullPointerException.class, () ->
            implUtil.generateUserDelegationSas(null, "accountName", Context.NONE));
        assertTrue(ex.getMessage().contains("delegationKey"));

        ex = assertThrows(NullPointerException.class, () ->
                implUtil.generateUserDelegationSas(new UserDelegationKey(), null, Context.NONE));

        assertTrue(ex.getMessage().contains("accountName"));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void ensureStateIllegalArgument() {
        BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues();
        BlobSasImplUtil implUtil = new BlobSasImplUtil(sasSignatureValues, null);
        assertThrows(IllegalStateException.class, () ->
            implUtil.generateSas(ENVIRONMENT.getPrimaryAccount().getCredential(), Context.NONE));
    }

    @ParameterizedTest
    @MethodSource("ensureStateResourceAndPermissionSupplier")
    public void ensureStateResourceAndPermission(String container, String blob, String snapshot, String versionId,
        BlobContainerSasPermission blobContainerSasPermission, BlobSasPermission blobSasPermission, String resource,
        String permissionString) {
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);

        BlobServiceSasSignatureValues values = blobContainerSasPermission != null
            ? new BlobServiceSasSignatureValues(expiryTime, blobContainerSasPermission)
            : new BlobServiceSasSignatureValues(expiryTime, blobSasPermission);

        BlobSasImplUtil implUtil = new BlobSasImplUtil(values, container, blob, snapshot, versionId, null);
        implUtil.ensureState();
        assertEquals(resource, implUtil.getResource());
        assertEquals(permissionString, implUtil.getPermissions());
    }

    private static Stream<Arguments> ensureStateResourceAndPermissionSupplier() {
        return Stream.of(Arguments.of("container", null, null, null, new BlobContainerSasPermission().setReadPermission(true).setListPermission(true), null, "c", "rl"),
            Arguments.of("container", "blob", null, null, null, new BlobSasPermission().setReadPermission(true), "b", "r"),
            Arguments.of("container", "blob", "snapshot", null, null, new BlobSasPermission().setReadPermission(true), "bs", "r"),
            Arguments.of("container", "blob", null, "version", null, new BlobSasPermission().setReadPermission(true), "bv", "r"));
    }
}

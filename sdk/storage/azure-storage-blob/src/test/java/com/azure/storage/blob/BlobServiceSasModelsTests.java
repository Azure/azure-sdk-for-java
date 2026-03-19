// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.Context;
import com.azure.storage.blob.implementation.util.BlobSasImplUtil;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.sas.CommonSasQueryParameters;
import com.azure.storage.common.sas.SasIpRange;
import com.azure.storage.common.sas.SasProtocol;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlobServiceSasModelsTests extends BlobTestBase {

    @ParameterizedTest
    @MethodSource("blobSASPermissionsToStringSupplier")
    public void blobSASPermissionsToString(boolean read, boolean write, boolean delete, boolean create, boolean add,
        boolean deleteVersion, boolean tags, boolean list, boolean move, boolean execute, boolean setImmutabilityPolicy,
        String expectedString) {
        BlobSasPermission perms = new BlobSasPermission().setReadPermission(read)
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
        return Stream.of(Arguments.of(true, false, false, false, false, false, false, false, false, false, false, "r"),
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
            Arguments.of(true, true, true, true, true, true, true, true, true, true, true, "racwdxltmei"));
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
        return Stream.of(Arguments.of(true, false, false, false, false, false, false, false, false, false, false, "r"),
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
            Arguments.of(true, true, true, true, true, true, true, true, true, true, true, "dtcxiewlrma"));
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
    public void containerSASPermissionsToString(boolean read, boolean write, boolean delete, boolean create,
        boolean add, boolean deleteVersion, boolean tags, boolean list, boolean move, boolean execute,
        boolean setImmutabilityPolicy, String expectedString) {
        BlobContainerSasPermission perms = new BlobContainerSasPermission().setReadPermission(read)
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
        OffsetDateTime e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        BlobSasPermission p = new BlobSasPermission().setReadPermission(true);
        BlobServiceSasSignatureValues v = new BlobServiceSasSignatureValues(e, p);
        BlobSasImplUtil implUtil = new BlobSasImplUtil(v, "containerName", "blobName", null, null, null);

        NullPointerException ex
            = assertThrows(NullPointerException.class, () -> implUtil.generateSas(null, Context.NONE));
        assertTrue(ex.getMessage().contains("storageSharedKeyCredential"));

        ex = assertThrows(NullPointerException.class,
            () -> implUtil.generateUserDelegationSas(null, "accountName", Context.NONE));
        assertTrue(ex.getMessage().contains("delegationKey"));

        ex = assertThrows(NullPointerException.class,
            () -> implUtil.generateUserDelegationSas(new UserDelegationKey(), null, Context.NONE));

        assertTrue(ex.getMessage().contains("accountName"));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void ensureStateIllegalArgument() {
        BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues();
        BlobSasImplUtil implUtil = new BlobSasImplUtil(sasSignatureValues, null);
        assertThrows(IllegalStateException.class,
            () -> implUtil.generateSas(ENVIRONMENT.getPrimaryAccount().getCredential(), Context.NONE));
    }

    @ParameterizedTest
    @MethodSource("ensureStateResourceAndPermissionSupplier")
    public void ensureStateResourceAndPermission(String container, String blob, String snapshot, String versionId,
        boolean isDirectory, Integer directoryDepth, BlobContainerSasPermission blobContainerSasPermission,
        BlobSasPermission blobSasPermission, String resource, String permissionString) {
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);

        BlobServiceSasSignatureValues values = blobContainerSasPermission != null
            ? new BlobServiceSasSignatureValues(expiryTime, blobContainerSasPermission).setDirectory(isDirectory)
            : new BlobServiceSasSignatureValues(expiryTime, blobSasPermission).setDirectory(isDirectory);

        BlobSasImplUtil implUtil = new BlobSasImplUtil(values, container, blob, snapshot, versionId, null);
        implUtil.ensureState();

        assertEquals(resource, implUtil.getResource());
        assertEquals(permissionString, implUtil.getPermissions());
        assertEquals(directoryDepth, implUtil.getDirectoryDepth());
    }

    private static Stream<Arguments> ensureStateResourceAndPermissionSupplier() {
        return Stream.of(
            // container, blob, snapshot, versionId, isDirectory, directoryDepth, containerSasPermission, 
            // blobSasPermission, resource, permissionString
            Arguments.of("container", null, null, null, false, null,
                new BlobContainerSasPermission().setReadPermission(true).setListPermission(true), null, "c", "rl"),
            Arguments.of("container", "blob", null, null, false, null, null,
                new BlobSasPermission().setReadPermission(true), "b", "r"),
            Arguments.of("container", "blob", "snapshot", null, false, null, null,
                new BlobSasPermission().setReadPermission(true), "bs", "r"),
            Arguments.of("container", "blob", null, "version", false, null, null,
                new BlobSasPermission().setReadPermission(true), "bv", "r"),
            Arguments.of("container", "foo/bar/hello", null, null, true, 3, null,
                new BlobSasPermission().setReadPermission(true), "d", "r"),
            Arguments.of("container", "foo/bar", null, null, true, 2, null,
                new BlobSasPermission().setReadPermission(true), "d", "r"),
            Arguments.of("container", "foo/", null, null, true, 1, null,
                new BlobSasPermission().setReadPermission(true), "d", "r"),
            Arguments.of("container", "/", null, null, true, 0, null, new BlobSasPermission().setReadPermission(true),
                "d", "r"));
    }

    /**
     * Validates encoded query parameters for a directory scoped blob SAS signed with the account key.
     */
    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-02-10")
    @Test
    public void toSasQueryParametersDirectoryTest() {
        String containerName = generateContainerName();
        String blobName = "foo/bar/hello";

        OffsetDateTime start = OffsetDateTime.of(2020, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC);
        OffsetDateTime expiry = OffsetDateTime.of(2020, 1, 3, 3, 4, 5, 0, ZoneOffset.UTC);
        SasIpRange ipRange = new SasIpRange().setIpMin("1.1.1.1").setIpMax("2.2.2.2");

        BlobSasPermission permissions = getAllBlobSasPermissions();

        BlobServiceSasSignatureValues sasValues
            = new BlobServiceSasSignatureValues(expiry, permissions).setDirectory(true)
                .setIdentifier("myidentifier")
                .setStartTime(start)
                .setProtocol(SasProtocol.HTTPS_HTTP)
                .setSasIpRange(ipRange)
                .setCacheControl("cache")
                .setContentDisposition("disposition")
                .setContentEncoding("encoding")
                .setContentLanguage("language")
                .setContentType("type");

        BlobSasImplUtil implUtil = new BlobSasImplUtil(sasValues, containerName, blobName, null, null, null);

        List<String> stringToSignHolder = new ArrayList<>();
        String sasToken = implUtil.generateSas(ENVIRONMENT.getPrimaryAccount().getCredential(), stringToSignHolder::add,
            Context.NONE);
        assertEquals(1, stringToSignHolder.size());
        assertNotNull(stringToSignHolder.get(0));

        CommonSasQueryParameters qp
            = BlobUrlParts.parse("https://account.blob.core.windows.net/c?" + sasToken).getCommonSasQueryParameters();

        String expectedSig = ENVIRONMENT.getPrimaryAccount().getCredential().computeHmac256(stringToSignHolder.get(0));

        assertEquals(Constants.SAS_SERVICE_VERSION, qp.getVersion());
        assertNull(qp.getServices());
        assertNull(qp.getResourceTypes());
        assertEquals(SasProtocol.HTTPS_HTTP, qp.getProtocol());
        assertEquals(start, qp.getStartTime());
        assertEquals(expiry, qp.getExpiryTime());
        assertEquals(ipRange.getIpMin(), qp.getSasIpRange().getIpMin());
        assertEquals(ipRange.getIpMax(), qp.getSasIpRange().getIpMax());
        assertEquals("myidentifier", qp.getIdentifier());
        assertEquals("d", qp.getResource());
        assertEquals(3, qp.getDirectoryDepth());
        assertEquals(permissions.toString(), qp.getPermissions());
        assertEquals(expectedSig, qp.getSignature());
        assertEquals("cache", qp.getCacheControl());
        assertEquals("disposition", qp.getContentDisposition());
        assertEquals("encoding", qp.getContentEncoding());
        assertEquals("language", qp.getContentLanguage());
        assertEquals("type", qp.getContentType());
    }

    /**
     * Validates encoded query parameters for a directory scoped user delegation SAS, including delegated OID and request header/query key lists.
     */
    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-02-10")
    @Test
    public void toSasQueryParametersDirectoryIdentityTest() {
        String containerName = generateContainerName();
        String blobName = "foo/bar/hello";
        String accountName = ENVIRONMENT.getPrimaryAccount().getName();

        OffsetDateTime start = OffsetDateTime.of(2020, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC);
        OffsetDateTime expiry = OffsetDateTime.of(2020, 1, 3, 3, 4, 5, 0, ZoneOffset.UTC);
        OffsetDateTime keyStart = OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime keyExpiry = OffsetDateTime.of(2020, 1, 10, 0, 0, 0, 0, ZoneOffset.UTC);

        SasIpRange ipRange = new SasIpRange().setIpMin("1.1.1.1").setIpMax("2.2.2.2");

        BlobSasPermission permissions = getAllBlobSasPermissions();

        Map<String, String> requestHeaders = new TreeMap<>();
        requestHeaders.put("a-header", "a-value");
        requestHeaders.put("b-header", "b-value");

        Map<String, String> requestQueryParams = new TreeMap<>();
        requestQueryParams.put("q-one", "1");
        requestQueryParams.put("q-two", "2");

        String delegatedOid = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
        String delegatedKeyTid = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";

        BlobServiceSasSignatureValues sasValues
            = new BlobServiceSasSignatureValues(expiry, permissions).setDirectory(true)
                .setStartTime(start)
                .setProtocol(SasProtocol.HTTPS_HTTP)
                .setSasIpRange(ipRange)
                .setDelegatedUserObjectId(delegatedOid)
                .setRequestHeaders(requestHeaders)
                .setRequestQueryParameters(requestQueryParams)
                .setCacheControl("cache")
                .setContentDisposition("disposition")
                .setContentEncoding("encoding")
                .setContentLanguage("language")
                .setContentType("type");

        UserDelegationKey key = new UserDelegationKey().setSignedObjectId("keyOid")
            .setSignedTenantId("keyTid")
            .setSignedStart(keyStart)
            .setSignedExpiry(keyExpiry)
            .setSignedService("b")
            .setSignedVersion("2019-02-02")
            .setSignedDelegatedUserTenantId(delegatedKeyTid)
            .setValue(ENVIRONMENT.getPrimaryAccount().getKey());

        BlobSasImplUtil implUtil = new BlobSasImplUtil(sasValues, containerName, blobName, null, null, null);

        List<String> stringToSignHolder = new ArrayList<>();
        String sasToken = implUtil.generateUserDelegationSas(key, accountName, stringToSignHolder::add, Context.NONE);
        assertEquals(1, stringToSignHolder.size());
        assertNotNull(stringToSignHolder.get(0));

        CommonSasQueryParameters qp
            = BlobUrlParts.parse("https://account.blob.core.windows.net/c?" + sasToken).getCommonSasQueryParameters();

        String expectedSig = StorageImplUtils.computeHMac256(key.getValue(), stringToSignHolder.get(0));

        assertEquals(Constants.SAS_SERVICE_VERSION, qp.getVersion());
        assertNull(qp.getServices());
        assertNull(qp.getResourceTypes());
        assertEquals(SasProtocol.HTTPS_HTTP, qp.getProtocol());
        assertEquals(start, qp.getStartTime());
        assertEquals(expiry, qp.getExpiryTime());
        assertEquals(ipRange.getIpMin(), qp.getSasIpRange().getIpMin());
        assertEquals(ipRange.getIpMax(), qp.getSasIpRange().getIpMax());
        assertNull(qp.getIdentifier());
        assertEquals("keyOid", qp.getKeyObjectId());
        assertEquals("keyTid", qp.getKeyTenantId());
        assertEquals(keyStart, qp.getKeyStart());
        assertEquals(keyExpiry, qp.getKeyExpiry());
        assertEquals("b", qp.getKeyService());
        assertEquals("2019-02-02", qp.getKeyVersion());
        assertEquals(delegatedKeyTid, qp.getKeyDelegatedUserTenantId());
        assertEquals("d", qp.getResource());
        assertEquals(3, qp.getDirectoryDepth());
        assertEquals(permissions.toString(), qp.getPermissions());
        assertEquals(delegatedOid, qp.getDelegatedUserObjectId());
        assertEquals(Arrays.asList("a-header", "b-header"), qp.getRequestHeaders());
        assertEquals(Arrays.asList("q-one", "q-two"), qp.getRequestQueryParameters());
        assertEquals(expectedSig, qp.getSignature());
        assertEquals("cache", qp.getCacheControl());
        assertEquals("disposition", qp.getContentDisposition());
        assertEquals("encoding", qp.getContentEncoding());
        assertEquals("language", qp.getContentLanguage());
        assertEquals("type", qp.getContentType());
    }
}

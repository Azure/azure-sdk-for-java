// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common.sas;

import com.azure.core.util.Context;
import com.azure.storage.common.implementation.AccountSasImplUtil;
import com.azure.storage.common.implementation.SasImplUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SasModelsTests {
    @Test
    public void accountSasSignatureValuesMin() {
        OffsetDateTime e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        AccountSasPermission p = AccountSasPermission.parse("l");
        AccountSasService s = AccountSasService.parse("b");
        AccountSasResourceType rt = AccountSasResourceType.parse("o");
        OffsetDateTime st = OffsetDateTime.of(2015, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        SasIpRange ip = SasIpRange.parse("a-b");
        SasProtocol prot = SasProtocol.HTTPS_ONLY;

        AccountSasSignatureValues v
            = new AccountSasSignatureValues(e, p, s, rt).setStartTime(st).setSasIpRange(ip).setProtocol(prot);

        assertEquals(e, v.getExpiryTime());
        assertEquals(p.toString(), v.getPermissions());
        assertEquals(s.toString(), v.getServices());
        assertEquals(rt.toString(), v.getResourceTypes());
        assertEquals(st, v.getStartTime());
        assertEquals(ip, v.getSasIpRange());
        assertEquals(prot, v.getProtocol());
    }

    @ParameterizedTest
    @MethodSource("accountSasSignatureValuesNullSupplier")
    public void accountSasSignatureValuesNull(boolean expiryTime, boolean permissions, boolean services,
        boolean resourceTypes, String variable) {
        OffsetDateTime e = expiryTime ? null : OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        AccountSasPermission p = permissions ? null : AccountSasPermission.parse("l");
        AccountSasService s = services ? null : AccountSasService.parse("b");
        AccountSasResourceType rt = resourceTypes ? null : AccountSasResourceType.parse("o");

        NullPointerException ex
            = assertThrows(NullPointerException.class, () -> new AccountSasSignatureValues(e, p, s, rt));
        assertTrue(ex.getMessage().contains(variable));
    }

    private static Stream<Arguments> accountSasSignatureValuesNullSupplier() {
        // expiryTime, permissions, services, resourceTypes, variable
        return Stream.of(Arguments.of(true, false, false, false, "expiryTime"),
            Arguments.of(false, true, false, false, "permissions"), Arguments.of(false, false, true, false, "services"),
            Arguments.of(false, false, false, true, "resourceTypes"));
    }

    @ParameterizedTest
    @MethodSource("accountSasPermissionsToStringSupplier")
    public void accountSASPermissionsToString(boolean read, boolean write, boolean delete, boolean list, boolean add,
        boolean create, boolean update, boolean process, boolean deleteVersion, boolean tags, boolean filterTags,
        boolean setImmutabilityPolicy, boolean permanentDelete, String expectedString) {
        AccountSasPermission perms = new AccountSasPermission().setReadPermission(read)
            .setWritePermission(write)
            .setDeletePermission(delete)
            .setListPermission(list)
            .setAddPermission(add)
            .setCreatePermission(create)
            .setUpdatePermission(update)
            .setProcessMessages(process)
            .setDeleteVersionPermission(deleteVersion)
            .setTagsPermission(tags)
            .setFilterTagsPermission(filterTags)
            .setImmutabilityPolicyPermission(setImmutabilityPolicy)
            .setPermanentDeletePermission(permanentDelete);

        assertEquals(expectedString, perms.toString());
    }

    private static Stream<Arguments> accountSasPermissionsToStringSupplier() {
        // read, write, delete, list, add, create, update, process, deleteVersion, tags, filterTags,
        // setImmutabilityPolicy, permanentDelete, expectedString
        return Stream.of(
            Arguments.of(true, false, false, false, false, false, false, false, false, false, false, false, false, "r"),
            Arguments.of(false, true, false, false, false, false, false, false, false, false, false, false, false, "w"),
            Arguments.of(false, false, true, false, false, false, false, false, false, false, false, false, false, "d"),
            Arguments.of(false, false, false, true, false, false, false, false, false, false, false, false, false, "l"),
            Arguments.of(false, false, false, false, true, false, false, false, false, false, false, false, false, "a"),
            Arguments.of(false, false, false, false, false, true, false, false, false, false, false, false, false, "c"),
            Arguments.of(false, false, false, false, false, false, true, false, false, false, false, false, false, "u"),
            Arguments.of(false, false, false, false, false, false, false, true, false, false, false, false, false, "p"),
            Arguments.of(false, false, false, false, false, false, false, false, true, false, false, false, false, "x"),
            Arguments.of(false, false, false, false, false, false, false, false, false, true, false, false, false, "t"),
            Arguments.of(false, false, false, false, false, false, false, false, false, false, true, false, false, "f"),
            Arguments.of(false, false, false, false, false, false, false, false, false, false, false, true, false, "i"),
            Arguments.of(false, false, false, false, false, false, false, false, false, false, false, false, true, "y"),
            Arguments.of(true, true, true, true, true, true, true, true, true, true, true, true, true,
                "rwdxylacuptfi"));
    }

    @ParameterizedTest
    @MethodSource("accountSASPermissionsParseSupplier")
    public void accountSASPermissionsParse(String permString, boolean read, boolean write, boolean delete, boolean list,
        boolean add, boolean create, boolean update, boolean process, boolean deleteVersion, boolean tags,
        boolean filterTags, boolean immutabilityPolicy) {
        AccountSasPermission perms = AccountSasPermission.parse(permString);

        assertEquals(read, perms.hasReadPermission());
        assertEquals(write, perms.hasWritePermission());
        assertEquals(delete, perms.hasDeletePermission());
        assertEquals(list, perms.hasListPermission());
        assertEquals(add, perms.hasAddPermission());
        assertEquals(create, perms.hasCreatePermission());
        assertEquals(update, perms.hasUpdatePermission());
        assertEquals(process, perms.hasProcessMessages());
        assertEquals(deleteVersion, perms.hasDeleteVersionPermission());
        assertEquals(tags, perms.hasTagsPermission());
        assertEquals(filterTags, perms.hasFilterTagsPermission());
        assertEquals(immutabilityPolicy, perms.hasImmutabilityPolicyPermission());
    }

    private static Stream<Arguments> accountSASPermissionsParseSupplier() {
        // permString, read, write, delete, list, add, create, update, process, deleteVersion, tags, filterTags,
        // immutabilityPolicy
        return Stream.of(
            Arguments.of("r", true, false, false, false, false, false, false, false, false, false, false, false),
            Arguments.of("w", false, true, false, false, false, false, false, false, false, false, false, false),
            Arguments.of("d", false, false, true, false, false, false, false, false, false, false, false, false),
            Arguments.of("l", false, false, false, true, false, false, false, false, false, false, false, false),
            Arguments.of("a", false, false, false, false, true, false, false, false, false, false, false, false),
            Arguments.of("c", false, false, false, false, false, true, false, false, false, false, false, false),
            Arguments.of("u", false, false, false, false, false, false, true, false, false, false, false, false),
            Arguments.of("p", false, false, false, false, false, false, false, true, false, false, false, false),
            Arguments.of("x", false, false, false, false, false, false, false, false, true, false, false, false),
            Arguments.of("t", false, false, false, false, false, false, false, false, false, true, false, false),
            Arguments.of("f", false, false, false, false, false, false, false, false, false, false, true, false),
            Arguments.of("i", false, false, false, false, false, false, false, false, false, false, false, true),
            Arguments.of("y", false, false, false, false, false, false, false, false, false, false, false, false, true),
            Arguments.of("rwdxlacuptfi", true, true, true, true, true, true, true, true, true, true, true, true, false),
            Arguments.of("lwfriutpcaxd", true, true, true, true, true, true, true, true, true, true, true, true,
                false));
    }

    @Test
    public void accountSASPermissionsParseIA() {
        assertThrows(IllegalArgumentException.class, () -> AccountSasPermission.parse("rwaq"));
    }

    @ParameterizedTest
    @MethodSource("accountSASResourceTypeToStringSupplier")
    public void accountSASResourceTypeToString(boolean service, boolean container, boolean object,
        String expectedString) {
        AccountSasResourceType resourceTypes
            = new AccountSasResourceType().setService(service).setContainer(container).setObject(object);

        assertEquals(expectedString, resourceTypes.toString());
    }

    private static Stream<Arguments> accountSASResourceTypeToStringSupplier() {
        // service, container, object, expectedString
        return Stream.of(Arguments.of(true, false, false, "s"), Arguments.of(false, true, false, "c"),
            Arguments.of(false, false, true, "o"), Arguments.of(true, true, true, "sco"));
    }

    @ParameterizedTest
    @MethodSource("accountSASResourceTypeParseSupplier")
    public void accountSASResourceTypeParse(String resourceTypeString, boolean service, boolean container,
        boolean object) {
        AccountSasResourceType resourceTypes = AccountSasResourceType.parse(resourceTypeString);

        assertEquals(service, resourceTypes.isService());
        assertEquals(container, resourceTypes.isContainer());
        assertEquals(object, resourceTypes.isObject());
    }

    private static Stream<Arguments> accountSASResourceTypeParseSupplier() {
        // resourceTypeString, service, container, object
        return Stream.of(Arguments.of("s", true, false, false), Arguments.of("c", false, true, false),
            Arguments.of("o", false, false, true), Arguments.of("sco", true, true, true));
    }

    @Test
    public void accountSASResourceTypeIA() {
        assertThrows(IllegalArgumentException.class, () -> AccountSasResourceType.parse("scq"));
    }

    @ParameterizedTest
    @MethodSource("ipRangeToStringSupplier")
    public void ipRangeToString(String min, String max, String expectedString) {
        SasIpRange ip = new SasIpRange().setIpMin(min).setIpMax(max);

        assertEquals(expectedString, ip.toString());
    }

    private static Stream<Arguments> ipRangeToStringSupplier() {
        return Stream.of(Arguments.of("a", "b", "a-b"), Arguments.of("a", null, "a"), Arguments.of(null, "b", ""));
    }

    @ParameterizedTest
    @MethodSource("ipRangeParseSupplier")
    public void ipRangeParse(String rangeStr, String min, String max) {
        SasIpRange ip = SasIpRange.parse(rangeStr);

        assertEquals(min, ip.getIpMin());
        assertEquals(max, ip.getIpMax());
    }

    private static Stream<Arguments> ipRangeParseSupplier() {
        return Stream.of(Arguments.of("a-b", "a", "b"), Arguments.of("a", "a", null), Arguments.of("", "", null));
    }

    @ParameterizedTest
    @MethodSource("sasProtocolParseSupplier")
    public void sasProtocolParse(String protocolStr, SasProtocol protocol) {
        assertEquals(protocol, SasProtocol.parse(protocolStr));
    }

    private static Stream<Arguments> sasProtocolParseSupplier() {
        return Stream.of(Arguments.of("https", SasProtocol.HTTPS_ONLY),
            Arguments.of("https,http", SasProtocol.HTTPS_HTTP));
    }

    @Test
    public void accountSasImplUtilNull() {
        OffsetDateTime e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        AccountSasPermission p = AccountSasPermission.parse("l");
        AccountSasService s = AccountSasService.parse("b");
        AccountSasResourceType rt = AccountSasResourceType.parse("o");
        AccountSasSignatureValues v = new AccountSasSignatureValues(e, p, s, rt);
        AccountSasImplUtil implUtil = new AccountSasImplUtil(v, null);

        NullPointerException ex
            = assertThrows(NullPointerException.class, () -> implUtil.generateSas(null, Context.NONE));
        assertTrue(ex.getMessage().contains("storageSharedKeyCredential"));
    }

    @Test
    public void sasDateTimeRoundTrip() {
        // These datetime values do not specify seconds, which is valid on azure, but our default is always to add seconds
        String originalString
            = "st=2021-07-20T13%3A21Z&se=2021-07-20T13%3A21Z&skt=2021-07-20T13%3A21Z&ske=2021-07-20T13%3A21Z";
        Map<String, String[]> splitOriginalParams = SasImplUtils.parseQueryString(originalString);

        String encodedParams = new CommonSasQueryParameters(splitOriginalParams, false).encode();
        Map<String, String[]> splitEncodedParams = SasImplUtils.parseQueryString(encodedParams);

        assertEquals(splitOriginalParams.size(), splitEncodedParams.size());
        for (Map.Entry<String, String[]> entry : splitOriginalParams.entrySet()) {
            assertTrue(splitEncodedParams.containsKey(entry.getKey()));
            String[] originalValue = entry.getValue();
            String[] encodedValue = splitEncodedParams.get(entry.getKey());
            assertEquals(originalValue.length, encodedValue.length);
            for (int i = 0; i < originalValue.length; i++) {
                assertEquals(originalValue[i], encodedValue[i]);
            }
        }
    }
}

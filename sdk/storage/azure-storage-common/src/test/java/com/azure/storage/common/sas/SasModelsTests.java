// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.sas;

import com.azure.core.test.TestProxyTestBase;
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SasModelsTests extends TestProxyTestBase {

    @Test
    public void accountSasSignatureValuesMin() {
        OffsetDateTime e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        AccountSasPermission p = AccountSasPermission.parse("l");
        AccountSasService s = AccountSasService.parse("b");
        AccountSasResourceType rt = AccountSasResourceType.parse("o");

        OffsetDateTime st = OffsetDateTime.of(2015, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        SasIpRange ip = SasIpRange.parse("a-b");
        SasProtocol prot = SasProtocol.HTTPS_ONLY;

        AccountSasSignatureValues v = new AccountSasSignatureValues(e, p, s, rt)
            .setStartTime(st)
            .setSasIpRange(ip)
            .setProtocol(prot);

        assertEquals(v.getExpiryTime(), e);
        assertEquals(v.getPermissions(), p.toString());
        assertEquals(v.getServices(), s.toString());
        assertEquals(v.getResourceTypes(), rt.toString());
        assertEquals(v.getStartTime(), st);
        assertEquals(v.getSasIpRange(), ip);
        assertEquals(v.getProtocol(), prot);
    }

    @ParameterizedTest
    @MethodSource("accountSasSignatureValuesNullSupplier")
    public void accountSasSignatureValuesNull(boolean expiryTime, boolean permissions, boolean services, boolean resourceTypes, String variable) {
        OffsetDateTime e = expiryTime ? null : OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        AccountSasPermission p = permissions ? null : AccountSasPermission.parse("l");
        AccountSasService s = services ? null : AccountSasService.parse("b");
        AccountSasResourceType rt = resourceTypes ? null : AccountSasResourceType.parse("o");

        NullPointerException ex = assertThrows(NullPointerException.class, () -> new AccountSasSignatureValues(e, p, s, rt));

        assertTrue(ex.getMessage().contains(variable));
    }

    private static Stream<Arguments> accountSasSignatureValuesNullSupplier() {
        return Stream.of(
            Arguments.of(true, false, false, false, "expiryTime"),
            Arguments.of(false, true, false, false, "permissions"),
            Arguments.of(false, false, true, false, "services"),
            Arguments.of(false, false, false, true, "resourceTypes")
        );
    }

    @ParameterizedTest
    @MethodSource("accountSASPermissionsToStringSupplier")
    public void accountSASPermissionsToString(boolean read, boolean write, boolean delete, boolean list, boolean add,
        boolean create, boolean update, boolean process, boolean deleteVersion, boolean tags, boolean filterTags,
        boolean setImmutabilityPolicy, boolean permanentDelete, String expectedString) {
        AccountSasPermission perms = new AccountSasPermission()
            .setReadPermission(read)
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

        assertEquals(perms.toString(), expectedString);
    }

    private static Stream<Arguments> accountSASPermissionsToStringSupplier() {
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
            Arguments.of(true, true, true, true, true, true, true, true, true, true, true, true, true, "rwdxylacuptfi")
        );
    }

    @ParameterizedTest
    @MethodSource("accountSASPermissionsParseSupplier")
    public void accountSASPermissionsParse(String permString, boolean read, boolean write, boolean delete, boolean list,
        boolean add, boolean create, boolean update, boolean process, boolean deleteVersion, boolean tags,
        boolean filterTags, boolean immutabilityPolicy) {
        AccountSasPermission perms = AccountSasPermission.parse(permString);

        assertEquals(perms.hasReadPermission(), read);
        assertEquals(perms.hasWritePermission(), write);
        assertEquals(perms.hasDeletePermission(), delete);
        assertEquals(perms.hasListPermission(), list);
        assertEquals(perms.hasAddPermission(), add);
        assertEquals(perms.hasCreatePermission(), create);
        assertEquals(perms.hasUpdatePermission(), update);
        assertEquals(perms.hasProcessMessages(), process);
        assertEquals(perms.hasDeleteVersionPermission(), deleteVersion);
        assertEquals(perms.hasTagsPermission(), tags);
        assertEquals(perms.hasFilterTagsPermission(), filterTags);
        assertEquals(perms.hasImmutabilityPolicyPermission(), immutabilityPolicy);
    }

    private static Stream<Arguments> accountSASPermissionsParseSupplier() {
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
            Arguments.of("rwdxlacuptfi", true, true, true, true, true, true, true, true, true, true, true, true),
            Arguments.of("lwfriutpcaxd", true, true, true, true, true, true, true, true, true, true, true, true)
        );
    }

    @Test
    public void accountSASPermissionsParseIA() {
        assertThrows(IllegalArgumentException.class, () -> AccountSasPermission.parse("rwaq"));
    }

    @ParameterizedTest
    @MethodSource("accountSASResourceTypeToStringSupplier")
    public void accountSASResourceTypeToString(boolean service, boolean container, boolean object,
        String expectedString) {
        AccountSasResourceType resourceTypes = new AccountSasResourceType()
            .setService(service)
            .setContainer(container)
            .setObject(object);

        assertEquals(resourceTypes.toString(), expectedString);
    }

    private static Stream<Arguments> accountSASResourceTypeToStringSupplier() {
        return Stream.of(
            Arguments.of(true, false, false, "s"),
            Arguments.of(false, true, false, "c"),
            Arguments.of(false, false, true, "o"),
            Arguments.of(true, true, true, "sco")
        );
    }

    @ParameterizedTest
    @MethodSource("accountSASResourceTypeParseSupplier")
    public void accountSASResourceTypeParse(String resourceTypeString, boolean service, boolean container,
        boolean object) {
        AccountSasResourceType resourceTypes = AccountSasResourceType.parse(resourceTypeString);
        assertEquals(resourceTypes.isService(), service);
        assertEquals(resourceTypes.isContainer(), container);
        assertEquals(resourceTypes.isObject(), object);
    }

    private static Stream<Arguments> accountSASResourceTypeParseSupplier() {
        return Stream.of(
            Arguments.of("s", true, false, false),
            Arguments.of("c", false, true, false),
            Arguments.of("o", false, false, true),
            Arguments.of("sco", true, true, true)
        );
    }

    @Test
    public void accountSASResourceTypeIA() {
        assertThrows(IllegalArgumentException.class, () -> AccountSasResourceType.parse("scq"));
    }

    @ParameterizedTest
    @MethodSource("ipRangeToStringSupplier")
    public void ipRangeToString(String min, String max, String expectedString) {
        SasIpRange ip = new SasIpRange()
            .setIpMin(min)
            .setIpMax(max);

        assertEquals(ip.toString(), expectedString);
    }

    private static Stream<Arguments> ipRangeToStringSupplier() {
        return Stream.of(
            Arguments.of("a", "b", "a-b"),
            Arguments.of("a", null, "a"),
            Arguments.of(null, "b", "")
        );
    }

    @ParameterizedTest
    @MethodSource("iPRangeParseSupplier")
    public void iPRangeParse(String rangeStr, String min, String max) {
        SasIpRange ip = SasIpRange.parse(rangeStr);

        assertEquals(ip.getIpMin(), min);
        assertEquals(ip.getIpMax(), max);
    }

    private static Stream<Arguments> iPRangeParseSupplier() {
        return Stream.of(
            Arguments.of("a", "b", "a-b"),
            Arguments.of("a", null, "a"),
            Arguments.of(null, "", "")
        );
    }

    @Test
    public void sasProtocolParse() {
        assertEquals(SasProtocol.parse("https"), SasProtocol.HTTPS_ONLY);
        assertEquals(SasProtocol.parse("https,http"), SasProtocol.HTTPS_HTTP);
    }

    @Test
    public void accountSasImplUtilNull() {
        OffsetDateTime e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        AccountSasPermission p = AccountSasPermission.parse("l");
        AccountSasService s = AccountSasService.parse("b");
        AccountSasResourceType rt = AccountSasResourceType.parse("o");
        AccountSasSignatureValues v = new AccountSasSignatureValues(e, p, s, rt);
        AccountSasImplUtil implUtil = new AccountSasImplUtil(v, null);

        NullPointerException ex = assertThrows(NullPointerException.class, () -> implUtil.generateSas(null, Context.NONE));
        assertTrue(ex.getMessage().contains("storageSharedKeyCredential"));
    }

    @Test
    public void sasDateTimeRoundTrip() {
        // These datetime values do not specify seconds, which is valid on azure, but our default is always to add seconds
        String originalString = "st=2021-07-20T13%3A21Z&se=2021-07-20T13%3A21Z&skt=2021-07-20T13%3A21Z&ske=2021-07-20T13%3A21Z";
        Map<String, String[]> splitOriginalParams = SasImplUtils.parseQueryString(originalString);

        CommonSasQueryParameters commonSasQueryParameters = new CommonSasQueryParameters(splitOriginalParams, false);
        String encodedParams = commonSasQueryParameters.encode();
        Map<String, String[]> splitEncodedParams = SasImplUtils.parseQueryString(encodedParams);

        for (String key : splitOriginalParams.keySet()) {
            assertArrayEquals(splitOriginalParams.get(key), splitEncodedParams.get(key));
        }
    }
}

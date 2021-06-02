// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.sas;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SasModelsTest {
    @Test
    public void create_tableAccountSasSignatureValues_WithMinimumValues() {
        OffsetDateTime expiryTime = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        TableAccountSasPermission permissions = TableAccountSasPermission.parse("l");
        TableAccountSasService services = TableAccountSasService.parse("t");
        TableAccountSasResourceType resourceTypes = TableAccountSasResourceType.parse("o");

        OffsetDateTime startTime = OffsetDateTime.of(2015, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        TableSasIpRange ipRange = TableSasIpRange.parse("a-b");
        TableSasProtocol protocol = TableSasProtocol.HTTPS_ONLY;

        TableAccountSasSignatureValues sasSignatureValues =
            new TableAccountSasSignatureValues(expiryTime, permissions, services, resourceTypes)
                .setStartTime(startTime)
                .setSasIpRange(ipRange)
                .setProtocol(protocol);

        assertEquals(expiryTime, sasSignatureValues.getExpiryTime());
        assertEquals(permissions.toString(), sasSignatureValues.getPermissions());
        assertEquals(services.toString(), sasSignatureValues.getServices());
        assertEquals(resourceTypes.toString(), sasSignatureValues.getResourceTypes());
        assertEquals(startTime, sasSignatureValues.getStartTime());
        assertEquals(ipRange, sasSignatureValues.getSasIpRange());
        assertEquals(protocol, sasSignatureValues.getProtocol());
    }

    @Test
    public void create_tableAccountSasSignatureValues_WithNullRequiredValue() {
        OffsetDateTime expiryTime = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        TableAccountSasPermission permissions = TableAccountSasPermission.parse("l"); // List permission
        TableAccountSasService services = TableAccountSasService.parse("t"); // Tables service
        TableAccountSasResourceType resourceTypes = TableAccountSasResourceType.parse("o"); // Object resource

        assertThrows(NullPointerException.class,
            () -> new TableAccountSasSignatureValues(null, permissions, services, resourceTypes));
        assertThrows(NullPointerException.class,
            () -> new TableAccountSasSignatureValues(expiryTime, null, services, resourceTypes));
        assertThrows(NullPointerException.class,
            () -> new TableAccountSasSignatureValues(expiryTime, permissions, null, resourceTypes));
        assertThrows(NullPointerException.class,
            () -> new TableAccountSasSignatureValues(expiryTime, permissions, services, null));
    }

    @Test
    public void tableAccountSasPermission_toString() {
        assertEquals("rwdxlacuptf", new TableAccountSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setDeletePermission(true)
            .setListPermission(true)
            .setAddPermission(true)
            .setCreatePermission(true)
            .setUpdatePermission(true)
            .setProcessMessages(true)
            .setDeleteVersionPermission(true)
            .setTagsPermission(true)
            .setFilterTagsPermission(true)
            .toString());
        assertEquals("r", new TableAccountSasPermission().setReadPermission(true).toString());
        assertEquals("w", new TableAccountSasPermission().setWritePermission(true).toString());
        assertEquals("d", new TableAccountSasPermission().setDeletePermission(true).toString());
        assertEquals("l", new TableAccountSasPermission().setListPermission(true).toString());
        assertEquals("a", new TableAccountSasPermission().setAddPermission(true).toString());
        assertEquals("c", new TableAccountSasPermission().setCreatePermission(true).toString());
        assertEquals("u", new TableAccountSasPermission().setUpdatePermission(true).toString());
        assertEquals("p", new TableAccountSasPermission().setProcessMessages(true).toString());
        assertEquals("x", new TableAccountSasPermission().setDeleteVersionPermission(true).toString());
        assertEquals("t", new TableAccountSasPermission().setTagsPermission(true).toString());
        assertEquals("f", new TableAccountSasPermission().setFilterTagsPermission(true).toString());
    }

    @Test
    public void tableAccountSasPermission_parse() {
        TableAccountSasPermission tableAccountSasPermission = TableAccountSasPermission.parse("rwdxlacuptf");

        assertTrue(tableAccountSasPermission.hasReadPermission());
        assertTrue(tableAccountSasPermission.hasWritePermission());
        assertTrue(tableAccountSasPermission.hasDeletePermission());
        assertTrue(tableAccountSasPermission.hasListPermission());
        assertTrue(tableAccountSasPermission.hasAddPermission());
        assertTrue(tableAccountSasPermission.hasCreatePermission());
        assertTrue(tableAccountSasPermission.hasUpdatePermission());
        assertTrue(tableAccountSasPermission.hasProcessMessages());
        assertTrue(tableAccountSasPermission.hasDeleteVersionPermission());
        assertTrue(tableAccountSasPermission.hasTagsPermission());
        assertTrue(tableAccountSasPermission.hasFilterTagsPermission());

        tableAccountSasPermission = TableAccountSasPermission.parse("lwfrutpcaxd");

        assertTrue(tableAccountSasPermission.hasReadPermission());
        assertTrue(tableAccountSasPermission.hasWritePermission());
        assertTrue(tableAccountSasPermission.hasDeletePermission());
        assertTrue(tableAccountSasPermission.hasListPermission());
        assertTrue(tableAccountSasPermission.hasAddPermission());
        assertTrue(tableAccountSasPermission.hasCreatePermission());
        assertTrue(tableAccountSasPermission.hasUpdatePermission());
        assertTrue(tableAccountSasPermission.hasProcessMessages());
        assertTrue(tableAccountSasPermission.hasDeleteVersionPermission());
        assertTrue(tableAccountSasPermission.hasTagsPermission());
        assertTrue(tableAccountSasPermission.hasFilterTagsPermission());

        assertTrue(TableAccountSasPermission.parse("r").hasReadPermission());
        assertTrue(TableAccountSasPermission.parse("w").hasWritePermission());
        assertTrue(TableAccountSasPermission.parse("d").hasDeletePermission());
        assertTrue(TableAccountSasPermission.parse("l").hasListPermission());
        assertTrue(TableAccountSasPermission.parse("a").hasAddPermission());
        assertTrue(TableAccountSasPermission.parse("c").hasCreatePermission());
        assertTrue(TableAccountSasPermission.parse("u").hasUpdatePermission());
        assertTrue(TableAccountSasPermission.parse("p").hasProcessMessages());
        assertTrue(TableAccountSasPermission.parse("x").hasDeleteVersionPermission());
        assertTrue(TableAccountSasPermission.parse("t").hasTagsPermission());
        assertTrue(TableAccountSasPermission.parse("f").hasFilterTagsPermission());
    }

    @Test
    public void tableAccountSasPermission_parse_illegalString() {
        assertThrows(IllegalArgumentException.class, () -> TableAccountSasPermission.parse("rwaq"));
    }

    @Test
    public void tableAccountSasResourceType_toString() {
        assertEquals("sco", new TableAccountSasResourceType()
            .setService(true)
            .setContainer(true)
            .setObject(true)
            .toString());

        assertEquals("s", new TableAccountSasResourceType().setService(true).toString());
        assertEquals("c", new TableAccountSasResourceType().setContainer(true).toString());
        assertEquals("o", new TableAccountSasResourceType().setObject(true).toString());
    }

    @Test
    public void tableAccountSasResourceType_parse() {
        TableAccountSasResourceType tableAccountSasResourceType = TableAccountSasResourceType.parse("sco");

        assertTrue(tableAccountSasResourceType.isService());
        assertTrue(tableAccountSasResourceType.isContainer());
        assertTrue(tableAccountSasResourceType.isObject());

        assertTrue(TableAccountSasResourceType.parse("s").isService());
        assertTrue(TableAccountSasResourceType.parse("c").isContainer());
        assertTrue(TableAccountSasResourceType.parse("o").isObject());
    }

    @Test
    public void tableAccountSasResourceType_parse_illegalString() {
        assertThrows(IllegalArgumentException.class, () -> TableAccountSasResourceType.parse("scq"));
    }

    @Test
    public void tableAccountSasService_toString() {
        assertEquals("bqtf", new TableAccountSasService()
            .setBlobAccess(true)
            .setQueueAccess(true)
            .setTableAccess(true)
            .setFileAccess(true)
            .toString());

        assertEquals("b", new TableAccountSasService().setBlobAccess(true).toString());
        assertEquals("q", new TableAccountSasService().setQueueAccess(true).toString());
        assertEquals("t", new TableAccountSasService().setTableAccess(true).toString());
        assertEquals("f", new TableAccountSasService().setFileAccess(true).toString());
    }

    @Test
    public void tableAccountSasService_parse() {
        TableAccountSasService tableAccountSasService = TableAccountSasService.parse("bqtf");

        assertTrue(tableAccountSasService.hasBlobAccess());
        assertTrue(tableAccountSasService.hasQueueAccess());
        assertTrue(tableAccountSasService.hasTableAccess());
        assertTrue(tableAccountSasService.hasFileAccess());

        assertTrue(TableAccountSasService.parse("b").hasBlobAccess());
        assertTrue(TableAccountSasService.parse("q").hasQueueAccess());
        assertTrue(TableAccountSasService.parse("t").hasTableAccess());
        assertTrue(TableAccountSasService.parse("f").hasFileAccess());
    }

    @Test
    public void tableAccountSasService_parse_illegalString() {
        assertThrows(IllegalArgumentException.class, () -> TableAccountSasService.parse("bqta"));
    }

    @Test
    public void tableSasIpRange_toString() {
        assertEquals("a-b", new TableSasIpRange()
            .setIpMin("a")
            .setIpMax("b")
            .toString());

        assertEquals("a", new TableSasIpRange().setIpMin("a").toString());
        assertEquals("", new TableSasIpRange().setIpMax("b").toString());
    }

    @Test
    public void tableSasIpRange_parse() {
        TableSasIpRange tableSasIpRange = TableSasIpRange.parse("a-b");

        assertEquals("a", tableSasIpRange.getIpMin());
        assertEquals("b", tableSasIpRange.getIpMax());

        tableSasIpRange = TableSasIpRange.parse("a");

        assertEquals("a", tableSasIpRange.getIpMin());
        assertNull(tableSasIpRange.getIpMax());

        tableSasIpRange = TableSasIpRange.parse("");

        assertEquals("", tableSasIpRange.getIpMin());
        assertNull(tableSasIpRange.getIpMax());
    }

    @Test
    public void tableSasProtocol_parse() {
        assertEquals(TableSasProtocol.HTTPS_ONLY, TableSasProtocol.parse("https"));
        assertEquals(TableSasProtocol.HTTPS_HTTP, TableSasProtocol.parse("https,http"));
    }

    @Test
    public void create_tableSasSignatureValues_WithMinimumValues() {
        OffsetDateTime expiryTime = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        TableSasPermission permissions = TableSasPermission.parse("r");

        OffsetDateTime startTime = OffsetDateTime.of(2015, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        TableSasIpRange ipRange = TableSasIpRange.parse("a-b");
        TableSasProtocol protocol = TableSasProtocol.HTTPS_ONLY;
        String startPartitionKey = "startPartitionKey";
        String startRowKey = "startRowKey";
        String endPartitionKey = "endPartitionKey";
        String endRowKey = "endRowKey";

        TableSasSignatureValues sasSignatureValues =
            new TableSasSignatureValues(expiryTime, permissions)
                .setStartTime(startTime)
                .setSasIpRange(ipRange)
                .setProtocol(protocol)
                .setStartPartitionKey(startPartitionKey)
                .setStartRowKey(startRowKey)
                .setEndPartitionKey(endPartitionKey)
                .setEndRowKey(endRowKey);

        assertEquals(expiryTime, sasSignatureValues.getExpiryTime());
        assertEquals(permissions.toString(), sasSignatureValues.getPermissions());
        assertEquals(startTime, sasSignatureValues.getStartTime());
        assertEquals(ipRange, sasSignatureValues.getSasIpRange());
        assertEquals(protocol, sasSignatureValues.getProtocol());
        assertEquals(startPartitionKey, sasSignatureValues.getStartPartitionKey());
        assertEquals(startRowKey, sasSignatureValues.getStartRowKey());
        assertEquals(endPartitionKey, sasSignatureValues.getEndPartitionKey());
        assertEquals(endRowKey, sasSignatureValues.getEndRowKey());
    }

    @Test
    public void create_tableSasSignatureValues_WithNullRequiredValue() {
        OffsetDateTime expiryTime = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        TableSasPermission permissions = TableSasPermission.parse("r");

        assertThrows(NullPointerException.class,
            () -> new TableSasSignatureValues(null, permissions));
        assertThrows(NullPointerException.class,
            () -> new TableSasSignatureValues(expiryTime, null));
    }

    @Test
    public void tableSasPermission_toString() {
        assertEquals("raup", new TableSasPermission()
            .setReadPermission(true)
            .setAddPermission(true)
            .setUpdatePermission(true)
            .setProcessPermission(true)
            .toString());
        assertEquals("r", new TableSasPermission().setReadPermission(true).toString());
        assertEquals("a", new TableSasPermission().setAddPermission(true).toString());
        assertEquals("u", new TableSasPermission().setUpdatePermission(true).toString());
        assertEquals("p", new TableSasPermission().setProcessPermission(true).toString());
    }

    @Test
    public void tableSasPermission_parse() {
        TableSasPermission tableSasPermission = TableSasPermission.parse("raup");

        assertTrue(tableSasPermission.hasReadPermission());
        assertTrue(tableSasPermission.hasAddPermission());
        assertTrue(tableSasPermission.hasUpdatePermission());
        assertTrue(tableSasPermission.hasProcessPermission());

        tableSasPermission = TableSasPermission.parse("urpa");

        assertTrue(tableSasPermission.hasReadPermission());
        assertTrue(tableSasPermission.hasAddPermission());
        assertTrue(tableSasPermission.hasUpdatePermission());
        assertTrue(tableSasPermission.hasProcessPermission());

        assertTrue(TableSasPermission.parse("r").hasReadPermission());
        assertTrue(TableSasPermission.parse("a").hasAddPermission());
        assertTrue(TableSasPermission.parse("u").hasUpdatePermission());
        assertTrue(TableSasPermission.parse("p").hasProcessPermission());
    }

    @Test
    public void tableSasPermission_parse_illegalString() {
        assertThrows(IllegalArgumentException.class, () -> TableSasPermission.parse("rauq"));
    }
}

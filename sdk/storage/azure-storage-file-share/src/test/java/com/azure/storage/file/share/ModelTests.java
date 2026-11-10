// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.storage.file.share.implementation.models.SharePropertiesInternal;
import com.azure.storage.file.share.implementation.models.SharesGetPropertiesHeaders;
import com.azure.storage.file.share.implementation.util.ModelHelper;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.ShareFileUploadRangeOptions;
import com.azure.storage.file.share.models.ShareProperties;
import com.azure.xml.XmlReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ModelTests {

    @Test
    void sharePropertiesChangeFeedDefaultsAndClearing() {
        ShareProperties properties = new ShareProperties();
        assertNull(properties.isChangeFeedEnabled());
        assertNull(properties.getChangeFeedRetentionInDays());

        properties.setChangeFeedEnabled(true).setChangeFeedRetentionInDays(7);
        properties.setChangeFeedEnabled(null).setChangeFeedRetentionInDays(null);
        assertNull(properties.isChangeFeedEnabled());
        assertNull(properties.getChangeFeedRetentionInDays());
    }

    @Test
    void ntfsToAttributes() {
        assertEquals(EnumSet.of(NtfsFileAttributes.READ_ONLY), NtfsFileAttributes.toAttributes("ReadOnly"));
        assertEquals(EnumSet.of(NtfsFileAttributes.READ_ONLY), NtfsFileAttributes.toAttributes("ReadOnly "));
        assertEquals(EnumSet.of(NtfsFileAttributes.READ_ONLY), NtfsFileAttributes.toAttributes(" ReadOnly"));
        assertEquals(EnumSet.of(NtfsFileAttributes.HIDDEN), NtfsFileAttributes.toAttributes("Hidden"));
        assertEquals(EnumSet.of(NtfsFileAttributes.SYSTEM), NtfsFileAttributes.toAttributes("System"));
        assertEquals(EnumSet.of(NtfsFileAttributes.NORMAL), NtfsFileAttributes.toAttributes("None"));
        assertEquals(EnumSet.of(NtfsFileAttributes.DIRECTORY), NtfsFileAttributes.toAttributes("Directory"));
        assertEquals(EnumSet.of(NtfsFileAttributes.ARCHIVE), NtfsFileAttributes.toAttributes("Archive"));
        assertEquals(EnumSet.of(NtfsFileAttributes.TEMPORARY), NtfsFileAttributes.toAttributes("Temporary"));
        assertEquals(EnumSet.of(NtfsFileAttributes.OFFLINE), NtfsFileAttributes.toAttributes("Offline"));
        assertEquals(EnumSet.of(NtfsFileAttributes.NOT_CONTENT_INDEXED),
            NtfsFileAttributes.toAttributes("NotContentIndexed"));
        assertEquals(EnumSet.of(NtfsFileAttributes.NO_SCRUB_DATA), NtfsFileAttributes.toAttributes("NoScrubData"));
        assertEquals(
            EnumSet.of(NtfsFileAttributes.READ_ONLY, NtfsFileAttributes.NO_SCRUB_DATA, NtfsFileAttributes.OFFLINE,
                NtfsFileAttributes.DIRECTORY),
            NtfsFileAttributes.toAttributes("ReadOnly |  NoScrubData | Offline   |Directory"));
    }

    @Test
    void ntfsFromAttributes() {
        assertNull(NtfsFileAttributes.toString(null));
        assertEquals("ReadOnly", NtfsFileAttributes.toString(EnumSet.of(NtfsFileAttributes.READ_ONLY)));
        assertEquals("Hidden", NtfsFileAttributes.toString(EnumSet.of(NtfsFileAttributes.HIDDEN)));
        assertEquals("System", NtfsFileAttributes.toString(EnumSet.of(NtfsFileAttributes.SYSTEM)));
        assertEquals("None", NtfsFileAttributes.toString(EnumSet.of(NtfsFileAttributes.NORMAL)));
        assertEquals("Directory", NtfsFileAttributes.toString(EnumSet.of(NtfsFileAttributes.DIRECTORY)));
        assertEquals("Archive", NtfsFileAttributes.toString(EnumSet.of(NtfsFileAttributes.ARCHIVE)));
        assertEquals("Temporary", NtfsFileAttributes.toString(EnumSet.of(NtfsFileAttributes.TEMPORARY)));
        assertEquals("Offline", NtfsFileAttributes.toString(EnumSet.of(NtfsFileAttributes.OFFLINE)));
        assertEquals("NotContentIndexed",
            NtfsFileAttributes.toString(EnumSet.of(NtfsFileAttributes.NOT_CONTENT_INDEXED)));
        assertEquals("NoScrubData", NtfsFileAttributes.toString(EnumSet.of(NtfsFileAttributes.NO_SCRUB_DATA)));
        assertEquals("ReadOnly|Directory|Offline|NoScrubData",
            NtfsFileAttributes.toString(EnumSet.of(NtfsFileAttributes.READ_ONLY, NtfsFileAttributes.NO_SCRUB_DATA,
                NtfsFileAttributes.OFFLINE, NtfsFileAttributes.DIRECTORY)));
    }

    @Test
    void uploadZeroLengthFails() {
        assertThrows(IllegalArgumentException.class,
            () -> new ShareFileUploadRangeOptions(new ByteArrayInputStream(new byte[0]), 0));
    }

    @Test
    void shareCreationTimeIsMappedFromResponses() throws XMLStreamException {
        OffsetDateTime creationTime = OffsetDateTime.parse("2026-10-01T12:00:00Z");
        String creationTimeRfc1123 = new DateTimeRfc1123(creationTime).toString();
        ShareProperties shareProperties = new ShareProperties().setCreationTime(creationTime);

        assertEquals(creationTime, shareProperties.getCreationTime());

        String propertiesXml = "<Properties><Last-Modified>" + creationTimeRfc1123 + "</Last-Modified>"
            + "<Etag>etag</Etag><Quota>1</Quota><Creation-Time>" + creationTimeRfc1123
            + "</Creation-Time></Properties>";
        try (XmlReader xmlReader = XmlReader.fromString(propertiesXml)) {
            assertEquals(creationTime,
                ModelHelper.populateShareProperties(SharePropertiesInternal.fromXml(xmlReader, "Properties"))
                    .getCreationTime());
        }

        HttpHeaders rawHeaders
            = new HttpHeaders().set(HttpHeaderName.fromString("x-ms-share-creation-time"), creationTimeRfc1123)
                .set(HttpHeaderName.fromString("x-ms-share-quota"), "1");
        Response<SharesGetPropertiesHeaders> response
            = new SimpleResponse<>(null, 200, rawHeaders, new SharesGetPropertiesHeaders(rawHeaders));
        assertEquals(creationTime, ModelHelper.mapGetPropertiesResponse(response).getValue().getCreationTime());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void shareSmbDirectoryLeaseEnabledIsMappedFromResponses(boolean enabled) throws XMLStreamException {
        String lastModified = new DateTimeRfc1123(OffsetDateTime.parse("2026-10-01T12:00:00Z")).toString();
        String propertiesXml = "<Properties><Last-Modified>" + lastModified + "</Last-Modified><Etag>etag</Etag>"
            + "<Quota>1</Quota><EnableSmbDirectoryLease>" + enabled + "</EnableSmbDirectoryLease></Properties>";
        try (XmlReader xmlReader = XmlReader.fromString(propertiesXml)) {
            assertEquals(enabled,
                ModelHelper.populateShareProperties(SharePropertiesInternal.fromXml(xmlReader, "Properties"))
                    .isSmbDirectoryLeaseEnabled());
        }

        HttpHeaders rawHeaders = new HttpHeaders()
            .set(HttpHeaderName.fromString("x-ms-enable-smb-directory-lease"), String.valueOf(enabled))
            .set(HttpHeaderName.fromString("x-ms-share-quota"), "1");
        Response<SharesGetPropertiesHeaders> response
            = new SimpleResponse<>(null, 200, rawHeaders, new SharesGetPropertiesHeaders(rawHeaders));
        assertEquals(enabled, ModelHelper.mapGetPropertiesResponse(response).getValue().isSmbDirectoryLeaseEnabled());
    }

    @Test
    void shareSmbDirectoryLeaseEnabledIsNullWhenNotReturned() {
        HttpHeaders rawHeaders = new HttpHeaders().set(HttpHeaderName.fromString("x-ms-share-quota"), "1");
        Response<SharesGetPropertiesHeaders> response
            = new SimpleResponse<>(null, 200, rawHeaders, new SharesGetPropertiesHeaders(rawHeaders));
        assertNull(ModelHelper.mapGetPropertiesResponse(response).getValue().isSmbDirectoryLeaseEnabled());
    }
}

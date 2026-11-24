// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.storage.file.share.implementation.models.FilesDownloadHeaders;
import com.azure.storage.file.share.implementation.models.FilesGetHardLinksHeaders;
import com.azure.storage.file.share.implementation.models.HardLinkList;
import com.azure.storage.file.share.implementation.models.SharePropertiesInternal;
import com.azure.storage.file.share.implementation.models.SharesGetPropertiesHeaders;
import com.azure.storage.file.share.implementation.util.ModelHelper;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.ShareFileDownloadHeaders;
import com.azure.storage.file.share.models.ShareFileUploadRangeOptions;
import com.azure.storage.file.share.models.ShareProperties;
import com.azure.xml.XmlReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void fileLinksResponseWithoutHardLinksIsEmpty() throws XMLStreamException {
        HttpHeaders rawHeaders = new HttpHeaders();
        HardLinkList emptyList;
        try (XmlReader xmlReader = XmlReader.fromString("<HardLinks></HardLinks>")) {
            emptyList = HardLinkList.fromXml(xmlReader);
        }

        for (HardLinkList body : new HardLinkList[] { emptyList, null }) {
            ResponseBase<FilesGetHardLinksHeaders, HardLinkList> response
                = new ResponseBase<>(null, 200, rawHeaders, body, new FilesGetHardLinksHeaders(rawHeaders));
            assertTrue(ModelHelper.getFileLinksResponse(response).getValue().getLinks().isEmpty());
        }
    }

    @Test
    void fileDownloadHeadersMapsEveryPublicProperty() throws ReflectiveOperationException {
        String date = new DateTimeRfc1123(OffsetDateTime.parse("2026-10-01T12:00:00Z")).toString();
        String md5 = "AQIDBA==";
        HttpHeaders rawHeaders = new HttpHeaders().set(HttpHeaderName.LAST_MODIFIED, date)
            .set(HttpHeaderName.fromString("x-ms-meta-key"), "value")
            .set(HttpHeaderName.CONTENT_LENGTH, "5")
            .set(HttpHeaderName.CONTENT_TYPE, "text/plain")
            .set(HttpHeaderName.CONTENT_RANGE, "bytes 0-4/5")
            .set(HttpHeaderName.ETAG, "\"etag\"")
            .set(HttpHeaderName.CONTENT_MD5, md5)
            .set(HttpHeaderName.CONTENT_ENCODING, "gzip")
            .set(HttpHeaderName.CACHE_CONTROL, "no-cache")
            .set(HttpHeaderName.CONTENT_DISPOSITION, "inline")
            .set(HttpHeaderName.CONTENT_LANGUAGE, "en")
            .set(HttpHeaderName.X_MS_REQUEST_ID, "request-id")
            .set(HttpHeaderName.fromString("x-ms-version"), "2027-03-07")
            .set(HttpHeaderName.ACCEPT_RANGES, "bytes")
            .set(HttpHeaderName.DATE, date)
            .set(HttpHeaderName.fromString("x-ms-copy-completion-time"), date)
            .set(HttpHeaderName.fromString("x-ms-copy-status-description"), "description")
            .set(HttpHeaderName.fromString("x-ms-copy-id"), "copy-id")
            .set(HttpHeaderName.fromString("x-ms-copy-progress"), "5/5")
            .set(HttpHeaderName.fromString("x-ms-copy-source"), "https://account.file.core.windows.net/share/file")
            .set(HttpHeaderName.fromString("x-ms-copy-status"), "success")
            .set(HttpHeaderName.fromString("x-ms-content-md5"), md5)
            .set(HttpHeaderName.fromString("x-ms-server-encrypted"), "true")
            .set(HttpHeaderName.fromString("x-ms-file-attributes"), "Archive")
            .set(HttpHeaderName.fromString("x-ms-file-creation-time"), "2026-10-01T12:00:00.0000000Z")
            .set(HttpHeaderName.fromString("x-ms-file-last-write-time"), "2026-10-01T12:00:00.0000000Z")
            .set(HttpHeaderName.fromString("x-ms-file-change-time"), "2026-10-01T12:00:00.0000000Z")
            .set(HttpHeaderName.fromString("x-ms-file-permission-key"), "permission-key")
            .set(HttpHeaderName.fromString("x-ms-file-id"), "file-id")
            .set(HttpHeaderName.fromString("x-ms-file-parent-id"), "parent-id")
            .set(HttpHeaderName.fromString("x-ms-lease-duration"), "infinite")
            .set(HttpHeaderName.fromString("x-ms-lease-state"), "leased")
            .set(HttpHeaderName.fromString("x-ms-lease-status"), "locked")
            .set(HttpHeaderName.fromString("x-ms-error-code"), "ConditionNotMet");

        ShareFileDownloadHeaders headers
            = ModelHelper.transformFileDownloadHeaders(new FilesDownloadHeaders(rawHeaders), rawHeaders);

        for (Method method : ShareFileDownloadHeaders.class.getMethods()) {
            boolean isGetter = method.getName().startsWith("get") || method.getName().startsWith("is");
            if (isGetter
                && method.getParameterCount() == 0
                && method.getDeclaringClass() == ShareFileDownloadHeaders.class) {
                assertNotNull(method.invoke(headers), method.getName() + " was not mapped");
            }
        }
        assertEquals("text/plain", headers.getContentType());
        assertEquals("ConditionNotMet", headers.getErrorCode());
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class FileIdTestHelper {
    static final String ENDPOINT = "https://account.file.core.windows.net";
    static final String SHARE_NAME = "share";
    static final String FILE_ID = "12384898975283830";
    private static final String FILE_TIME = "2026-09-29T12:34:56.1234567Z";

    private FileIdTestHelper() {
    }

    static HttpHeaders fileHeaders(String fileName) {
        return new HttpHeaders().set(HttpHeaderName.ETAG, "\"0x8DEAF1479E1C087\"")
            .set(HttpHeaderName.LAST_MODIFIED, "Wed, 21 Oct 2015 07:28:00 GMT")
            .set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream")
            .set(HttpHeaderName.CONTENT_LENGTH, "1024")
            .set(HttpHeaderName.CONTENT_LANGUAGE, "en")
            .set(HttpHeaderName.CONTENT_ENCODING, "gzip")
            .set(HttpHeaderName.fromString("x-ms-meta-key"), "value")
            .set(HttpHeaderName.fromString("x-ms-file-creation-time"), FILE_TIME)
            .set(HttpHeaderName.fromString("x-ms-file-last-write-time"), FILE_TIME)
            .set(HttpHeaderName.fromString("x-ms-file-change-time"), FILE_TIME)
            .set(HttpHeaderName.fromString("x-ms-file-id"), FILE_ID)
            .set(HttpHeaderName.fromString("x-ms-file-parent-id"), "parent-id")
            .set(HttpHeaderName.fromString("x-ms-file-name"), fileName)
            .set(HttpHeaderName.fromString("x-ms-type"), "file")
            .set(HttpHeaderName.fromString("x-ms-server-encrypted"), "false")
            .set(HttpHeaderName.fromString("x-ms-content-type"), "application/octet-stream")
            .set(HttpHeaderName.fromString("x-ms-content-length"), "1024")
            .set(HttpHeaderName.fromString("x-ms-content-language"), "en")
            .set(HttpHeaderName.fromString("x-ms-content-encoding"), "gzip");
    }

    static void assertSmbProperties(FileSmbProperties properties) {
        assertEquals(FILE_ID, properties.getFileId());
        assertEquals("parent-id", properties.getParentId());
        assertEquals(OffsetDateTime.parse(FILE_TIME), properties.getFileCreationTime());
        assertEquals(OffsetDateTime.parse(FILE_TIME), properties.getFileLastWriteTime());
        assertEquals(OffsetDateTime.parse(FILE_TIME), properties.getFileChangeTime());
    }
}

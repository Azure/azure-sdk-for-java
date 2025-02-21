// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.ShareFileUploadRangeOptions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ModelTests {

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
        assertThrows(IllegalArgumentException.class, () -> {
            new ShareFileUploadRangeOptions(new ByteArrayInputStream(new byte[0]), 0);
        });
    }
}

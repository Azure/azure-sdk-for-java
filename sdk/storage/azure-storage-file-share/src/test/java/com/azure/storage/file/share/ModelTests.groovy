// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.storage.file.share.models.NtfsFileAttributes
import com.azure.storage.file.share.models.ShareFileUploadOptions
import com.azure.storage.file.share.models.ShareFileUploadRangeOptions
import reactor.core.publisher.Flux
import spock.lang.Unroll

class ModelTests extends APISpec {

    @Unroll
    def "Ntfs toAttributes"() {
        expect:
        NtfsFileAttributes.toAttributes(attribute).equals(enumContained)

        where:
        attribute               | enumContained
        "ReadOnly"              | EnumSet.of(NtfsFileAttributes.READ_ONLY)
        "ReadOnly "             | EnumSet.of(NtfsFileAttributes.READ_ONLY)
        " ReadOnly"             | EnumSet.of(NtfsFileAttributes.READ_ONLY)
        "Hidden"                | EnumSet.of(NtfsFileAttributes.HIDDEN)
        "System"                | EnumSet.of(NtfsFileAttributes.SYSTEM)
        "None"                  | EnumSet.of(NtfsFileAttributes.NORMAL)
        "Directory"             | EnumSet.of(NtfsFileAttributes.DIRECTORY)
        "Archive"               | EnumSet.of(NtfsFileAttributes.ARCHIVE)
        "Temporary"             | EnumSet.of(NtfsFileAttributes.TEMPORARY)
        "Offline"               | EnumSet.of(NtfsFileAttributes.OFFLINE)
        "NotContentIndexed"     | EnumSet.of(NtfsFileAttributes.NOT_CONTENT_INDEXED)
        "NoScrubData"           | EnumSet.of(NtfsFileAttributes.NO_SCRUB_DATA)
        "ReadOnly |  NoScrubData | Offline   |Directory" | EnumSet.of(NtfsFileAttributes.READ_ONLY, NtfsFileAttributes.NO_SCRUB_DATA, NtfsFileAttributes.OFFLINE, NtfsFileAttributes.DIRECTORY)
    }

    @Unroll
    def "Ntfs fromAttributes"() {
        expect:
        NtfsFileAttributes.toString(enumContained) == enumString

        where:
        enumContained | enumString
        null | null
        EnumSet.of(NtfsFileAttributes.READ_ONLY) | "ReadOnly"
        EnumSet.of(NtfsFileAttributes.HIDDEN) | "Hidden"
        EnumSet.of(NtfsFileAttributes.SYSTEM) | "System"
        EnumSet.of(NtfsFileAttributes.NORMAL) | "None"
        EnumSet.of(NtfsFileAttributes.DIRECTORY) | "Directory"
        EnumSet.of(NtfsFileAttributes.ARCHIVE) | "Archive"
        EnumSet.of(NtfsFileAttributes.TEMPORARY) | "Temporary"
        EnumSet.of(NtfsFileAttributes.OFFLINE) | "Offline"
        EnumSet.of(NtfsFileAttributes.NOT_CONTENT_INDEXED) | "NotContentIndexed"
        EnumSet.of(NtfsFileAttributes.NO_SCRUB_DATA) | "NoScrubData"
        EnumSet.of(NtfsFileAttributes.READ_ONLY, NtfsFileAttributes.NO_SCRUB_DATA, NtfsFileAttributes.OFFLINE, NtfsFileAttributes.DIRECTORY) | "ReadOnly|Directory|Offline|NoScrubData"
    }

    def "Upload zero length fails"() {
        when:
        new ShareFileUploadRangeOptions(new ByteArrayInputStream(new byte[0]), 0)

        then:
        thrown(IllegalArgumentException)
    }
}

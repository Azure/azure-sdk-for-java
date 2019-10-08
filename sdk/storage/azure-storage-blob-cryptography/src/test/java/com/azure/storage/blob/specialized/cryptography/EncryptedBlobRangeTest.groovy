package com.azure.storage.blob.specialized.cryptography

import com.azure.storage.blob.models.BlobRange
import spock.lang.Requires
import spock.lang.Unroll

class EncryptedBlobRangeTest extends APISpec {

    // This test checks that the EncryptedBlobRange cna be properly constructed from a BlobRange
    @Unroll
    @Requires({ liveMode() })
    def "Test constructor"() {
        setup:
        BlobRange range
        if (offset == null && count == null) {
            range = new BlobRange(0)
        } else if (offset == null) {
            range = new BlobRange(offset)
        } else {
            range = new BlobRange(offset, count)
        }

        when:
        EncryptedBlobRange ebr = new EncryptedBlobRange(range)

        then:
        ebr.toBlobRange().toString() == expectedString

        where:
        offset | count || expectedString
        null   | null  || "bytes=0-"   // Both null
        3      | null  || "bytes=0-"   // Only offset specified
        17     | null  || "bytes=0-"
        34     | null  || "bytes=16-"
        47     | null  || "bytes=16-"
        48     | null  || "bytes=32-"
        2      | 6     || "bytes=0-15" // Two parameters specified
        18     | 2     || "bytes=0-31"
        38     | 17    || "bytes=16-63"

    }

    // This test checks that the encrypted blob range can be correctly constructed from a BlobRange header string
    @Unroll
    @Requires({ liveMode() })
    def "Test from blob range header"() {
        setup:
        BlobRange range
        if (offset == null && count == null) {
            range = new BlobRange(0)
        } else if (offset == null) {
            range = new BlobRange(offset)
        } else {
            range = new BlobRange(offset, count)
        }

        when:
        EncryptedBlobRange encryptedRangeFromBlobRange = new EncryptedBlobRange(range)
        EncryptedBlobRange encryptedRangeFromHeader = EncryptedBlobRange.getEncryptedBlobRangeFromHeader(range.toHeaderValue())

        then:
        encryptedRangeFromBlobRange.toBlobRange().toHeaderValue() == encryptedRangeFromHeader.toBlobRange().toHeaderValue()

        where:
        offset | count
        null   | null   // Both null
        3      | null   // Only offset specified
        17     | null
        34     | null
        47     | null
        48     | null
        2      | 6     // Two parameters specified
        18     | 2
        38     | 17

    }
}

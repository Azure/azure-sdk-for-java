package com.azure.storage.blob.cryptography

import com.azure.storage.blob.models.BlobRange
import spock.lang.Unroll

class EncryptedBlobRangeTest extends APISpec {

    @Unroll
    def "Test from header string"() {
        setup:
        BlobRange range
        if (offset == null && count == null) {
            range = new BlobRange()
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

        // TODO : Add more cases here
        where:
        offset | count
        null   | null
        17     | null
        2      | 6

    }
}

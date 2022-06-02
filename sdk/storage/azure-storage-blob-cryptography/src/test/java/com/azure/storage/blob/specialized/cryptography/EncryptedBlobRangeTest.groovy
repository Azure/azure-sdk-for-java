package com.azure.storage.blob.specialized.cryptography

import com.azure.storage.blob.models.BlobRange
import com.azure.storage.common.implementation.Constants
import spock.lang.Unroll

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.*
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_PROTOCOL_V2

class EncryptedBlobRangeTest extends APISpec {

    // This test checks that the EncryptedBlobRange cna be properly constructed from a BlobRange
    @Unroll
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
        EncryptedBlobRange ebr = new EncryptedBlobRange(range,
            new EncryptionDataV1().setEncryptionAgent(new EncryptionAgent().setProtocol(ENCRYPTION_PROTOCOL_V1)))

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

    @Unroll
    def "Constructor v2"() {
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
        EncryptedBlobRange ebr = new EncryptedBlobRange(range,
            new EncryptionDataV1().setEncryptionAgent(new EncryptionAgent().setProtocol(ENCRYPTION_PROTOCOL_V2)))

        then:
        ebr.toBlobRange().toString() == expectedString

        where:
        offset | count || expectedString
        null   | null  || "bytes=0-"   // Both null
        3      | null  || "bytes=0-"   // Only offset specified. First region
        GCM_ENCRYPTION_REGION_LENGTH - 1 | null || "bytes=0-" // 4mb + 28 - 1
        GCM_ENCRYPTION_REGION_LENGTH + 1024 | null || "bytes=4194332-" // Second region
        2 * GCM_ENCRYPTION_REGION_LENGTH     | GCM_ENCRYPTION_REGION_LENGTH  || "bytes=8388664-12582995" // Third region exact
        2      | 6     || "bytes=0-4194331" // Two parameters specified. All in first region. 4mb + 28
        5000000     | 5000000     || "bytes=4194332-12582995" // Second to third region.
        5000000     | 20165000    || "bytes=4194332-25165991" // Second to fifth region
    }

    // This test checks that the encrypted blob range can be correctly constructed from a BlobRange header string
    @Unroll
    def "Test from blob range header"() {
        setup:
        def encryptionDataV1 = new EncryptionDataV1().setEncryptionAgent(
            new EncryptionAgent().setProtocol(ENCRYPTION_PROTOCOL_V1))
        def encryptionDataV2 = new EncryptionDataV2().setEncryptionAgent(
            new EncryptionAgent().setProtocol(ENCRYPTION_PROTOCOL_V2))
        BlobRange range
        if (offset == null && count == null) {
            range = new BlobRange(0)
        } else if (offset == null) {
            range = new BlobRange(offset)
        } else {
            range = new BlobRange(offset, count)
        }

        when:
        EncryptedBlobRange encryptedRangeFromBlobRangeV1 = new EncryptedBlobRange(range, encryptionDataV1)
        EncryptedBlobRange encryptedRangeFromHeaderV1 =
            EncryptedBlobRange.getEncryptedBlobRangeFromHeader(range.toHeaderValue(), encryptionDataV1)
        EncryptedBlobRange encryptedRangeFromBlobRangeV2 = new EncryptedBlobRange(range, encryptionDataV2)
        EncryptedBlobRange encryptedRangeFromHeaderV2 =
            EncryptedBlobRange.getEncryptedBlobRangeFromHeader(range.toHeaderValue(), encryptionDataV2)

        then:
        encryptedRangeFromBlobRangeV1.toBlobRange().toHeaderValue() == encryptedRangeFromHeaderV1.toBlobRange().toHeaderValue()
        encryptedRangeFromBlobRangeV2.toBlobRange().toHeaderValue() == encryptedRangeFromHeaderV2.toBlobRange().toHeaderValue()

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

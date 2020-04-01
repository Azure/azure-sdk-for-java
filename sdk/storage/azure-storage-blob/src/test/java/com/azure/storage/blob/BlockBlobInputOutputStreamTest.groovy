package com.azure.storage.blob

import com.azure.storage.blob.models.BlobType
import com.azure.storage.blob.specialized.BlobOutputStream
import com.azure.storage.blob.specialized.BlockBlobClient
import com.azure.storage.common.implementation.Constants
import spock.lang.Requires

class BlockBlobInputOutputStreamTest extends APISpec {
    BlockBlobClient bc

    def setup() {
        bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @Requires({ liveMode() })
    def "Upload download"() {
        when:
        int length = 6 * Constants.MB
        byte[] randomBytes = getRandomByteArray(length)

        BlobOutputStream outStream = bc.getBlobOutputStream()
        outStream.write(randomBytes, 1 * Constants.MB, 5 * Constants.MB)
        outStream.close()

        then:
        def inputStream = bc.openInputStream()
        int b
        def outputStream = new ByteArrayOutputStream()
        try {
            while ((b = inputStream.read()) != -1) {
                outputStream.write(b)
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex)
        }
        def propertiesAfter = inputStream.getProperties()
        propertiesAfter.getBlobType() == BlobType.BLOCK_BLOB
        propertiesAfter.getBlobSize() == 4 * Constants.MB
        byte[] randomBytes2 = outputStream.toByteArray()
        assert randomBytes2 == Arrays.copyOfRange(randomBytes, 1 * Constants.MB, 6 * Constants.MB)
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @Requires({ liveMode() })
    def "Get properties before"() {
        when:
        int length = 6 * Constants.MB
        byte[] randomBytes = getRandomByteArray(length)

        BlobOutputStream outStream = bc.getBlobOutputStream()
        outStream.write(randomBytes, 1 * Constants.MB, 5 * Constants.MB)
        outStream.close()

        then:
        def inputStream = bc.openInputStream()
        def propertiesBefore = inputStream.getProperties()
        int b
        def outputStream = new ByteArrayOutputStream()
        try {
            while ((b = inputStream.read()) != -1) {
                outputStream.write(b)
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex)
        }
        propertiesBefore.getBlobType() == BlobType.BLOCK_BLOB
        propertiesBefore.getBlobSize() == 5 * Constants.MB
        byte[] randomBytes2 = outputStream.toByteArray()
        assert randomBytes2 == Arrays.copyOfRange(randomBytes, 1 * Constants.MB, 6 * Constants.MB)
    }
}

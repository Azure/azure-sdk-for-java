package com.azure.storage.blob

import com.azure.storage.common.Constants
import spock.lang.Requires

class BlockBlobInputOutputStreamTest extends APISpec {
    BlockBlobClient bc

    def setup() {
        bc = cc.getBlockBlobClient(generateBlobName())
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @Requires({ APISpec.liveMode() })
    def "Upload download"() {
        when:
        int length = 30 * Constants.MB
        byte[] randomBytes = getRandomByteArray(length)

        BlobOutputStream outStream = bc.getBlobOutputStream()
        outStream.write(randomBytes)
        outStream.close()

        then:
        BlobInputStream inputStream = bc.openInputStream()
        int b
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        try {
            while ((b = inputStream.read()) != -1) {
                outputStream.write(b)
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex)
        }
        byte[] randomBytes2 = outputStream.toByteArray()
        assert randomBytes2 == randomBytes
    }
}

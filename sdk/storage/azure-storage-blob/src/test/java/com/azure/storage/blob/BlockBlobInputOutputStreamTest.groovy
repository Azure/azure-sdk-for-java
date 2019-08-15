package com.azure.storage.blob

import com.azure.core.test.TestMode
import com.azure.storage.common.Constants
import spock.lang.IgnoreIf

class BlockBlobInputOutputStreamTest extends APISpec {
    BlockBlobClient bu

    def setup() {
        bu = cu.getBlockBlobClient(generateBlobName())
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @IgnoreIf({ testMode == TestMode.PLAYBACK })
    def "Upload download"() {
        when:
        int length = 30 * Constants.MB
        byte[] randomBytes = getRandomByteArray(length)

        BlobOutputStream outStream = bu.getBlobOutputStream()
        outStream.write(randomBytes)
        outStream.close()

        then:
        BlobInputStream inputStream = bu.openInputStream()
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

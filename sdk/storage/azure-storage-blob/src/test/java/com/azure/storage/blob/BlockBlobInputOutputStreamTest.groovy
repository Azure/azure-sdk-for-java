package com.azure.storage.blob

import com.azure.core.test.TestMode
import com.azure.storage.common.Constants
import org.junit.Assume

class BlockBlobInputOutputStreamTest extends APISpec {
    BlockBlobClient bu

    def setup() {
        bu = cu.getBlockBlobClient(generateBlobName())
    }

    def "Upload download"() {
        when:
        // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
        Assume.assumeTrue(testCommon.getTestMode() == TestMode.RECORD)

        int length = 30 * Constants.MB
        byte[] randomBytes = testCommon.getRandomData(length)

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

package com.azure.storage.blob


import com.azure.storage.blob.specialized.BlockBlobClient
import com.azure.storage.common.Constants
import spock.lang.Requires

class BlockBlobInputOutputStreamTest extends APISpec {
    BlockBlobClient bc

    def setup() {
        bc = cc.getBlobClient(generateBlobName()).asBlockBlobClient()
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @Requires({ liveMode() })
    def "Upload download"() {
        when:
        def length = 30 * Constants.MB
        def randomBytes = getRandomByteArray(length)

        def outStream = bc.getBlobOutputStream()
        outStream.write(randomBytes)
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
        assert outputStream.toByteArray() == randomBytes
    }
}

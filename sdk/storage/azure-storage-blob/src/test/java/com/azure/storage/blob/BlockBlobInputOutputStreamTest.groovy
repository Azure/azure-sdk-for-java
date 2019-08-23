package com.azure.storage.blob

import com.azure.storage.common.Constants

class BlockBlobInputOutputStreamTest extends APISpec {
    BlockBlobClient bc

    def setup() {
        bc = cc.getBlockBlobClient(generateBlobName())
    }

    def "Upload download"() {
        when:
        int length = 30 * Constants.MB
        byte[] randomBytes = new byte[length]
        (new Random()).nextBytes(randomBytes)

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

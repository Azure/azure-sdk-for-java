package com.azure.storage.blob

class BlockBlobInputOutputStreamTest extends APISpec {
    BlockBlobClient bu

    def setup() {
        bu = cu.getBlockBlobClient(generateBlobName())
    }

    def "Upload download"() {
        when:
        int length = 30 * Constants.MB
        byte[] randomBytes = new byte[length]
        (new Random()).nextBytes(randomBytes)

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

package com.azure.storage.blob


import com.azure.storage.common.Constants
import spock.lang.Ignore
import spock.lang.Requires

class BlobOutputStreamTest extends APISpec {
    private static int FOUR_MB = 4 * Constants.MB

    @Requires({ liveMode() })
    def "BlockBlob output stream"() {
        setup:
        def data = getRandomByteArray(10 * Constants.MB)
        def blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        def outputStream = blockBlobClient.getBlobOutputStream()
        outputStream.write(data)
        outputStream.close()

        then:
        blockBlobClient.getProperties().getBlobSize() == data.length
        convertInputStreamToByteArray(blockBlobClient.openInputStream()) == data
    }

    @Requires({ liveMode() })
    def "PageBlob output stream"() {
        setup:
        def data = getRandomByteArray(16 * Constants.MB - 512)
        def pageBlobClient = cc.getBlobClient(generateBlobName()).getPageBlobClient()
        pageBlobClient.create(data.length)


        when:
        def outputStream = pageBlobClient.getBlobOutputStream(data.length)
        outputStream.write(data)
        outputStream.close()

        then:
        convertInputStreamToByteArray(pageBlobClient.openInputStream()) == data
    }

    // Test is failing, need to investigate.
    @Ignore
    def "AppendBlob output stream"() {
        setup:
        def data = getRandomByteArray(4 * FOUR_MB)
        def appendBlobClient = cc.getBlobClient(generateBlobName()).getAppendBlobClient()
        appendBlobClient.create()

        when:
        def outputStream = appendBlobClient.getBlobOutputStream()
        for (int i = 0; i != 4; i++) {
            outputStream.write(Arrays.copyOfRange(data, i * FOUR_MB, ((i + 1) * FOUR_MB) - 1))
        }
        outputStream.close()

        then:
        appendBlobClient.getProperties().getBlobSize() == data.length
        convertInputStreamToByteArray(appendBlobClient.openInputStream()) == data
    }

    def convertInputStreamToByteArray(InputStream inputStream) {
        int b
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        try {
            while ((b = inputStream.read()) != -1) {
                outputStream.write(b)
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex)
        }

        return outputStream.toByteArray()
    }
}

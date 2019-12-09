package com.azure.storage.blob

import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.PageRange
import com.azure.storage.common.implementation.Constants
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
    def "BlockBlob output stream default no overwrite"() {
        setup:
        def data = getRandomByteArray(10 * Constants.MB)
        def blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        def outputStream1 = blockBlobClient.getBlobOutputStream()
        outputStream1.write(data)
        outputStream1.close()

        and:
        blockBlobClient.getBlobOutputStream()


        then:
        thrown(IllegalArgumentException)
    }

    @Requires({ liveMode() })
    def "BlockBlob output stream default no overwrite interrupted"() {
        setup:
        def data = getRandomByteArray(10 * Constants.MB)
        def blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        def outputStream1 = blockBlobClient.getBlobOutputStream()
        def outputStream2 = blockBlobClient.getBlobOutputStream()
        outputStream2.write(data)
        outputStream2.close()

        and:
        outputStream1.write(data)
        outputStream1.close()

        then:
        def e = thrown(IOException)
        e.getCause() instanceof BlobStorageException
        ((BlobStorageException) e.getCause()).getErrorCode() == BlobErrorCode.BLOB_ALREADY_EXISTS
    }

    @Requires({ liveMode() })
    def "BlockBlob output stream overwrite"() {
        setup:
        def data = getRandomByteArray(10 * Constants.MB)
        def blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        blockBlobClient.upload(defaultInputStream.get(), defaultDataSize)

        when:
        def outputStream = blockBlobClient.getBlobOutputStream(true)
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
        def outputStream = pageBlobClient.getBlobOutputStream(new PageRange().setStart(0).setEnd(16 * Constants.MB - 1))
        outputStream.write(data)
        outputStream.close()

        then:
        convertInputStreamToByteArray(pageBlobClient.openInputStream()) == data
    }

    // Test is failing, need to investigate.
    @Requires({ liveMode() })
    def "AppendBlob output stream"() {
        setup:
        def data = getRandomByteArray(4 * FOUR_MB)
        def appendBlobClient = cc.getBlobClient(generateBlobName()).getAppendBlobClient()
        appendBlobClient.create()

        when:
        def outputStream = appendBlobClient.getBlobOutputStream()
        for (int i = 0; i != 4; i++) {
            outputStream.write(Arrays.copyOfRange(data, i * FOUR_MB, ((i + 1) * FOUR_MB)))
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

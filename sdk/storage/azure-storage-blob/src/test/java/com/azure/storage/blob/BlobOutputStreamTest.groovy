package com.azure.storage.blob

import com.azure.storage.blob.specialized.AppendBlobClient
import com.azure.storage.blob.specialized.BlobOutputStream
import com.azure.storage.blob.specialized.BlockBlobClient
import com.azure.storage.blob.specialized.PageBlobClient
import com.azure.storage.common.Constants
import spock.lang.Ignore

class BlobOutputStreamTest extends APISpec {
    private static int FOUR_MB = 4 * Constants.MB

    @Ignore
    def "BlockBlob output stream"() {
        setup:
        byte[] data = getRandomByteArray(100 * Constants.MB)
        BlockBlobClient blockBlobClient = cc.getBlockBlobClient(generateBlobName())

        when:
        BlobOutputStream outputStream = blockBlobClient.getBlobOutputStream()
        outputStream.write(data)
        outputStream.close()

        then:
        blockBlobClient.getProperties().getBlobSize() == data.length
        convertInputStreamToByteArray(blockBlobClient.openInputStream()) == data
    }

    @Ignore
    def "PageBlob output stream"() {
        setup:
        byte[] data = getRandomByteArray(1024 * Constants.MB - 512)
        PageBlobClient pageBlobClient = cc.getPageBlobClient(generateBlobName())
        pageBlobClient.setCreate(data.length)


        when:
        BlobOutputStream outputStream = pageBlobClient.getBlobOutputStream(data.length)
        outputStream.write(data)
        outputStream.close()

        then:
        convertInputStreamToByteArray(pageBlobClient.openInputStream()) == data
    }

    @Ignore
    def "AppendBlob output stream"() {
        setup:
        byte[] data = getRandomByteArray(64 * FOUR_MB)
        AppendBlobClient appendBlobClient = cc.getAppendBlobClient(generateBlobName())
        appendBlobClient.create()

        when:
        BlobOutputStream outputStream = appendBlobClient.getBlobOutputStream()
        for (int i = 0; i != 64; i++) {
            outputStream.write(Arrays.copyOfRange(data, i * FOUR_MB, ((i + 1) * FOUR_MB) - 1))
        }
        outputStream.close()

        then:
        appendBlobClient.getProperties().getBlobSize() == data.length
        convertInputStreamToByteArray(appendBlobClient.openInputStream()) == data
    }

    private static byte[] convertInputStreamToByteArray(InputStream inputStream) {
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

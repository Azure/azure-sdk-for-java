package com.azure.storage.blob

import com.azure.core.http.HttpClient
import com.azure.core.http.HttpRequest
import com.azure.core.http.HttpResponse
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.PageRange
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.extensions.LiveOnly
import reactor.core.publisher.Mono
import spock.lang.Requires
import spock.lang.Unroll

class BlobOutputStreamTest extends APISpec {
    private static int FOUR_MB = 4 * Constants.MB

    @LiveOnly
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

    @LiveOnly
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

    @LiveOnly
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

    @LiveOnly
    def "BlockBlob output stream overwrite"() {
        setup:
        def randomData = getRandomByteArray(10 * Constants.MB)
        def blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        blockBlobClient.upload(data.defaultInputStream, data.defaultDataSize)

        when:
        def outputStream = blockBlobClient.getBlobOutputStream(true)
        outputStream.write(randomData)
        outputStream.close()

        then:
        blockBlobClient.getProperties().getBlobSize() == randomData.length
        convertInputStreamToByteArray(blockBlobClient.openInputStream()) == randomData
    }

    @Unroll
    def "BlockBlob output stream error"() {
        setup:
        def credentials = new StorageSharedKeyCredential("accountName", "accountKey")
        def endpoint = "https://account.blob.core.windows.net/"
        def data = getRandomByteArray(10 * Constants.MB)
        def ex = (Exception) exception
        def httpClient = new HttpClient() {
            @Override
            Mono<HttpResponse> send(HttpRequest httpRequest) {
                return Mono.error(ex)
            }
        }
        def blockBlobClient = new SpecializedBlobClientBuilder()
            .endpoint(endpoint)
            .containerName("container")
            .blobName("blob")
            .credential(credentials)
            .httpClient(httpClient)
            .buildBlockBlobClient()

        when:
        def outputStream = blockBlobClient.getBlobOutputStream(true)
        outputStream.write(data)
        outputStream.close()

        then:
        def e = thrown(IOException)
        if (exceptionClass != IOException) { /* IOExceptions are not wrapped. */
            assert exceptionClass.isCase(e.getCause())
        }

        where:
        exception                                  || exceptionClass
        new BlobStorageException(null, null, null) || BlobStorageException
        new IllegalArgumentException()             || IllegalArgumentException
        new IOException()                          || IOException
    }

    @LiveOnly
    def "BlockBlob output stream buffer reuse"() {
        setup:
        def data = getRandomByteArray(10 * Constants.KB)
        def inputStream = new ByteArrayInputStream(data)
        def buffer = new byte[1024]
        def blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        def outputStream = blockBlobClient.getBlobOutputStream()
        for (int i=0; i<10; i++) {
            inputStream.read(buffer)
            outputStream.write(buffer)
        }
        outputStream.close()

        then:
        blockBlobClient.getProperties().getBlobSize() == data.length
        convertInputStreamToByteArray(blockBlobClient.openInputStream()) == data
    }

    @LiveOnly
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
    @LiveOnly
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

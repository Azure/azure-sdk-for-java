package com.azure.storage.blob

import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.PageRange
import com.azure.storage.blob.models.ParallelTransferOptions
import com.azure.storage.blob.options.BlobInputStreamOptions
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.implementation.Constants
import org.junit.Ignore
import reactor.core.publisher.Mono
import spock.lang.Requires
import spock.lang.Unroll

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

    @Unroll
    def "BlockBlob output stream error"() {
        setup:
        def credentials = new StorageSharedKeyCredential("accountName", "accountKey")
        def endpoint = "https://account.blob.core.windows.net/"
        def data = getRandomByteArray(10 * Constants.MB)
        def blockBlobClient = new SpecializedBlobClientBuilder()
            .endpoint(endpoint)
            .containerName("container")
            .blobName("blob")
            .credential(credentials)
            .httpClient({ httpRequest -> return Mono.error(exception) })
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

    @Requires({ liveMode() })
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

    @Ignore // Uncomment and run in VMs with good network bandwidth
    def "BlockBlob output stream large"() {
        setup:
        def blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        def options = new BlockBlobOutputStreamOptions().setParallelTransferOptions(new ParallelTransferOptions().setBlockSizeLong(64 * Constants.MB).setMaxSingleUploadSizeLong(64 * Constants.MB).setMaxConcurrency(1))
        def blockBlobOS = blockBlobClient.getBlobOutputStream(options)
        def os = new DataOutputStream(new BufferedOutputStream(blockBlobOS))

        when:
        for(long i = 0; i < size / 8; i++) {
            os.writeLong(i)
        }
        os.close()

        then:
        blockBlobClient.getProperties().getBlobSize() == size
        def is = new DataInputStream(new BufferedInputStream(blockBlobClient.openInputStream(new BlobInputStreamOptions().setBlockSize(32 * Constants.MB))))
        for(long i = 0; i < size / 8; i++) {
            assert is.readLong() == i
        }

        where:
        size             || _
        Constants.GB     || _
        5 * Constants.GB || _ /* Run this with a heap size of 3GB locally. */
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

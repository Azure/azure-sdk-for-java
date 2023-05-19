package com.azure.storage.file.datalake

import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.extensions.LiveOnly

import com.azure.storage.file.datalake.models.DataLakeRequestConditions
import com.azure.storage.file.datalake.options.DataLakeFileInputStreamOptions
import com.azure.storage.file.datalake.options.DataLakeFileOutputStreamOptions
import spock.lang.Unroll

class FileOutputStreamTest extends APISpec {
    DataLakeFileClient fc

    def setup() {
        fc = fsc.getFileClient(generatePathName())
        fc.create()
    }

    // Only run this test in live mode since blocks are dynamically assigned
    @LiveOnly
    def "Upload download"() {
        when:
        int length = 6 * Constants.MB
        byte[] randomBytes = getRandomByteArray(length)

        OutputStream outStream = fc.getOutputStream()
        outStream.write(randomBytes, 1 * Constants.MB, 5 * Constants.MB)
        outStream.close()

        then:
        def inputStream = fc.openInputStream().getInputStream()
        int b
        def outputStream = new ByteArrayOutputStream()
        try {
            while ((b = inputStream.read()) != -1) {
                outputStream.write(b)
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex)
        }

        byte[] randomBytes2 = outputStream.toByteArray()
        assert randomBytes2 == Arrays.copyOfRange(randomBytes, 1 * Constants.MB, 6 * Constants.MB)
    }

    // Only run this test in live mode since blocks are dynamically assigned
    @LiveOnly
    @Unroll
    def "Upload download block size"() {
        when:
        int length = 6 * Constants.MB
        byte[] randomBytes = getRandomByteArray(length)

        OutputStream outStream = fc.getOutputStream()
        outStream.write(randomBytes, 0, 6 * Constants.MB)
        outStream.close()

        then:
        def inputStreamResult = fc.openInputStream(new DataLakeFileInputStreamOptions().setBlockSize(blockSize))
        def inputStream = inputStreamResult.getInputStream()
        int b
        def outputStream = new ByteArrayOutputStream()
        try {
            for (int i = 0; i < numChunks; i++) {
                b = inputStream.read()
                assert b != -1
                outputStream.write(b)
                assert inputStream.available() == sizes[i] - 1 // Make sure the internal buffer is the expected chunk size.
                // Read the rest of the chunk
                for (int j = 0; j < sizes[i] - 1; j++) {
                    b = inputStream.read()
                    assert b != -1
                    outputStream.write(b)
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex)
        }
        assert inputStream.read() == -1 // Make sure we are at the end of the stream.
        byte[] randomBytes2 = outputStream.toByteArray()
        assert randomBytes2 == randomBytes

        where:
        blockSize        || numChunks | sizes
        null             || 2         | [4 * Constants.MB, 2 * Constants.MB] // Default
        5 * Constants.MB || 2         | [5 * Constants.MB, 1 * Constants.MB] // Greater than default
        3 * Constants.MB || 2         | [3 * Constants.MB, 3 * Constants.MB] // Smaller than default
    }

    @LiveOnly
    def "Output stream with close multiple times"() {
        setup:
        def data = getRandomByteArray(10 * Constants.MB)

        when:
        // set option for allowing multiple close() calls
        OutputStream outputStream = fc.getOutputStream()

        outputStream.write(data)
        outputStream.close()
        def etag = fc.getProperties().getETag()

        then:
        assert etag == fc.getProperties().getETag()
        // call again, no exceptions should be thrown
        outputStream.close()
        assert etag == fc.getProperties().getETag()
        outputStream.close()
        assert etag == fc.getProperties().getETag()

        fc.getProperties().getFileSize() == data.length
        convertInputStreamToByteArray(fc.openInputStream().getInputStream()) == data
    }

    @LiveOnly
    def "Output stream default no overwrite"() {
        setup:
        def data = getRandomByteArray(10 * Constants.MB)
        def options = new DataLakeFileOutputStreamOptions()
            .setRequestConditions(new DataLakeRequestConditions().setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD))

        when:
        def outputStream1 = fc.getOutputStream(options)
        def outputStream2 = fc.getOutputStream(options)
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
    def "Output stream buffer reuse"() {
        setup:
        fc = fsc.getFileClient(generatePathName())
        def data = getRandomByteArray(10 * Constants.KB)
        def inputStream = new ByteArrayInputStream(data)
        def buffer = new byte[1024]

        when:
        def outputStream = fc.getOutputStream()
        for (int i=0; i<10; i++) {
            inputStream.read(buffer)
            outputStream.write(buffer)
        }
        outputStream.close()

        then:
        fc.getProperties().getFileSize() == data.length
        convertInputStreamToByteArray(fc.openInputStream().getInputStream()) == data
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

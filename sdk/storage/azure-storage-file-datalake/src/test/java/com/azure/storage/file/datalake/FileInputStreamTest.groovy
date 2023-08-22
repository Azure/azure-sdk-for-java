package com.azure.storage.file.datalake

import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.extensions.LiveOnly
import com.azure.storage.file.datalake.models.ConsistentReadControl
import com.azure.storage.file.datalake.models.DataLakeRequestConditions
import com.azure.storage.file.datalake.options.DataLakeFileInputStreamOptions
import spock.lang.Unroll

class FileInputStreamTest extends APISpec {
    DataLakeFileClient fc

    def setup() {
        fc = fsc.getFileClient(generatePathName())
        fc.create()
    }

    def "Read InputStream"() {
        setup:
        byte[] randomBytes = getRandomByteArray(length)
        fc.upload(new ByteArrayInputStream(randomBytes), length, true)

        when:
        def is = fc.openInputStream(new DataLakeFileInputStreamOptions().setBlockSize(blockSize)).getInputStream()
        def downloadedData = new byte[length]
        is.read(downloadedData)

        then:
        randomBytes == downloadedData

        where:
        length               | blockSize
        Constants.KB         | null
        4 * Constants.KB     | Constants.KB
        4 * Constants.KB + 5 | Constants.KB
    }

    @Unroll
    def "BlobInputStream read to large buffer"() {
        setup:
        byte[] data = getRandomByteArray(dataSize)
        fc.upload(new ByteArrayInputStream(data), data.length, true)
        def is = fc.openInputStream().getInputStream()
        byte[] outArr = new byte[10 * 1024 * 1024]

        when:
        def count = is.read(outArr)

        then:
        for (int i=0; i < dataSize; i++) {
            assert data[i] == outArr[i]
        }
        for (int i=dataSize; i < (outArr.length); i++) {
            assert outArr[i] == (byte) 0
        }

        count == retVal

        where:
        dataSize        || retVal
        // can't port zero-size test from blobs; datalake doesn't support empty append calls
        6 * 1024 * 1024 || 6 * 1024 * 1024 // Test for github issue #13811
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @LiveOnly
    @Unroll
    def "Upload download block size"() {
        when:
        int length = 6 * Constants.MB
        byte[] randomBytes = getRandomByteArray(length)
        fc.upload(new ByteArrayInputStream(randomBytes), length, true)

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
        def propertiesAfter = inputStreamResult.getProperties()
        propertiesAfter.getFileSize() == 6 * Constants.MB
        byte[] randomBytes2 = outputStream.toByteArray()
        assert randomBytes2 == randomBytes

        where:
        blockSize        || numChunks | sizes
        null             || 2         | [4 * Constants.MB, 2 * Constants.MB] // Default
        5 * Constants.MB || 2         | [5 * Constants.MB, 1 * Constants.MB] // Greater than default
        3 * Constants.MB || 2         | [3 * Constants.MB, 3 * Constants.MB] // Smaller than default
    }

    def "Input stream etag lock default"() {
        setup:
        int length = 6 * Constants.MB
        byte[] randomBytes = getRandomByteArray(length)
        fc.upload(new ByteArrayInputStream(randomBytes), length, true)

        // Create the input stream and read from it.
        // Note: Setting block size to 1 is inefficient but helps demonstrate the purpose of this test.
        def inputStream = fc.openInputStream(new DataLakeFileInputStreamOptions().setBlockSize(1)).getInputStream()
        inputStream.read()

        // Modify the blob again.
        fc.upload(new ByteArrayInputStream(randomBytes), length, true)

        when: "Reading after etag has been changed"
        inputStream.read()

        then:
        thrown(IOException)
    }

    def "IS consistent read control none"() {
        setup:
        int length = 6 * Constants.MB
        byte[] randomBytes = getRandomByteArray(length)
        fc.upload(new ByteArrayInputStream(randomBytes), length, true)

        // Create the input stream and read from it.
        // Note: Setting block size to 1 is inefficient but helps demonstrate the purpose of this test.
        def inputStream = fc.openInputStream(new DataLakeFileInputStreamOptions().setBlockSize(1)
            .setConsistentReadControl(ConsistentReadControl.NONE)).getInputStream()
        inputStream.read()

        // Modify the blob again.
        fc.upload(new ByteArrayInputStream(randomBytes), length, true)

        when:
        inputStream.read()

        then: "Exception should not be thrown even though blob was modified"
        notThrown(IOException)
    }

    def "IS consistent read control etag client chooses etag"() {
        setup:
        int length = Constants.KB
        byte[] randomBytes = getRandomByteArray(length)
        fc.upload(new ByteArrayInputStream(randomBytes), length, true)

        when:
        // No eTag specified - client will lock on latest one.
        def inputStream = fc.openInputStream(new DataLakeFileInputStreamOptions()
            .setConsistentReadControl(ConsistentReadControl.ETAG)).getInputStream()

        then: "Successful read"
        def outputStream = new ByteArrayOutputStream()
        def b
        while ((b = inputStream.read()) != -1) {
            outputStream.write(b)
        }

        byte[] randomBytes1 = outputStream.toByteArray()
        assert randomBytes1 == randomBytes
    }

    def "IS consistent read control etag user provides etag"() {
        setup:
        int length = Constants.KB
        byte[] randomBytes = getRandomByteArray(length)
        fc.upload(new ByteArrayInputStream(randomBytes), length, true)
        def properties = fc.getProperties()

        when:
        def inputStream = fc.openInputStream(new DataLakeFileInputStreamOptions()
            .setConsistentReadControl(ConsistentReadControl.ETAG)
            // User provides eTag to use
            .setRequestConditions(new DataLakeRequestConditions().setIfMatch(properties.getETag()))).getInputStream()

        then: "Successful read"
        def outputStream = new ByteArrayOutputStream()
        def b
        while ((b = inputStream.read()) != -1) {
            outputStream.write(b)
        }

        byte[] randomBytes1 = outputStream.toByteArray()
        assert randomBytes1 == randomBytes
    }

    // Error case
    def "IS consistent read control etag user provides old etag"() {
        setup:
        int length = Constants.KB
        byte[] randomBytes = getRandomByteArray(length)
        fc.upload(new ByteArrayInputStream(randomBytes), length, true)
        def properties = fc.getProperties()

        when:
        def inputStream = fc.openInputStream(new DataLakeFileInputStreamOptions().setBlockSize(1)
            .setConsistentReadControl(ConsistentReadControl.ETAG)
            .setRequestConditions(new DataLakeRequestConditions().setIfMatch(properties.getETag()))).getInputStream()

        // Since eTag is the only form of consistentReadControl and the blob is modified, we will throw.
        fc.upload(new ByteArrayInputStream(randomBytes), length, true)

        inputStream.read() // initial block
        inputStream.read() // trigger another download

        then: "Failed read"
        thrown(IOException) // BlobStorageException = ConditionNotMet
    }
}

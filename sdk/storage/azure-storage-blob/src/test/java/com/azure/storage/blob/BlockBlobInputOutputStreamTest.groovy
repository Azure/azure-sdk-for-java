package com.azure.storage.blob

import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobType
import com.azure.storage.blob.models.ConcurrencyControl
import com.azure.storage.blob.options.BlobInputStreamOptions
import com.azure.storage.blob.specialized.BlobOutputStream
import com.azure.storage.blob.specialized.BlockBlobClient
import com.azure.storage.common.implementation.Constants
import spock.lang.Requires
import spock.lang.Unroll

class BlockBlobInputOutputStreamTest extends APISpec {
    BlockBlobClient bc

    def setup() {
        bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
    }

    @Unroll
    def "BlobInputStream read to large buffer"() {
        setup:
        byte[] data = getRandomByteArray(dataSize)
        bc.upload(new ByteArrayInputStream(data), data.length, true)
        def is = bc.openInputStream()
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
        0               || -1
        6 * 1024 * 1024 || 6 * 1024 * 1024 // Test for github issue #13811
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @Requires({ liveMode() })
    def "Upload download"() {
        when:
        int length = 6 * Constants.MB
        byte[] randomBytes = getRandomByteArray(length)

        BlobOutputStream outStream = bc.getBlobOutputStream()
        outStream.write(randomBytes, 1 * Constants.MB, 5 * Constants.MB)
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
        def propertiesAfter = inputStream.getProperties()
        propertiesAfter.getBlobType() == BlobType.BLOCK_BLOB
        propertiesAfter.getBlobSize() == 5 * Constants.MB
        byte[] randomBytes2 = outputStream.toByteArray()
        assert randomBytes2 == Arrays.copyOfRange(randomBytes, 1 * Constants.MB, 6 * Constants.MB)
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @Requires({ liveMode() })
    @Unroll
    def "Upload download block size"() {
        when:
        int length = 6 * Constants.MB
        byte[] randomBytes = getRandomByteArray(length)

        BlobOutputStream outStream = bc.getBlobOutputStream()
        outStream.write(randomBytes, 0, 6 * Constants.MB)
        outStream.close()

        then:
        def inputStream = bc.openInputStream(new BlobInputStreamOptions().setBlockSize(blockSize))
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
        def propertiesAfter = inputStream.getProperties()
        propertiesAfter.getBlobType() == BlobType.BLOCK_BLOB
        propertiesAfter.getBlobSize() == 6 * Constants.MB
        byte[] randomBytes2 = outputStream.toByteArray()
        assert randomBytes2 == randomBytes

        where:
        blockSize        || numChunks | sizes
        null             || 2         | [4 * Constants.MB, 2 * Constants.MB] // Default
        5 * Constants.MB || 2         | [5 * Constants.MB, 1 * Constants.MB] // Greater than default
        3 * Constants.MB || 2         | [3 * Constants.MB, 3 * Constants.MB] // Smaller than default
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @Requires({ liveMode() })
    def "Get properties before"() {
        when:
        int length = 6 * Constants.MB
        byte[] randomBytes = getRandomByteArray(length)

        BlobOutputStream outStream = bc.getBlobOutputStream()
        outStream.write(randomBytes, 1 * Constants.MB, 5 * Constants.MB)
        outStream.close()

        then:
        def inputStream = bc.openInputStream()
        def propertiesBefore = inputStream.getProperties()
        int b
        def outputStream = new ByteArrayOutputStream()
        try {
            while ((b = inputStream.read()) != -1) {
                outputStream.write(b)
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex)
        }
        propertiesBefore.getBlobType() == BlobType.BLOCK_BLOB
        propertiesBefore.getBlobSize() == 5 * Constants.MB
        byte[] randomBytes2 = outputStream.toByteArray()
        assert randomBytes2 == Arrays.copyOfRange(randomBytes, 1 * Constants.MB, 6 * Constants.MB)
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @Requires({ liveMode() })
    def "Input stream etag lock default"() {
        setup:
        int length = 6 * Constants.MB
        byte[] randomBytes = getRandomByteArray(length)
        BlobOutputStream outStream = bc.getBlobOutputStream()
        outStream.write(randomBytes, 1 * Constants.MB, 5 * Constants.MB)
        outStream.close()

        // Read from the input stream
        // Note: Setting block size to 1 is inefficient but helps demonstrate the purpose of this test.
        def inputStream = bc.openInputStream(new BlobInputStreamOptions().setBlockSize(1))
        inputStream.read()

        // Modify the blob again.
        outStream = bc.getBlobOutputStream(true)
        outStream.write(randomBytes, 1 * Constants.MB, 5 * Constants.MB)
        outStream.close()

        when:
        inputStream.read()

        then:
        thrown(IOException)
    }

    @Requires({ liveMode() })
    def "Input stream concurrency control none"() {
        setup:
        int length = 6 * Constants.MB
        byte[] randomBytes = getRandomByteArray(length)
        BlobOutputStream outStream = bc.getBlobOutputStream()
        outStream.write(randomBytes, 1 * Constants.MB, 5 * Constants.MB)
        outStream.close()

        // Read from the input stream
        // Note: Setting block size to 1 is inefficient but helps demonstrate the purpose of this test.
        def inputStream = bc.openInputStream(new BlobInputStreamOptions().setBlockSize(1).setConcurrencyControl(ConcurrencyControl.NONE))
        inputStream.read()

        // Modify the blob again.
        outStream = bc.getBlobOutputStream(true)
        outStream.write(randomBytes, 1 * Constants.MB, 5 * Constants.MB)
        outStream.close()

        when:
        inputStream.read()

        then: "Exception should not be thrown even though blob was modified"
        notThrown(IOException)
    }

    @Requires({ liveMode() })
    def "Input stream concurrency control none error"() {
        setup:
        int length = 6 * Constants.MB
        byte[] randomBytes = getRandomByteArray(length)
        def blobContainerClient = versionedBlobServiceClient.createBlobContainer(containerName)
        def blobClient = blobContainerClient.getBlobClient(generateBlobName())
        BlobOutputStream outStream = blobClient.getBlockBlobClient().getBlobOutputStream()
        outStream.write(randomBytes, 1 * Constants.MB, 5 * Constants.MB)
        outStream.close()
        def properties = blobClient.getProperties()

        when:
        blobClient.openInputStream(new BlobInputStreamOptions().setBlockSize(1).setConcurrencyControl(ConcurrencyControl.NONE)
            .setRequestConditions(new BlobRequestConditions().setIfMatch(properties.getETag())))

        then:
        thrown(UnsupportedOperationException)

        when:
        blobClient.getVersionClient(properties.getVersionId()).openInputStream(new BlobInputStreamOptions().setBlockSize(1).setConcurrencyControl(ConcurrencyControl.NONE))

        then:
        thrown(UnsupportedOperationException)
    }

    @Requires({ liveMode() })
    def "Input stream concurrency control etag"() {
        setup:
        int length = 6 * Constants.MB
        byte[] randomBytes = getRandomByteArray(length)
        def blobContainerClient = versionedBlobServiceClient.createBlobContainer(containerName)
        def blobClient = blobContainerClient.getBlobClient(generateBlobName())
        BlobOutputStream outStream = blobClient.getBlockBlobClient().getBlobOutputStream()
        outStream.write(randomBytes, 1 * Constants.MB, 5 * Constants.MB)
        outStream.close()
        def properties = blobClient.getProperties()

        when: "Use recent eTag"
        def inputStream = blobClient.openInputStream(new BlobInputStreamOptions().setConcurrencyControl(ConcurrencyControl.ETAG)
            .setRequestConditions(new BlobRequestConditions().setIfMatch(properties.getETag())))

        then:
        def outputStream = new ByteArrayOutputStream()
        def b
        try {
            while ((b = inputStream.read()) != -1) {
                outputStream.write(b)
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex)
        }
        byte[] randomBytes2 = outputStream.toByteArray()
        assert randomBytes2 == Arrays.copyOfRange(randomBytes, 1 * Constants.MB, 6 * Constants.MB)

        when: "Use old eTag"
        // Write more bytes (just to change eTag).
        outStream = blobClient.getBlockBlobClient().getBlobOutputStream(true)
        outStream.write(getRandomByteArray(length), 1 * Constants.MB, 5 * Constants.MB)
        outStream.close()

        blobClient.openInputStream(new BlobInputStreamOptions().setBlockSize(1).setConcurrencyControl(ConcurrencyControl.ETAG)
            .setRequestConditions(new BlobRequestConditions().setIfMatch(properties.getETag()))).read()

        then: "An old etag will fail due to ConditionNotMet"
        thrown(IOException)
    }

    @Requires({ liveMode() })
    def "Input stream concurrency control version"() {
        setup:
        int length = 6 * Constants.MB
        byte[] randomBytes = getRandomByteArray(length)
        def blobContainerClient = versionedBlobServiceClient.createBlobContainer(containerName)
        def blobClient = blobContainerClient.getBlobClient(generateBlobName())
        BlobOutputStream outStream = blobClient.getBlockBlobClient().getBlobOutputStream()
        outStream.write(randomBytes, 1 * Constants.MB, 5 * Constants.MB)
        outStream.close()
        def oldProperties = blobClient.getProperties()

        when: "Use recent version"
        def inputStream = blobClient.openInputStream(new BlobInputStreamOptions().setConcurrencyControl(ConcurrencyControl.VERSION_ID))
        // Write more bytes (just to change version).
        outStream = blobClient.getBlockBlobClient().getBlobOutputStream(true)
        outStream.write(getRandomByteArray(length), 1 * Constants.MB, 5 * Constants.MB)
        outStream.close()

        then:
        def outputStream = new ByteArrayOutputStream()
        def b
        try {
            while ((b = inputStream.read()) != -1) {
                outputStream.write(b)
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex)
        }
        byte[] randomBytes2 = outputStream.toByteArray()
        assert randomBytes2 == Arrays.copyOfRange(randomBytes, 1 * Constants.MB, 6 * Constants.MB)

        when: "Use old version"
        inputStream = blobClient.getVersionClient(oldProperties.getVersionId()).openInputStream(new BlobInputStreamOptions().setConcurrencyControl(ConcurrencyControl.VERSION_ID))

        outputStream = new ByteArrayOutputStream()
        try {
            while ((b = inputStream.read()) != -1) {
                outputStream.write(b)
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex)
        }
        randomBytes2 = outputStream.toByteArray()

        then:
        assert randomBytes2 == Arrays.copyOfRange(randomBytes, 1 * Constants.MB, 6 * Constants.MB)
    }


}

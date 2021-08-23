package com.azure.storage.blob

import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobType
import com.azure.storage.blob.models.ConsistentReadControl
import com.azure.storage.blob.options.BlobInputStreamOptions
import com.azure.storage.blob.specialized.BlobOutputStream
import com.azure.storage.blob.specialized.BlockBlobClient
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.extensions.LiveOnly
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion
import spock.lang.Requires
import spock.lang.Unroll

import java.nio.ByteBuffer

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
        for (int i = 0; i < dataSize; i++) {
            assert data[i] == outArr[i]
        }
        for (int i = dataSize; i < (outArr.length); i++) {
            assert outArr[i] == (byte) 0
        }

        count == retVal

        // I think this first test case is failing. What does download file do? I think it catches this exception on initial download
        // Does the way I've written it work if the blob is < 4mb but just non-zero? i.e. does the service accept an end range that is past blob
        where:
        dataSize        || retVal
        0               || -1
        6 * 1024 * 1024 || 6 * 1024 * 1024 // Test for github issue #13811
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @LiveOnly
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
    @LiveOnly
    @Unroll
    def "Upload download block size"() {
        when:
        int length = 6 * Constants.MB
        byte[] randomBytes = getRandomByteArray(length)

        BlobOutputStream outStream = bc.getBlobOutputStream(true)
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
                assert inputStream.available() == sizes[i] - 1
                // Make sure the internal buffer is the expected chunk size.
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
    @LiveOnly
    def "BlobRange"() {
        setup:
        int length = 6 * Constants.MB
        byte[] randomBytes = getRandomByteArray(length)

        BlobOutputStream outStream = bc.getBlobOutputStream(true)
        outStream.write(randomBytes, 0, 6 * Constants.MB)
        outStream.close()

        def resultBytes = new byte[count == null ? length : count]

        when:
        def inputStream = bc.openInputStream(new BlobInputStreamOptions().setRange(new BlobRange(start, count))
            .setBlockSize(4 * Constants.MB))
        inputStream.read(resultBytes) // read the whole range

        then:
        inputStream.read() == -1
        ByteBuffer.wrap(randomBytes, start, count)  == ByteBuffer.wrap(resultBytes)

        where:
        start            | count
        0                | 100 // Small range
        0                | 4 * Constants.MB // block size
        0                | 5 * Constants.MB // Requires multiple chunks
        5                | 100 // small offset
        1 * Constants.MB | 2 * Constants.MB // larger offset inside first chunk
        1 * Constants.KB | 4 * Constants.MB // offset with range spanning chunks
        5 * Constants.MB | (1 * Constants.KB) // Range entirely in second chunk
        // full blob tested in other tests
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @LiveOnly
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

    def "Input stream etag lock default"() {
        setup:
        int length = 6 * Constants.MB
        byte[] randomBytes = getRandomByteArray(length)
        bc.upload(new ByteArrayInputStream(randomBytes), length, true)

        // Create the input stream and read from it.
        // Note: Setting block size to 1 is inefficient but helps demonstrate the purpose of this test.
        def inputStream = bc.openInputStream(new BlobInputStreamOptions().setBlockSize(1))
        inputStream.read()

        // Modify the blob again.
        bc.upload(new ByteArrayInputStream(randomBytes), length, true)

        when: "Reading after etag has been changed"
        inputStream.read()

        then:
        thrown(IOException)
    }

    def "IS consistent read control none"() {
        setup:
        int length = 6 * Constants.MB
        byte[] randomBytes = getRandomByteArray(length)
        bc.upload(new ByteArrayInputStream(randomBytes), length, true)

        // Create the input stream and read from it.
        // Note: Setting block size to 1 is inefficient but helps demonstrate the purpose of this test.
        def inputStream = bc.openInputStream(new BlobInputStreamOptions().setBlockSize(1).setConsistentReadControl(ConsistentReadControl.NONE))
        inputStream.read()

        // Modify the blob again.
        bc.upload(new ByteArrayInputStream(randomBytes), length, true)

        when:
        inputStream.read()

        then: "Exception should not be thrown even though blob was modified"
        notThrown(IOException)
    }

    def "IS consistent read control etag client chooses etag"() {
        setup:
        int length = Constants.KB
        byte[] randomBytes = getRandomByteArray(length)
        def blobContainerClient = versionedBlobServiceClient.createBlobContainer(containerName)
        def blobClient = blobContainerClient.getBlobClient(generateBlobName())
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true)

        when:
        // No eTag specified - client will lock on latest one.
        def inputStream = blobClient.openInputStream(new BlobInputStreamOptions().setConsistentReadControl(ConsistentReadControl.ETAG))
        def outputStream = new ByteArrayOutputStream()

        then: "Successful read"
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
        def blobContainerClient = versionedBlobServiceClient.createBlobContainer(containerName)
        def blobClient = blobContainerClient.getBlobClient(generateBlobName())
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true)
        def properties = blobClient.getProperties()

        when:
        def inputStream = blobClient.openInputStream(new BlobInputStreamOptions().setConsistentReadControl(ConsistentReadControl.ETAG)
        // User provides eTag to use
            .setRequestConditions(new BlobRequestConditions().setIfMatch(properties.getETag())))
        def outputStream = new ByteArrayOutputStream()

        then: "Successful read"
        def b
        while ((b = inputStream.read()) != -1) {
            outputStream.write(b)
        }

        byte[] randomBytes1 = outputStream.toByteArray()
        assert randomBytes1 == randomBytes
    }

    def "IS consistent read control etag user provides version and etag"() {
        setup:
        int length = Constants.KB
        byte[] randomBytes = getRandomByteArray(length)
        def blobContainerClient = versionedBlobServiceClient.createBlobContainer(containerName)
        def blobClient = blobContainerClient.getBlobClient(generateBlobName())
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true)
        def properties = blobClient.getProperties()

        when:
        // User provides version client
        def inputStream = blobClient.getVersionClient(properties.getVersionId()).openInputStream(new BlobInputStreamOptions().setConsistentReadControl(ConsistentReadControl.ETAG)
        // User provides eTag to use
            .setRequestConditions(new BlobRequestConditions().setIfMatch(properties.getETag())))
        def outputStream = new ByteArrayOutputStream()

        then: "Successful read"
        def b
        while ((b = inputStream.read()) != -1) {
            outputStream.write(b)
        }

        byte[] randomBytes1 = outputStream.toByteArray()
        assert randomBytes1 == randomBytes
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "IS consistent read control etag user provides version client chooses etag"() {
        setup:
        int length = Constants.KB
        byte[] randomBytes = getRandomByteArray(length)
        def blobContainerClient = versionedBlobServiceClient.createBlobContainer(containerName)
        def blobClient = blobContainerClient.getBlobClient(generateBlobName())
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true)
        def properties = blobClient.getProperties()

        when:
        // User provides version client
        def inputStream = blobClient.getVersionClient(properties.getVersionId()).openInputStream(new BlobInputStreamOptions().setConsistentReadControl(ConsistentReadControl.ETAG))
        def outputStream = new ByteArrayOutputStream()
        // When a versioned client is used it should still succeed if the blob has been modified
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true)

        then: "Successful read"
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
        def blobContainerClient = versionedBlobServiceClient.createBlobContainer(containerName)
        def blobClient = blobContainerClient.getBlobClient(generateBlobName())
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true)
        def properties = blobClient.getProperties()

        when:
        def inputStream = blobClient.openInputStream(new BlobInputStreamOptions().setBlockSize(1)
            .setConsistentReadControl(ConsistentReadControl.ETAG)
            .setBlockSize(500) // Set the block size to be small enough to not retrieve the whole blob on initial download
            .setRequestConditions(new BlobRequestConditions().setIfMatch(properties.getETag())))

        // Since eTag is the only form of consistentReadControl and the blob is modified, we will throw.
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true)

        inputStream.read(new byte[600]) // Read enough to exceed the initial download

        then: "Failed read"
        thrown(IOException) // BlobStorageException = ConditionNotMet
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "IS consistent read control version client chooses version"() {
        setup:
        int length = Constants.KB
        byte[] randomBytes = getRandomByteArray(length)
        def blobContainerClient = versionedBlobServiceClient.createBlobContainer(containerName)
        def blobClient = blobContainerClient.getBlobClient(generateBlobName())
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true)

        when:
        // No version specified - client will lock on it.
        def inputStream = blobClient.openInputStream(new BlobInputStreamOptions().setConsistentReadControl(ConsistentReadControl.VERSION_ID))
        def outputStream = new ByteArrayOutputStream()

        // When a versioned client is used it should still succeed if the blob has been modified
        blobClient.upload(new ByteArrayInputStream(getRandomByteArray(length)), length, true)

        then:
        def b
        while ((b = inputStream.read()) != -1) {
            outputStream.write(b)
        }
        byte[] randomBytes2 = outputStream.toByteArray()
        assert randomBytes2 == randomBytes
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "IS consistent read control version user provides version"() {
        setup:
        int length = Constants.KB
        byte[] randomBytes = getRandomByteArray(length)
        def blobContainerClient = versionedBlobServiceClient.createBlobContainer(containerName)
        def blobClient = blobContainerClient.getBlobClient(generateBlobName())
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true)
        def properties = blobClient.getProperties()

        blobClient.upload(new ByteArrayInputStream(getRandomByteArray(length)), length, true)

        when:
        // User provides version client
        def inputStream = blobClient.getVersionClient(properties.getVersionId()).openInputStream(new BlobInputStreamOptions().setConsistentReadControl(ConsistentReadControl.VERSION_ID))
        def outputStream = new ByteArrayOutputStream()

        // When a versioned client is used it should still succeed if the blob has been modified
        blobClient.upload(new ByteArrayInputStream(getRandomByteArray(length)), length, true)

        then:
        def b
        while ((b = inputStream.read()) != -1) {
            outputStream.write(b)
        }
        byte[] randomBytes2 = outputStream.toByteArray()
        assert randomBytes2 == randomBytes
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "IS consistent read control version user provides version and etag"() {
        setup:
        int length = Constants.KB
        byte[] randomBytes = getRandomByteArray(length)
        def blobContainerClient = versionedBlobServiceClient.createBlobContainer(containerName)
        def blobClient = blobContainerClient.getBlobClient(generateBlobName())
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true)
        def properties = blobClient.getProperties()

        when:
        // User provides version client
        def inputStream = blobClient.getVersionClient(properties.getVersionId()).openInputStream(new BlobInputStreamOptions().setConsistentReadControl(ConsistentReadControl.VERSION_ID)
        // User provides eTag to use
            .setRequestConditions(new BlobRequestConditions().setIfMatch(properties.getETag())))
        def outputStream = new ByteArrayOutputStream()

        // When a versioned client is used it should still succeed if the blob has been modified
        blobClient.upload(new ByteArrayInputStream(getRandomByteArray(length)), length, true)

        then:
        def b
        while ((b = inputStream.read()) != -1) {
            outputStream.write(b)
        }
        byte[] randomBytes2 = outputStream.toByteArray()
        assert randomBytes2 == randomBytes
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "IS consistent read control version user provides etag client chooses version"() {
        setup:
        int length = Constants.KB
        byte[] randomBytes = getRandomByteArray(length)
        def blobContainerClient = versionedBlobServiceClient.createBlobContainer(containerName)
        def blobClient = blobContainerClient.getBlobClient(generateBlobName())
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true)
        def properties = blobClient.getProperties()

        when:
        // No version specified - client will lock on it.
        def inputStream = blobClient.openInputStream(new BlobInputStreamOptions().setConsistentReadControl(ConsistentReadControl.VERSION_ID)
        // User provides eTag to use
            .setRequestConditions(new BlobRequestConditions().setIfMatch(properties.getETag())))
        def outputStream = new ByteArrayOutputStream()
        // When a versioned client is used it should still succeed if the blob has been modified
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true)

        then: "Successful read"
        def b
        while ((b = inputStream.read()) != -1) {
            outputStream.write(b)
        }

        byte[] randomBytes1 = outputStream.toByteArray()
        assert randomBytes1 == randomBytes
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "IS consistent read control valid states"() {
        setup:
        def blobContainerClient = versionedBlobServiceClient.createBlobContainer(containerName)
        def blobClient = blobContainerClient.getBlobClient(generateBlobName())
        blobClient.upload(new ByteArrayInputStream(new byte[0]), 0, true)
        def properties = blobClient.getProperties()

        when:
        if (useVersionId) {
            blobClient = blobClient.getVersionClient(properties.getVersionId())
        }
        def requestConditions = null
        if (useETag) {
            requestConditions = new BlobRequestConditions().setIfMatch(properties.getETag())
        }
        blobClient.openInputStream(new BlobInputStreamOptions().setConsistentReadControl(consistentReadControl)
            .setRequestConditions(requestConditions))

        then:
        notThrown(IllegalStateException)

        where:
        useETag | useVersionId | consistentReadControl            || _
        true    | false        | ConsistentReadControl.NONE       || _
        false   | true         | ConsistentReadControl.NONE       || _
        true    | false        | ConsistentReadControl.VERSION_ID || _
        false   | true         | ConsistentReadControl.ETAG       || _
        true    | true         | ConsistentReadControl.VERSION_ID || _
        true    | true         | ConsistentReadControl.ETAG       || _
    }

}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio

import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.BlockListType
import com.azure.storage.blob.models.ParallelTransferOptions
import com.azure.storage.blob.specialized.BlockBlobClient
import com.azure.storage.common.test.shared.extensions.LiveOnly
import spock.lang.Ignore
import spock.lang.Requires
import spock.lang.Unroll

import java.nio.file.ClosedFileSystemException

class NioBlobOutputStreamTest extends APISpec {
    BlockBlobClient bc
    NioBlobOutputStream nioStream
    def blockSize = 50
    def maxSingleUploadSize = 200
    AzureFileSystem fs

    def setup() {
        cc.create()
        bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        fs = createFS(initializeConfigMap())
        def path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), bc.getBlobName()))

        nioStream = new NioBlobOutputStream(bc.getBlobOutputStream(new ParallelTransferOptions(blockSize, null, null,
            maxSingleUploadSize), null, null, null, null), path)
    }

    def cleanup() {
        cc.delete()
    }

    def "Write min"() {
        when:
        nioStream.write(1)
        nioStream.close()

        then:
        bc.getProperties().getBlobSize() == 1

        when:
        def inputStream = bc.openInputStream()

        then:
        inputStream.read() == 1
        inputStream.read() == -1
    }

    @LiveOnly // Because we upload in blocks
    @Ignore("failing in ci")
    def "Write min error"() {
        // Create an append blob at the destination to ensure writes fail. Customers should eventually be notified via
        // writing that there was an error
        setup:
        def abc = cc.getBlobClient(bc.getBlobName()).getAppendBlobClient()
        abc.create()

        // Write enough data to force making network requests.
        nioStream.write(getRandomByteArray(maxSingleUploadSize + 1))
        // Issue a spurious request: A more reliable way than sleeping to ensure the previous stage block has enough
        // time to round trip.
        bc.getProperties()

        when:
        nioStream.write(1)

        then:
        thrown(IOException)
    }

    def "Write array"() {
        setup:
        def dataSize = 100
        def data = getRandomByteArray(dataSize)

        when:
        nioStream.write(data)
        nioStream.close()

        then:
        bc.getProperties().getBlobSize() == dataSize

        when:
        def inputStream = bc.openInputStream()

        then:
        compareInputStreams(inputStream, new ByteArrayInputStream(data), dataSize)
    }

    @LiveOnly // Because we upload in blocks
    @Ignore("Ci failures")
    def "Write array error"() {
        // Create an append blob at the destination to ensure writes fail. Customers should eventually be notified via
        // writing that there was an error
        setup:
        def abc = cc.getBlobClient(bc.getBlobName()).getAppendBlobClient()
        abc.create()

        /*
         Write enough data to force making network requests. The error will not be thrown until the next time a method
         on the stream is called.
         */
        nioStream.write(getRandomByteArray(maxSingleUploadSize + 1))
        // Issue a spurious request: A more reliable way than sleeping to ensure the previous stage block has enough
        // time to round trip.
        bc.getProperties()

        when:
        nioStream.write(new byte[1])

        then:
        thrown(IOException)
    }

    @Unroll
    def "Write offset len"() {
        setup:
        def dataSize = 100
        def data = getRandomByteArray(dataSize)

        when:
        nioStream.write(data, offset, len)
        nioStream.close()

        then:
        bc.getProperties().getBlobSize() == len

        when:
        def inputStream = bc.openInputStream()

        then:
        compareInputStreams(inputStream, new ByteArrayInputStream(data, offset, len), dataSize)

        where:
        offset | len
        0      | 100
        20     | 80
        20     | 40
    }

    // To ensure the error isn't being wrapped unnecessarily
    def "Write offset len IOB"() {
        when:
        nioStream.write(new byte[5], -1, 6)

        then:
        thrown(IndexOutOfBoundsException)
    }

    @LiveOnly // Because we upload in blocks
    @Ignore("failing in ci")
    def "Write offset len network error"() {
        // Create an append blob at the destination to ensure writes fail. Customers should eventually be notified via
        // writing that there was an error
        setup:
        def abc = cc.getBlobClient(bc.getBlobName()).getAppendBlobClient()
        abc.create()

        // Write enough data to force making network requests.
        nioStream.write(getRandomByteArray(maxSingleUploadSize + 1))
        // Issue a spurious request: A more reliable way than sleeping to ensure the previous stage block has enough
        // time to round trip.
        bc.getProperties()

        when:
        nioStream.write(new byte[1], 0, 1)

        then:
        thrown(IOException)
    }

    def "Write fs closed"() {
        when:
        fs.close()
        nioStream.write(5)

        then:
        thrown(ClosedFileSystemException)

        when:
        nioStream.write(new byte[5])

        then:
        thrown(ClosedFileSystemException)

        when:
        nioStream.write(new byte[5], 2, 1)

        then:
        thrown(ClosedFileSystemException)
    }

    // Flush does not actually flush data right now
    def "Flush"() {
        setup:
        nioStream.write(1)

        when:
        nioStream.flush()
        bc.listBlocks(BlockListType.ALL)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.BLOB_NOT_FOUND
    }

    // Flush should at least check the stream state
    @LiveOnly // Because we upload in blocks
    @Ignore("failing in ci")
    def "Flush error"() {
        // Create an append blob at the destination to ensure writes fail. Customers should eventually be notified via
        // writing that there was an error
        setup:
        def abc = cc.getBlobClient(bc.getBlobName()).getAppendBlobClient()
        abc.create()

        // Write enough data to force making network requests.
        nioStream.write(getRandomByteArray(maxSingleUploadSize + 1))
        // Issue a spurious request: A more reliable way than sleeping to ensure the previous stage block has enough
        // time to round trip.
        bc.getProperties()

        when:
        nioStream.flush()

        then:
        thrown(IOException)
    }

    def "Flush closed fs"() {
        setup:
        nioStream.write(1)

        when:
        fs.close()
        nioStream.flush()

        then:
        thrown(ClosedFileSystemException)
    }

    def "Close"() {
        when:
        nioStream.close()
        nioStream.write(1)

        then:
        thrown(IOException)
    }

    def "Close error"() {
        when:
        nioStream.close()
        nioStream.close()

        then:
        thrown(IOException)
    }

    def "Close fs closed"() {
        when:
        fs.close()
        nioStream.close()

        then:
        thrown(ClosedFileSystemException)
    }
}


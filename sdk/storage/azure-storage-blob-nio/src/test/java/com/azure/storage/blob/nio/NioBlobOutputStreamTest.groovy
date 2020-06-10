package com.azure.storage.blob.nio

import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.BlockListType
import com.azure.storage.blob.models.ParallelTransferOptions
import com.azure.storage.blob.specialized.BlockBlobClient
import spock.lang.Unroll

class NioBlobOutputStreamTest extends APISpec {
    BlockBlobClient bc
    NioBlobOutputStream nioStream

    def setup() {
        cc.create()
        bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        nioStream = new NioBlobOutputStream(bc.getBlobOutputStream())
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

    def "Write min error"() {

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

    def "Write array error"() {

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

    def "Write offset len network error"() {

    }

    // Flush is a no-op right now
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

    /*
     Since this isn't really useful (closed state only gets checked on close and flush, which is also a no-op, maybe we
     should just doc close as a no-op and swallow errors. Can't do that really because it's how we manifest errors we
     got while writing.
     */
    def "Close"() {
        when:
        nioStream.close()
        nioStream.write(1)
        // TODO: Fix. Decide on behavior
        then:
        thrown(IOException)
    }

    def "Close error"() {

    }

    // all overloads of write
    // all overloads of write throwing an error
    // close (manifesting errors, throwing an error upon writing again)
    // flush. Should stage a block
}


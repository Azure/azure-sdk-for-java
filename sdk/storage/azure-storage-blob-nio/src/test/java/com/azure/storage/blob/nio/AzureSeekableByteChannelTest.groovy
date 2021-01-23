// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.storage.blob.BlobClient

import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import java.nio.file.ClosedFileSystemException

class AzureSeekableByteChannelTest extends APISpec {

    int sourceFileSize
    File sourceFile
    BlobClient bc
    AzureSeekableByteChannel readByteChannel
    AzureSeekableByteChannel writeByteChannel
    FileInputStream fileStream
    AzureFileSystem fs

    def setup() {
        sourceFileSize = 10 * 1024 * 1024
        sourceFile = getRandomFile(sourceFileSize)

        cc.create()
        bc = cc.getBlobClient(generateBlobName())
        bc.uploadFromFile(sourceFile.getPath())
        fs = createFS(initializeConfigMap())
        def path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), bc.getBlobName()))

        readByteChannel = new AzureSeekableByteChannel(new NioBlobInputStream(bc.openInputStream(), path), path)
        writeByteChannel = new AzureSeekableByteChannel(
            new NioBlobOutputStream(bc.getBlockBlobClient().getBlobOutputStream(true), path), path)
        fileStream = new FileInputStream(sourceFile)
    }

    def "Read"() {
        // Generate buffers of random sizes and do reads until the end. Then compare arrays
        // look at input stream tests
    }

    def "Read fs close"() {
        when:
        fs.close()
        readByteChannel.read(ByteBuffer.allocate(1))

        then:
        thrown(ClosedFileSystemException)
    }

    def "Write"() {
        // look at output stream tests
    }

    def "Write fs close"() {
        when:
        fs.close()
        writeByteChannel.write(ByteBuffer.allocate(1))

        then:
        thrown(ClosedFileSystemException)
    }

    def "Position read"() {
        setup:
        def bufferSize = (int) (sourceFileSize / 10)
        def dest = ByteBuffer.allocate(bufferSize)

        expect:
        readByteChannel.position() == 0

        for (int i=0; i<10; i++) {
            readByteChannel.read(dest)
            assert readByteChannel.position() == (i+1) * bufferSize
            dest.flip()
        }
    }

    def "Position Size write"() {
        setup:
        def bufferSize = (int) (sourceFileSize / 10)
        def src = getRandomData(bufferSize)

        expect:
        writeByteChannel.position() == 0

        for (int i=0; i<10; i++) {
            writeByteChannel.write(src)
            assert writeByteChannel.position() == (i+1) * bufferSize
            assert writeByteChannel.size() == writeByteChannel.position()
            src.flip()
        }
    }

    def "Position fs close"() {
        setup:
        fs.close()

        when:
        readByteChannel.position()

        then:
        thrown(ClosedFileSystemException)

        when:
        writeByteChannel.position()

        then:
        thrown(ClosedFileSystemException)
    }

    def "Seek"() {
        // Check that position is updated and also that reading the data from various points works
    }

    def "Seek fs close"() {
        when:
        fs.close()
        readByteChannel.position(0)

        then:
        thrown(ClosedFileSystemException)
    }

    def "Size read"() {
        expect:
        readByteChannel.size() == 10 * 1024 * 1024

        and:
        bc.upload(defaultInputStream.get(), defaultDataSize, true)
        def path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), bc.getBlobName()))
        readByteChannel = new AzureSeekableByteChannel(new NioBlobInputStream(bc.openInputStream(), path), path)

        then:
        readByteChannel.size() == defaultDataSize
    }

    def "Size fs closed"() {
        when:
        fs.close()
        readByteChannel.size()

        then:
        thrown(ClosedFileSystemException)

        when:
        writeByteChannel.size()

        then:
        thrown(ClosedFileSystemException)
    }

    def "Close"() {
        setup:
        readByteChannel.close()

        when:
        readByteChannel.read(ByteBuffer.allocate(1))

        then:
        thrown(ClosedChannelException)

        when:
        readByteChannel.size()

        then:
        thrown(ClosedChannelException)

        when:
        readByteChannel.position()

        then:
        thrown(ClosedChannelException)

        when:
        writeByteChannel.write(ByteBuffer.allocate(1))

        then:
        thrown(ClosedChannelException)

        when:
        writeByteChannel.position()

        then:
        thrown(ClosedChannelException)

        when:
        writeByteChannel.size()

        then:
        thrown(ClosedChannelException)
    }

    def "Close fs close"() {
        when:
        fs.close()
        readByteChannel.close()

        then:
        thrown(ClosedFileSystemException)

        when:
        writeByteChannel.close()

        then:
        thrown(ClosedFileSystemException)
    }

    def "isOpen"() {
        expect:
        readByteChannel.isOpen()
        writeByteChannel.isOpen()

        and:
        readByteChannel.close()
        writeByteChannel.close()

        then:
        !readByteChannel.isOpen()
        !writeByteChannel.isOpen()
    }

    def "isOpen fs close"() {
        when:
        fs.close()
        readByteChannel.isOpen()

        then:
        thrown(ClosedFileSystemException)

        when:
        writeByteChannel.isOpen()

        then:
        thrown(ClosedFileSystemException)
    }

    def "Unsupported operations"() {
        when:
        readByteChannel.write(ByteBuffer.allocate(1))

        then:
        thrown(UnsupportedOperationException)

        when:
        writeByteChannel.read(ByteBuffer.allocate(1))

        then:
        thrown(UnsupportedOperationException)

        when:
        writeByteChannel.position(5)

        then:
        thrown(UnsupportedOperationException)

        when:
        readByteChannel.truncate(0)

        then:
        thrown(UnsupportedOperationException)

        when:
        writeByteChannel.truncate(0)

        then:
        thrown(UnsupportedOperationException)
    }

    // Test in file system provider: Invalid options
    // Read
    // write
    // seek
}

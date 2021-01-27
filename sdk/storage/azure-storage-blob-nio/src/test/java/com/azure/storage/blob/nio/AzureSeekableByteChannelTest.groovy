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
    BlobClient writeBc
    AzureSeekableByteChannel readByteChannel
    AzureSeekableByteChannel writeByteChannel
    FileInputStream fileStream
    AzureFileSystem fs

    def setup() {
        sourceFileSize = 10 * 1024 * 1024
        sourceFile = getRandomFile(sourceFileSize)

        cc.create()
        bc = cc.getBlobClient(generateBlobName())
        writeBc = cc.getBlobClient(generateBlobName())
        bc.uploadFromFile(sourceFile.getPath())
        fs = createFS(initializeConfigMap())
        def path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), bc.getBlobName()))

        readByteChannel = new AzureSeekableByteChannel(new NioBlobInputStream(bc.openInputStream(), path), path)
        // For writing, we don't want a blob to exist there yet
        writeByteChannel = new AzureSeekableByteChannel(
            new NioBlobOutputStream(writeBc.getBlockBlobClient().getBlobOutputStream(true), path), path)
        fileStream = new FileInputStream(sourceFile)
    }

    def "Read"() {
        setup:
        def fileContent = new byte[sourceFileSize]
        fileStream.read(fileContent)
        def os = new ByteArrayOutputStream()
        int count = 0
        def rand = new Random()

        when:
        while (count < sourceFileSize) {
            def buffer = ByteBuffer.allocate(rand.nextInt(1024 * 1024))
            int readAmount = readByteChannel.read(buffer)
            os.write(buffer.array(), 0, readAmount) // limit the write in case we allocated more than we needed
            count += readAmount
        }

        then:
        os.toByteArray() == fileContent
    }

    def "Read fs close"() {
        when:
        fs.close()
        readByteChannel.read(ByteBuffer.allocate(1))

        then:
        thrown(ClosedFileSystemException)
    }

    def "Write"() {
        setup:
        def fileContent = new byte[sourceFileSize]
        int count = 0
        def rand = new Random()

        when:
        while (count < sourceFileSize) {
            int writeAmount = Math.min(rand.nextInt(1024 * 1024), sourceFileSize - count)
            def buffer = new byte[writeAmount]
            fileStream.read(buffer)
            writeByteChannel.write(ByteBuffer.wrap(buffer))
            count += writeAmount
        }

        then:
        compareInputStreams(writeBc.openInputStream(), new FileInputStream(sourceFile), 0)
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

        for (int i = 0; i < 10; i++) {
            readByteChannel.read(dest)
            assert readByteChannel.position() == (i + 1) * bufferSize
            dest.flip()
        }
    }

    def "Position Size write"() {
        setup:
        def bufferSize = (int) (sourceFileSize / 10)
        def src = getRandomData(bufferSize)

        expect:
        writeByteChannel.position() == 0

        for (int i = 0; i < 10; i++) {
            writeByteChannel.write(src)
            assert writeByteChannel.position() == (i + 1) * bufferSize
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
        setup:
        def streamContent = ByteBuffer.allocate(readCount0)

        when:
        readByteChannel.read(streamContent)
        def is = new ByteArrayInputStream(streamContent.array())

        then:
        compareInputStreams(fileStream, is, readCount0)

        when:
        readByteChannel.position(seekPos1)

        then:
        readByteChannel.position() == seekPos1

        when:
        fileStream = new FileInputStream(sourceFile)
        fileStream.skip(seekPos1)
        streamContent = ByteBuffer.allocate(readCount1)
        readByteChannel.read(streamContent)
        is = new ByteArrayInputStream(streamContent.array())

        then:
        compareInputStreams(fileStream, is, readCount1)

        when:
        readByteChannel.position(seekPos2)

        then:
        readByteChannel.position() == seekPos2

        when:
        fileStream = new FileInputStream(sourceFile)
        fileStream.skip(seekPos2)
        streamContent = ByteBuffer.allocate(readCount2)
        readByteChannel.read(streamContent)
        is = new ByteArrayInputStream(streamContent.array())

        then:
        compareInputStreams(fileStream, is, readCount2)

        where:
        readCount0      | seekPos1        | readCount1               | seekPos2                  | readCount2
        1024            | 1024            | (5 * 1024 * 1024) - 1024 | 5 * 1024 * 1024           | 5 * 1024 * 1024 // Only ever seek in place. Read whole blob
        1024            | 5 * 1024 * 1024 | 1024                     | 2048                      | 1024 // Seek forward then seek backward
        5 * 1024 * 1024 | 1024            | 1024                     | (10 * 1024 * 1024) - 1024 | 1024  // Seek backward then seek forward
    }

    def "Seek out of bounds"() {
        when:
        readByteChannel.position(-1)

        then:
        thrown(IllegalArgumentException)

        when:
        readByteChannel.position(sourceFileSize + 1)

        then:
        thrown(IllegalArgumentException)
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

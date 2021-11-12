// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.specialized.BlobOutputStream
import org.mockito.Answers
import org.mockito.Mockito
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import java.nio.channels.NonReadableChannelException
import java.nio.channels.NonWritableChannelException
import java.nio.channels.SeekableByteChannel
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
        def writePath = ((AzurePath) fs.getPath(writeBc.getContainerName() + ":", writeBc.getBlobName()))

        readByteChannel = new AzureSeekableByteChannel(new NioBlobInputStream(bc.openInputStream(), path), path)
        // For writing, we don't want a blob to exist there yet
        writeByteChannel = new AzureSeekableByteChannel(
            new NioBlobOutputStream(writeBc.getBlockBlobClient().getBlobOutputStream(true), writePath), writePath)
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

    def "Read loop until EOF"() {
        setup:
        def fileContent = new byte[sourceFileSize]
        fileStream.read(fileContent)
        def os = new ByteArrayOutputStream(sourceFileSize)
        def rand = new Random()
        long timeLimit = System.currentTimeMillis() + 60_000 // fail if test runs >= 1 minute

        when:
        while (System.currentTimeMillis() < timeLimit) { // ensures test duration is bounded
            def buffer = ByteBuffer.allocate(rand.nextInt(1024 * 1024))
            int readAmount = readByteChannel.read(buffer)
            if (readAmount == -1) {
                break; // reached EOF
            }
            os.write(buffer.array(), 0, readAmount) // limit the write in case we allocated more than we needed
        }

        then:
        os.toByteArray() == fileContent
        System.currentTimeMillis() < timeLimit // else potential inf. loop if read() always returns 0
    }

    def "Read respect dest buffer pos"() {
        setup:
        def fileContent = new byte[sourceFileSize]
        fileStream.read(fileContent)

        def rand = new Random()
        int initialOffset = rand.nextInt(512) + 1 // always > 0
        byte[] randArray = new byte[2 * initialOffset + sourceFileSize]
        rand.nextBytes(randArray) // fill with random bytes

        // copy same random bytes, but in this copy some will eventually be overwritten by read()
        byte[] destArray = new byte[randArray.length]
        System.arraycopy(randArray, 0, destArray, 0, randArray.length)
        def dest = ByteBuffer.wrap(destArray)
        dest.position(initialOffset) // will have capacity on either side that should not be touched

        when:
        int readAmount = 0;
        while (readAmount != -1) {
            assert dest.position() != 0
            readAmount = readByteChannel.read(dest) // backed by an array, but position != 0
        }

        then:
        dest.position() == initialOffset + sourceFileSize
        compareInputStreams( // destination content should match file content at initial read position
            new ByteArrayInputStream(destArray, initialOffset, sourceFileSize),
            new ByteArrayInputStream(fileContent),
            sourceFileSize)
        compareInputStreams( // destination content should be untouched prior to initial position
            new ByteArrayInputStream(destArray, 0, initialOffset),
            new ByteArrayInputStream(randArray, 0, initialOffset),
            initialOffset)
        compareInputStreams( // destination content should be untouched past end of read
            new ByteArrayInputStream(destArray, initialOffset + sourceFileSize, initialOffset),
            new ByteArrayInputStream(randArray, initialOffset + sourceFileSize, initialOffset),
            initialOffset)
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
        fileStream.read(fileContent)
        int count = 0
        def rand = new Random()

        when:
        writeByteChannel.write(ByteBuffer.wrap(fileContent))
        then:
        while (count < sourceFileSize) {
            int writeAmount = Math.min(rand.nextInt(1024 * 1024), sourceFileSize - count)
            def buffer = new byte[writeAmount]
            fileStream.read(buffer)
            writeByteChannel.write(ByteBuffer.wrap(buffer))
            count += writeAmount
        }

        writeByteChannel.close()

        then:
        compareInputStreams(writeBc.openInputStream(), new ByteArrayInputStream(fileContent), sourceFileSize)
    }

    def "Write respect src buffer pos"() {
        setup:
        def rand = new Random()
        int initialOffset = rand.nextInt(512) + 1 // always > 0
        def srcBufferContent = new byte[2 * initialOffset + sourceFileSize]
        rand.nextBytes(srcBufferContent) // fill with random bytes

        def fileContent = new byte[sourceFileSize]
        fileStream.read(fileContent)

        // place expected file content into source buffer at random location, retain other random bytes
        System.arraycopy(fileContent, 0, srcBufferContent, initialOffset, sourceFileSize)
        def srcBuffer = ByteBuffer.wrap(srcBufferContent)
        srcBuffer.position(initialOffset)
        srcBuffer.limit(initialOffset + sourceFileSize)

        // This test aims to observe the actual bytes written by the ByteChannel to the underlying OutputStream,
        // not just the number of bytes allegedly written as reported by its position. It would prefer to examine
        // the OutputStream directly, but the channel requires the specific NioBlobOutputStream implementation
        // and does not accept something generic like a ByteArrayOutputStream. NioBlobOutputStream is final, so
        // it cannot be subclassed or mocked and has little state of its own -- writes go to a BlobOutputStream.
        // That class is abstract, but its constructor is not accessible outside its package and cannot normally
        // be subclassed to provide custom behavior, but a runtime mocking framework like Mockito can. This is
        // the nearest accessible observation point, so the test mocks a BlobOutputStream such that all write
        // methods store data in ByteArrayOutputStream which it can later examine for its size and content.
        def actualOutput = new ByteArrayOutputStream(sourceFileSize)
        def blobOutputStream = Mockito.mock(
            BlobOutputStream.class, Mockito.withSettings().useConstructor(4096 /* block size */))
        Mockito.doAnswer( { invoked -> actualOutput.write(invoked.getArgument(0)) } )
            .when(blobOutputStream).write(Mockito.anyInt())
        Mockito.doAnswer( { invoked -> actualOutput.writeBytes(invoked.getArgument(0)) } )
            .when(blobOutputStream).write(Mockito.any(byte[].class))
        Mockito.doAnswer( { invoked -> actualOutput.write(
                invoked.getArgument(0), invoked.getArgument(1), invoked.getArgument(2)) } )
            .when(blobOutputStream).write(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt())
        def path = writeByteChannel.getPath()
        writeByteChannel = new AzureSeekableByteChannel(new NioBlobOutputStream(blobOutputStream, path), path)

        when:
        int written = 0
        while (written < sourceFileSize) {
            written += writeByteChannel.write(srcBuffer)
        }
        writeByteChannel.close()

        then:
        srcBuffer.position() == initialOffset + sourceFileSize // src buffer position SHOULD be updated
        srcBuffer.limit() == srcBuffer.position() // limit SHOULD be unchanged (still at end of content)
        // the above report back to the caller, but this verifies the correct bytes are going to the blob:
        compareInputStreams(
            new ByteArrayInputStream(actualOutput.toByteArray()),
            new ByteArrayInputStream(fileContent),
            sourceFileSize)
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
        writeByteChannel.size() == 0

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

    @Unroll
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
        readByteChannel(readByteChannel, streamContent)
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
        readByteChannel(readByteChannel, streamContent)
        is = new ByteArrayInputStream(streamContent.array())

        then:
        compareInputStreams(fileStream, is, readCount2)

        where:
        readCount0      | seekPos1        | readCount1               | seekPos2                  | readCount2
        1024            | 1024            | (5 * 1024 * 1024) - 1024 | 5 * 1024 * 1024           | 5 * 1024 * 1024 // Only ever seek in place. Read whole blob
        1024            | 5 * 1024 * 1024 | 1024                     | 2048                      | 1024 // Seek forward then seek backward
        5 * 1024 * 1024 | 1024            | 1024                     | (10 * 1024 * 1024) - 1024 | 1024  // Seek backward then seek forward
    }

    def readByteChannel(SeekableByteChannel channel, ByteBuffer dst) {
        while (dst.remaining() > 0) {
            channel.read(dst)
        }
    }

    def "Seek out of bounds"() {
        when:
        readByteChannel.position(-1)

        then:
        thrown(IllegalArgumentException)

        when:
        readByteChannel.position(sourceFileSize) // position is 0-based, so seeking to size --> EOF

        then:
        readByteChannel.read(ByteBuffer.allocate(1)) == -1 // Seeking to the end and then reading should indicate EOF
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
        readByteChannel.size() == sourceFileSize

        when:
        bc.upload(data.defaultInputStream, data.defaultDataSize, true)
        def path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), bc.getBlobName()))
        readByteChannel = new AzureSeekableByteChannel(new NioBlobInputStream(bc.openInputStream(), path), path)

        then:
        readByteChannel.size() == data.defaultDataSize
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
        writeByteChannel.close()

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

        when:
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
        thrown(NonWritableChannelException)

        when:
        writeByteChannel.read(ByteBuffer.allocate(1))

        then:
        thrown(NonReadableChannelException)

        when:
        writeByteChannel.position(5)

        then:
        thrown(NonReadableChannelException)

        when:
        readByteChannel.truncate(0)

        then:
        thrown(UnsupportedOperationException)

        when:
        writeByteChannel.truncate(0)

        then:
        thrown(UnsupportedOperationException)
    }
}

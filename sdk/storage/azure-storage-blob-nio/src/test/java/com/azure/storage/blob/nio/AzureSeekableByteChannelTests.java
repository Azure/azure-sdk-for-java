// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.specialized.BlobOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureSeekableByteChannelTests extends BlobNioTestBase {
    private int sourceFileSize;
    private byte[] fileBytes;
    private File sourceFile;
    private BlobClient bc;
    private BlobClient writeBc;
    private AzureSeekableByteChannel readByteChannel;
    private AzureSeekableByteChannel writeByteChannel;
    private FileInputStream fileStream;
    private AzureFileSystem fs;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        sourceFileSize = 5 * 1024 * 1024;
        fileBytes = getRandomByteArray(sourceFileSize);
        sourceFile = getRandomFile(fileBytes);

        cc.create();
        bc = cc.getBlobClient(generateBlobName());
        writeBc = cc.getBlobClient(generateBlobName());
        bc.upload(DATA.getDefaultBinaryData());
        fs = createFS(initializeConfigMap());
        AzurePath path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), bc.getBlobName()));
        AzurePath writePath = ((AzurePath) fs.getPath(writeBc.getContainerName() + ":", writeBc.getBlobName()));

        readByteChannel = new AzureSeekableByteChannel(new NioBlobInputStream(bc.openInputStream(), path), path);
        // For writing, we don't want a blob to exist there yet
        writeByteChannel = new AzureSeekableByteChannel(
            new NioBlobOutputStream(writeBc.getBlockBlobClient().getBlobOutputStream(true), writePath), writePath);
        try {
            fileStream = new FileInputStream(sourceFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void resetForLargeSource() {
        if (getTestMode() != TestMode.PLAYBACK) {
            // Base setup only uploads a small source to reduce size of session record.
            BlobClient blobClient = getNonRecordingServiceClient().getBlobContainerClient(bc.getContainerName())
                .getBlobClient(bc.getBlobName());
            blobClient.upload(BinaryData.fromBytes(fileBytes), true);
        }

        AzurePath path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), bc.getBlobName()));
        AzurePath writePath = ((AzurePath) fs.getPath(writeBc.getContainerName() + ":", writeBc.getBlobName()));

        readByteChannel = new AzureSeekableByteChannel(new NioBlobInputStream(bc.openInputStream(), path), path);
        // For writing, we don't want a blob to exist there yet
        writeByteChannel = new AzureSeekableByteChannel(
            new NioBlobOutputStream(writeBc.getBlockBlobClient().getBlobOutputStream(true), writePath), writePath);
    }

    @Test
    public void read() throws IOException {
        resetForLargeSource();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int count = 0;
        Random rand = new Random();

        while (count < sourceFileSize) {
            ByteBuffer buffer = ByteBuffer.allocate(rand.nextInt(1024 * 1024));
            int readAmount = readByteChannel.read(buffer);
            os.write(buffer.array(), 0, readAmount); // limit the write in case we allocated more than we needed
            count += readAmount;
        }

        assertArrayEquals(fileBytes, os.toByteArray());
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS) // fail if test runs >= 1 minute
    public void readLoopUntilEof() throws IOException {
        resetForLargeSource();
        ByteArrayOutputStream os = new ByteArrayOutputStream(sourceFileSize);
        Random rand = new Random();

        while (true) { // ensures test duration is bounded
            ByteBuffer buffer = ByteBuffer.allocate(rand.nextInt(1024 * 1024));
            int readAmount = readByteChannel.read(buffer);
            if (readAmount == -1) {
                break; // reached EOF
            }
            os.write(buffer.array(), 0, readAmount); // limit the write in case we allocated more than we needed
        }

        assertArrayEquals(fileBytes, os.toByteArray());
    }

    @Test
    public void readRespectDestBufferPos() throws IOException {
        resetForLargeSource();
        Random rand = new Random();
        int initialOffset = rand.nextInt(512) + 1; // always > 0
        byte[] randArray = new byte[2 * initialOffset + sourceFileSize];
        rand.nextBytes(randArray); // fill with random bytes

        // copy same random bytes, but in this copy some will eventually be overwritten by read()
        byte[] destArray = new byte[randArray.length];
        System.arraycopy(randArray, 0, destArray, 0, randArray.length);
        ByteBuffer dest = ByteBuffer.wrap(destArray);
        dest.position(initialOffset); // will have capacity on either side that should not be touched

        int readAmount = 0;
        while (readAmount != -1) {
            assert dest.position() != 0;
            readAmount = readByteChannel.read(dest); // backed by an array, but position != 0
        }

        assertEquals(initialOffset + sourceFileSize, dest.position());
        // destination content should match file content at initial read position
        assertArraysEqual(fileBytes, 0, destArray, initialOffset, sourceFileSize);
        // destination content should be untouched prior to initial position
        assertArraysEqual(randArray, 0, destArray, 0, initialOffset);
        // destination content should be untouched past end of read
        assertArraysEqual(randArray, initialOffset + sourceFileSize, destArray, initialOffset + sourceFileSize,
            initialOffset);
    }

    @Test
    public void readFSClosed() throws IOException {
        fs.close();

        assertThrows(ClosedFileSystemException.class, () -> readByteChannel.read(ByteBuffer.allocate(1)));
    }

    @Test
    public void write() throws IOException {
        resetForLargeSource();
        int count = 0;
        Random rand = new Random();
        writeByteChannel.write(ByteBuffer.wrap(fileBytes));

        while (count < sourceFileSize) {
            int writeAmount = Math.min(rand.nextInt(1024 * 1024), sourceFileSize - count);
            byte[] buffer = new byte[writeAmount];
            fileStream.read(buffer);
            writeByteChannel.write(ByteBuffer.wrap(buffer));
            count += writeAmount;
        }

        writeByteChannel.close();
        compareInputStreams(writeBc.openInputStream(), new ByteArrayInputStream(fileBytes), sourceFileSize);
    }

    @Test
    public void writeRespectSrcBufferPos() throws IOException {
        resetForLargeSource();
        Random rand = new Random();
        int initialOffset = rand.nextInt(512) + 1; // always > 0
        byte[] srcBufferContent = new byte[2 * initialOffset + sourceFileSize];
        rand.nextBytes(srcBufferContent); // fill with random bytes

        // place expected file content into source buffer at random location, retain other random bytes
        System.arraycopy(fileBytes, 0, srcBufferContent, initialOffset, sourceFileSize);
        ByteBuffer srcBuffer = ByteBuffer.wrap(srcBufferContent);
        srcBuffer.position(initialOffset);
        srcBuffer.limit(initialOffset + sourceFileSize);

        // This test aims to observe the actual bytes written by the ByteChannel to the underlying OutputStream,
        // not just the number of bytes allegedly written as reported by its position. It would prefer to examine
        // the OutputStream directly, but the channel requires the specific NioBlobOutputStream implementation
        // and does not accept something generic like a ByteArrayOutputStream. NioBlobOutputStream is final, so
        // it cannot be subclassed or mocked and has little state of its own -- writes go to a BlobOutputStream.
        // That class is abstract, but its constructor is not accessible outside its package and cannot normally
        // be subclassed to provide custom behavior, but a runtime mocking framework like Mockito can. This is
        // the nearest accessible observation point, so the test mocks a BlobOutputStream such that all write
        // methods store data in ByteArrayOutputStream which it can later examine for its size and content.
        ByteArrayOutputStream actualOutput = new ByteArrayOutputStream(sourceFileSize);
        BlobOutputStream blobOutputStream
            = Mockito.mock(BlobOutputStream.class, Mockito.withSettings().useConstructor(4096 /* block size */));
        Mockito.doAnswer(invocation -> {
            actualOutput.write(invocation.getArgument(0));
            return null;
        }).when(blobOutputStream).write(Mockito.anyInt());
        Mockito.doAnswer(invoked -> {
            actualOutput.write(invoked.getArgument(0));
            return null;
        }).when(blobOutputStream).write(Mockito.any(byte[].class));
        Mockito.doAnswer(invoked -> {
            actualOutput.write(invoked.getArgument(0), invoked.getArgument(1), invoked.getArgument(2));
            return null;
        }).when(blobOutputStream).write(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt());
        Path path = writeByteChannel.getPath();
        writeByteChannel = new AzureSeekableByteChannel(new NioBlobOutputStream(blobOutputStream, path), path);

        int written = 0;
        while (written < sourceFileSize) {
            written += writeByteChannel.write(srcBuffer);
        }
        writeByteChannel.close();

        assertEquals(initialOffset + sourceFileSize, srcBuffer.position()); // src buffer position SHOULD be updated
        assertEquals(srcBuffer.position(), srcBuffer.limit()); // limit SHOULD be unchanged (still at end of content)
        // the above report back to the caller, but this verifies the correct bytes are going to the blob:
        assertArraysEqual(fileBytes, 0, actualOutput.toByteArray(), 0, sourceFileSize);
    }

    @Test
    public void writeFSClosed() throws IOException {
        fs.close();

        assertThrows(ClosedFileSystemException.class, () -> writeByteChannel.write(ByteBuffer.allocate(1)));
    }

    @Test
    public void positionRead() throws IOException {
        resetForLargeSource();
        int bufferSize = sourceFileSize / 10;
        ByteBuffer dest = ByteBuffer.allocate(bufferSize);

        assertEquals(0, readByteChannel.position());

        for (int i = 0; i < 10; i++) {
            readByteChannel.read(dest);
            assertEquals((i + 1) * bufferSize, readByteChannel.position());
            dest.flip();
        }
    }

    @Test
    public void positionSizeWrite() throws IOException {
        resetForLargeSource();
        int bufferSize = sourceFileSize / 10;
        ByteBuffer src = getRandomData(bufferSize);

        assertEquals(0, writeByteChannel.position());
        assertEquals(0, writeByteChannel.size());

        for (int i = 0; i < 10; i++) {
            writeByteChannel.write(src);
            assertEquals((i + 1) * bufferSize, writeByteChannel.position());
            assertEquals(writeByteChannel.position(), writeByteChannel.size());
            src.flip();
        }
    }

    @Test
    public void positionFSClosed() throws IOException {
        fs.close();

        assertThrows(ClosedFileSystemException.class, readByteChannel::position);
        assertThrows(ClosedFileSystemException.class, writeByteChannel::position);
    }

    @ParameterizedTest
    @MethodSource("seekSupplier")
    public void seek(int readCount0, int seekPos1, int readCount1, int seekPos2, int readCount2) throws IOException {
        resetForLargeSource();
        ByteBuffer streamContent = ByteBuffer.allocate(readCount0);
        readByteChannel(readByteChannel, streamContent);
        compareInputStreams(fileStream, new ByteArrayInputStream(streamContent.array()), readCount0);

        readByteChannel.position(seekPos1);
        assertEquals(seekPos1, readByteChannel.position());

        fileStream = new FileInputStream(sourceFile);
        fileStream.skip(seekPos1);
        streamContent = ByteBuffer.allocate(readCount1);
        readByteChannel(readByteChannel, streamContent);
        compareInputStreams(fileStream, new ByteArrayInputStream(streamContent.array()), readCount1);

        readByteChannel.position(seekPos2);
        assertEquals(seekPos2, readByteChannel.position());

        fileStream = new FileInputStream(sourceFile);
        fileStream.skip(seekPos2);
        streamContent = ByteBuffer.allocate(readCount2);
        readByteChannel(readByteChannel, streamContent);
        compareInputStreams(fileStream, new ByteArrayInputStream(streamContent.array()), readCount2);
    }

    private static Stream<Arguments> seekSupplier() {
        return Stream.of(Arguments.of(1024, 1024, (2 * 1024 * 1024) - 1024, 3 * 1024 * 1024, 2 * 1024 * 1024), // Only ever seek in place. Read whole blob
            Arguments.of(1024, (5 * 1024 * 1024) - 1024, 1024, 2048, 1024), // Seek forward then seek backward
            Arguments.of(2 * 1024 * 1024, 1024, 1024, (5 * 1024 * 1024) - 1024, 1024) // Seek backward then seek forward
        );
    }

    private static void readByteChannel(SeekableByteChannel channel, ByteBuffer dst) throws IOException {
        while (dst.remaining() > 0) {
            if (channel.read(dst) == -1) { // Prevent infinite read
                break;
            }
        }
    }

    @Test
    public void seekOutOfBounds() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> readByteChannel.position(-1));

        readByteChannel.position(sourceFileSize); // position is 0-based, so seeking to size --> EOF
        assertEquals(-1, readByteChannel.read(ByteBuffer.allocate(1))); // Seeking to the end and then reading should indicate EOF
    }

    @Test
    public void seekFSClosed() throws IOException {
        fs.close();

        assertThrows(ClosedFileSystemException.class, () -> readByteChannel.position(0));
    }

    @Test
    public void sizeRead() throws IOException {
        bc.upload(DATA.getDefaultBinaryData(), true);
        AzurePath path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), bc.getBlobName()));
        readByteChannel = new AzureSeekableByteChannel(new NioBlobInputStream(bc.openInputStream(), path), path);

        assertEquals(DATA.getDefaultDataSize(), readByteChannel.size());
    }

    @Test
    public void sizeFSClosed() throws IOException {
        fs.close();

        assertThrows(ClosedFileSystemException.class, readByteChannel::size);
        assertThrows(ClosedFileSystemException.class, writeByteChannel::size);
    }

    @Test
    public void close() throws IOException {
        readByteChannel.close();
        writeByteChannel.close();

        assertThrows(ClosedChannelException.class, () -> readByteChannel.read(ByteBuffer.allocate(1)));
        assertThrows(ClosedChannelException.class, readByteChannel::size);
        assertThrows(ClosedChannelException.class, readByteChannel::position);
        assertThrows(ClosedChannelException.class, () -> writeByteChannel.write(ByteBuffer.allocate(1)));
        assertThrows(ClosedChannelException.class, writeByteChannel::size);
        assertThrows(ClosedChannelException.class, writeByteChannel::position);
    }

    @Test
    public void closeFSClose() throws IOException {
        fs.close();

        assertThrows(ClosedFileSystemException.class, readByteChannel::close);
        assertThrows(ClosedFileSystemException.class, writeByteChannel::close);
    }

    @Test
    public void isOpen() throws IOException {
        assertTrue(readByteChannel.isOpen());
        assertTrue(writeByteChannel.isOpen());

        readByteChannel.close();
        writeByteChannel.close();

        assertFalse(readByteChannel.isOpen());
        assertFalse(writeByteChannel.isOpen());
    }

    @Test
    public void isOpenFSClosed() throws IOException {
        fs.close();

        assertThrows(ClosedFileSystemException.class, readByteChannel::isOpen);
        assertThrows(ClosedFileSystemException.class, writeByteChannel::isOpen);
    }

    @Test
    public void unsupportedOperations() {
        assertThrows(NonWritableChannelException.class, () -> readByteChannel.write(ByteBuffer.allocate(1)));
        assertThrows(NonReadableChannelException.class, () -> writeByteChannel.read(ByteBuffer.allocate(1)));
        assertThrows(NonReadableChannelException.class, () -> writeByteChannel.position(5));
        assertThrows(UnsupportedOperationException.class, () -> readByteChannel.truncate(0));
        assertThrows(UnsupportedOperationException.class, () -> writeByteChannel.truncate(0));
    }
}

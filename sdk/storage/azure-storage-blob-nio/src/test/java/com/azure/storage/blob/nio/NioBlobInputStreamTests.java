// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobStorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.Files;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NioBlobInputStreamTests extends BlobNioTestBase {
    private byte[] fileBytes;
    private File sourceFile;
    private BlobClient bc;
    private NioBlobInputStream nioStream;
    private FileInputStream fileStream;
    private AzureFileSystem fs;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        var size = 5 * 1024 * 1024;
        fileBytes = new byte[size];

        for (int i = 0; i < size; i++){
            fileBytes[i] = (byte) 0xFC;
        }
        sourceFile = getRandomFile(fileBytes);

        cc.create();
        bc = cc.getBlobClient(generateBlobName());
        bc.upload(DATA.getDefaultBinaryData());
        fs = createFS(initializeConfigMap());
        AzurePath path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), bc.getBlobName()));

        nioStream = new NioBlobInputStream(bc.openInputStream(), path);
        try {
            fileStream = new FileInputStream(sourceFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void resetForLargeSource() {
        if (getTestMode() != TestMode.PLAYBACK) {
            // Base setup only uploads a small source to reduce size of session record.
            BlobClient blobClient = getNonRecordingServiceClient()
                .getBlobContainerClient(bc.getContainerName())
                .getBlobClient(bc.getBlobName());
            blobClient.upload(BinaryData.fromBytes(fileBytes), true);
        }

        // Base setup only uploads a small source to reduce size of session record.
        AzurePath path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), bc.getBlobName()));
        nioStream = new NioBlobInputStream(bc.openInputStream(), path);
    }

    @Override
    protected void afterTest() {
        super.afterTest();
        sourceFile.delete();
    }

    @Test
    public void readWholeFile() throws IOException {
        resetForLargeSource();
        compareInputStreams(nioStream, fileStream, Files.size(sourceFile.toPath()));
    }

    @Test
    public void readMin() throws IOException {
        resetForLargeSource();
        for (int i = 0; i < 100; i++) {
            assertEquals(fileStream.read(), nioStream.read());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 100, 4 * 1024 * 1024})
    public void readBuff(int size) throws IOException {
        resetForLargeSource();
        byte[] nioBytes = new byte[size];
        nioStream.read(nioBytes);

        assertArraysEqual(fileBytes, 0, nioBytes, 0, size);
    }

    @Test
    public void readBuffOffsetLen() throws IOException {
        resetForLargeSource();
        byte[] nioBytes = new byte[100];
        nioStream.read(nioBytes, 5, 50);

        assertArraysEqual(fileBytes, 0, nioBytes, 5, 50);
    }

    @ParameterizedTest
    @CsvSource(value = {"-1,5", "3,-1", "0,11", "3,8"})
    public void readBuffOffsetLenFail(int off, int len) {
        byte[] b = new byte[10];

        assertThrows(IndexOutOfBoundsException.class, () -> nioStream.read(b, off, len));
    }

    @ParameterizedTest
    @MethodSource("readFailSupplier")
    public void readFail(Function<NioBlobInputStream, Executable> methodCall) throws IOException {
        resetForLargeSource();
        bc.delete();
        nioStream.read(new byte[4 * 1024 * 1024]); // Must read through the initial download to trigger failed response

        IOException e = assertThrows(IOException.class, methodCall.apply(nioStream));
        assertInstanceOf(BlobStorageException.class, e.getCause());
    }

    private static Stream<Function<NioBlobInputStream, Executable>> readFailSupplier() {
        return Stream.of(nioStream -> nioStream::read, nioStream -> () -> nioStream.read(new byte[5]),
            nioStream -> () -> nioStream.read(new byte[5], 0, 4));
    }

    @Test
    public void readFSClosed() throws IOException {
        fs.close();

        assertThrows(ClosedFileSystemException.class, nioStream::read);
        assertThrows(ClosedFileSystemException.class, () -> nioStream.read(new byte[1]));
        assertThrows(ClosedFileSystemException.class, () -> nioStream.read(new byte[10], 2, 5));
    }


    @ParameterizedTest
    @CsvSource(value = {"0,0", "0,50", "50,0", "50,50", "50,5242780", "5242780,50"})
    public void markAndReset(int markAfter, int resetAfter) throws IOException {
        resetForLargeSource();
        byte[] b = new byte[markAfter];
        nioStream.read(b);
        fileStream.skip(markAfter); // Position the file stream where we expect to be after resetting.

        // Read some bytes past the mark
        nioStream.mark(Integer.MAX_VALUE);

        nioStream.read(new byte[resetAfter]);

        // Reset to the mark
        nioStream.reset();

        compareInputStreams(nioStream, fileStream, sourceFile.length() - markAfter);
    }

    @Test
    public void markReadLimit() throws IOException {
        nioStream.mark(5);
        nioStream.read(new byte[6]);

        assertThrows(IOException.class, nioStream::reset);
    }

    @Test
    public void resetFail() throws IOException {
        // Mark never set
        nioStream.read();

        assertThrows(IOException.class, nioStream::reset);
    }

    @Test
    public void resetFSClosed() throws IOException {
        nioStream.mark(5);
        fs.close();

        assertThrows(ClosedFileSystemException.class, nioStream::reset);
    }

    @Test
    public void markSupported() {
        assertTrue(nioStream.markSupported());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 10, 4 * 1024 * 1024, (5 * 1024 * 1024) - 1})
    public void skip(int skip) throws IOException {
        resetForLargeSource();
        nioStream.skip(skip);
        fileStream.skip(skip);

        compareInputStreams(nioStream, fileStream, Files.size(sourceFile.toPath()) - skip);
    }

    @Test
    public void skipFSClosed() throws IOException {
        fs.close();

        assertThrows(ClosedFileSystemException.class, () -> nioStream.skip(5));
    }

    @Test
    public void close() throws IOException {
        nioStream.close();

        assertThrows(IOException.class, nioStream::read);
        assertThrows(IOException.class, () -> nioStream.read(new byte[5]));
        assertThrows(IOException.class, () -> nioStream.read(new byte[5], 0, 4));
    }

    @Test
    public void closeFSClosed() throws IOException {
        fs.close();

        assertThrows(ClosedFileSystemException.class, nioStream::close);
    }

    @ParameterizedTest
    @CsvSource(value = {"0,4194304", "5,4194299", "5242880,0"})
    public void available(int readAmount, int available) throws IOException {
        resetForLargeSource();
        nioStream.read(new byte[readAmount]);

        assertEquals(available, nioStream.available());
    }

    @Test
    public void availableFSClosed() throws IOException {
        fs.close();

        assertThrows(ClosedFileSystemException.class, nioStream::available);
    }
}

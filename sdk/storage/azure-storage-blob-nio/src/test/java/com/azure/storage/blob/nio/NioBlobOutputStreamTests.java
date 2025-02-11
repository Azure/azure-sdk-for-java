// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.BlockListType;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.ClosedFileSystemException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NioBlobOutputStreamTests extends BlobNioTestBase {
    private static final int BLOCK_SIZE = 50;
    private static final int MAX_SINGLE_UPLOAD_SIZE = 200;

    private BlockBlobClient bc;
    private NioBlobOutputStream nioStream;
    private AzureFileSystem fs;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        cc.create();
        bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        fs = createFS(initializeConfigMap());
        AzurePath path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), bc.getBlobName()));

        nioStream = new NioBlobOutputStream(bc.getBlobOutputStream(
            new ParallelTransferOptions(BLOCK_SIZE, null, null, MAX_SINGLE_UPLOAD_SIZE), null, null, null, null), path);
    }

    @Test
    public void writeMin() throws IOException {
        nioStream.write(1);
        nioStream.close();

        assertEquals(1, bc.getProperties().getBlobSize());

        InputStream inputStream = bc.openInputStream();

        assertEquals(1, inputStream.read());
        assertEquals(-1, inputStream.read());
    }

    @LiveOnly // Because we upload in blocks
    @Disabled("failing in ci")
    public void writeMinError() throws IOException {
        // Create an append blob at the destination to ensure writes fail. Customers should eventually be notified via
        // writing that there was an error
        cc.getBlobClient(bc.getBlobName()).getAppendBlobClient().create();

        // Write enough data to force making network requests.
        nioStream.write(getRandomByteArray(MAX_SINGLE_UPLOAD_SIZE + 1));
        // Issue a spurious request: A more reliable way than sleeping to ensure the previous stage block has enough
        // time to round trip.
        bc.getProperties();

        assertThrows(IOException.class, () -> nioStream.write(1));
    }

    @Test
    public void writeArray() throws IOException {
        int dataSize = 100;
        byte[] data = getRandomByteArray(dataSize);
        nioStream.write(data);
        nioStream.close();

        assertEquals(dataSize, bc.getProperties().getBlobSize());
        compareInputStreams(bc.openInputStream(), new ByteArrayInputStream(data), dataSize);
    }

    @LiveOnly // Because we upload in blocks
    @Disabled("failing in ci")
    public void writeArrayError() throws IOException {
        // Create an append blob at the destination to ensure writes fail. Customers should eventually be notified via
        // writing that there was an error
        cc.getBlobClient(bc.getBlobName()).getAppendBlobClient().create();

        /*
         Write enough data to force making network requests. The error will not be thrown until the next time a method
         on the stream is called.
         */
        nioStream.write(getRandomByteArray(MAX_SINGLE_UPLOAD_SIZE + 1));
        // Issue a spurious request: A more reliable way than sleeping to ensure the previous stage block has enough
        // time to round trip.
        bc.getProperties();

        assertThrows(IOException.class, () -> nioStream.write(new byte[1]));
    }

    @ParameterizedTest
    @CsvSource(value = { "0,100", "20,80", "20,40" })
    public void writeOffsetLen(int offset, int len) throws IOException {
        int dataSize = 100;
        byte[] data = getRandomByteArray(dataSize);

        nioStream.write(data, offset, len);
        nioStream.close();

        assertEquals(len, bc.getProperties().getBlobSize());
        compareInputStreams(bc.openInputStream(), new ByteArrayInputStream(data, offset, len), dataSize);
    }

    // To ensure the error isn't being wrapped unnecessarily
    @Test
    public void writeOffsetLenIOB() {
        assertThrows(IndexOutOfBoundsException.class, () -> nioStream.write(new byte[5], -1, 6));
    }

    @LiveOnly // Because we upload in blocks
    @Disabled("failing in ci")
    public void writeOffsetLenNetworkError() throws IOException {
        // Create an append blob at the destination to ensure writes fail. Customers should eventually be notified via
        // writing that there was an error
        cc.getBlobClient(bc.getBlobName()).getAppendBlobClient().create();

        // Write enough data to force making network requests.
        nioStream.write(getRandomByteArray(MAX_SINGLE_UPLOAD_SIZE + 1));
        // Issue a spurious request: A more reliable way than sleeping to ensure the previous stage block has enough
        // time to round trip.
        bc.getProperties();

        assertThrows(IOException.class, () -> nioStream.write(new byte[1], 0, 1));
    }

    @Test
    public void writeFSClosed() throws IOException {
        fs.close();

        assertThrows(ClosedFileSystemException.class, () -> nioStream.write(5));
        assertThrows(ClosedFileSystemException.class, () -> nioStream.write(new byte[5]));
        assertThrows(ClosedFileSystemException.class, () -> nioStream.write(new byte[5], 2, 1));
    }

    // Flush does not actually flush data right now
    @Test
    public void flush() throws IOException {
        nioStream.write(1);
        nioStream.flush();

        BlobStorageException e = assertThrows(BlobStorageException.class, () -> bc.listBlocks(BlockListType.ALL));
        assertEquals(BlobErrorCode.BLOB_NOT_FOUND, e.getErrorCode());
    }

    // Flush should at least check the stream state
    @LiveOnly // Because we upload in blocks
    @Disabled("failing in ci")
    public void flushError() throws IOException {
        // Create an append blob at the destination to ensure writes fail. Customers should eventually be notified via
        // writing that there was an error
        cc.getBlobClient(bc.getBlobName()).getAppendBlobClient().create();

        // Write enough data to force making network requests.
        nioStream.write(getRandomByteArray(MAX_SINGLE_UPLOAD_SIZE + 1));
        // Issue a spurious request: A more reliable way than sleeping to ensure the previous stage block has enough
        // time to round trip.
        bc.getProperties();

        assertThrows(IOException.class, nioStream::flush);
    }

    @Test
    public void flushClosedFS() throws IOException {
        nioStream.write(1);
        fs.close();

        assertThrows(ClosedFileSystemException.class, nioStream::flush);
    }

    @Test
    public void close() throws IOException {
        nioStream.close();

        assertThrows(IOException.class, () -> nioStream.write(1));
    }

    @Test
    public void closeError() throws IOException {
        // now calling close multiple times does not cause any error
        nioStream.close();
        assertDoesNotThrow(nioStream::close);
    }

    @Test
    public void closeDoesNotThrowError() throws IOException {
        bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        OutputStream nioStream = new NioBlobOutputStream(bc.getBlobOutputStream(new BlockBlobOutputStreamOptions()),
            fs.getPath(getNonDefaultRootDir(fs), bc.getBlobName()));

        nioStream.write(1);
        nioStream.close();
        // assert no error is thrown since close handles multiple close requests now
        assertDoesNotThrow(nioStream::close);
    }

    @Test
    public void closeFSClosed() throws IOException {
        fs.close();

        assertThrows(ClosedFileSystemException.class, nioStream::close);
    }
}

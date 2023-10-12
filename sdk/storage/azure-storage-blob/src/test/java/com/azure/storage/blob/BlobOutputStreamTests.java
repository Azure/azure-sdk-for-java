// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.test.utils.TestUtils;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
public class BlobOutputStreamTests extends BlobTestBase {
    private static final int FOUR_MB = 4 * Constants.MB;

    @Test
    public void blockBlobOutputStream() throws IOException {
        byte[] data = getRandomByteArray(10 * Constants.MB);
        BlockBlobClient blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        BlobOutputStream outputStream = blockBlobClient.getBlobOutputStream();
        outputStream.write(data);
        outputStream.close();

        assertEquals(data.length, blockBlobClient.getProperties().getBlobSize());
        TestUtils.assertArraysEqual(convertInputStreamToByteArray(blockBlobClient.openInputStream()), data);
    }

    @Test
    public void blockBlobOutputStreamWithCloseMultipleTimes() throws IOException {
        byte[] data = getRandomByteArray(10 * Constants.MB);
        BlockBlobClient blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        // set option for allowing multiple close() calls
        BlobOutputStream outputStream = blockBlobClient.getBlobOutputStream(new BlockBlobOutputStreamOptions());

        outputStream.write(data);
        outputStream.close();
        String etag = blockBlobClient.getProperties().getETag();

        assertEquals(etag, blockBlobClient.getProperties().getETag());
        // call again, no exceptions should be thrown
        outputStream.close();
        assertEquals(etag, blockBlobClient.getProperties().getETag());
        outputStream.close();
        assertEquals(etag, blockBlobClient.getProperties().getETag());

        assertEquals(data.length, blockBlobClient.getProperties().getBlobSize());
        TestUtils.assertArraysEqual(convertInputStreamToByteArray(blockBlobClient.openInputStream()), data);
    }

    @Test
    public void blockBlobOutputStreamDefaultNoOverwrite() throws IOException {
        byte[] data = getRandomByteArray(10 * Constants.MB);
        BlockBlobClient blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        BlobOutputStream outputStream1 = blockBlobClient.getBlobOutputStream();
        outputStream1.write(data);
        outputStream1.close();

        assertThrows(IllegalArgumentException.class, blockBlobClient::getBlobOutputStream);
    }

    @Test
    public void blockBlobOutputStreamDefaultNoOverwriteInterrupted() throws IOException {
        byte[] data = getRandomByteArray(10 * Constants.MB);
        BlockBlobClient blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        BlobOutputStream outputStream1 = blockBlobClient.getBlobOutputStream();
        BlobOutputStream outputStream2 = blockBlobClient.getBlobOutputStream();
        outputStream2.write(data);
        outputStream2.close();

        outputStream1.write(data);
        IOException e = assertThrows(IOException.class, outputStream1::close);

        assertInstanceOf(BlobStorageException.class, e.getCause());
        assertEquals(BlobErrorCode.BLOB_ALREADY_EXISTS, ((BlobStorageException) e.getCause()).getErrorCode());
    }

    @Test
    public void blockBlobOutputStreamOverwrite() throws IOException {
        byte[] randomData = getRandomByteArray(10 * Constants.MB);
        BlockBlobClient blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        blockBlobClient.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        BlobOutputStream outputStream = blockBlobClient.getBlobOutputStream(true);
        outputStream.write(randomData);
        outputStream.close();

        assertEquals(blockBlobClient.getProperties().getBlobSize(), randomData.length);
        TestUtils.assertArraysEqual(convertInputStreamToByteArray(blockBlobClient.openInputStream()), randomData);
    }

    @ParameterizedTest
    @MethodSource("blockBlobOutputStreamErrorSupplier")
    public void blockBlobOutputStreamError(Exception exception, Class<?> expectedExceptionClass) {
        StorageSharedKeyCredential credentials = new StorageSharedKeyCredential("accountName", "accountKey");
        String endpoint = "https://account.blob.core.windows.net/";
        byte[] data = getRandomByteArray(10 * Constants.MB);
        HttpClient httpClient = httpRequest -> Mono.error(exception);
        BlockBlobClient blockBlobClient = new SpecializedBlobClientBuilder()
            .endpoint(endpoint)
            .containerName("container")
            .blobName("blob")
            .retryOptions(new RetryOptions(new FixedDelayOptions(0, Duration.ofMillis(1))))
            .credential(credentials)
            .httpClient(httpClient)
            .buildBlockBlobClient();

        BlobOutputStream outputStream = blockBlobClient.getBlobOutputStream(true);
        outputStream.write(data);
        Exception e = assertThrows(IOException.class, outputStream::close);

        if (expectedExceptionClass != IOException.class) { /* IOExceptions are not wrapped. */
            //assertEquals(exception, e);
            assertInstanceOf(expectedExceptionClass, e.getCause());
        }
    }

    private static Stream<Arguments> blockBlobOutputStreamErrorSupplier() {
        return Stream.of(
            Arguments.of(new BlobStorageException(null, null, null), BlobStorageException.class),
            Arguments.of(new IllegalArgumentException(), IllegalArgumentException.class),
            Arguments.of(new IOException(), IOException.class));
    }

    @Test
    public void blockBlobOutputStreamBufferReuse() throws IOException {
        byte[] data = getRandomByteArray(10 * Constants.KB);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        byte[] buffer = new byte[1024];
        BlockBlobClient blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        BlobOutputStream outputStream = blockBlobClient.getBlobOutputStream();
        for (int i = 0; i < 10; i++) {
            inputStream.read(buffer);
            outputStream.write(buffer);
        }
        outputStream.close();

        assertEquals(data.length, blockBlobClient.getProperties().getBlobSize());
        TestUtils.assertArraysEqual(convertInputStreamToByteArray(blockBlobClient.openInputStream()), data);
    }

    @Test
    public void pageBlobOutputStream() throws IOException {
        byte[] data = getRandomByteArray(16 * Constants.MB - 512);
        PageBlobClient pageBlobClient = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        pageBlobClient.create(data.length);


        BlobOutputStream outputStream = pageBlobClient.getBlobOutputStream(new PageRange().setStart(0)
            .setEnd(16 * Constants.MB - 1));
        outputStream.write(data);
        outputStream.close();

        TestUtils.assertArraysEqual(convertInputStreamToByteArray(pageBlobClient.openInputStream()), data);
    }

    @Test
    public void appendBlobOutputStream() throws IOException {
        byte[] data = getRandomByteArray(4 * FOUR_MB);
        AppendBlobClient appendBlobClient = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        appendBlobClient.create();

        BlobOutputStream outputStream = appendBlobClient.getBlobOutputStream();
        for (int i = 0; i != 4; i++) {
            outputStream.write(Arrays.copyOfRange(data, i * FOUR_MB, ((i + 1) * FOUR_MB)));
        }
        outputStream.close();

        assertEquals(appendBlobClient.getProperties().getBlobSize(), data.length);
        TestUtils.assertArraysEqual(convertInputStreamToByteArray(appendBlobClient.openInputStream()), data);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20221102ServiceVersion")
    @Test
    public void appendBlobOutputStreamHighThroughput() throws IOException {
        // using data greater than 4MB and service versions above 2022_11_02 to test uploading up to 100MB per block
        byte[] data = getRandomByteArray(2 * FOUR_MB);
        AppendBlobClient appendBlobClient = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        appendBlobClient.create();

        BlobOutputStream outputStream = appendBlobClient.getBlobOutputStream();
        outputStream.write(data);
        outputStream.close();

        TestUtils.assertArraysEqual(convertInputStreamToByteArray(appendBlobClient.openInputStream()), data);
    }

    @Test
    public void appendBlobOutputStreamOverwrite() throws IOException {
        byte[] data = getRandomByteArray(FOUR_MB);
        AppendBlobClient appendBlobClient = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        appendBlobClient.create();

        BlobOutputStream outputStream = appendBlobClient.getBlobOutputStream();
        outputStream.write(data);
        outputStream.close();

        TestUtils.assertArraysEqual(convertInputStreamToByteArray(appendBlobClient.openInputStream()), data);

        byte[] data2 = getRandomByteArray(FOUR_MB);

        BlobOutputStream outputStream2 = appendBlobClient.getBlobOutputStream(true);
        outputStream2.write(data2);
        outputStream2.close();

        assertEquals(appendBlobClient.getProperties().getBlobSize(), data2.length);
        TestUtils.assertArraysEqual(convertInputStreamToByteArray(appendBlobClient.openInputStream()), data2);
    }

    @Test
    public void appendBlobOutputStreamOverwriteFalse() throws IOException {
        byte[] data = getRandomByteArray(Constants.MB);
        AppendBlobClient appendBlobClient = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        appendBlobClient.create();

        BlobOutputStream outputStream = appendBlobClient.getBlobOutputStream();
        outputStream.write(data);
        outputStream.close();

        TestUtils.assertArraysEqual(convertInputStreamToByteArray(appendBlobClient.openInputStream()), data);

        byte[] data2 = getRandomByteArray(Constants.MB);
        outputStream = appendBlobClient.getBlobOutputStream(false);
        outputStream.write(data2);
        outputStream.close();

        byte[] finalData = new byte[2 * Constants.MB];
        System.arraycopy(data, 0, finalData, 0, data.length);
        System.arraycopy(data2, 0, finalData, data.length, data2.length);
        TestUtils.assertArraysEqual(convertInputStreamToByteArray(appendBlobClient.openInputStream()), finalData);
    }
}

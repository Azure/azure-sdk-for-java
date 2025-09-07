// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.contentValidation;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.ProgressListener;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.structuredmessage.StorageChecksumAlgorithm;
import com.azure.storage.common.test.shared.StorageCommonTestUtils;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.storage.common.implementation.Constants.HeaderConstants.CONTENT_CRC64_HEADER_NAME;
import static com.azure.storage.common.implementation.Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME;
import static com.azure.storage.common.implementation.structuredmessage.StructuredMessageConstants.STRUCTURED_BODY_TYPE_VALUE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@LiveOnly
public class BlobMessageEncoderUploadTests extends BlobTestBase {
    private BlobClient bc;

    @BeforeEach
    public void setup() {
        bc = cc.getBlobClient(generateBlobName());
    }

    @Test
    public void uploadBinaryDataFullCRCHeader() {
        BlobParallelUploadOptions options = new BlobParallelUploadOptions(DATA.getDefaultBinaryData())
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        Response<BlockBlobItem> response = bc.uploadWithResponse(options, null, Context.NONE);
        assertNotNull(response.getRequest().getHeaders().getValue(CONTENT_CRC64_HEADER_NAME));
        assertNull(response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
    }

    @Test
    public void uploadBinaryDataFullStructMess() {
        byte[] data = getRandomByteArray(Constants.MB * 5);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(BinaryData.fromBytes(data))
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        Response<BlockBlobItem> response = bc.uploadWithResponse(options, null, Context.NONE);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        bc.downloadStream(outStream);

        assertArrayEquals(data, outStream.toByteArray());

        assertEquals(STRUCTURED_BODY_TYPE_VALUE,
            response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
    }

    @Test
    public void uploadBinaryDataChunkedStructMess() {
        byte[] data = getRandomByteArray(Constants.MB * 10);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(BinaryData.fromBytes(data))
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB * 2)
                    .setBlockSizeLong((long) Constants.MB * 2));

        assertDoesNotThrow(() -> bc.uploadWithResponse(options, null, Context.NONE));

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        bc.downloadStream(outStream);

        assertArrayEquals(data, outStream.toByteArray());
    }

    @Test
    void verifyUploadUsingChunkedStructMess() {
        byte[] data = getRandomByteArray(Constants.MB * 10);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(BinaryData.fromBytes(data))
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB * 2)
                    .setBlockSizeLong((long) Constants.MB * 2));

        StorageCommonTestUtils.CaptureAllRequestsHttpClient httpClient
            = new StorageCommonTestUtils.CaptureAllRequestsHttpClient(
                StorageCommonTestUtils.getHttpClient(interceptorManager));
        BlobClient capturingBlobClient = new BlobClientBuilder().endpoint(bc.getBlobUrl())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .httpClient(httpClient)
            .buildClient();

        capturingBlobClient.uploadWithResponse(options, null, Context.NONE);

        List<HttpRequest> putRequests = httpClient.getCapturedRequests()
            .stream()
            .filter(r -> r.getHttpMethod() == HttpMethod.PUT)
            .collect(Collectors.toList());

        // Multipart upload should have at least 2 PUTs (staged + final)
        assertTrue(putRequests.size() >= 2, "Expected multipart upload with multiple PUT requests.");

        for (int i = 0; i < putRequests.size() - 1; i++) {
            String headerValue = putRequests.get(i).getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME);
            assertNotNull(headerValue, "Missing structured body header on staged PUT index " + i);
        }
    }

    @Test
    @Disabled
    public void uploadBinaryDataChunkedStructMessProgressListenerBehavior() {
        long size = Constants.MB * 10;
        byte[] data = getRandomByteArray((int) size);
        long blockSize = (long) Constants.MB * 2;

        Listener uploadListenerChecksum = new Listener();

        Listener uploadListenerNoChecksum = new Listener();

        BlobParallelUploadOptions optionsChecksum = new BlobParallelUploadOptions(BinaryData.fromBytes(data))
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB * 2)
                    .setBlockSizeLong(blockSize)
                    .setProgressListener(uploadListenerChecksum));

        BlobParallelUploadOptions optionsNoChecksum = new BlobParallelUploadOptions(BinaryData.fromBytes(data))
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.NONE)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB * 2)
                    .setBlockSizeLong(blockSize)
                    .setProgressListener(uploadListenerNoChecksum));

        bc.uploadWithResponse(optionsChecksum, null, Context.NONE);
        bc.uploadWithResponse(optionsNoChecksum, null, Context.NONE);

        System.out.println("checksum reporting count: " + uploadListenerChecksum.getReportingCount() + "\n"
            + "checksum byte count: " + uploadListenerChecksum.getReportedByteCount() + "\n"
            + "no checksum reporting count: " + uploadListenerNoChecksum.getReportingCount() + "\n"
            + "no checksum byte count: " + uploadListenerNoChecksum.getReportedByteCount());

        /*
        result:
        checksum reporting count: 15
        checksum byte count: 10485955
        no checksum reporting count: 15
        no checksum byte count: 10485760

        progress listener reports the encoded byte count
         */
    }

    @Test
    public void uploadInputStreamFullCRCHeader() {
        BlobParallelUploadOptions options = new BlobParallelUploadOptions(DATA.getDefaultInputStream())
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        Response<BlockBlobItem> response = bc.uploadWithResponse(options, null, Context.NONE);
        assertNotNull(response.getRequest().getHeaders().getValue(CONTENT_CRC64_HEADER_NAME));
        assertNull(response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
    }

    @Test
    public void uploadInputStreamFullStructMess() {
        byte[] data = getRandomByteArray(Constants.MB * 5);

        ByteArrayInputStream input = new ByteArrayInputStream(data);
        BlobParallelUploadOptions options
            = new BlobParallelUploadOptions(input).setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        Response<BlockBlobItem> response = bc.uploadWithResponse(options, null, Context.NONE);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        bc.downloadStream(outStream);

        assertArrayEquals(data, outStream.toByteArray());

        assertEquals(STRUCTURED_BODY_TYPE_VALUE,
            response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
    }

    @Test
    public void uploadInputStreamChunkedStructMess() {
        byte[] data = getRandomByteArray(Constants.MB * 10);

        ByteArrayInputStream input = new ByteArrayInputStream(data);
        BlobParallelUploadOptions options
            = new BlobParallelUploadOptions(input).setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)
                .setParallelTransferOptions(
                    new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB * 2)
                        .setBlockSizeLong((long) Constants.MB * 2));

        assertDoesNotThrow(() -> bc.uploadWithResponse(options, null, Context.NONE));

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        bc.downloadStream(outStream);

        assertArrayEquals(data, outStream.toByteArray());
    }

    @Test
    public void uploadFluxFullCRCHeader() {
        BlobParallelUploadOptions options = new BlobParallelUploadOptions(DATA.getDefaultFlux())
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        Response<BlockBlobItem> response = bc.uploadWithResponse(options, null, Context.NONE);
        assertNotNull(response.getRequest().getHeaders().getValue(CONTENT_CRC64_HEADER_NAME));
        assertNull(response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
    }

    @Test
    public void uploadFluxFullStructMess() {
        byte[] data = getRandomByteArray(Constants.MB * 5);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(Flux.just(ByteBuffer.wrap(data)))
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        Response<BlockBlobItem> response = bc.uploadWithResponse(options, null, Context.NONE);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        bc.downloadStream(outStream);

        assertArrayEquals(data, outStream.toByteArray());

        assertEquals(STRUCTURED_BODY_TYPE_VALUE,
            response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
    }

    @Test
    public void uploadFluxChunkedStructMess() {
        byte[] data = getRandomByteArray(Constants.MB * 10);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(Flux.just(ByteBuffer.wrap(data)))
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB * 2)
                    .setBlockSizeLong((long) Constants.MB * 2));

        assertDoesNotThrow(() -> bc.uploadWithResponse(options, null, Context.NONE));
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        bc.downloadStream(outStream);

        assertArrayEquals(data, outStream.toByteArray());
    }

    static class Listener implements ProgressListener {
        private long reportingCount;
        private long reportedByteCount;

        Listener() {
        }

        @Override
        public void handleProgress(long bytesTransferred) {
            this.reportingCount += 1;
            this.reportedByteCount = bytesTransferred;
        }

        long getReportingCount() {
            return this.reportingCount;
        }

        long getReportedByteCount() {
            return this.reportedByteCount;
        }
    }
}

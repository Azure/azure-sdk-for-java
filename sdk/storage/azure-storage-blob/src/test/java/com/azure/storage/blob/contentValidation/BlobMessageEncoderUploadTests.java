// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.contentValidation;

import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.structuredmessage.StorageChecksumAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;

import static com.azure.storage.common.implementation.Constants.HeaderConstants.CONTENT_CRC64_HEADER_NAME;
import static com.azure.storage.common.implementation.Constants.HeaderConstants.STRUCTURED_BODY_TYPE;
import static com.azure.storage.common.implementation.Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    }

    @Test
    public void uploadBinaryDataFullStructMess() {
        BlobParallelUploadOptions options
            = new BlobParallelUploadOptions(BinaryData.fromBytes(getRandomByteArray(Constants.MB * 5)))
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        Response<BlockBlobItem> response = bc.uploadWithResponse(options, null, Context.NONE);
        assertEquals(STRUCTURED_BODY_TYPE,
            response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
    }

    @Test
    public void uploadBinaryDataChunkedStructMess() {
        BlobParallelUploadOptions options
            = new BlobParallelUploadOptions(BinaryData.fromBytes(getRandomByteArray(Constants.MB * 8)))
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB * 4));

        assertDoesNotThrow(() -> bc.uploadWithResponse(options, null, Context.NONE));

        //        Response<BlockBlobItem> response = bc.uploadWithResponse(options, null, Context.NONE);
        //        assertEquals(STRUCTURED_BODY_TYPE,
        //            response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
    }

    @Test
    public void uploadInputStreamFullCRCHeader() {
        BlobParallelUploadOptions options = new BlobParallelUploadOptions(DATA.getDefaultInputStream())
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        Response<BlockBlobItem> response = bc.uploadWithResponse(options, null, Context.NONE);
        assertNotNull(response.getRequest().getHeaders().getValue(CONTENT_CRC64_HEADER_NAME));
    }

    @Test
    public void uploadInputStreamFullStructMess() {
        byte[] randomData = getRandomByteArray(Constants.MB * 5);
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);
        BlobParallelUploadOptions options
            = new BlobParallelUploadOptions(input).setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        Response<BlockBlobItem> response = bc.uploadWithResponse(options, null, Context.NONE);
        assertEquals(STRUCTURED_BODY_TYPE,
            response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
    }

    @Test
    public void uploadInputStreamChunkedStructMess() {
        byte[] randomData = getRandomByteArray(Constants.MB * 8);
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);
        BlobParallelUploadOptions options
            = new BlobParallelUploadOptions(input).setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB * 4));

        assertDoesNotThrow(() -> bc.uploadWithResponse(options, null, Context.NONE));
        //        Response<BlockBlobItem> response = bc.uploadWithResponse(options, null, Context.NONE);
        //        assertEquals(STRUCTURED_BODY_TYPE,
        //            response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
    }

    @Test
    public void uploadFluxFullCRCHeader() {
        BlobParallelUploadOptions options = new BlobParallelUploadOptions(DATA.getDefaultFlux())
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        Response<BlockBlobItem> response = bc.uploadWithResponse(options, null, Context.NONE);
        assertNotNull(response.getRequest().getHeaders().getValue(CONTENT_CRC64_HEADER_NAME));
    }

    @Test
    public void uploadFluxFullStructMess() {
        BlobParallelUploadOptions options = new BlobParallelUploadOptions(Flux.just(getRandomData(Constants.MB * 5)))
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        Response<BlockBlobItem> response = bc.uploadWithResponse(options, null, Context.NONE);
        assertEquals(STRUCTURED_BODY_TYPE,
            response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
    }

    @Test
    public void uploadFluxChunkedStructMess() {
        BlobParallelUploadOptions options = new BlobParallelUploadOptions(Flux.just(getRandomData(Constants.MB * 8)))
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB * 4));

        assertDoesNotThrow(() -> bc.uploadWithResponse(options, null, Context.NONE));
        //        Response<BlockBlobItem> response = bc.uploadWithResponse(options, null, Context.NONE);
        //        assertEquals(STRUCTURED_BODY_TYPE,
        //            response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
    }
}

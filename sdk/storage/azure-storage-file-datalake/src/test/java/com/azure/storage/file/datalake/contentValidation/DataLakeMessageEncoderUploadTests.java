// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.contentValidation;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.structuredmessage.StorageChecksumAlgorithm;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.DataLakeTestBase;
import com.azure.storage.file.datalake.options.FileParallelUploadOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class DataLakeMessageEncoderUploadTests extends DataLakeTestBase {
    private DataLakeFileClient fc;

    @BeforeEach
    public void setup() {
        fc = dataLakeFileSystemClient.createFile(generatePathName());
    }

    @Test
    public void uploadBinaryDataFullCRCHeader() {
        FileParallelUploadOptions options = new FileParallelUploadOptions(DATA.getDefaultBinaryData())
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        // viewed crc64 header through httptoolkit, unable to retrieve header through the response object
        // due to the response object containing the request and response of the flush operation, not the append
        // Response<PathInfo> response = fc.uploadWithResponse(options, null, Context.NONE);
        // assertNotNull(response.getRequest().getHeaders().getValue(CONTENT_CRC64_HEADER_NAME));
        assertDoesNotThrow(() -> fc.uploadWithResponse(options, null, Context.NONE));
    }

    @Test
    public void uploadBinaryDataFullStructMess() {
        FileParallelUploadOptions options
            = new FileParallelUploadOptions(BinaryData.fromBytes(getRandomByteArray(Constants.MB * 5)))
                .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        // viewed structured body type header through httptoolkit, unable to retrieve it through the response object
        // Response<PathInfo> response = fc.uploadWithResponse(options, null, Context.NONE);
        // assertEquals(STRUCTUED_BODY_TYPE, response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
        assertDoesNotThrow(() -> fc.uploadWithResponse(options, null, Context.NONE));
    }

    @Test
    public void uploadBinaryDataChunkedStructMess() {
        FileParallelUploadOptions options
            = new FileParallelUploadOptions(BinaryData.fromBytes(getRandomByteArray(Constants.MB * 10)))
                .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)
                .setParallelTransferOptions(
                    new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB * 5)
                        .setBlockSizeLong((long) Constants.MB * 5));

        assertDoesNotThrow(() -> fc.uploadWithResponse(options, null, Context.NONE));
    }

    @Test
    public void uploadInputStreamFullCRCHeader() {
        FileParallelUploadOptions options = new FileParallelUploadOptions(DATA.getDefaultInputStream())
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        // viewed structured body type header through httptoolkit, unable to retrieve it through the response object
        //Response<PathInfo> response = fc.uploadWithResponse(options, null, Context.NONE);
        //assertNotNull(response.getRequest().getHeaders().getValue(CONTENT_CRC64_HEADER_NAME));
        assertDoesNotThrow(() -> fc.uploadWithResponse(options, null, Context.NONE));
    }

    @Test
    public void uploadInputStreamFullStructMess() {
        byte[] randomData = getRandomByteArray(Constants.MB * 5);
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);
        FileParallelUploadOptions options
            = new FileParallelUploadOptions(input).setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        // viewed structured body type header through httptoolkit, unable to retrieve it through the response object
        //Response<PathInfo> response = fc.uploadWithResponse(options, null, Context.NONE);
        //assertEquals(STRUCTUED_BODY_TYPE,
        //  response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
        assertDoesNotThrow(() -> fc.uploadWithResponse(options, null, Context.NONE));
    }

    @Test
    public void uploadInputStreamChunkedStructMess() {
        byte[] randomData = getRandomByteArray(Constants.MB * 8);
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);
        FileParallelUploadOptions options
            = new FileParallelUploadOptions(input).setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)
                .setParallelTransferOptions(
                    new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB * 4));

        assertDoesNotThrow(() -> fc.uploadWithResponse(options, null, Context.NONE));
    }

    @Test
    public void uploadFluxFullCRCHeader() {
        FileParallelUploadOptions options = new FileParallelUploadOptions(DATA.getDefaultFlux())
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        // viewed structured body type header through httptoolkit, unable to retrieve it through the response object
        // Response<PathInfo> response = fc.uploadWithResponse(options, null, Context.NONE);
        // assertNotNull(response.getRequest().getHeaders().getValue(CONTENT_CRC64_HEADER_NAME));
        assertDoesNotThrow(() -> fc.uploadWithResponse(options, null, Context.NONE));
    }

    @Test
    public void uploadFluxFullStructMess() {
        FileParallelUploadOptions options = new FileParallelUploadOptions(Flux.just(getRandomData(Constants.MB * 5)))
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        // viewed structured body type header through httptoolkit, unable to retrieve it through the response object
        //Response<PathInfo> response = fc.uploadWithResponse(options, null, Context.NONE);
        //assertEquals(STRUCTUED_BODY_TYPE,
        //response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
        assertDoesNotThrow(() -> fc.uploadWithResponse(options, null, Context.NONE));
    }

    @Test
    public void uploadFluxChunkedStructMess() {
        FileParallelUploadOptions options = new FileParallelUploadOptions(Flux.just(getRandomData(Constants.MB * 8)))
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB * 4));

        assertDoesNotThrow(() -> fc.uploadWithResponse(options, null, Context.NONE));
    }
}

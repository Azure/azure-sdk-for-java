// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.contentValidation;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.structuredmessage.StorageChecksumAlgorithm;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.DataLakeTestBase;
import com.azure.storage.file.datalake.options.FileParallelUploadOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@LiveOnly
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

        // viewed crc header through httptoolkit, unable to retrieve it through the response object
        assertDoesNotThrow(() -> fc.uploadWithResponse(options, null, Context.NONE));
    }

    @Test
    public void uploadBinaryDataFullStructMess() {
        byte[] data = getRandomByteArray(Constants.MB * 5);

        FileParallelUploadOptions options = new FileParallelUploadOptions(BinaryData.fromBytes(data))
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        fc.uploadWithResponse(options, null, Context.NONE);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        fc.read(outStream);

        assertArrayEquals(data, outStream.toByteArray());
    }

    @Test
    public void uploadBinaryDataChunkedStructMess() {
        byte[] data = getRandomByteArray(Constants.MB * 10);

        FileParallelUploadOptions options = new FileParallelUploadOptions(BinaryData.fromBytes(data))
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB * 2)
                    .setBlockSizeLong((long) Constants.MB * 2));

        fc.uploadWithResponse(options, null, Context.NONE);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        fc.read(outStream);

        assertArrayEquals(data, outStream.toByteArray());
    }

    @Test
    public void uploadInputStreamFullCRCHeader() {
        FileParallelUploadOptions options = new FileParallelUploadOptions(DATA.getDefaultInputStream())
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        // viewed crc header through httptoolkit, unable to retrieve it through the response object
        assertDoesNotThrow(() -> fc.uploadWithResponse(options, null, Context.NONE));
    }

    @Test
    public void uploadInputStreamFullStructMess() {
        byte[] data = getRandomByteArray(Constants.MB * 5);
        FileParallelUploadOptions options = new FileParallelUploadOptions(new ByteArrayInputStream(data))
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        fc.uploadWithResponse(options, null, Context.NONE);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        fc.read(outStream);

        assertArrayEquals(data, outStream.toByteArray());
    }

    @Test
    public void uploadInputStreamChunkedStructMess() {
        byte[] data = getRandomByteArray(Constants.MB * 10);
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        FileParallelUploadOptions options
            = new FileParallelUploadOptions(input).setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)
                .setParallelTransferOptions(
                    new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB * 2)
                        .setBlockSizeLong((long) Constants.MB * 2));

        fc.uploadWithResponse(options, null, Context.NONE);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        fc.read(outStream);

        assertArrayEquals(data, outStream.toByteArray());
    }

    @Test
    public void uploadFluxFullCRCHeader() {
        FileParallelUploadOptions options = new FileParallelUploadOptions(DATA.getDefaultFlux())
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        // viewed crc header through httptoolkit, unable to retrieve it through the response object
        assertDoesNotThrow(() -> fc.uploadWithResponse(options, null, Context.NONE));
    }

    @Test
    public void uploadFluxFullStructMess() {
        byte[] data = getRandomByteArray(Constants.MB * 5);
        FileParallelUploadOptions options = new FileParallelUploadOptions(Flux.just(ByteBuffer.wrap(data)))
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        fc.uploadWithResponse(options, null, Context.NONE);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        fc.read(outStream);

        assertArrayEquals(data, outStream.toByteArray());
    }

    @Test
    public void uploadFluxChunkedStructMess() {
        byte[] data = getRandomByteArray(Constants.MB * 10);
        FileParallelUploadOptions options = new FileParallelUploadOptions(Flux.just(ByteBuffer.wrap(data)))
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB * 2)
                    .setBlockSizeLong((long) Constants.MB * 2));

        fc.uploadWithResponse(options, null, Context.NONE);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        fc.read(outStream);

        assertArrayEquals(data, outStream.toByteArray());
    }
}

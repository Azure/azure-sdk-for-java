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

public class BlobMessageEncoderUploadTests extends BlobTestBase {
    private BlobClient bc;

    @BeforeEach
    public void setup() {
        bc = cc.getBlobClient(generateBlobName());
    }

    @Test
    public void uploadBinaryDataFull() {
        BlobParallelUploadOptions options = new BlobParallelUploadOptions(DATA.getDefaultBinaryData())
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);
        Response<BlockBlobItem> response = bc.uploadWithResponse(options, null, Context.NONE);
    }

    @Test
    public void uploadBinaryDataChunked() {
        BlobParallelUploadOptions options
            = new BlobParallelUploadOptions(BinaryData.fromBytes(getRandomByteArray(Constants.MB * 8)))
                .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)
                .setParallelTransferOptions(
                    new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB * 4));
        Response<BlockBlobItem> response = bc.uploadWithResponse(options, null, Context.NONE);
    }

    @Test
    public void uploadInputStream() {
        BlobParallelUploadOptions options = new BlobParallelUploadOptions(DATA.getDefaultInputStream())
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);
        Response<BlockBlobItem> response = bc.uploadWithResponse(options, null, Context.NONE);
    }

    @Test
    public void uploadFlux() {
        BlobParallelUploadOptions options = new BlobParallelUploadOptions(DATA.getDefaultFlux())
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);
        Response<BlockBlobItem> response = bc.uploadWithResponse(options, null, Context.NONE);
    }
}

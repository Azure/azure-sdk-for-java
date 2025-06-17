// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.contentValidation;

import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.structuredmessage.StorageChecksumAlgorithm;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.DataLakeTestBase;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.options.FileParallelUploadOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DataLakeMessageEncoderUploadTests extends DataLakeTestBase {
    private DataLakeFileClient fc;

    @BeforeEach
    public void setup() {
        fc = dataLakeFileSystemClient.createFile(generatePathName());
    }

    @Test
    public void uploadBinaryDataFull() {
        FileParallelUploadOptions options = new FileParallelUploadOptions(DATA.getDefaultBinaryData())
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);
        Response<PathInfo> response = fc.uploadWithResponse(options, null, Context.NONE);
    }

    @Test
    public void uploadBinaryDataChunked() {
        FileParallelUploadOptions options
            = new FileParallelUploadOptions(BinaryData.fromBytes(getRandomByteArray(Constants.MB * 8)))
                .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)
                .setParallelTransferOptions(
                    new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB * 4));
        Response<PathInfo> response = fc.uploadWithResponse(options, null, Context.NONE);
    }

    @Test
    public void uploadInputStream() {
        FileParallelUploadOptions options = new FileParallelUploadOptions(DATA.getDefaultInputStream())
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);
        Response<PathInfo> response = fc.uploadWithResponse(options, null, Context.NONE);
    }

    @Test
    public void uploadFlux() {
        FileParallelUploadOptions options = new FileParallelUploadOptions(DATA.getDefaultFlux())
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);
        Response<PathInfo> response = fc.uploadWithResponse(options, null, Context.NONE);
    }
}

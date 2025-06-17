// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.contentValidation;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.structuredmessage.StorageChecksumAlgorithm;
import com.azure.storage.file.share.FileShareTestBase;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.models.ShareFileUploadInfo;
import com.azure.storage.file.share.models.ShareFileUploadOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

public class FileShareMessageEncoderUploadTests extends FileShareTestBase {
    private ShareFileClient fc;

    @BeforeEach
    public void setup() {
        String shareName = generateShareName();
        ShareClient shareClient = shareBuilderHelper(shareName).buildClient();
        shareClient.create();
        fc = fileBuilderHelper(shareName, generatePathName()).buildFileClient();
    }

    @Test
    public void uploadInputStream() {
        ShareFileUploadOptions options = new ShareFileUploadOptions(DATA.getDefaultInputStream())
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);
        Response<ShareFileUploadInfo> response = fc.uploadWithResponse(options, null, Context.NONE);
    }

    @Test
    public void uploadInputStreamChunked() {
        byte[] randomData = getRandomByteArray(Constants.MB * 8);
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);

        ShareFileUploadOptions options
            = new ShareFileUploadOptions(input).setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)
                .setParallelTransferOptions(
                    new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB * 4));
        Response<ShareFileUploadInfo> response = fc.uploadWithResponse(options, null, Context.NONE);
    }

    @Test
    public void uploadFlux() {
        ShareFileUploadOptions options = new ShareFileUploadOptions(DATA.getDefaultFlux())
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);
        Response<ShareFileUploadInfo> response = fc.uploadWithResponse(options, null, Context.NONE);
    }
}

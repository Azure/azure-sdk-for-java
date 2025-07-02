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
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;

import static com.azure.storage.common.implementation.Constants.HeaderConstants.CONTENT_CRC64_HEADER_NAME;
import static com.azure.storage.common.implementation.Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME;
import static com.azure.storage.common.implementation.structuredmessage.StructuredMessageConstants.STRUCTUED_BODY_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    public void uploadInputStreamFullCRCHeader() {
        fc.create(DATA.getDefaultDataSize());
        ShareFileUploadOptions options = new ShareFileUploadOptions(DATA.getDefaultInputStream())
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        Response<ShareFileUploadInfo> response = fc.uploadWithResponse(options, null, Context.NONE);
        assertNotNull(response.getRequest().getHeaders().getValue(CONTENT_CRC64_HEADER_NAME));
    }

    @Test
    public void uploadInputStreamFullStructMess() {
        byte[] randomData = getRandomByteArray(Constants.MB * 5);
        fc.create(randomData.length);
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);
        ShareFileUploadOptions options
            = new ShareFileUploadOptions(input).setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);
        Response<ShareFileUploadInfo> response = fc.uploadWithResponse(options, null, Context.NONE);
        assertEquals(STRUCTUED_BODY_TYPE,
            response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
    }

    @Test
    public void uploadInputStreamChunkedStructMess() {
        byte[] randomData = getRandomByteArray(Constants.MB * 8);
        fc.create(randomData.length);
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);
        ShareFileUploadOptions options
            = new ShareFileUploadOptions(input).setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)
                .setParallelTransferOptions(
                    new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB * 4));

        Response<ShareFileUploadInfo> response = fc.uploadWithResponse(options, null, Context.NONE);
        assertEquals(STRUCTUED_BODY_TYPE,
            response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
    }

    @Test
    public void uploadFluxFullCRCHeader() {
        fc.create(DATA.getDefaultDataSize());
        ShareFileUploadOptions options = new ShareFileUploadOptions(DATA.getDefaultFlux())
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        Response<ShareFileUploadInfo> response = fc.uploadWithResponse(options, null, Context.NONE);
        assertNotNull(response.getRequest().getHeaders().getValue(CONTENT_CRC64_HEADER_NAME));
    }

    @Test
    public void uploadFluxFullStructMess() {
        fc.create(Constants.MB * 5);
        ShareFileUploadOptions options = new ShareFileUploadOptions(Flux.just(getRandomByteBuffer(Constants.MB * 5)))
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        Response<ShareFileUploadInfo> response = fc.uploadWithResponse(options, null, Context.NONE);
        assertEquals(STRUCTUED_BODY_TYPE,
            response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
    }

    @Test
    public void uploadFluxChunkedStructMess() {
        fc.create(Constants.MB * 8);
        ShareFileUploadOptions options = new ShareFileUploadOptions(Flux.just(getRandomByteBuffer(Constants.MB * 8)))
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB * 4));

        Response<ShareFileUploadInfo> response = fc.uploadWithResponse(options, null, Context.NONE);
        assertEquals(STRUCTUED_BODY_TYPE,
            response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
    }
}

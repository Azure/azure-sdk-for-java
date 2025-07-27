// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.contentValidation;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.structuredmessage.StorageChecksumAlgorithm;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.file.share.FileShareTestBase;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.models.ShareFileUploadInfo;
import com.azure.storage.file.share.models.ShareFileUploadOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import static com.azure.storage.common.implementation.Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME;
import static com.azure.storage.common.implementation.structuredmessage.StructuredMessageConstants.STRUCTURED_BODY_TYPE_VALUE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@LiveOnly
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
    public void uploadInputStreamFullStructMessSmall() {
        fc.create(DATA.getDefaultDataSize());

        ShareFileUploadOptions options = new ShareFileUploadOptions(DATA.getDefaultInputStream())
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        Response<ShareFileUploadInfo> response = fc.uploadWithResponse(options, null, Context.NONE);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        fc.download(outStream);
        assertArrayEquals(DATA.getDefaultBytes(), outStream.toByteArray());

        assertEquals(STRUCTURED_BODY_TYPE_VALUE,
            response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
    }

    @Test
    public void uploadInputStreamFullStructMess() {
        byte[] randomData = getRandomByteArray(Constants.MB * 5);
        fc.create(randomData.length);

        ByteArrayInputStream input = new ByteArrayInputStream(randomData);
        ShareFileUploadOptions options
            = new ShareFileUploadOptions(input).setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);
        Response<ShareFileUploadInfo> response = fc.uploadWithResponse(options, null, Context.NONE);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        fc.download(outStream);
        assertArrayEquals(randomData, outStream.toByteArray());

        assertEquals(STRUCTURED_BODY_TYPE_VALUE,
            response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
    }

    @Test
    public void uploadInputStreamChunkedStructMess() {
        byte[] randomData = getRandomByteArray(Constants.MB * 10);
        fc.create(randomData.length);

        ByteArrayInputStream input = new ByteArrayInputStream(randomData);
        ShareFileUploadOptions options
            = new ShareFileUploadOptions(input).setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)
                .setParallelTransferOptions(
                    new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB * 2)
                        .setBlockSizeLong((long) Constants.MB * 2));

        Response<ShareFileUploadInfo> response = fc.uploadWithResponse(options, null, Context.NONE);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        fc.download(outStream);
        assertArrayEquals(randomData, outStream.toByteArray());

        assertEquals(STRUCTURED_BODY_TYPE_VALUE,
            response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
    }

    @Test
    public void uploadFluxFullStructMessSmall() {
        //TODO (isbr): why does fileshare accept x-ms-content-crc64 when the notes say it shouldn't and its not in the spec
        fc.create(DATA.getDefaultDataSize());
        ShareFileUploadOptions options = new ShareFileUploadOptions(DATA.getDefaultFlux())
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        Response<ShareFileUploadInfo> response = fc.uploadWithResponse(options, null, Context.NONE);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        fc.download(outStream);
        assertArrayEquals(DATA.getDefaultBytes(), outStream.toByteArray());

        assertEquals(STRUCTURED_BODY_TYPE_VALUE,
            response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
    }

    @Test
    public void uploadFluxFullStructMess() {
        byte[] randomData = getRandomByteArray(Constants.MB * 5);
        fc.create(randomData.length);

        ShareFileUploadOptions options = new ShareFileUploadOptions(Flux.just(ByteBuffer.wrap(randomData)))
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        Response<ShareFileUploadInfo> response = fc.uploadWithResponse(options, null, Context.NONE);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        fc.download(outStream);
        assertArrayEquals(randomData, outStream.toByteArray());

        assertEquals(STRUCTURED_BODY_TYPE_VALUE,
            response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
    }

    @Test
    public void uploadFluxChunkedStructMess() {
        byte[] randomData = getRandomByteArray(Constants.MB * 10);
        fc.create(randomData.length);

        ShareFileUploadOptions options = new ShareFileUploadOptions(Flux.just(ByteBuffer.wrap(randomData)))
            .setStorageChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB * 2)
                    .setBlockSizeLong((long) Constants.MB * 2));

        Response<ShareFileUploadInfo> response = fc.uploadWithResponse(options, null, Context.NONE);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        fc.download(outStream);
        assertArrayEquals(randomData, outStream.toByteArray());

        assertEquals(STRUCTURED_BODY_TYPE_VALUE,
            response.getRequest().getHeaders().getValue(STRUCTURED_BODY_TYPE_HEADER_NAME));
    }
}

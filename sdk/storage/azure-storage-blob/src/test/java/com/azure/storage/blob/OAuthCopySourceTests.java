// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpAuthorization;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.options.AppendBlobAppendBlockFromUrlOptions;
import com.azure.storage.blob.options.BlobUploadFromUrlOptions;
import com.azure.storage.blob.options.BlockBlobStageBlockFromUrlOptions;
import com.azure.storage.blob.options.PageBlobUploadPagesFromUrlOptions;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class OAuthCopySourceTests extends BlobTestBase {
    private BlobClient defaultDataSourceBlobClient;
    private BlobClient pageBlobDataSourceBlobClient;

    private AppendBlobClient appendBlobClient;
    private BlockBlobClient blockBlobClient;
    private PageBlobClient pageBlobClient;

    @BeforeEach
    public void setup() {
        defaultDataSourceBlobClient = cc.getBlobClient(generateBlobName());
        defaultDataSourceBlobClient.upload(DATA.getDefaultBinaryData());
        pageBlobDataSourceBlobClient = cc.getBlobClient(generateBlobName());
        pageBlobDataSourceBlobClient.upload(BinaryData.fromBytes(getRandomByteArray(PageBlobClient.PAGE_BYTES)));

        appendBlobClient = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        appendBlobClient.create();

        blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        pageBlobClient = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        pageBlobClient.create(PageBlobClient.PAGE_BYTES);
    }

    // RBAC replication lag
    @Test
    public void appendBlobAppendBlockFromURLSourceOauth() {
        liveTestScenarioWithRetry(() -> {
            BlobClient sourceBlob = cc.getBlobClient(generateBlobName());
            sourceBlob.upload(DATA.getDefaultBinaryData());
            String oauthHeader = getAuthToken();
            appendBlobClient
                .appendBlockFromUrlWithResponse(new AppendBlobAppendBlockFromUrlOptions(sourceBlob.getBlobUrl())
                    .setSourceAuthorization(new HttpAuthorization("Bearer", oauthHeader)), null, Context.NONE);
            TestUtils.assertArraysEqual(appendBlobClient.downloadContent().toBytes(), DATA.getDefaultBytes());
        });
    }

    @Test
    public void appendBlobAppendBlockFromURLSourceOauthFail() {
        BlobClient sourceBlob = cc.getBlobClient(generateBlobName());
        sourceBlob.upload(DATA.getDefaultBinaryData());
        String oauthHeader = "garbage";

        assertThrows(BlobStorageException.class,
            () -> appendBlobClient
                .appendBlockFromUrlWithResponse(new AppendBlobAppendBlockFromUrlOptions(sourceBlob.getBlobUrl())
                    .setSourceAuthorization(new HttpAuthorization("Bearer", oauthHeader)), null, Context.NONE));
    }

    // RBAC replication lag
    @Test
    public void blockBlobUploadFromURLSourceOauth() {
        liveTestScenarioWithRetry(() -> {
            String oauthHeader = getAuthToken();
            blockBlobClient
                .uploadFromUrlWithResponse(new BlobUploadFromUrlOptions(defaultDataSourceBlobClient.getBlobUrl())
                    .setSourceAuthorization(new HttpAuthorization("Bearer", oauthHeader)), null, Context.NONE);
            TestUtils.assertArraysEqual(blockBlobClient.downloadContent().toBytes(), DATA.getDefaultBytes());
        });
    }

    @Test
    public void blockBlobUploadFromURLSourceOauthFail() {
        BlobClient sourceBlob = cc.getBlobClient(generateBlobName());
        sourceBlob.upload(DATA.getDefaultBinaryData());
        String oauthHeader = "garbage";

        assertThrows(BlobStorageException.class,
            () -> blockBlobClient.uploadFromUrlWithResponse(new BlobUploadFromUrlOptions(sourceBlob.getBlobUrl())
                .setSourceAuthorization(new HttpAuthorization("Bearer", oauthHeader)), null, Context.NONE));
    }

    // RBAC replication lag
    @Test
    public void blockBlobStageBlockFromURLSourceOauth() {
        liveTestScenarioWithRetry(() -> {
            String oauthHeader = getAuthToken();
            String blockId = Base64.getEncoder().encodeToString("myBlockId".getBytes());
            blockBlobClient.stageBlockFromUrlWithResponse(
                new BlockBlobStageBlockFromUrlOptions(blockId, defaultDataSourceBlobClient.getBlobUrl())
                    .setSourceAuthorization(new HttpAuthorization("Bearer", oauthHeader)),
                null, Context.NONE);
            blockBlobClient.commitBlockList(Collections.singletonList(blockId), true);
            TestUtils.assertArraysEqual(blockBlobClient.downloadContent().toBytes(), DATA.getDefaultBytes());
        });

    }

    @Test
    public void blockBlobStageBlockFromURLSourceOauthFail() {
        BlobClient sourceBlob = cc.getBlobClient(generateBlobName());
        sourceBlob.upload(DATA.getDefaultBinaryData());
        String oauthHeader = "garbage";
        String blockId = Base64.getEncoder().encodeToString("myBlockId".getBytes());

        assertThrows(BlobStorageException.class,
            () -> blockBlobClient
                .stageBlockFromUrlWithResponse(new BlockBlobStageBlockFromUrlOptions(blockId, sourceBlob.getBlobUrl())
                    .setSourceAuthorization(new HttpAuthorization("Bearer", oauthHeader)), null, Context.NONE));

    }

    // RBAC replication lag
    @Test
    public void uploadPagesFromURLSourceOauth() {
        liveTestScenarioWithRetry(() -> {
            PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
            String oauthHeader = getAuthToken();

            pageBlobClient.uploadPagesFromUrlWithResponse(
                new PageBlobUploadPagesFromUrlOptions(pageRange, pageBlobDataSourceBlobClient.getBlobUrl())
                    .setSourceAuthorization(new HttpAuthorization("Bearer", oauthHeader)),
                null, Context.NONE);

            TestUtils.assertArraysEqual(pageBlobClient.downloadContent().toBytes(),
                pageBlobDataSourceBlobClient.downloadContent().toBytes());
        });
    }

    @Test
    public void uploadPagesFromURLSourceOauthFail() {
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        String oauthHeader = "garbage";

        assertThrows(BlobStorageException.class,
            () -> pageBlobClient.uploadPagesFromUrlWithResponse(
                new PageBlobUploadPagesFromUrlOptions(pageRange, pageBlobDataSourceBlobClient.getBlobUrl())
                    .setSourceAuthorization(new HttpAuthorization("Bearer", oauthHeader)),
                null, Context.NONE));
    }
}

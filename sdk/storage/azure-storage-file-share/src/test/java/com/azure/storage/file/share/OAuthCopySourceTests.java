// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.HttpAuthorization;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.options.ShareFileUploadRangeFromUrlOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OAuthCopySourceTests extends FileShareTestBase {
    private ShareFileClient primaryFileClient;
    private String shareName;

    private BlobContainerClient container;
    private BlobClient blob;

    @BeforeEach
    public void setup() {
        shareName = generateShareName();
        String filePath = generatePathName();
        ShareClient shareClient = shareBuilderHelper(shareName).buildClient();
        shareClient.create();
        primaryFileClient = fileBuilderHelper(shareName, filePath).buildFileClient();

        container = getBlobContainer();
        blob = container.getBlobClient(generatePathName());
        blob.upload(DATA.getDefaultBinaryData());
    }

    @AfterEach
    public void cleanup() {
        container.delete();
    }

    private BlobContainerClient getBlobContainer() {
        return instrument(new BlobServiceClientBuilder()).endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .buildClient()
            .createBlobContainer(shareName);
    }

    @Test
    public void copyFromURLWithOAuthSource() {
        int retry = 0;
        // RBAC replication lag
        while (retry < 5 && !interceptorManager.isPlaybackMode()) {
            try {
                String oauthHeader = getAuthToken();
                primaryFileClient.create(DATA.getDefaultDataSizeLong());

                primaryFileClient.uploadRangeFromUrlWithResponse(
                    new ShareFileUploadRangeFromUrlOptions(DATA.getDefaultDataSizeLong(), blob.getBlobUrl())
                        .setSourceAuthorization(new HttpAuthorization("Bearer", oauthHeader)),
                    null, Context.NONE);

                ByteArrayOutputStream os = new ByteArrayOutputStream(DATA.getDefaultDataSize());
                primaryFileClient.download(os);
                assertArrayEquals(os.toByteArray(), DATA.getDefaultBytes());
                break;
            } catch (Exception ex) {
                // Retry delay
                sleepIfRunningAgainstService(30 * 1000);
            } finally {
                retry++;
            }
        }
        String oauthHeader = getAuthToken();
        primaryFileClient.create(DATA.getDefaultDataSizeLong());

        primaryFileClient.uploadRangeFromUrlWithResponse(
            new ShareFileUploadRangeFromUrlOptions(DATA.getDefaultDataSizeLong(), blob.getBlobUrl())
                .setSourceAuthorization(new HttpAuthorization("Bearer", oauthHeader)),
            null, Context.NONE);

        ByteArrayOutputStream os = new ByteArrayOutputStream(DATA.getDefaultDataSize());
        primaryFileClient.download(os);
        assertArrayEquals(os.toByteArray(), DATA.getDefaultBytes());
    }

    @Test
    public void copyFromURLWithOAuthSourceInvalidCredential() {
        String oauthHeader = "garbage";
        primaryFileClient.create(DATA.getDefaultDataSizeLong());

        assertThrows(ShareStorageException.class,
            () -> primaryFileClient.uploadRangeFromUrlWithResponse(
                new ShareFileUploadRangeFromUrlOptions(DATA.getDefaultDataSize(), blob.getBlobUrl())
                    .setSourceAuthorization(new HttpAuthorization("Bearer", oauthHeader)),
                null, Context.NONE));
    }

}

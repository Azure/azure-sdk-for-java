// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.ListBlobsOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SoftDeleteTests extends BlobTestBase {
    private BlobContainerClient containerClient;
    private BlobClient blobClient;

    @BeforeEach
    public void setup() {
        containerClient = softDeleteServiceClient.getBlobContainerClient(generateContainerName());
        containerClient.create();
        blobClient = containerClient.getBlobClient(generateBlobName());
        blobClient.getBlockBlobClient().upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
    }

    @AfterEach
    public void cleanup() {
        containerClient.delete();
    }

    @Test
    public void undeleteMin() {
        blobClient.delete();
        assertResponseStatusCode(blobClient.undeleteWithResponse(null, null), 200);
    }

    @Test
    public void undeleteSimple() {
        blobClient.delete();
        assertFalse(blobClient.exists());

        blobClient.undelete();
        assertTrue(blobClient.exists());
    }

    @Test
    public void undelete() {
        blobClient.delete();

        HttpHeaders undeleteHeaders = blobClient.undeleteWithResponse(null, null).getHeaders();
        blobClient.getProperties();

        assertNotNull(undeleteHeaders.getValue(X_MS_REQUEST_ID));
        assertNotNull(undeleteHeaders.getValue(X_MS_VERSION));
        assertNotNull(undeleteHeaders.getValue(HttpHeaderName.DATE));
    }

    @Test
    public void listBlobsFlatOptionsDeleted() {
        blobClient.delete();

        ListBlobsOptions options
            = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveDeletedBlobs(true)).setPrefix(prefix);
        Iterator<BlobItem> blobs = containerClient.listBlobs(options, null).iterator();

        assertEquals(blobClient.getBlobName(), blobs.next().getName());
        assertFalse(blobs.hasNext());
    }

    @Test
    public void listBlobsHierOptionsDeleted() {
        blobClient.delete();

        ListBlobsOptions options
            = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveDeletedBlobs(true)).setPrefix(prefix);
        Iterator<BlobItem> blobs = containerClient.listBlobsByHierarchy("", options, null).iterator();

        assertEquals(blobClient.getBlobName(), blobs.next().getName());
        assertFalse(blobs.hasNext());
    }
}

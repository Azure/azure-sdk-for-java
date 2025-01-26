// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.rest.Response;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.common.Flags;
import com.azure.storage.common.StructuredMessageEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

public class BlobMessageEncoderTests extends BlobTestBase {
    private BlobClient bc;

    @BeforeEach
    public void setup() {
        String blobName = generateBlobName();
        bc = cc.getBlobClient(blobName);
    }

    @Test
    public void isBodyAccepted() {
        byte[] randomData = "hello world".getBytes();
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);

        Response<BlockBlobItem> response = bc.uploadWithResponse(new BlobParallelUploadOptions(input), null, null);
        assertResponseStatusCode(response, 201);
    }
}

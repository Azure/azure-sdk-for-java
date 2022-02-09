// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.test.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.util.StreamUtils;

@SpringBootTest
public class StorageWriteIT {

    @Value("${blob}")
    private Resource blobStorage;

    @Value("${file}")
    private Resource fileStorage;

    @Test
    public void testWriteBlobStorage() throws IOException {
        String data = "test blob storage";
        try (OutputStream os = ((WritableResource) this.blobStorage).getOutputStream()) {
            os.write(data.getBytes());
        }
        String blobContent = StreamUtils.copyToString(this.blobStorage.getInputStream(),
            Charset.defaultCharset());
        assertEquals(data, blobContent);
    }

    @Test
    public void testWriteFileStorage() throws IOException {
        String data = "test file storage";
        try (OutputStream os = ((WritableResource) this.fileStorage).getOutputStream()) {
            os.write(data.getBytes());
        }
        String fileContent = StreamUtils.copyToString(this.fileStorage.getInputStream(),
            Charset.defaultCharset());

        assertEquals(data, fileContent.substring(0, data.length()));
    }
}

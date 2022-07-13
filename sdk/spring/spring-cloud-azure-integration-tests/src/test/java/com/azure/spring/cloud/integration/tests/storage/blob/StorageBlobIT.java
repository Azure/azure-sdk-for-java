// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests.storage.blob;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

@SpringBootTest
@ActiveProfiles("storage-blob")
public class StorageBlobIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageBlobIT.class);
    private final String data = "sample-data";

    @Value("${resource.blob}")
    private Resource storageBlobResource;

    @Test
    public void testStorageBlobOperation() throws IOException {
        LOGGER.info("StorageBlobIT begin.");
        try (OutputStream os = ((WritableResource)storageBlobResource).getOutputStream()) {
            os.write(data.getBytes());
        }
        String blobContent = StreamUtils.copyToString(storageBlobResource.getInputStream(), Charset.defaultCharset());
        Assertions.assertEquals(data, blobContent);
        LOGGER.info("StorageBlobIT end.");
    }
}

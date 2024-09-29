// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests.storage.fileshare;

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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("storage-fileshare")
public class StorageFileShareIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageFileShareIT.class);
    private static final String DATA = "sample-data";

    @Value("${resource.file}")
    private Resource storageFileResource;

    @Test
    public void testStorageFileShareOperation() throws IOException {
        LOGGER.info("StorageFileShareIT begin.");
        try (OutputStream os = ((WritableResource) storageFileResource).getOutputStream()) {
            os.write(DATA.getBytes());
        }
        String fileContent = StreamUtils.copyToString(storageFileResource.getInputStream(), Charset.defaultCharset());
        Assertions.assertEquals(DATA, fileContent.substring(0, DATA.length()));
        LOGGER.info("StorageFileShareIT end.");
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageConfiguration.class);
    private static final String BLOB_URL = "http://%s.blob.core.windows.net";
    private static final String BLOB_HTTPS_URL = "https://%s.blob.core.windows.net";

    private final StorageProperties properties;

    public StorageConfiguration(StorageProperties properties) {
        this.properties = properties;
    }

    private String getEndpoint() {
        if (properties.isUseEmulator()) {
            LOGGER.debug("Using emulator address instead..");
            return String.format("%s/%s", properties.getEmulatorBlobHost(), properties.getAccountName());
        }
        if (properties.isEnableHttps()) {
            return String.format(BLOB_HTTPS_URL, properties.getAccountName());
        }
        return String.format(BLOB_URL, properties.getAccountName());
    }

    @Bean
    public BlobContainerAsyncClient blobContainerAsyncClient() {
        final BlobServiceAsyncClient blobServiceAsyncClient = new BlobServiceClientBuilder()
            .endpoint(getEndpoint())
            .credential(new StorageSharedKeyCredential(properties.getAccountName(), properties.getAccountKey()))
            .buildAsyncClient();

        return blobServiceAsyncClient.getBlobContainerAsyncClient(properties.getContainerName());
    }

}

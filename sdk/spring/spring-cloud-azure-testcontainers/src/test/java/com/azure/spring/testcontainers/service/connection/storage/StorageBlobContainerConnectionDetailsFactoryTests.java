// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.testcontainers.service.connection.storage;

import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.storage.blob.AzureStorageBlobAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.storage.blob.AzureStorageBlobResourceAutoConfiguration;
import com.azure.storage.blob.BlobServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.StreamUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import static com.azure.spring.testcontainers.service.connection.storage.ApiVersionUtilTests.apiVersionIsAfterToday;
import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@Testcontainers
class StorageBlobContainerConnectionDetailsFactoryTests {
    @Container
    @ServiceConnection
    private static final GenericContainer<?> azurite = new GenericContainer<>(
        "mcr.microsoft.com/azure-storage/azurite:latest")
        .withExposedPorts(10000)
        .withCommand("azurite --skipApiVersionCheck && azurite -l /data --blobHost 0.0.0.0 --queueHost 0.0.0.0 --tableHost 0.0.0.0");

    @Value("azure-blob://testcontainers/message.txt")
    private Resource blobFile;

    @Autowired
    BlobServiceClient client;

    @Test
    void test() throws IOException {
        if (apiVersionIsAfterToday(client.getServiceVersion().getVersion())) {
            return;
        }
        try (OutputStream os = ((WritableResource) this.blobFile).getOutputStream()) {
            os.write("Hello World!".getBytes());
        }
        var content = StreamUtils.copyToString(this.blobFile.getInputStream(), Charset.defaultCharset());
        assertThat(content).isEqualTo("Hello World!");
    }

    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration(classes = {AzureGlobalPropertiesAutoConfiguration.class, AzureStorageBlobAutoConfiguration.class, AzureStorageBlobResourceAutoConfiguration.class})
    static class Config {
    }
}

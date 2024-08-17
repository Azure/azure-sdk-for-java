// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.testcontainers.implementation.service.connection.storage;

import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.storage.blob.AzureStorageBlobAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.storage.blob.AzureStorageBlobResourceAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@Testcontainers
@EnabledOnOs(OS.LINUX)
class StorageBlobContainerConnectionDetailsFactoryTests {
    @Container
    @ServiceConnection
    private static final GenericContainer<?> AZURITE_CONTAINER = new GenericContainer<>(
        "mcr.microsoft.com/azure-storage/azurite:latest")
        .withExposedPorts(10000)
        .withCommand("azurite --skipApiVersionCheck && azurite -l /data --blobHost 0.0.0.0 --queueHost 0.0.0.0 --tableHost 0.0.0.0");

    @Value("azure-blob://testcontainers/message.txt")
    private Resource blobFile;

    @Test
    void test() throws IOException {
        String originalContent = "Hello World!";
        try (OutputStream os = ((WritableResource) this.blobFile).getOutputStream()) {
            os.write(originalContent.getBytes());
        }
        String resultContent = StreamUtils.copyToString(this.blobFile.getInputStream(), Charset.defaultCharset());
        assertThat(resultContent).isEqualTo(originalContent);
    }

    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration(classes = {AzureGlobalPropertiesAutoConfiguration.class, AzureStorageBlobAutoConfiguration.class, AzureStorageBlobResourceAutoConfiguration.class})
    static class Config {
    }
}

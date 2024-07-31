// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.docker.compose.implementation.service.connection.storage;

import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.storage.blob.AzureStorageBlobAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.storage.blob.AzureStorageBlobResourceAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.docker.compose.skip.in-tests=false",
    "spring.docker.compose.file=classpath:com/azure/spring/cloud/docker/compose/implementation/service/connection/storage/storage-compose.yaml",
    "spring.docker.compose.stop.command=down"
})
class StorageBlobDockerComposeConnectionDetailsFactoryTests {

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
    @ImportAutoConfiguration(classes = {
        AzureGlobalPropertiesAutoConfiguration.class,
        AzureStorageBlobAutoConfiguration.class,
        AzureStorageBlobResourceAutoConfiguration.class})
    static class Config {
    }
}

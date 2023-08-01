// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class AzureStorageResourceTests {

    private AzureStorageResource storageResource;

    @BeforeEach
    public void setup() {
        storageResource = new MockAzureStorageResource(StorageType.BLOB);
    }


    @ParameterizedTest
    @MethodSource("validLocationsProvider")
    public void testGetContainerName(ArgumentsAccessor arguments) {
        String containerName = arguments.getString(1);
        String location = arguments.getString(0);
        assertEquals(containerName, storageResource.getContainerName(location));
    }

    @ParameterizedTest
    @MethodSource("validLocationsProvider")
    public void testGetFileName(ArgumentsAccessor arguments) {
        String fileName = arguments.getString(2);
        String location = arguments.getString(0);
        assertEquals(fileName, storageResource.getFilename(location));
    }


    @ParameterizedTest
    @MethodSource("contentTypeProvider")
    public void testGetContentType(ArgumentsAccessor arguments) {
        String location = arguments.getString(0);
        String contentType = arguments.getString(1);
        assertEquals(contentType, storageResource.getContentType(location));
    }

    /**
     * Provides a list of valid locations as parameters in the format of:
     * <p>
     * location -- container name -- blob name
     */
    static Stream<Arguments> contentTypeProvider() {
        return Stream.of(
            arguments("azure-blob://c/b/a.pdf", "application/pdf"),
            arguments("azure-BLOB://c/b/a.txt", "text/plain"),
            arguments("AZURE-BLOB://c/b/a.jpg", "image/jpeg"),
            arguments("azure-blob://c/b.unknown", null)
        );
    }

    /**
     * Provides a list of valid locations as parameters in the format of:
     * <p>
     * location -- container name -- blob name
     */
    static Stream<Arguments> validLocationsProvider() {
        return Stream.of(
            arguments("azure-blob://c/b/a", "c", "b/a"),
            arguments("azure-BLOB://c/b/a", "c", "b/a"),
            arguments("AZURE-BLOB://c/b/a/", "c", "b/a")
        );
    }

    /**
     * Mock class to test common method within {@code AzureStorageResource}
     */
    private static class MockAzureStorageResource extends AzureStorageResource {

        private final StorageType storageType;

        MockAzureStorageResource(StorageType storageType) {
            this.storageType = storageType;
        }

        @Override
        StorageType getStorageType() {
            return storageType;
        }


        @Override
        public OutputStream getOutputStream() throws IOException {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return null;
        }
    }

}

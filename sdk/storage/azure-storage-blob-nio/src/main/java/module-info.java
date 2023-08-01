// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.storage.blob.nio {

    requires com.azure.storage.blob;
    requires com.fasterxml.jackson.dataformat.xml;

    exports com.azure.storage.blob.nio;

    opens com.azure.storage.blob.nio to
        com.fasterxml.jackson.databind,
        com.azure.core;

    provides java.nio.file.spi.FileSystemProvider with com.azure.storage.blob.nio.AzureFileSystemProvider;
}

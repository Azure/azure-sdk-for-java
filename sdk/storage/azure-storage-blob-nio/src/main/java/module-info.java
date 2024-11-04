// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.storage.blob.nio {
    requires com.azure.storage.blob;

    exports com.azure.storage.blob.nio;

    opens com.azure.storage.blob.nio to com.azure.core;

    provides java.nio.file.spi.FileSystemProvider with com.azure.storage.blob.nio.AzureFileSystemProvider;
}

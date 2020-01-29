// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import java.nio.file.attribute.FileAttributeView;

/**
 * Provides support for properties specific to Azure Blob Storage such as tier.
 *
 * {@inheritDoc}
 */
public class AzureStorageFileAttributeView implements FileAttributeView {

    /**
     * Returns {@code "azureStorage"}
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return "azureStorage";
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.core.annotation.Immutable;

/** File list in Azure Blob Storage. */
@Immutable
public final class BlobFileListContentSource extends ContentSource {
    /*
     * Azure Blob Storage container URL.
     */
    private final String containerUrl;

    /*
     * Path to a JSONL file within the container specifying a subset of documents for training.
     */
    private final String fileList;

    /**
     * Creates an instance of BlobFileListContentSource class.
     *
     * @param containerUrl the containerUrl value to set.
     * @param fileList the fileList value to set.
     */
    public BlobFileListContentSource(String containerUrl, String fileList) {
        super(ContentSourceKind.AZURE_BLOB_FILE_LIST);
        this.containerUrl = containerUrl;
        this.fileList = fileList;
    }

    /**
     * Get the containerUrl property: Azure Blob Storage container URL.
     *
     * @return the containerUrl value.
     */
    public String getContainerUrl() {
        return this.containerUrl;
    }

    /**
     * Get the fileList property: Path to a JSONL file within the container specifying a subset of documents for
     * training.
     *
     * @return the fileList value.
     */
    public String getFileList() {
        return this.fileList;
    }

    @Override
    public ContentSourceKind getKind() {
        return ContentSourceKind.AZURE_BLOB_FILE_LIST;
    }
}

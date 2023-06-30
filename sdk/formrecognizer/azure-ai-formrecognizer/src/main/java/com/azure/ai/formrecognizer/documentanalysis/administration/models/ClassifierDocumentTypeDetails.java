// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.core.annotation.Fluent;

/** Training data source. */
@Fluent
public final class ClassifierDocumentTypeDetails {
    /*
     * Azure Blob Storage location containing the training data.
     */
    private AzureBlobContentSource azureBlobSource;

    /*
     * Azure Blob Storage file list specifying the training data.
     */
    private AzureBlobFileListContentSource azureBlobFileListContentSource;

    /** Creates an instance of ClassifierDocumentTypeDetails class. */
    public ClassifierDocumentTypeDetails() {}

    /**
     * Get the Azure Blob Storage location containing the training data.
     *
     * @return the azureBlobSource value.
     */
    public AzureBlobContentSource getAzureBlobSource() {
        return this.azureBlobSource;
    }

    /**
     * Set the Azure Blob Storage location containing the training data.
     *
     * @param azureBlobSource the azureBlobSource value to set.
     * @return the ClassifierDocumentTypeDetails object itself.
     */
    public ClassifierDocumentTypeDetails setAzureBlobSource(AzureBlobContentSource azureBlobSource) {
        this.azureBlobSource = azureBlobSource;
        return this;
    }

    /**
     * Get the Azure Blob Storage file list specifying the training data.
     *
     * @return the azureBlobFileListContentSource value.
     */
    public AzureBlobFileListContentSource getAzureBlobFileListSource() {
        return this.azureBlobFileListContentSource;
    }

    /**
     * Set the Azure Blob Storage file list specifying the training data.
     *
     * @param azureBlobFileListContentSource the azureBlobFileListContentSource value to set.
     * @return the ClassifierDocumentTypeDetails object itself.
     */
    public ClassifierDocumentTypeDetails setAzureBlobFileListSource(
        AzureBlobFileListContentSource azureBlobFileListContentSource) {
        this.azureBlobFileListContentSource = azureBlobFileListContentSource;
        return this;
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.core.annotation.Fluent;

/**
 * Model representing details for classifier document types info.
 */
@Fluent
public final class ClassifierDocumentTypeDetails {
    private final ContentSource contentSource;

    /**
     * Creates an instance of ClassifierDocumentTypeDetails class.
     *
     * @param source the source of the training data.
     */
    public ClassifierDocumentTypeDetails(ContentSource source) {
        this.contentSource = source;
    }

    /**
     * Get the source of the data.
     * It can be a {@link BlobContentSource} or a {@link BlobFileListContentSource}.
     * @return the ContentSource value.
     */
    public ContentSource getContentSource() {
        return contentSource;
    }
}

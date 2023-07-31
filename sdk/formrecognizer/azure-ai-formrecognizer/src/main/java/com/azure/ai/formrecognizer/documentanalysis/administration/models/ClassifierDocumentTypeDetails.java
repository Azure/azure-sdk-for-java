// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.ai.formrecognizer.documentanalysis.models.ContentSourceKind;
import com.azure.ai.formrecognizer.documentanalysis.models.TrainingDataContentSource;
import com.azure.core.annotation.Fluent;

/**
 * Model representing details for classifier document types info.
 */
@Fluent
public final class ClassifierDocumentTypeDetails {
    private final TrainingDataContentSource trainingDataContentSource;
    private final ContentSourceKind sourceKind;

    /**
     * Creates an instance of ClassifierDocumentTypeDetails class.
     *
     * @param source the source of the training data.
     */
    public ClassifierDocumentTypeDetails(TrainingDataContentSource source, ContentSourceKind sourceKind) {
        this.trainingDataContentSource = source;
        this.sourceKind = sourceKind;
    }

    /**
     * Get the trainingDataContentSource property: The source of the training data.
     * It can be a {@link BlobContentSource} or a {@link BlobFileListContentSource}.
     * @return the trainingDataContentSource value.
     */
    public TrainingDataContentSource getTrainingDataContentSource() {
        return trainingDataContentSource;
    }

    /**
     * Get type of training data source.
     * @return the sourceKind value.
     */
    public ContentSourceKind getSourceKind() {
        return sourceKind;
    }
}

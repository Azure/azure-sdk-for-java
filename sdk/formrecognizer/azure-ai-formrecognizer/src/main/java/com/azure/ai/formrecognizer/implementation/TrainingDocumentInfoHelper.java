// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.ai.formrecognizer.training.models.TrainingDocumentInfo;

/**
 * The helper class to set the non-public properties of an {@link TrainingDocumentInfo} instance.
 */
public final class TrainingDocumentInfoHelper {
    private static TrainingDocumentInfoAccessor accessor;

    private TrainingDocumentInfoHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link TrainingDocumentInfo} instance.
     */
    public interface TrainingDocumentInfoAccessor {
        void setModelId(TrainingDocumentInfo documentInfo, String modelId);
    }

    /**
     * The method called from {@link TrainingDocumentInfo} to set it's accessor.
     *
     * @param documentInfoAccessor The accessor.
     */
    public static void setAccessor(final TrainingDocumentInfoAccessor documentInfoAccessor) {
        accessor = documentInfoAccessor;
    }

    public static void setModelId(TrainingDocumentInfo documentInfo, String modelId) {
        accessor.setModelId(documentInfo, modelId);
    }
}

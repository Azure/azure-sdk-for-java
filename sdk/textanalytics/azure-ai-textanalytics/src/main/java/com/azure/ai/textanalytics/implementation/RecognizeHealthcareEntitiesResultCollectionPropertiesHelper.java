// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.RecognizeHealthcareEntitiesResultCollection;

/**
 * The helper class to set the non-public properties of an {@link RecognizeHealthcareEntitiesResultCollection} instance.
 */
public final class RecognizeHealthcareEntitiesResultCollectionPropertiesHelper {
    private static RecognizeHealthcareEntitiesResultCollectionAccessor accessor;

    private RecognizeHealthcareEntitiesResultCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an
     * {@link RecognizeHealthcareEntitiesResultCollection} instance.
     */
    public interface RecognizeHealthcareEntitiesResultCollectionAccessor {
        void setModelVersion(RecognizeHealthcareEntitiesResultCollection healthcareEntitiesResultCollection,
            String modelVersion);
        void setStatistics(RecognizeHealthcareEntitiesResultCollection healthcareEntitiesResultCollection,
            TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link RecognizeHealthcareEntitiesResultCollection} to set it's accessor.
     *
     * @param recognizeHealthcareEntitiesResultCollectionAccessor The accessor.
     */
    public static void setAccessor(final RecognizeHealthcareEntitiesResultCollectionAccessor
        recognizeHealthcareEntitiesResultCollectionAccessor) {
        accessor = recognizeHealthcareEntitiesResultCollectionAccessor;
    }


    public static void setModelVersion(RecognizeHealthcareEntitiesResultCollection healthcareEntitiesResultCollection,
        String modelVersion) {
        accessor.setModelVersion(healthcareEntitiesResultCollection, modelVersion);
    }

    public static void setStatistics(RecognizeHealthcareEntitiesResultCollection healthcareEntitiesResultCollection,
        TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(healthcareEntitiesResultCollection, statistics);
    }
}

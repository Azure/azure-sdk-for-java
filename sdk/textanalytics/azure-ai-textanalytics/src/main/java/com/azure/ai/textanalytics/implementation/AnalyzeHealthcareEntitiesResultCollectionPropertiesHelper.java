// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesResultCollection;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link AnalyzeHealthcareEntitiesResultCollection} instance.
 */
public final class AnalyzeHealthcareEntitiesResultCollectionPropertiesHelper {
    private static AnalyzeHealthcareEntitiesResultCollectionAccessor accessor;

    private AnalyzeHealthcareEntitiesResultCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnalyzeHealthcareEntitiesResultCollection}
     * instance.
     */
    public interface AnalyzeHealthcareEntitiesResultCollectionAccessor {
        void setModelVersion(AnalyzeHealthcareEntitiesResultCollection healthcareEntitiesResultCollection,
            String modelVersion);
        void setStatistics(AnalyzeHealthcareEntitiesResultCollection analyzeHealthcareEntitiesResultCollection,
            TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link AnalyzeHealthcareEntitiesResultCollection} to set it's accessor.
     *
     * @param analyzeHealthcareEntitiesResultCollectionAccessor The accessor.
     */
    public static void setAccessor(final AnalyzeHealthcareEntitiesResultCollectionAccessor analyzeHealthcareEntitiesResultCollectionAccessor) {
        accessor = analyzeHealthcareEntitiesResultCollectionAccessor;
    }

    public static void setModelVersion(AnalyzeHealthcareEntitiesResultCollection healthcareEntitiesResultCollection,
        String modelVersion) {
        accessor.setModelVersion(healthcareEntitiesResultCollection, modelVersion);
    }

    public static void setStatistics(AnalyzeHealthcareEntitiesResultCollection analyzeHealthcareEntitiesResultCollection,
        TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(analyzeHealthcareEntitiesResultCollection, statistics);
    }
}

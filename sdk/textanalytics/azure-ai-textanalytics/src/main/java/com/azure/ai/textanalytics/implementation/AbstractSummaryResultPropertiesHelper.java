// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AbstractiveSummary;
import com.azure.ai.textanalytics.models.AbstractSummaryResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link AbstractSummaryResult} instance.
 */
public final class AbstractSummaryResultPropertiesHelper {
    private static AbstractSummaryResultAccessor accessor;

    private AbstractSummaryResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AbstractSummaryResult}
     * instance.
     */
    public interface AbstractSummaryResultAccessor {
        void setDetectedLanguage(AbstractSummaryResult documentResult, DetectedLanguage detectedLanguage);
        void setWarnings(AbstractSummaryResult documentResult, IterableStream<TextAnalyticsWarning> warnings);
        void setSummaries(AbstractSummaryResult documentResult, IterableStream<AbstractiveSummary> summaries);
    }

    /**
     * The method called from {@link AbstractSummaryResult} to set it's accessor.
     *
     * @param abstractSummaryResultAccessor The accessor.
     */
    public static void setAccessor(final AbstractSummaryResultAccessor abstractSummaryResultAccessor) {
        accessor = abstractSummaryResultAccessor;
    }

    public static void setDetectedLanguage(AbstractSummaryResult documentResult, DetectedLanguage detectedLanguage) {
        accessor.setDetectedLanguage(documentResult, detectedLanguage);
    }

    public static void setWarnings(AbstractSummaryResult documentResult,
        IterableStream<TextAnalyticsWarning> warnings) {
        accessor.setWarnings(documentResult, warnings);
    }

    public static void setSummaries(AbstractSummaryResult documentResult,
        IterableStream<AbstractiveSummary> summaries) {
        accessor.setSummaries(documentResult, summaries);
    }
}

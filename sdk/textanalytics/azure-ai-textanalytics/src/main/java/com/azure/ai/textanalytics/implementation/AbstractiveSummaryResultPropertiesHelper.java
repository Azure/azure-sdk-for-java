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
public final class AbstractiveSummaryResultPropertiesHelper {
    private static AbstractiveSummaryResultAccessor accessor;

    private AbstractiveSummaryResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AbstractSummaryResult}
     * instance.
     */
    public interface AbstractiveSummaryResultAccessor {
        void setDetectedLanguage(AbstractSummaryResult summaryResult, DetectedLanguage detectedLanguage);
        void setWarnings(AbstractSummaryResult summaryResult, IterableStream<TextAnalyticsWarning> warnings);
        void setSummaries(AbstractSummaryResult summaryResult, IterableStream<AbstractiveSummary> summaries);
    }

    /**
     * The method called from {@link AbstractSummaryResult} to set it's accessor.
     *
     * @param abstractiveSummaryResultAccessor The accessor.
     */
    public static void setAccessor(final AbstractiveSummaryResultAccessor abstractiveSummaryResultAccessor) {
        accessor = abstractiveSummaryResultAccessor;
    }

    public static void setDetectedLanguage(AbstractSummaryResult summaryResult, DetectedLanguage detectedLanguage) {
        accessor.setDetectedLanguage(summaryResult, detectedLanguage);
    }

    public static void setWarnings(AbstractSummaryResult summaryResult,
        IterableStream<TextAnalyticsWarning> warnings) {
        accessor.setWarnings(summaryResult, warnings);
    }

    public static void setSummaries(AbstractSummaryResult summaryResult,
        IterableStream<AbstractiveSummary> summaries) {
        accessor.setSummaries(summaryResult, summaries);
    }
}

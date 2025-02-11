// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ExtractiveSummaryResult;
import com.azure.ai.textanalytics.models.ExtractiveSummarySentence;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link ExtractiveSummaryResult} instance.
 */
public final class ExtractiveSummaryResultPropertiesHelper {
    private static ExtractiveSummaryResultAccessor accessor;

    private ExtractiveSummaryResultPropertiesHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link ExtractiveSummaryResult} instance.
     */
    public interface ExtractiveSummaryResultAccessor {
        void setSentences(ExtractiveSummaryResult documentResult, IterableStream<ExtractiveSummarySentence> sentences);

        void setWarnings(ExtractiveSummaryResult documentResult, IterableStream<TextAnalyticsWarning> warnings);
    }

    /**
     * The method called from {@link ExtractiveSummaryResult} to set it's accessor.
     *
     * @param extractiveSummaryResultAccessor The accessor.
     */
    public static void setAccessor(final ExtractiveSummaryResultAccessor extractiveSummaryResultAccessor) {
        accessor = extractiveSummaryResultAccessor;
    }

    public static void setSentences(ExtractiveSummaryResult documentResult,
        IterableStream<ExtractiveSummarySentence> sentences) {
        accessor.setSentences(documentResult, sentences);
    }

    public static void setWarnings(ExtractiveSummaryResult documentResult,
        IterableStream<TextAnalyticsWarning> warnings) {
        accessor.setWarnings(documentResult, warnings);
    }
}

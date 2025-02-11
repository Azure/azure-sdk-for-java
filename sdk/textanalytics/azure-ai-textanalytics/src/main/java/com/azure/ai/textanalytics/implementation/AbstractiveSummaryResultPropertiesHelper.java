// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AbstractiveSummary;
import com.azure.ai.textanalytics.models.AbstractiveSummaryResult;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link AbstractiveSummaryResult} instance.
 */
public final class AbstractiveSummaryResultPropertiesHelper {
    private static AbstractiveSummaryResultAccessor accessor;

    private AbstractiveSummaryResultPropertiesHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link AbstractiveSummaryResult}
     * instance.
     */
    public interface AbstractiveSummaryResultAccessor {
        void setWarnings(AbstractiveSummaryResult documentResult, IterableStream<TextAnalyticsWarning> warnings);

        void setSummaries(AbstractiveSummaryResult documentResult, IterableStream<AbstractiveSummary> summaries);
    }

    /**
     * The method called from {@link AbstractiveSummaryResult} to set it's accessor.
     *
     * @param abstractiveSummaryResultAccessor The accessor.
     */
    public static void setAccessor(final AbstractiveSummaryResultAccessor abstractiveSummaryResultAccessor) {
        accessor = abstractiveSummaryResultAccessor;
    }

    public static void setWarnings(AbstractiveSummaryResult documentResult,
        IterableStream<TextAnalyticsWarning> warnings) {
        accessor.setWarnings(documentResult, warnings);
    }

    public static void setSummaries(AbstractiveSummaryResult documentResult,
        IterableStream<AbstractiveSummary> summaries) {
        accessor.setSummaries(documentResult, summaries);
    }
}

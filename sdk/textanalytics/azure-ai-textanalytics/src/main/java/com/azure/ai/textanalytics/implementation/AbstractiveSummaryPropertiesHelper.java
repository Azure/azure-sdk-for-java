// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AbstractiveSummary;
import com.azure.ai.textanalytics.models.SummaryContext;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link AbstractiveSummary} instance.
 */
public final class AbstractiveSummaryPropertiesHelper {
    private static AbstractiveSummaryAccessor accessor;

    private AbstractiveSummaryPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AbstractiveSummary}
     * instance.
     */
    public interface AbstractiveSummaryAccessor {
        void setText(AbstractiveSummary abstractiveSummary, String text);
        void setSummaryContexts(AbstractiveSummary abstractiveSummary, List<SummaryContext> summaryContexts);
    }

    /**
     * The method called from {@link AbstractiveSummary} to set it's accessor.
     *
     * @param abstractiveSummaryAccessor The accessor.
     */
    public static void setAccessor(final AbstractiveSummaryAccessor abstractiveSummaryAccessor) {
        accessor = abstractiveSummaryAccessor;
    }

    public static void setText(AbstractiveSummary abstractiveSummary, String text) {
        accessor.setText(abstractiveSummary, text);
    }


    public static void setSummaryContexts(AbstractiveSummary abstractiveSummary, List<SummaryContext> summaryContexts) {
        accessor.setSummaryContexts(abstractiveSummary, summaryContexts);
    }
}

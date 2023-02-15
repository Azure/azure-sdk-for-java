// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.SummaryContext;

/**
 * The helper class to set the non-public properties of an {@link SummaryContext} instance.
 */
public final class SummaryContextPropertiesHelper {
    private static SummaryContextAccessor accessor;

    private SummaryContextPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link SummaryContext}
     * instance.
     */
    public interface SummaryContextAccessor {
        void setOffset(SummaryContext summaryContext, int offset);
        void setLength(SummaryContext summaryContext, int length);
    }

    /**
     * The method called from {@link SummaryContext} to set it's accessor.
     *
     * @param summaryContextAccessor The accessor.
     */
    public static void setAccessor(
        final SummaryContextAccessor summaryContextAccessor) {
        accessor = summaryContextAccessor;
    }

    public static void setOffset(SummaryContext summaryContext, int offset) {
        accessor.setOffset(summaryContext, offset);
    }

    public static void setLength(SummaryContext summaryContext, int length) {
        accessor.setLength(summaryContext, length);
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.SummarySentence;

/**
 * The helper class to set the non-public properties of an {@link SummarySentence} instance.
 */
public final class SummarySentencePropertiesHelper {
    private static SummarySentenceAccessor accessor;

    private SummarySentencePropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link SummarySentence} instance.
     */
    public interface SummarySentenceAccessor {
        void setText(SummarySentence summarySentence, String text);
        void setRankScore(SummarySentence summarySentence, double rankScore);
        void setOffset(SummarySentence summarySentence, int offset);
        void setLength(SummarySentence summarySentence, int length);
    }

    /**
     * The method called from {@link SummarySentence} to set it's accessor.
     *
     * @param summarySentenceAccessor The accessor.
     */
    public static void setAccessor(final SummarySentenceAccessor summarySentenceAccessor) {
        accessor = summarySentenceAccessor;
    }

    public static void setText(SummarySentence summarySentence, String text) {
        accessor.setText(summarySentence, text);
    }

    public static void setRankScore(SummarySentence summarySentence, double rankScore) {
        accessor.setRankScore(summarySentence, rankScore);
    }

    public static void setOffset(SummarySentence summarySentence, int offset) {
        accessor.setOffset(summarySentence, offset);
    }

    public static void setLength(SummarySentence summarySentence, int length) {
        accessor.setLength(summarySentence, length);
    }
}
